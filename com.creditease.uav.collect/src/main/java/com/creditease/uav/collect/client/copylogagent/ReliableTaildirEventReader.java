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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.helpers.JVMToolHelper;
import com.creditease.agent.helpers.NetworkHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.uav.collect.client.collectdata.CollectTask;
import com.creditease.uav.collect.client.collectdata.DataCollector;
import com.creditease.uav.collect.client.copylogagent.LogPatternInfo.StateFlag;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

public class ReliableTaildirEventReader {

    private ISystemLogger log = SystemLogger.getLogger(this.getClass());

    private boolean mutiThreadEnable;

    private long readMaxByte;

    /**
     * Create a ReliableTaildirEventReader to watch the given directory. map<serverid.appid.logid, logpath>
     */
    private ReliableTaildirEventReader(Map<String, CollectTask> tasks, Table<String, String, String> headerTable,
            boolean skipToEnd, boolean addByteOffset) throws IOException {
        Map<String, LogPatternInfo> filePaths = getFilePaths(tasks);

        // Sanity checks
        Preconditions.checkNotNull(filePaths);
        // get operation system info
        if (log.isDebugEnable()) {
            log.debug(this, "Initializing {" + ReliableTaildirEventReader.class.getSimpleName() + "} with directory={"
                    + filePaths + "}");
        }

        // tailFile
        this.tailFileTable = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.DAYS)
                .<String, LogPatternInfo> build();
        this.headerTable = headerTable;
        this.addByteOffset = addByteOffset;
        this.os = JVMToolHelper.isWindows() ? OS_WINDOWS : null;

        updatelog(filePaths);
        updateTailFiles(skipToEnd);

