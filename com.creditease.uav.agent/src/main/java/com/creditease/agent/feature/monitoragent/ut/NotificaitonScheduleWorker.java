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

package com.creditease.agent.feature.monitoragent.ut;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.AbstractTimerWork;
import com.creditease.uav.httpasync.HttpAsyncClient;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;

/**
 * NC@test
 */

public class NotificaitonScheduleWorker extends AbstractTimerWork implements UnitTestInterface {

    public static final String testcasebase = "../../utdata/notify/notificationTestCase";

    public NotificaitonScheduleWorker(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public void run() {

        executeTestCase();

        /**
         * Http update testcase
         **/
        // executeNCHttpTestcase();
    }

    @Override
    public String prepareTestData(String filepath) {

        return IOHelper.readTxtFile(filepath, "utf-8");

    }

    @Override
    public void executeTestCase() {

        for (int i = 1; i < 10; i++) {
            executencTestcase(testcasebase + i);
        }

    }

    public void executencTestcase(String testcaseNumber) {

        log.info(this, "executeNCTestcase:" + testcaseNumber);

        String jsonString = prepareTestData(testcaseNumber);

        NotificationEvent event = new NotificationEvent(jsonString);

        log.info(this, "jsonStr:" + event.toJSONString());

        this.putNotificationEvent(event);
    }

    public void executeNCHttpTestcase() {

        String url = "http://localhost:8766/nc/update";

        UAVHttpMessage request = new UAVHttpMessage();

        String nceventKey = "127.0.0.1@notificationEvent@NotificaitonScheduleWorker@notifytestagent@-1172353146";
        request.putRequest("ncevent", nceventKey);
        String content = JSONHelper.toString(request);
        executeHttpCommand(url, content);
    }

    public void executeHttpCommand(String url, String content) {

        byte[] datab = null;
        try {
            datab = content.getBytes("utf-8");
        }
        catch (UnsupportedEncodingException e) {
            return;
        }

        HttpAsyncClient.instance().doAsyncHttpPost(url, datab, "application/json", "utf-8", new HttpClientCallback() {

            @Override
            public void completed(HttpClientCallbackResult result) {

                String results = result.getReplyDataAsString();
                log.info(this, "getReplyDataAsString. \n" + results);

                log.info(this, "getReplyData. \n" + result.getReplyData());

            }

            @Override
            public void failed(HttpClientCallbackResult result) {

                log.info(this, "failed. \n");
            }

        });
    }

    public static int getRandom(int max) {

        return (int) (Math.random() * max);
    }

    public String getData(String filepath) {

        StringBuffer buffer = new StringBuffer();

        log.info(this, "current local:::::" + System.getProperty("user.dir"));
        try {

            File file = new File(filepath);
            BufferedReader reader = null;
            reader = new BufferedReader(new FileReader(file));

            String tempString = null;
            while ((tempString = reader.readLine()) != null) {

                buffer.append(tempString + "\n");
            }
            reader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        String rawData = buffer.toString();

        return rawData;
    }
}
