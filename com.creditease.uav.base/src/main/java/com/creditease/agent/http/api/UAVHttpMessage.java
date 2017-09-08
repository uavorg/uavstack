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

package com.creditease.agent.http.api;

import java.util.LinkedHashMap;
import java.util.Map;

import com.creditease.agent.helpers.JSONHelper;

/**
 * UAV网络通用HttpMessage
 * 
 * @author zhen zhang
 *
 */
public class UAVHttpMessage {

    public final static String RESULT = "rs";
    public final static String ERR = "err";
    public final static String BODY = "body";

    private String intent;

    private Map<String, String> request = new LinkedHashMap<String, String>();

    private Map<String, String> response = new LinkedHashMap<String, String>();

    public UAVHttpMessage() {
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public UAVHttpMessage(String jsonStr) {
        Map m = JSONHelper.toObject(jsonStr, Map.class);

        this.intent = (String) m.get("intent");
        this.request = (Map) m.get("request");

        Map resp = (Map) m.get(response);

        if (null != resp) {
            this.response = resp;
        }
    }

    public void setIntent(String intent) {

        this.intent = intent;
    }

    public String getIntent() {

        return this.intent;
    }

    public void putRequest(String key, String val) {

        this.request.put(key, val);
    }

    public String getRequest(String key) {

        return this.request.get(key);
    }

    public Map<String, String> getRequest() {

        return this.request;
    }

    public void putResponse(String key, String val) {

        this.response.put(key, val);
    }

    public String getResponseAsJsonString() {

        return JSONHelper.toString(response);
    }
}
