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

public class MqSlowOperSpan extends SlowOperSpan {

    private String mqHead;
    private String mqBody;

    public MqSlowOperSpan(String traceId, String spanId, String endpointInfo, String appid, String mqHead,
            String mqBody) {
        super(traceId, spanId, endpointInfo, appid);
        this.mqHead = mqHead;
        this.mqBody = mqBody;
    }

    public String getMqHead() {

        return mqHead;
    }

    public void setMqHead(String mqHead) {

        this.mqHead = mqHead;
    }

    public String getMqBody() {

        return mqBody;
    }

    public void setMqBody(String mqBody) {

        this.mqBody = mqBody;
    }

    @Override
    public Map<String, Object> toMap() {

        Map<String, Object> m = new LinkedHashMap<String, Object>();
        m.putAll(super.toMap());
        m.put("mq_head", this.mqHead);
        m.put("mq_body", this.mqBody);
        return m;
    }

}
