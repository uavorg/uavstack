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

package com.creditease.uav.apm.invokechain.span;

import java.util.concurrent.atomic.AtomicInteger;

import com.creditease.agent.helpers.EncodeHelper;
import com.creditease.agent.helpers.StringHelper;

public class Span implements Cloneable {

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

    private String endpointInfo;

    private String state = "";

    private String appid = "";

    private String appHostPort = "";

    /**
     * sub span seq counter
     */
    private volatile AtomicInteger subSpanSeqCounter = new AtomicInteger(0);

    private volatile AtomicInteger spanMethodSeqCounter = new AtomicInteger(0);

    public Span(SpanEndpointType typeT, String traceIdT, String spanid, String parentid) {

        traceId = traceIdT;
        spanId = spanid;
        if (parentid != null) {
            parentId = parentid;
        }
        endPointType = typeT;

    }

    public AtomicInteger getSubSpanSeqCounter() {

        return this.subSpanSeqCounter;
    }

    public int getSpanMethodSeqCounter() {

        return this.spanMethodSeqCounter.get();
    }

    public int incrementAndGetSpanMethodSeqCounter() {

        return this.spanMethodSeqCounter.incrementAndGet();
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

    public void setState(String rc, String state) {

        if (StringHelper.isEmpty(state)) {
            this.state = rc;
        }
        else {
            this.state = rc + "?" + state;
        }
        this.state = EncodeHelper.urlEncode(this.state);
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

    public String getAppHostPort() {

        return appHostPort;
    }

    public void setAppHostPort(String appHostPort) {

        this.appHostPort = appHostPort;
    }

    @Override
    public Span clone() {

        return new Span(endPointType, traceId, spanId, parentId);
    }

    public void end() {

        long endTime = System.currentTimeMillis();
        this.cost = endTime - this.startTime;
    }
}
