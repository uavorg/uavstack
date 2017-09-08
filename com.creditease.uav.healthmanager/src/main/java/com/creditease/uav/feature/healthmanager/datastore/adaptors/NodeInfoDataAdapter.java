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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.creditease.agent.helpers.DataStoreHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.uav.datastore.api.DataStoreAdapter;
import com.creditease.uav.datastore.api.DataStoreConnection;
import com.creditease.uav.datastore.api.DataStoreMsg;
import com.creditease.uav.datastore.api.DataStoreProtocol;

/**
 * Monitor Data insert data and query data prepare
 * 
 * @author minglang
 */

public class NodeInfoDataAdapter extends DataStoreAdapter {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object prepareInsertObj(DataStoreMsg msg, DataStoreConnection connection) {

        boolean isDataStoreOK = (this.dataSource.getAvalibleAddressEntry() == null) ? false : true;

        // OpenTSDB points list
        List<Map<String, ?>> points = new ArrayList<Map<String, ?>>();

        /**
         * 消息message传输过来的MDF是一个列表包含多个MDF的json字符串
         */
        // parse MDF list
        String monitorDataFramesStr = (String) msg.get(MonitorDataFrame.MessageType.NodeInfo.toString());

        List<String> monitorDataFrames = JSONHelper.toObjectArray(monitorDataFramesStr, String.class);

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
                         * FAST Failure: if there is no available address, we may not go to DataStore part
                         */
                        if (isDataStoreOK == false) {
                            if (log.isDebugEnable() == true) {
                                log.debug(this, "Skip MonitorData INSERT as no available address");
                            }
                            continue;
                        }

                        /**
                         * prepare the inst's metic data for opentsdb
                         */
                        for (String key : fields.keySet()) {
                            Map<String, Object> point = new LinkedHashMap<String, Object>();

                            String mkey = key;

                            if (mkey.indexOf("cpu") < 0 && mkey.indexOf("conn") < 0 && mkey.indexOf("os.io.disk.") < 0
                                    && mkey.indexOf("cpu") < 0 && mkey.indexOf("disk") != 0 && mkey.indexOf("in") != 0
                                    && mkey.indexOf("out") != 0 && mkey.indexOf("mem") != 0) {
                                continue;
                            }

                            /**
                             * NOTE: os.io.disk.*,conn_*,in_*,out_* . they are dynamic tags, so make the metric key as
                             * "XX" add a new tag "ptag" as the last tag
                             */
                            String plusTag = null;
                            if (key.indexOf("os.io.disk") == 0) {
                                mkey = "os.io.disk";
                                plusTag = key;
                            }
                            else if (key.indexOf("conn_") == 0) {
                                mkey = "conn_port";
                                plusTag = key;
                            }
                            else if (key.indexOf("in_") == 0) {
                                mkey = "in_port";
                                plusTag = key;
                            }
                            else if (key.indexOf("out_") == 0) {
                                mkey = "out_port";
                                plusTag = key;
                            }
                            /**
                             * WRITE DATA Schema Sample:
                             * 
                             * {
                             * 
                             * metric: procStat.cpu,
                             * 
                             * timestamp: 1463816026510,
                             * 
                             * value: 4,
                             * 
                             * tags:[
                             * 
                             * ip: 127.0.0.1,
                             * 
                             * pgid: mongo.exe,
                             * 
                             * instid: 127.0.0.1_mongo.exe_3342,
                             * 
                             * ptag(optional): os.io.disk.home.useRate
                             * 
                             * ]
                             * 
                             * 
                             * }
                             */

                            point.put("metric", meId + "." + mkey);
                            point.put("timestamp", mdf.getTimeFlag());
                            double value;
                            try {
                                value = Double.parseDouble((String) fields.get(key));
                            }
                            catch (NumberFormatException e) {
                                continue;
                            }

                            point.put("value", value);

                            point.put("tags", createTags(mdf, instanceId, meId, plusTag, fields));

                            points.add(point);
                        }

                    }
                }
            }
        }

        return points;
    }

    private Map<String, String> createTags(MonitorDataFrame mdf, String instanceId, String meId, String plusTag,
            Map<String, Object> fields) {

        Map<String, String> tags = new LinkedHashMap<>();

        // IP
        String encodeIP = DataStoreHelper.encodeForOpenTSDB(mdf.getIP());
        tags.put("ip", encodeIP);

        if (meId.indexOf("hostState") == 0) {
            // group Id :for hostState, always "0"
            tags.put("pgid", "0");
            // Instance Id: for hostState, instid is IP
            tags.put("instid", encodeIP);
        }
        else if (meId.indexOf("procState") == 0) {
            String temp[] = instanceId.split("_");
            if ((temp[1].equals("java") || temp[1].equals("javaw.exe") || temp[1].equals("java.exe"))
                    && fields.get("main") != null) {

                String mainClass = (String) fields.get("main");
                // group Id :for java, pgid is main class
                tags.put("pgid", DataStoreHelper.encodeForOpenTSDB(mainClass));
                // Instance Id: for java, instance id is ip_mainClass_pid
                tags.put("instid", DataStoreHelper.encodeForOpenTSDB(temp[0] + "_" + mainClass + "_" + temp[2]));
            }
            else {
                // group Id :program name
                tags.put("pgid", DataStoreHelper.encodeForOpenTSDB(temp[1]));
                // Instance Id
                tags.put("instid", DataStoreHelper.encodeForOpenTSDB(instanceId));
            }
        }

        if (plusTag != null) {
            tags.put("ptag", DataStoreHelper.encodeForOpenTSDB(plusTag));
        }

        /**
         * note: not store host as no use for tags
         */
        // tags.put("host", mdf.getHost());
        return tags;
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

}
