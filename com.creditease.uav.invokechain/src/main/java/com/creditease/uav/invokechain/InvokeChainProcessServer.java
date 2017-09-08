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

package com.creditease.uav.invokechain;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.cache.api.CacheManagerFactory;
import com.creditease.uav.elasticsearch.client.ESClient;
import com.creditease.uav.invokechain.collect.InvokeChainDataCollectHandler;
import com.creditease.uav.invokechain.collect.SlowOperDataCollectHandler;
import com.creditease.uav.invokechain.http.InvokeChainQueryServerWorker;

public class InvokeChainProcessServer extends AgentFeatureComponent {

    private InvokeChainQueryServerWorker ivcQueryServerWorker;

    public InvokeChainProcessServer(String cName, String feature) {
        super(cName, feature);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void start() {

        // init cache manager
        String cacheServerAddress = this.getConfigManager().getFeatureConfiguration(this.feature, "store.addr");
        int minConcurrent = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "store.concurrent.min"), 10);
        int maxConcurrent = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "store.concurrent.max"), 50);
        int queueSize = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "store.concurrent.bqsize"), 5);
        String password = this.getConfigManager().getFeatureConfiguration(this.feature, "store.concurrent.pwd");

        CacheManager cm = CacheManagerFactory.build(cacheServerAddress, minConcurrent, maxConcurrent, queueSize,
                password);

        this.getConfigManager().registerComponent(this.feature, "IVCCacheManager", cm);

        // init ES Client
        String esAddrStr = this.getConfigManager().getFeatureConfiguration(this.feature, "es.addr");
        String clusterName = this.getConfigManager().getFeatureConfiguration(this.feature, "es.clustername");
        ESClient client = new ESClient(esAddrStr, clusterName);

        this.getConfigManager().registerComponent(this.feature, "ESClient", client);

        // register invokechain index mgr
        new InvokeChainIndexMgr("InvokeChainIndexMgr", feature);

        // register slowoper index mgr
        new SlowOperIndexMgr("SlowOperIndexMgr", feature);

        // register invokechain collect handler
        new InvokeChainDataCollectHandler("JQ_IVC_CollectHandler", feature);

        // 注册重调用链action engine
        this.getActionEngineMgr().newActionEngine("SlowOperActionEngine", feature);

        // register slowoper collect handler
        new SlowOperDataCollectHandler("JQ_SLW_CollectHandler", feature);

        // run InvokeChainQueryServerWorker
        boolean isStartQueryServer = DataConvertHelper
                .toBoolean(this.getConfigManager().getFeatureConfiguration(this.feature, "http.enable"), true);

        if (isStartQueryServer == true) {

            int port = DataConvertHelper
                    .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.port"), 7799);
            int backlog = DataConvertHelper
                    .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.backlog"), 10);
            int core = DataConvertHelper
                    .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.core"), 10);

            int max = DataConvertHelper.toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.max"),
                    100);
            int bqsize = DataConvertHelper
                    .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.bqsize"), 10);

            ivcQueryServerWorker = new InvokeChainQueryServerWorker("InvokeChainQueryServerWorker", feature,
                    "qhandlers");

            ThreadPoolExecutor executor = new ThreadPoolExecutor(core, max, 30, TimeUnit.SECONDS,
                    new ArrayBlockingQueue(bqsize));

            ivcQueryServerWorker.start(executor, port, backlog);
            // ivcQueryServerWorker.start(port, backlog, Runtime.getRuntime().availableProcessors() * 2, core);

            if (log.isTraceEnable()) {
                log.info(this, "InvokeChainQueryServerWorker started");
            }
        }
    }

    @Override
    public void stop() {

        // close ESClient
        ESClient client = (ESClient) this.getConfigManager().getComponent(this.feature, "ESClient");

        if (client != null) {
            client.close();
        }

        // stop InvokeChainQueryServerWorker
        if (ivcQueryServerWorker != null) {
            ivcQueryServerWorker.stop();
            if (log.isTraceEnable()) {
                log.info(this, "InvokeChainQueryServerWorker stop");
            }
        }

        // remove cacheManager
        CacheManager cm = (CacheManager) this.getConfigManager().getComponent(this.feature, "IVCCacheManager");

        if (cm != null) {
            cm.shutdown();
        }

        this.getConfigManager().unregisterComponent(this.feature, "IVCCacheManager");

        // shutdown NodeOperActionEngine
        this.getActionEngineMgr().shutdown("SlowOperActionEngine");
        super.stop();
    }

    @Override
    public Object exchange(String eventKey, Object... data) {

        return null;
    }

}
