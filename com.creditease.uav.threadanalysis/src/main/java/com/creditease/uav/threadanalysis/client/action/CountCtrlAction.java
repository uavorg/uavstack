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
import com.creditease.agent.spi.AbstractBaseAction;
import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.IActionEngine;

public class CountCtrlAction extends AbstractBaseAction {

    public CountCtrlAction(String cName, String feature, IActionEngine engine) {
        super(cName, feature, engine);
    }

    @Override
    public void doAction(final ActionContext context) throws Exception {

        if(log.isDebugEnable()) {
            log.debug(this, "CountCtrlAction.doAction context=" + JSONHelper.toString(context));
        }
        
        String fileName = (String) context.getParam("msg");
        if (fileName.startsWith("ERR")) {
            context.setSucessful(false);
            return;
        }

        int times = (int) context.getParam("times");
        context.putParam("times", --times);
        if (times > 0) {
            Thread t = new Thread(new Runnable() {

                @Override
                public void run() {

                    getActionEngineMgr().getActionEngine("JTAActionEngine").execute("SuspendAction", context);
                }
            });
            t.start();
        }
        
        context.setSucessful(true);
    }

    @Override
    public String getSuccessNextActionId() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getFailureNextActionId() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getExceptionNextActionId() {

        // TODO Auto-generated method stub
        return null;
    }

}
