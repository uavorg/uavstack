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

package com.creditease.uav.invokechain.data;

import java.util.LinkedHashMap;
import java.util.Map;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.EncodeHelper;
import com.creditease.agent.helpers.StringHelper;

public class Span {

    /**
     * 
     * SpanEndpointType description:
     * 
     * Root 代表span的总入口端
     * 
     * Serivce 代表span的服务组件端
     * 
     * Client 代表span的客户端组件端
     * 
     * NOTE：一个span有两端，两端的spanid，parentid都一样，只是EndpointType不同而已，当然有的span，如调用DB，只有一端，通常是Client类型的endpoint
     *
     */
    public enum SpanEndpointType {
        Root("E"), Service("S"), Client("C"), Method("M");

        public static SpanEndpointType parse(String s) {

            switch (s) {
                case "E":
                default:
                    return SpanEndpointType.Root;
                case "S":
                    return SpanEndpointType.Service;
                case "C":
                    return SpanEndpointType.Client;
                case "M":
                    return SpanEndpointType.Method;

            }
        }

        private String type;

        private SpanEndpointType(String str) {
            type = str;
        }

        @Override
        public String toString() {

            return type;
        }
    }

    private SpanEndpointType endPointType;

    private String traceId = "N";

    private String spanId = "N";

    private String parentId = "N";

    private String url = "";

    private long startTime = 0L;

    private long cost = -1; // the cost mean the time spent on this method

    private String className = "";

    private String methodName = "";

    private String endpointInfo = "";

    private String state = "";

    private String appid = "";

    private String appHostPort = "";

    public Span(String ivcLogSpan) {
        String[] info = ivcLogSpan.split(";");
        this.traceId = info[0];
        this.spanId = info[1];
        this.parentId = info[2];
        this.endPointType = SpanEndpointType.parse(info[3]);
        this.startTime = DataConvertHelper.toLong(info[4], -1);
        this.cost = DataConvertHelper.toLong(info[5], -1);
        this.appHostPort = info[6];
        this.appid = decodeForIVC(info[7]);
        this.endpointInfo = decodeForIVC(info[8]);
        this.className = info[9];
        this.methodName = info[10];
        this.url = decodeForIVC(info[11]);
        this.state = EncodeHelper.urlDecode(info[12]);

    }

    public String getAppid() {

        return appid;
    }

    public void setAppid(String appid) {

        this.appid = appid;
    }

    public String getEndpointType() {

        return endPointType.toString();
    }

    public String getEndpointInfo() {

        return this.endpointInfo;
    }

    public void setEndpointInfo(String endpointInfo) {

        if (StringHelper.isEmpty(endpointInfo)) {
            return;
        }
        this.endpointInfo = endpointInfo;
    }

    public String getUrl() {

        return url;
    }

    public void setUrl(String url) {

        if (StringHelper.isEmpty(url)) {
            return;
        }

        this.url = url;
    }

    public long getStartTime() {

        return startTime;
    }

    public void setStartTime(long startTime) {

        this.startTime = startTime;
    }

    public String getClassName() {

        return className;
    }

    public void setClassName(String className) {

        if (StringHelper.isEmpty(className)) {
            return;
        }

        this.className = className;
    }

    public String getMethodName() {

        return methodName;
    }

    public void setMethodName(String methodName) {

        if (StringHelper.isEmpty(methodName)) {
            return;
        }

        this.methodName = methodName;
    }

    public String getTraceId() {

        return traceId;
    }

    public String getSpanId() {

        return spanId;
    }

    public String getParentId() {

        return parentId;
    }

    public long getCost() {

        return cost;
    }

    public void setCost(long ct) {

        this.cost = ct;
    }

    public String getState() {

        return state;
    }

    public void setState(String state) {

        if (StringHelper.isEmpty(state)) {
            return;
        }

        this.state = state;
    }

    @Override
    public String toString() {

        StringBuffer b = new StringBuffer();

        b.append(this.traceId).append(";");
        b.append(this.spanId).append(";");
        b.append(this.parentId).append(";");
        b.append(this.endPointType.toString()).append(";");
        b.append(this.startTime).append(";");
        b.append(this.cost).append(";");
        b.append(this.appHostPort).append(";");
        b.append(this.appid).append(";");
        b.append(this.endpointInfo).append(";");
        b.append(this.className).append(";");
        b.append(this.methodName).append(";");
        b.append(this.url).append(";");
        b.append(this.state);

        return b.toString();
    }

    public Map<String, Object> toMap() {

        Map<String, Object> m = new LinkedHashMap<String, Object>();

        m.put("traceid", this.traceId);
        m.put("spanid", this.spanId);
        m.put("parentid", this.parentId);
        m.put("eptype", this.endPointType.toString());
        m.put("stime", this.startTime);
        m.put("cost", this.cost);
        m.put("ipport", this.appHostPort);
        m.put("appid", this.appid);
        m.put("epinfo", this.endpointInfo);
        m.put("class", this.className);
        m.put("method", this.methodName);
        m.put("url", this.url);
        m.put("state", this.state);

        return m;
    }

    public String getAppHostPort() {

        return appHostPort;
    }

    public void setAppHostPort(String appHostPort) {

        this.appHostPort = appHostPort;
    }
    private String decodeForIVC(String s) {

        if (!StringHelper.isEmpty(s) && s.contains("%3b")) {
            return s.replace("%3b", ";");
        }
        else {
            return s;
        }
    }

}
