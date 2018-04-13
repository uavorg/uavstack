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
import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.uav.datastore.api.DataStoreConnection;
import com.creditease.uav.datastore.api.DataStoreMsg;

/**
 * @author peihua
 */

public class NotifyDataAdpater extends AbstractMongoDataAdpater {

    @Override
    public Object prepareInsertObj(DataStoreMsg msg, DataStoreConnection NotifyDataAdpaterconnection) {

        List<Map<String, Object>> documents = new ArrayList<Map<String, Object>>();

        /**
         * 消息message传输过来的NTFEVENT是一个列表包含多个NTFEVENT的json字符串
         */
        // parse NTFEVENT list
        String ntfListStr = (String) msg.get(MonitorDataFrame.MessageType.Notification.toString());

        List<String> ntfList = JSONHelper.toObjectArray(ntfListStr, String.class);

        for (String ntfStr : ntfList) {

            NotificationEvent ne = new NotificationEvent(ntfStr);

            Map<String, Object> document = new LinkedHashMap<String, Object>();

            // add4NotificationCenter usage: bug fix , before encode value of Event, should not change the code line
            document.put("ntfkey", DataStoreHelper.encodeForMongoDB(getKeyfromNTFE(ne)));
            document.put("firstrecord", "false");

            Map<String, String> args = new LinkedHashMap<String, String>();

            Map<String, String> argsDecode = ne.getArgs(true);

            for (String key : ne.getArgs(false).keySet()) {

                args.put(DataStoreHelper.encodeForMongoDB(key), argsDecode.get(key));
            }

            document.put("args", args);
            document.put("eventid", ne.getId());
            document.put("title", ne.getTitle());
            document.put("description", ne.getDescription());
            document.put("time", ne.getTime());
            document.put("host", ne.getHost());
            document.put("ip", ne.getIP());
            document.put("appgroup", args.get("appgroup"));
            document.put("createtime", System.currentTimeMillis());
            document.put("notifyType", getNotifyType(ne.getTitle()));

            documents.add(document);
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

    public String getKeyfromNTFE(NotificationEvent data) {

        StringBuffer keybuffer = new StringBuffer();
        keybuffer.append(data.getIP() + "@");
        keybuffer.append(data.getId() + "@");
        keybuffer.append(data.getArg("component") + "@");
        keybuffer.append(data.getArg("feature") + "@");
        // NOTE: the Title in db should sych with cache
        keybuffer.append(DataStoreHelper.decodeForMongoDB(data.getTitle()).hashCode());

        String key = keybuffer.toString();
        return key;
    }

    public static String getNotifyType(String title) {
        String[] titleArray = title.split("@");
        if (titleArray.length > 2) {
            return titleArray[1];
        }
        else {
            return "UNKNOWN";
        }
    }
}
