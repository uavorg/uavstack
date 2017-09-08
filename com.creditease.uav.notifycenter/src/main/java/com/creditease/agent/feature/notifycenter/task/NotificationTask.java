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

package com.creditease.agent.feature.notifycenter.task;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import com.creditease.agent.feature.NotificationCenter;
import com.creditease.agent.feature.notifycenter.NCConstant;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.Abstract1NTask;

/**
 * 
 * @author peihua
 *
 */
public class NotificationTask extends Abstract1NTask {

    public NotificationTask(String name, String feature) {
        super(name, feature);
    }

    @Override
    public void run() {

        NotificationEvent event = (NotificationEvent) this.get(NCConstant.NCEventParam);

        int priorityLevel = (int) this.get(NCConstant.PRIORITYLEVEL);

        NotificationCenter nc = (NotificationCenter) this.getConfigManager().getComponent(this.feature,
                "NotificationCenter");

        if (log.isTraceEnable()) {
            log.info(this, "NotificationTask Actions START: event=" + event.toJSONString());
        }

        /**
         * Step 1: get all actions for this event
         */
        Map<String, String> args = event.getArgs(true);

        Map<String, String> actionMap = new LinkedHashMap<String, String>();

        for (String key : args.keySet()) {

            if (key.indexOf("action_") != 0) {
                continue;
            }

            String actionTag = key.substring("action_".length());

            String actionStr = args.get(key);

            /**
             * find matched priority action
             * 
             */
            String actionPrame = getActionParmeByPriorityLevel(actionStr, priorityLevel);
            if (!StringHelper.isEmpty(actionPrame)) {

                actionMap.put(actionTag, actionPrame);
            }
        }

        /**
         * Step 1.5: send notify to push notify event action
         */
        String action4pushntf = new String();
        actionMap.put("pushntf", action4pushntf);

        /**
         * Step 2: execute actions
         */
        nc.executeNotifyAction(actionMap, event);

        if (log.isTraceEnable()) {
            log.info(this, "NotificationTask Actions END.");
        }
    }

    public String getActionParmeByPriorityLevel(String actionStr, int plevel) {

        @SuppressWarnings("rawtypes")
        LinkedList list = JSONHelper.toObject(actionStr, LinkedList.class);

        String actionParm = (String) list.get(plevel);

        return actionParm;

    }

}
