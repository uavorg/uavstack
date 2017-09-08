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

package com.creditease.uav.threadanalysis.client.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.RuntimeHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.spi.AbstractBaseAction;
import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.agent.spi.IConfigurationManager;
import com.creditease.uav.httpasync.HttpAsyncClient;
import com.creditease.uav.httpasync.HttpAsyncClientFactory;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;

public class ThreadAnalysisAction extends AbstractBaseAction {

    private HttpAsyncClient client;

    private String rootMetaPath;

    public ThreadAnalysisAction(String cName, String feature, IActionEngine engine) {
        super(cName, feature, engine);
        client = HttpAsyncClientFactory.build(2, 50, 10000, 10000, 10000);

        rootMetaPath = this.getConfigManager().getContext(IConfigurationManager.METADATAPATH) + "thread.analysis";
        // 线程分析的文件位置，不存在则创建。只有一个MA，存在多个用户的情况，考虑权限问题，设置这个文件夹对别的用户可读写
        try {
            IOHelper.createFolder(rootMetaPath);
            RuntimeHelper.exec(10000, "/bin/sh", "-c", "chmod 777 " + rootMetaPath);
        }
        catch (Exception e) {
            // ignore
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void doAction(ActionContext context) throws Exception {

        try {
            UAVHttpMessage data = (UAVHttpMessage) context.getParam("msg");
            String action = "runSupporter";
            String server = data.getRequest("server") + "/com.creditease.uav/server?action=" + action;
            final String url = server;

            String param = data.getRequest("actparam");

            String user = data.getRequest("user");
            if (StringHelper.isEmpty(user)) {
                user = "UNKNOWN";
            }

            Map<String, Object> paramMap = JSONHelper.toObject(param, Map.class);
            List<Object> paramsList = (List<Object>) paramMap.get("param");
            paramsList.add(this.rootMetaPath);
            paramMap.put("param", paramsList);

            final AtomicBoolean isSuccess = new AtomicBoolean(false);

            final CountDownLatch cdl = new CountDownLatch(1);

            final StringBuilder response = new StringBuilder();

            client.doAsyncHttpPost(url, JSONHelper.toString(paramMap), new HttpClientCallback() {

                @Override
                public void completed(HttpClientCallbackResult result) {

                    String res = result.getReplyDataAsString();

                    log.info(this, "MOFCtrlAction Success: url=" + url + ", res=" + res);

                    if (!StringHelper.isEmpty(res) && !res.contains("ERR") && !res.contains("Err")) {
                        isSuccess.set(true);
                    }

                    response.append(res);

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

            cdl.await(10000, TimeUnit.MILLISECONDS);

            if (isSuccess.get() == false) {
                data.putResponse("rs", "ERR");
                data.putResponse("msg", response.toString());
                return;
            }
            // 成功则返回文件名
            String fileName = response.toString();
            // 如果传入的参数不正确，则不会生成文件。至此可知参数符合规则。
            String ipport = getIpport(fileName);
            String pid = "unknown";
            String time = System.currentTimeMillis() + "";
            if (paramsList.size() > 2) {
                pid = paramsList.get(0).toString();
                time = paramsList.get(1).toString();
            }
            String target = ipport + "_" + pid + "_" + time + "_" + user;
            doCollectFiles(data, fileName, target);
        }
        catch (Exception e) {
            log.err(this, "do thread analysis FAILED.", e);
            throw e;
        }
    }

    private String getIpport(String fileName) {

        String file = StringHelper.getFilename(fileName);
        String[] args = file.split("_");
        if (args.length > 2) {
            return args[0] + ":" + args[1];
        }
        return "127.0.0.1:8080";

    }

    /**
     * doCollectFiles
     * 
     * @param data
     * @param fileName
     */
    private void doCollectFiles(UAVHttpMessage data, String fileName, String target) {

        AgentFeatureComponent afc = (AgentFeatureComponent) this.getConfigManager().getComponent("collectclient",
                "CollectDataAgent");

        if (afc == null) {
            data.putResponse("rs", "ERR");
            data.putResponse("msg", "归集客户端未启动");
            return;
        }

        String collectAct = "collectdata.add";
        String mqTopic = "JQ_JTA";

        // call CollectDataAgent, prepare parameters
        Map<String, Object> task = new HashMap<String, Object>();
        task.put("target", target);
        task.put("action", mqTopic);
        task.put("file", fileName);
        task.put("topic", mqTopic);
        task.put("unsplit", true);
        List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
        tasks.add(task);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("tasks", tasks);
        Map<String, Object> call = new HashMap<String, Object>();
        call.put("feature", "threadanalysis");
        call.put("component", "ThreadAnalysisAgent");
        call.put("eventKey", "collect.callback");
        params.put("callback", call);

        String collectTasks = JSONHelper.toString(params);
        afc.exchange(collectAct, collectTasks);

        data.putResponse("rs", "OK");
        data.putResponse("msg", fileName);
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
