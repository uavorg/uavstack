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

package com.creditease.uav.grafana;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.ws.rs.container.AsyncResponse;

import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;

@SuppressWarnings("rawtypes")
public class GrafanaHttpCallBack implements HttpClientCallback {

    protected ISystemLogger logger = SystemLogger.getLogger(GrafanaHttpCallBack.class);
    protected String requestType = null;
    protected String requestUrl = null;
    protected String requestData = null;
    protected CountDownLatch latch = null;
    protected Map params = new HashMap();
    protected AsyncResponse response;

    public void setResponse(AsyncResponse response) {

        this.response = response;
    }

    public AsyncResponse getResponse() {

        return this.response;
    }

    public GrafanaHttpCallBack() {
    }

    @SuppressWarnings("unchecked")
    public GrafanaHttpCallBack(Map _params) {
        if (null != _params && !_params.isEmpty()) {
            this.params.putAll(_params);
        }
    }

    @SuppressWarnings("unchecked")
    public GrafanaHttpCallBack(Map _params, CountDownLatch latch) {
        if (null != _params && !_params.isEmpty()) {
            this.params.putAll(_params);
        }
        this.latch = latch;
    }

    public String getRequestType() {

        return requestType;
    }

    public void setRequestType(String requestType) {

        this.requestType = requestType;
    }

    public String getRequestUrl() {

        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {

        this.requestUrl = requestUrl;
    }

    public String getRequestData() {

        return requestData;
    }

    public void setRequestData(String requestData) {

        this.requestData = requestData;
    }

    @SuppressWarnings("unchecked")
    public void appendParams(Map _params) {

        if (null != _params && !_params.isEmpty()) {
            this.params.putAll(_params);
        }
    }

    public Map getParams() {

        return params;
    }

    @Override
    public void completed(HttpClientCallbackResult result) {

        String resp = result.getReplyDataAsString();// 此方法是流读取，只能获取一次
        completedLog(result, resp);
    }

    @Override
    public void failed(HttpClientCallbackResult result) {

        String resp = result.getReplyDataAsString();
        String resultMsg = "{\"code\":\"00\",\"msg\":\"操作失败\"}";
        failedLog(result, resp);
        if (latch != null) {
            latch.countDown();
        }
        if (this.response != null) {
            this.response.resume(resultMsg);
        }

    }

    protected void completedLog(HttpClientCallbackResult result, String respStr) {

        logger.info(this, getLogFormat(result, respStr));
    }

    protected void failedLog(HttpClientCallbackResult result, String respStr) {

        logger.err(this, getLogFormat(result, respStr), result.getException());
    }

    private String getLogFormat(HttpClientCallbackResult result, String respStr) {

        return "GrafanaHttpCallBack. " + " returnCode:" + result.getRetCode() + " requestType:" + requestType
                + " requestUrl:" + requestUrl + " requestData:" + requestData + " requestResp:" + respStr;
    }

}