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

package com.creditease.uav.feature.healthmanager.http.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.creditease.agent.helpers.DataStoreHelper;
import com.creditease.agent.helpers.DateTimeHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.spi.AbstractHttpHandler;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.feature.healthmanager.HealthManagerConstants;

public class HMCacheQueryHandler extends AbstractHttpHandler<UAVHttpMessage> {

    private CacheManager cm;

    public HMCacheQueryHandler(String cName, String feature) {
        super(cName, feature);

        cm = (CacheManager) this.getConfigManager().getComponent(this.feature, "HMCacheManager");
    }

    @Override
    public String getContextPath() {

        return "/hm/cache/q";
    }

    @Override
    public void handle(UAVHttpMessage data) {

        if (cm == null) {
            return;
        }

        switch (data.getIntent()) {
            case "profile.detail":
                queryProfileDetail(data);
                break;
            case "profile":
                queryProfileList(data);
                break;
            case "clients":
                queryClientProfileList(data);
                break;
            case "iplnk":
                queryIPLinkList(data);
                break;
            case "monitor":
                queryMonitorData(data);
                break;
        }

    }

    /**
     * 查询profile的具体信息
     * 
     * @param data
     */
    private void queryProfileDetail(UAVHttpMessage data) {

        String target = data.getRequest("target");
        String field = data.getRequest("field");

        Map<String, String> d = Collections.emptyMap();

        d = cm.getHash("store.region.uav", target, field);

        String resultMsg = JSONHelper.toString(d);

        data.putResponse(UAVHttpMessage.RESULT, resultMsg);
    }

    /**
     * query ip link list
     * 
     * @param data
     */
    private void queryIPLinkList(UAVHttpMessage data) {

        String appgpids = data.getRequest("appgpids");

        List<String> ls = JSONHelper.toObjectArray(appgpids, String.class);

        @SuppressWarnings("rawtypes")
        Map<String, Map> retMap = new HashMap<String, Map>();

        /**
         * query iplink by app group+app id
         */
        for (String appgpid : ls) {

            Map<String, String> d = cm.getHashAll("store.region.uav", "LNK@" + appgpid);

            retMap.put(appgpid, d);
        }

        String resultMsg = JSONHelper.toString(retMap);

        data.putResponse(UAVHttpMessage.RESULT, resultMsg);
    }

    /**
     * 查询 当前1分钟内MonitorData的数据
     * 
     * @param data
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void queryMonitorData(UAVHttpMessage data) {

        String q = data.getRequest("cache.query.json");

        Map qObj = JSONHelper.toObject(q, Map.class);

        Long stTime = (Long) qObj.get("start");
        Long edTime = (Long) qObj.get("end");

        long timeStamp = (stTime + edTime) / 2;

        List<Map> subqueries = (List<Map>) qObj.get("queries");

        long timeRangeIndex = DateTimeHelper.getTimeRangeIndexIn1Min(timeStamp);

        Map<String, List> instsMD = new HashMap<String, List>();

        Map<String, Map> resData = new HashMap<String, Map>();

        Map<String, Map> instsTags = new HashMap<String, Map>();

        /**
         * Step 1: build metric points map from query json object and also get insts's monitor data
         */
        for (Map subq : subqueries) {

            Map tags = (Map) subq.get("tags");

            Map dTags = decodeTags(tags);

            String instId = (String) tags.get("instid");

            String metric = (String) subq.get("metric");

            String[] mInfo = metric.split("\\.");

            String type = mInfo[0];

            String mkey = mInfo[1];

            /**
             * for RC, we only can build metricPoints when get the monitor data
             */
            if (mkey.indexOf("RC") == 0 || mkey.indexOf("AC") == 0 || mkey.indexOf("EXT") == 0) {
                instsTags.put(instId, dTags);
            }
            /**
             * for other fixed metrics
             */
            else {
                String putKey = instId + "@" + mkey;

                Map<String, Object> metricPoints = new LinkedHashMap<String, Object>();

                metricPoints.put("metric", metric);
                metricPoints.put("tags", dTags);
                metricPoints.put("dps", new LinkedHashMap());

                resData.put(putKey, metricPoints);
            }

            /**
             * get the monitor data for each inst+type
             */
            String instTypeId = instId + "@" + type;

            if (!instsMD.containsKey(instTypeId)) {

                String key = HealthManagerConstants.STORE_KEY_MDCACHE_PREFIX + "@" + instId + "@" + type + "@"
                        + timeRangeIndex;

                List instMD = cm.lrange(HealthManagerConstants.STORE_REGION_UAV, key, 0, -1);

                instsMD.put(instTypeId, instMD);
            }
        }

