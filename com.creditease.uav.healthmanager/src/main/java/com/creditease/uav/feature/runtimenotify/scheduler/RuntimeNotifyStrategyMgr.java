/*-
 * <<
 * UAVStack
 * ==
 * Copyright (C) 2016 - 2017 UAVStack
 * ==
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * >>
 */

package com.creditease.uav.feature.runtimenotify.scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.spi.AbstractTimerWork;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.feature.RuntimeNotifyCatcher;
import com.creditease.uav.feature.runtimenotify.NotifyStrategy;

/**
 * 
 * RuntimeNotifyStrategyMgr description:
 * 
 * 1. provide Strategy function
 * 
 * 2. get the strategy setting interval
 *
 */
public class RuntimeNotifyStrategyMgr extends AbstractTimerWork {

    public static final String UAV_CACHE_REGION = "store.region.uav";
    public static final String RT_STRATEGY_KEY = "runtime.strategy";

    private Map<String, NotifyStrategy> iScope = new ConcurrentHashMap<>();
    private Map<String, NotifyStrategy> mScope = new ConcurrentHashMap<>();
    private Map<String, NotifyStrategy> fScope = new ConcurrentHashMap<>();
    private Map<String, List<String>> multiInsts = new HashMap<>();
    private HashSet<NotifyStrategy> strategies = new HashSet<>();

    private Lock strategyLock = new ReentrantLock();

    private CacheManager cm;

    public RuntimeNotifyStrategyMgr(String cName, String feature) {
        super(cName, feature);
    }

    public RuntimeNotifyStrategyMgr(String cName, String feature, CacheManager cm) {
        super(cName, feature);

        this.cm = cm;
    }

    public void setCacheManager(CacheManager cm) {

        this.cm = cm;
    }

    public HashSet<NotifyStrategy> getStrategies() {

        return strategies;
    }

    /**
     * only for development load from local configuration file
     * 
     * @param json
     */
    @SuppressWarnings("unchecked")
    public void loadStrategy(String json) {

        Map<String, Object> stras = JSONHelper.toObject(json, Map.class);
        for (Entry<String, Object> entry : stras.entrySet()) {
            String key = entry.getKey();
            Map<String, Object> m = (Map<String, Object>) entry.getValue();

            NotifyStrategy stra = new NotifyStrategy(key, JSONHelper.toString(m));

            if (log.isDebugEnable()) {
                log.debug(this, "Parse NotifyStrategy: " + JSONHelper.toString(stra));
            }

            if ("I".equals(stra.getScope()) && !stra.getInstances().isEmpty()) {
                // deal with multi instances
                putMultiInstStrategy(key, stra);
                continue;
            }

            putStrategy(key, stra);
        }
    }

    /**
     * updateStrategy
     * 
     * @param jsonStrategy
     *            {<Strategy key>:{<Strategy body>},...}
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean updateStrategy(String jsonStrategy) {

        Map<String, Object> stras = JSONHelper.toObject(jsonStrategy, Map.class);

        Map<String, String> fieldValues = new HashMap<String, String>();

        for (Entry<String, Object> entry : stras.entrySet()) {
            String key = entry.getKey();
            Map<String, Object> m = (Map<String, Object>) entry.getValue();

            String str = JSONHelper.toString(m);

            fieldValues.put(key, str);
        }

        cm.putHash(UAV_CACHE_REGION, RT_STRATEGY_KEY, fieldValues);

        return true;
    }

    /**
     * query Strategy
     * 
     * @param strategyQueryObject
     *            {keys:"xxx,yyy"} 逗号分隔key，查询多个key {keys:""} 空串代表全部 {keys:"zzz*,www*"} 带*代表过滤匹配,*只能放在最后
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> queryStrategy(String strategyQueryObject) {

        Map<String, Object> qObj = JSONHelper.toObject(strategyQueryObject, Map.class);

        Map<String, String> strategyMap = Collections.emptyMap();

        String keysStr = (String) qObj.get("keys");

        if (keysStr.equalsIgnoreCase("")) {
            strategyMap = cm.getHashAll(UAV_CACHE_REGION, RT_STRATEGY_KEY);

            return strategyMap;
        }

        boolean isFilter = (keysStr.indexOf("*") > 0) ? true : false;

        String[] keys = keysStr.split(",");

        if (isFilter == false) {
            strategyMap = cm.getHash(UAV_CACHE_REGION, RT_STRATEGY_KEY, keys);
        }
        else {
            strategyMap = cm.getHashAll(UAV_CACHE_REGION, RT_STRATEGY_KEY);

            Map<String, String> filteredMap = new HashMap<String, String>();

            for (int i = 0; i < keys.length; i++) {
                keys[i] = keys[i].substring(0, keys[i].length() - 1);
            }

            for (String skey : strategyMap.keySet()) {

                for (int i = 0; i < keys.length; i++) {
                    if (skey.indexOf(keys[i]) > -1) {
                        filteredMap.put(skey, strategyMap.get(skey));
                        break;
                    }
                    String svalue = strategyMap.get(skey);
                    if (svalue.indexOf(keys[i]) >= 0) {
                        filteredMap.put(skey, strategyMap.get(skey));
                        break;
                    }
                }
            }

            strategyMap = filteredMap;
        }

        return strategyMap;
    }

    /**
     * removeStrategy
     * 
     * @param key
     * @return
     */
    public boolean removeStrategy(String key) {

        cm.delHash(UAV_CACHE_REGION, RT_STRATEGY_KEY, key);
        return true;
    }

