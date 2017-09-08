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

public class MethodSlowOperSpan extends SlowOperSpan {

    private String methodReq;
    private String methodRet;

    public MethodSlowOperSpan(String traceId, String spanId, String endpointInfo, String appid, String methodReq,
            String methodRet) {
        super(traceId, spanId, endpointInfo, appid);
        this.methodReq = methodReq;
        this.methodRet = methodRet;
    }

    public String getMethodReq() {

        return methodReq;
    }

    public void setMethodReq(String methodReq) {

        this.methodReq = methodReq;
    }

    public String getMethodRet() {

        return methodRet;
    }

    public void setMethodRet(String methodRet) {

        this.methodRet = methodRet;
    }

    @Override
    public Map<String, Object> toMap() {

        Map<String, Object> m = new LinkedHashMap<String, Object>();
        m.putAll(super.toMap());
        m.put("method_req", this.methodReq);
        m.put("method_ret", this.methodRet);
        return m;
    }

}
