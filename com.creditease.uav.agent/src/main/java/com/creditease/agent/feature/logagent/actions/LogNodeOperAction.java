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

package com.creditease.agent.feature.logagent.actions;

import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.spi.AbstractBaseAction;
import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.agent.spi.IActionEngine;

public class LogNodeOperAction extends AbstractBaseAction {

    public LogNodeOperAction(String cName, String feature, IActionEngine engine) {
        super(cName, feature, engine);
    }

    @Override
    public void doAction(ActionContext context) throws Exception {

        AgentFeatureComponent afc = (AgentFeatureComponent) getConfigManager().getComponent(feature, "LogAgent");
        if (afc == null) {
            return;
        }

        UAVHttpMessage data = (UAVHttpMessage) context.getParam("msg");
        String strategy = data.getRequest("strategy");
        try {
            afc.exchange("logagent.strategy", strategy);
            data.putResponse("rs", "OK");
        }
        catch (Exception e) {
            log.err(this, "exchange logagent strategy FAILED. " + strategy, e);
            data.putResponse("rs", "ERR");
            data.putResponse("msg", "设置日志抓取策略出错！");
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
