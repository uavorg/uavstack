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

package com.creditease.agent.spi;

import java.util.HashMap;
import java.util.Map;

import com.creditease.agent.helpers.StringHelper;

public class ActionEngine extends AbstractComponent implements IActionEngine {

    private volatile boolean stopFlag = false;
    private Map<String, AbstractBaseAction> actionMap = new HashMap<String, AbstractBaseAction>();

    public ActionEngine(String name, String feature) {
        super(name, feature);
    }

    @Override
    public ActionContext execute(String actionId, ActionContext context) {

        AbstractBaseAction action = getAction(actionId);
        if (action == null) {
            return context;
        }

        String nextActionId = null;

        while (action != null) {

            if (stopFlag) {
                if (log.isTraceEnable()) {
                    log.info(this, "Received stop command, so stop executing actions");
                }

                break;
            }

            try {
                context.setSucessful(false);

                action.doAction(context);

                String jumpTargetActionId = context.getJumpTargetActionId();

                if (!StringHelper.isEmpty(jumpTargetActionId)) {
                    nextActionId = jumpTargetActionId;
                }
                else {
                    if (context.isSucessful()) {
                        nextActionId = action.getSuccessNextActionId();
                    }
                    else {
                        nextActionId = action.getFailureNextActionId();
                    }
                }

                context.setJumpTargetActionId(null);
                context.setE(null);
            }
            catch (Exception e) {
                if (log.isTraceEnable()) {
                    log.err(this, "Exception occured " + e.getMessage());
                }
                context.setE(e);
                context.setJumpTargetActionId(null);
                nextActionId = action.getExceptionNextActionId();
            }

            action = getAction(nextActionId);
        }

        return context;
    }

    @Override
    public void registerAction(AbstractBaseAction action) {

        actionMap.put(action.getName(), action);
    }

    @Override
    public AbstractBaseAction unregisterAction(String actionId) {

        return actionMap.remove(actionId);
    }

    @Override
    public AbstractBaseAction getAction(String actionId) {

        return actionMap.get(actionId);
    }

    @Override
    public void stop() {

        this.stopFlag = true;
    }

    @Override
    public void jump(String actionId, ActionContext context) {

        context.setJumpTargetActionId(actionId);
    }

    @Override
    public void clean() {

        stop();

        for (AbstractBaseAction action : actionMap.values()) {
            try {
                action.destroy();
            }
            catch (Exception e) {
                log.err(this, "Action[" + action.cName + "] Destroy FAIL.", e);
            }
        }

        actionMap.clear();
    }

}
