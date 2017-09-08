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

package com.creditease.agent.feature.hbagent;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.creditease.agent.heartbeat.api.HeartBeatEvent;
import com.creditease.agent.heartbeat.api.HeartBeatEvent.Stage;
import com.creditease.agent.helpers.ConnectionFailoverMgrHelper;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.spi.AbstractTimerWork;
import com.creditease.uav.helpers.connfailover.ConnectionFailoverMgr;
import com.creditease.uav.httpasync.HttpAsyncClient;
import com.creditease.uav.httpasync.HttpAsyncClientFactory;
import com.creditease.uav.httpasync.HttpAsyncException;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;

public class HeartBeatClientReqWorker extends AbstractTimerWork {

    private HttpAsyncClient client = null;

    private ConnectionFailoverMgr cfmgr;

    public HeartBeatClientReqWorker(String cName, String feature) {
        super(cName, feature);

        /**
         * in fact only 1 connection is enough, another route is for backup thinking the max route could be 5=10/2,
         * considering the route switching
         */
        client = HttpAsyncClientFactory.build(2, 10, 30000, 30000, 30000);

        /**
         * load heartbeat server urls
         */

        String serverURLs = this.getConfigManager().getFeatureConfiguration("hbclientagent", "hbservers");

        cfmgr = ConnectionFailoverMgrHelper.getConnectionFailoverMgr(serverURLs, 30000);

    }

    @Override
    public void setPeriod(long period) {

        super.setPeriod(period);
    }

    /**
     * 实现服务发现功能：MSCP的服务发现需要利用心跳客户端的支持
     * 
     * @param serviceName
     * @return
     */
    public String[] doServiceDiscovery(String serviceName) {

        /**
         * step 1: select a heartbeat server url
         */
        final String connectStr = cfmgr.getConnection();

        if (null == connectStr) {
            log.err(this, "Select HeartBeatServer URL Fail as no available HeartBeatServer.");
            return null;
        }

        String[] connectInfo = connectStr.split(":");

        /**
         * NOTE: hbQueryServer's port=hbServer's port +10
         */
        int hbServerPort = DataConvertHelper.toInt(connectInfo[1], 8010) + 10;

        String hbserverURL = "http://" + connectInfo[0] + ":" + hbServerPort + "/hb/query";

        if (log.isDebugEnable()) {
            log.debug(this, "Selected HeartBeatServer URL is " + hbserverURL);
        }

        /**
         * step 2: build msg
         */
        UAVHttpMessage msg = new UAVHttpMessage();

        msg.setIntent("services");

        msg.putRequest("service", serviceName);

        byte[] datab = null;
        try {
            datab = JSONHelper.toString(msg).getBytes("utf-8");
        }
        catch (UnsupportedEncodingException e1) {
            return null;
        }

        /**
         * step 3: request hbquery
         */
        final CountDownLatch cdl = new CountDownLatch(1);

        final StringBuffer resultStr = new StringBuffer();

        client.doAsyncHttpPost(hbserverURL, datab, "application/json", "utf-8", new HttpClientCallback() {

            @Override
            public void completed(HttpClientCallbackResult result) {

                /**
                 * step 4: run handlers for downstream response
                 */
                resultStr.append(result.getReplyDataAsString());

                // CountDownLatch unlock
                cdl.countDown();
            }

            @Override
            public void failed(HttpClientCallbackResult result) {

                /**
                 * mark this hbserver is NOT OK
                 */
                cfmgr.putFailConnection(connectStr);

                // CountDownLatch unlock
                cdl.countDown();
            }

        });

        /**
         * step 4: wait the async http invoking result
         */
        try {
            cdl.await(5000, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e) {
            // ignore
        }

        if (resultStr.length() == 0) {
            return null;
        }

        @SuppressWarnings("unchecked")
        Map<String, String> resMap = JSONHelper.toObject(resultStr.toString(), Map.class);

        if (resMap.containsKey("err")) {
            return null;
        }

        String servicesStr = resMap.get("rs");

        List<String> services = JSONHelper.toObjectArray(servicesStr, String.class);

        String[] urls = new String[services.size()];

        return services.toArray(urls);
    }

    @Override
    public void run() {

        /**
         * step 1: select a heartbeat server url
         */
        final String connectStr = cfmgr.getConnection();

        if (null == connectStr) {
            log.err(this, "Select HeartBeatServer URL Fail as no available HeartBeatServer.");
            return;
        }

        String hbserverURL = "http://" + connectStr + "/heartbeat";

        if (log.isDebugEnable()) {
            log.debug(this, "Selected HeartBeatServer URL is " + hbserverURL);
        }

        /**
         * step 2: run handlers for upstream request
         */
        final HeartBeatEventClientWorker hbEventClientWorker = (HeartBeatEventClientWorker) this.getConfigManager()
                .getComponent(this.feature, "HeartBeatEventClientWorker");

        HeartBeatEvent reqevent = new HeartBeatEvent(Stage.CLIENT_OUT);

        hbEventClientWorker.runHandlers(reqevent);

        byte[] datab = null;
        try {
            datab = reqevent.toJSONString().getBytes("utf-8");
        }
        catch (UnsupportedEncodingException e) {
            log.err(this, "Convert HeartBeatClientEvent into bytes Fail.", e);
            return;
        }

        /**
         * step 3: send HeartBeatClientEvent out
         */

        final CountDownLatch cdl = new CountDownLatch(1);

        client.doAsyncHttpPost(hbserverURL, datab, "application/json", "utf-8", new HttpClientCallback() {

            @Override
            public void completed(HttpClientCallbackResult result) {

                /**
                 * step 4: run handlers for downstream response
                 */
                handleResponse(hbEventClientWorker, result);

                // CountDownLatch unlock
                cdl.countDown();
            }

            @Override
            public void failed(HttpClientCallbackResult result) {

                /**
                 * mark this hbserver is NOT OK
                 */
                cfmgr.putFailConnection(connectStr);
                /**
                 * step 4: run handlers for downstream response
                 */
                handleResponse(hbEventClientWorker, result);

                // CountDownLatch unlock
                cdl.countDown();
            }

        });

        /**
         * step 4: wait the async http invoking result
         */
        try {
            cdl.await(60000, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e) {
            // ignore
        }
    }

    protected void handleResponse(HeartBeatEventClientWorker hbEventClientWorker, HttpClientCallbackResult result) {

        /**
         * step 4.1: check if there is exception
         */
        HttpAsyncException hae = result.getException();

        if (null != hae) {
            log.err(this, "Receive HeartBeatClientEvent Fail: StatusCode=" + result.getRetCode() + ",ExceptionEvent="
                    + hae.getExceptionEvent(), hae.getCause());
            return;
        }

        /**
         * step 4.2: put the HeartBeatClientEvent into downstream queue
         */
        String respStr = result.getReplyDataAsString();

        HeartBeatEvent respEvent = new HeartBeatEvent(Stage.CLIENT_IN, respStr);
        respEvent.setStage(Stage.CLIENT_IN);

        hbEventClientWorker.putData(respEvent);
    }

    /**
     * getCurrentHBServerURL
     * 
     * @return
     */
    public String getCurrentHBServerURL() {

        return cfmgr.getConnection();
    }

    @Override
    public void cancel() {

        super.cancel();

        client.shutdown();
    }
}
