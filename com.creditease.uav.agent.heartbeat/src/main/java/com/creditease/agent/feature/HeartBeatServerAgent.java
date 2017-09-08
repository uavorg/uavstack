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

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.creditease.agent.feature.hbagent.HeartBeatQueryListenWorker;
import com.creditease.agent.feature.hbagent.HeartBeatServerLifeKeeper;
import com.creditease.agent.feature.hbagent.HeartBeatServerListenWorker;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.cache.api.CacheManagerFactory;

public class HeartBeatServerAgent extends AgentFeatureComponent {

    private HeartBeatServerListenWorker hbServerListenWorker;

    private HeartBeatQueryListenWorker hbqueryListenWorker;

    public HeartBeatServerAgent(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public void start() {

        // init cache manager
        String cacheServerAddress = this.getConfigManager().getFeatureConfiguration(this.feature, "store.addr");
        int minConcurrent = Integer
                .parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "store.concurrent.min"));
        int maxConcurrent = Integer
                .parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "store.concurrent.max"));
        int queueSize = Integer
                .parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "store.concurrent.bqsize"));
        String password = this.getConfigManager().getFeatureConfiguration(this.feature, "store.concurrent.pwd");

        CacheManager cm = CacheManagerFactory.build(cacheServerAddress, minConcurrent, maxConcurrent, queueSize,
                password);

        this.getConfigManager().registerComponent(this.feature, "HBCacheManager", cm);

        // start HeartBeatServerListenWorker
        hbServerListenWorker = new HeartBeatServerListenWorker("HeartBeatServerListenWorker", this.feature,
                "hbhandlers");

        int port = Integer.parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.port"));

        int backlog = Integer.parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.backlog"));
        int core = Integer.parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.core"));
        int max = Integer.parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.max"));
        int bqsize = Integer.parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.bqsize"));

        @SuppressWarnings({ "rawtypes", "unchecked" })
        ThreadPoolExecutor exe = new ThreadPoolExecutor(core, max, 30000, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue(bqsize));

        hbServerListenWorker.start(exe, port, backlog);

        if (log.isTraceEnable()) {
            log.info(this, "HeartBeatServerListenWorker started");
        }

        // start HeartBeatQueryListenWorker
        hbqueryListenWorker = new HeartBeatQueryListenWorker("HeartBeatQueryListenWorker", this.feature,
                "hbqueryhandlers");

        int qport = Integer.parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.qport"));

        @SuppressWarnings({ "rawtypes", "unchecked" })
        ThreadPoolExecutor qexe = new ThreadPoolExecutor(core, max, 30000, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue(bqsize));

        hbqueryListenWorker.start(qexe, qport, backlog);

        if (log.isTraceEnable()) {
            log.info(this, "HeartBeatQueryListenWorker started");
        }

        // start HeartBeatServerLifeKeeper
        boolean isStartLifeKeeper = Boolean
                .parseBoolean(this.getConfigManager().getFeatureConfiguration(this.feature, "lifekeeper.enable"));

        if (!isStartLifeKeeper) {
            return;
        }

        HeartBeatServerLifeKeeper hbserverLifeKeepWorker = new HeartBeatServerLifeKeeper("HeartBeatServerLifeKeeper",
                this.feature);

        long interval = Long
                .parseLong(this.getConfigManager().getFeatureConfiguration(this.feature, "lifekeeper.interval"));

        long randomDely = new Random().nextInt(3) + 3;

        this.getTimerWorkManager().scheduleWorkInPeriod("HeartBeatServerLifeKeeper", hbserverLifeKeepWorker,
                randomDely * 1000, interval);

        if (log.isTraceEnable()) {
            log.info(this, "HeartBeatServerLifeKeeper started");
        }

    }

    @Override
    public void stop() {

        // stop HeartBeatEventServerWorker
        if (null != hbServerListenWorker) {

            hbServerListenWorker.stop();

            if (log.isTraceEnable()) {
                log.info(this, "HeartBeatServerListenWorker stopped");
            }
        }

        // stop HeartBeatQueryListenWorker
        if (null != this.hbqueryListenWorker) {

            hbqueryListenWorker.stop();

            if (log.isTraceEnable()) {
                log.info(this, "HeartBeatQueryListenWorker stopped");
            }

        }

        // stop HeartBeatServerLifeKeeper
        this.getTimerWorkManager().cancel("HeartBeatServerLifeKeeper");

        // shutdown CacheManager
        CacheManager HBCacheManager = (CacheManager) this.getConfigManager().getComponent(this.feature,
                "HBCacheManager");
        HBCacheManager.shutdown();

        this.getConfigManager().unregisterComponent(this.feature, "HBCacheManager");

        if (log.isTraceEnable()) {
            log.info(this, "HeartBeatServerLifeKeeper stopped");
        }
        super.stop();
    }

    @Override
    public Object exchange(String eventKey, Object... data) {

        return null;
    }

}
