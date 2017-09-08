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

package com.creditease.agent.apm.api;

import java.util.ArrayList;
import java.util.List;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.NetworkHelper;

public class CollectDataFrame {

    private long time;
    private String host;
    private String ip;
    private String appgroup;

    private String target;
    private String action;

    private String file;

    private List<Line> lines;

    private boolean eof;

    @SuppressWarnings("unused")
    private CollectDataFrame() {
    }

    public CollectDataFrame(String target, String action, String file) {
        time = System.currentTimeMillis();
        host = NetworkHelper.getHostName();
        ip = NetworkHelper.getLocalIP();
        this.target = target;
        this.action = action;
        this.file = file;
        lines = new ArrayList<>();
    }

    public void append(int line, String content, long timestamp) {

        lines.add(new Line(line, content, timestamp));
    }

    public void setEof(boolean eof) {

        this.eof = eof;
    }

    public String toJSONString() {

        return JSONHelper.toString(this);
    }

    public static CollectDataFrame parse(String json) {

        return JSONHelper.toObject(json, CollectDataFrame.class);
    }

    public static class Line {

        private int lnum;
        private String content;
        private long timestamp;

        @SuppressWarnings("unused")
        private Line() {
        }

        public Line(int lnum, String content, long timestamp) {
            this.lnum = lnum;
            this.content = content;
            this.timestamp = timestamp;
        }

        public int getLnum() {

            return lnum;
        }

        public void setLnum(int lnum) {

            this.lnum = lnum;
        }

        public String getContent() {

            return content;
        }

        public void setContent(String content) {

            this.content = content;
        }

        public long getTimestamp() {

            return timestamp;
        }

        public void setTimestamp(long timestamp) {

            this.timestamp = timestamp;
        }

    }

    public long getTime() {

        return time;
    }

    public void setTime(long time) {

        this.time = time;
    }

    public String getHost() {

        return host;
    }

    public void setHost(String host) {

        this.host = host;
    }

    public String getIp() {

        return ip;
    }

    public void setIp(String ip) {

        this.ip = ip;
    }

    public String getTarget() {

        return target;
    }

    public void setTarget(String target) {

        this.target = target;
    }

    public String getAction() {

        return action;
    }

    public void setAction(String action) {

        this.action = action;
    }

    public String getFile() {

        return file;
    }

    public void setFile(String file) {

        this.file = file;
    }

    public List<Line> getLines() {

        return lines;
    }

    public void setLines(List<Line> lines) {

        this.lines = lines;
    }

    public boolean isEof() {

        return eof;
    }

    public String getAppgroup() {

        return appgroup;
    }

    public void setAppgroup(String appgroup) {

        this.appgroup = appgroup;
    }

}
