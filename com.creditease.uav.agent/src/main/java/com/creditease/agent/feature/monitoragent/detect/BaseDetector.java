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

package com.creditease.agent.feature.monitoragent.detect;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.creditease.agent.feature.monitoragent.datacatch.BaseMonitorDataCatchWorker;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.ReflectionHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.helpers.jvmtool.JVMAgentInfo;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.agent.spi.AbstractHandleWorkComponent;
import com.creditease.agent.spi.AbstractHandler;

public abstract class BaseDetector
        extends AbstractHandleWorkComponent<MonitorDataFrame, AbstractHandler<MonitorDataFrame>> {

    protected final static Map<String, String> jvmTypes = new HashMap<String, String>();

    static {
        jvmTypes.put("Tomcat", "appserver");
        jvmTypes.put("Jetty", "appserver");
        jvmTypes.put("MSCP", "mscp");
        jvmTypes.put("SpringBoot", "springboot");
    }

    protected final Map<String, String> workerTypeClsMap = new ConcurrentHashMap<String, String>();

    protected Map<String, BaseMonitorDataCatchWorker> workers;

    protected final Map<String, JVMAgentInfo> jvmAgentInfos = new ConcurrentHashMap<String, JVMAgentInfo>();

    protected long monitorInterval = 5000;

    protected long detectInterval = 0;

    public BaseDetector(String cName, String feature, String initHandlerKey, long detectInterval) {
        super(cName, feature, initHandlerKey);

        this.monitorInterval = DataConvertHelper
                .toLong(this.getConfigManager().getFeatureConfiguration(this.feature, "monitor.interval"), 5000);

        this.detectInterval = detectInterval;
    }

    /**
     * register DataCatchWorker Class Implementation
     * 
     * @param jvmType
     * @param dataCatchWorkerClass
     */
    public void register(String jvmType, String dataCatchWorkerClass) {

        if (StringHelper.isEmpty(jvmType) || StringHelper.isEmpty(dataCatchWorkerClass)) {
            return;
        }

        workerTypeClsMap.put(jvmType, dataCatchWorkerClass);
    }

    /**
     * start a new data catch worker to one target appserver
     * 
     * @param appServerInfo
     */
    protected void addWorker(JVMAgentInfo appServerInfo) {

        String processId = appServerInfo.getId();
        String workerName = "MO-" + processId;

        if (this.workers.containsKey(workerName)) {
            /**
             * If the worker exists, just update the appServerInfo this is the hyperspace dark tech, hehe, hehe, hehe :)
             */
            this.workers.get(workerName).setAppServerInfo(appServerInfo);
            return;
        }

        // start data catch worker

        // match JVM Type
        String jvmType = "unknown";

        String jvmTypeTmp = this.matchJVMType(appServerInfo);

        if (!StringHelper.isEmpty(jvmTypeTmp)) {
            jvmType = jvmTypeTmp;
        }

        BaseMonitorDataCatchWorker worker = newWoker(appServerInfo, workerName, jvmType);

        if (worker == null) {
            return;
        }

        int res = worker.start();

        if (res == -1) {
            worker.cancel();
            return;
        }

        /**
         * the app server with out UAV MonitorFramework
         */
        if (res == 0) {
            worker = newWoker(appServerInfo, workerName, "unknown");

            if (worker.start() == -1) {
                worker.cancel();
                return;
            }
        }

        this.getTimerWorkManager().scheduleWork(workerName, worker, 0, monitorInterval);

        // save this worker
        this.workers.put(workerName, worker);

        // save appserverinfo
        this.jvmAgentInfos.put(worker.getWorkerId(), appServerInfo);

        log.info(this, worker.getClass().getSimpleName() + "[" + workerName + "] started");
    }

    /**
     * Create a new DataCatchWorker
     * 
     * @param appServerInfo
     * @param workerName
     * @param jvmType
     * @return
     */
    private BaseMonitorDataCatchWorker newWoker(JVMAgentInfo appServerInfo, String workerName, String jvmType) {

        BaseMonitorDataCatchWorker worker = null;

        String bmdcWorkerCls = workerTypeClsMap.get(jvmType);

        if (bmdcWorkerCls == null) {
            return worker;
        }

        worker = (BaseMonitorDataCatchWorker) ReflectionHelper.newInstance(bmdcWorkerCls,
                new Class<?>[] { String.class, String.class, JVMAgentInfo.class, BaseDetector.class },
                new Object[] { workerName, this.feature, appServerInfo, this },
                this.getConfigManager().getFeatureClassLoader(this.feature));

        return worker;
    }

    public void stop() {

        log.info(this, "AppServerMonitorDetector[" + this.cName + "] stopped");

        // cancel all worker timer
        for (BaseMonitorDataCatchWorker worker : workers.values()) {

            // cancel worker
            worker.cancel();

            this.jvmAgentInfos.remove(worker.getWorkerId());

            log.info(this, "MonitorDataCatchWorker[" + worker.getName() + "] stopped");
        }

        workers.clear();
    }

    public void removeWorker(String workName) {

        if (workName == null) {
            return;
        }

        if (this.workers.containsKey(workName)) {
            BaseMonitorDataCatchWorker worker = this.workers.remove(workName);

            worker.cancel();

            this.jvmAgentInfos.remove(worker.getWorkerId());

            log.info(this, "MonitorDataCatchWorker[" + worker.getName() + "] stopped");
        }
    }

    /**
     * check if one appserver is worked by one AppServerMonitorDataCatchWorker that means the appserver is also alive
     * 
     * @param appServerId
     * @return
     */
    public boolean checkAppServerInWorkingById(String appServerId) {

        boolean isInWork = false;

        if (appServerId == null || "".equals(appServerId)) {
            return isInWork;
        }

        for (BaseMonitorDataCatchWorker worker : workers.values()) {
            if (appServerId.equals(worker.getWorkerId())) {
                isInWork = true;
                break;
            }
        }

        return isInWork;
    }

    /**
     * get the AppServer JVM Info
     * 
     * @param serverId
     * @return
     */
    public JVMAgentInfo getJVMAgentInfo(String serverId) {

        if (null == serverId || "".equals(serverId)) {
            return null;
        }

        return this.jvmAgentInfos.get(serverId);
    }

    /**
     * match jvm type
     * 
     * @param appServerInfo
     * @return
     * @throws RuntimeException
     */
    protected String matchJVMType(JVMAgentInfo appServerInfo) throws RuntimeException {

        String vendor = appServerInfo.getSystemProperties().getProperty("uav.engine.vendor");

        String jvmType = "unknown";

        String jvmTypeTemp = jvmTypes.get(vendor);

        if (!StringHelper.isEmpty(jvmTypeTemp)) {
            jvmType = jvmTypeTemp;
        }

        return jvmType;
    }
	
     /**
     * @param set the workers 
     */
    protected void setWorkers(Map<String, BaseMonitorDataCatchWorker> workers) {
    
        this.workers = workers;
    }
}
