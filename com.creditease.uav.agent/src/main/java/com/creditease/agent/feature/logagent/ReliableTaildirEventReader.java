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

package com.creditease.agent.feature.logagent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.feature.LogAgent;
import com.creditease.agent.feature.logagent.event.Event;
import com.creditease.agent.feature.logagent.objects.LogPatternInfo;
import com.creditease.agent.feature.logagent.objects.LogPatternInfo.StateFlag;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.NetworkHelper;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.agent.monitor.api.NotificationEvent;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

public class ReliableTaildirEventReader {

    private ISystemLogger logger = (ISystemLogger) ConfigurationManager.getInstance().getComponent("logagent",
            "LogDataLog");

    private Cache<String, LogPatternInfo> tailFileTable;
    private final Table<String, String, String> headerTable;

    private ThreadLocal<TailFile> currentFileTL = new ThreadLocal<TailFile>();
    private ThreadLocal<Boolean> committed = new ThreadLocal<Boolean>() {

        @Override
        protected Boolean initialValue() {

            return true;
        }
    };
    private Map<Long, TailFile> tailFiles = Maps.newConcurrentMap();
    private Map<Long, Long[]> maybeReloadMap = Maps.newHashMap();
    private long updateTime;
    private boolean addByteOffset;

    private String os = null;
    private static final String inode = "inode";
    private static Random random = new Random();

    /**
     * Create a ReliableTaildirEventReader to watch the given directory. map<serverid.appid.logid, logpath>
     */
    private ReliableTaildirEventReader(Map<String, LogPatternInfo> filePaths, Table<String, String, String> headerTable,
            String positionFilePath, boolean skipToEnd, boolean addByteOffset, String os) throws IOException {
        // Sanity checks
        Preconditions.checkNotNull(filePaths);
        Preconditions.checkNotNull(positionFilePath);
        // get operation system info
        if (logger.isDebugEnable()) {
            logger.debug(this, "Initializing {" + ReliableTaildirEventReader.class.getSimpleName()
                    + "} with directory={" + filePaths + "}");
        }

        // tailFile
        this.tailFileTable = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.DAYS)
                .<String, LogPatternInfo> build();
        this.headerTable = headerTable;
        this.addByteOffset = addByteOffset;
        this.os = os;
        updatelog(filePaths);
        updateTailFiles(skipToEnd);

