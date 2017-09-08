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

package com.creditease.uav.healthmanager.newlog;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.creditease.agent.feature.common.messaging.StandardMessagingBuilder;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.uav.elasticsearch.client.ESClient;
import com.creditease.uav.healthmanager.newlog.http.NewLogQueryServerWorker;
import com.creditease.uav.messaging.api.MessageConsumer;

/**
 * 
 * HMNewLogService description: 全新一代的日志存储与查询服务，采用ES实现存储和查询
 *
 */
public class HMNewLogService extends AgentFeatureComponent {

    private MessageConsumer logDataConsumer;

    private NewLogQueryServerWorker queryServer;

    public HMNewLogService(String cName, String feature) {
        super(cName, feature);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void start() {

        /**
         * INIT ESClient
         */
        String esAddrStr = this.getConfigManager().getFeatureConfiguration(this.feature, "es.addr");
        String clusterName = this.getConfigManager().getFeatureConfiguration(this.feature, "es.clustername");
        ESClient client = new ESClient(esAddrStr, clusterName);
        this.getConfigManager().registerComponent(this.feature, "ESClient", client);

        /**
         * HMNewLogIndexMgr
         */
        new HMNewLogIndexMgr("HMNewLogIndexMgr", feature);

        /**
         * Log Consumer
         */
        StandardMessagingBuilder smb = new StandardMessagingBuilder("HMCommonMsgBuilder", this.feature);

        try {
            smb.init("com.creditease.uav.healthmanager.newlog.handlers");
        }
        catch (IOException e) {
            log.err(this, "Read msgtype2topic.properties FAILs, HealthManager can not START", e);
            return;
        }
        logDataConsumer = smb.buildConsumer(MonitorDataFrame.MessageType.Log.toString());

        // start logDataConsumer
        if (this.logDataConsumer != null) {
            logDataConsumer.start();

            this.getConfigManager().registerComponent(this.feature, "NewlogDataConsumer", logDataConsumer);

            if (log.isTraceEnable()) {
                log.info(this, "HMNewLogService LogConsumer started");
            }
        }

        // run NewLogQueryServerWorker
        boolean isStartQueryServer = DataConvertHelper
                .toBoolean(this.getConfigManager().getFeatureConfiguration(this.feature, "http.enable"), true);

        if (isStartQueryServer == true) {

            int port = DataConvertHelper
                    .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.port"), 7899);
            int backlog = DataConvertHelper
                    .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.backlog"), 10);
            int core = DataConvertHelper
                    .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.core"), 10);
            int max = DataConvertHelper.toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.max"),
                    100);
            int bqsize = DataConvertHelper
                    .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.bqsize"), 10);

            queryServer = new NewLogQueryServerWorker("NewLogQueryServerWorker", feature, "qhandlers");

            ThreadPoolExecutor executor = new ThreadPoolExecutor(core, max, 30, TimeUnit.SECONDS,
                    new ArrayBlockingQueue(bqsize));

            queryServer.start(executor, port, backlog);

            // queryServer.start(port, backlog, Runtime.getRuntime().availableProcessors() * 2, core);

            if (log.isTraceEnable()) {
                log.info(this, "NewLogQueryServerWorker started");
            }
        }
    }

    @Override
    public void stop() {

        // stop logDataConsumer
        if (this.logDataConsumer != null) {
            logDataConsumer.shutdown();
            this.getConfigManager().unregisterComponent(this.feature, "NewlogDataConsumer");
            if (log.isTraceEnable()) {
                log.info(this, "HMNewLogService LogConsumer shutdown");
            }
        }

        // close ESClient
        ESClient client = (ESClient) this.getConfigManager().getComponent(this.feature, "ESClient");

        if (client != null) {
            client.close();
        }

        // stop NewLogQueryServerWorker
        if (queryServer != null) {
            queryServer.stop();
            if (log.isTraceEnable()) {
                log.info(this, "NewLogQueryServerWorker stop");
            }
        }

        super.stop();
    }

    @Override
    public Object exchange(String eventKey, Object... data) {

        return null;
    }

}
