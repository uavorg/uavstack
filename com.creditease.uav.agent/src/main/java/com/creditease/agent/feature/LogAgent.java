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

import static com.creditease.agent.feature.logagent.TaildirSourceConfigurationConstants.POSITION_FILE_ROOT;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSON;
import com.creditease.agent.ConfigurationContext;
import com.creditease.agent.feature.logagent.AppServerLogPublishWorker;
import com.creditease.agent.feature.logagent.AppServerLogPublishWorkerByStream;
import com.creditease.agent.feature.logagent.RuleFilterFactory;
import com.creditease.agent.feature.logagent.TailLogContext;
import com.creditease.agent.feature.logagent.TaildirLogComponent;
import com.creditease.agent.feature.logagent.TaildirLogComponent.Status;
import com.creditease.agent.feature.logagent.actions.LogNodeOperAction;
import com.creditease.agent.feature.logagent.api.LogFilterAndRule;
import com.creditease.agent.feature.logagent.far.DefaultLogFilterAndRule;
import com.creditease.agent.feature.logagent.objects.LogPatternInfo;
import com.creditease.agent.feature.logagent.objects.LogPatternInfo.StateFlag;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.helpers.jvmtool.JVMAgentInfo;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.AbstractTimerWork;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.agent.spi.IConfigurationManager;
import com.creditease.agent.spi.IForkjoinWorker;

/**
 * 日志抓取
 * 
 * @author zhen zhang
 * 
 */
public class LogAgent extends AgentFeatureComponent {

    private static final String LOG_CFG_FILE = "logstrategy.cache";

    @SuppressWarnings("rawtypes")
    private Map<String, Map> logCfgMapping = new ConcurrentHashMap<>();

    private int spantime = 100;

    /**
     * timer to run logCatcher to catch logs
     * 
     * @author zhen zhang
     * 
     */
    private class LogCatchScheduleWorker extends AbstractTimerWork {

        public LogCatchScheduleWorker(String cName, String feature) {
            super(cName, feature);
        }

        @Override
        public void run() {

            if (null == logCatcher) {
                if (spantime++ % 100 == 0) {
                    log.warn(this, "ApplicationServer LogCatcher is not running");
                    spantime = (spantime == Integer.MAX_VALUE) ? 0 : spantime;
                }
                return;
            }

            Status status = logCatcher.process();

            if (Status.BACKOFF == status) {
                // notify
                log.warn(this, "ApplicationServer LogCatcher run with Error: status=" + Status.BACKOFF);

                String content = "Unable to tail log files";

                log.err(this, content);

                NotificationEvent event = new NotificationEvent(NotificationEvent.EVENT_LogCatchFailed,
                        "LogCatchFailure", content);
                this.putNotificationEvent(event);
            }
        }
    }

    /**
     * store all log pattern infos for applications
     * 
     * @author zhen zhang
     * 
     */
    public static class AppLogPatternInfoCollection extends LinkedHashMap<String, Map<String, LogPatternInfo>> {

        /**
         * 
         */
        private static final long serialVersionUID = -7402799091342640234L;

        public void remove(String appUUID, String logUUID) {

            if (null == appUUID || null == appUUID) {
                return;
            }

            Map<String, LogPatternInfo> lpiMap = this.get(appUUID);

            if (null == lpiMap) {
                return;
            }

            lpiMap.remove(logUUID);
            if (lpiMap.size() == 0) {
                this.remove(appUUID);
            }
        }

        public LogPatternInfo get(String appUUID, String logUUID) {

            if (null == appUUID || null == logUUID) {
                return null;
            }

            Map<String, LogPatternInfo> lpiMap = this.get(appUUID);

            if (null == lpiMap) {
                return null;
            }

            return lpiMap.get(logUUID);
        }

        public void put(LogPatternInfo info) {

            if (null == info) {
                return;
            }

            Map<String, LogPatternInfo> lpiMap = this.get(info.getAppUUID());

            if (null == lpiMap) {

                synchronized (this) {

                    lpiMap = this.get(info.getAppUUID());

                    if (null == lpiMap) {

                        lpiMap = new LinkedHashMap<String, LogPatternInfo>();

                        this.put(info.getAppUUID(), lpiMap);
                    }
                }
            }

            /**
             * NOTE: in fact, there is a situation that 2 same logpattern in one application, BUT we will not handle
             * that because: 1) This application log configuration should be corrected by developers in FUNCATION
             * TESTING 2) Profiling should merge 2 or over appenders which share the same log file into one log pattern
             * 
             */
            synchronized (lpiMap) {
                lpiMap.put(info.getUUID(), info);
            }
        }

    }

