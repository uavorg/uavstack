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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ReflectionException;

import com.creditease.agent.feature.monitoragent.AppServerProfileDataCatchWorker;
import com.creditease.agent.feature.monitoragent.datacatch.BaseJMXMonitorDataCatchWorker;
import com.creditease.agent.feature.monitoragent.detect.BaseDetector;
import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.JVMToolHelper;
import com.creditease.agent.helpers.NetworkHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.helpers.jvmtool.JVMAgentInfo;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.agent.monitor.api.StandardMonitorElement;
import com.creditease.agent.monitor.api.StandardMonitorElementInstance;
import com.creditease.agent.monitor.api.StandardMonitorRepository;
import com.creditease.agent.profile.api.StandardProfileElement;
import com.creditease.agent.profile.api.StandardProfileElementInstance;
import com.creditease.agent.profile.api.StandardProfileRespository;

public class JMXJavaMonitorDataCatchWorker extends BaseJMXMonitorDataCatchWorker {

    private static interface MXBeanHandler {

        public Map<String, Object> handle(ObjectInstance oi, MBeanServerConnection mbsc);

    }

    protected static final Map<String, MXBeanHandler> MBeanClassMap = new HashMap<String, MXBeanHandler>();

    static {

        MBeanClassMap.put("java.lang.management.ClassLoadingMXBean", new MXBeanHandler() {

            @Override
            public Map<String, Object> handle(ObjectInstance oi, MBeanServerConnection mbsc) {

                return JVMToolHelper.readClassLoadUsage(oi, mbsc);
            }

        });

        MBeanClassMap.put("com.sun.management.GarbageCollectorMXBean", new MXBeanHandler() {

            @Override
            public Map<String, Object> handle(ObjectInstance oi, MBeanServerConnection mbsc) {

                return JVMToolHelper.readGCUsage(oi, mbsc);
            }

        });
        MBeanClassMap.put("java.lang.management.MemoryMXBean", new MXBeanHandler() {

            @Override
            public Map<String, Object> handle(ObjectInstance oi, MBeanServerConnection mbsc) {

                Map<String, Object> m = new LinkedHashMap<String, Object>();

                m.putAll(JVMToolHelper.readHeapUsage(oi, mbsc));
                m.putAll(JVMToolHelper.readNonHeapUsage(oi, mbsc));

                return m;
            }

        });
        MBeanClassMap.put("java.lang.management.MemoryPoolMXBean", new MXBeanHandler() {

            @Override
            public Map<String, Object> handle(ObjectInstance oi, MBeanServerConnection mbsc) {

                return JVMToolHelper.readHeapPoolUsage(oi, mbsc);
            }

        });
        MBeanClassMap.put("com.sun.management.ThreadMXBean", new MXBeanHandler() {

            @Override
            public Map<String, Object> handle(ObjectInstance oi, MBeanServerConnection mbsc) {

                return JVMToolHelper.readThreadUsage(oi, mbsc);
            }

        });

        MXBeanHandler mxbeanInst = new MXBeanHandler() {

            @Override
            public Map<String, Object> handle(ObjectInstance oi, MBeanServerConnection mbsc) {

                return JVMToolHelper.readCPUUsage(oi, mbsc);
            }

        };

        MBeanClassMap.put("com.sun.management.OperatingSystemMXBean", mxbeanInst);
        MBeanClassMap.put("com.sun.management.UnixOperatingSystemMXBean", mxbeanInst);

    }

    // java main class
    protected String javaMain;
    // java main class Name
    protected String javaMainClassName;
    // java proc id
    protected String javaProcId;
    // java app id, come from -DJAppID
    protected String javaAppId;
    // java app name, come from -DJAppName
    protected String javaAppName;
    // java run command
    protected String javaCommand;
    // java run command args
    protected String javaArgs;
    // java home
    protected String javaHome;
    // the path where the java execute
    protected String javaProcRoot;
    // localIP
    protected String localIP;

    public JMXJavaMonitorDataCatchWorker(String cName, String feature, JVMAgentInfo appServerInfo,
            BaseDetector detector) {
        super(cName, feature, appServerInfo, detector);

        collectJavaProcRunInfo(appServerInfo);
    }

