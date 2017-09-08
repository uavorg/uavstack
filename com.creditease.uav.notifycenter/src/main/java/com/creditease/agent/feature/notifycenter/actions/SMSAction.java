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

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.uav.httpasync.HttpAsyncClient;
import com.creditease.uav.httpasync.HttpAsyncClientFactory;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;
import com.creditease.uav.httpasync.api.ParaValuePair;

/**
 * 
 * @author pengfei
 * @since 20160520
 *
 */
public class SMSAction extends BaseNotifyAction {

    private HttpAsyncClient client = null;

    // default value
    private String baseUrl = "http://127.0.0.1:8902/smsgateway/services/MessageService3.0/sms/";

    public SMSAction(String cName, String feature, IActionEngine engine) {

        super(cName, feature, engine);

        client = HttpAsyncClientFactory.build(10, 10, 30000, 30000, 30000);

        baseUrl = this.getConfigManager().getFeatureConfiguration(this.feature, "nc.notify.sms.baseurl");
    }

    @Override
    public boolean run(NotificationEvent notifyEvent) {

        // 模板关键字
        Map<String, String> keywords = new HashMap<String, String>();

        String title = notifyEvent.getTitle();
        long timeFlag = notifyEvent.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sd = sdf.format(new Date(timeFlag));

        keywords.put("custName", sd);
        keywords.put("comName", title);

        /**
         * 若notifyEvent中包含目的地址，则用该地址；否则使用默认地址
         */
        String sms = notifyEvent.getArg(cName);

        if (StringHelper.isEmpty(sms)) {
            if (log.isTraceEnable()) {
                log.warn(this, "Send SMS FAIL as no any phone numbers.");
            }
            return false;
        }

        String[] phoneNums = sms.split(",");

        for (String phone : phoneNums) {

            List<ParaValuePair> nvps = new ArrayList<ParaValuePair>();
            // 接口版本
            nvps.add(new ParaValuePair("version", "3.0"));
            // 批次号
            nvps.add(new ParaValuePair("batchId", Long.toString(System.currentTimeMillis())));
            // 组织机构号
            nvps.add(new ParaValuePair("orgNo", "2265"));
            // 模板号
            nvps.add(new ParaValuePair("typeNo", "7209"));
            // 关键字替换
            nvps.add(new ParaValuePair("keywords", JSON.toJSONString(keywords)));
            // 手机号
            nvps.add(new ParaValuePair("mobile", phone));

            if (log.isDebugEnable()) {
                log.debug(this, "Send SMS START: phone=" + phone);
            }

            final String phoneNum = phone;

            client.doAsyncHttpPost(baseUrl + "send", nvps, "utf-8", new HttpClientCallback() {

                @Override
                public void completed(HttpClientCallbackResult result) {

                    String results = result.getReplyDataAsString();

                    JSONObject jo = JSON.parseObject(results);
                    // 错误码
                    String code = jo.getString("code");
                    // 错误信息
                    String desc = jo.getString("desc");

                    if (log.isDebugEnable()) {
                        log.debug(this, "Send SMS END: phone=" + phoneNum + ",code=" + code + ",desc=" + desc);
                    }
                }

                @Override
                public void failed(HttpClientCallbackResult result) {

                    String results = result.getReplyDataAsString();

                    log.err(this, "Send SMS FAIL: phone=" + phoneNum + ", result=" + results);
                }

            });
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
