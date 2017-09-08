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

package com.creditease.agent.feature.common;

import java.io.IOException;
import java.util.Properties;

import com.creditease.agent.feature.common.messaging.StandardMessagingBuilder;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.spi.AgentResourceComponent;
import com.creditease.uav.messaging.api.MessageProducer;
import com.creditease.uav.messaging.api.MessagingContext;

public class MessageProducerResourceComponent extends AgentResourceComponent {

    private MessageProducer msgProducer;

    public MessageProducerResourceComponent(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public Object initResource() {

        StandardMessagingBuilder smb = new StandardMessagingBuilder("CommonProducerBuilder", this.feature);

        try {
            smb.init(null);
        }
        catch (IOException e) {
            log.err(this, "Read msgtype2topic.properties FAILs, UAV MessageProducer can not start", e);
            return null;
        }

        msgProducer = smb.buildProducer();
        msgProducer.start();

        if (log.isTraceEnable()) {
            log.info(this, "UAV MessageProducer is started");
        }

        return msgProducer;
    }

    @Override
    public void releaseResource() {

        if (msgProducer != null) {
            msgProducer.stop();
        }

        if (log.isTraceEnable()) {
            log.info(this, "UAV MessageProducer is stopped");
        }
    }

    @Override
    public Object getResource() {

        return msgProducer;
    }

    @Override
    public void onConfigUpdate(Properties updatedConfig) {

        if (StringHelper.isEmpty((String) updatedConfig.get("resource.messageproducer.messagesizelimit"))) {
            return;
        }

        long msgSizeLimit = DataConvertHelper.toLong(updatedConfig.get("resource.messageproducer.messagesizelimit"),
                MessagingContext.DEFAULT_MSG_SIZE_LIMIT);

        msgProducer.setMsgSizeLimit(msgSizeLimit);

        if (log.isTraceEnable()) {
            log.info(this, "Update Message Size Limit To " + msgSizeLimit);
        }
    }
}
