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

package com.creditease.uav.feature.runtimenotify;

import java.util.Map;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.monitor.api.MonitorDataFrame;

public class Slice {

    private long time;

    private String key;

    private Map<String, Object> args;

    private MonitorDataFrame mdf;

    public Slice() {
    }

    public Slice(String key, long time) {
        this.key = key;
        this.time = time;
    }

    @SuppressWarnings("unchecked")
    public Slice(String json) {
        Map<String, Object> jo = JSONHelper.toObject(json, Map.class);
        this.time = (long) jo.get("time");
        this.key = (String) jo.get("key");
        this.args = jo.get("args") == null ? null : (Map<String, Object>) JSONHelper.convertJO2POJO(jo.get("args"));
//        this.mdf = jo.get("mdf") == null ? null : new MonitorDataFrame(jo.get("mdf").toString());
    }

    public String toJSONString() {

        StringBuilder sb = new StringBuilder();
        sb.append("{");

        sb.append("\"time\"").append(":").append(time).append(",");
        sb.append("\"key\"").append(":").append("\"" + key + "\"").append(",");
        // args
        sb.append("\"args\"").append(":").append(args == null ? "null" : JSONHelper.toString(args));
//        sb.append(",");
//        // mdf
//        sb.append("\"mdf\"").append(":").append(mdf == null ? "null" : mdf.toJSONString());

        sb.append("}");
        return sb.toString();
    }

    public MonitorDataFrame getMdf() {

        return mdf;
    }

    public void setMdf(MonitorDataFrame mdf) {

        this.mdf = mdf;
    }

    public long getTime() {

        return time;
    }

    public void setTime(long time) {

        this.time = time;
    }

    public String getKey() {

        return key;
    }

    public void setKey(String key) {

        this.key = key;
    }

    public Map<String, Object> getArgs() {

        return args;
    }

    public void setArgs(Map<String, Object> args) {

        this.args = args;
    }

}
