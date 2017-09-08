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

package com.creditease.agent.feature.logagent;

import java.util.List;

import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.agent.spi.AbstractQueueWorkComponent;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.uav.messaging.api.Message;
import com.creditease.uav.messaging.api.MessageProducer;
import com.creditease.uav.messaging.api.MessagingFactory;

public class AppServerLogPublishWorker extends AbstractQueueWorkComponent<MonitorDataFrame> {

    public AppServerLogPublishWorker(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    protected void handle(List<MonitorDataFrame> mdflist) {

        MessageProducer producer = (MessageProducer) this.getComponentResource("messageproducer",
                "MessageProducerResourceComponent");
        // submit profile data
        Message msg = MessagingFactory.createMessage(MonitorDataFrame.MessageType.Log.toString());
        StringBuilder sb = new StringBuilder("[");

        for (MonitorDataFrame mdf : mdflist) {
            sb.append(mdf.toJSONString() + ",");
        }

        if (mdflist.size() > 0) {
            sb = sb.deleteCharAt(sb.length() - 1);
        }

        String stream = sb.append("]").toString();
        msg.setParam(MonitorDataFrame.MessageType.Log.toString(), stream);

        if (log.isDebugEnable()) {
            log.debug(this, "## LogPublishWorker stream length ## : " + stream.getBytes().length);
            log.debug(this, "## LogPublishWorker stream value ## : " + stream);
        }

        producer.setLogger(log);

        boolean check = producer.submit(msg);
        String sendState = "Log Data Sent " + (check ? "SUCCESS" : "FAIL");

        if (log.isDebugEnable()) {
            log.debug(this, sendState + " " + sendState);
        }

        if (null != getConfigManager().getComponent("monitortestagent", "MonitorAgentUT")) {

            AgentFeatureComponent afc = (AgentFeatureComponent) this.getConfigManager().getComponent("monitortestagent",
                    "monitortestagent");
            afc.exchange("logTest", mdflist);

        }
    }

    @Override
    protected ISystemLogger getLogger(String cName, String feature) {

        return (ISystemLogger) this.getConfigManager().getComponent(this.feature, "LogDataLog");
    }
}
