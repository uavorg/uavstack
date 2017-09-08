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

package com.creditease.uav.feature.healthmanager.messaging.handlers;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.uav.datastore.api.DataStoreMsg;
import com.creditease.uav.datastore.api.DataStoreProtocol;
import com.creditease.uav.feature.healthmanager.HealthManagerConstants;
import com.creditease.uav.feature.healthmanager.messaging.AbstractMessageHandler;
import com.creditease.uav.messaging.api.Message;

public class LogDataMessageHandler extends AbstractMessageHandler {

    @Override
    public void handle(Message msg) {

        super.handle(msg);

        AgentFeatureComponent rn = (AgentFeatureComponent) ConfigurationManager.getInstance()
                .getComponent("runtimenotify", "RuntimeNotifyCatcher");
        if (rn != null) {
            rn.exchange("runtime.notify", msg.getParam(getMsgTypeName()), true);
        }

    }

    @Override
    public String getMsgTypeName() {

        return MonitorDataFrame.MessageType.Log.toString();
    }

    @Override
    protected void preInsert(DataStoreMsg dsMsg) {

        // 表名
        dsMsg.put(DataStoreProtocol.HBASE_TABLE_NAME, HealthManagerConstants.HBASE_TABLE_LOGDATA);
    }

    // public void writeLogDataToFile(Message msg) {
    //
    // JSONObject object = JSONObject.parseObject(msg.toJSONString());
    //
    // JSONObject params = object.getJSONObject("params");
    //
    // String logString = params.getString("MT_Log");
    //
    // JSONArray array = JSONArray.parseArray(logString);
    //
    // JSONObject frames = array.getJSONObject(0).getJSONObject("frames");
    //
    // JSONArray appArray = frames.getJSONArray("com.creditease.uav.monitorframework.buildFat");
    //
    // JSONArray instanceArray = appArray.getJSONObject(0).getJSONArray("Instances");
    //
    // String logFileName = instanceArray.getJSONObject(0).getString("id");
    //
    // String prefix = logFileName.substring(0, logFileName.indexOf(".txt"));
    //
    // String realFileName = prefix + "RealOutput.txt";
    //
    // FileWriter writer = null;
    // try {
    // writer = new FileWriter(new File(realFileName), true);
    // JSONObject valueObject = instanceArray.getJSONObject(0).getJSONObject("values");
    // JSONArray contentsArray = valueObject.getJSONArray("content");
    // for (int i = 0; i < contentsArray.size(); i++) {
    // String content = contentsArray.getJSONObject(i).getString("content");
    // writer.write(content + "\r\n");
    // writer.flush();
    // }
    // }
    // catch (IOException e) {
    // }
    // finally {
    // try {
    // writer.close();
    // }
    // catch (IOException e) {
    // }
    // }
    //
    // }
}
