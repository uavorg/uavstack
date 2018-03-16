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

package com.creditease.uav.threadanalysis.server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.uav.elasticsearch.client.ESClient;
import com.creditease.uav.threadanalysis.http.ThreadAnalysisQueryServerWorker;
import com.creditease.uav.threadanalysis.server.collect.ThreadAnalysisCollectDataHandler;

/**
 * 线程分析Consumer类
 * 
 * @author xinliang
 *
 */
public class ThreadAnalysisServer extends AgentFeatureComponent {

    private ThreadAnalysisQueryServerWorker jtaQueryServerWorker;

    public ThreadAnalysisServer(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public void start() {

        // init ES Client
        String esAddrStr = this.getConfigManager().getFeatureConfiguration(this.feature, "es.addr");
        String clusterName = this.getConfigManager().getFeatureConfiguration(this.feature, "es.clustername");
        ESClient client = new ESClient(esAddrStr, clusterName);

        this.getConfigManager().registerComponent(this.feature, "ESClient", client);

        // register thread analysis index mgr
        new ThreadAnalysisIndexMgr("ThreadAnalysisIndexMgr", feature);

        // 初始化线程分析Handler对象
        new ThreadAnalysisCollectDataHandler("JQ_JTA_CollectHandler", feature);

        // run ThreadAnalysisQueryServerWorker
        boolean isStartQueryServer = DataConvertHelper
                .toBoolean(this.getConfigManager().getFeatureConfiguration(this.feature, "http.enable"), true);

        if (isStartQueryServer == true) {

            int port = DataConvertHelper
                    .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.port"), 5566);
            int backlog = DataConvertHelper
                    .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.backlog"), 10);
            int core = DataConvertHelper
                    .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.core"), 10);

            int max = DataConvertHelper.toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.max"),
                    100);
            int bqsize = DataConvertHelper
                    .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.bqsize"), 10);

            jtaQueryServerWorker = new ThreadAnalysisQueryServerWorker("ThreadAnalysisQueryServerWorker", feature,
                    "qhandlers");

            ThreadPoolExecutor executor = new ThreadPoolExecutor(core, max, 30, TimeUnit.SECONDS,
                    new ArrayBlockingQueue<Runnable>(bqsize));

            jtaQueryServerWorker.start(executor, port, backlog);

            if (log.isTraceEnable()) {
                log.info(this, "ThreadAnalysisQueryServerWorker started");
            }
        }

        // deep thread analysis
        new ThreadAnalyser("ThreadAnalyser", feature);
    }

    @Override
    public Object exchange(String eventKey, Object... data) {

        return null;
    }

    @Override
    public void stop() {

        // close ESClient
        ESClient client = (ESClient) this.getConfigManager().getComponent(this.feature, "ESClient");

        if (client != null) {
            client.close();
        }

        // stop ThreadAnalysisQueryServerWorker
        if (jtaQueryServerWorker != null) {
            jtaQueryServerWorker.stop();
            if (log.isTraceEnable()) {
                log.info(this, "ThreadAnalysisQueryServerWorker stop");
            }
        }

        super.stop();
    }

}
