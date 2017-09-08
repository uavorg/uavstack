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

package com.creditease.agent.feature.monitoragent.datacatch.jmx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import com.creditease.agent.feature.monitoragent.AppServerProfileDataCatchWorker;
import com.creditease.agent.feature.monitoragent.datacatch.BaseJMXMonitorDataCatchWorker;
import com.creditease.agent.feature.monitoragent.detect.BaseDetector;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.helpers.jvmtool.JVMAgentInfo;
import com.creditease.agent.monitor.api.MonitorDataFrame;

public class JMXAppServerMonitorDataCatchWorker extends BaseJMXMonitorDataCatchWorker {

    public JMXAppServerMonitorDataCatchWorker(String cName, String feature, JVMAgentInfo appServerInfo,
            BaseDetector detector) {
        super(cName, feature, appServerInfo, detector);
    }

    @Override
    public void run() {

        try {

            boolean needProcessCheck = true;

            long timeFlag = (System.currentTimeMillis() / 10) * 10;

            // get all monitor's MBean
            MonitorDataFrame mdf = new MonitorDataFrame(this.getWorkerId(), "M", timeFlag);

            needProcessCheck = doCaptureMonitorData(mbsc, timeFlag, mdf);

            // if needProcessCheck is still true, need see if the appserver is still alive
            if (needProcessCheck == true) {
                doHealthReaction();
                return;
            }
            else {
                /**
                 * if there is data, we handle MDF using monitor data handler to process the monitor data
                 */
                if (!mdf.isEmpty()) {
                    List<MonitorDataFrame> ml = new ArrayList<MonitorDataFrame>();
                    ml.add(mdf);
                    this.detector.runHandlers(ml);
                }
            }

            // get all profile's MBean
            MonitorDataFrame pmdf = new MonitorDataFrame(this.getWorkerId(), "P", timeFlag);

            needProcessCheck = doCaptureProfileData(mbsc, timeFlag, pmdf);

            // if needProcessCheck is still true, need see if the appserver is still alive
            if (needProcessCheck == true) {
                doHealthReaction();
                return;
            }
            else {
                /**
                 * if there is data, we handle MDF
                 */
                if (!pmdf.isEmpty()) {
                    /**
                     * as profile data is low frequency data, then we just need 1 thread for all appservers on the same
                     * host machine to publish the data
                     */
                    AppServerProfileDataCatchWorker apdc = (AppServerProfileDataCatchWorker) this.getConfigManager()
                            .getComponent(this.feature, "AppServerProfileDataCatchWorker");

                    apdc.putData(pmdf);
                }
            }

        }
        catch (IOException e) {
            // if connect fails, try process detecting
            doHealthReaction();
        }
    }

    /**
     * doCaptureProfileData
     * 
     * @param mbsc
     * @param timeFlag
     * @param pmdf
     * @return
     * @throws IOException
     */
    protected boolean doCaptureProfileData(MBeanServerConnection mbsc, long timeFlag, MonitorDataFrame pmdf)
            throws IOException {

        boolean needProcessCheck = true;

        Set<ObjectInstance> profileMBeans = scanProfileMBeans(mbsc);

        // the monitorMBeans should not be null if connection is all right, still check that in case abnormal issues
        if (doNoMBeanAction(profileMBeans, "Profile")) {
            /**
             * if the connection is not available, then the IOException will trigger process checking so if the
             * connection is OK but there is no any MBean, we notify that but still return false that means we will not
             * run process checking
             */
            return false;
        }

        Iterator<ObjectInstance> piterator = profileMBeans.iterator();
        /**
         * if there is the first to touch the profile data, we need pass it
         */
        long curTime = System.currentTimeMillis();

        boolean isRefreshTimestamp = false;

        while (piterator.hasNext()) {

            isRefreshTimestamp = false;

            ObjectInstance oi = piterator.next();
            try {
                // catch monitor data
                ObjectName profileON = oi.getObjectName();

                /**
                 * check the state is 2(finished)
                 */
                Integer needProcessCheckState = (Integer) mbsc.getAttribute(profileON, "State");
                Boolean isUpdate = (Boolean) mbsc.getAttribute(profileON, "Update");

                if (null != needProcessCheckState) {
                    needProcessCheck = false;
                }

                /**
                 * if the profile data is not finished, we need wait next time
                 */
                if (null == needProcessCheckState || needProcessCheckState != 2) {
                    continue;
                }

                /**
                 * this data is new and also need for profile heartbeat
                 */
                if (state.getProfileTimestamp() == 0 || isUpdate == true) {
                    isRefreshTimestamp = true;

                }
                /**
                 * this data is old, but need for profile heartbeat even the if Update = false, but we will still pass
                 * the profile data as a heartbeat for profiledata the heart beat interval = 1 min
                 */
                else if (isUpdate == false && curTime - state.getProfileTimestamp() > 60000) {
                    isRefreshTimestamp = true;
                    pmdf.setTag("P:HB");
                }
                else {
                    continue;
                }

                Object oData = mbsc.getAttribute(profileON, "Data");

                if (oData != null) {
                    // set Update=false
                    if (isUpdate == true) {
                        mbsc.setAttribute(profileON, new Attribute("Update", false));
                    }
                    // add data to MonitorDataFrame
                    String frameId = profileON.getKeyProperty("id");
                    pmdf.addData(frameId, oData.toString());

                    Map<String, Object> webapp = pmdf.getElemInstValues(frameId, "cpt", "webapp");

                    if (webapp.isEmpty()) {
                        continue;
                    }

                    /**
                     * 【1】AppGroup
                     * 
                     * NOTE: if there is no special appgroup, use MonitorAgent appgroup
                     */
                    String uavMAAppGroup = System.getProperty("JAppGroup");

                    String appGroup = this.appServerInfo.getSystemProperties().getProperty("JAppGroup");

                    if (!StringHelper.isEmpty(appGroup)) {
                        webapp.put("appgroup", appGroup);
                    }
                    else if (!StringHelper.isEmpty(uavMAAppGroup)) {
                        webapp.put("appgroup", uavMAAppGroup);
                    }

                    /**
                     * 【2】自定义指标
                     */
                    Enumeration<?> enumeration = this.appServerInfo.getSystemProperties().propertyNames();

                    @SuppressWarnings("rawtypes")
                    Map<String, Map> custMetrics = new HashMap<>();

                    while (enumeration.hasMoreElements()) {

                        String name = (String) enumeration.nextElement();

                        int moIndex = name.indexOf("mo@");

                        if (moIndex != 0) {
                            continue;
                        }

                        String[] metrics = null;
                        try {
                            metrics = name.split("@");

                            // add metricName to customizedMetrics
                            if (metrics.length == 3) {
                                custMetrics.put(metrics[1], JSONHelper.toObject(metrics[2], Map.class));
                            }
                            else {
                                custMetrics.put(metrics[1], Collections.emptyMap());
                            }
                        }
                        catch (Exception e) {
                            log.err(this, "AppServerMonitorDataCatchWorker[" + this.cName + "] CHECK customized metric["
                                    + name + "] format error: cur metric=" + name, e);

                        }
                    }

                    webapp.put("appmetrics", JSONHelper.toString(custMetrics));

                    /**
                     * 【3】MOFMetaData
                     */
                    String jAppMOFMeta = this.appServerInfo.getSystemProperties().getProperty("JAppMOFMetaData");

                    if (!StringHelper.isEmpty(jAppMOFMeta)) {
                        webapp.put("mofmeta", jAppMOFMeta);
                    }
                }

            }
            catch (Exception e) {
                log.err(this, "ProfileMBean[" + oi.getObjectName().toString() + "] catch data FAILs", e);
            }
        }

        if (isRefreshTimestamp == true) {
            state.setProfileTimestamp(curTime);
        }

        return needProcessCheck;
    }

