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

package com.creditease.agent.feature.monitoragent.datacatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.creditease.agent.feature.monitoragent.AppServerProfileDataCatchWorker;
import com.creditease.agent.feature.monitoragent.detect.BaseDetector;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.helpers.jvmtool.JVMAgentInfo;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.uav.httpasync.HttpAsyncClient;
import com.creditease.uav.httpasync.HttpAsyncClientFactory;
import com.creditease.uav.httpasync.HttpAsyncException;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;

public abstract class BaseHttpMonitorDataCatchWorker extends BaseMonitorDataCatchWorker {

    /**
     * 
     * HttpDataResult description: when access data via http request, wrap response as object
     *
     */
    private class HttpDataResult {

        private String data;
        private Throwable e;

        public String getData() {

            return data;
        }

        public void setData(String data) {

            this.data = data;
        }

        public Throwable getE() {

            return e;
        }

        public void setE(Throwable e) {

            this.e = e;
        }
    }

    protected HttpAsyncClient client;

    public BaseHttpMonitorDataCatchWorker(String cName, String feature, JVMAgentInfo appServerInfo,
            BaseDetector detector) {
        super(cName, feature, appServerInfo, detector);

        client = HttpAsyncClientFactory.build(2, 5, 2, 2, 2);
    }

    @Override
    public void run() {

        try {

            boolean needProcessCheck = true;

            long timeFlag = (System.currentTimeMillis() / 10) * 10;

            // get all monitor's MBean
            MonitorDataFrame mdf = new MonitorDataFrame(this.getWorkerId(), "M", timeFlag);

            needProcessCheck = doCaptureMonitorData(timeFlag, mdf);

            // if needProcessCheck is still true, need see if the appserver is still alive
            if (needProcessCheck == true) {
                doHealthReaction();
                return;
            }
            else {
                /**
                 * if there is data, we handle MDF using monitor data handler to process the monitor data
                 */
                if (!mdf.isEmpty()) {
                    List<MonitorDataFrame> ml = new ArrayList<MonitorDataFrame>();
                    ml.add(mdf);
                    this.detector.runHandlers(ml);
                }
            }

            // get all profile's MBean
            MonitorDataFrame pmdf = new MonitorDataFrame(this.getWorkerId(), "P", timeFlag);

            needProcessCheck = doCaptureProfileData(timeFlag, pmdf);

            // if needProcessCheck is still true, need see if the appserver is still alive
            if (needProcessCheck == true) {
                doHealthReaction();
                return;
            }
            else {
                /**
                 * if there is data, we handle MDF
                 */
                if (!pmdf.isEmpty()) {
                    /**
                     * as profile data is low frequency data, then we just need 1 thread for all appservers on the same
                     * host machine to publish the data
                     */
                    AppServerProfileDataCatchWorker apdc = (AppServerProfileDataCatchWorker) this.getConfigManager()
                            .getComponent(this.feature, "AppServerProfileDataCatchWorker");

                    apdc.putData(pmdf);
                }
            }

        }
        catch (IOException e) {
            // if connect fails, try process detecting
            doHealthReaction();
        }
    }

