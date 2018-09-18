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

package com.creditease.uav.collect.client.copylogagent;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSONObject;
import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.apm.api.CollectDataFrame;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.agent.workqueue.SystemForkjoinWorker;
import com.creditease.uav.collect.client.collectdata.DataCollector;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

@SuppressWarnings("rawtypes")
public class CopyOfProcessOfLogagent {

    public void process() {

        _process();
    }

    public void close() {

        executor.shutdown();
    }

    @SuppressWarnings("unchecked")
    protected void sendLogDataBatch(Map<TailFile, List<Map>> serverlogs) {

        DataCollector dc = (DataCollector) ConfigurationManager.getInstance().getComponent("collectclient",
                DataCollector.class.getName());
        for (Entry<TailFile, List<Map>> en : serverlogs.entrySet()) {
            TailFile tf = en.getKey();
            List<Map> data = en.getValue();
            CollectDataFrame frame = new CollectDataFrame(tf.getServerId(), tf.getAppId(), tf.getPath());
            // 给日志设置应用组
            frame.setAppgroup(System.getProperty("JAppGroup"));
            for (Map<String, String> m : data) {
                frame.append(Integer.parseInt(m.get("_lnum")), m.get("content"), Long.parseLong(m.get("_timestamp")));
            }
            dc.submit(frame);
        }
    }

    private ISystemLogger log;

    private ReliableTaildirEventReader reader;
    private List<Long> existingInodes = new CopyOnWriteArrayList<Long>();
    private boolean mutiThreadEnable;
    private SystemForkjoinWorker executor;
    private int retryInterval = 1000;
    private int totalEventsLength = 0;
    private int MaxAllowLength = 261000;
    Map<TailFile, List<Map>> serverlogs = new HashMap<>();
    private List<Long> idleInodes = new CopyOnWriteArrayList<Long>();
    private int batchSize; // reader numEvents
    private long timeOutInterval = 2L * 24 * 60 * 60 * 1000; // 2 days

    public static final int DEFAULT_IDLE_TIMEOUT = 120000;
    private int idleTimeout = DEFAULT_IDLE_TIMEOUT;

    private AppLogPatternInfoCollection latestLogProfileDataMap = new AppLogPatternInfoCollection();

    public CopyOfProcessOfLogagent(ISystemLogger log, ReliableTaildirEventReader reader, boolean mutiThreadEnable,
            SystemForkjoinWorker executor, int batchSize, int maxAllowLength) {
        this.log = log;
        this.reader = reader;
        this.mutiThreadEnable = mutiThreadEnable;
        this.executor = executor;
        this.batchSize = batchSize;
        this.MaxAllowLength = maxAllowLength;
    }

    private void _process() {

        if (reader == null) {
            log.warn(this, "ReliableTaildirEventReader is null");
            return;
        }

        try {
            existingInodes.clear();
            List<Long> tfiles = reader.updateTailFiles();

            if (log.isDebugEnable()) {
                log.debug(this, "reader.updateTailFiles() positions: " + JSONHelper.toString(tfiles));
            }

            if (tfiles != null && !tfiles.isEmpty()) {
                existingInodes.addAll(tfiles);
            }

            executeTaskJob(tfiles);

            serverlogs.clear();
            totalEventsLength = 0;
        }
        catch (Throwable t) {
            log.err(this, "Unable to tail files.", t);
        }
    }

    private void executeTaskJob(List<Long> inodes) throws IOException, InterruptedException {

        if (mutiThreadEnable) {
            try {
                TailFilesMutiJobs job = new TailFilesMutiJobs(log);
                job.put("existingInodes", inodes);
                job.put("reader", reader);
                job.put("TailLogcomp", this);

                executor.submit(job);

                job.waitAllTaskDone();

                TimeUnit.MILLISECONDS.sleep(retryInterval);
            }
            catch (InterruptedException e) {
                log.info(this, "Interrupted while sleeping");
            }
        }
        else {
            for (long inode : inodes) {
                TailFile tf = reader.getTailFiles().get(inode);
                tf.setCurrentSumEventsLength(totalEventsLength);

                if (tf.needTail() && (totalEventsLength < MaxAllowLength)) {
                    tailFileProcess(tf, true);
                    totalEventsLength = tf.getCurrentSumEventsLength();
                }
            }
            if (log.isDebugEnable()) {
                log.debug(this, "### Before Sum event Length: ###" + this.totalEventsLength);
            }

            closeTailFiles();

            if (!serverlogs.isEmpty()) {
                sendLogDataBatch();
            }
        }
    }

    private void tailFileProcess(TailFile tf, boolean backoffWithoutNL) throws IOException, InterruptedException {

        tailFileCommon(tf, backoffWithoutNL, serverlogs);
    }

