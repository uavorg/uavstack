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

package com.creditease.uav.collect.server.messaging;

import java.util.List;
import java.util.Set;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.apm.api.AbstractCollectDataHandler;
import com.creditease.agent.apm.api.CollectDataFrame;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.uav.messaging.api.Message;
import com.creditease.uav.messaging.api.MessageHandler;

public class DefaultGatherDataHandler implements MessageHandler {

    private ISystemLogger log = SystemLogger.getLogger(DefaultGatherDataHandler.class);

    private String msgTypeName;

    public DefaultGatherDataHandler(String msgTypeName) {
        this.msgTypeName = msgTypeName;
    }

    @Override
    public void handle(Message msg) {

        String jsonarr = msg.getParam(msg.getMessageType());

        List<CollectDataFrame> frames = JSONHelper.toObjectArray(jsonarr, CollectDataFrame.class);

        Set<AbstractCollectDataHandler> handlers = ConfigurationManager.getInstance()
                .getComponents(AbstractCollectDataHandler.class);
        for (AbstractCollectDataHandler handler : handlers) {
            try {
                if (handler.isHandleable(msg.getMessageType())) {
                    handler.handle(frames);
                }
            }
            catch (Exception e) {
                log.err(this, "gather data handler failed. msgType:" + msg.getMessageType() + ", handler: "
                        + handler.getName(), e);
            }
        }
    }

    @Override
    public String getMsgTypeName() {

        return msgTypeName;
    }

}