    /**
     * collectJavaProcRunInfo
     * 
     * @param appServerInfo
     */
    private void collectJavaProcRunInfo(JVMAgentInfo appServerInfo) {

        javaCommand = appServerInfo.getAgentProperties().getProperty("sun.java.command");
        String[] jCommands = javaCommand.split(" ");
        javaMain = jCommands[0];

        String[] javaMainClassInfo = javaMain.split("\\.");

        javaMainClassName = javaMainClassInfo[javaMainClassInfo.length - 1];

        javaProcId = appServerInfo.getId();
        javaArgs = appServerInfo.getAgentProperties().getProperty("sun.jvm.args");

        javaAppId = appServerInfo.getSystemProperties().getProperty("JAppID");
        javaAppId = (javaAppId == null) ? javaMain : javaAppId;

        javaAppName = appServerInfo.getSystemProperties().getProperty("JAppName");

        if (javaAppName == null) {
            String[] s = javaMain.split("\\.");
            javaAppName = s[s.length - 1];
        }

        localIP = NetworkHelper.getLocalIP();

        javaHome = appServerInfo.getSystemProperties().getProperty("java.home");
        javaHome = javaHome.replace("\\", "/");

        javaProcRoot = appServerInfo.getSystemProperties().getProperty("user.dir");
    }

    @Override
    public void run() {

        /**
         * detect if the mof is installed on this jvm, if yes, the worker should be end, detector will use appserver
         * worker to handle this jvm
         */
        if (this.checkIfMofInstalled() == true) {
            detector.removeWorker(this.cName);
            return;
        }

        long timeFlag = (System.currentTimeMillis() / 10) * 10;

        // get monitor data
        boolean needProcessCheck = doCaptureMonitorData(timeFlag, mbsc);

        if (needProcessCheck == true) {

            this.doHealthReaction();

            return;
        }

        // get profile data
        doCaptureProfileData(timeFlag);
    }

    @Override
    public String getWorkerId() {

        return this.javaHome;
    }

    @Override
    public String getProcessInfoName() {

        return "JSE程序[" + this.javaHome + "---" + this.javaCommand + "]";
    }

    /**
     * --------------------------------------------COMMON----------------------------------------------------
     */

    private String getJSEURL() {

        return "jse://" + localIP + "/" + javaAppId + "-" + javaProcId;
    }

    /**
     * --------------------------------------------Profile Data----------------------------------------------------
     */
    /**
     * TODO: doCaptureProfileData
     * 
     * every 1 minute to send the profile data as heartbeat
     * 
     * @param timeFlag
     */
    private void doCaptureProfileData(long timeFlag) {

        MonitorDataFrame pmdf = new MonitorDataFrame(this.getWorkerId(), "P", timeFlag);

        if (state.getProfileTimestamp() == 0) {
            state.setProfileTimestamp(System.currentTimeMillis());
        }
        else {
            long curTime = System.currentTimeMillis();

            if (curTime - state.getProfileTimestamp() < 60000) {
                return;
            }

            pmdf.setTag("P:HB");

            state.setProfileTimestamp(curTime);
        }

        // get Profile MDF
        this.buildProfileMDF(pmdf);

        if (!pmdf.isEmpty()) {
            /**
             * as profile data is low frequency data, then we just need 1 thread for all appservers on the same host
             * machine to publish the data
             */
            AppServerProfileDataCatchWorker apdc = (AppServerProfileDataCatchWorker) this.getConfigManager()
                    .getComponent(this.feature, "AppServerProfileDataCatchWorker");

            apdc.putData(pmdf);
        }
    }

