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

public class NotificationMessageHandler extends AbstractMessageHandler {

    @Override
    public void handle(Message msg) {

        super.handle(msg);

        // exchange msg to NotifyCenter
        AgentFeatureComponent afc = (AgentFeatureComponent) ConfigurationManager.getInstance()
                .getComponent("notifycenter", "NotificationCenter");

        if (null != afc) {
            String notifyDataArrayStr = msg.getParam(getMsgTypeName());

            afc.exchange("notify.center.put", notifyDataArrayStr);
        }
    }

    @Override
    public String getMsgTypeName() {

        return MonitorDataFrame.MessageType.Notification.toString();
    }

    @Override
    protected void preInsert(DataStoreMsg dsMsg) {

        // set target mongo collection name
        dsMsg.put(DataStoreProtocol.MONGO_COLLECTION_NAME, HealthManagerConstants.MONGO_COLLECTION_NOTIFY);
    }

}