    /**
     * record last updated log profile data status key is servid+appid
     */
    private AppLogPatternInfoCollection LatestLogProfileDataMap = new AppLogPatternInfoCollection();

    /**
     * record the log profile data which may have issue key is servid+appid
     */
    private AppLogPatternInfoCollection IssueLogProfileDataMap = new AppLogPatternInfoCollection();

    private TaildirLogComponent logCatcher;

    private Thread logCatchWorkerThread = null;

    private IForkjoinWorker executor = null;

    public LogAgent(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    protected ISystemLogger getLogger(String cName, String feature) {

        return SystemLogger.getLogger("LogDataLog", feature + ".logd.%g.%u.log", "DEBUG", true, 5 * 1024 * 1024);
    }

    @Override
    public void start() {

        /**
         * start LogCatchScheduleWorker
         * 
         * timing to run logCatcher
         */
        this.getConfigManager().registerComponent(this.feature, "LogDataLog", super.log);

        LogCatchScheduleWorker catcher = new LogCatchScheduleWorker("LogCatchScheduleWorker", this.feature);
        String interval = this.getConfigManager().getFeatureConfiguration(this.feature, "interval");
        this.getTimerWorkManager().scheduleWork("LogCatchScheduleWorker", catcher, 0,
                DataConvertHelper.toLong(interval, 3000));

        if (log.isTraceEnable()) {
            log.info(this, "ApplicationServer LogCatchScheduleWorker started");
        }

        TailLogContext.getInstance().put("selfLog.enable",
                this.getConfigManager().getFeatureConfiguration(this.feature, "selfLog.enable"));

        /**
         * start AppServerLogPublishWorker
         * 
         * when logCatcher processes data done, AppServerLogPublishWorker publishes logData
         */
        log.info(this, "LogAgent LogDataFrame.enable: "
                + this.getConfigManager().getFeatureConfiguration(this.feature, "LogDataFrame.enable"));

        TailLogContext.getInstance().put("LogDataFrame.enable",
                this.getConfigManager().getFeatureConfiguration(this.feature, "LogDataFrame.enable"));

        if (TailLogContext.getInstance().getBoolean("LogDataFrame.enable")) {

            AppServerLogPublishWorkerByStream aplcbs = new AppServerLogPublishWorkerByStream(
                    "AppServerLogPublishWorker", this.feature);

            logCatchWorkerThread = new Thread(aplcbs);

        }
        else {
            AppServerLogPublishWorker aplc = new AppServerLogPublishWorker("AppServerLogPublishWorker", this.feature);

            logCatchWorkerThread = new Thread(aplc);
        }

        logCatchWorkerThread.start();

        if (log.isTraceEnable()) {
            log.info(this, "ApplicationServer LogPublishWorker started");
        }

        /**
         * TailFileProcess will process existingInodes in Muti-Thread Model
         */

        log.info(this, "LogAgent Muti Thread enabled:"
                + this.getConfigManager().getFeatureConfiguration(this.feature, "MutiThread.enable"));

        TailLogContext.getInstance().put("MutiThread.enable",
                this.getConfigManager().getFeatureConfiguration(this.feature, "MutiThread.enable"));
        TailLogContext.getInstance().put("MutiThread.thread.max",
                this.getConfigManager().getFeatureConfiguration(this.feature, "MutiThread.thread.max"));

        if (TailLogContext.getInstance().getBoolean("MutiThread.enable")) {
            executor = this.getForkjoinWorkerMgr().newForkjoinWorker("TailLogProcessPoolWorker", this.feature,
                    TailLogContext.getInstance().getInteger("MutiThread.thread.max"));
        }

        /**
         * regist node ctrl action
         */
        IActionEngine engine = this.getActionEngineMgr().getActionEngine("NodeOperActionEngine");
        new LogNodeOperAction("logstragety", feature, engine);

        loadLogCfg();
    }

    @Override
    public void stop() {

        if (null != logCatcher) {
            logCatcher.stop();
        }

        this.getTimerWorkManager().cancel("LogCatchScheduleWorker");

        if (log.isTraceEnable()) {
            log.info(this, "ApplicationServer LogCatchScheduleWorker stopped");
        }

        if (null != this.logCatchWorkerThread && this.logCatchWorkerThread.isAlive()) {
            logCatchWorkerThread.interrupt();
        }

        /**
         * shutdown TailLogProcessPoolWorker
         */
        if (TailLogContext.getInstance().getBoolean("MutiThread.enable")) {
            executor.shutdown();
        }

        this.getConfigManager().unregisterComponent(this.feature, "LogDataLog");

        if (log.isTraceEnable()) {
            log.info(this, "ApplicationServer LogPublishWorker stopped");
        }

        super.stop();

    }

    @Override
    public Object exchange(String eventKey, Object... data) {

        switch (eventKey) {
            case "logagent.profiledata.notify":
                if (null != data && data.length == 2) {
                    notifyProfileDataUpdate((MonitorDataFrame) data[0], (JVMAgentInfo) data[1]);
                }
                break;
            case "logagent.strategy":
                if (log.isTraceEnable()) {
                    log.info(this, "exchange: eventKey: " + eventKey + ", data: " + data[0]);
                }
                updateAllStrategy((String) data[0]);
                break;
            default:
                throw new RuntimeException("Exchange Event [" + eventKey + "] handle FAIL: data=" + data);
        }
        return null;
    }

    /**
     * notify there is profile data update
     * 
     * @param profileData
     */
    @SuppressWarnings("rawtypes")
    private void notifyProfileDataUpdate(MonitorDataFrame profileData, JVMAgentInfo agentInfo) {

        /**
         * Step 1: parse MDF to build NewLogPatternInfoMap
         */
        AppLogPatternInfoCollection newLogPatternInfoMap = new AppLogPatternInfoCollection();

        Map<String, LogPatternInfo> newLogPatternPathConflictMap = new HashMap<String, LogPatternInfo>();

        // jvm system properties for ${xxxx} varable
        Properties systemPro = agentInfo.getSystemProperties();
        for (Entry<String, List<Map>> e : profileData.getDatas().entrySet()) {

            // get appid
            String appID = e.getKey();

            // get webapproot
            Map<String, Object> webappInfo = profileData.getElemInstValues(appID, "cpt", "webapp");

            String webAppRoot = (String) webappInfo.get("webapproot");

            if (null == webAppRoot) {
                // notify
                String content = "Application[" + appID + "]'s profileData is incomplete: field[webapproot] is null.";

                log.warn(this, content);

                NotificationEvent event = new NotificationEvent(NotificationEvent.EVENT_LogAppProfileError,
                        "AppProfileError", content);
                event.addArg("serverid", profileData.getServerId());
                event.addArg("appid", appID);
                this.putNotificationEvent(event);

                continue;
            }

            String appurl = (String) webappInfo.get("appurl");

            // get PEId=logs
            List<Map> logsPD = profileData.getElemInstances(appID, "logs");

            for (Map logPD : logsPD) {

                Map logInstanceValues = (Map) logPD.get("values");

                for (Object keyObj : logInstanceValues.keySet()) {

                    String logPattern = (String) keyObj;

                    // figure out absolute path
                    String absPath = figureOutAbsolutePath(webAppRoot, logPattern, systemPro);

                    LogPatternInfo lpi = new LogPatternInfo();

                    // set appid
                    lpi.setAppId(appID);

                    // set servid
                    lpi.setServId(profileData.getServerId());

                    // set logpattern
                    lpi.setLogParttern(logPattern);

                    // set webapproot
                    lpi.setWebAppRoot(webAppRoot);

                    // set appurl
                    lpi.setAppUrl(appurl);

                    // set abspath
                    lpi.setAbsolutePath(absPath);

                    // set state flag as NEWCOME
                    lpi.setFlag(StateFlag.NEWCOME);

                    // set timeStamp as current
                    lpi.setTimeStamp(System.currentTimeMillis());

                    // Step 1.1: check if the logpattern exists in ISSUE MAP
                    LogPatternInfo ilpi = this.IssueLogProfileDataMap.get(lpi.getAppUUID(), lpi.getUUID());
                    // project may have same serverID,appID,logpattern,but has different absolutepath -- by hongqiangwei
                    if (null != ilpi) {
                        // if no change to absPath, just leave it in ISSUE
                        // this logpattern should not go to NEW MAP, so do
                        // nothing
                        if (ilpi.getAbsolutePath().equalsIgnoreCase(lpi.getAbsolutePath())) {
                            continue;
                        }
                        // if there is change, then remove it from ISSUE MAP,
                        // this logpattern may go to NEW MAP
                        else {
                            this.IssueLogProfileDataMap.remove(lpi.getAppUUID(), lpi.getUUID());
                        }
                    }

                    // Step 1.2: we should check if there is conflict among the
                    // new coming profile data
                    boolean isDuplicated = newLogPatternPathConflictMap.containsKey(lpi.getAbsolutePath());

                    /**
                     * if duplicated, that means this logpattern has issue and just put it into IssueLogProfileDataMap
                     */
                    if (isDuplicated == true) {
                        // move to ISSUE MAP
                        LogPatternInfo templpi = newLogPatternPathConflictMap.get(lpi.getAbsolutePath());

                        LogPatternInfo originlpi = LatestLogProfileDataMap.get(lpi.getAppUUID(), lpi.getUUID());

                        if (originlpi != null) {

                            // remove new(who is not exist in
                            // LatestLogProfileDataMap) from
                            // newLogPatternInfoMap and
                            // newLogPatternPathConflictMap
                            newLogPatternInfoMap.remove(templpi.getAppUUID(), templpi.getUUID());
                            newLogPatternPathConflictMap.remove(templpi.getAbsolutePath());

                            // add new(who is not exist in
                            // LatestLogProfileDataMap) to
                            // IssueLogProfileDataMap
                            this.IssueLogProfileDataMap.put(templpi);

                            // add current(who is exist in
                            // LatestLogProfileDataMap) to newLogPatternInfoMap
                            // and newLogPatternPathConflictMap
                            newLogPatternInfoMap.put(lpi);
                            newLogPatternPathConflictMap.put(lpi.getAbsolutePath(), lpi);

                        }
                        else {
                            this.IssueLogProfileDataMap.put(lpi);
                        }

                        log.err(this, "APPUUID : " + lpi.getAppUUID()
                                + " whoese log absolute path conflict with APPUUID :" + templpi.getAppUUID());

                        String content = "APPUUID : " + lpi.getAppUUID()
                                + " whoese log absolute path conflict with APPUUID :" + templpi.getAppUUID();

                        NotificationEvent event = new NotificationEvent(NotificationEvent.EVENT_LogPathConflict,
                                "LogPathConflict", content);
                        this.putNotificationEvent(event);

                    }
                    /**
                     * if not duplicated, that means this logpattern seems NEWCOME just at this moment.
                     */
                    else {
                        newLogPatternPathConflictMap.put(lpi.getAbsolutePath(), lpi);
                        // move to NEW MAP
                        newLogPatternInfoMap.put(lpi);
                    }
                }
            }
        }

        /**
         * Step 2: Compare newLogPatternInfoMap with LatestLogProfileDataMap to find NEWCOME or UPDATE
         */
        for (Entry<String, Map<String, LogPatternInfo>> entry : newLogPatternInfoMap.entrySet()) {

            String appUUID = entry.getKey();
            Map<String, LogPatternInfo> logPatternMap = entry.getValue();

            boolean lC = this.LatestLogProfileDataMap.containsKey(appUUID);

            for (LogPatternInfo lpi : logPatternMap.values()) {

                // appUUID EXISTs in LatestLogProfileDataMap
                if (lC == true) {
                    LogPatternInfo ilpi = this.LatestLogProfileDataMap.get(appUUID, lpi.getUUID());
                    // if the logpattern exists in LatestLogProfileDataMap, then
                    // the state should be UPDATE
                    if (null != ilpi) {
                        if (!lpi.getAbsolutePath().equals(ilpi.getAbsolutePath())) {
                            lpi.setFlag(StateFlag.UPDATE);
                        }
                        else {
                            lpi.setFlag(StateFlag.EXIST);
                        }

                    }
                }

                // add this logpattern to LatestLogProfileDataMap
                // NOTE: currently we are ignoring the abspath's conflicts in
                // LatestLogProfileDataMap,
                // we will handle that in next step
                this.LatestLogProfileDataMap.put(lpi);
            }
        }

        /**
         * Step 3: Compare updated LatestLogProfileDataMap with newLogPatternInfoMap again to find EXIST_UNKOWN
         * logpattern
         */
        for (Entry<String, Map<String, LogPatternInfo>> entry : LatestLogProfileDataMap.entrySet()) {

            String appUUID = entry.getKey();
            Map<String, LogPatternInfo> logPatternMap = entry.getValue();

            boolean isInNew = newLogPatternInfoMap.containsKey(appUUID);

            for (LogPatternInfo lpi : logPatternMap.values()) {

                // appUUID in NEWCOME
                if (isInNew) {

                    // check if the logpattern exists in NEWCOME
                    LogPatternInfo ilpi = newLogPatternInfoMap.get(appUUID, lpi.getUUID());

                    // logUUID not in NEWCOME
                    if (ilpi == null) {

                        // mark this logpattern in EXIST_UNKOWN,logCatcher
                        // should fix the state
                        lpi.setFlag(StateFlag.EXIST_UNKOWN);
                    }
                }
                // appUUID not in NEWCOME
                else {

                    // mark this logpattern in EXIST_UNKOWN,logCatcher should
                    // fix the state
                    lpi.setFlag(StateFlag.EXIST_UNKOWN);
                }
            }
        }

        /**
         * Step 4: so sorry, we should rescan the LatestLogProfileDataMap, currently only EXIST_UNKOWN,UPDATE,NEWCOME
         * left to ensure the following conflict rules: 1) UPATE should kick off NEWCOME: 必然存在 2) EXIST_UNKOWN should
         * kick off NEWCOME：尽管可能EXIST_UNKOWN不存在，但保持现状吧，需要logCatcher进行修正, 如果EXIST_UNKOWN存在， 则更新为EXIST，则NEWCOME继续保持在ISSUE
         * MAP 如果EXIST_UNKOWN不存在，则将之移动到ISSUE MAP;其他所有NEWCOME继续保持在ISSUE MAP
         * 
         */
        Map<String, List<LogPatternInfo>> logPatternInfoToIssuesMap = new HashMap<String, List<LogPatternInfo>>();

        for (Entry<String, Map<String, LogPatternInfo>> entry : LatestLogProfileDataMap.entrySet()) {

            Map<String, LogPatternInfo> logPatternMap = entry.getValue();

            for (LogPatternInfo lpi : logPatternMap.values()) {

                String absPath = lpi.getAbsolutePath();

                if (!logPatternInfoToIssuesMap.containsKey(absPath)) {

                    List<LogPatternInfo> lpiList = new ArrayList<LogPatternInfo>();

                    lpiList.add(lpi);

                    logPatternInfoToIssuesMap.put(absPath, lpiList);
                }
                else {

                    List<LogPatternInfo> lpiList = logPatternInfoToIssuesMap.get(absPath);

                    lpiList.add(lpi);

                    /**
                     * sort the list, then every list for one application should have order by flag weight
                     */
                    Collections.sort(lpiList, new Comparator<LogPatternInfo>() {

                        @Override
                        public int compare(LogPatternInfo o1, LogPatternInfo o2) {

                            int weightSpan = o2.getFlag().getFlagWeight() - o1.getFlag().getFlagWeight();

                            return weightSpan;
                        }

                    });

                }
            }

        }

        /**
         * Step 5: build logCatcherInfoMap
         */
        Map<String, LogPatternInfo> logCatcherInfoMap = new LinkedHashMap<String, LogPatternInfo>();

        for (List<LogPatternInfo> lpiList : logPatternInfoToIssuesMap.values()) {

            LogPatternInfo lpi = lpiList.get(0);

            // we just need the first one as the updated one
            logCatcherInfoMap.put(lpi.getUUID(), lpi);

            // the other ones go to ISSUE MAP
            for (int i = 1; i < lpiList.size(); i++) {
                this.IssueLogProfileDataMap.put(lpiList.get(i));
                LatestLogProfileDataMap.remove(lpiList.get(i).getAppUUID(), lpiList.get(i).getUUID());
            }
        }

        /**
         * in some cases there are may no log pattern infos
         */
        if (logCatcherInfoMap.isEmpty()) {
            log.warn(this,
                    "The logCatcherInfoMap is empty and will not update log pattern info in ProfileData for logCatcher.");
            return;
        }

        /**
         * Step 6: run logCatcher
         * 
         */
        /**
         * 
         * start logCatcher
         * 
         * logCatcher is used to process logs
         */
        if (null == logCatcher) {

            String positionfileroot = this.getConfigManager().getContext(IConfigurationManager.METADATAPATH);

            logCatcher = new TaildirLogComponent("TaildirlogComponent", this.feature);

            // support multiple file in future,add dynamic update tailfiles list
            ConfigurationContext context = new ConfigurationContext();

            context.put(POSITION_FILE_ROOT, positionfileroot);
            try {

                logCatcher.configure(context, logCatcherInfoMap);
                logCatcher.start();

                log.info(this, "ApplicationServer LogCatcher starts SUCCESS");
            }
            catch (IOException e) {
                log.err(this, "ApplicationServer LogCatcher starts FAIL.", e);
                return;
            }
        }
        /**
         * 
         * update logCatcher
         * 
         */
        else {
            logCatcher.configure(logCatcherInfoMap);

            log.info(this, "ApplicationServer LogCatcher updates SUCCESS");
        }

    }

    public AppLogPatternInfoCollection getLatestLogProfileDataMap() {

        return LatestLogProfileDataMap;
    }

    public AppLogPatternInfoCollection getIssueLogProfileDataMap() {

        return IssueLogProfileDataMap;
    }

    /**
     * figureOutAbsolutePath
     * 
     * @param logParttern
     */
    protected String figureOutAbsolutePath(String webAppRoot, String logParttern, Properties systemPro) {

        /**
         * position of log should be relative to the current working directory
         */
        String workingDir = systemPro.getProperty("user.dir");
        if (workingDir != null) {
            webAppRoot = workingDir.replace("\\", "/");
        }

        String relPath = logParttern;
        String absPath = "";
        if (relPath.charAt(1) == ':' || relPath.charAt(0) == '/') {
            relPath = relPath.replace("\\", "/");
            absPath = relPath;
        }
        else if (relPath.contains("../")) {
            int index = webAppRoot.lastIndexOf("/");
            String rootpathTemp = webAppRoot.substring(0, index);
            relPath = relPath.substring(2);
            absPath = rootpathTemp + relPath;
        }
        else if (relPath.contains("./")) {
            relPath = relPath.substring(1);
            absPath = webAppRoot + relPath;
        }
        else {

            absPath = webAppRoot + "/" + relPath;
        }

        return absPath;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void updateAllStrategy(String stragetyJson) {

        // full stragety
        List<Map> list = JSONHelper.toObjectArray(stragetyJson, Map.class);
        if (list == null || list.size() == 0) {
            log.warn(this, "stragety json is empty!");
            return;
        }

        for (Map m : list) {
            String serverid = (String) m.get("servid");
            String appid = (String) m.get("appid");
            String logid = (String) m.get("logid");
            String filter = (String) m.get("filter");
            String separator = (String) m.get("separator");
            String fields = (String) m.get("fields");

            LogPatternInfo lcfg = new LogPatternInfo();
            lcfg.setServId(serverid);
            lcfg.setAppId(appid);
            lcfg.setLogParttern(logid);

            LogPatternInfo info = this.LatestLogProfileDataMap.get(lcfg.getAppUUID(), lcfg.getUUID());
            if (info == null) {
                log.warn(this, "log pattern info is null! loguuid:" + lcfg.getUUID());
                return;
            }

            String logPath = info.getAbsolutePath();
            Map mapping = new HashMap();
            mapping.putAll(m);
            mapping.put("absPath", logPath);
            logCfgMapping.put(lcfg.getUUID(), mapping);

            LogFilterAndRule lfar = new DefaultLogFilterAndRule(filter, separator, JSON.parseObject(fields), 0, 0);
            RuleFilterFactory.getInstance().pubLogFilterAndRule(logPath, lfar);
        }

        saveLogCfg();
    }

    private void saveLogCfg() {

        String cfgFile = getLogCfgFile();

        try {
            IOHelper.writeTxtFile(cfgFile, JSONHelper.toString(this.logCfgMapping), "utf-8", false);
        }
        catch (IOException e) {
            log.err(this, "save log config mapping Fail.  file=" + cfgFile, e);
        }
    }

    private void loadLogCfg() {

        String cfgFile = getLogCfgFile();

        String cfg = IOHelper.readTxtFile(cfgFile, "utf-8");
        if (StringHelper.isEmpty(cfg)) {
            return;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        Map<String, Map> mapping = JSONHelper.toObject(cfg, Map.class);
        logCfgMapping.putAll(mapping);

        for (@SuppressWarnings("rawtypes")
        Map m : logCfgMapping.values()) {
            String absPath = (String) m.get("absPath");
            String filter = (String) m.get("filter");
            String separator = (String) m.get("separator");
            String fields = (String) m.get("fields");

            LogFilterAndRule lfar = new DefaultLogFilterAndRule(filter, separator, JSON.parseObject(fields), 0, 0);
            RuleFilterFactory.getInstance().pubLogFilterAndRule(absPath, lfar);
        }
    }

    private String getLogCfgFile() {

        String rootMetaPath = this.getConfigManager().getContext(IConfigurationManager.METADATAPATH);
        return rootMetaPath + File.separator + LOG_CFG_FILE;
    }
}