        logger.info(this, "tailFileTable: " + tailFileTable.toString());
        logger.info(this, "headerTable: " + headerTable.toString());
        logger.info(this, "Updating position from position file: " + positionFilePath);
        loadPositionFile(positionFilePath);
    }

    public void updatelog(Map<String, LogPatternInfo> filePaths) {

        for (Entry<String, LogPatternInfo> e : filePaths.entrySet()) {

            LogPatternInfo logPatternInfo = e.getValue();

            LogAgent logagent = (LogAgent) ConfigurationManager.getInstance().getComponent("logagent", "LogAgent");

            LogPatternInfo logPatternInfoTemp = logagent.getLatestLogProfileDataMap().get(logPatternInfo.getAppUUID(),
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

                String title = NetworkHelper.getLocalIP() + "在" + logPatternInfo.getParentDir() + "下没有符合日志文件匹配规则["
                        + logPatternInfo.getLogRegxPattern() + "]的日志文件。";
                String content = "失败原因：1）日志文件匹配规则配置错误，所以不能定位日志文件。2）日志文件的命名已经改变，但没有修过日志文件匹配规则。";

                logger.warn(this, title);

                NotificationEvent event = new NotificationEvent(NotificationEvent.EVENT_LogNotExist, title, content);
                event.addArg("serverid", logPatternInfo.getServId());
                event.addArg("appid", logPatternInfo.getAppId());
                logagent.putNotificationEvent(event);

            }
            tailFileTable.put(logPatternInfo.getAbsolutePath(), logPatternInfo);// <R=filepath,C=logPatternInfo,V=

            if (logger.isDebugEnable()) {
                logger.debug(this, "update log table. absPath=" + logPatternInfo.getAbsolutePath() + ", info="
                        + JSONHelper.toString(logPatternInfo));
            }
        }
    }

    public Cache<String, LogPatternInfo> getTailFileTable() {

        return tailFileTable;
    }

    /**
     * Load a position file which has the last read position of each file. If the position file exists, update tailFiles
     * mapping.
     */
    public void loadPositionFile(String filePath) {

        Long inode = 0L, pos = 0L, number = 0L;
        String file = "";
        FileReader fr = null;
        BufferedReader reader = null;
        try {
            fr = new FileReader(filePath);
            reader = new BufferedReader(fr);
            StringBuffer content = new StringBuffer();
            String temp = null;
            while ((temp = reader.readLine()) != null) {
                content.append(temp);
            }
            if (content.length() == 0) {
                return;
            }
            JSONArray positionRecords = JSONArray.parseArray(content.toString());
            for (int i = 0; i < positionRecords.size(); i++) {
                JSONObject positionObject = (JSONObject) positionRecords.get(i);
                inode = positionObject.getLong("inode");
                pos = positionObject.getLong("pos");
                file = positionObject.getString("file");
                Long currentInode = getInode(new File(file));
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
                    if (tf != null && tf.updatePos(file, inode, pos, number)) {
                        tailFiles.put(inode, tf);
                    }
                    else {
                        // add old tail file into memory
                        maybeReloadMap.put(inode, new Long[] { pos, number });
                        if (logger.isDebugEnable()) {
                            logger.debug(this, "add old&inInterrupt file: " + file + ", inode: " + inode + ", pos: " + pos);
                        }

                    }
                }
            }

        }
        catch (FileNotFoundException e1) {
            logger.err(this, "File not found: " + filePath + ", not updating position");
        }
        catch (IOException e) {
            logger.err(this, "Failed loading positionFile: " + filePath, e);
        }
        finally {
            try {
                if (reader != null)
                    reader.close();
                if (fr != null)
                    fr.close();
            }
            catch (IOException e) {
                logger.err(this, "Error: " + e.getMessage(), e);
            }

        }

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

    // @Override
    public Event readEvent() throws IOException {

        List<Event> events = readEvents(1);
        if (events.isEmpty()) {
            return null;
        }
        return events.get(0);
    }

    // @Override
    public List<Event> readEvents(int numEvents) throws IOException {

        return readEvents(numEvents, false);
    }

    @VisibleForTesting
    public List<Event> readEvents(TailFile tf, int numEvents) throws IOException {

        setCurrentFile(tf);
        return readEvents(numEvents, true);
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

        if (!this.getCommitted().get() && isRollBack) {
            if (getCurrentFile() == null) {
                throw new IllegalStateException("current file does not exist. " + getCurrentFile().getPath());
            }
            logger.info(this, "Last read was never committed - resetting position");
            long lastPos = getCurrentFile().getPos();
            getCurrentFile().getRaf().seek(lastPos);
        }
        List<Event> events = getCurrentFile().readEvents(numEvents, backoffWithoutNL, addByteOffset);
        if (events.isEmpty()) {

            this.committed.set(false);
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

    // @Override
    public void close() throws IOException {

        for (TailFile tf : tailFiles.values()) {
            if (tf.getRaf() != null)
                tf.getRaf().close();
        }
    }

    /**
     * Commit the last lines which were read.
     * 
     * @param isRead
     *            是否更新读取时间,当本批次无法读取全部日志信息时，下次读取时更新时间需要小于文件更新时间才能确保文件能再次读取
     */
    // @Override
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

    /**
     * Update tailFiles mapping if a new file is created or appends are detected to the existing file.
     */
    public List<Long> updateTailFiles(boolean skipToEnd) throws IOException {

        LogAgent logagent = (LogAgent) ConfigurationManager.getInstance().getComponent("logagent", "LogAgent");

        updateTime = System.currentTimeMillis();
        List<Long> updatedInodes = Lists.newArrayList();
        String serverid = null;
        String appid = null;
        String logid = null;
        String appurl = null;
        for (Entry<String, LogPatternInfo> cell : tailFileTable.asMap().entrySet()) {
            // cell<serverid--appid--logid, logpath, logname>
            Map<String, String> headers = headerTable.row(cell.getKey());//

            LogPatternInfo logPatternInfo = cell.getValue();

            File parentDir = logPatternInfo.getParentDir();// 文件父路径
            Pattern fileNamePattern = logPatternInfo.getLogRegxPattern();// 编译后的文件名
            serverid = logPatternInfo.getServId();
            appid = logPatternInfo.getAppId();
            logid = logPatternInfo.getLogParttern();
            appurl = logPatternInfo.getAppUrl();
            List<File> files = getMatchFiles(parentDir, fileNamePattern);
            LogPatternInfo logPatternInfo2 = logagent.getLatestLogProfileDataMap().get(logPatternInfo.getAppUUID(),
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
                String content = "失败原因：1）错误删除了这些日志文件。2）修改了日志文件名称，且新名称不符合日志文件匹配规则[" + logPatternInfo.getLogRegxPattern()
                        + "]。";

                logger.warn(this, title);

                NotificationEvent event = new NotificationEvent(NotificationEvent.EVENT_LogNotExist, title, content);
                event.addArg("serverid", logPatternInfo.getServId());
                event.addArg("appid", logPatternInfo.getAppId());
                logagent.putNotificationEvent(event);
            }
            for (File f : files) {
                long inode = getInode(f);
                removeInvalidTFInode(f, inode);
                TailFile tf = tailFiles.get(inode);

                /**
                 * 如果是刚刚开启日志归集那么跳到文件尾部进行归集
                 */
                Set<String> newTailFileSet = logagent.getNewTailFileSet();
                if (newTailFileSet.contains(f.getAbsolutePath())) {
                    newTailFileSet.remove(f.getAbsolutePath());
                    if (tf != null && tf.getRaf() != null) {
                        tf.getRaf().seek(f.length());
                    }
                    else if (tf != null) {
                        tf.setPos(f.length());
                    }
                    else {
                        skipToEnd = true;
                    }
                }

                if (tf == null || !tf.getPath().equals(f.getAbsolutePath())) {
                    long startPos = skipToEnd ? f.length() : 0;// 第一次读取从头开始读
                    // how to get line's number ?
                    long startNum = 0;
                    // try to get pos form position file
                    if (maybeReloadMap.containsKey(inode)) {
                        startPos = maybeReloadMap.get(inode)[0];
                        startNum = maybeReloadMap.get(inode)[1];
                    }
                    tf = openFile(serverid, appid, logid, f, headers, inode, startPos, startNum);
                    tf.setAppUrl(appurl);
                }
                else {
                    boolean updated = tf.getLastUpdated() < f.lastModified() || tf.getPos() < f.length();
                    if (updated) {
                        if (tf.getRaf() == null) {// 获取文件的读取手柄
                            tf = openFile(serverid, appid, logid, f, headers, inode, tf.getPos(), tf.getNum());
                            tf.setAppUrl(appurl);
                        }
                        if (f.length() < tf.getPos()) { // 文件的长度小于上次读取的指针说明文件内容被删除了，改成从0读取
                            logger.info(this, "Pos " + tf.getPos() + " is larger than file size! "
                                    + "Restarting from pos 0, file: " + tf.getPath() + ", inode: " + inode);
                            tf.updatePos(tf.getPath(), inode, 0, tf.getNum());
                        }
                    }
                    tf.setNeedTail(updated);// 设置是否需要监控指标
                }
                tailFiles.put(inode, tf);
                updatedInodes.add(inode);

                if (logger.isDebugEnable()) {
                    logger.debug(this, "tailfile mapping: " + inode + " --> " + tf.getId());
                }
            }
        }
        return updatedInodes;
    }

    /**
     * @param f
     * @param inodeCurrent
     * @throws IOException
     */
    private void removeInvalidTFInode(File f, long inodeCurrent) throws IOException {
        for (Long inodeKey : tailFiles.keySet()) {
            TailFile tf = tailFiles.get(inodeKey);
            if (tf.getPath().equals(f.getAbsolutePath()) && inodeKey != inodeCurrent) {
                tailFiles.remove(inodeKey);
                if (tf.getRaf() != null) {
                    tf.getRaf().close();
                }
            }
        }
    }

    public List<Long> updateTailFiles() throws IOException {

        return updateTailFiles(false);
    }

    public List<File> getMatchFiles(File parentDir, final Pattern fileNamePattern) {

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

    private long getInode(File file) throws IOException {

        UserDefinedFileAttributeView view = null;
        // windows system and file customer Attribute
        if (TaildirSourceConfigurationConstants.OS_WINDOWS.equals(os)) {
            view = Files.getFileAttributeView(file.toPath(), UserDefinedFileAttributeView.class);// 把文件的内容属性值放置在view里面？
            try {
                ByteBuffer buffer = ByteBuffer.allocate(view.size(inode));// view.size得到inode属性值大小
                view.read(inode, buffer);// 把属性值放置在buffer中
                buffer.flip();
                return Long.parseLong(Charset.defaultCharset().decode(buffer).toString());// 返回编码后的inode的属性值

            }
            catch (NoSuchFileException e) {
                long winode = random.nextLong();
                view.write(inode, Charset.defaultCharset().encode(String.valueOf(winode)));
                return winode;
            }
        }

        long inode = (long) Files.getAttribute(file.toPath(), "unix:ino");// 返回unix的inode的属性值
        return inode;
    }

    private TailFile openFile(String serverid, String appid, String logid, File file, Map<String, String> headers,
            long inode, long pos, long num) throws IOException {

        logger.info(this, "serverid: " + serverid + ", appid: " + appid + ", Opening file: " + file + ", inode: "
                + inode + ", pos: " + pos);
        return new TailFile(serverid, appid, logid, file, headers, inode, pos, num);
    }

    /**
     * Special builder class for ReliableTaildirEventReader
     */
    public static class Builder {

        private Map<String, LogPatternInfo> filePaths;
        private Table<String, String, String> headerTable;
        private String positionFilePath;
        private boolean skipToEnd;
        private boolean addByteOffset;
        private String os;

        public Builder filePaths(Map<String, LogPatternInfo> filePaths) {

            this.filePaths = filePaths;
            return this;
        }

        public Builder headerTable(Table<String, String, String> headerTable) {

            this.headerTable = headerTable;
            return this;
        }

        public Builder positionFilePath(String positionFilePath) {

            this.positionFilePath = positionFilePath;
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

        public Builder OperSystem(String os) {

            this.os = os;
            return this;
        }

        public ReliableTaildirEventReader build() throws IOException {

            return new ReliableTaildirEventReader(filePaths, headerTable, positionFilePath, skipToEnd, addByteOffset,
                    os);
        }
    }
}
