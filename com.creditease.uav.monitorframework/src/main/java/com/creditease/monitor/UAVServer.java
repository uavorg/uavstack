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

package com.creditease.monitor;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.CaptureContext;
import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.monitor.captureframework.spi.Monitor.CapturePhase;
import com.creditease.monitor.captureframework.spi.MonitorFactory;
import com.creditease.monitor.log.Logger;
import com.creditease.uav.common.Supporter;

/**
 * 
 * @author zhen zhang
 * 
 */
public class UAVServer {

    public enum ServerVendor {
        TOMCAT("Tomcat"), JETTY("Jetty"), JBOSS("JBoss"), JSE("JSE"), MSCP("MSCP"), SPRINGBOOT("SpringBoot");

        private String name;

        private ServerVendor(String name) {
            this.name = name;
        }

        @Override
        public String toString() {

            return this.name;
        }

    }

    private static UAVServer server = new UAVServer();

    public static UAVServer instance() {

        return server;
    }

    private Logger log;
    private Map<String, Object> appServerInfo = new ConcurrentHashMap<String, Object>();

    private Map<String, Supporter> supporters = new ConcurrentHashMap<String, Supporter>();

    private UAVMonitor monitor;

    private UAVMetaDataMgr metaMgr;

    private UAVServer() {

    }

    /**
     * put application server info as global scope
     * 
     * @param key
     * @param val
     */
    public void putServerInfo(String key, Object val) {

        if (null == key || "".equals(key) || null == val) {
            return;
        }

        appServerInfo.put(key, val);
    }

    /**
     * get application server info as global scope
     * 
     * @param key
     * @param val
     */
    public Object getServerInfo(String key) {

        if (null == key || "".equals(key)) {
            return null;
        }

        return appServerInfo.get(key);
    }

    public void setLog(Logger tlog) {

        log = tlog;
        monitor = new UAVMonitor(log, 300000);
    }

    public Logger getLog() {

        return log;
    }

    /**
     * getSupportor
     * 
     * @param name
     * @return
     */
    public Supporter getSupportor(String name) {

        if (null == name) {
            return null;
        }

        return supporters.get(name);
    }

    /**
     * 判断是否开启了名为name的supportor
     * 
     * @param name
     * @return
     */
    public boolean isExistSupportor(String name) {

        return supporters.containsKey(name);
    }

    public void start(Object[] args) {

        ServerVendor vendor = ServerVendor.MSCP;

        if (args.length > 0) {
            vendor = (ServerVendor) args[0];
            putServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR, vendor);
            System.setProperty(CaptureConstants.INFO_APPSERVER_VENDOR, vendor.toString());
        }

        log.info("<-----------------------------UAV Application Server started------------------------------------->");

        log.info("UAVClassLoader Class Map:\n" + this.getClass().getClassLoader().toString());

        String mofRoot = System.getProperty("com.creditease.uav.uavmof.root");

        String metaPath = System.getProperty("com.creditease.uav.uavmof.metapath");

        if (StringHelper.isEmpty(metaPath)) {
            metaPath = mofRoot + "/../uavmof.metadata/";
        }

        File f = new File(metaPath);

        if (!f.exists()) {
            IOHelper.createFolder(f.getAbsolutePath());
        }

        putServerInfo(CaptureConstants.INFO_MOF_METAPATH, f.getAbsolutePath() + "/");

        log.info("MetaPath: " + f.getAbsolutePath());

        metaMgr = new UAVMetaDataMgr(log);

        // load meta data
        metaMgr.loadMetaData();

        // save key system property into meta data
        saveSystemProIntoMeta();

        // start default supporters
        startDefaultSupporters();

        // start meta saved supporters
        startMetaSavedSupporters();

