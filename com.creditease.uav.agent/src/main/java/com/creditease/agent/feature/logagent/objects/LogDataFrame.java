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

package com.creditease.agent.feature.logagent.objects;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.NetworkHelper;
import com.creditease.agent.helpers.StringHelper;

public class LogDataFrame {

    public enum MessageType {
        Log("MT_Log");

        private String type;

        MessageType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {

            return type;
        }
    }

    private long timeFlag;
    private String serverId;
    private String host;
    private String ip;
    private String tag;
    private Map<String, String> ext = new HashMap<String, String>();

    @SuppressWarnings("rawtypes")
    private final Map<String, List<Map>> rawDataObjects = new HashMap<String, List<Map>>();

    public LogDataFrame(String serverId, String tag) {
        this(serverId, tag, ((System.currentTimeMillis() / 10) * 10));
    }

    public LogDataFrame(String serverId, String tag, long timeFlag) {
        this.timeFlag = timeFlag;
        this.serverId = serverId;
        host = NetworkHelper.getHostName();
        ip = NetworkHelper.getLocalIP();
        this.tag = tag;
    }

    @SuppressWarnings("rawtypes")
    public void addData(String InfoId, List<Map> data) {

        this.rawDataObjects.put(InfoId, data);
    }

    public boolean isEmpty() {

        return this.rawDataObjects.isEmpty();
    }

    @SuppressWarnings("rawtypes")
    public List<Map> getData(String Logid) {

        if (Logid == null || "".equals(Logid)) {
            return null;
        }
        return rawDataObjects.get(Logid);
    }

    @SuppressWarnings("rawtypes")
    public Map<String, List<Map>> getDatas() {

        return Collections.unmodifiableMap(this.rawDataObjects);
    }

    @SuppressWarnings("rawtypes")
    public String toJSONString() {

        StringBuilder sb = new StringBuilder("{");

        sb.append("time:" + timeFlag + ",");
        sb.append("host:\"" + host + "\",");
        sb.append("ip:\"" + ip + "\\\",");
        sb.append("svrid:\"" + serverId + "\",");
        sb.append("tag:\"" + tag + "\",");

        sb.append("ext:{");
        for (String key : ext.keySet()) {
            sb.append("\"" + key + "\":\"" + ext.get(key) + "\",");
        }

        if (ext.size() > 0) {
            sb = sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("},");

        Set<Entry<String, List<Map>>> sets = rawDataObjects.entrySet();
        sb.append("frames:{");
        for (Entry<String, List<Map>> set : sets) {

            sb.append("\"" + set.getKey() + "\":" + JSONHelper.toString(set.getValue()) + ",");
        }

        if (!sets.isEmpty()) {
            sb = sb.deleteCharAt(sb.length() - 1);
        }

        sb.append("}");
        return sb.append("}").toString();
    }

    public void addExt(String key, String val) {

        if (StringHelper.isEmpty(key) || StringHelper.isEmpty(val)) {
            return;
        }

        this.ext.put(key, val);
    }

    public String getServerId() {

        return serverId;
    }

    public String getHost() {

        return host;
    }

    public String getIP() {

        return ip;
    }

    public String getTag() {

        return tag;
    }

    public void setTag(String tag) {

        this.tag = tag;
    }

    public long getTimeFlag() {

        return timeFlag;
    }
}
