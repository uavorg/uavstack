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

package com.creditease.agent.feature.notifycenter.actions;

import java.util.Map;

import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.AbstractSystemInvoker;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.agent.spi.ISystemInvokerMgr.InvokerType;

/**
 * 
 * PushNotifyEventAction description: 提供报警推送服务
 *
 */
public class PushNotifyEventAction extends BaseNotifyAction {

    public PushNotifyEventAction(String cName, String feature, IActionEngine engine) {
        super(cName, feature, engine);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public boolean run(NotificationEvent event) {

        AbstractSystemInvoker invoker = this.getSystemInvokerMgr().getSystemInvoker(InvokerType.HTTP);

        UAVHttpMessage tmsg = new UAVHttpMessage();

        tmsg.setIntent("ntfpush");

        tmsg.putRequest("ntfevent", event.toJSONString());

        String serviceStr = this.getConfigManager().getFeatureConfiguration(this.feature, "push.services");

        if (StringHelper.isEmpty(serviceStr)) {
            return true;
        }

        String[] services = serviceStr.split(",");

        boolean check = true;

        for (String service : services) {

            try {
                Map<String, Object> res = (Map<String, Object>) invoker.invoke(service, tmsg, Map.class);

                if (res == null) {
                    check = false;
                    continue;
                }

                if (log.isTraceEnable()) {
                    log.info(this, "Push Notification to Service[" + service + "] SUCCESS.");
                }

                return true;
            }
            catch (Exception e) {
                check = false;
                // ignore
            }
        }

        return check;
    }

}