    /**
     * get profile MDF
     * 
     * @param pmdf
     */
    protected void buildProfileMDF(MonitorDataFrame pmdf) {

        doProfile_JSE(pmdf);

        // String mscpServices =null;// this.appServerInfo.getSystemProperties().getProperty("uav.mscp.services");
        //
        // // for JSE Application
        // if (mscpServices == null) {
        //
        // return;
        // }
        //
        // //TODO: for UAV MSCP Application 可能不采取这种方式
        // Map<String, String> services = JSONHelper.toObject(mscpServices, Map.class);
        //
        // Map<String, StandardProfileRespository> appProfiles = new HashMap<String, StandardProfileRespository>();
        //
        // for (String key : services.keySet()) {
        //
        // String[] sInfo = key.split("-");
        //
        // String appName = sInfo[0] + "." + sInfo[1];
        // String servletURLPattern = sInfo[2];
        // String servletURL = services.get(key);
        //
        // String[] appInfo = servletURL.split("/");
        // String appUrl = appInfo[0] + "//" + appInfo[2] + "/";
        //
        // StandardProfileRespository spr = appProfiles.get(appName);
        //
        // if (spr == null) {
        //
        // spr = new StandardProfileRespository(new String[] { "jars", "cpt", "logs" });
        //
        // // do profiling jars
        // doProfile_Jars(spr);
        //
        // // do profiling logs
        // doProfile_Logs(spr);
        //
        // // do profiling app des
        // StandardProfileElement cpt = spr.getElement("cpt");
        //
        // /**
        // * PEI:webapp 兼容JEE
        // */
        // StandardProfileElementInstance webapp = cpt.getInstance("webapp");
        //
        // String appGroup = this.getAppGroup();
        //
        // Properties sysPro = this.appServerInfo.getSystemProperties();
        //
        // String appNamePrefix = sysPro.getProperty("JAppName");
        //
        // appNamePrefix = (appNamePrefix == null) ? this.javaAppId : appNamePrefix;
        //
        // webapp.setValue("appname", appNamePrefix + "." + sInfo[1]);
        // webapp.setValue("webapproot", this.javaProcRoot);
        // webapp.setValue("appurl", appUrl);
        // webapp.setValue("appdes", "");
        // webapp.setValue("appmetrics", this.customizedMetrics);
        // webapp.setValue("appgroup", appGroup);
        //
        // // add spr
        // appProfiles.put(appName, spr);
        // }
        //
        // // do profiling MSCP Components
        // doProfile_MSCPComponents(spr, appName, servletURLPattern);
        // }
        //
        // for (String appName : appProfiles.keySet()) {
        //
        // StandardProfileRespository spr = appProfiles.get(appName);
        //
        // pmdf.addData(appName, spr.toJSONString());
        // }
    }

    // private void doProfile_MSCPComponents(StandardProfileRespository spr, String appName, String servletURLPattern) {
    //
    // StandardProfileElement cpt = spr.getElement("cpt");
    // /**
    // * PEI:webapp 兼容JEE: take MSCP http service as JEE Servlet :)
    // */
    // StandardProfileElementInstance servletPEI = cpt.getInstance("javax.servlet.annotation.WebServlet");
    //
    // Map<String, Object> sProfile = new HashMap<String, Object>();
    //
    // Map<String, Object> des = new HashMap<String, Object>();
    //
    // des.put("name", appName + "." + servletURLPattern);
    //
    // des.put("urlPatterns", new String[] { servletURLPattern });
    //
    // sProfile.put("des", des);
    //
    // servletPEI.setValue(servletURLPattern, sProfile);
    // }

    /**
     * for JSE Application
     * 
     * @param pmdf
     */
    private void doProfile_JSE(MonitorDataFrame pmdf) {

        StandardProfileRespository spr = new StandardProfileRespository(new String[] { "jars", "cpt", "logs" });

        // do profiling jars
        doProfile_Jars(spr);

        // do profileing components
        doProfile_Components(spr);

        // do profiling logs
        doProfile_Logs(spr);

        pmdf.addData(javaAppId, spr.toJSONString());
    }

    private boolean isUAVLog(String logFileStr) {

        boolean isUAVSelfLog = false;
        if (logFileStr.indexOf("uav") > -1 || logFileStr.endsWith(".0.0.log")) {
            isUAVSelfLog = true;
        }

        return isUAVSelfLog;
    }