    /**
     * doCaptureMonitorData
     * 
     * @param mbsc
     * @param timeFlag
     * @return
     * @throws IOException
     */
    protected boolean doCaptureMonitorData(MBeanServerConnection mbsc, long timeFlag, MonitorDataFrame mdf)
            throws IOException {

        boolean needProcessCheck = true;
        // get all monitors' MBean
        Set<ObjectInstance> monitorMBeans = scanMonitorMBeans(mbsc);

        // the monitorMBeans should not be null if connection is all right, still check that in case abnormal issues
        if (doNoMBeanAction(monitorMBeans, "Monitor")) {
            /**
             * if the connection is not available, then the IOException will trigger process checking so if the
             * connection is OK but there is no any MBean, we notify that but still return false that means we will not
             * run process checking
             */
            return false;
        }

        Iterator<ObjectInstance> iterator = monitorMBeans.iterator();
        while (iterator.hasNext()) {
            ObjectInstance oi = iterator.next();
            try {
                // catch monitor data
                ObjectName monitorON = oi.getObjectName();
                Object oData = mbsc.getAttribute(monitorON, "Data");

                if (oData != null) {
                    needProcessCheck = false;
                    // add data to MonitorDataFrame
                    mdf.addData(monitorON.getKeyProperty("id"), oData.toString());
                }

            }
            catch (Exception e) {
                log.err(this, "MonitorMBean[" + oi.getObjectName().toString() + "] catch data FAILs", e);
            }
        }

        // add appgroup
        mdf.addExt("appgroup", this.getAppGroup());

        return needProcessCheck;
    }

    /**
     * if there is no any MBean, we should notify that
     * 
     * @param monitorMBeans
     * @param prefix
     * @return
     */
    protected boolean doNoMBeanAction(Set<ObjectInstance> monitorMBeans, String prefix) {

        if (monitorMBeans == null || monitorMBeans.isEmpty()) {
            // stop catch timer, no need do any catch data action
            detector.removeWorker(this.cName);

            /**
             * we will not notify anything now, just consider there is UAV MonitorFramework
             */
            // // notify
            // String content = "AppServer[" + this.getAppServerId() + "] No " + prefix + " MBean Available";
            //
            // log.err(this, content);
            //
            // NotificationEvent event = new NotificationEvent(NotificationEvent.EVENT_NoMBean,
            // "AppServerNo" + prefix + "MBean", content);
            // event.addArg("serverid", this.getAppServerId());
            // this.putNotificationEvent(event);
            return true;
        }

        return false;
    }

    private Set<ObjectInstance> scanMonitorMBeans(MBeanServerConnection mbsc) throws IOException {

        return scanMBeans(mbsc, "com.creditease:feature=monitors,*");
    }

    private Set<ObjectInstance> scanProfileMBeans(MBeanServerConnection mbsc) throws IOException {

        return scanMBeans(mbsc, "com.creditease:feature=profiles,*");
    }

    /**
     * the app server id is something distinct string which can help identify the real app server note: process id is
     * changed time to time, so use some stable info as app server id TODO: only support tomcat yet
     * 
     * @return
     */
    @Override
    public String getWorkerId() {

        return this.getWorkerIdFromSystemProForAppServer();
    }

    @Override
    public String getProcessInfoName() {

        return "应用服务器[" + this.getWorkerId() + "]";
    }

    @Override
    public int start() {

        int res = super.start();

        if (res == 1 && this.checkIfMofInstalled() == false) {

            return 0;
        }

        return res;
    }

}
