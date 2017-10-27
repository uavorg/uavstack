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

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.creditease.agent.feature.notifycenter.NCConstant;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.uav.helpers.url.BASE64EncoderUrl;

public abstract class AbstractMailAction extends BaseNotifyAction {

    private static List<String> nameList = new LinkedList<String>();

    static {
        nameList.add("ip");
        nameList.add("host");
        nameList.add("component");
        nameList.add("nodename");
        nameList.add("feature");
        nameList.add("nodeuuid");
    }

    private String msgurl = "http://127.0.0.1:8080/apphub/uavapp_godeye/notifymgr/desc.html";
    private String uavurl = "http://127.0.0.1:8080/apphub/main.html";

    public AbstractMailAction(String cName, String feature, IActionEngine engine) {
        super(cName, feature, engine);

        msgurl = this.getConfigManager().getFeatureConfiguration(this.feature, "nc.notify.mail.msgurl");
        uavurl = this.getConfigManager().getFeatureConfiguration(this.feature, "nc.notify.mail.uavurl");
    }

    @Override
    public boolean run(NotificationEvent event, ActionContext context) {

        // 默认邮件模板地址和邮件主题
        String mailTemplatePath = "config/mail.template";
        String mailTitle = "【UAV预警】" + event.getTitle();

        // 尝试从actionContext中获取邮件模板地址和邮件主题，如果没有则使用默认值
        if (!StringHelper.isEmpty((String) context.getParam("mailTemplatePath"))) {
            mailTemplatePath = (String) context.getParam("mailTemplatePath");
        }

        if (!StringHelper.isEmpty((String) context.getParam("mailTitle"))) {
            mailTitle = (String) context.getParam("mailTitle");
        }

        boolean Result = sendMail(mailTitle, mailTemplatePath, event);
        return Result;
    }

    @Override
    public boolean run(NotificationEvent event) {

        return false;
    }

    abstract boolean sendMail(String title, String mailTemplatePath, NotificationEvent notifyEvent);

    /**
     * 属性替换
     * 
     * @param html
     * @param notifyEvent
     * @return String
     */
    public String buildMailBody(String html, NotificationEvent notifyEvent) {

        String id = notifyEvent.getId();
        String title = notifyEvent.getTitle();

        long timeFlag = notifyEvent.getTime();
        String description = notifyEvent.getDescription();
        String host = notifyEvent.getHost();
        String ip = notifyEvent.getIP();

        html = html.replace("#ip#", ip);
        html = html.replace("#host#", host);
        html = html.replace("#title#", title);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        html = html.replace("#timeFlag#", sdf.format(timeFlag));
        html = html.replace("#id#", id);
        html = html.replace("#description#", description);

        String feature = notifyEvent.getArg("feature");
        String component = notifyEvent.getArg("component");
        String nodeuuid = notifyEvent.getArg("nodeuuid");
        String nodename = notifyEvent.getArg("nodename");

        if (feature != null) {
            html = html.replace("#feature#", feature);
        }
        if (component != null) {
            html = html.replace("#component#", component);
        }
        if (nodeuuid != null) {
            html = html.replace("#nodeuuid#", nodeuuid);
        }
        if (nodename != null) {
            html = html.replace("#nodename#", nodename);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> ntfargs = JSONHelper.toObject(notifyEvent.getArg(NCConstant.NTFVALUE), Map.class);

        Map<String, String> args = notifyEvent.getArgs(true);
        String keys = notifyEvent.getArg(NCConstant.NTFKEY);
        keys += "&" + ntfargs.get(NCConstant.COLUMN_STARTTIME);
        StringBuffer argstr = new StringBuffer();
        StringBuffer suffixNTF = new StringBuffer();
        StringBuffer suffixAction = new StringBuffer();
        for (String key : args.keySet()) {
            if (nameList.contains(key)) {
                continue;
            }
            else if (key.startsWith("NTF")) {
                suffixNTF.append("<li><span>");
                suffixNTF.append(key);
                suffixNTF.append("</span><span>：</span><span>");
                suffixNTF.append(args.get(key));
                suffixNTF.append("</span></li><hr>");
            }
            else if (key.startsWith("action_")) {
                suffixAction.append("<li><span>");
                suffixAction.append(key);
                suffixAction.append("</span><span>：</span><span>");
                suffixAction.append(args.get(key));
                suffixAction.append("</span></li><hr>");
            }
            else {
                argstr.append("<li><span>");
                argstr.append(key);
                argstr.append("</span><span>：</span><span>");
                argstr.append(args.get(key));
                argstr.append("</span></li><hr>");
            }
        }
        argstr.append(suffixNTF);
        argstr.append(suffixAction);
        try {
            keys = new BASE64EncoderUrl().encode(keys.getBytes("utf-8"));
        }
        catch (Exception e) {
            log.err(this, "Build Mail Body Error:", e);
        }

        html = html.replace("#key#", keys);
        html = html.replace("#args#", argstr.toString());
        html = html.replace("#msgurl#", msgurl);
        html = html.replace("#uavurl#", uavurl);
        return html;
    }
}
