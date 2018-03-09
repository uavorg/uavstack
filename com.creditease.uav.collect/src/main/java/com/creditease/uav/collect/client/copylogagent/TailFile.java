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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.uav.collect.client.collectdata.DataCollector;
import com.google.common.base.Charsets;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class TailFile {

    private static final String LINE_SEP = "\n";
    private static final String LINE_SEP_WIN = "\r\n";

    public static final String READ_LINE_NUMBER = "line.number";
    public static final String READ_TIMESTAMP = "read.timestamp";
    public static final String BYTE_OFFSET_HEADER_KEY = "byteoffset";

    private boolean eofOccur;
    private boolean unsplit;

    private RandomAccessFile raf;
    private final String path;
    private final long inode;
    private long pos;
    private long lnumber;
    private long lastUpdated;
    private boolean needTail;
    private final Map<String, String> headers;
    private final String serverid;
    private final String appid;
    private final String logid;

    // RMQ最大允许长度是261000,转为JSON格式后会增加大概60K的转义符,所以这里最大允许长度少60K
    // private final int MAX_SIZE = 200000;
    private long readMaxByte;
    private int currentSumEventsLength = 0;

    /**
     * serverid--appid--logid
     */
    private final String id;

    public TailFile(String serverid, String appid, String logid, File file, Map<String, String> headers, long inode,
            long pos, long lnumber, boolean unsplit) throws IOException {
        this.raf = new RandomAccessFile(file, "r");
        if (pos > 0)
            raf.seek(pos);
        this.path = file.getAbsolutePath();
        this.inode = inode;
        this.pos = pos;
        this.lastUpdated = 0L;
        this.needTail = true;
        this.headers = headers;
        this.serverid = serverid;
        this.appid = appid;
        this.logid = logid;
        this.id = serverid + "-" + appid + "-" + logid;
        this.lnumber = lnumber;
        this.unsplit = unsplit;

        if (this.unsplit) {
            this.lastUpdated = System.currentTimeMillis();
        }
    }

    @Override
    public int hashCode() {

        return id.hashCode();
    }

    public RandomAccessFile getRaf() {

        return raf;
    }

    public String getPath() {

        return path;
    }

    public long getInode() {

        return inode;
    }

    public long getPos() {

        return pos;
    }

    public long getLastUpdated() {

        return lastUpdated;
    }

    public boolean needTail() {

        return needTail;
    }

    public Map<String, String> getHeaders() {

        return headers;
    }

    public String getServerId() {

        return serverid;
    }

    public String getAppId() {

        return appid;
    }

    public String getLogId() {

        return logid;
    }

    public String getId() {

        return id;
    }

    public void setPos(long pos) {

        this.pos = pos;
    }

    public long getNum() {

        return lnumber;
    }

    public void setNum(long num) {

        this.lnumber = num;
    }

    public void setLastUpdated(long lastUpdated) {

        this.lastUpdated = lastUpdated;
    }

    public void setNeedTail(boolean needTail) {

        this.needTail = needTail;
    }

    public void setCurrentSumEventsLength(int length) {

        this.currentSumEventsLength = length;
    }

    public int getCurrentSumEventsLength() {

        return this.currentSumEventsLength;
    }

    public boolean updatePos(String path, long inode, long pos, long num) throws IOException {

        if (this.inode == inode && this.path.equals(path)) {
            raf.seek(pos);
            setPos(pos);
            setNum(num);
            // logger.info(this, "Updated position, file: " + path + ", inode: " + inode + ", pos: " + pos
            // + ", line number: " + num);
            return true;
        }
        return false;
    }

    public static class CompareByLastModifiedTime implements Comparator<File> {

        @Override
        public int compare(File f1, File f2) {

            return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
        }
    }

    public List<Event> readEvents(int numEvents, boolean backoffWithoutNL, boolean addByteOffset, boolean mutiFlag)
            throws IOException {

        List<Event> events = new LinkedList<>();
        // Fix:computer the eventSize, if the size overflow Max size, break the numEvents loop
        int sumEventsLength = mutiFlag ? 0 : getCurrentSumEventsLength();
        // int readLineNumber = 0;

        if (unsplit) {
            numEvents = Integer.MAX_VALUE;
        }

        for (int i = 0; i < numEvents; i++) {
            Event event = readEvent(backoffWithoutNL, addByteOffset);
            if (event == null) {
                break;
            }

            if (event == Eof.instance()) {
                if (!eofOccur) {
                    eofOccur = true;
                    DataCollector dc = (DataCollector) ConfigurationManager.getInstance().getComponent("collectclient",
                            DataCollector.class.getName());
                    dc.onTaskEof(serverid, appid);
                }
                break;
            }

            // readLineNumber++;
            sumEventsLength += event.getBodyLength();

            if (sumEventsLength > readMaxByte) {
                // logger.err(this, "## overflow Max size ### sumEventsLength ###: " + sumEventsLength);
                break;
            }
            events.add(event);
        }
        currentSumEventsLength = sumEventsLength;
        return events;
    }

    private Event readEvent(boolean backoffWithoutNL, boolean addByteOffset) throws IOException {

        Long posTmp = raf.getFilePointer();
        String line = readLine();
        if (line == null) {
            return Eof.instance();
        }
        if (backoffWithoutNL && !line.endsWith(LINE_SEP)) {
            // logger.info(this, "Backing off in file without newline: " + path + ", inode: " + inode + ", pos: " +
            // raf.getFilePointer());
            raf.seek(posTmp);
            return null;
        }

        String lineSep = LINE_SEP;
        if (line.endsWith(LINE_SEP_WIN)) {
            lineSep = LINE_SEP_WIN;
        }
        // add line number and timeStamp
        Map<String, String> map = new HashMap<String, String>();
        map.put(READ_LINE_NUMBER, String.valueOf(++lnumber));
        map.put(READ_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        if (addByteOffset == true) {
            map.put(BYTE_OFFSET_HEADER_KEY, posTmp.toString());
        }
        return EventBuilder.withBody(StringHelper.removeEnd(line, lineSep), Charsets.UTF_8, map);
    }

    private String readLine() throws IOException {

        ByteArrayDataOutput out = ByteStreams.newDataOutput(300);
        int i = 0;
        int c;
        while ((c = raf.read()) != -1) {
            i++;
            out.write((byte) c);
            if (c == LINE_SEP.charAt(0)) {
                break;
            }
        }
        if (i == 0) {
            return null;
        }
        return new String(out.toByteArray(), Charsets.UTF_8);
    }

    public void close() {

        try {
            raf.close();
            raf = null;
            // long now = System.currentTimeMillis();
            // because catch log may be in many of batch, so sometimes out of idle time.
            // setLastUpdated(now);
        }
        catch (IOException e) {
            // logger.err(this, "Failed closing file: " + path + ", inode: " + inode, e);
        }
    }

    public void setReadMaxByte(long readMaxByte) {

        this.readMaxByte = readMaxByte;
    }

}

