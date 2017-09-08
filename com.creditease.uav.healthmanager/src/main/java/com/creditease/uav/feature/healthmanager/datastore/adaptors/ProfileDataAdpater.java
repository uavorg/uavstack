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
import com.creditease.uav.datastore.api.DataStoreConnection;
import com.creditease.uav.datastore.api.DataStoreMsg;

/**
 * @author peihua
 */
public class ProfileDataAdpater extends AbstractMongoDataAdpater {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Object prepareInsertObj(DataStoreMsg msg, DataStoreConnection connection) {

        List<Map<String, Object>> documents = new ArrayList<Map<String, Object>>();

        /**
         * 消息message传输过来的MDF是一个列表包含多个MDF的json字符串
         */
        // parse MDF list
        String monitorDataFramesStr = (String) msg.get(MonitorDataFrame.MessageType.Profile.toString());

        List<String> monitorDataFrames = JSONHelper.toObjectArray(monitorDataFramesStr, String.class);

        for (String mdfStr : monitorDataFrames) {

            mdfStr = DataStoreHelper.encodeForMongoDB(mdfStr);

            // 反序列化为MonitorDataFrame
            MonitorDataFrame mdf = new MonitorDataFrame(mdfStr);

            // get all frames
            Map<String, List<Map>> frames = mdf.getDatas();

            for (String appid : frames.keySet()) {

                List<Map> appDatas = frames.get(appid);

                for (Map appData : appDatas) {
                    // get PEId
                    String peId = (String) appData.get("PEId");

                    List<Map> instances = (List<Map>) appData.get("Instances");

                    for (Map instance : instances) {
                        // get instance id
                        String instanceId = (String) instance.get("id");
                        // get instance fields
                        Map<String, Object> fields = (Map<String, Object>) instance.get("values");

                        // convert Map to List for Mongo Query
                        List<Object> listValues = new ArrayList<Object>();

                        for (Object key : fields.keySet()) {

                            // get fieldValue
                            Object fieldValue = fields.get(key);

                            Map<String, Object> values = new LinkedHashMap<String, Object>();

                            values.put("id", key.toString());
                            values.put("value", fieldValue);

                            listValues.add(values);
                        }

                        // build instance info MAP
                        Map<String, Object> instanceInfo = new LinkedHashMap<String, Object>();
                        instanceInfo.put("id", instanceId);
                        instanceInfo.put("values", listValues);

                        // build document
                        Map<String, Object> document = new LinkedHashMap<String, Object>();
                        document.put("time", mdf.getTimeFlag());
                        document.put("host", mdf.getHost());
                        document.put("ip", mdf.getIP());
                        document.put("svrid", mdf.getServerId());
                        document.put("appid", appid);
                        document.put("peid", peId);
                        document.put("inst", instanceInfo);

                        documents.add(document);
                    }
                }
            }
        }

        return documents;
    }

    @Override
    public boolean handleInsertResult(Object result, DataStoreMsg msg, DataStoreConnection connection) {

        Boolean isOK = (Boolean) result;

        if (isOK) {
            if (log.isDebugEnable()) {
                log.debug(this, "INSERT DATA SUCCESS:" + msg.toJSONString());
            }
        }
        else {
            if (log.isDebugEnable()) {
                log.debug(this, "INSERT DATA FAIL:" + msg.toJSONString());
            }
        }

        return isOK;
    }
}
