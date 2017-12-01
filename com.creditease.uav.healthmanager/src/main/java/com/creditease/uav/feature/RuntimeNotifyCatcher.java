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

package com.creditease.uav.feature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.aredis.cache.AsyncRedisConnection;
import org.uavstack.resources.common.messaging.StandardMessagingBuilder;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.agent.spi.I1NQueueWorker;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.cache.api.CacheManagerFactory;
import com.creditease.uav.feature.runtimenotify.RuntimeNotifySliceMgr;
import com.creditease.uav.feature.runtimenotify.Slice;
import com.creditease.uav.feature.runtimenotify.StrategyJudgement;
import com.creditease.uav.feature.runtimenotify.http.RuntimeNotifyServerWorker;
import com.creditease.uav.feature.runtimenotify.scheduler.NodeInfoWatcher;
import com.creditease.uav.feature.runtimenotify.scheduler.RuntimeNotifyStrategyMgr;
import com.creditease.uav.feature.runtimenotify.task.JudgeNotifyTask;
import com.creditease.uav.messaging.api.MessageConsumer;

public class RuntimeNotifyCatcher extends AgentFeatureComponent {

    public static final String UAV_CACHE_REGION = "store.region.uav";

    public static final String CACHE_MANAGER_NAME = "RuntimeNotifyCacheManager";
    public static final String QWORKER_NAME = "RuntimeNotifyCatcher.JudgeWorker";
    public static final String STRATEGY_SEPARATOR = "@";

    public static final String STORAGE_CACHE_MANAGER_NAME = "RNStorageCacheManager";

    private static final String COMSUMER_RUNTIME = "runtimeDataConsumer";

    private static final long DROP_TIMEOUT = 1000L * 60 * 60;

    private MessageConsumer runtimeDataConsumer;

    private RuntimeNotifyServerWorker runtimeNotifyServerWorker;

    private Thread qwThread;

