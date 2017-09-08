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

package com.creditease.agent.feature;

import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.creditease.agent.feature.nodeopagent.NodeOperHttpServer;
import com.creditease.agent.feature.nodeopagent.actions.MOFCtrlAction;
import com.creditease.agent.feature.nodeopagent.actions.MOFInstallMgtAction;
import com.creditease.agent.feature.nodeopagent.actions.MSCPGeneralAction;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.log.api.IPLogger;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.agent.spi.IActionEngine;

/**
 * 
 * NodeOperAgent description: operation any node
 *
 */
public class NodeOperAgent extends AgentFeatureComponent {

    private NodeOperHttpServer nodeOperHttpServer;

    public NodeOperAgent(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public void start() {

        IActionEngine engine = this.getActionEngineMgr().getActionEngine("NodeOperActionEngine");

        // setup actions
        new MOFInstallMgtAction("installmof", feature, engine);
        new MOFInstallMgtAction("uninstallmof", feature, engine);
        new MSCPGeneralAction("fstart", feature, engine);
        new MSCPGeneralAction("fstop", feature, engine);
        new MSCPGeneralAction("killproc", feature, engine);
        new MSCPGeneralAction("kill", feature, engine);
        new MSCPGeneralAction("shutdown", feature, engine);
        new MSCPGeneralAction("chgsyspro", feature, engine);
        new MSCPGeneralAction("loadnodepro", feature, engine);
        new MSCPGeneralAction("chgnodepro", feature, engine);
        new MSCPGeneralAction("watch", feature, engine);
        new MSCPGeneralAction("unwatch", feature, engine);
        new MSCPGeneralAction("upgrade", feature, engine);
        new MSCPGeneralAction("restart", feature, engine);
        new MSCPGeneralAction("stopuav", feature, engine);
        new MOFCtrlAction("ctrlmof", feature, engine);

        // start HeartBeatServerListenWorker
        nodeOperHttpServer = new NodeOperHttpServer("NodeOperHttpServer", this.feature, "nodeophandlers");

        int port = DataConvertHelper.toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.port"),
                10101);
        int backlog = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.backlog"), 10);
        int core = DataConvertHelper.toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.core"),
                5);
        int max = DataConvertHelper.toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.max"),
                10);
        int bqsize = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.bqsize"), 10);

        @SuppressWarnings({ "rawtypes", "unchecked" })
        ThreadPoolExecutor exe = new ThreadPoolExecutor(core, max, 30000, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue(bqsize));

        /**
         * if the node ctrl server port conflicts, the node process will exit
         */
        nodeOperHttpServer.start(exe, port, backlog, true);

        if (log.isTraceEnable()) {
            log.info(this, "NodeOperHttpServer started");
        }
    }

    @Override
    public void stop() {

        if (nodeOperHttpServer != null) {

            nodeOperHttpServer.stop();

            if (log.isTraceEnable()) {
                log.info(this, "NodeOperHttpServer stopped");
            }
        }
        super.stop();
    }

    @Override
    public Object exchange(String eventKey, Object... data) {

        return null;
    }

    /**
     * NOTE: 由于每个节点都有NodeOperAgent所以把一些Profile级别的配置变更都放在这里
     */
    @Override
    public void onConfigUpdate(Properties updatedConfig) {

        if (updatedConfig.containsKey("log.level")) {
            String logLevel = (String) updatedConfig.get("log.level");
            this.log.setLevel(IPLogger.LogLevel.valueOf(logLevel));
        }

        if (updatedConfig.containsKey("log.debug")) {
            String debug = (String) updatedConfig.get("log.debug");
            this.log.enableDebug(Boolean.valueOf(debug));
        }
    }

}
