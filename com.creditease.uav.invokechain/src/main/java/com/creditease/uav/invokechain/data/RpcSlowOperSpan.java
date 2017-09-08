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

import com.creditease.agent.helpers.StringHelper;

public class RpcSlowOperSpan extends SlowOperSpan {

    private String rpcReqHead;
    private String rpcReqBody;
    private String rpcRspHead;
    private String rpcRspBody;
    private String rpcRspException;

    public RpcSlowOperSpan(String traceId, String spanId, String endpointInfo, String appid, String rpcReqHead,
            String rpcReqBody, String rpcRspHead, String rpcRspBody, String rpcRspException) {
        super(traceId, spanId, endpointInfo, appid);
        this.rpcReqHead = rpcReqHead;
        this.rpcReqBody = rpcReqBody;
        this.rpcRspHead = rpcRspHead;
        this.rpcRspBody = rpcRspBody;
        this.rpcRspException = rpcRspException;
    }

    public String getRpcReqHead() {

        return rpcReqHead;
    }

    public void setRpcReqHead(String rpcReqHead) {

        this.rpcReqHead = rpcReqHead;
    }

    public String getRpcReqBody() {

        return rpcReqBody;
    }

    public void setRpcReqBody(String rpcReqBody) {

        this.rpcReqBody = rpcReqBody;
    }

    public String getRpcRspHead() {

        return rpcRspHead;
    }

    public void setRpcRspHead(String rpcRspHead) {

        this.rpcRspHead = rpcRspHead;
    }

    public String getRpcRspBody() {

        return rpcRspBody;
    }

    public void setRpcRspBody(String rpcRspBody) {

        this.rpcRspBody = rpcRspBody;
    }

    public String getRpcRspException() {

        return rpcRspException;
    }

    public void setRpcRspException(String rpcRspException) {

        this.rpcRspException = rpcRspException;
    }

    @Override
    public Map<String, Object> toMap() {

        Map<String, Object> m = new LinkedHashMap<String, Object>();
        m.putAll(super.toMap());
        m.put("rpc_req_head", this.rpcReqHead);
        m.put("rpc_req_body", this.rpcReqBody);
        if (StringHelper.isEmpty(this.rpcRspException)) {
            m.put("rpc_rsp_head", this.rpcRspHead);
            m.put("rpc_rsp_body", this.rpcRspBody);
        }
        else {
            m.put("rpc_rsp_exception", this.rpcRspException);
        }
        return m;
    }

}
