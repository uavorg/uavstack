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

package com.creditease.uav.apm.slowoper.span;

import com.creditease.agent.helpers.EncodeHelper;

/**
 * 
 * 重调用链的span信息，与轻调用链区别为：1，只关心轻调用链的traceId，spanId，epinfo(只关心","之前，不关心src)；2，额外添加报文体。
 *
 */
public class SlowOperSpan {

    private String traceId = "N";
    private String spanId = "N";
    private String endpointInfo = "";
    private StringBuilder content = new StringBuilder();

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

    public String getContent() {

        return content.toString();
    }

    /**
     * 将指定字符串拼接入content
     * 
     * @param str
     */
    public void appendContent(String str) {

        this.content.append(str);
    }

    /**
     * 将限定长度的字符串追加入content
     * 
     * 当length为小于0时不限制长度，为0时则直接为空（不去获取）
     * 
     * @param str
     * @param length
     * @param urlEncode
     */
    public void appendContent(String str, int length, boolean urlEncode) {

        if (length > 0 && str.length() > length) {
            str = str.substring(0, length);
        }
        else if (length == 0) {
            str = "";
        }
        if (urlEncode) {
            str = EncodeHelper.urlEncode(str);
        }
        this.content.append(";");
        this.content.append(str.length());
        this.content.append(";");
        this.content.append(str);
    }

    @Override
    public String toString() {

        StringBuffer b = new StringBuffer();

        b.append(this.traceId).append(";");
        b.append(this.spanId).append(";");
        b.append(this.endpointInfo);
        b.append(this.content);

        return b.toString();
    }

}
