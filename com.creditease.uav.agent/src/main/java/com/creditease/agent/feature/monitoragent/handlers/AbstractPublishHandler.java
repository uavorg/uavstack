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

package com.creditease.agent.feature.monitoragent.handlers;

import java.util.ArrayList;
import java.util.List;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.spi.AbstractHandler;
import com.creditease.uav.messaging.api.Message;
import com.creditease.uav.messaging.api.MessageProducer;
import com.creditease.uav.messaging.api.MessagingFactory;

public abstract class AbstractPublishHandler<T> extends AbstractHandler<T> {

    protected String msgKey;

    public AbstractPublishHandler(String cName, String feature) {
        super(cName, feature);
    }

    protected abstract String getMessageKey();

    protected abstract String getMessage(T t);

    @Override
    public void handle(List<T> mdflist) {

        MessageProducer producer = (MessageProducer) this.getComponentResource("messageproducer",
                "MessageProducerResourceComponent");
        Message msg = MessagingFactory.createMessage(getMessageKey());

        String stream = toJSONString(mdflist);

        msg.setParam(getMessageKey(), stream);
        boolean check = producer.submit(msg);

        String sendState = getMessageKey() + " Data Sent " + (check ? "SUCCESS" : "FAIL");

        if (log.isTraceEnable()) {
            log.info(this, sendState + "	" + stream);
        }
    }

    @Override
    public void handle(T data) {

        // ignore
        // we will directly overrie the handle(List<T> mdflist), then no need write this one.
    }

    protected String toJSONString(List<T> mdflist) {

        List<String> ls = new ArrayList<String>();

        for (T mdf : mdflist) {
            ls.add(getMessage(mdf));
        }

        return JSONHelper.toString(ls);
    }
}