        log.info("UAV Application Server started");
    }

    /**
     * saveSystemProIntoMeta
     */
    private void saveSystemProIntoMeta() {

        Properties systemPros = System.getProperties();
        Map<String, Object> metaMap = new HashMap<String, Object>();
        for (String key : UAVMetaDataMgr.SystemMeta) {

            if (systemPros.containsKey(key)) {
                metaMap.put(key, systemPros.getProperty(key));
            }
        }

        metaMgr.addMetaData(metaMap);
    }

    /**
     * startMetaSavedSupporters
     */
    private void startMetaSavedSupporters() {

        String[] supporters = this.metaMgr.loadSupporterMeta();

        if (supporters == null) {
            return;
        }

        this.startSupporters(supporters, false);
    }

    /**
     * stopAllSupporters
     */
    private void stopAllSupporters() {

        Collection<Supporter> supportc = supporters.values();
        for (Supporter supporter : supportc) {

            try {
                supporter.stop();

                if (log.isLogEnabled()) {
                    log.info("Supporter[" + supporter.getClass().getName() + "] stops SUCCESS");
                }
            }
            catch (Exception e) {
                log.error("Supporter[" + supporter.getClass().getName() + "] stops FAIL", e);
            }
        }

        supporters.clear();
    }

    /**
     * stop supporters
     * 
     * @param supporterClasses
     */
    public void stopSupporters(String[] supporterClasses, boolean needPersist) {

        if (supporterClasses == null) {
            return;
        }

        for (String supporterClass : supporterClasses) {

            if (!supporters.containsKey(supporterClass)) {
                continue;
            }

            Supporter supporter = supporters.get(supporterClass);

            try {
                supporter.stop();

                if (log.isLogEnabled()) {
                    log.info("Supporter[" + supporterClass + "] stops SUCCESS");
                }
            }
            catch (Exception e) {
                log.error("Supporter[" + supporter.getClass().getName() + "] stops FAIL", e);
            }
            finally {
                this.supporters.remove(supporterClass);
            }

        }

        if (needPersist) {
            this.metaMgr.removeSupporterMeta(supporterClasses);
        }
    }

    /**
     * startDefaultSupporters
     */
    private void startDefaultSupporters() {

        String supportStr = System.getProperty("com.creditease.uav.supporters");

        if (null == supportStr || "".equals(supportStr)) {
            return;
        }

        String[] supporterClasses = supportStr.split(",");

        startSupporters(supporterClasses, false);
    }

    /**
     * start supporters
     * 
     * @param supporterClasses
     * @param cl
     */
    public Set<String> startSupporters(String[] supporterClasses, boolean needPersist) {

        ClassLoader cl = this.getClass().getClassLoader();

        Set<String> existSupporters = new HashSet<String>();

        for (String supporterClass : supporterClasses) {

            if (supporters.containsKey(supporterClass)) {
                existSupporters.add(supporterClass);
                continue;
            }

            try {
                Class<?> supporterCls = cl.loadClass(supporterClass);

                if (!Supporter.class.isAssignableFrom(supporterCls)) {
                    continue;
                }

                Object supporterObj = supporterCls.newInstance();

                Supporter supporter = Supporter.class.cast(supporterObj);

                supporters.put(supporterClass, supporter);

                supporter.start();

                if (log.isLogEnabled()) {
                    log.info("Supporter[" + supporterClass + "] starts SUCCESS");
                }

            }
            catch (Exception e) {

                supporters.remove(supporterClass);

                log.error("Supporter[" + supporterClass + "] starts FAIL", e);
            }
        }

        if (needPersist) {
            this.metaMgr.addSupporterMeta(supporterClasses);
        }

        return existSupporters;
    }

    public void stop() {

        // stop supporters
        stopAllSupporters();

        // clear appServerInfo
        appServerInfo.clear();

        log.info("UAV Application Server stopped");
    }

    /**
     * runMonitorCaptureOnServerCapPoint
     * 
     * @param captureId
     * @param phase
     * @param contextParams
     */
    public void runMonitorCaptureOnServerCapPoint(String captureId, CapturePhase phase,
            Map<String, Object> contextParams) {

        long st = System.currentTimeMillis();

        Monitor[] monitors = MonitorFactory.instance().getServerCapPointBindMonitors(captureId, phase);

        if (monitors == null || monitors.length == 0) {
            return;
        }

        String contextTag = null;

        if (contextParams != null) {
            contextTag = (String) contextParams.get(CaptureConstants.INFO_CAPCONTEXT_TAG);
        }

        for (Monitor monitor : monitors) {
            CaptureContext context = null;

            if (null == contextTag) {
                context = monitor.getCaptureContext();
            }
            else {
                context = monitor.getCaptureContext(contextTag);
            }

            if (contextParams != null) {
                context.put(contextParams);
            }
            monitor.doCapture(captureId, context, phase);
            // release capture context when DOCAP is done
            if (phase == CapturePhase.DOCAP) {

                if (null == contextTag) {
                    monitor.releaseCaptureContext();
                }
                else {
                    monitor.releaseCaptureContext(contextTag);
                }
            }
        }

        if (this.monitor != null) {
            this.monitor.logPerf(st, "");
        }
    }

    /**
     * runMonitorAsyncCaptureOnServerCapPoint
     * 
     * @param captureId
     * @param phase
     * @param contextParams
     * @param CaptureContextMapFromAnotherThread
     * @return
     */
    public Map<String, CaptureContext> runMonitorAsyncCaptureOnServerCapPoint(String captureId, CapturePhase phase,
            Map<String, Object> contextParams, Map<String, CaptureContext> CaptureContextMapFromAnotherThread) {

        long st = System.currentTimeMillis();

        Map<String, CaptureContext> curCaptureContextMap = new HashMap<String, CaptureContext>();

        Monitor[] monitors = MonitorFactory.instance().getServerCapPointBindMonitors(captureId, phase);

        if (monitors == null || monitors.length == 0) {
            return curCaptureContextMap;
        }

        for (Monitor monitor : monitors) {
            CaptureContext context = monitor.getCaptureContext();

            /**
             * if CaptureContextMapFromAnotherThread exists, need merge CaptureContext first
             */
            if (CaptureContextMapFromAnotherThread != null) {
                CaptureContext anotherCC = CaptureContextMapFromAnotherThread.get(monitor.getId());
                if (anotherCC != null) {
                    context.put(anotherCC.getAll());
                }
            }

            if (contextParams != null) {
                context.put(contextParams);
            }

            // tmp store CaptureContext
            curCaptureContextMap.put(monitor.getId(), context);

            monitor.doCapture(captureId, context, phase);

            // release capture context when PRECAP or DOCAP is done
            monitor.releaseCaptureContext();
        }

        if (this.monitor != null) {
            this.monitor.logPerf(st, "");
        }

        return curCaptureContextMap;
    }

    /**
     * run one supporter
     * 
     * @param supporterName
     *            Supporter Class Name
     * @param methodName
     *            Supporter Class's Method Name
     * @param params
     * @return
     */
    public Object runSupporter(String supporterName, String methodName, Object... params) {

        if (StringHelper.isEmpty(supporterName) || StringHelper.isEmpty(methodName)) {
            return null;
        }

        if (!supporters.containsKey(supporterName)) {
            return null;
        }

        Supporter supporter = supporters.get(supporterName);

        Object result = null;

        try {
            result = supporter.run(methodName, params);
        }
        catch (Exception e) {
            log.error("Supporter[" + supporter.getClass().getName() + "] run FAIL", e);
        }

        return result;
    }

    public UAVMetaDataMgr getMetaMgr() {

        return metaMgr;
    }
}