        /**
         * Step 2: wrap the cached monitor data into the opentsdb format data
         */
        for (String instTypeId : instsMD.keySet()) {

            List mDataMapList = instsMD.get(instTypeId);

            String[] instTypeInfo = instTypeId.split("@");

            for (Object mDataMapObj : mDataMapList) {

                Map<String, Object> mDataMap = JSONHelper.toObject((String) mDataMapObj, Map.class);

                Long mTimeStamp = (Long) mDataMap.get("time");
                Map<String, Long> meData = (Map) mDataMap.get("data");

                for (String meKey : meData.keySet()) {

                    String putKey = instTypeInfo[0] + "@" + meKey;

                    if (!resData.containsKey(putKey)) {

                        /**
                         * NOTE: special for RC to build metric points
                         */
                        if (meKey.indexOf("RC") == 0) {
                            buildDynData("appResp", resData, instsTags, instTypeInfo, meKey, putKey);
                        }
                        /**
                         * NOTE: special for AC to build metric points
                         */
                        else if (meKey.indexOf("AC") == 0) {
                            buildDynData("clientResp", resData, instsTags, instTypeInfo, meKey, putKey);
                        }
                        /**
                         * NOTE: special for EXT to build metric points for clientResp
                         */
                        else if (meKey.indexOf("EXT") == 0) {
                            buildDynData("clientResp", resData, instsTags, instTypeInfo, meKey, putKey);
                        }
                        else {
                            continue;
                        }
                    }

                    Map<String, Object> metricPoints = resData.get(putKey);

                    Map points = (Map) metricPoints.get("dps");

                    points.put(String.valueOf(mTimeStamp), meData.get(meKey));
                }

            }

        }

        String resultMsg = JSONHelper.toString(resData.values());

        data.putResponse(UAVHttpMessage.RESULT, resultMsg);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void buildDynData(String type, Map<String, Map> resData, Map<String, Map> instsTags, String[] instTypeInfo,
            String meKey, String putKey) {

        Map<String, Object> metricPoints = new LinkedHashMap<String, Object>();

        Map tags = instsTags.get(instTypeInfo[0]);

        Map ntags = new LinkedHashMap();

        if (tags == null) {
            return;
        }

        ntags.put("ip", tags.get("ip"));
        ntags.put("instid", tags.get("instid"));
        ntags.put("pgid", tags.get("pgid"));
        ntags.put("ptag", meKey);

        metricPoints.put("metric", type + "." + meKey);
        metricPoints.put("tags", ntags);
        metricPoints.put("dps", new LinkedHashMap());

        resData.put(putKey, metricPoints);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map decodeTags(Map tags) {

        Map dtags = new LinkedHashMap();

        for (Object key : tags.keySet()) {

            String dAttr = DataStoreHelper.decodeForOpenTSDB((String) tags.get(key));

            dtags.put(key, dAttr);
        }

        return dtags;
    }

    /**
     * 查询Client Profile Cache
     * 
     * @param data
     */
    private void queryClientProfileList(UAVHttpMessage data) {

        queryCache(data, "profile.info.client");
    }

    /**
     * 查询Profile Cache
     * 
     * @param data
     */
    private void queryProfileList(UAVHttpMessage data) {

        queryCache(data, "profile.info");
    }

    private void queryCache(UAVHttpMessage data, String cacheKey) {

        String filterKey = data.getRequest("fkey");
        String filterValue = data.getRequest("fvalue"); // 多字段逗号分开
        Map<String, String> d = Collections.emptyMap();

        if ((!StringHelper.isEmpty(filterKey)) && (!StringHelper.isEmpty(filterValue))) {

            String[] fValues = filterValue.split(",");

            List<String> keys = cm.getHashKeys("store.region.uav", cacheKey);
            List<String> targets = new ArrayList<String>();
            if ("appgroup".equals(filterKey)) {
                for (String key : keys) {
                    String keyCheck = key.substring(0, key.indexOf("@"));
                    for (String vString : fValues) {
                        if (vString.equals(keyCheck)) {
                            targets.add(key);
                        }
                    }

                }
            }
            else if ("appurl".equals(filterKey)) {

                for (String key : keys) {
                    String keyCheck = key.substring(0, key.indexOf("@"));
                    for (String vString : fValues) {
                        if (vString.equals(keyCheck)) {
                            targets.add(key);
                        }
                    }

                }
            }

            String[] tkeys = new String[targets.size()];
            d = cm.getHash("store.region.uav", cacheKey, targets.toArray(tkeys));

        }
        else {
            d = cm.getHashAll("store.region.uav", cacheKey);

        }

        String resultMsg = JSONHelper.toString(d);

        data.putResponse(UAVHttpMessage.RESULT, resultMsg);
    }

}
