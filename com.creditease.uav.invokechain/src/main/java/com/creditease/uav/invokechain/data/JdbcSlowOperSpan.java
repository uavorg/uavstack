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

public class JdbcSlowOperSpan extends SlowOperSpan {

    private String sqlReq;
    private String sqlRet;

    public JdbcSlowOperSpan(String traceId, String spanId, String endpointInfo, String appid, String sqlReq,
            String sqlRet) {
        super(traceId, spanId, endpointInfo, appid);
        this.sqlReq = sqlReq;
        this.sqlRet = sqlRet;
    }

    public String getSqlReq() {

        return sqlReq;
    }

    public void setSqlReq(String sqlReq) {

        this.sqlReq = sqlReq;
    }

    public String getSqlRet() {

        return sqlRet;
    }

    public void setSqlRet(String sqlRet) {

        this.sqlRet = sqlRet;
    }

    @Override
    public Map<String, Object> toMap() {

        Map<String, Object> m = new LinkedHashMap<String, Object>();
        m.putAll(super.toMap());
        m.put("sql_req", this.sqlReq);
        m.put("sql_ret", this.sqlRet);
        return m;
    }

}