    /**
     * doProfile_Logs
     * 
     * @param spr
     */
    private void doProfile_Logs(StandardProfileRespository spr) {

        String JAppLogs = this.appServerInfo.getSystemProperties().getProperty("JAppLogs");

        if (JAppLogs == null) {
            return;
        }

        /**
         * PEI: log {capture the logs and filter the Log patten}
         */

        Set<String> logSet = new HashSet<String>();

        StandardProfileElement logs = spr.getElement("logs");

        StandardProfileElementInstance instance = logs.getInstance("log4j");

        if (log.isDebugEnable()) {
            log.debug(this, "### JAppLogs: ###" + JAppLogs);
        }
        /***
         * 文件表达式过滤规则:
         * 
         * 1. FilePath/* ->Folder下所有文件
         * 
         * 2. FilePath/file1.txt|file2.*.log -> Folder下符合某正则表达式文件
         */
        String[] logFilesStr = JAppLogs.split(";");

        boolean selfLogEnable = false;

        String selfLogEnableConfigStr = this.getConfigManager().getFeatureConfiguration("logagent", "selfLog.enable");

        if (!StringHelper.isEmpty(selfLogEnableConfigStr)) {
            selfLogEnable = Boolean.parseBoolean(selfLogEnableConfigStr);
        }

        for (String logFileStr : logFilesStr) {

            logFileStr = logFileStr.replace("\\", "/");

            if (selfLogEnable == false && isUAVLog(logFileStr)) {

                if (log.isDebugEnable()) {
                    log.debug(this, "### This Is UAV Self Log: ###" + logFileStr);
                }
                continue;

            }

            if (logFileStr.endsWith("/*")) {
                int index = logFileStr.indexOf("/*");
                String folder = logFileStr.substring(0, index);
                List<File> files = IOHelper.getFiles(folder);
                for (File file : files) {
                    logSet.add(file.getAbsolutePath());
                }
            }
            else {
                int pathIndex = logFileStr.lastIndexOf("/");
                String rootPath = logFileStr.substring(0, pathIndex);
                String fileString = logFileStr.substring(pathIndex + 1);

                final String[] fileArray = fileString.split("\\|");

                File file = new File(rootPath);

                File[] filefiltered = file.listFiles(new FilenameFilter() {

                    @Override
                    public boolean accept(File file, String name) {

                        for (String filepatten : fileArray) {

                            String patten = filepatten.replace("*", ".+");
                            if (name.equals(filepatten) || name.matches(patten)) {

                                return true;
                            }
                        }
                        return false;
                    }

                });

                for (File filefilter : filefiltered) {
                    logSet.add(filefilter.getAbsolutePath());
                }
            }

        }
        for (Object log : logSet.toArray()) {
            instance.setValue(new File(String.valueOf(log)).getAbsolutePath(), Collections.emptyMap());
        }

        if (log.isDebugEnable()) {
            log.debug(this, "### doProfile_Logs: spr JsonString ###" + spr.toJSONString());
        }

    }

    /**
     * doProfileComponents
     * 
     * @param spr
     */
    private void doProfile_Components(StandardProfileRespository spr) {

        StandardProfileElement cpt = spr.getElement("cpt");

        /**
         * PEI:webapp 兼容JEE
         */
        StandardProfileElementInstance webapp = cpt.getInstance("webapp");

        Properties sysPro = this.appServerInfo.getSystemProperties();

        String appName = sysPro.getProperty("JAppName");

        String appGroup = this.getAppGroup();

        webapp.setValue("appname", (appName == null) ? javaMainClassName : appName);
        webapp.setValue("webapproot", this.javaProcRoot);
        webapp.setValue("appurl", getJSEURL());
        webapp.setValue("appdes", "");// this.javaCommand + " " + this.javaArgs);
        webapp.setValue("appmetrics", this.customizedMetrics);
        webapp.setValue("appgroup", appGroup);
    }

    /**
     * doProfileJars
     * 
     * @param spr
     */
    private void doProfile_Jars(StandardProfileRespository spr) {

        StandardProfileElement jars = spr.getElement("jars");

        StandardProfileElementInstance spei = jars.getInstance("lib");

        Properties sysPro = this.appServerInfo.getSystemProperties();

        String pathSep = sysPro.getProperty("path.separator");

        String jClassPath = sysPro.getProperty("java.class.path");

        if (StringHelper.isEmpty(jClassPath)) {
            return;
        }

        String[] libs = jClassPath.split(pathSep);

        for (String lib : libs) {

            String key = lib;

            int f = lib.lastIndexOf(File.separator);
            if (f > -1 && f < lib.length() - 1) {
                key = lib.substring(f + 1);
            }

            spei.setValue(key, lib);
        }
    }

