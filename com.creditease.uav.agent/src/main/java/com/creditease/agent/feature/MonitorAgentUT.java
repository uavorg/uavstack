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

package com.creditease.agent.feature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.creditease.agent.feature.monitoragent.ut.NotificaitonScheduleWorker;
import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.ThreadHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.agent.spi.AbstractSystemInvoker;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.agent.spi.ISystemInvokerMgr.InvokerType;
import com.creditease.uav.httpasync.HttpAsyncClient;

/**
 * NC@test
 */
public class MonitorAgentUT extends AgentFeatureComponent {

    private final Map<String, ISystemLogger> logMap = new HashMap<>();
    private final List<Map<String, Object>> list = new ArrayList<>();
    private final String curPath = IOHelper.getCurrentPath() + "/logs";
    public int interval = 0;

    public MonitorAgentUT(String cName, String feature) {
        super(cName, feature);

        // LogTest usages
        initList();
        initLineNumWriter();
    }

    public void initList() {

        for (int i = 0; i < 10; i++) {

            Map<String, Object> map = new HashMap<>();

            list.add(map);

        }

    }

    public void initLineNumWriter() {

        LineNumberWriter lineWriter = new LineNumberWriter(curPath, list);

        ScheduledExecutorService scheduleWorker = Executors.newSingleThreadScheduledExecutor();

        scheduleWorker.scheduleAtFixedRate(lineWriter, 2000, 3000, TimeUnit.MILLISECONDS);

    }

    @Override
    public void start() {

        if (log.isTraceEnable()) {
            log.info(this, " NotificaitonScheduleWorker started");
        }

        // testSystemInvoker();

    }

    private void testSystemInvoker() {

        Thread thd = new Thread(new Runnable() {

            @Override
            public void run() {

                ThreadHelper.suspend(5000);

                // AgentFeatureComponent afc = (AgentFeatureComponent) getConfigManager().getComponent("hbclientagent",
                // "HeartBeatClientAgent");
                //
                // if (afc == null) {
                // return;
                // }
                //
                // String[] urls = (String[]) afc.exchange("hbclientagent.service.discovery",
                // "healthmanager-HealthMangerServerWorker-/hm/cache/q");
                //
                // log.info(this, ">>" + urls);

                AbstractSystemInvoker invoker = getSystemInvokerMgr().getSystemInvoker(InvokerType.HTTP);

                for (int i = 0; i < 5; i++) {
                    UAVHttpMessage msg = new UAVHttpMessage();

                    msg.setIntent("services");

                    msg.putRequest("service", "hbserveragent-HeartBeatQueryListenWorker-/hb/query");

                    String res = (String) invoker.invoke("hbserveragent-HeartBeatQueryListenWorker-/hb/query", msg,
                            String.class);

                    log.info(this, ">>" + res);

                    // invoker.invoke("hbserveragent-HeartBeatQueryListenWorker-/hb/query", msg, new
                    // HttpClientCallback() {
                    //
                    // @Override
                    // public void completed(HttpClientCallbackResult result) {
                    //
                    // log.info(this, ">>" + result.getReplyDataAsString());
                    // }
                    //
                    // @Override
                    // public void failed(HttpClientCallbackResult result) {
                    //
                    // }
                    //
                    // });

                }

                log.info(this, ">>>>");
            }
        });

        thd.start();
    }

    public void startHttpClient() {

        HttpAsyncClient.build(1000, 1000, 100000, 100000, 100000);
        interval = Integer.parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "interval"));
        // start NotificationScheduleWorker
        NotificaitonScheduleWorker nsw = new NotificaitonScheduleWorker("NotificaitonScheduleWorker", this.feature);
        this.getTimerWorkManager().scheduleWork("NotificaitonScheduleWorker", nsw, 0, interval);
    }

    @Override
    public void stop() {

        if (log.isTraceEnable()) {
            log.info(this, "NotificationTestAgent  stopped");
        }

        this.getTimerWorkManager().cancel("NotificaitonScheduleWorker");
    }

    @Override
    public Object exchange(String eventKey, Object... data) {

        if (eventKey.equals("logTest")) {
            writeLogToFile(data);
        }

        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })

    public void writeLogToFile(Object... data) {

        List<MonitorDataFrame> mdflist = (List<MonitorDataFrame>) data[0];

        for (MonitorDataFrame mdf : mdflist) {

            List<Map> instances = mdf.getElemInstances("com.creditease.uav.logProducer", "log");

            for (Map instance : instances) {

                String logFileName = (String) instance.get("id");

                int mapIndex = obtainMapIndex(logFileName);

                Map<String, Object> map = list.get(mapIndex);

                if (map.isEmpty()) {

                    map.put("fileName", logFileName);
                    map.put("lineNum", 0);
                }

                File file = new File(logFileName);

                ISystemLogger logger = null;

                if (!logMap.containsKey(logFileName)) {
                    logger = SystemLogger.getTestlogger(file.getName() + "-logger", file.getName() + ".%g.%u.log", null,
                            true, 100 * 1024 * 1024, 1);
                    logMap.put(logFileName, logger);
                }

                logger = logMap.get(logFileName);

                Map valuesMap = (Map) instance.get("values");

                String values = JSON.toJSONString(valuesMap);

                JSONObject contentObeject = JSONObject.parseObject(values);

                JSONArray array = contentObeject.getJSONArray("content");

                int logLineNumber = 0;

                for (int i = 0; i < array.size(); i++) {

                    JSONObject contentObejct = array.getJSONObject(i);

                    String content = contentObejct.getString("content");

                    logger.trace(this, content, true, data);

                    logLineNumber++;

                }

                map.put("lineNum", (int) map.get("lineNum") + logLineNumber);
            }
        }

    }

    public int obtainMapIndex(String logFileName) {

        int beginIndex = logFileName.lastIndexOf("log") + 3;
        int endIndex = logFileName.indexOf(".txt");
        String logIndex = logFileName.substring(beginIndex, endIndex);
        return Integer.parseInt(logIndex) - 1;
    }
}

class LineNumberWriter implements Runnable {

    private String curPath;
    private List<Map<String, Object>> list;

    public LineNumberWriter(String curPath, List<Map<String, Object>> list) {
        this.curPath = curPath;
        this.list = list;
    }

    @Override
    public void run() {

        String filePath = curPath + "/logLineNumR.txt";
        FileWriter writer = null;
        try {
            writer = new FileWriter(new File(filePath));

            String lineNum = JSONArray.toJSONString(list);
            writer.write(lineNum);
            writer.flush();
        }

        catch (IOException e) {

        }
        finally {
            try {
                writer.close();
            }
            catch (IOException e) {
            }
        }
    }
}
