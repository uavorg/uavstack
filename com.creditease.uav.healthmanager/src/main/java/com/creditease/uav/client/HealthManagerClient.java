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

package com.creditease.uav.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.heartbeat.api.HeartBeatProtocol;
import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.feature.healthmanager.HealthManagerConstants;
import com.creditease.uav.feature.runtimenotify.Slice;
import com.creditease.uav.feature.runtimenotify.scheduler.RuntimeNotifyStrategyMgr;

public class HealthManagerClient {

    public static void main(String[] args) {

        switch (args[0]) {
            case "profilecache":
                loadProfileCache(args[1]);
                break;
            case "nodecache":
                loadNodeInfoCache(args[1]);
                break;
            case "rtstgy":
                loadRuntimeStrategy(args[1]);
                break;
            case "rtslice":
                loadSlices(args[1], Long.parseLong(args[2]), args[3]);
                break;
        }
    }

    private static void loadSlices(String ip, long range, String key) {

        CacheManager cm = getCM(ip);

        StringBuilder sb = new StringBuilder();

        long end = System.currentTimeMillis();
        long start = end - range * 1000;

        int cacheLen = cm.llen("store.region.uav", key);

        sb.append("range:" + range + "\n\r");
        sb.append("key:" + key + "\n\r");
        sb.append("len:" + cacheLen + "\n\r");

        List<Slice> list = new ArrayList<>();

        for (int index = cacheLen - 1; index > 0; index--) {

            String sj = cm.lindex("store.region.uav", key, index);

            Slice r = new Slice(sj);

            if (r.getTime() <= end && r.getTime() >= start) {
                list.add(r);
                continue;
            }

            if (r.getTime() < start) {
                break;
            }
        }

        Collections.reverse(list);
        sb.append("sellen:" + list.size() + "\n\r");

        for (Slice slice : list) {
            sb.append(JSONHelper.toString(slice) + "\n\r");
        }

        try {
            IOHelper.writeTxtFile(IOHelper.getCurrentPath() + "/rtslice_cache.txt", sb.toString(), "utf-8", false);
        }
        catch (IOException e) {

            e.printStackTrace();
        }
    }

    private static void loadRuntimeStrategy(String ip) {

        CacheManager cm = getCM(ip);

        Map<String, String> strategyMap = cm.getHashAll(RuntimeNotifyStrategyMgr.UAV_CACHE_REGION,
                RuntimeNotifyStrategyMgr.RT_STRATEGY_KEY);

        String dat = JSONHelper.toString(strategyMap);

        try {
            IOHelper.writeTxtFile(IOHelper.getCurrentPath() + "/rtstgy_cache.txt", dat, "utf-8", false);
        }
        catch (IOException e) {

            e.printStackTrace();
        }
    }

    private static void loadNodeInfoCache(String ip) {

        CacheManager cm = getCM(ip);

        Map<String, String> data = cm.getHashAll(HeartBeatProtocol.STORE_REGION_UAV,
                HeartBeatProtocol.STORE_KEY_NODEINFO);

        String dat = JSONHelper.toString(data);

        try {
            IOHelper.writeTxtFile(IOHelper.getCurrentPath() + "/nodeinfo_cache.txt", dat, "utf-8", false);
        }
        catch (IOException e) {

            e.printStackTrace();
        }
    }

    public static void loadProfileCache(String ip) {

        CacheManager cm = getCM(ip);

        Map<String, String> data = cm.getHashAll(HealthManagerConstants.STORE_REGION_UAV,
                HealthManagerConstants.STORE_KEY_PROFILEINFO);

        String dat = JSONHelper.toString(data);

        try {
            IOHelper.writeTxtFile(IOHelper.getCurrentPath() + "/profile_cache.txt", dat, "utf-8", false);
        }
        catch (IOException e) {

            e.printStackTrace();
        }
    }

    private static CacheManager getCM(String ip) {

        SystemLogger.init("INFO", false, 5);

        ConfigurationManager.build(new HashMap<String, String>());

        CacheManager.build(ip, 2, 5, 10);

        CacheManager cm = CacheManager.instance();
        return cm;
    }
}
