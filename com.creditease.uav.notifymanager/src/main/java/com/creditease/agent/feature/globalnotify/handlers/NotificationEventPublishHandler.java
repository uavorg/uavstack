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

package com.creditease.agent.feature.globalnotify.handlers;

import java.util.ArrayList;
import java.util.List;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.AbstractHandler;
import com.creditease.uav.messaging.api.Message;
import com.creditease.uav.messaging.api.MessageProducer;
import com.creditease.uav.messaging.api.MessagingFactory;

/**
 * DefaultNotificationEventHandler helps to publish notification including HealthReAction,NoMonitorMBean,ReAccessFAIL to
 * messaging system
 * 
 * @author zhen zhang
 *
 */
public class NotificationEventPublishHandler extends AbstractHandler<NotificationEvent> {

    public NotificationEventPublishHandler(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public void handle(List<NotificationEvent> data) {

        MessageProducer producer = (MessageProducer) this.getComponentResource("messageproducer",
                "MessageProducerResourceComponent");

        String notifyKey = MonitorDataFrame.MessageType.Notification.toString();

        Message msg = MessagingFactory.createMessage(notifyKey);

        String stream = toJSONString(data);

        msg.setParam(notifyKey, stream);
        boolean check = producer.submit(msg);

        String sendState = notifyKey + " Data Sent " + (check ? "SUCCESS" : "FAIL");

        if (log.isTraceEnable()) {
            log.info(this, sendState + "	" + stream);
        }
    }

    protected String toJSONString(List<NotificationEvent> mdflist) {

        List<String> ls = new ArrayList<String>();

        for (NotificationEvent mdf : mdflist) {
            ls.add(mdf.toJSONString());
        }

        return JSONHelper.toString(ls);
    }

    @Override
    public void handle(NotificationEvent data) {

        // ignore
    }

    @Override
    protected ISystemLogger getLogger(String cName, String feature) {

        return SystemLogger.getLogger("NotifyLog", feature + ".notify.%g.%u.log", "INFO", false, 5 * 1024 * 1024);
    }
}
