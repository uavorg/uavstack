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
import com.creditease.uav.messaging.api.Message;
import com.creditease.uav.messaging.api.MessageProducer;
import com.creditease.uav.messaging.api.MessagingFactory;

public class AppServerLogPublishWorkerByStream extends AbstractQueueWorkComponent<String> {

    public AppServerLogPublishWorkerByStream(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    protected void handle(List<String> streamList) {

        MessageProducer producer = (MessageProducer) this.getComponentResource("messageproducer",
                "MessageProducerResourceComponent");
        // submit profile data
        Message msg = MessagingFactory.createMessage(MonitorDataFrame.MessageType.Log.toString());

        for (String stream : streamList) {

            msg.setParam(MonitorDataFrame.MessageType.Log.toString(), stream);

            if (log.isDebugEnable()) {
                log.debug(this, "## final stream length ## : " + stream.getBytes().length);
                log.debug(this, "## final stream value ## : " + stream);
            }
            /***
             * TestUsage: output the result
             */
            // IOHelper.write(stream, "" + this.toString());

            producer.setLogger(log);
            boolean check = producer.submit(msg);

            String sendState = "Log Data Sent " + (check ? "SUCCESS" : "FAIL");

            if (log.isDebugEnable()) {
                log.debug(this, sendState + " " + stream);
            }
        }
    }

    @Override
    protected ISystemLogger getLogger(String cName, String feature) {

        return (ISystemLogger) this.getConfigManager().getComponent(this.feature, "LogDataLog");
    }
}
