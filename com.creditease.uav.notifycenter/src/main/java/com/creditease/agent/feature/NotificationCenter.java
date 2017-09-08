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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.creditease.agent.feature.notifycenter.NCConstant;
import com.creditease.agent.feature.notifycenter.NCHttpServerWorker;
import com.creditease.agent.feature.notifycenter.NCJudgementWorker;
import com.creditease.agent.feature.notifycenter.actions.CEMailAction;
import com.creditease.agent.feature.notifycenter.actions.HttpCallAction;
import com.creditease.agent.feature.notifycenter.actions.JavaMailAction;
import com.creditease.agent.feature.notifycenter.actions.PushNotifyEventAction;
import com.creditease.agent.feature.notifycenter.actions.SMSAction;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.agent.spi.I1NQueueWorker;
import com.creditease.agent.spi.I1NQueueWorkerMgr;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.cache.api.CacheManagerFactory;

/**
 * @author peihua
 */
public class NotificationCenter extends AgentFeatureComponent {

    private IActionEngine engine;
    private ExecutorService notifyCenter_ncj;
    private ExecutorService notifyCenter_inqw;

    private NCHttpServerWorker ncServerListenWorker;

    public NotificationCenter(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public void start() {

        engine = this.getActionEngineMgr().newActionEngine("NotifyActionEngine", feature);

        // init actions
        new SMSAction(NCConstant.ACTION4SMS, this.feature, engine);

        String mailProvider = this.getConfigManager().getFeatureConfiguration(this.feature, "nc.notify.mail.provider");
        if (StringHelper.isEmpty(mailProvider) || "CEMail".equals(mailProvider)) {
            new CEMailAction(NCConstant.ACTION4MAIL, this.feature, engine);
        }

        if ("JavaMail".equals(mailProvider)) {
            new JavaMailAction(NCConstant.ACTION4MAIL, this.feature, engine);
        }

        new HttpCallAction(NCConstant.ACTION4HTTP, this.feature, engine);
        new PushNotifyEventAction(NCConstant.ACTION4PUSHNTF, this.feature, engine);

        String cacheServerAddress = this.getConfigManager().getFeatureConfiguration(this.feature, "nc.cache.addr");
        String password = this.getConfigManager().getFeatureConfiguration(this.feature, "nc.cache.concurrent.pwd");
        int minConcurrent = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "nc.cache.concurrent.min"), 10);
        int maxConcurrent = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "nc.cache.concurrent.max"), 20);
        int queueSize = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "nc.cache.concurrent.bqsize"), 10);

        // cache manager
        CacheManager cm = CacheManagerFactory.build(cacheServerAddress, minConcurrent, maxConcurrent, queueSize,
                password);

        this.getConfigManager().registerComponent(this.feature, "NCCacheManager", cm);

        // start I1NQueueWorkerMgr
        int coreSize = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "inqw.coreSize"), 10);
        int maxSize = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "inqw.maxSize"), 10);
        int bQueueSize = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "inqw.bQueueSize"), 10000);
        int keepAliveTimeout = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "inqw.keepAliveTimeout"), 10000);

        I1NQueueWorkerMgr mgt = this.get1NQueueWorkerMgr();

        I1NQueueWorker INqueueworker = mgt.newQueueWorker(NCConstant.NC1NQueueWorkerName, this.feature, coreSize,
                maxSize, bQueueSize, keepAliveTimeout);

        notifyCenter_inqw = Executors.newSingleThreadExecutor();
        notifyCenter_inqw.execute(INqueueworker);

        if (log.isTraceEnable()) {
            log.info(this, NCConstant.NC1NQueueWorkerName + " started");
        }

        // start NCJudgementWorker
        NCJudgementWorker ncj = new NCJudgementWorker("NCJudgementWorker", this.feature, "notifycenterhandlers");

        notifyCenter_ncj = Executors.newSingleThreadExecutor();
        notifyCenter_ncj.execute(ncj);

        if (log.isTraceEnable()) {
            log.info(this, "NCJudgementWorker started");
        }

        // Start the NC Http service
        int port = DataConvertHelper.toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.port"),
                8766);
        int backlog = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.backlog"), 10);
        int core = DataConvertHelper.toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.core"),
                10);
        int max = DataConvertHelper.toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.max"),
                50);
        int bqsize = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.bqsize"), 10);

        ncServerListenWorker = new NCHttpServerWorker("NCHttpServerWorker", this.feature, "nchttpHandler");

        @SuppressWarnings({ "rawtypes", "unchecked" })
        ThreadPoolExecutor exe = new ThreadPoolExecutor(core, max, 30000, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue(bqsize));

        ncServerListenWorker.start(exe, port, backlog);

        if (log.isTraceEnable()) {
            log.info(this, "NCHttpServerWorker started");
        }

    }

    @Override
    public void stop() {

        // stop NotificationCenterManager

        notifyCenter_ncj.shutdownNow();

        if (log.isTraceEnable()) {
            log.info(this, "NCJudgementWorker stopped");
        }

        ncServerListenWorker.stop();

        if (log.isTraceEnable()) {
            log.info(this, "NCHttpServerWorker stopped");
        }

        notifyCenter_inqw.shutdownNow();

        this.get1NQueueWorkerMgr().getQueueWorker(this.feature, NCConstant.NC1NQueueWorkerName).shutdown();

        if (log.isTraceEnable()) {
            log.info(this, NCConstant.NC1NQueueWorkerName + " stopped");
        }

        this.getConfigManager().unregisterComponent(this.feature, "NCCacheManager");

        if (engine != null) {
            engine.clean();
        }

        super.stop();
    }

    @Override
    public Object exchange(String eventKey, Object... data) {

        if (null == data) {
            return null;
        }

        switch (eventKey) {
            case "notify.center.put":
                return putNotificationEventToNC(data);
        }

        throw new RuntimeException("Exchange Event [" + eventKey + "] handle FAIL: data=" + data);
    }

    /**
     * putNotificationEventToNC
     * 
     * @param data
     * @return
     */
    private Object putNotificationEventToNC(Object... data) {

        if (data == null || data.length != 1) {
            log.err(this, "data is null in putNotificationEventToNC");
            return null;
        }

        String notifyValue = (String) data[0];

        List<String> notifyValueString = JSONHelper.toObjectArray(notifyValue, String.class);

        for (String eventString : notifyValueString) {

            if (log.isDebugEnable()) {
                log.debug(this, "NCEvent:" + eventString);
            }

            NotificationEvent event = new NotificationEvent(eventString);

            NCJudgementWorker ncj = (NCJudgementWorker) this.getConfigManager().getComponent(this.feature,
                    "NCJudgementWorker");

            ncj.putData(event);
        }

        return null;
    }

    /**
     * 执行NotifyAction
     * 
     * @param actionSet
     * @param event
     */
    public void executeNotifyAction(Map<String, String> actionMap, NotificationEvent event) {

        for (String actionkey : actionMap.keySet()) {

            ActionContext ac = new ActionContext();

            ac.putParam("event", event);
            ac.putParam(NCConstant.ACTIONVALUE, actionMap.get(actionkey));

            ac = engine.execute(actionkey, ac);

            String stateStr = (ac.isSucessful() == true) ? "SUCCESS" : "FAIL";

            if (log.isTraceEnable()) {
                log.info(this, "NotificationTask Action[" + actionkey + "] " + stateStr);
            }
        }
    }

}
