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

package com.creditease.agent.feature.notifycenter.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.creditease.agent.feature.notifycenter.NCConstant;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.uav.httpasync.HttpAsyncClient;
import com.creditease.uav.httpasync.HttpAsyncClientFactory;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;

public class ThreadAnalysisAction extends BaseNotifyAction {

    private HttpAsyncClient client = null;

    public ThreadAnalysisAction(String cName, String feature, IActionEngine engine) {
        super(cName, feature, engine);
        client = HttpAsyncClientFactory.build(10, 10, 30000, 30000, 30000);

    }

    @Override
    public boolean run(final NotificationEvent event) {

        // 目前能够获取到线程分析地址的报警事件有：服务端：[服务状态指标系,应用状态指标系,应用服务器状态指标系],客户端:[调用状态指标系]
        // 根据event.title获取需要线程分析的server地址
        final String server = getTargetServer(event.getTitle());
        if (StringHelper.isEmpty(server)) {
            if (log.isTraceEnable()) {
                log.warn(this, "ThreadAnalysis FAILED because the event(" + event.getTitle()
                        + ") do not support ThreadAnalysis!");
            }
            return false;
        }

        // 获取发送数据
        String data = buildRequestData(event, server);

        // 拼接线程分析命令发送地址url
        final String url = "http://" + event.getIP() + ":10101/node/ctrl";

        // 发送请求
        client.doAsyncHttpPost(url, data, new HttpClientCallback() {

            @Override
            public void completed(HttpClientCallbackResult result) {

                String results = result.getReplyDataAsString();

                @SuppressWarnings("unchecked")
                Map<String, String> map = JSONHelper.toObject(results, Map.class);
                String rs = map.get("rs");

                if ("OK".equals(rs)) {
                    log.info(this, "ThreadAnalysis cmd send[" + url + "] SUCCEESS, [" + server
                            + "] ThreadAnalysis result:" + results);
                    sendMail(event);
                }
                else {
                    if (log.isTraceEnable()) {
                        log.warn(this, "ThreadAnalysis cmd send[" + url + "] SUCCEESS, [" + server
                                + "] ThreadAnalysis result:" + results);
                    }
                }
            }

            @Override
            public void failed(HttpClientCallbackResult result) {

                String results = result.getReplyDataAsString();
                log.err(this, "ThreadAnalysis cmd send[" + url + "] FAILED! result:" + results, result.getException());
            }

        });
        return true;
    }

    private String getTargetServer(String title) {

        // 192.168.56.2[server@appResp@http://192.168.56.2:8080/---]触发2个报警(条件序号：2 5)
        if (title.indexOf('@') < 0) {
            return null;
        }
        String[] info = title.split("@");
        if (info.length < 3) {
            return null;
        }
        Pattern p = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+:\\d+)");
        Matcher matcher = p.matcher(info[2]);
        if (matcher.find()) {
            return "http://" + matcher.group(0);
        }
        return null;
    }

    private String buildRequestData(NotificationEvent event, String server) {

        /*
         * 进行线程分析的data格式 { "intent": "threadanalysis", "request": { "server":"http://192.168.56.2:8080", "user": "报警触发",
         * "actparam": "{ \"supporter\":\"com.creditease.uav.apm.supporters.ThreadAnalysisSupporter\", \
         * "method\":\"captureJavaThreadAnalysis\", \"param\":[\"\",\"1506586160750\",\"192.168.56.2\"] }" } }
         */
        // 拼接actparam
        StringBuilder actParam = new StringBuilder();
        actParam.append("{\"supporter\":\"com.creditease.uav.apm.supporters.ThreadAnalysisSupporter\",")
                .append("\"method\":\"captureJavaThreadAnalysis\",").append("\"param\":[\"\",\"")
                .append(event.getTime()).append("\",\"").append(event.getIP()).append("\"]}");

        // 拼接request
        Map<String, String> request = new HashMap<>();
        request.put("server", server);
        request.put("user", "报警触发");
        request.put("actparam", actParam.toString());

        // 拼接最终发送的data
        StringBuilder data = new StringBuilder();
        data.append("{\"intent\":\"threadanalysis\",").append("\"request\":").append(JSONHelper.toString(request))
                .append("}");
        return data.toString();
    }

    private boolean sendMail(NotificationEvent notifyEvent) {

        String address = getMailAddress(notifyEvent);
        // 如果没有获取到地址，则返回不发送通知邮件
        if (StringHelper.isEmpty(address)) {
            if (log.isTraceEnable()) {
                log.warn(this, "Threadanalysis notify mail Send FAIL as email addresses is EMPTY or INVALID!");
            }
            return false;
        }
        ActionContext ac = new ActionContext();
        ac.putParam("event", notifyEvent);
        ac.putParam(NCConstant.ACTIONVALUE, address);
        ac.putParam("mailTemplatePath", "config/JTAmail.template");
        ac.putParam("mailTitle", "【UAV预警】线程分析执行通知");
        ac = engine.execute(NCConstant.ACTION4MAIL, ac);
        return ac.isSucessful();
    }

    private String getMailAddress(NotificationEvent notifyEvent) {

        // 首先从用户配置的地址中获取地址
        String address = notifyEvent.getArg(cName);
        // 如果为空，则尝试从actionmail中获取地址
        if (StringHelper.isEmpty(address) || !validateMailAddress(address)) {
            address = notifyEvent.getArg(NCConstant.ACTION4MAIL);
        }
        return address;
    }

    /*
     * 校验邮箱地址是否合法，可存在多个邮箱，通过‘,’分隔，有一个邮箱不合格，则校验失败
     */
    private boolean validateMailAddress(String address) {

        Pattern p = Pattern.compile("^[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");
        Matcher matcher = null;
        String[] addr = address.split(",");
        for (int i = 0; i < addr.length; i++) {
            matcher = p.matcher(addr[i].trim());
            if (!matcher.matches()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void destroy() {

        if (client != null) {
            client.shutdown();
        }
    }

}
