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
import com.creditease.uav.threadanalysis.client.action.CountCtrlAction;
import com.creditease.uav.threadanalysis.client.action.DumpThreadAction;
import com.creditease.uav.threadanalysis.client.action.SuspendAction;
import com.creditease.uav.threadanalysis.client.action.ThreadAnalysisAction;

/**
 * 线程分析feature
 * 
 * @author xinliang
 *
 */
public class ThreadAnalysisAgent extends AgentFeatureComponent {

    private IActionEngine actionEngine;

    public ThreadAnalysisAgent(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public void start() {

        actionEngine = getActionEngineMgr().newActionEngine("JTAActionEngine", feature);
        new DumpThreadAction("DumpThreadAction", feature, actionEngine);
        new CountCtrlAction("CountCtrlAction", feature, actionEngine);
        new SuspendAction("SuspendAction", feature, actionEngine);

        // setup actions
        IActionEngine engine = this.getActionEngineMgr().getActionEngine("NodeOperActionEngine");
        new ThreadAnalysisAction("threadanalysis", feature, engine);
    }

    @Override
    public void stop() {

        actionEngine.clean();

        super.stop();
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
