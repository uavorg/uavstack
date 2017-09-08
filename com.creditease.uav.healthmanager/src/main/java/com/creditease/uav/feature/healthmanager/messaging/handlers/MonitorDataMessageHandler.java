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
import com.creditease.agent.spi.AgentResourceComponent;
import com.creditease.uav.datastore.api.DataStoreMsg;
import com.creditease.uav.datastore.api.DataStoreProtocol;
import com.creditease.uav.feature.healthmanager.messaging.AbstractMessageHandler;
import com.creditease.uav.messaging.api.Message;
import com.creditease.uav.messaging.api.MessageProducer;
import com.creditease.uav.messaging.api.MessagingFactory;

public class MonitorDataMessageHandler extends AbstractMessageHandler {

    @Override
    public void handle(Message msg) {

        super.handle(msg);

        // NOW, we send out the MDF for runtime notification

        AgentResourceComponent arc = (AgentResourceComponent) ConfigurationManager.getInstance()
                .getComponent("messageproducer", "MessageProducerResourceComponent");

        MessageProducer producer = (MessageProducer) arc.getResource();

        if (producer != null) {

            String runtimeKey = MonitorDataFrame.MessageType.RuntimeNtf.toString();
            Message rtntfmsg = MessagingFactory.createMessage(runtimeKey);
            String dataStream = msg.getParam(this.getMsgTypeName());
            rtntfmsg.setParam(runtimeKey, dataStream);
            boolean check = producer.submit(rtntfmsg);
            String sendState = runtimeKey + " Data Sent " + (check ? "SUCCESS" : "FAIL");

            if (log.isDebugEnable()) {
                log.debug(this, sendState + "    " + dataStream);
            }

        }
    }

    @Override
    public String getMsgTypeName() {

        return MonitorDataFrame.MessageType.Monitor.toString();
    }

    @Override
    protected void preInsert(DataStoreMsg dsMsg) {

        dsMsg.put(DataStoreProtocol.OPENTSDB_INSERT_BATCHSIZE,
                ConfigurationManager.getInstance().getFeatureConfiguration("healthmanager", "MT_Monitor.ds.batchsize"));
    }

}
