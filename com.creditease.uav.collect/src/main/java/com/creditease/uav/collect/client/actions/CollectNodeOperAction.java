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

package com.creditease.uav.collect.client.actions;

import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.spi.AbstractBaseAction;
import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.agent.spi.IActionEngine;

public class CollectNodeOperAction extends AbstractBaseAction {

    public CollectNodeOperAction(String cName, String feature, IActionEngine engine) {
        super(cName, feature, engine);
    }

    @Override
    public void doAction(ActionContext context) throws Exception {

        AgentFeatureComponent afc = (AgentFeatureComponent) getConfigManager().getComponent("collectclient",
                "CollectDataAgent");
        if (afc == null) {
            return;
        }

        UAVHttpMessage data = (UAVHttpMessage) context.getParam("msg");

        String cmd = data.getRequest().get("cmd");
        if ("add".equals(cmd)) {
            afc.exchange("collectdata.add", data.getRequest().get("task"));
        }
        else if ("del".equals(cmd)) {
            afc.exchange("collectdata.del", data.getRequest().get("task"));
        }
        else if ("status".equals(cmd)) {
            String status = (String) afc.exchange("collectdata.status", cmd);
            data.putResponse(UAVHttpMessage.RESULT, status);
        }
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

}