    public RuntimeNotifyCatcher(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public void start() {

        new StrategyJudgement("StrategyJudgement", this.feature);

        // reset aredis bootstrap executor pool size
        AsyncRedisConnection.setBootstrapExecutorPoolSize(getCfgInt("cm.bootstrappoolsize", 10));

        // init cache manager
        String cacheServer = getCfg("cm.server");
        int cmMinSize = getCfgInt("cm.minsize", 5);
        int cmMaxSize = getCfgInt("cm.maxsize", 10);
        int cmQueueSize = getCfgInt("cm.qsize", 5);
        String cmPwd = getCfg("cm.pwd");
        CacheManager cm = CacheManagerFactory.build(cacheServer, cmMinSize, cmMaxSize, cmQueueSize, cmPwd);
        getConfigManager().registerComponent(this.feature, CACHE_MANAGER_NAME, cm);
        if (log.isTraceEnable()) {
            log.info(this,
                    String.format("RuntimeNotifyCatcher-CacheManager INIT: server:%s,minsize:%d,maxsize:%d,qsize:%d",
                            cacheServer, cmMinSize, cmMaxSize, cmQueueSize));
        }

        // init slice storage cm
        String storeCMServer = getCfg("storecm.server");
        int storeCMMinSize = getCfgInt("storecm.minsize", 5);
        int storeCMMaxSize = getCfgInt("storecm.maxsize", 10);
        int storeCMQueueSize = getCfgInt("storecm.qsize", 5);
        String storeCMPwd = getCfg("storecm.pwd");
        CacheManager storeCM = CacheManagerFactory.build(storeCMServer, storeCMMinSize, storeCMMaxSize,
                storeCMQueueSize, storeCMPwd);
        getConfigManager().registerComponent(this.feature, STORAGE_CACHE_MANAGER_NAME, storeCM);
        if (log.isTraceEnable()) {
            log.info(this,
                    String.format(
                            "RuntimeNotifyCatcher-StorageCacheManager INIT: server:%s,minsize:%d,maxsize:%d,qsize:%d",
                            storeCMServer, storeCMMinSize, storeCMMaxSize, storeCMQueueSize));
        }

        // init strategyMgr
        String strategy = getCfg("strategy.config");
        RuntimeNotifyStrategyMgr rtNotifyStrategyMgr = new RuntimeNotifyStrategyMgr("RuntimeNotifyStrategyMgr", feature,
                cm);
        /**
         * NOTE: this setting is only for development testing for production env, the strategy is only read from cache
         */
        if (strategy != null) {
            rtNotifyStrategyMgr.loadStrategy(strategy);
        }

        long strategyPeriod = DataConvertHelper
                .toLong(this.getConfigManager().getFeatureConfiguration(this.feature, "strategy.interval"), 30000);

        this.getTimerWorkManager().scheduleWork("RuntimeNotifyStrategyMgr", rtNotifyStrategyMgr, 0, strategyPeriod);

        if (log.isTraceEnable()) {
            log.info(this, "RuntimeNotifyCatcher-RuntimeNotifyStrategyMgr started: " + strategy);
        }

        // init SliceMgr
        String sliceConfig = getCfg("slice.config");
        RuntimeNotifySliceMgr sliceMgr = new RuntimeNotifySliceMgr("RuntimeNotifySliceMgr", feature, storeCM);
        sliceMgr.loadSliceConfig(sliceConfig);

        if (log.isTraceEnable()) {
            log.info(this, "RuntimeNotifyCatcher-RuntimeNotifySliceMgr started: " + sliceConfig);
        }

        // init 1+N qworker
        int coreSize = getCfgInt("qworker.coresize", 5);
        int maxSize = getCfgInt("qworker.maxsize", 50);
        int bqSize = getCfgInt("qworker.bqsize", 20);
        int timeout = getCfgInt("qworker.keepalivetimeout", 60000);
        I1NQueueWorker runtimeNotifyJudgeWorker = get1NQueueWorkerMgr().newQueueWorker(QWORKER_NAME, feature, coreSize,
                maxSize, bqSize, timeout);
        qwThread = new Thread(runtimeNotifyJudgeWorker);
        qwThread.setName("RuntimeNotifyCatcher-I1NQueueWorker-MainThread");
        qwThread.start();
        if (log.isTraceEnable()) {
            log.info(this,
                    String.format(
                            "RuntimeNotifyCatcher-I1NQueueWorker[" + QWORKER_NAME
                                    + "] started: coresize:%d,maxsize:%d,bqsize:%d,keepalivetimeout:%d",
                            coreSize, maxSize, bqSize, timeout));
        }

        // start runtime notify data consumer
        StandardMessagingBuilder smb = new StandardMessagingBuilder("RTNTFCommonMsgBuilder", this.feature);

        try {
            smb.init("com.creditease.uav.feature.runtimenotify.messaging.handlers");
        }
        catch (IOException e) {
            log.err(this, "Read msgtype2topic.properties FAILs, RuntimeNotifyCatcher can not START", e);
            return;
        }

        runtimeDataConsumer = smb.buildConsumer(MonitorDataFrame.MessageType.RuntimeNtf.toString());

        if (runtimeDataConsumer != null) {

            runtimeDataConsumer.start();

            this.getConfigManager().registerComponent(this.feature, COMSUMER_RUNTIME, runtimeDataConsumer);

            if (log.isTraceEnable()) {
                log.info(this, "RuntimeNotifyCacher RuntimeConsumer started");
            }
        }

        // init timer worker
        boolean isEnableInfoTimer = DataConvertHelper.toBoolean(this.getCfg("nodeinfotimer.enable"), false);

        if (isEnableInfoTimer == true) {
            int period = getCfgInt("nodeinfotimer.period", 15000);
            getTimerWorkManager().scheduleWorkInPeriod("NodeInfoWatcher",
                    new NodeInfoWatcher("NodeInfoWatcher", feature), 0, period);
            if (log.isTraceEnable()) {
                log.info(this, "RuntimeNotifyCatcher-NodeInfoWatcher started: period:" + period);
            }
        }

        // init RuntimeNotifyServerWorker
        int port = Integer.parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.port"));
        int backlog = Integer.parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.backlog"));
        int core = Integer.parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.core"));
        int max = Integer.parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.max"));
        int bqsize = Integer.parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.bqsize"));

        runtimeNotifyServerWorker = new RuntimeNotifyServerWorker("RuntimeNotifyServerWorker", feature, "rnswhandlers");

        @SuppressWarnings({ "rawtypes", "unchecked" })
        ThreadPoolExecutor exe = new ThreadPoolExecutor(core, max, 30000, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue(bqsize));

        runtimeNotifyServerWorker.start(exe, port, backlog);

        if (log.isTraceEnable()) {
            log.info(this, "RuntimeNotifyCatcher-RuntimeNotifyServerWorker started");
        }
    }

    @Override
    public void stop() {

        // stop runtimeDataConsumer
        if (runtimeDataConsumer != null) {
            runtimeDataConsumer.shutdown();

            this.getConfigManager().unregisterComponent(this.feature, COMSUMER_RUNTIME);

            if (log.isTraceEnable()) {
                log.info(this, "RuntimeNotifyCatcher RuntimeConsumer shutdown");
            }
        }

        // stop runtime server worker

        runtimeNotifyServerWorker.stop();

        if (log.isTraceEnable()) {
            log.info(this, "RuntimeNotifyCatcher-RuntimeNotifyServerWorker stopped");
        }

        // stop nodeinfotimer
        boolean isEnableInfoTimer = DataConvertHelper.toBoolean(this.getCfg("nodeinfotimer.enable"), false);

        if (isEnableInfoTimer == true) {
            getTimerWorkManager().cancel("NodeInfoWatcher");
            if (log.isTraceEnable()) {
                log.info(this, "RuntimeNotifyCatcher-NodeInfoWatcher stopped");
            }
        }

        // stop 1+N worker
        if (qwThread != null && qwThread.isAlive()) {
            qwThread.interrupt();
        }

        get1NQueueWorkerMgr().shutdown(this.feature, QWORKER_NAME);
        if (log.isTraceEnable()) {
            log.info(this, "RuntimeNotifyCatcher-I1NQueueWorker[" + QWORKER_NAME + "] stopped");
        }

        // stop RuntimeNotifyStrategyMgr
        this.getTimerWorkManager().cancel("RuntimeNotifyStrategyMgr");

        if (log.isTraceEnable()) {
            log.info(this, "RuntimeNotifyCatcher-RuntimeNotifyStrategyMgr stopped");
        }

        // shutdown CacheManager
        CacheManager RNStorageCacheManager = (CacheManager) this.getConfigManager().getComponent(this.feature,
                STORAGE_CACHE_MANAGER_NAME);
        RNStorageCacheManager.shutdown();

        CacheManager RuntimeNotifyCacheManager = (CacheManager) this.getConfigManager().getComponent(this.feature,
                CACHE_MANAGER_NAME);
        RuntimeNotifyCacheManager.shutdown();

        if (log.isTraceEnable()) {
            log.info(this, "RuntimeNotifyCatcher-CacheManager stopped");
        }

        this.getConfigManager().unregisterComponent(this.feature, "StrategyJudgement");

        super.stop();
    }

    @Override
    public Object exchange(String eventKey, Object... data) {

        switch (eventKey) {
            case "runtime.notify":

                pushToSliceQueues((String) data[0], (Boolean) data[1]);

                break;
            default:
                log.err(this, "exchange illegal eventKey: " + eventKey);
                break;
        }
        return null;
    }

    private String getCfg(String key) {

        return getConfigManager().getFeatureConfiguration(feature, key);
    }

    private int getCfgInt(String key, int defaultValue) {

        return DataConvertHelper.toInt(getCfg(key), defaultValue);
    }

    /**
     * pushToSliceQueues
     * 
     * @param string
     */
    private void pushToSliceQueues(String jsonStr, boolean needConcurrent) {

        List<String> mdfJsonList = JSONHelper.toObjectArray(jsonStr, String.class);

        for (String mdfJson : mdfJsonList) {

            MonitorDataFrame mdf = new MonitorDataFrame(mdfJson);

            pushToSliceQueue(mdf, needConcurrent);

        }
    }

    private void pushToSliceQueue(MonitorDataFrame mdf, boolean needConcurrent) {

        long now = System.currentTimeMillis();
        if (now - mdf.getTimeFlag() > DROP_TIMEOUT) {
            if (log.isDebugEnable()) {
                log.debug(this, "MDF too late, drop it: " + mdf.toJSONString());
            }

            return;
        }

        // step 1: store slices
        List<Slice> slices = extractSlice(mdf);

        RuntimeNotifySliceMgr rnsm = (RuntimeNotifySliceMgr) ConfigurationManager.getInstance()
                .getComponent(this.feature, "RuntimeNotifySliceMgr");
        rnsm.storeSlices(slices, mdf.getTag());

        if (log.isDebugEnable()) {
            log.debug(this, "Prepare Slices for Judge: count= " + slices.size());
        }

        // step 2: notification judge
        if (needConcurrent == false) {
            // one thread judge
            for (Slice slice : slices) {
                new JudgeNotifyTask(JudgeNotifyTask.class.getSimpleName(), "runtimenotify", slice).run();
            }
        }
        else {
            // 1+N Queue Judge
            I1NQueueWorker n1nqw = get1NQueueWorkerMgr().getQueueWorker(this.feature,
                    RuntimeNotifyCatcher.QWORKER_NAME);
            for (Slice slice : slices) {
                n1nqw.put(new JudgeNotifyTask(JudgeNotifyTask.class.getSimpleName(), feature, slice));
            }
        }
    }

    /**
     * extractSlice
     * 
     * @param mdf
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private List<Slice> extractSlice(MonitorDataFrame mdf) {

        List<Slice> list = new ArrayList<>();
        Map<String, List<Map>> frames = mdf.getDatas();

        for (Map.Entry<String, List<Map>> frame : frames.entrySet()) {
            List<Map> servers = frame.getValue();
            for (Map server : servers) {
                String meId = (String) server.get("MEId");
                List<Map> instances = (List<Map>) server.get("Instances");

                for (Map ins : instances) {
                    String id = (String) ins.get("id");
                    Map values = (Map) ins.get("values");

                    String key = frame.getKey() + RuntimeNotifyCatcher.STRATEGY_SEPARATOR + meId
                            + RuntimeNotifyCatcher.STRATEGY_SEPARATOR + id;
                    Slice slice = new Slice(key, mdf.getTimeFlag());
                    slice.setArgs(values);
                    slice.setMdf(mdf);

                    list.add(slice);
                }
            }
        }

        if (log.isDebugEnable()) {
            log.debug(this, "RuntimeNotify Extract Slices: slices size=" + list.size());
        }

        return list;
    }
}
