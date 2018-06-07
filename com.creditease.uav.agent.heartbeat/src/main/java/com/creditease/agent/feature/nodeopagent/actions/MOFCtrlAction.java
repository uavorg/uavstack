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

package com.creditease.agent.feature.nodeopagent.actions;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.spi.AbstractBaseAction;
import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.uav.httpasync.HttpAsyncClient;
import com.creditease.uav.httpasync.HttpAsyncClientFactory;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;

/**
 * 
 * MOFCtrlAction description: 支持UAVMOF的能力变更
 *
 */
public class MOFCtrlAction extends AbstractBaseAction {

    private HttpAsyncClient client;

    public MOFCtrlAction(String cName, String feature, IActionEngine engine) {
        super(cName, feature, engine);
        client = HttpAsyncClientFactory.build(2, 30, 2000, 2000, 2000);
    }

    @Override
    public void doAction(ActionContext context) throws Exception {

        UAVHttpMessage data = (UAVHttpMessage) context.getParam("msg");

        /**
         * Step 1: start the Supporter (AutoCheck if it is started)
         */
        String action = data.getRequest("action");

        String server = data.getRequest("server") + "/com.creditease.uav/server?action=" + action;

        String rootPath = data.getRequest("root");
        if (!StringHelper.isEmpty(rootPath)) {
            server += "&tag=" + rootPath;
        }

        final String url = server;

        String param = data.getRequest("actparam");

        final AtomicBoolean isSuccess = new AtomicBoolean(false);

        final CountDownLatch cdl = new CountDownLatch(1);

        final StringBuilder response = new StringBuilder();

        client.doAsyncHttpPost(url, param, new HttpClientCallback() {

            @Override
            public void completed(HttpClientCallbackResult result) {

                String res = result.getReplyDataAsString();

                if (result.getRetCode() >= 400) {
                    log.err(this,
                            "MOFCtrlAction FAIL: retcode=" + result.getRetCode() + ", url=" + url + ", err=" + res);
                    if (result.getRetCode() >= 500) {
                        response.append("请求" + url + "完成时的状态码为【" + result.getRetCode() + "】, 服务器遇到错误而不能完成该请求.");
                    }
                    else {
                        response.append("请求" + url + "完成时的状态码为【" + result.getRetCode() + "】, 请求客户端错误.");
                    }

                    isSuccess.set(false);
                }
                else {
                    response.append(res);
                    log.info(this,
                            "MOFCtrlAction Success: retcode=" + result.getRetCode() + ", url=" + url + ", res=" + res);
                    if (!StringHelper.isEmpty(res)) {
                        isSuccess.set(true);
                    }
                }

                cdl.countDown();
            }

            @Override
            public void failed(HttpClientCallbackResult result) {

                String exp = result.getException().getMessage();
                response.append(exp);

                log.err(this, "MOFCtrlAction FAIL: url=" + url + ", err=" + exp);

                isSuccess.set(false);
                cdl.countDown();
            }
        });

        cdl.await(5000, TimeUnit.MILLISECONDS);

        if (isSuccess.get() == false) {
            data.putResponse("rs", "ERR");
            data.putResponse("msg", response.toString());
            return;
        }

        /**
         * Step 2: notify Collect Client for capture files
         */
        if (data.getRequest().containsKey("collectact")) {
            doCollectFiles(data, response, rootPath);
            return;
        }

        data.putResponse("rs", "OK");
        data.putResponse("msg", response.toString());
    }

    /**
     * Do Action which require Collect Client
     * 
     * @param data
     * @param response
     * @param collectAct
     */
    @SuppressWarnings("unchecked")
    private void doCollectFiles(UAVHttpMessage data, final StringBuilder response, String rootPath) {

        AgentFeatureComponent afc = (AgentFeatureComponent) this.getConfigManager().getComponent("collectclient",
                "CollectDataAgent");

        if (afc == null) {
            data.putResponse("rs", "ERR");
            data.putResponse("msg", "归集客户端未启动");
            return;
        }

        String collectAct = data.getRequest("collectact");

        Map<String, String> respObj = JSONHelper.toObject(response.toString(), Map.class);

        File rootFile = new File(respObj.get(rootPath));

        String filePattern = rootFile.getAbsolutePath().replace("\\", "/") + "/" + data.getRequest("file") + ".0.log";
        String appUUID = data.getRequest("appuuid");
        String mqTopic = data.getRequest("mq");

        String collectTasks = "{\"tasks\": [ {\"target\": \"" + appUUID + "\", \"action\": \"" + mqTopic
                + "\", \"file\": \"" + filePattern + "\", \"topic\":\"" + mqTopic + "\"} ]}";

        afc.exchange(collectAct, collectTasks);

        data.putResponse("rs", "OK");
        data.putResponse("msg", respObj.get("rs"));
    }

    @Override
    public String getSuccessNextActionId() {

        return null;
    }

    @Override
    public String getFailureNextActionId() {

        return null;
    }

    @Override
    public String getExceptionNextActionId() {

        return null;
    }

}
