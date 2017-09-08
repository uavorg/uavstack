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

package com.creditease.uav.feature.runtimenotify.http;

import java.util.Map;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.spi.AbstractHttpHandler;
import com.creditease.uav.feature.runtimenotify.scheduler.RuntimeNotifyStrategyMgr;

/**
 * 
 * RuntimeNotifyServerHandler description:
 * 
 * 1. 运行时预警策略查询
 * 
 * 2. 运行时预警策略修改
 *
 */
public class RuntimeNotifyServerHandler extends AbstractHttpHandler<UAVHttpMessage> {

    private RuntimeNotifyStrategyMgr strategyMgr;

    public RuntimeNotifyServerHandler(String cName, String feature) {
        super(cName, feature);

        strategyMgr = (RuntimeNotifyStrategyMgr) getConfigManager().getComponent(this.feature,
                "RuntimeNotifyStrategyMgr");
    }

    @Override
    public String getContextPath() {

        return "/rtntf/oper";
    }

    @Override
    public void handle(UAVHttpMessage data) {

        String intent = data.getIntent();

        String body = data.getRequest(UAVHttpMessage.BODY);

        switch (intent) {
            case "strategy.update":

                strategyMgr.updateStrategy(body);

                data.putResponse(UAVHttpMessage.RESULT, "OK");
                return;
            case "strategy.query":

                Map<String, String> rs = strategyMgr.queryStrategy(body);

                data.putResponse(UAVHttpMessage.RESULT, JSONHelper.toString(rs));
                return;
            case "strategy.remove":
                strategyMgr.removeStrategy(body);
                data.putResponse(UAVHttpMessage.RESULT, "OK");
                return;
        }

        data.putResponse(UAVHttpMessage.ERR, "UNKOWN_Intent");
    }

}
