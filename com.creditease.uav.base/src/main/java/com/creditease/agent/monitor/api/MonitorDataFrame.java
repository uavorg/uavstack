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

package com.creditease.agent.monitor.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.NetworkHelper;
import com.creditease.agent.helpers.StringHelper;

public class MonitorDataFrame {

    /**
     * MessageType 2 topic
     * 
     * @author zhen zhang
     *
     */
    public enum MessageType {
        Monitor("MT_Monitor"), Profile("MT_Profile"), Notification("MT_Notify"), Log("MT_Log"), RuntimeNtf(
                "MT_Runtime"), NodeInfo("MT_Node");

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

    /**
     * NOTE: only when there is processing
     */
    @SuppressWarnings("rawtypes")
    private final Map<String, List<Map>> rawDataObjects = new HashMap<String, List<Map>>();

    @SuppressWarnings("rawtypes")
    public MonitorDataFrame(Map m) {
        init(m);
    }

    @SuppressWarnings({ "rawtypes", })
    public MonitorDataFrame(String jsonStr) {

        Map m = JSONHelper.toObject(jsonStr, Map.class);

        init(m);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void init(Map m) {

        this.timeFlag = (Long) m.get("time");
        this.tag = (String) m.get("tag");
        this.serverId = (String) m.get("svrid");
        this.host = (String) m.get("host");
        this.ip = (String) m.get("ip");

        // add ext fields
        if (m.containsKey("ext")) {
            Object to = m.get("ext");

            if (Map.class.isAssignableFrom(to.getClass())) {
                this.ext = (Map) to;
            }
        }

        Map frames = (Map) m.get("frames");

        for (Object key : frames.keySet()) {
            String keyStr = (String) key;
            Object frmData = JSONHelper.convertJO2POJO(frames.get(keyStr));
            rawDataObjects.put(keyStr, (List<Map>) frmData);
        }
    }

    public MonitorDataFrame(String serverId, String tag, long timeFlag) {
        this.timeFlag = timeFlag;
        this.serverId = serverId;
        host = NetworkHelper.getHostName();
        ip = NetworkHelper.getLocalIP();
        this.tag = tag;
    }

    public MonitorDataFrame(String serverId, String tag) {
        this(serverId, tag, ((System.currentTimeMillis() / 10) * 10));
    }

    public long getTimeFlag() {

        return timeFlag;
    }

    @SuppressWarnings("rawtypes")
    public void addData(String InfoId, List<Map> data) {

        this.rawDataObjects.put(InfoId, data);
    }

    @SuppressWarnings("rawtypes")
    public void addData(String monitorId, String rawData) {

        rawData = rawData.replace("\\", "/");
        if (rawData == null || "".equals(rawData) || monitorId == null || "".equals(monitorId)) {
            return;
        }

        List<Map> dataObj = JSONHelper.toObjectArray(rawData, Map.class);

        this.rawDataObjects.put(monitorId, dataObj);
    }

    public boolean isEmpty() {

        return this.rawDataObjects.isEmpty();
    }

    @SuppressWarnings("rawtypes")
    public List<Map> getData(String monitorId) {

        if (monitorId == null || "".equals(monitorId)) {
            return null;
        }

        return rawDataObjects.get(monitorId);
    }

    @SuppressWarnings("rawtypes")
    public Map<String, List<Map>> getDatas() {

        return Collections.unmodifiableMap(this.rawDataObjects);
    }

    public Map<String, String> getExts() {

        return Collections.unmodifiableMap(this.ext);
    }

    public String getExt(String key) {

        if (StringHelper.isEmpty(key)) {
            return null;
        }

        return this.ext.get(key);
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

    @SuppressWarnings("rawtypes")
    public String toJSONString() {

        StringBuilder sb = new StringBuilder("{");

        sb.append("time:" + timeFlag + ",");
        sb.append("host:\"" + host + "\",");
        sb.append("ip:\"" + ip + "\",");
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<Map> getElemInstances(String frameId, String elemId) {

        if (null == frameId || null == elemId) {
            return Collections.emptyList();
        }

        List<Map> elemDatas = rawDataObjects.get(frameId);

        if (null == elemDatas) {
            return Collections.emptyList();
        }

        for (Map elemData : elemDatas) {

            String tmp_elemId = null;

            for (Object keyObj : elemData.keySet()) {

                String key = (String) keyObj;

                if (key.indexOf("EId") >= 0) {

                    tmp_elemId = (String) elemData.get(key);

                    break;
                }
            }

            if (!tmp_elemId.equalsIgnoreCase(elemId)) {
                continue;
            }

            List<Map> instances = (List<Map>) elemData.get("Instances");

            return instances;
        }

        return Collections.emptyList();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map<String, Object> getElemInstValues(String frameId, String elemId, String instanceId) {

        if (null == frameId || null == elemId || null == instanceId) {
            return Collections.emptyMap();
        }

        List<Map> elemDatas = rawDataObjects.get(frameId);

        if (null == elemDatas) {
            return Collections.emptyMap();
        }

        for (Map elemData : elemDatas) {

            String tmp_elemId = null;

            for (Object keyObj : elemData.keySet()) {

                String key = (String) keyObj;

                if (key.indexOf("EId") >= 0) {

                    tmp_elemId = (String) elemData.get(key);

                    break;
                }
            }

            if (!tmp_elemId.equalsIgnoreCase(elemId)) {
                continue;
            }

            List<Map> instances = (List<Map>) elemData.get("Instances");

            if (null == instances) {
                continue;
            }

            for (Map instance : instances) {

                String tmp_instId = null;

                for (Object instKeyObj : instance.keySet()) {

                    String instKey = (String) instKeyObj;

                    if (instKey.indexOf("id") >= 0) {

                        tmp_instId = (String) instance.get(instKey);

                        break;
                    }
                }

                if (!tmp_instId.equalsIgnoreCase(instanceId)) {
                    continue;
                }

                Map<String, Object> values = (Map<String, Object>) instance.get("values");

                return values;
            }
        }

        return Collections.emptyMap();
    }

    public String getTag() {

        return tag;
    }

    public void setTag(String tag) {

        this.tag = tag;
    }

}
