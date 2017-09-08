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

/**
 * 
 * 重调用链的span信息，与轻调用链区别为：
 * 
 * 1，只关心轻调用链的traceId，spanId，epinfo(只关心","之前，不关心src),appid；
 * 
 * 2，额外报文体由具体子类实现。
 *
 */
public class SlowOperSpan {

    private String traceId = "N";
    private String spanId = "N";
    private String endpointInfo = "";
    private String appid = "";

    public SlowOperSpan(String traceId, String spanId, String endpointInfo, String appid) {
        super();
        this.traceId = traceId;
        this.spanId = spanId;
        this.endpointInfo = endpointInfo;
        this.appid = appid;
    }

    public String getAppid() {

        return appid;
    }

    public void setAppid(String appid) {

        this.appid = appid;
    }

    public String getEndpointInfo() {

        return this.endpointInfo;
    }

    public void setEndpointInfo(String endpointInfo) {

        this.endpointInfo = endpointInfo;
    }

    public String getTraceId() {

        return traceId;
    }

    public void setTraceId(String traceId) {

        this.traceId = traceId;
    }

    public String getSpanId() {

        return spanId;
    }

    public void setSpanId(String spanId) {

        this.spanId = spanId;
    }

    // 由于当前通过以下三个属性就能唯一确定一条调用链中的一个节点，故子类无需事先toString()，变相能够提高计算md5的速度
    @Override
    public String toString() {

        StringBuffer b = new StringBuffer();

        b.append(this.traceId).append(";");
        b.append(this.spanId).append(";");
        b.append(this.endpointInfo);

        return b.toString();
    }

    public Map<String, Object> toMap() {

        Map<String, Object> m = new LinkedHashMap<String, Object>();

        m.put("traceid", this.traceId);
        m.put("spanid", this.spanId);
        m.put("epinfo", this.endpointInfo);
        m.put("appid", this.appid);

        return m;
    }

}
