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

import com.creditease.agent.feature.notifycenter.NCConstant;
import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.AbstractBaseAction;
import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.IActionEngine;

/**
 * 
 * BaseNotifyAction description:
 * 
 * this is the base notify action for all actions
 *
 */
public abstract class BaseNotifyAction extends AbstractBaseAction {

    public BaseNotifyAction(String cName, String feature, IActionEngine engine) {
        super(cName, feature, engine);
    }

    @Override
    public void doAction(ActionContext context) throws Exception {

        NotificationEvent event = (NotificationEvent) context.getParam("event");

        String actionParam = (String) context.getParam(NCConstant.ACTIONVALUE);
        event.addArg(cName, actionParam);

        boolean isSuccess = this.run(event);

        context.setSucessful(isSuccess);
    }

    @Override
    public String getSuccessNextActionId() {

        return null;
    }

    @Override
    public String getFailureNextActionId() {

        return null;
    }

    @Override
    public String getExceptionNextActionId() {

        return null;
    }

    /**
     * action run with the event
     * 
     * @param event
     */
    public abstract boolean run(NotificationEvent event);
}
