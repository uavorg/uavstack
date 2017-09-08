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

package com.creditease.uav.feature.runtimenotify.messaging.handlers;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.uav.datastore.api.DataStoreMsg;
import com.creditease.uav.feature.healthmanager.messaging.AbstractMessageHandler;
import com.creditease.uav.messaging.api.Message;

public class RuntimeDataMessageHandler extends AbstractMessageHandler {

    @Override
    public void handle(Message msg) {

        String dataStream = msg.getParam(getMsgTypeName());

        AgentFeatureComponent rn = (AgentFeatureComponent) ConfigurationManager.getInstance()
                .getComponent("runtimenotify", "RuntimeNotifyCatcher");
        if (rn != null) {
            rn.exchange("runtime.notify", dataStream, false);
        }
    }

    @Override
    public String getMsgTypeName() {

        return MonitorDataFrame.MessageType.RuntimeNtf.toString();
    }

    @Override
    protected void preInsert(DataStoreMsg dsMsg) {

        // ignore
    }

}
