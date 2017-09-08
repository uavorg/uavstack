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

package com.creditease.uav.es;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.creditease.agent.helpers.ConnectionFailoverMgrHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.uav.helpers.connfailover.ConnectionFailoverMgr;
import com.creditease.uav.httpasync.HttpAsyncClient;
import com.creditease.uav.httpasync.HttpAsyncClientFactory;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;

public class EsRestServlet extends HttpServlet {

    static class EsResource {

        private static HttpAsyncClient httpAsyncClient = null;

        private static Map<String, String> info = null;

        private static Map<String, ConnectionFailoverMgr> connectionMgrPool = null;

        @SuppressWarnings("unchecked")
        public static void init(String esInfo, String context) {

            if (null == info) {
                info = JSONHelper.toObject(esInfo, Map.class);
            }

            if (null == httpAsyncClient) {
                Map<String, Integer> httpParamsMap = JSONHelper.toObject(context, Map.class);
                httpAsyncClient = HttpAsyncClientFactory.build(httpParamsMap.get("max.con"),
                        httpParamsMap.get("max.tot.con"), httpParamsMap.get("sock.time.out"),
                        httpParamsMap.get("con.time.out"), httpParamsMap.get("req.time.out"));
            }

            if (null == connectionMgrPool) {
                connectionMgrPool = new ConcurrentHashMap<String, ConnectionFailoverMgr>();
                String forwarUrl = getInfoValue("forwar.url");
                forwarUrl = forwarUrl.trim().replace("\n", "").replace("\r", "");
                ConnectionFailoverMgr cfm = ConnectionFailoverMgrHelper.getConnectionFailoverMgr(forwarUrl, 60000);
                connectionMgrPool.put("es.info.forwar.url", cfm);
            }

        }

        public static String getInfoValue(String key) {

            if (null == info) {
                return null;
            }
            return info.get(key);
        }

        public static HttpAsyncClient getHttpAsyncClient() {

            return httpAsyncClient;
        }

        public static Map<String, ConnectionFailoverMgr> getConnMgr() {

            return connectionMgrPool;
        }

    }

    private static final long serialVersionUID = 1441879223883391706L;

    @Override
    public void init() {

        EsResource.init(this.getInitParameter("es.info"),
                this.getServletContext().getInitParameter("uav.app.es.http.client.params"));
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // 获取请求资源
        String proName = req.getServletContext().getContextPath();
        String requestSource = req.getRequestURI();
        requestSource = requestSource.substring(requestSource.indexOf(proName) + proName.length());

        if (requestSource.startsWith("/es")) {
            requestSource = requestSource.substring(3);
        }

        /**
         * get url
         */
        Map<String, ConnectionFailoverMgr> connectionMgrPool = EsResource.getConnMgr();
        String forwarUrl = connectionMgrPool.get("es.info.forwar.url").getConnection() + requestSource;

        /**
         * get method
         */
        String method = req.getMethod();

        /**
         * get body
         */
        ServletInputStream input = req.getInputStream();

        EsResource.getHttpAsyncClient().doAsyncHttpMethodWithReqAsync(method, forwarUrl, null, input, null, null,
                "application/json", "utf-8", new EsRestServletCallBack(), req);
    }

}

class EsRestServletCallBack implements HttpClientCallback {

    @Override
    public void completed(HttpClientCallbackResult result) {

        resp(result);
    }

    @Override
    public void failed(HttpClientCallbackResult result) {

        resp(result);
    }

    private void resp(HttpClientCallbackResult result) {

        if (null != result.getException()) {
            ISystemLogger logger = SystemLogger.getLogger(EsRestServletCallBack.class);
            logger.err(this, result.getException().getMessage(), result.getException());
        }
        else {

            String respStr = result.getReplyDataAsString();
            try {
                result.getResponseForRequestAsync().write(respStr.getBytes());
                result.getResponseForRequestAsync().flush();
                result.getResponseForRequestAsync().close();
            }
            catch (IOException e) {
                ISystemLogger logger = SystemLogger.getLogger(EsRestServletCallBack.class);
                logger.err(this, result.getException().getMessage(), result.getException());
            }

        }

    }

}
