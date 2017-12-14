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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpResponse;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.uav.helpers.encode.Base64;
import com.creditease.uav.httpasync.HttpAsyncClient;
import com.creditease.uav.httpasync.HttpAsyncClientFactory;

@SuppressWarnings("unchecked")
public class GrafanaClient {

    private HttpAsyncClient httpAsyncClient = null;
    private Map<String, String> configMap = new HashMap<String, String>();

    GrafanaClient(HttpServletRequest request) {
        init(request);
    }

    public void init(HttpServletRequest request) {

        if (configMap.isEmpty()) {
            configMap = JSONHelper.toObject(
                    request.getServletContext().getInitParameter("uav.apphub.sso.grafana.connection.info"), Map.class);
        }

        if (null == httpAsyncClient) {
            Map<String, Integer> httpParamsMap = JSONHelper.toObject(
                    request.getServletContext().getInitParameter("uav.app.grafana.http.client.params"), Map.class);
            httpAsyncClient = HttpAsyncClientFactory.build(httpParamsMap.get("max.con"),
                    httpParamsMap.get("max.tot.con"), httpParamsMap.get("sock.time.out"),
                    httpParamsMap.get("con.time.out"), httpParamsMap.get("req.time.out"));
        }

    }

    /***
     * 
     * @param type
     * @param path
     * @param dataStr
     * @param callback
     *            :包含配置信息，请求参数，传递参数，请求类型，请求地址
     * @return
     */
    public Future<HttpResponse> doAsyncHttp(String type, String path, String dataStr, GrafanaHttpCallBack callback) {

        return doAsyncHttp(type, path, dataStr, null, callback);
    }

    public Future<HttpResponse> doAsyncHttp(String type, String path, String dataStr, Map<String, String> head,
            GrafanaHttpCallBack callback) {

        Future<HttpResponse> response = null;

        // def callback
        if (null == callback) {
            callback = new GrafanaHttpCallBack(configMap);
        }

        // set req info
        callback.setRequestUrl(getApiHttpReqUrl(path));
        callback.setRequestType(type);
        callback.setRequestData(dataStr);
        callback.appendParams(configMap);

        // def head
        if (null == head) {
            head = getHttpReqHead();
        }
        else {
            head.putAll(getHttpReqHead());
        }

        // action
        if ("post".equals(type)) {
            response = httpAsyncClient.doAsyncHttpPost(getApiHttpReqUrl(path), head, dataStr, null, "application/json",
                    "utf-8", callback);
        }
        else if ("get".equals(type)) {
            response = httpAsyncClient.doAsyncHttpGet(getApiHttpReqUrl(path), head, "application/json", callback);
        }
        else if ("delete".equals(type)) {
            response = httpAsyncClient.doAsyncHttpDel(getApiHttpReqUrl(path), head, "application/json", callback);
        }
        return response;
    }

    public String getConfigValue(String key) {

        if (!configMap.containsKey(key)) {
            return null;
        }

        return configMap.get(key);

    }

    private String getAuthLoginId() {

        return getConfigValue("authorization.loginId");
    }

    private String getAuthLoginPwd() {

        return getConfigValue("authorization.loginPwd");
    }

    private Map<String, String> getHttpReqHead() {

        Map<String, String> header = new HashMap<String, String>();
        String authorInfo = getAuthLoginId() + ":" + getAuthLoginPwd();
        authorInfo = "Basic " + new String(Base64.encode(authorInfo.getBytes(), Base64.NO_WRAP));
        header.put("Authorization", authorInfo);
        header.put("Content-type", "application/json;charset=utf-8");
        header.put("Accept", "application/json");
        return header;
    }

    private String getApiHttpReqUrl(String methodPath) {

        return getConfigValue("api.url") + methodPath;

    }

    public HttpResponse doHttp(String type, String path, String dataStr, GrafanaHttpCallBack callback) {

        HttpResponse response = null;
        try {
            response = doAsyncHttp(type, path, dataStr, null, callback).get();
        }
        catch (InterruptedException | ExecutionException e) {
        }

        return response;
    }

    public HttpResponse doHttp(String type, String path, String dataStr, Map<String, String> head,
            GrafanaHttpCallBack callback) {

        HttpResponse response = null;
        try {
            response = doAsyncHttp(type, path, dataStr, head, callback).get();
        }
        catch (InterruptedException | ExecutionException e) {
        }

        return response;
    }
}