        log.info(this, "tailFileTable: " + tailFileTable.toString());
        log.info(this, "headerTable: " + headerTable.toString());
    }

    public void setLogger(ISystemLogger log) {

        this.log = log;
    }

    public void setMutiThreadEnable(boolean mutiThreadEnable) {

        this.mutiThreadEnable = mutiThreadEnable;
    }

    private ThreadLocal<TailFile> currentFileTL = new ThreadLocal<TailFile>();
    private ThreadLocal<Boolean> committed = new ThreadLocal<Boolean>() {

        @Override
        protected Boolean initialValue() {

            return true;
        }
    };

    private long updateTime;
    private Cache<String, LogPatternInfo> tailFileTable;
    private Table<String, String, String> headerTable;
    private Map<Long, TailFile> tailFiles = Maps.newConcurrentMap();
    private Map<Long, Long[]> maybeReloadMap = Maps.newHashMap();
    private boolean addByteOffset;

    public List<Long> updateTailFiles() throws IOException {

        return updateTailFiles(false);
    }

    /**
     * Update tailFiles mapping if a new file is created or appends are detected to the existing file.
     */
    public List<Long> updateTailFiles(boolean skipToEnd) throws IOException {

        updateTime = System.currentTimeMillis();
        List<Long> updatedInodes = new ArrayList<>();
        String serverid = null;
        String appid = null;
        String logid = null;
        for (Entry<String, LogPatternInfo> cell : tailFileTable.asMap().entrySet()) {
            // cell<serverid--appid--logid, logpath, logname>
            Map<String, String> headers = headerTable.row(cell.getKey());//

            LogPatternInfo logPatternInfo = cell.getValue();

            File parentDir = logPatternInfo.getParentDir();// 文件父路径
            Pattern fileNamePattern = logPatternInfo.getLogRegxPattern();// 编译后的文件名
            serverid = logPatternInfo.getServId();
            appid = logPatternInfo.getAppId();
            logid = logPatternInfo.getLogParttern();
            boolean unsplit = logPatternInfo.isUnsplit();
            List<File> files = getMatchFiles(parentDir, fileNamePattern);

            DataCollector dc = (DataCollector) ConfigurationManager.getInstance().getComponent("collectclient",
                    DataCollector.class.getName());
            LogPatternInfo logPatternInfo2 = dc.getLatestLogProfileDataMap().get(logPatternInfo.getAppUUID(),
                    logPatternInfo.getUUID());

            if (!files.isEmpty()) {
                // modify status UNKNOWN to EXISTS
                if (logPatternInfo2 != null) {
                    logPatternInfo2.setFlag(StateFlag.EXIST);
                }
            }
            else if (logPatternInfo2.getFlag() == StateFlag.EXIST) {
                logPatternInfo2.setFlag(StateFlag.EXIST_UNKOWN);

                String title = NetworkHelper.getLocalIP() + "曾经在" + logPatternInfo.getParentDir() + "符合日志文件匹配规则["
                        + logPatternInfo.getLogRegxPattern() + "]的日志文件消失了。";
                // String content = "失败原因：1）错误删除了这些日志文件。2）修改了日志文件名称，且新名称不符合日志文件匹配规则[" +
                // logPatternInfo.getLogRegxPattern()
                // + "]。";

                log.warn(this, title);

                // AgentFeatureComponent afc = (AgentFeatureComponent) ConfigurationManager.getInstance()
                // .getComponent("logagent");
                //
                // NotificationEvent event = new NotificationEvent(NotificationEvent.EVENT_LogNotExist, title, content);
                // event.addArg("serverid", logPatternInfo.getServId());
                // event.addArg("appid", logPatternInfo.getAppId());
                // afc.putNotificationEvent(event);
            }
            for (File f : files) {
                long inode = getInode(f);
                removeInvalidTFInode(f, inode);
                TailFile tf = tailFiles.get(inode);
                if (tf == null || !tf.getPath().equals(f.getAbsolutePath())) {
                    long startPos = skipToEnd ? f.length() : 0;// 第一次读取从头开始读
                    // how to get line's number ?
                    long startNum = 0;
                    // try to get pos form position file
                    if (maybeReloadMap.containsKey(inode)) {
                        startPos = maybeReloadMap.get(inode)[0];
                        startNum = maybeReloadMap.get(inode)[1];
                    }
                    tf = openFile(serverid, appid, logid, f, headers, inode, startPos, startNum, unsplit);
                }
                else {
                    boolean updated = tf.getLastUpdated() < f.lastModified();
                    if (updated) {
                        if (tf.getRaf() == null) {// 获取文件的读取手柄
                            tf = openFile(serverid, appid, logid, f, headers, inode, tf.getPos(), tf.getNum(), unsplit);
                        }
                        if (f.length() < tf.getPos()) { // 文件的长度小于上次读取的指针说明文件内容被删除了，改成从0读取
                            log.info(this, "Pos " + tf.getPos() + " is larger than file size! "
                                    + "Restarting from pos 0, file: " + tf.getPath() + ", inode: " + inode);
                            tf.updatePos(tf.getPath(), inode, 0, 0);
                        }
                    }
                    tf.setNeedTail(updated);// 设置是否需要监控指标
                }
                tailFiles.put(inode, tf);
                updatedInodes.add(inode);
            }
        }
        return updatedInodes;
    }

    /**
     * @param f
     * @param inodeCurrent
     */
    private void removeInvalidTFInode(File f, long inodeCurrent) {
        for (Long inodeKey : tailFiles.keySet()) {
            TailFile tf = tailFiles.get(inodeKey);
            if (tf.getPath().equals(f.getAbsolutePath()) && inodeKey != inodeCurrent) {
                tailFiles.remove(inodeKey);
            }
        }
    }

    private List<File> getMatchFiles(File parentDir, final Pattern fileNamePattern) {

        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File f) {

                String fileName = f.getName();
                if (f.isDirectory() || !fileNamePattern.matcher(fileName).matches()) {
                    return false;
                }
                return true;
            }
        };
        File[] files = parentDir.listFiles(filter);
        ArrayList<File> result = (files == null) ? Lists.<File> newArrayList() : Lists.newArrayList(files);
        Collections.sort(result, new TailFile.CompareByLastModifiedTime());
        return result;
    }

    private String os = null;
    private static final String OS_WINDOWS = "Windows";
    private static final String INODE = "inode";
    private static Random random = new Random();

    private long getInode(File file) throws IOException {

        UserDefinedFileAttributeView view = null;
        // windows system and file customer Attribute
        if (OS_WINDOWS.equals(os)) {
            view = Files.getFileAttributeView(file.toPath(), UserDefinedFileAttributeView.class);// 把文件的内容属性值放置在view里面？
            try {
                ByteBuffer buffer = ByteBuffer.allocate(view.size(INODE));// view.size得到inode属性值大小
                view.read(INODE, buffer);// 把属性值放置在buffer中
                buffer.flip();
                return Long.parseLong(Charset.defaultCharset().decode(buffer).toString());// 返回编码后的inode的属性值

            }
            catch (NoSuchFileException e) {
                long winode = random.nextLong();
                view.write(INODE, Charset.defaultCharset().encode(String.valueOf(winode)));
                return winode;
            }
        }
        long inode = (long) Files.getAttribute(file.toPath(), "unix:ino");// 返回unix的inode的属性值
        return inode;
    }

    private TailFile openFile(String serverid, String appid, String logid, File file, Map<String, String> headers,
            long inode, long pos, long num, boolean unsplit) throws IOException {

        log.info(this, "serverid: " + serverid + ", appid: " + appid + ", Opening file: " + file + ", inode: " + inode
                + ", pos: " + pos + ", readMaxByte: " + readMaxByte);

        TailFile tf = new TailFile(serverid, appid, logid, file, headers, inode, pos, num, unsplit);
        tf.setReadMaxByte(readMaxByte);
        return tf;
    }

    public Map<Long, TailFile> getTailFiles() {

        return tailFiles;
    }

    public void setCurrentFile(TailFile c) {

        currentFileTL.set(c);
    }

    public TailFile getCurrentFile() {

        return currentFileTL.get();
    }

    public ThreadLocal<Boolean> getCommitted() {

        return committed;
    }

    public List<Event> readEvents(int numEvents, boolean backoffWithOutNL) throws IOException {

        return readEvents(numEvents, backoffWithOutNL, true);
    }

    /**
     * 
     * @param numEvents
     * @param backoffWithoutNL
     * @param isRollBack
     *            在一些情况下，可能出现多次读取信息，中间不做commit的情况
     * @return
     * @throws IOException
     */
    public List<Event> readEvents(int numEvents, boolean backoffWithoutNL, boolean isRollBack) throws IOException {

        if (!getCommitted().get() && isRollBack) {
            if (getCurrentFile() == null) {
                throw new IllegalStateException("current file dos not exist. " + getCurrentFile().getPath());
            }
            log.info(this, "Last read was never committed - resetting position");
            long lastPos = getCurrentFile().getPos();
            getCurrentFile().getRaf().seek(lastPos);
        }
        List<Event> events = getCurrentFile().readEvents(numEvents, backoffWithoutNL, addByteOffset, mutiThreadEnable);
        if (events.isEmpty()) {
            committed.set(false);
            return events;
        }

        Map<String, String> headers = getCurrentFile().getHeaders();
        if (headers != null && !headers.isEmpty()) {
            for (Event event : events) {
                event.getHeaders().putAll(headers);
            }
        }
        this.committed.set(false);
        return events;
    }

    /**
     * Commit the last lines which were read.
     * 
     * @param isRead
     *            是否更新读取时间,当本批次无法读取全部日志信息时，下次读取时更新时间需要小于文件更新时间才能确保文件能再次读取
     */
    public void commit(boolean isRead) throws IOException {

        if (!this.getCommitted().get() && getCurrentFile() != null) {
            if (isRead) {
                getCurrentFile().setLastUpdated(updateTime);
            }
            long pos = getCurrentFile().getRaf().getFilePointer();
            getCurrentFile().setPos(pos);
            this.committed.set(true);
        }
    }

    public Cache<String, LogPatternInfo> getTailFileTable() {

        return tailFileTable;
    }

    public void updatelog(Map<String, LogPatternInfo> filePaths) {

        for (Entry<String, LogPatternInfo> e : filePaths.entrySet()) {

            LogPatternInfo logPatternInfo = e.getValue();
            DataCollector dc = (DataCollector) ConfigurationManager.getInstance().getComponent("collectclient",
                    DataCollector.class.getName());
            LogPatternInfo logPatternInfoTemp = dc.getLatestLogProfileDataMap().get(logPatternInfo.getAppUUID(),
                    logPatternInfo.getUUID());

            List<File> list = getMatchFiles(logPatternInfo.getParentDir(), logPatternInfo.getLogRegxPattern());
            if (!list.isEmpty()) {
                logPatternInfoTemp.setFlag(StateFlag.EXIST);
            }
            else {
                logPatternInfoTemp.setFlag(StateFlag.EXIST_UNKOWN);
            }
            if (list.isEmpty() && !logPatternInfo.getParentDir().isDirectory()) {
                // notify
                /*
                 * String title = NetworkHelper.getLocalIP() + "在" + logPatternInfo.getParentDir() + "下没有符合日志文件匹配规则[" +
                 * logPatternInfo.getLogRegxPattern() + "]的日志文件。"; String content =
                 * "失败原因：1）日志文件匹配规则配置错误，所以不能定位日志文件。2）日志文件的命名已经改变，但没有修过日志文件匹配规则。";
                 * 
                 * logger.warn(this, title);
                 * 
                 * AgentFeatureComponent afc = (AgentFeatureComponent) ConfigurationManager.getInstance()
                 * .getComponent("logagent");
                 * 
                 * NotificationEvent event = new NotificationEvent(NotificationEvent.EVENT_LogNotExist, title, content);
                 * event.addArg("serverid", logPatternInfo.getServId()); event.addArg("appid",
                 * logPatternInfo.getAppId()); afc.putNotificationEvent(event);
                 */
            }
            tailFileTable.put(logPatternInfo.getAbsolutePath(), logPatternInfo);// <R=filepath,C=logPatternInfo,V=
        }
        // logger.info(this, "update log table --" + tailFileTable.toString());
    }

    public void updatelogs(Map<String, CollectTask> tasks) {

        updatelog(getFilePaths(tasks));
    }

    private Map<String, LogPatternInfo> getFilePaths(Map<String, CollectTask> tasks) {

        Map<String, LogPatternInfo> filePaths = new HashMap<>();
        for (Entry<String, CollectTask> en : tasks.entrySet()) {
            CollectTask t = en.getValue();
            LogPatternInfo lpi = new LogPatternInfo(t.getTarget(), t.getAction(), t.getFile());
            lpi.setUnsplit(t.isUnsplit());
            filePaths.put(lpi.getUUID(), lpi);
        }
        return filePaths;
    }

    public void loadPositions(String json) {

        if (StringHelper.isEmpty(json)) {
            return;
        }

        Long inode = 0L, pos = 0L, number = 0L;
        String file = "";
        JSONArray positionRecords = JSONArray.parseArray(json);
        for (int i = 0; i < positionRecords.size(); i++) {
            JSONObject positionObject = (JSONObject) positionRecords.get(i);
            inode = positionObject.getLong("inode");
            pos = positionObject.getLong("pos");
            file = positionObject.getString("file");
            Long currentInode = 0L;
            try {
                currentInode = getInode(new File(file));
            }
            catch (IOException e1) {
                log.err(this, "TailFile updatePos FAILED,getInode Fail.", e1);
            }
            if (!currentInode.equals(inode)) {
                maybeReloadMap.remove(inode);
            }
            else {
                // add line number
                number = positionObject.getLongValue("num");
                for (Object v : Arrays.asList(inode, pos, file)) {
                    Preconditions.checkNotNull(v, "Detected missing value in position file. " + "inode: " + inode
                            + ", pos: " + pos + ", path: " + file);
                }
                TailFile tf = tailFiles.get(inode);
                try {
                    if (tf != null && tf.updatePos(file, inode, pos, number)) {
                        tailFiles.put(inode, tf);
                    }
                    else {
                        // add old tail file into memory
                        maybeReloadMap.put(inode, new Long[] { pos, number });
                        if (log.isDebugEnable()) {
                            log.debug(this, "add old&inInterrupt file: " + file + ", inode: " + inode + ", pos: " + pos);
                        }

                    }
                }
                catch (IOException e) {
                    log.err(this, "TailFile updatePos FAILED.", e);
                }
            }
        }
    }

    public void close() throws IOException {

        for (TailFile tf : tailFiles.values()) {
            if (tf.getRaf() != null)
                tf.getRaf().close();
        }
    }

    /**
     * Special builder class for ReliableTaildirEventReader
     */
    public static class Builder {

        private Map<String, CollectTask> tasks;
        private Table<String, String, String> headerTable;
        private boolean skipToEnd;
        private boolean addByteOffset;

        public Builder tasks(Map<String, CollectTask> tasks) {

            this.tasks = tasks;
            return this;
        }

        public Builder headerTable(Map<String, String> headerMap) {

            Table<String, String, String> table = HashBasedTable.create();
            for (Entry<String, String> en : headerMap.entrySet()) {
                String[] parts = en.getKey().split("\\.", 2);
                table.put(parts[0], parts[1], en.getValue());
            }
            this.headerTable = table;
            return this;
        }

        public Builder skipToEnd(boolean skipToEnd) {

            this.skipToEnd = skipToEnd;
            return this;
        }

        public Builder addByteOffset(boolean addByteOffset) {

            this.addByteOffset = addByteOffset;
            return this;
        }

        public ReliableTaildirEventReader build() throws IOException {

            return new ReliableTaildirEventReader(tasks, headerTable, skipToEnd, addByteOffset);
        }
    }

    public void setReadMaxByte(long readMaxByte) {

        this.readMaxByte = readMaxByte;
    }

}