    /**
     * --------------------------------------------Monitor Data----------------------------------------------------
     */
    /**
     * TODO: doCaptureMonitorData
     * 
     * @param timeFlag
     * @param mbsc
     * @return needProcessCheck
     */
    private boolean doCaptureMonitorData(long timeFlag, MBeanServerConnection mbsc) {

        StandardMonitorRepository smr = new StandardMonitorRepository();

        // MEId: jvm
        if (doCaptureMO_JVM(mbsc, smr) == true) {

            return true;
        }

        // 自定义指标 goes to jvm
        doCaptureMO_CustomizedMetrics(smr);

        // UAV MSCP Application
        doCaptureMO_MSCPApp(smr);

        // build MDF
        MonitorDataFrame mdf = new MonitorDataFrame(this.getWorkerId(), "M", timeFlag);

        mdf.addData("server", smr.toJSONString());

        // add appgroup to MDF
        mdf.addExt("appgroup", this.getAppGroup());

        /**
         * if there is data, we handle MDF using monitor data handler to process the monitor data
         */
        if (!mdf.isEmpty()) {
            List<MonitorDataFrame> ml = new ArrayList<MonitorDataFrame>();
            ml.add(mdf);
            this.detector.runHandlers(ml);
        }

        return false;
    }

    /**
     * UAV MSCP
     * 
     * @param smr
     */
    private void doCaptureMO_MSCPApp(StandardMonitorRepository smr) {

        String mscpServices = this.appServerInfo.getSystemProperties().getProperty("uav.mscp.services");

        if (mscpServices == null) {
            return;
        }

    }

    /**
     * 
     * @param mbsc
     * @param smr
     * @return
     */
    private boolean doCaptureMO_JVM(MBeanServerConnection mbsc, StandardMonitorRepository smr) {

        Set<ObjectInstance> monitorMBeans = Collections.emptySet();

        try {
            monitorMBeans = this.scanMBeans(mbsc, "java.lang:*");
        }
        catch (IOException e) {

            log.err(this, "JavaMonitorDataCatchWorker[" + this.cName + "] SCAN MBeans FAIL.", e);

            return true;
        }

        smr.addElement("jvm", "", "");

        StandardMonitorElement sme = smr.getElement("jvm", "", "");

        StandardMonitorElementInstance smi = sme.getInstance(this.getJSEURL());

        Iterator<ObjectInstance> iterator = monitorMBeans.iterator();

        while (iterator.hasNext()) {

            ObjectInstance oi = iterator.next();

            String MBeanIClass = "";
            try {
                MBeanInfo mbi = mbsc.getMBeanInfo(oi.getObjectName());

                MBeanIClass = (String) mbi.getDescriptor().getFieldValue("interfaceClassName");
            }
            catch (InstanceNotFoundException | IntrospectionException | ReflectionException | IOException e) {
                log.err(this, "JavaMonitorDataCatchWorker[" + this.cName + "] FIND MBean interfaceClassName FAIL.", e);
                continue;
            }

            if (log.isDebugEnable()) {
                // log.debug(this, "JavaMonitorDataCatchWorker[" + this.cName + "] FIND MBean interfaceClassName: "
                // + MBeanIClass + ",inst id=" + this.getJSEURL());
            }

            MXBeanHandler handler = MBeanClassMap.get(MBeanIClass);

            if (handler != null) {
                Map<String, Object> map = handler.handle(oi, mbsc);

                smi.putValues(map);
            }
        }

        return false;
    }

    /**
     * 捕获自定义实时数据指标
     * 
     * @param smr
     */
    protected void doCaptureMO_CustomizedMetrics(StandardMonitorRepository smr) {

        Enumeration<?> enumeration = this.appServerInfo.getSystemProperties().propertyNames();

        StandardMonitorElement sme = smr.getElement("jvm", "", "");

        StandardMonitorElementInstance smi = sme.getInstance(this.getJSEURL());

        this.customizedMetrics.clear();

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
                    this.customizedMetrics.put(metrics[1], JSONHelper.toObject(metrics[2], Map.class));
                }
                else {
                    this.customizedMetrics.put(metrics[1], Collections.emptyMap());
                }
            }
            catch (Exception e) {
                log.err(this, "JavaMonitorDataCatchWorker[" + this.cName + "] CHECK customized metric[" + name
                        + "] format error: cur metric=" + name, e);
                return;
            }

            // add monitor data
            String metricValStr = this.appServerInfo.getSystemProperties().getProperty(name);

            try {
                Double d = Double.parseDouble(metricValStr);

                smi.setValue(metrics[1], d);
            }
            catch (Exception e) {
                log.err(this, "JavaMonitorDataCatchWorker[" + this.cName + "] CHECK customized metric[" + name
                        + "] has no digit value: cur value=" + metricValStr, e);
            }
        }
    }

}
