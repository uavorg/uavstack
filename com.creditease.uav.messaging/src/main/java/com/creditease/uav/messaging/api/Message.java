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

package com.creditease.uav.messaging.api;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.creditease.agent.helpers.JSONHelper;


public class Message implements Serializable {

    private static final long serialVersionUID = -5490137500811637000L;

    private Map<String, String> params = new LinkedHashMap<String, String>();

    private String messageType;

    private long timeStamp;

    // 必须要有systemId区别 ， 因为同一个监控系统可能在监控不同应用，用于区别应用
    private String systemId;

    public String getSystemId() {

        return systemId;
    }

    public void setSystemId(String systemId) {

        this.systemId = systemId;
    }

    public String getIDString() {

        return getMessageType() + "-" + systemId + "-" + getTimeStamp();
    }

    public Map<String, String> getParams() {

        return params;
    }

    public void setParams(Map<String, String> params) {

        this.params = params;
    }

    public void setParam(String key, String value) {

        params.put(key, value);
    }

    public String getParam(String key) {

        return params.get(key);
    }

    public String toJSONString() {

        return JSONHelper.toString(this);
    }

    public void setTimeStamp(long timeStamp) {

        this.timeStamp = timeStamp;
    }

    public long getTimeStamp() {

        return timeStamp;
    }

    public String getMessageType() {

        return messageType;
    }

    public void setMessageType(String messageType) {

        this.messageType = messageType;
    }
}
