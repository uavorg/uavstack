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

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.spi.AbstractBaseAction;
import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.uav.httpasync.HttpAsyncClient;
import com.creditease.uav.httpasync.HttpAsyncClientFactory;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;

public class DumpThreadAction extends AbstractBaseAction {

    private HttpAsyncClient httpClient;

    public DumpThreadAction(String cName, String feature, IActionEngine engine) {
        super(cName, feature, engine);

        httpClient = HttpAsyncClientFactory.build(2, 50, 10000, 10000, 10000);
    }

    @Override
    public void doAction(ActionContext context) throws Exception {

        String url = (String) context.getParam("url");
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) context.getParam("paramMap");
        @SuppressWarnings("unchecked")
        List<Object> paramsList = (List<Object>) paramMap.get("param");
        paramsList.set(1, System.currentTimeMillis() + "");

        if(log.isDebugEnable()) {
            log.debug(this, "DumpThreadAction.doAction context=" + JSONHelper.toString(context));
        }
        
        String fileName = invokeJTASupporter(url, JSONHelper.toString(paramMap));

        if (fileName.startsWith("ERR:")) {
            context.putParam("msg", fileName);
            context.setSucessful(false);
            return;
        }

        fileName = collectFile(fileName, (String) context.getParam("user"), paramsList);
        context.putParam("msg", fileName);
        
        context.setSucessful(true);
    }

    private String invokeJTASupporter(final String url, final String content) {

        long start = System.currentTimeMillis();
        final CountDownLatch cdl = new CountDownLatch(1);
        final StringBuffer sb = new StringBuffer();

        httpClient.doAsyncHttpPost(url, content, new HttpClientCallback() {

            @Override
            public void completed(HttpClientCallbackResult result) {

                String res = result.getReplyDataAsString();
                if (!StringHelper.isEmpty(res) && !res.contains("ERR") && !res.contains("Err")) {
                    sb.append(res);
                }
                else {
                    log.err(this, "MOFCtrlAction process FAILED. url=" + url + ", req=" + content + ", resp=" + res);
                    sb.append("ERR:MOF PROCESS FAILED:" + res);
                }

                cdl.countDown();
            }

            @Override
            public void failed(HttpClientCallbackResult result) {

                Exception e = result.getException();
                sb.append("ERR:INVOKE MOF FAILED:" + e.getMessage());

                log.err(this, "invoke MOFCtrlAction FAILED. url=" + url + ", retcode=" + result.getRetCode() + ", resp="
                        + result.getReplyDataAsString(), e);

                cdl.countDown();
            }
        });

        try {
            cdl.await(10000, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e) {
            sb.append("ERR:INVOKE MOF TIMEOUT");
        }

        if (log.isDebugEnable()) {
            log.debug(this, "invoke MOFCtrlAction url=" + url + ", req=" + content + ", resp=" + sb.toString()
                    + ", cost=" + (System.currentTimeMillis() - start));
        }
        return sb.toString();
    }

    private String collectFile(String fileName, String user, List<Object> paramsList) {

        String ipport = getIPPort(fileName);
        String pid = "unknown";
        String time = System.currentTimeMillis() + "";
        if (paramsList.size() > 2) {
            pid = paramsList.get(0).toString();
            time = paramsList.get(1).toString();
        }
        String target = ipport + "_" + pid + "_" + time + "_" + user;

        AgentFeatureComponent afc = (AgentFeatureComponent) this.getConfigManager().getComponent("collectclient",
                "CollectDataAgent");

        if (afc == null) {
            return "ERR:归集客户端未启动";
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

        return fileName;
    }

    private String getIPPort(String fileName) {

        String file = StringHelper.getFilename(fileName);
        String[] args = file.split("_");
        if (args.length > 2) {
            return args[0] + ":" + args[1];
        }
        return "127.0.0.1:8080";
    }

    @Override
    public String getSuccessNextActionId() {

        return "CountCtrlAction";
    }

    @Override
    public String getFailureNextActionId() {

        return null;
    }

    @Override
    public String getExceptionNextActionId() {

        return null;
    }

    @Override
    public void destroy() {

        httpClient.shutdown();
    }

}
