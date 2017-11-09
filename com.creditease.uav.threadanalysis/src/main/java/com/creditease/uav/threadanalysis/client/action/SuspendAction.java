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

package com.creditease.uav.threadanalysis.client.action;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.ThreadHelper;
import com.creditease.agent.spi.AbstractBaseAction;
import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.IActionEngine;

public class SuspendAction extends AbstractBaseAction {

    public SuspendAction(String cName, String feature, IActionEngine engine) {
        super(cName, feature, engine);
    }

    @Override
    public void doAction(ActionContext context) throws Exception {

        if(log.isDebugEnable()) {
            log.debug(this, "SuspendAction.doAction context=" + JSONHelper.toString(context));
        }
        
        int suspendTime = (int) context.getParam("suspendTime");
        ThreadHelper.suspend(suspendTime);
        
        context.setSucessful(true);
    }

    @Override
    public String getSuccessNextActionId() {

        return "DumpThreadAction";
    }

    @Override
    public String getFailureNextActionId() {

        return null;
    }

    @Override
    public String getExceptionNextActionId() {

        return null;
    }

}