    /**
     * to sync the strategy setting from cache
     */
    @Override
    public void run() {

        Map<String, String> strategyMap = cm.getHashAll(UAV_CACHE_REGION, RT_STRATEGY_KEY);

        try {
            strategyLock.lockInterruptibly();

            fScope.clear();
            mScope.clear();
            iScope.clear();
            multiInsts.clear();
            strategies.clear();

            if (null == strategyMap || strategyMap.isEmpty()) {
                return;
            }

            for (String key : strategyMap.keySet()) {
                String json = strategyMap.get(key);
                NotifyStrategy stra = new NotifyStrategy(key, json);

                if (log.isDebugEnable()) {
                    log.debug(this, "Parse NotifyStrategy: " + JSONHelper.toString(stra));
                }

                if ("I".equals(stra.getScope()) && !stra.getInstances().isEmpty()) {
                    // deal with multi instances
                    putMultiInstStrategy(key, stra);
                    continue;
                }

                putStrategy(key, stra);

            }
        }
        catch (InterruptedException e) {
            // ignore
        }
        finally {
            strategyLock.unlock();
        }
    }

    private void putMultiInstStrategy(String key, NotifyStrategy stra) {

        List<String> instances = stra.getInstances();

        int f = key.indexOf(RuntimeNotifyCatcher.STRATEGY_SEPARATOR);
        int m = key.indexOf(RuntimeNotifyCatcher.STRATEGY_SEPARATOR, f + 1);
        String prefix = key.substring(0, m + 1);

        List<String> okeys = multiInsts.get(key);
        if (okeys != null) {
            for (String okey : okeys) {
                iScope.remove(okey);
            }
        }

        List<String> nkeys = new ArrayList<>();
        for (String inst : instances) {
            String nkey = prefix + inst;
            iScope.put(nkey, stra);
            nkeys.add(nkey);
        }

        multiInsts.put(key, nkeys);
        strategies.add(stra);
    }

    /**
     * find out if there is a Strategy for the slice
     * 
     * @param slice
     * @return
     */
    public NotifyStrategy seekStrategy(String sliceKey) {

        try {
            boolean check = strategyLock.tryLock(10, TimeUnit.SECONDS);

            if (!check) {
                log.warn(this, "Try to get lock of strategyLock failed: state=" + check);
            }

            Set<String> list = iScope.keySet();
            for (String k : list) {
                if (sliceKey.startsWith(k)) {
                    return iScope.get(k);
                }
            }

            list = mScope.keySet();
            for (String k : list) {
                if (sliceKey.startsWith(k)) {
                    return mScope.get(k);
                }
            }

            list = fScope.keySet();
            for (String k : list) {
                if (sliceKey.startsWith(k)) {
                    return fScope.get(k);
                }
            }
        }
        catch (InterruptedException e) {
            // ignore
        }
        finally {
            strategyLock.unlock();
        }

        return null;
    }

    private void putStrategy(String key, NotifyStrategy stra) {

        if ("I".equals(stra.getScope())) {
            iScope.put(key, stra);
            List<String> instances = new ArrayList<String>();
            instances.add(key.substring(key.lastIndexOf('@') + 1));
            stra.setInstances(instances);
        }
        else if ("M".equals(stra.getScope())) {
            mScope.put(key, stra);
        }
        else if ("F".equals(stra.getScope())) {
            fScope.put(key, stra);
        }
        else {
            log.err(this, "UNKNOWN strategy scope: " + stra.getScope() + ", key: " + key);
            return;
        }
        strategies.add(stra);
    }
}
