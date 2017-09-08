/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.creditease.agent.feature.logagent;

import static com.creditease.agent.feature.logagent.TaildirSourceConfigurationConstants.BATCH_SIZE;
import static com.creditease.agent.feature.logagent.TaildirSourceConfigurationConstants.BYTE_OFFSET_HEADER;
import static com.creditease.agent.feature.logagent.TaildirSourceConfigurationConstants.DEFAULT_BATCH_SIZE;
import static com.creditease.agent.feature.logagent.TaildirSourceConfigurationConstants.DEFAULT_BYTE_OFFSET_HEADER;
import static com.creditease.agent.feature.logagent.TaildirSourceConfigurationConstants.DEFAULT_IDLE_TIMEOUT;
import static com.creditease.agent.feature.logagent.TaildirSourceConfigurationConstants.DEFAULT_POSITION_FILE;
import static com.creditease.agent.feature.logagent.TaildirSourceConfigurationConstants.DEFAULT_SKIP_TO_END;
import static com.creditease.agent.feature.logagent.TaildirSourceConfigurationConstants.DEFAULT_WRITE_POS_INTERVAL;
import static com.creditease.agent.feature.logagent.TaildirSourceConfigurationConstants.HEADERS_PREFIX;
import static com.creditease.agent.feature.logagent.TaildirSourceConfigurationConstants.IDLE_TIMEOUT;
import static com.creditease.agent.feature.logagent.TaildirSourceConfigurationConstants.OS_WINDOWS;
import static com.creditease.agent.feature.logagent.TaildirSourceConfigurationConstants.POSITION_FILE_ROOT;
import static com.creditease.agent.feature.logagent.TaildirSourceConfigurationConstants.SKIP_TO_END;
import static com.creditease.agent.feature.logagent.TaildirSourceConfigurationConstants.WRITE_POS_INTERVAL;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.creditease.agent.ConfigurationContext;
import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.feature.LogAgent;
import com.creditease.agent.feature.logagent.api.LogFilterAndRule;
import com.creditease.agent.feature.logagent.event.Event;
import com.creditease.agent.feature.logagent.objects.LogDataElement;
import com.creditease.agent.feature.logagent.objects.LogDataFrame;
import com.creditease.agent.feature.logagent.objects.LogPatternInfo;
import com.creditease.agent.helpers.NetworkHelper;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.AbstractComponent;
import com.creditease.agent.workqueue.SystemForkjoinWorker;
import com.google.common.base.Optional;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class TaildirLogComponent extends AbstractComponent {

    private Map<String, LogPatternInfo> filePaths;
    private Table<String, String, String> headerTable;
    private int batchSize;
    private String positionFilePath;
    private boolean skipToEnd;
    private boolean byteOffsetHeader;

    private ReliableTaildirEventReader reader;
    // private LogFilterAndRule logFilterRule;
    private ScheduledExecutorService idleFileChecker;
    private ScheduledExecutorService positionWriter;
    private int retryInterval = 1000;
    // private int maxRetryInterval = 5000;
    private int idleTimeout;
    private int checkIdleInterval = 5000;
    private int writePosInitDelay = 5000;
    private long timeOutInterval = 2 * 24 * 60 * 60 * 1000; // 2 days
    private int writePosInterval;

    private List<Long> existingInodes = new CopyOnWriteArrayList<Long>();
    private List<Long> idleInodes = new CopyOnWriteArrayList<Long>();

    @SuppressWarnings("rawtypes")
    Map<TailFile, List<Map>> serverlogs = Maps.newHashMap();

    private int totalEventsLength = 0;
    private int MaxAllowLength = 261000;

    private Long backoffSleepIncrement;
    private Long maxBackOffSleepInterval;

    private SystemForkjoinWorker executor;

    public void setExecutor(SystemForkjoinWorker executor) {

        this.executor = executor;
    }

    public static enum Status {
        READY, BACKOFF
    }

    @Override
    protected ISystemLogger getLogger(String cName, String feature) {

        return (ISystemLogger) this.getConfigManager().getComponent(this.feature, "LogDataLog");
    }

    public TaildirLogComponent(String cName, String feature) {

        super(cName, feature);

        if (TailLogContext.getInstance().getBoolean("MutiThread.enable")) {
            setExecutor((SystemForkjoinWorker) ConfigurationManager.getInstance().getComponent(this.feature,
                    "TailLogProcessPoolWorker"));
        }
    }

    public synchronized void start() {

        log.info(this, cName + " LogAgent starting with directory: " + filePaths);
        String OS = null;
        if (System.getProperty("os.name").contains(OS_WINDOWS))
            OS = OS_WINDOWS;

        try {

            // build log read engine
            reader = new ReliableTaildirEventReader.Builder().filePaths(filePaths).headerTable(headerTable)
                    .positionFilePath(positionFilePath).skipToEnd(skipToEnd).addByteOffset(byteOffsetHeader)
                    .OperSystem(OS).build();
            // registion
        }
        catch (IOException e) {
            log.err(this, "Error instantiating ReliableTaildirEventReader", e);
            return;
        }

        // build maintain status of file (if idle many times, close file.)
        idleFileChecker = Executors
                .newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("idleFileChecker").build());
        idleFileChecker.scheduleWithFixedDelay(new idleFileCheckerRunnable(), idleTimeout, checkIdleInterval,
                TimeUnit.MILLISECONDS);

        // build maintain file of position
        positionWriter = Executors
                .newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("positionWriter").build());
        positionWriter.scheduleWithFixedDelay(new PositionWriterRunnable(), writePosInitDelay, writePosInterval,
                TimeUnit.MILLISECONDS);

        log.debug(this, "LogAgent started");
    }

    public synchronized void stop() {

        try {
            // super.stop();
            ExecutorService[] services = { idleFileChecker, positionWriter };
            for (ExecutorService service : services) {
                service.shutdown();
                if (!service.awaitTermination(1, TimeUnit.SECONDS)) {
                    service.shutdownNow();
                }
            }
            // write the last position
            writePosition();
            reader.close();
        }
        catch (InterruptedException e) {
            log.info(this, "Interrupted while awaiting termination", e);
        }
        catch (IOException e) {
            log.info(this, "Failed: " + e.getMessage(), e);
        }
        // sourceCounter.stop();
        log.info("Taildir source {} stopped.", cName);
    }

    @Override
    public String toString() {

        return String.format(
                "Taildir source: { positionFile: %s, skipToEnd: %s, "
                        + "byteOffsetHeader: %s, idleTimeout: %s, writePosInterval: %s }",
                positionFilePath, skipToEnd, byteOffsetHeader, idleTimeout, writePosInterval);
    }

    // @Override
    public synchronized void configure(ConfigurationContext context, Map<String, LogPatternInfo> filePaths)
            throws IOException {

        this.filePaths = filePaths;

        String homePath = System.getProperty("user.home").replace('\\', '/');
        positionFilePath = context.getString(POSITION_FILE_ROOT, homePath) + DEFAULT_POSITION_FILE;
        Path positionFile = Paths.get(positionFilePath);
        try {
            Files.createDirectories(positionFile.getParent());
            if (!Files.exists(positionFile, LinkOption.NOFOLLOW_LINKS)) {
                new File(positionFilePath).createNewFile();
            }
            ;

        }
        catch (IOException e) {
            throw new IOException("Error creating positionFile parent directories", e);
        }
        headerTable = getTable(context, HEADERS_PREFIX);
        batchSize = context.getInteger(BATCH_SIZE, DEFAULT_BATCH_SIZE);
        skipToEnd = context.getBoolean(SKIP_TO_END, DEFAULT_SKIP_TO_END);
        byteOffsetHeader = context.getBoolean(BYTE_OFFSET_HEADER, DEFAULT_BYTE_OFFSET_HEADER);
        idleTimeout = context.getInteger(IDLE_TIMEOUT, DEFAULT_IDLE_TIMEOUT);
        writePosInterval = context.getInteger(WRITE_POS_INTERVAL, DEFAULT_WRITE_POS_INTERVAL);

        backoffSleepIncrement = context.getLong(TaildirSourceConfigurationConstants.BACKOFF_SLEEP_INCREMENT,
                TaildirSourceConfigurationConstants.DEFAULT_BACKOFF_SLEEP_INCREMENT);
        maxBackOffSleepInterval = context.getLong(TaildirSourceConfigurationConstants.MAX_BACKOFF_SLEEP,
                TaildirSourceConfigurationConstants.DEFAULT_MAX_BACKOFF_SLEEP);
    }

    private Table<String, String, String> getTable(ConfigurationContext context, String prefix) {

        Table<String, String, String> table = HashBasedTable.create();
        for (Entry<String, String> e : context.getSubProperties(prefix).entrySet()) {
            String[] parts = e.getKey().split("\\.", 2);
            table.put(parts[0], parts[1], e.getValue());
        }
        return table;
    }

    public synchronized void configure(Map<String, LogPatternInfo> filePaths) {

        // 更新日志配置
        reader.updatelog(filePaths);
    }

    public Status process() {

        if (log.isDebugEnable()) {
            log.debug(this, " ###LogTailComponet starting process.###");
        }

        Status status = Status.READY;
        try {
            existingInodes.clear();

            if (null != reader) {
                List<Long> tfiles = reader.updateTailFiles();

                if (null != existingInodes && null != tfiles) {
                    existingInodes.addAll(tfiles);
                }
                else {
                    if (log.isDebugEnable()) {
                        log.debug(this, "existingInodes may be emptry: " + existingInodes);
                        log.debug(this, "tfiles may be emptry: " + tfiles);
                    }
                    return Status.READY;
                }
            }
            else {
                log.info(this, "Log reader is emptry!!!");
                return Status.READY;
            }

            if (TailLogContext.getInstance().getBoolean("MutiThread.enable")) {
                try {
                    TailFilesMutiJobs job = new TailFilesMutiJobs(log);
                    job.put("existingInodes", existingInodes);
                    job.put("reader", reader);
                    job.put("TailLogcomp", this);

                    executor.submitTask(job);

                    job.waitAllTaskDone();

                    TimeUnit.MILLISECONDS.sleep(retryInterval);
                }
                catch (InterruptedException e) {
                    log.info(this, "Interrupted while sleeping");
                    status = Status.BACKOFF;
                }
            }
            else {
                for (long inode : existingInodes) {
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

            serverlogs.clear();
            totalEventsLength = 0;
        }
        catch (Throwable t) {
            log.err(this, "Unable to tail files.", t);
            status = Status.BACKOFF;
        }
        if (log.isDebugEnable()) {
            log.debug(this, "### LogTailComponet stop process.###");
        }

        return status;
    }

    // @Override
    public long getBackOffSleepIncrement() {

        return backoffSleepIncrement;
    }

    // @Override
    public long getMaxBackOffSleepInterval() {

        return maxBackOffSleepInterval;
    }

    /**
     * 取得当前所有有效的日志文件列表
     * 
     * @return
     */
    public Map<String, LogPatternInfo> getTailFileTable() {

        return reader.getTailFileTable().asMap();
    }

    public void tailFileProcess(TailFile tf, boolean backoffWithoutNL) throws IOException, InterruptedException {

        tailFileCommon(tf, backoffWithoutNL, serverlogs);
    }

    @SuppressWarnings("rawtypes")
    public Map<TailFile, List<Map>> tailFileProcessSeprate(TailFile tf, boolean backoffWithoutNL)
            throws IOException, InterruptedException {

        Map<TailFile, List<Map>> serverlogs = Maps.newHashMap();

        tailFileCommon(tf, backoffWithoutNL, serverlogs);

        return serverlogs;

    }

    @SuppressWarnings("rawtypes")
    public void tailFileCommon(TailFile tf, boolean backoffWithoutNL, Map<TailFile, List<Map>> serverlogs)
            throws IOException, InterruptedException {

        long current = System.currentTimeMillis();
        boolean isEvents = false;
        // while (true) {
        reader.setCurrentFile(tf);
        List<Event> events = reader.readEvents(batchSize, backoffWithoutNL);

        if (!events.isEmpty()) {
            isEvents = true;
        }

        try {
            LogFilterAndRule main = RuleFilterFactory.getInstance().getLogFilterAndRule(tf.getPath());
            List<LogFilterAndRule> aids = RuleFilterFactory.getInstance().getAidLogFilterAndRuleList(tf.getPath());
            List<Map> datalog = RuleFilterFactory.getInstance().createChain(reader, batchSize)
                    .setMainLogFilterAndRule(main).setAidLogFilterAndRuleList(aids).doProcess(events, backoffWithoutNL);
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
            // TimeUnit.MILLISECONDS.sleep(retryInterval);
            // retryInterval = retryInterval << 1;
            // retryInter val = Math.min(retryInterval, maxRetryInterval);
            // continue;
        }
        // retryInterval = 1000;
        // if (events.size() < batchSize) {
        // break;
        // }
        // }
        // renew

        LogAgent logagent = (LogAgent) ConfigurationManager.getInstance().getComponent("logagent", "LogAgent");

        LogPatternInfo info = logagent.getLatestLogProfileDataMap().get(tf.getServerId() + "-" + tf.getAppId(),
                tf.getId());
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
            logagent.getLatestLogProfileDataMap().remove(tf.getServerId() + "-" + tf.getAppId(), tf.getId());

            // notify
            String title = NetworkHelper.getLocalIP() + "日志[" + tf.getId() + "]的过滤规则配置已经过期.";

            log.err(this, title);
            NotificationEvent event = new NotificationEvent(NotificationEvent.EVENT_LogRuleExpired, title, title);
            event.addArg("serverid", tf.getServerId());
            event.addArg("appid", tf.getAppId());
            this.putNotificationEvent(event);
        }
    }

    /**
     * send log information by queue
     */
    @SuppressWarnings("rawtypes")
    public void sendLogDataBatch(Map<TailFile, List<Map>> serverlogs) {

        if (TailLogContext.getInstance().getBoolean("LogDataFrame.enable")) {
            sendToPublisherByStream(serverlogs);
        }
        else {
            sendToPublisher(serverlogs);
        }
    }

    public void sendLogDataBatch() {

        if (TailLogContext.getInstance().getBoolean("LogDataFrame.enable")) {
            sendToPublisherByStream(serverlogs);
        }
        else {
            sendToPublisher(serverlogs);
        }
    }

    @SuppressWarnings("rawtypes")
    public void sendToPublisher(Map<TailFile, List<Map>> serverlogs) {

        Map<String, MonitorDataFrame> mdfs = Maps.newHashMap();
        AppServerLogPublishWorker aplc = (AppServerLogPublishWorker) this.getConfigManager().getComponent(this.feature,
                "AppServerLogPublishWorker");
        MonitorDataFrame mdf = null;
        TailFile file = null;
        for (Entry<TailFile, List<Map>> applogs : serverlogs.entrySet()) {
            file = applogs.getKey();

            if (mdfs.containsKey(file.getServerId())) {
                mdf = mdfs.get(file.getServerId());
            }
            else {
                mdf = new MonitorDataFrame(file.getServerId(), "L");
                // 给日志添加应用组信息
                mdf.addExt("appgroup", System.getProperty("JAppGroup"));
                mdf.addExt("appurl", file.getAppUrl());
                mdfs.put(file.getServerId(), mdf);
            }
            // mdf.addData(file.getAppId() + "," + file.getLogId(), applogs.getValue());
            mdf.addData(file.getAppId(), wearClothes(file.getLogId(), applogs.getValue()));

            if (log.isDebugEnable()) {
                log.debug(this, "## after filename:" + file.getInode());
                log.debug(this, "## after mdf:  " + mdf.toJSONString());
                log.debug(this, "## after mdf length: " + mdf.toJSONString().getBytes().length);
            }
        }
        for (MonitorDataFrame smdf : mdfs.values()) {
            aplc.putData(smdf);
        }
    }

    @SuppressWarnings("rawtypes")
    public void sendToPublisherByStream(Map<TailFile, List<Map>> serverlogs) {

        AppServerLogPublishWorkerByStream aplc = (AppServerLogPublishWorkerByStream) this.getConfigManager()
                .getComponent(this.feature, "AppServerLogPublishWorkerByStream");
        LogDataFrame ldf = null;
        LogDataElement ldet = null;

        StringBuilder sb = new StringBuilder("[");

        TailFile tfile = null;
        for (Entry<TailFile, List<Map>> applogs : serverlogs.entrySet()) {
            tfile = applogs.getKey();
            ldf = new LogDataFrame(tfile.getServerId(), "L");
            // 给日志添加应用组信息
            ldf.addExt("appgroup", System.getProperty("JAppGroup"));
            ldf.addExt("appurl", tfile.getAppUrl());
            ldet = new LogDataElement(tfile.getLogId());

            ldet.addLogElementList(applogs.getValue());
            ldf.addData(tfile.getAppId(), ldet.getReturnInstLists());

            sb.append(ldf.toJSONString() + ",");
        }
        if (serverlogs.size() > 0) {
            sb = sb.deleteCharAt(sb.length() - 1);
        }
        String stream = sb.append("]").toString();
        aplc.putData(stream);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private List<Map> wearClothes(String logid, List<Map> ilist) {

        List<Map> result = new ArrayList();
        result.add(ImmutableMap.of("MEId", "log", "Instances",
                Lists.newArrayList(((ImmutableMap.of("id", logid, "values", ImmutableMap.of("content", ilist)))))));
        return result;
    }

    private void closeTailFiles() throws IOException, InterruptedException {

        for (long inode : idleInodes) {
            TailFile tf = reader.getTailFiles().get(inode);
            if (tf.getRaf() != null) { // when file has not closed yet
                if (TailLogContext.getInstance().getBoolean("MutiThread.enable")) {
                    @SuppressWarnings("rawtypes")
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

    /**
     * Runnable class that checks whether there are files which should be closed.
     */
    private class idleFileCheckerRunnable implements Runnable {

        @Override
        public void run() {

            try {
                long now = System.currentTimeMillis();
                for (TailFile tf : reader.getTailFiles().values()) {
                    if (tf.getLastUpdated() + idleTimeout < now && tf.getRaf() != null) {
                        idleInodes.add(tf.getInode());
                    }
                }
                // MutiThread: Move the closeTailFile action in idleFileCheckerRunnable
                if (TailLogContext.getInstance().getBoolean("MutiThread.enable")) {
                    closeTailFiles();
                }

            }
            catch (Throwable t) {
                log.err(this, "Uncaught exception in IdleFileChecker thread", t);
            }
        }
    }

    /**
     * Runnable class that writes a position file which has the last read position of each file.
     */
    private class PositionWriterRunnable implements Runnable {

        @Override
        public void run() {

            writePosition(true);
        }
    }

    private void writePosition(boolean isAppend) {

        File file = new File(positionFilePath);
        LineNumberReader reader = null;
        FileWriter writer = null;
        @SuppressWarnings("rawtypes")
        List<Map> json = Lists.newArrayList();
        if (!existingInodes.isEmpty()) {
            json.addAll(toPosInfoJson());
        }
        try {
            reader = new LineNumberReader(new FileReader(file));
            JSONArray array = Optional.fromNullable(JSONObject.parseArray(reader.readLine())).or(new JSONArray());

            for (int i = 0; i < array.size(); i++) {
                JSONObject posfile = array.getJSONObject(i);
                if (!existingInodes.contains(posfile.getLongValue("inode")))
                    json.add(ImmutableMap.of("inode", posfile.getLongValue("inode"), "pos", posfile.getLongValue("pos"),
                            "num", posfile.getLong("num"), "file", posfile.getString("file")));
            }
            writer = new FileWriter(file);
            writer.write(JSONObject.toJSONString(json));
        }
        catch (Throwable t) {
            log.err(this, "Failed writing positionFile", t);
        }
        finally {
            try {
                if (reader != null)
                    reader.close();
            }
            catch (IOException e) {
                log.err(this, "Error: " + e.getMessage(), e);
            }
            try {
                if (writer != null)
                    writer.close();
            }
            catch (IOException e) {
                log.err(this, "Error: " + e.getMessage(), e);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void writePosition() {

        File file = new File(positionFilePath);
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            if (!existingInodes.isEmpty()) {
                List<Map> json = toPosInfoJson();
                writer.write(JSONObject.toJSONString(json));
            }
        }
        catch (Throwable t) {
            log.err(this, "Failed writing positionFile", t);
        }
        finally {
            try {
                if (writer != null)
                    writer.close();
            }
            catch (IOException e) {
                log.err(this, "Error: " + e.getMessage(), e);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private List<Map> toPosInfoJson() {

        List<Map> posInfos = Lists.newArrayList();
        for (Long inode : existingInodes) {
            TailFile tf = reader.getTailFiles().get(inode);
            posInfos.add(ImmutableMap.of("inode", inode, "pos", tf.getPos(), "num", tf.getNum(), "file", tf.getPath()));
        }

        return posInfos;
    }
}