    /**
     * doCaptureProfileData
     * 
     * @param mbsc
     * @param timeFlag
     * @param pmdf
     * @return
     * @throws IOException
     */
    @SuppressWarnings("rawtypes")
    protected boolean doCaptureProfileData(long timeFlag, MonitorDataFrame pmdf) throws IOException {

        boolean needProcessCheck = true;

        String profileService = this.appServerInfo.getJVMAccessURL() + "profile?action=getProfileData";

        String data = accessData(profileService, null);

        List<Map> profiles = JSONHelper.toObjectArray(data, Map.class);

        /**
         * if there is the first to touch the profile data, we need pass it
         */
        long curTime = System.currentTimeMillis();

        boolean isRefreshTimestamp = false;

        Map<String, Boolean> whoNeedUpdateState = new HashMap<String, Boolean>();

        for (Map profile : profiles) {

            Integer needProcessCheckState = (Integer) profile.get("state");

            Boolean isUpdate = (Boolean) profile.get("update");

            if (null != needProcessCheckState) {
                needProcessCheck = false;
            }

            /**
             * if the profile data is not finished, we need wait next time
             */
            if (null == needProcessCheckState || needProcessCheckState != 2) {
                continue;
            }

            /**
             * this data is new and also need for profile heartbeat
             */
            if (state.getProfileTimestamp() == 0 || isUpdate == true) {
                isRefreshTimestamp = true;
            }
            /**
             * this data is old, but need for profile heartbeat even the if Update = false, but we will still pass the
             * profile data as a heartbeat for profiledata the heart beat interval = 1 min
             */
            else if (isUpdate == false && curTime - state.getProfileTimestamp() > 60000) {
                isRefreshTimestamp = true;
                pmdf.setTag("P:HB");
            }
            else {
                continue;
            }

            // add data to MonitorDataFrame
            String frameId = (String) profile.get("id");

            // set Update=false
            if (isUpdate == true) {
                // need UPDATE the update field in HttpDataObserver
                whoNeedUpdateState.put(frameId, false);
            }

            String pdataStr = JSONHelper.toString(profile.get("data"));
            pmdf.addData(frameId, pdataStr);

            Map<String, Object> webapp = pmdf.getElemInstValues(frameId, "cpt", "webapp");

            if (webapp.isEmpty()) {
                continue;
            }

            /**
             * NOTE:为了实现动态抓取Profile的变化，一些属性被写到SystemProperties中，这样MA就可以抓走了
             */
            /**
             * 【1】AppGroup
             * 
             * NOTE: if there is no special appgroup, use MonitorAgent appgroup
             */
            String uavMAAppGroup = System.getProperty("JAppGroup");

            String appGroup = this.appServerInfo.getSystemProperties().getProperty("JAppGroup");

            if (!StringHelper.isEmpty(appGroup)) {
                webapp.put("appgroup", appGroup);
            }
            else if (!StringHelper.isEmpty(uavMAAppGroup)) {
                webapp.put("appgroup", uavMAAppGroup);
            }

            /**
             * 【2】自定义指标
             */
            Enumeration<?> enumeration = this.appServerInfo.getSystemProperties().propertyNames();

            Map<String, Map> custMetrics = new HashMap<>();

            while (enumeration.hasMoreElements()) {

                String name = (String) enumeration.nextElement();

                int moIndex = name.indexOf("mo@");

                if (moIndex != 0) {
                    continue;
                }

                String[] metrics = null;
                try {
                    metrics = name.split("@");

                    // add metricName to customizedMetrics
                    if (metrics.length == 3) {
                        custMetrics.put(metrics[1], JSONHelper.toObject(metrics[2], Map.class));
                    }
                    else {
                        custMetrics.put(metrics[1], Collections.emptyMap());
                    }
                }
                catch (Exception e) {
                    log.err(this, "AppServerMonitorDataCatchWorker[" + this.cName + "] CHECK customized metric[" + name
                            + "] format error: cur metric=" + name, e);

                }
            }

            webapp.put("appmetrics", JSONHelper.toString(custMetrics));

            /**
             * 【3】MOFMetaData
             */
            String jAppMOFMeta = this.appServerInfo.getSystemProperties().getProperty("JAppMOFMetaData");

            if (!StringHelper.isEmpty(jAppMOFMeta)) {
                webapp.put("mofmeta", jAppMOFMeta);
            }
        }

        if (isRefreshTimestamp == true) {
            state.setProfileTimestamp(curTime);
        }

        if (whoNeedUpdateState.size() > 0) {
            String upProfileService = this.appServerInfo.getJVMAccessURL() + "profile?action=updateProfile";
            this.accessData(upProfileService, JSONHelper.toString(whoNeedUpdateState));
        }

        return needProcessCheck;
    }

    /**
     * doCaptureMonitorData
     * 
     * @param mbsc
     * @param timeFlag
     * @return
     * @throws IOException
     */
    @SuppressWarnings("rawtypes")
    protected boolean doCaptureMonitorData(long timeFlag, MonitorDataFrame mdf) throws IOException {

        boolean needProcessCheck = true;

        String monitorService = this.appServerInfo.getJVMAccessURL() + "monitor?action=getMonitorData";

        String data = accessData(monitorService, null);

        List<Map> monitors = JSONHelper.toObjectArray(data, Map.class);

        for (Map monitor : monitors) {

            String id = (String) monitor.get("id");
            String mdataStr = JSONHelper.toString(monitor.get("data"));

            mdf.addData(id, mdataStr);
        }

        // add appgroup
        mdf.addExt("appgroup", this.getAppGroup());

        needProcessCheck = false;

        return needProcessCheck;
    }

    /**
     * try to get data from UAVMOF http service
     * 
     * @param serviceURL
     * @return
     * @throws IOException
     */
    private String accessData(String serviceURL, String postData) throws IOException {

        final CountDownLatch wait = new CountDownLatch(1);

        final HttpDataResult hdr = new HttpDataResult();

        HttpClientCallback callback = new HttpClientCallback() {

            @Override
            public void completed(HttpClientCallbackResult result) {

                String data = result.getReplyDataAsString();

                if (!StringHelper.isEmpty(data)) {
                    hdr.setData(data);
                }

                wait.countDown();
            }

            @Override
            public void failed(HttpClientCallbackResult result) {

                HttpAsyncException hae = result.getException();

                if (hae != null) {
                    hdr.setE(hae.getCause());
                }

                wait.countDown();
            }
        };

        if (postData == null) {
            client.doAsyncHttpGet(serviceURL, callback);
        }
        else {
            client.doAsyncHttpPost(serviceURL, postData, callback);
        }

        // timeout for response
        try {
            wait.await(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            throw new IOException(e);
        }

        // check if there is exception during http response
        if (hdr.getE() != null) {
            throw new IOException(hdr.getE());
        }

        return hdr.getData();
    }

    @Override
    public void updateAttrs(JVMAgentInfo appServerInfo) {

        // do nothing
    }

    @Override
    public boolean isVMAlive(JVMAgentInfo appServerInfo) {

        String serviceURL = appServerInfo.getJVMAccessURL() + "jvm?action=ping";

        try {
            String vendor = this.accessData(serviceURL, null);

            if (!StringHelper.isEmpty(vendor)) {
                return true;
            }
        }
        catch (IOException e) {
            return false;
        }

        return false;
    }

    @Override
    public int start() {

        return 1;
    }

}