    private void tailFileCommon(TailFile tf, boolean backoffWithoutNL, Map<TailFile, List<Map>> serverlogs)
            throws IOException {

        long current = System.currentTimeMillis();
        boolean isEvents = false;
        reader.setCurrentFile(tf);
        List<Event> events = reader.readEvents(batchSize, backoffWithoutNL);

        if (!events.isEmpty()) {
            isEvents = true;
        }

        try {
            LogFilterAndRule main = RuleFilterFactory.getInstance().getLogFilterAndRule(tf.getPath());
            List<LogFilterAndRule> aids = RuleFilterFactory.getInstance().getAidLogFilterAndRuleList(tf.getPath());
            List<Map> datalog = RuleFilterFactory.getInstance().createChain(reader).setMainLogFilterAndRule(main)
                    .setAidLogFilterAndRuleList(aids).doProcess(events, backoffWithoutNL);
            if (!datalog.isEmpty()) {
                if (serverlogs.containsKey(tf)) {
                    serverlogs.get(tf).addAll(datalog);
                }
                else
                    serverlogs.put(tf, datalog);
            }

            reader.commit(events.size() < batchSize);

        }
        catch (IOException ex) {
            log.warn(this, "The unexpected failure. " + "The source will try again after " + retryInterval + " ms");

            DataCollector dc = (DataCollector) ConfigurationManager.getInstance().getComponent("collectclient",
                    DataCollector.class.getName());
            dc.onTaskFail(tf.getServerId(), tf.getAppId());
        }

        LogPatternInfo info = getLatestLogProfileDataMap().get(tf.getServerId() + "-" + tf.getAppId(), tf.getId());
        if (info != null && isEvents) {
            info.setTimeStamp(current);
            LogPatternInfo innerInfo = reader.getTailFileTable().asMap().get(info.getAbsolutePath());
            if (innerInfo != null) {
                innerInfo.setTimeStamp(current);
                // FIXME concurrent problem
                reader.getTailFileTable().put(innerInfo.getAbsolutePath(), innerInfo);
            }
        }
        if (info != null && current - info.getTimeStamp() > timeOutInterval) {
            getLatestLogProfileDataMap().remove(tf.getServerId() + "-" + tf.getAppId(), tf.getId());
        }
    }

    private void closeTailFiles() throws IOException, InterruptedException {

        for (long inode : idleInodes) {
            TailFile tf = reader.getTailFiles().get(inode);
            if (tf.getRaf() != null) { // when file has not closed yet
                if (mutiThreadEnable) {
                    Map<TailFile, List<Map>> serverlogs = tailFileProcessSeprate(tf, true);
                    if (!(serverlogs.isEmpty())) {
                        this.sendLogDataBatch(serverlogs);
                    }
                    else {
                        if (log.isDebugEnable()) {
                            log.debug(this, "serverlogs is emptry!!!");
                        }
                    }
                }
                else {
                    tailFileProcess(tf, true);
                }

                tf.close();
                log.info(this, "Closed file: " + tf.getPath() + ", inode: " + inode + ", pos: " + tf.getPos());
            }
        }
        idleInodes.clear();
    }

    private void sendLogDataBatch() {

        sendLogDataBatch(serverlogs);
    }

    public AppLogPatternInfoCollection getLatestLogProfileDataMap() {

        return latestLogProfileDataMap;
    }

    public Map<TailFile, List<Map>> tailFileProcessSeprate(TailFile tf, boolean backoffWithoutNL)
            throws IOException, InterruptedException {

        Map<TailFile, List<Map>> serverlogs = new HashMap<>();

        tailFileCommon(tf, backoffWithoutNL, serverlogs);

        return serverlogs;

    }

    /**
     * store all log pattern infos for applications
     */
    public static class AppLogPatternInfoCollection extends LinkedHashMap<String, Map<String, LogPatternInfo>> {

        private static final long serialVersionUID = -7402799091342640234L;

        public void remove(String appUUID, String logUUID) {

            if (appUUID == null || logUUID == null) {
                return;
            }
            Map<String, LogPatternInfo> lpiMap = this.get(appUUID);
            if (lpiMap == null) {
                return;
            }
            lpiMap.remove(logUUID);
            if (lpiMap.size() == 0) {
                this.remove(appUUID);
            }
        }

        public LogPatternInfo get(String appUUID, String logUUID) {

            if (appUUID == null || logUUID == null) {
                return null;
            }
            Map<String, LogPatternInfo> lpiMap = this.get(appUUID);
            if (lpiMap == null) {
                return null;
            }
            return lpiMap.get(logUUID);
        }

        public void put(LogPatternInfo info) {

            if (info == null) {
                return;
            }
            Map<String, LogPatternInfo> lpiMap = this.get(info.getAppUUID());
            if (lpiMap == null) {
                synchronized (this) {
                    lpiMap = this.get(info.getAppUUID());
                    if (lpiMap == null) {
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

    private List<Map> toPosInfoJson() {

        List<Map> posInfos = Lists.newArrayList();
        for (Long inode : existingInodes) {
            TailFile tf = reader.getTailFiles().get(inode);
            posInfos.add(ImmutableMap.of("inode", inode, "pos", tf.getPos(), "num", tf.getNum(), "file", tf.getPath()));
        }

        return posInfos;
    }

    public String getPosInfoJson() {

        return JSONObject.toJSONString(toPosInfoJson());
    }

    // maintain status of file (if idle many times, close file.)
    public void checkIdleFile() {

        try {
            long now = System.currentTimeMillis();
            for (TailFile tf : reader.getTailFiles().values()) {
                if (tf.getLastUpdated() + idleTimeout < now && tf.getRaf() != null) {
                    idleInodes.add(tf.getInode());
                }
            }
            // MutiThread: Move the closeTailFile action in idleFileCheckerRunnable
            if (mutiThreadEnable) {
                closeTailFiles();
            }

        }
        catch (Throwable t) {
            log.err(this, "Uncaught exception in IdleFileChecker thread", t);
        }
    }
}
