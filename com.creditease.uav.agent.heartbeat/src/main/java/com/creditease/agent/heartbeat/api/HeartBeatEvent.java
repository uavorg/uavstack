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

package com.creditease.agent.heartbeat.api;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.creditease.agent.helpers.JSONHelper;

/**
 * HeartBeatEvent is the payload communicated among the hb client and server
 * 
 * @author zhen zhang
 *
 */
public class HeartBeatEvent {

    public static enum Stage {
        CLIENT_OUT, CLIENT_IN, SERVER_IN, SERVER_OUT
    }

    private Stage stage;

    private Map<String, Map<String, Object>> requestMsgs = new LinkedHashMap<String, Map<String, Object>>();

    private Map<String, Map<String, Object>> responseMsgs = new LinkedHashMap<String, Map<String, Object>>();

    public HeartBeatEvent(Stage stage) {
        this.stage = stage;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public HeartBeatEvent(Stage stage, String jsonString) {

        this(stage);

        Map tmpMsgs = JSONHelper.toObject(jsonString, Map.class);

        Set s = tmpMsgs.entrySet();

        for (Object o : s) {

            Entry e = (Entry) o;

            Map m = (Map) JSONHelper.convertJO2POJO(e.getValue());

            getMsgMap().put((String) e.getKey(), m);
        }
    }

    private Map<String, Map<String, Object>> getMsgMap() {

        switch (stage) {
            case CLIENT_IN:
                return responseMsgs;
            case CLIENT_OUT:
                return requestMsgs;
            case SERVER_IN:
                return requestMsgs;
            case SERVER_OUT:
                return responseMsgs;
            default:
                return requestMsgs;
        }
    }

    public Stage getStage() {

        return stage;
    }

    public void setStage(Stage stage) {

        this.stage = stage;
    }

    public boolean containEvent(String eventKey) {

        boolean exist = false;

        if (stage == Stage.SERVER_OUT) {
            exist = this.requestMsgs.containsKey(eventKey);
        }

        if (exist == false) {
            return getMsgMap().containsKey(eventKey);
        }
        else {
            return true;
        }
    }

    public void putParam(String eventKey, String paramKey, Object paramValue) {

        /**
         * CLIENT_IN can not write anything
         */
        if (stage == Stage.CLIENT_IN) {
            return;
        }

        if (null == eventKey || null == paramKey || null == paramValue) {
            return;
        }

        Map<String, Object> paramMap = getMsgMap().get(eventKey);

        if (null == paramMap) {
            paramMap = new LinkedHashMap<String, Object>();
            getMsgMap().put(eventKey, paramMap);
        }

        paramMap.put(paramKey, paramValue);
    }

    public Object getParam(String eventKey, String paramKey) {

        if (null == eventKey || null == paramKey) {
            return null;
        }

        Map<String, Object> paramMap = getMsgMap().get(eventKey);

        if (null == paramMap) {

            /**
             * SERVER_OUT need also search the event in requestMsgs
             */
            if (stage == Stage.SERVER_OUT) {
                paramMap = this.requestMsgs.get(eventKey);

                if (null == paramMap) {

                    return null;
                }

                return paramMap.get(paramKey);
            }
            else {
                return null;
            }
        }

        return paramMap.get(paramKey);
    }

    public String toJSONString() {

        return JSONHelper.toString(getMsgMap());
    }
}
