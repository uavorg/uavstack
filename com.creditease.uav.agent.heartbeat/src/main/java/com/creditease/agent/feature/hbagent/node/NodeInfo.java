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

package com.creditease.agent.feature.hbagent.node;

import java.util.LinkedHashMap;
import java.util.Map;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;

public class NodeInfo {

    public enum InfoType {

        OS("os"), Connections("connect"), Node("node");

        private String s;

        private InfoType(String s) {
            this.s = s;
        }

        @Override
        public String toString() {

            return this.s;
        }
    }

    // node id
    private String id;

    // node name
    private String name;

    // node host machine IP address
    private String ip;

    // node host machine HOST NAME
    private String host;

    // node group
    private String group;

    // client sync time stamp
    private long ctimestamp;

    // server sync time stamp
    private long stimestamp;

    // node info
    private Map<String, String> info = new LinkedHashMap<String, String>();

    public NodeInfo() {

    }

    public long getClientTimestamp() {

        return ctimestamp;
    }

    public void setClientTimestamp(long timestamp) {

        this.ctimestamp = timestamp;
    }

    public long getServerTimestamp() {

        return stimestamp;
    }

    public void setServerTimestamp(long timestamp) {

        this.stimestamp = timestamp;
    }

    public void putInfo(InfoType type, String subkey, String value) {

        if (null == type || StringHelper.isEmpty(subkey) || StringHelper.isEmpty(value)) {
            return;
        }

        info.put(getInfoKey(type, subkey), value);
    }

    protected String getInfoKey(InfoType type, String subkey) {

        return type.toString() + "." + subkey;
    }

    public String getInfo(InfoType type, String subkey) {

        if (null == type || StringHelper.isEmpty(subkey)) {
            return null;
        }

        return info.get(getInfoKey(type, subkey));
    }

    public Map<String, String> getInfo() {

        return info;
    }

    public void setInfo(Map<String, String> info) {

        this.info = info;
    }

    public String getGroup() {

        return group;
    }

    public void setGroup(String group) {

        this.group = group;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getIp() {

        return ip;
    }

    public void setIp(String ip) {

        this.ip = ip;
    }

    public String getHost() {

        return host;
    }

    public void setHost(String host) {

        this.host = host;
    }

    public String toJSONString() {

        return JSONHelper.toString(this);
    }

    public static NodeInfo toNodeInfo(String jsonString) {

        return JSONHelper.toObject(jsonString, NodeInfo.class);
    }

}
