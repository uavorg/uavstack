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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.creditease.agent.feature.monitoragent.AppServerProfileDataCatchWorker;
import com.creditease.agent.feature.monitoragent.MDFListenServer;
import com.creditease.agent.feature.monitoragent.detect.DetectorManager;
import com.creditease.agent.feature.monitoragent.detect.JVMContainerOSDetector;
import com.creditease.agent.feature.monitoragent.detect.JVMLocalOSDetector;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.spi.AgentFeatureComponent;

/**
 * 实时监控数据，Profiling数据
 * 
 * @author zhen zhang
 *
 */
public class MonitorAgent extends AgentFeatureComponent {

    private Thread profileThread;

    private MDFListenServer mdfListenServer;

    public MonitorAgent(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public void start() {

        // create ApplicationServerMonitorDetector
        startAppServerMonitorDetector();

        // create AppServerProfileDataCatchWorker
        startAppServerProfileCatchWorker();

        // create MDFListenServer
        startMDSListenServer();
    }

    private void startMDSListenServer() {

        boolean isStartMDFListener = DataConvertHelper
                .toBoolean(this.getConfigManager().getFeatureConfiguration(this.feature, "http.enable"), false);

        if (isStartMDFListener == true) {

            // start MDFListenServer
            int port = Integer.parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.port"));
            int backlog = Integer
                    .parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.backlog"));
            int core = Integer.parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.core"));
            int max = Integer.parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.max"));
            int bqsize = Integer.parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.bqsize"));

            mdfListenServer = new MDFListenServer("MDFListenServer", this.feature, "mdfhandlers");

            @SuppressWarnings({ "rawtypes", "unchecked" })
            ThreadPoolExecutor exe = new ThreadPoolExecutor(core, max, 30000, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue(bqsize));

            mdfListenServer.start(exe, port, backlog);

            if (log.isTraceEnable()) {
                log.info(this, "MDFListenServer started");
            }
        }
    }

    private void startAppServerProfileCatchWorker() {

        AppServerProfileDataCatchWorker apdc = new AppServerProfileDataCatchWorker("AppServerProfileDataCatchWorker",
                this.feature, "prodatahandlers");

        // start AppServerProfileDataCatchWorker
        profileThread = new Thread(apdc);
        profileThread.start();

        if (log.isTraceEnable()) {
            log.info(this, "ApplicationServer ProfileDataCatchWorker started");
        }
    }

    private void startAppServerMonitorDetector() {

        long detectInterval = DataConvertHelper
                .toLong(this.getConfigManager().getFeatureConfiguration(this.feature, "detector.interval"), 20000);

        // init DetectorManager
        DetectorManager appServerMonitorDetector_TimerWorker = new DetectorManager(
                "AppServerMonitorDetector_TimerWorker", this.feature);        

        boolean isContainerDetectEnable = DataConvertHelper.toBoolean(
                this.getConfigManager().getFeatureConfiguration(this.feature, "detector.container.scan.enable"), false);

        if (isContainerDetectEnable == true) {
            // init JVMContainerOSDetector
            JVMContainerOSDetector cosd = new JVMContainerOSDetector("JVMContainerOSDetector", this.feature,
                    "modatahandlers", detectInterval);

            cosd.register("appserver",
                    "com.creditease.agent.feature.monitoragent.datacatch.http.HttpAppServerMonitorDataCatchWorker");
            cosd.register("mscp",
                    "com.creditease.agent.feature.monitoragent.datacatch.http.HttpMSCPMonitorDataCatchWorker");
            cosd.register("springboot",
                    "com.creditease.agent.feature.monitoragent.datacatch.http.HttpSpringBootMonitorDataCatchWorker");

            // install JVMContainerOSDetector
            appServerMonitorDetector_TimerWorker.installDetector(cosd);
        }
		
        boolean isLocalOSDetectEnable = DataConvertHelper.toBoolean(
                this.getConfigManager().getFeatureConfiguration(this.feature, "detector.local.scan.enable"), true);

        if (isLocalOSDetectEnable == true) {
            // init JVMLocalOSDetector
            JVMLocalOSDetector asmd = new JVMLocalOSDetector("JVMLocalOSDetector", this.feature, "modatahandlers",
                    detectInterval);

            asmd.register("appserver",
                    "com.creditease.agent.feature.monitoragent.datacatch.jmx.JMXAppServerMonitorDataCatchWorker");
            asmd.register("mscp",
                    "com.creditease.agent.feature.monitoragent.datacatch.jmx.JMXMSCPMonitorDataCatchWorker");
            asmd.register("unknown",
                    "com.creditease.agent.feature.monitoragent.datacatch.jmx.JMXJavaMonitorDataCatchWorker");
            asmd.register("springboot",
                    "com.creditease.agent.feature.monitoragent.datacatch.jmx.JMXSpringBootMonitorDataCatchWorker");

            // install JVMLocalOSDetector
            appServerMonitorDetector_TimerWorker.installDetector(asmd);
        }
		
        // start ApplicationServerMonitorDetector
        this.getTimerWorkManager().scheduleWork("AppServerMonitorDetector_TimerWorker",
                appServerMonitorDetector_TimerWorker, 0, detectInterval);

        if (log.isTraceEnable()) {
            log.info(this, "ApplicationServer MonitorDetector started");
        }
    }

    @Override
    public void stop() {

        // stop MDFListenServer
        if (mdfListenServer != null) {

            mdfListenServer.stop();

            if (log.isTraceEnable()) {
                log.info(this, "MDFListenServer stopped");
            }
        }

        // cancel all data catch workers
        this.getTimerWorkManager().cancel("AppServerMonitorDetector_TimerWorker");

        if (log.isTraceEnable()) {
            log.info(this, "ApplicationServer MonitorDetector stopped");
        }

        // stop
        if (profileThread != null && profileThread.isAlive()) {
            profileThread.interrupt();
        }

        if (log.isTraceEnable()) {
            log.info(this, "ApplicationServer ProfileDataCatchWorker stopped");
        }
        super.stop();
    }

    @Override
    public Object exchange(String eventKey, Object... data) {

        return null;
    }
}
