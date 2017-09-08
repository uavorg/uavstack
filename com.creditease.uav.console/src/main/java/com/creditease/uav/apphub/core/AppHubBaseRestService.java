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

package com.creditease.uav.apphub.core;

import java.net.ConnectException;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;

import com.creditease.agent.helpers.ConnectionFailoverMgrHelper;
import com.creditease.agent.helpers.NetworkHelper;
import com.creditease.uav.helpers.connfailover.ConnectionFailoverMgr;
import com.creditease.uav.httpasync.HttpAsyncClient;
import com.creditease.uav.httpasync.HttpAsyncClientFactory;
import com.creditease.uav.httpasync.HttpAsyncException;
import com.creditease.uav.httpasync.HttpAsyncException.ExceptionEvent;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;

@Singleton
public abstract class AppHubBaseRestService extends AppHubBaseComponent {

    /**
     * 实现AppHubBaseRestService的服务发现调用和failover能力
     */
    private static class HttpConnectionInvokeMgr {

        protected Map<String, ConnectionFailoverMgr> connectionMgrPool = null;

        /**
         * auto scan all init param started with @url: to build ConnectionFailoverMgr for each http server adress list
         * 
         * @param request
         */
        public synchronized void init(HttpServletRequest request) {

            if (connectionMgrPool != null) {
                return;
            }

            connectionMgrPool = new ConcurrentHashMap<String, ConnectionFailoverMgr>();

            Enumeration<String> initParamNames = request.getServletContext().getInitParameterNames();

            while (initParamNames.hasMoreElements()) {

                String paramName = initParamNames.nextElement();

                int index = paramName.indexOf("@url:");

                if (index != 0) {
                    continue;
                }

                String paramValue = request.getServletContext().getInitParameter(paramName);

                paramValue = paramValue.trim().replace("\n", "").replace("\r", "");

                ConnectionFailoverMgr cfm = ConnectionFailoverMgrHelper.getConnectionFailoverMgr(paramValue, 60000);

                connectionMgrPool.put(paramName.substring(5), cfm);
            }
        }

        /**
         * get ConnectionFailoverMgr
         * 
         * @param name
         * @return
         */
        public ConnectionFailoverMgr get(String name) {

            return connectionMgrPool.get(name);
        }
    }

    protected final HttpConnectionInvokeMgr httpConnInvokeMgr = new HttpConnectionInvokeMgr();

    @Context
    protected HttpServletRequest request;

    @Context
    protected HttpServletResponse response;

    protected HttpAsyncClient httpAsyncClient = null;

    /**
     * 初始化 APIService相关资源
     */
    @PostConstruct
    private void initService() {

        httpConnInvokeMgr.init(request);

        init();
    }

    protected abstract void init();

    /**
     * just for checking if the service is still available
     * 
     * @return
     */
    @GET
    public String ping() {

        return this.getClass().getName();
    }

    /**
     * 初始化HttpClient
     * 
     * @param maxConnectionPerRoute
     * @param maxTotalConnection
     * @param sockTimeout
     * @param connectTimeout
     * @param requestTimeout
     */
    protected void initHttpClient(int maxConnectionPerRoute, int maxTotalConnection, int sockTimeout,
            int connectTimeout, int requestTimeout) {

        httpAsyncClient = HttpAsyncClientFactory.build(maxConnectionPerRoute, maxTotalConnection, sockTimeout,
                connectTimeout, requestTimeout);
    }

    /**
     * 支持普通Http Post调用和智能的带FailOver的调用
     * 
     * @param postUrl
     * @param data
     * @param contentType
     * @param encoding
     * @param callBack
     */
    public void doHttpPost(String serverAddress, String subPath, byte[] data, String contentType, String encoding,
            HttpClientCallback callBack) {

        ConnectionFailoverMgr cfm = httpConnInvokeMgr.get(serverAddress);

        /**
         * Step 1: if there is no ConnectionFailoverMgr, take it as normal http post
         */
        if (cfm == null) {

            final String postURL = (subPath != null) ? (serverAddress + subPath) : serverAddress;

            httpAsyncClient.doAsyncHttpPost(postURL, data, contentType, encoding, callBack);
            return;
        }

        /**
         * Step 2: do smart http post
         */

        String url = cfm.getConnection();

        if (url == null) {
            String msg = "No Available Address for ServerAddressList[" + serverAddress + "]";
            this.getLogger().warn(this, msg);
            if (callBack != null) {

                HttpClientCallbackResult result = new HttpClientCallbackResult(null, null);
                result.setException(new HttpAsyncException(ExceptionEvent.REPLY_ERROR, new ConnectException(msg)));
                callBack.failed(result);
            }
            return;
        }

        String postURL = (subPath != null) ? (url + subPath) : url;

        getLogger().info(this, "doHttpPost URL :" + postURL);

        PostHttpCallback postHttpCb = new PostHttpCallback();
        postHttpCb.setCallBack(callBack);
        postHttpCb.setCfm(cfm);
        postHttpCb.setContentType(contentType);
        postHttpCb.setData(data);
        postHttpCb.setEncoding(encoding);
        postHttpCb.setPostURL(postURL);
        postHttpCb.setServerAddress(serverAddress);
        postHttpCb.setSubPath(subPath);
        postHttpCb.setUrl(url);

        httpAsyncClient.doAsyncHttpPost(postURL, data, contentType, encoding, postHttpCb);
    }

