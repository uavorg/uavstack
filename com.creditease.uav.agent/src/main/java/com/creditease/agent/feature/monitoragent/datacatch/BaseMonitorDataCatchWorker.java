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

package com.creditease.agent.feature.monitoragent.datacatch;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.creditease.agent.feature.monitoragent.detect.BaseDetector;
import com.creditease.agent.helpers.NetworkHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.helpers.jvmtool.JVMAgentInfo;
import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.AbstractTimerWork;

public abstract class BaseMonitorDataCatchWorker extends AbstractTimerWork {

    public final static Map<String, String> appServerWorkIdFromSystemPro = new HashMap<String, String>();

    static {
        appServerWorkIdFromSystemPro.put("Tomcat", "catalina.");
        appServerWorkIdFromSystemPro.put("Jetty", "jetty.");
    }

    public class WorkerState {

        // the number of re-access jvm agent info, if over 3, the worker should stop and notify
        private int count_reaccessinfo = 1;

        // the latest timestamp when get the profile data for this server
        private long timestamp_profile = 0;

        public int getReAccessCount() {

            return count_reaccessinfo;
        }

        public void decreReAccessCount() {

            --count_reaccessinfo;
        }

        public long getProfileTimestamp() {

            return this.timestamp_profile;
        }

        public void setProfileTimestamp(long t) {

            this.timestamp_profile = t;
        }
    }

    protected WorkerState state = new WorkerState();
    protected JVMAgentInfo appServerInfo;
    protected BaseDetector detector;
    protected String workerId;
    @SuppressWarnings("rawtypes")
    protected Map<String, Map> customizedMetrics = new HashMap<>();

    public BaseMonitorDataCatchWorker(String cName, String feature, JVMAgentInfo appServerInfo, BaseDetector detector) {

        super(cName, feature);
        this.detector = detector;
        this.appServerInfo = appServerInfo;
        updateAttrs(appServerInfo);
    }

    protected void doHealthReaction() {

        String title = null;
        String content = null;
        // check if appserver is dead
        if (isVMAlive(this.appServerInfo) == true) {
            title = NetworkHelper.getLocalIP() + this.getProcessInfoName() + "仍然运行中，但监测数据采集连接失败.";
            content = "失败原因：1）该应用进程实例没有安装UAV监控捕获框架。2）可能由于应用容器（物理机或虚机）IP地址变化。3）可能当前的捕获连接["
                    + this.appServerInfo.getJVMAccessURL().toString() + "]失效。";
        }
        else {
            title = NetworkHelper.getLocalIP() + this.getProcessInfoName() + "已经死掉，不能进行数据采集.";
            content = "该进程已经死掉。";
        }

        // step 1: stop catch timer
        detector.removeWorker(this.cName);

        // step 2: notify
        NotificationEvent event = new NotificationEvent(NotificationEvent.EVENT_ReAccessFAIL, title, content);
        event.addArg("serverid", this.getWorkerId());
        this.putNotificationEvent(event);
        log.err(this, title);

    }

    /**
     * check if monitorframework is installed
     * 
     * @return
     */
    protected boolean checkIfMofInstalled() {

        // step 1: if there is a flag
        if (this.appServerInfo.getSystemProperties().containsKey("com.creditease.uav.supporters") == true) {
            return true;
        }

        // // step 2: if no flag, detect mof MBean
        // if (this.isVMAlive(appServerInfo)) {
        //
        // }
        // try {
        // Set<ObjectInstance> mBeans = scanMBeans(mbsc, "com.creditease:feature=monitors,*");
        //
        // if (mBeans != null && mBeans.size() > 0) {
        // return true;
        // }
        // }
        // catch (IOException e) {
        // // ignore
        // }

        return false;
    }

    /**
     * getAppGroup
     * 
     * @return
     */
    protected String getAppGroup() {

        Properties sysPro = this.appServerInfo.getSystemProperties();

        String appGroup = sysPro.getProperty("JAppGroup", "");

        String uavMAAppGroup = System.getProperty("JAppGroup");

        /**
         * NOTE: if there is no special appgroup, use MonitorAgent appgroup
         */
        if (!StringHelper.isEmpty(uavMAAppGroup) && (StringHelper.isEmpty(appGroup)
                || "UNKNOWN".equalsIgnoreCase(appGroup))) {
            appGroup = uavMAAppGroup;
        }
        return appGroup;
    }

    public void setAppServerInfo(JVMAgentInfo agentInfo) {

        this.appServerInfo = agentInfo;
    }

    public abstract String getWorkerId();

    public abstract String getProcessInfoName();

    public abstract void updateAttrs(JVMAgentInfo appServerInfo);

    public abstract boolean isVMAlive(JVMAgentInfo appServerInfo);

    public int start() {

        return 0;
    }

    @Override
    public void cancel() {

        super.cancel();
    }

    /**
     * 多数AppServer的都是Home，Base模式
     * 
     * @return
     */
    public String getWorkerIdFromSystemProForAppServer() {

        String vendor = appServerInfo.getSystemProperties().getProperty("uav.engine.vendor");

        String prefix = appServerWorkIdFromSystemPro.get(vendor);

        String appServerHome = this.appServerInfo.getSystemProperties().getProperty(prefix + "home");
        String appServerBase = this.appServerInfo.getSystemProperties().getProperty(prefix + "base");
        String workerid = appServerHome + "---" + appServerBase;

        workerid = workerid.replace("\\", "/");

        return workerid;
    }
}
