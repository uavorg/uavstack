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

package com.creditease.agent.spi;

import java.util.HashMap;
import java.util.Map;

public class ActionContext {

    private boolean isSucessful;
    private String jumpTargetActionId;
    private Map<String, Object> params = new HashMap<String, Object>();
    private Exception e;

    public Exception getE() {

        return e;
    }

    public void setE(Exception e) {

        this.e = e;
    }

    public ActionContext() {
    }

    // public ActionContext(boolean isSucessful, Map<String, Object> params) {
    // this.isSucessful = isSucessful;
    // this.params = params;
    // }

    public boolean isSucessful() {

        return isSucessful;
    }

    public void setSucessful(boolean isSucessful) {

        this.isSucessful = isSucessful;
    }

    public Map<String, Object> getParams() {

        return params;
    }

    public void setParams(Map<String, Object> params) {

        this.params = params;
    }

    public Object getParam(String param) {

        return params.get(param);
    }

    public void putParam(String param, Object value) {

        params.put(param, value);
    }

    public void reset() {

        params.clear();
        jumpTargetActionId = null;
    }

    protected String getJumpTargetActionId() {

        return jumpTargetActionId;
    }

    protected void setJumpTargetActionId(String jumpTargetActionId) {

        this.jumpTargetActionId = jumpTargetActionId;
    }
}