    protected String XSSFilter(String input) {

        return input.replaceAll("&lt;", "").replaceAll("&lt;/", "").replaceAll("&gt;", "").replaceAll("<script>", "")
                .replaceAll("</script>", "").replaceAll("javascript:", "").replaceAll("alert\\(", "")
                .replaceAll("console\\.", "");
    }

    public class PostHttpCallback implements HttpClientCallback {

        private HttpClientCallback callBack = null;
        private String serverAddress;
        private String subPath;
        private byte[] data;
        private String contentType;
        private String encoding;
        private ConnectionFailoverMgr cfm;
        private String postURL;
        private String url;

        public HttpClientCallback getCallBack() {

            return callBack;
        }

        public void setCallBack(HttpClientCallback callBack) {

            this.callBack = callBack;
        }

        public String getServerAddress() {

            return serverAddress;
        }

        public void setServerAddress(String serverAddress) {

            this.serverAddress = serverAddress;
        }

        public String getSubPath() {

            return subPath;
        }

        public void setSubPath(String subPath) {

            this.subPath = subPath;
        }

        public byte[] getData() {

            return data;
        }

        public void setData(byte[] data) {

            this.data = data;
        }

        public String getContentType() {

            return contentType;
        }

        public void setContentType(String contentType) {

            this.contentType = contentType;
        }

        public String getEncoding() {

            return encoding;
        }

        public void setEncoding(String encoding) {

            this.encoding = encoding;
        }

        public ConnectionFailoverMgr getCfm() {

            return cfm;
        }

        public void setCfm(ConnectionFailoverMgr cfm) {

            this.cfm = cfm;
        }

        public String getPostURL() {

            return postURL;
        }

        public void setPostURL(String postURL) {

            this.postURL = postURL;
        }

        public String getUrl() {

            return url;
        }

        public void setUrl(String url) {

            this.url = url;
        }

        @Override
        public void completed(HttpClientCallbackResult result) {

            if (callBack != null) {
                callBack.completed(result);
            }
        }

        @Override
        public void failed(HttpClientCallbackResult result) {

            /**
             * auto fail record
             */
            HttpAsyncException e = result.getException();

            if (e != null && ((e.getCause() instanceof java.net.ConnectException)
                    && e.getCause().getMessage().indexOf("Connection refused") > -1)) {

                getLogger().err(this, "Connection[" + postURL + "] is Unavailable. ", e);

                cfm.putFailConnection(url);
                /**
                 * let's retry
                 */
                doHttpPost(serverAddress, subPath, data, contentType, encoding, callBack);
                return;
            }

            if (callBack != null) {
                callBack.failed(result);
            }
        }
    }

    /**
     * getClientIP
     * 
     * @param remoteAddr
     * @param xForwardHeader
     * @return
     */
    public String getClientIP(String remoteAddr, String xForwardHeader) {

        String ip = xForwardHeader;
        if (ip != null && !"unKnown".equalsIgnoreCase(ip)) {
            int index = ip.indexOf(",");
            if (index != -1) {
                ip = ip.substring(0, index);
            }

            ip = getLocalHostToIP(ip);

        }
        else {
            ip = getLocalHostToIP(remoteAddr);
        }

        return ip;
    }

    /**
     * getLocalHostToIP
     * 
     * @param ip
     * @return
     */
    public String getLocalHostToIP(String ip) {

        if (ip == null) {
            return ip;
        }

        if (ip.equals("127.0.0.1") || "0:0:0:0:0:0:0:1".equals(ip) || "localhost".equals(ip)) {
            return NetworkHelper.getLocalIP();
        }

        if (ip.indexOf("127.0.0.1") > -1 || ip.indexOf("localhost") > -1 || ip.indexOf("0:0:0:0:0:0:0:1") > -1) {
            String localip = NetworkHelper.getLocalIP();
            ip = ip.replace("127.0.0.1", localip).replace("localhost", localip).replace("0:0:0:0:0:0:0:1", localip);
        }

        return ip;
    }
}
