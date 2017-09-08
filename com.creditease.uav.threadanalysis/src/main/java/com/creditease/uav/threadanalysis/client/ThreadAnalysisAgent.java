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

package com.creditease.uav.threadanalysis.client;

import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.uav.threadanalysis.client.action.ThreadAnalysisAction;

/**
 * 线程分析feature
 * 
 * @author xinliang
 *
 */
public class ThreadAnalysisAgent extends AgentFeatureComponent {

    public ThreadAnalysisAgent(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public void start() {

        IActionEngine engine = this.getActionEngineMgr().getActionEngine("NodeOperActionEngine");
        // setup actions
        new ThreadAnalysisAction("threadanalysis", feature, engine);
    }

    @Override
    public Object exchange(String eventKey, Object... data) {

        if (log.isDebugEnable()) {
            log.debug(this, eventKey, data);
        }
        if ("collect.callback".equalsIgnoreCase(eventKey)) {
            /**
             * TODO 需要对返回结果是OK还是有错误做更多处理，比如通知一下等
             */
        }
        return null;
    }

}
