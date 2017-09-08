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

package com.creditease.uav.feature.healthmanager.messaging;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.uav.datastore.api.DataStoreFactory;
import com.creditease.uav.datastore.api.DataStoreMsg;
import com.creditease.uav.datastore.core.AbstractDataStore;
import com.creditease.uav.messaging.api.Message;
import com.creditease.uav.messaging.api.MessageHandler;

/**
 * the basic processing for log,monitor,profile,notify
 * 
 * @author zhen zhang
 *
 */
public abstract class AbstractMessageHandler implements MessageHandler {

    protected static final ISystemLogger log = SystemLogger.getLogger(AbstractMessageHandler.class);

    /**
     * do some action before really insertion
     * 
     * @param dsMsg
     */
    protected abstract void preInsert(DataStoreMsg dsMsg);

    @Override
    public void handle(Message msg) {

        String msgKey = getMsgTypeName();

        if (log.isDebugEnable()) {
            log.debug(this, "CONSUME MSG[" + msgKey + "]: " + JSONHelper.toString(msg));
        }

        @SuppressWarnings("rawtypes")
        AbstractDataStore store = DataStoreFactory.getInstance().get(msgKey);

        if (store != null) {

            if (!store.isStarted()) {

                store.start();

                if (!store.isStarted()) {
                    if (log.isTraceEnable()) {
                        log.warn(this, "DataStore[" + msgKey + "] CAN NOT START.");
                    }
                    return;
                }
            }

            DataStoreMsg dsMsg = new DataStoreMsg();

            dsMsg.put(msgKey, msg.getParam(msgKey));

            // pre insert to process DataStoreMsg
            preInsert(dsMsg);

            // do insert
            boolean insertR = store.doInsert(dsMsg);

            if (log.isDebugEnable()) {

                String state = (insertR) ? "SUCCESS" : "FAIL";

                log.debug(this, "DataStore[" + msgKey + "] INSERT DATA " + state);
            }
        }
        else {
            if (log.isTraceEnable()) {
                log.warn(this, "DataStore[" + msgKey + "] NO EXIST");
            }
        }
    }
}
