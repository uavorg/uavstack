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

package com.creditease.agent.feature.nodeopagent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.spi.AbstractHttpHandler;
import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.IActionEngine;

/**
 * 
 * NodeOperCtrlHandler description: 控制UAV节点的相关动作
 *
 */
public class NodeOperCtrlHandler extends AbstractHttpHandler<UAVHttpMessage> {

    private Map<String, Long> actionLimits = new ConcurrentHashMap<String, Long>();

    private boolean isCheckSec = true;

    public NodeOperCtrlHandler(String cName, String feature) {
        super(cName, feature);

        isCheckSec = DataConvertHelper
                .toBoolean(this.getConfigManager().getFeatureConfiguration(this.feature, "checksec"), true);

    }

    @Override
    public String getContextPath() {

        return "/node/ctrl";
    }

    @Override
    public void handle(UAVHttpMessage data) {

        long curTS = System.currentTimeMillis();

        String intent = data.getIntent();

        if (isCheckSec == true) {

            Long ts = actionLimits.get(intent);

            if (ts != null && curTS - ts < 1000
                    && (!"loadnodepro".equalsIgnoreCase(intent) && !"chgsyspro".equalsIgnoreCase(intent))) {
                data.putResponse("rs", "该节点上的操作[" + intent + "]1秒内只能进行一次");
                return;
            }

            actionLimits.put(intent, curTS);
        }

        if (log.isDebugEnable()) {
            log.debug(this, "NodeOperation[" + intent + "] START: request=" + JSONHelper.toString(data.getRequest()));
        }

        IActionEngine engine = this.getActionEngineMgr().getActionEngine("NodeOperActionEngine");

        ActionContext ac = new ActionContext();

        ac.putParam("msg", data);

        engine.execute(data.getIntent(), ac);

        if (log.isDebugEnable()) {
            log.debug(this, "NodeOperation[" + intent + "] END: response="
                    + JSONHelper.toString(data.getResponseAsJsonString()));
        }
    }
}
