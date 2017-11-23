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

package org.uavstack.resources.common.invoker;

import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.spi.AbstractSystemInvoker;
import com.creditease.uav.helpers.connfailover.ConnectionFailoverMgr;
import com.creditease.uav.httpasync.HttpAsyncClient;
import com.creditease.uav.httpasync.HttpAsyncClientFactory;
import com.creditease.uav.httpasync.HttpAsyncException;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;

public class HttpSystemInvoker extends AbstractSystemInvoker<Object, HttpClientCallback> {

    /**
     * 
     * SyncResult description: 同步结果类
     *
     */
    private class SyncResult {

        private volatile Exception e;

        private volatile byte[] bdata;

        private volatile String sdata;

        private volatile CountDownLatch cdl = new CountDownLatch(1);

        public Exception getE() {

            return e;
        }

        public void setE(Exception e) {

            this.e = e;

            cdl.countDown();
        }

        public String getSdata() {

            return sdata;
        }

        public void setSdata(String sdata) {

            this.sdata = sdata;

            cdl.countDown();
        }

        public byte[] getBdata() {

            return bdata;
        }

        public void setBdata(byte[] bdata) {

            this.bdata = bdata;

            cdl.countDown();
        }

        public void waitSync(long timeout) {

            try {
                cdl.await(timeout, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    protected HttpAsyncClient httpAsyncClient;

    public HttpSystemInvoker(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public void start() {

        super.start();

        int maxConnectionPerRoute = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(feature, "http.client.maxperroute"), 20);
        int maxTotalConnection = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(feature, "http.client.maxtotal"), 100);
        int sockTimeout = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(feature, "http.client.sotimeout"), 10000);
        int connectTimeout = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(feature, "http.client.conntimeout"), 10000);
        int requestTimeout = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(feature, "http.client.reqtimeout"), 10000);

        httpAsyncClient = HttpAsyncClientFactory.build(maxConnectionPerRoute, maxTotalConnection, sockTimeout,
                connectTimeout, requestTimeout);
    }

    @Override
    public byte[] invoke(String serviceName, String serviceSubPath, Object msg) {

        return (byte[]) invoke(serviceName, serviceSubPath, msg, null, 30000, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> V invoke(String serviceName, String serviceSubPath, Object msg, Class<V> returnClass) {

        return (V) invoke(serviceName, serviceSubPath, msg, null, 30000, returnClass);
    }

    @Override
    public void invoke(String serviceName, String serviceSubPath, Object msg, HttpClientCallback callback) {

        invoke(serviceName, serviceSubPath, msg, callback, -1, null);
    }

    @Override
    public byte[] invoke(String serviceName, Object msg) {

        return (byte[]) invoke(serviceName, null, msg, null, 30000, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> V invoke(String serviceName, Object msg, Class<V> returnClass) {

        return (V) invoke(serviceName, null, msg, null, 30000, returnClass);
    }

    @Override
    public void invoke(String serviceName, Object msg, HttpClientCallback callback) {

        invoke(serviceName, null, msg, callback, -1, null);
    }

    /**
     * invoke
     * 
     * @param serviceName
     *            服务名
     * @param serviceSubPath
     *            服务相对路径（可选）
     * @param msg
     *            提交msg对象
     * @param callback
     *            异步回调（可选）
     * @param syncTimeout
     *            同步等待超时（可选）
     * @param returnClass
     *            同步返回对象类型（可选）
     * @return
     */
    private Object invoke(final String serviceName, final String serviceSubPath, final Object msg,
            final HttpClientCallback callback, final long syncTimeout, final Class<?> returnClass) {

        if (StringHelper.isEmpty(serviceName)) {
            return null;
        }

        // step 1: get service URI
        final ConnectionFailoverMgr cfm = this.getServiceRoute(serviceName);

        if (cfm == null) {
            throw new RuntimeException("NoSuchServiceExist:" + serviceName);
        }

        final String serviceURL = cfm.getConnection();

        if (serviceURL == null) {
            throw new RuntimeException("NoAvailableServiceInstance:" + serviceName);
        }

        String finalServiceURL = serviceURL;

        if (serviceSubPath != null) {
            finalServiceURL = serviceURL + serviceSubPath;
        }

        // step 2: convert request Object to byte
        byte[] data = null;

        // byte[]
        if (msg.getClass().isArray() && msg instanceof byte[]) {
            data = (byte[]) msg;
        }
        // UAVHttpMessage
        else if (UAVHttpMessage.class.isAssignableFrom(msg.getClass())) {
            try {
                data = JSONHelper.toString(msg).getBytes("utf-8");
            }
            catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        // String
        else if (String.class.isAssignableFrom(msg.getClass())) {
            try {
                data = msg.toString().getBytes("utf-8");
            }
            catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        final SyncResult syncResult = new SyncResult();

        // step 3: do post invoke
        httpAsyncClient.doAsyncHttpPost(finalServiceURL, data, "application/json", "utf-8", new HttpClientCallback() {

            @Override
            public void completed(HttpClientCallbackResult result) {

                if (callback != null) {
                    callback.completed(result);
                }

                if (syncTimeout <= 0) {
                    return;
                }

                if (returnClass != null) {
                    syncResult.setSdata(result.getReplyDataAsString());
                }
                else {
                    syncResult.setBdata(result.getResulDataAsByteArray());
                }
            }

            @Override
            public void failed(HttpClientCallbackResult result) {

                Exception exception = result.getException();

                /**
                 * Auto Failover
                 */
                if (getDefFailoverCondition(exception)) {

                    cfm.putFailConnection(serviceURL);

                    invoke(serviceName, serviceSubPath, msg, callback);

                    return;
                }

                if (callback != null) {
                    callback.failed(result);
                }

                if (syncTimeout <= 0) {
                    return;
                }

                HttpAsyncException excep = result.getException();

                if (excep != null) {
                    syncResult.setE(excep);
                }
            }

        });

        // step 4(optional): if sync mode, we should wait
        if (syncTimeout <= 0) {
            return null;
        }

        syncResult.waitSync(syncTimeout);

        if (syncResult.getE() != null) {

            throw new RuntimeException(syncResult.getE());
        }

        if (returnClass == null) {
            return syncResult.getBdata();
        }

        if (String.class.isAssignableFrom(returnClass)) {
            return syncResult.getSdata();
        }
        else if (UAVHttpMessage.class.isAssignableFrom(returnClass)) {
            return new UAVHttpMessage(syncResult.getSdata());
        }
        else {
            return JSONHelper.toObject(syncResult.getSdata(), returnClass);
        }
    }

    private boolean getDefFailoverCondition(Exception exception) {

        return exception != null && (exception.getMessage().indexOf("java.net.ConnectException") > -1
                || exception.getCause() instanceof SocketTimeoutException);
    }

    @Override
    public void stop() {

        this.httpAsyncClient.shutdown();

        super.stop();

    }

}
