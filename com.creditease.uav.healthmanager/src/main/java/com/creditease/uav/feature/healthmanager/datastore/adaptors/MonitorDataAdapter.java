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

package com.creditease.uav.feature.healthmanager.datastore.adaptors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.DataStoreHelper;
import com.creditease.agent.helpers.DateTimeHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.datastore.api.DataStoreAdapter;
import com.creditease.uav.datastore.api.DataStoreConnection;
import com.creditease.uav.datastore.api.DataStoreMsg;
import com.creditease.uav.datastore.api.DataStoreProtocol;
import com.creditease.uav.feature.healthmanager.HealthManagerConstants;

/**
 * Monitor Data insert data and query data prepare
 * 
 * @author hongqiang
 */

public class MonitorDataAdapter extends DataStoreAdapter {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object prepareInsertObj(DataStoreMsg msg, DataStoreConnection connection) {

        boolean isDataStoreOK = (this.dataSource.getAvalibleAddressEntry() == null) ? false : true;

        // OpenTSDB points list
        List<Map<String, ?>> points = new ArrayList<Map<String, ?>>();

        CacheManager cm = (CacheManager) ConfigurationManager.getInstance().getComponent("healthmanager",
                "HMCacheManager");

        int cacheTime = DataConvertHelper.toInt(
                ConfigurationManager.getInstance().getFeatureConfiguration("healthmanager", "MT_Monitor.ds.cachetime"),
                1);

        /**
         * 消息message传输过来的MDF是一个列表包含多个MDF的json字符串
         */
        // parse MDF list
        String monitorDataFramesStr = (String) msg.get(MonitorDataFrame.MessageType.Monitor.toString());

        List<String> monitorDataFrames = JSONHelper.toObjectArray(monitorDataFramesStr, String.class);

        cm.beginBatch();

        for (String mdfStr : monitorDataFrames) {

            // 反序列化为MonitorDataFrame
            MonitorDataFrame mdf = new MonitorDataFrame(mdfStr);

            // get all frames
            Map<String, List<Map>> frames = mdf.getDatas();

            for (String monitorId : frames.keySet()) {

                List<Map> monitorDatas = frames.get(monitorId);

                for (Map monitorData : monitorDatas) {

                    String meId = (String) monitorData.get("MEId");

                    List<Map> instances = (List<Map>) monitorData.get("Instances");

                    for (Map instance : instances) {
                        // get instance id
                        String instanceId = (String) instance.get("id");
                        // get instance fields
                        Map<String, Object> fields = (Map<String, Object>) instance.get("values");

                        /**
                         * Step 1: put the inst's metric data into cache
                         */
                        String encodedInstId = DataStoreHelper.encodeForOpenTSDB(instanceId);

                        String mdCacheKey = getMDCacheKey(mdf, encodedInstId, meId);

                        Map<String, Object> fieldValues = new HashMap<String, Object>();

                        fieldValues.put("data", fields);
                        fieldValues.put("time", mdf.getTimeFlag());

                        cm.rpush(HealthManagerConstants.STORE_REGION_UAV, mdCacheKey, JSONHelper.toString(fieldValues));

                        cm.expire(HealthManagerConstants.STORE_REGION_UAV, mdCacheKey, cacheTime, TimeUnit.MINUTES);

                        /**
                         * FAST Failure: if there is no available address, we may not go to DataStore part
                         */
                        if (isDataStoreOK == false) {
                            if (log.isDebugEnable() == true) {
                                log.debug(this, "Skip MonitorData INSERT as no available address");
                            }
                            continue;
                        }

                        /**
                         * Step 2: prepare the inst's metic data for opentsdb
                         */
                        for (String key : fields.keySet()) {
                            Map<String, Object> point = new LinkedHashMap<String, Object>();

                            String mkey = key;

                            /**
                             * NOTE: RC=return code, they are dynamic tags, so make the metric key as "XX.rc" add a new
                             * tag "ptag" as the last tag
                             */
                            String plusTag = null;
                            if (key.indexOf("RC") == 0) {
                                mkey = "RC";
                                plusTag = key;
                            }
                            else if (key.indexOf("AC") == 0) {
                                mkey = "AC";
                                plusTag = key;
                            }
                            else if (key.indexOf("EXT") == 0) {
                                mkey = "EXT";
                                plusTag = key;
                            }
                            /**
                             * WRITE DATA Schema Sample:
                             * 
                             * {
                             * 
                             * metric: urlResp.RC,
                             * 
                             * timestamp: 1463816026510,
                             * 
                             * value: 200,
                             * 
                             * tags:[
                             * 
                             * ip: 127.0.0.1,
                             * 
                             * pgid:
                             * E:/UAVIDE/tomcat/apache-tomcat-7.0.65---E:/UAVIDE/defaultworkspace/.metadata/.plugins/org
                             * .eclipse.wst.server.core/tmp0,
                             * 
                             * instid: http://127.0.0.1:8080/com.creditease.uav.console/rs/godeye/profile/q/cache,
                             * 
                             * ptag(optional): RC400
                             * 
                             * ]
                             * 
                             * 
                             * }
                             */

                            point.put("metric", meId + "." + mkey);
                            point.put("timestamp", mdf.getTimeFlag());
                            point.put("value", fields.get(key));

                            Map<String, String> tags = new LinkedHashMap<>();
                            // IP
                            tags.put("ip", DataStoreHelper.encodeForOpenTSDB(mdf.getIP()));
                            // Program Id: Tomcat Server Profile Or Other Service Program Profile
                            tags.put("pgid", DataStoreHelper.encodeForOpenTSDB(mdf.getServerId()));
                            // Instance Id: May be A Tomcat Server, A Tomcat App, A Tomcat App URL or Process Id
                            tags.put("instid", encodedInstId);

                            if (plusTag != null) {
                                tags.put("ptag", DataStoreHelper.encodeForOpenTSDB(plusTag));
                            }

                            /**
                             * note: not store host as no use for tags
                             */
                            // tags.put("host", mdf.getHost());

                            point.put("tags", tags);

                            points.add(point);
                        }

                    }
                }
            }
        }

        cm.submitBatch();

        return points;
    }

    @Override
    public boolean handleInsertResult(Object result, DataStoreMsg msg, DataStoreConnection connection) {

        Boolean isOK = (Boolean) result;
        return isOK;
    }

    @Override
    public Object prepareQueryObj(DataStoreMsg msg, DataStoreConnection connection) {

        return msg.get(DataStoreProtocol.OPENTSDB_QUERY_KEY);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List handleQueryResult(Object result, DataStoreMsg msg, DataStoreConnection connection) {

        // just return
        List results = (List) result;

        return results;
    }

    @Override
    public Object prepareUpdateObj(DataStoreMsg msg, DataStoreConnection connection) {

        return null;
    }

    @Override
    public boolean handleUpdateResult(Object result, DataStoreMsg msg, DataStoreConnection connection) {

        return false;
    }

    private String getMDCacheKey(MonitorDataFrame mdf, String instId, String meId) {

        long timeRangeIndex = DateTimeHelper.getTimeRangeIndexIn1Min(mdf.getTimeFlag());

        String key = HealthManagerConstants.STORE_KEY_MDCACHE_PREFIX + "@" + instId + "@" + meId + "@" + timeRangeIndex;

        return key;
    }

}
