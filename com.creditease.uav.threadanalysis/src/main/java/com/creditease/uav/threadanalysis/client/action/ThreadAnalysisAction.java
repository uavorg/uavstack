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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.RuntimeHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.spi.AbstractBaseAction;
import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.agent.spi.IConfigurationManager;

public class ThreadAnalysisAction extends AbstractBaseAction {

    private static final String SERVICE_POSTFIX = "/com.creditease.uav/server?action=runSupporter";

    // 执行时间map,key为进程号，value为执行时间；用于判断在限定时间段内不需要发起多次请求
    private static volatile Map<String, Long> timeIntervalMap = new ConcurrentHashMap<String, Long>();

    private String dumpFileDirectory;

    private long timeInterval = 30000L; // 30s

    public ThreadAnalysisAction(String cName, String feature, IActionEngine engine) {
        super(cName, feature, engine);

        // 线程分析的文件位置，不存在则创建。只有一个MA，存在多个用户的情况，考虑权限问题，设置这个文件夹对别的用户可读写
        dumpFileDirectory = getConfigManager().getContext(IConfigurationManager.METADATAPATH) + "thread.analysis";
        try {
            IOHelper.createFolder(dumpFileDirectory);
            RuntimeHelper.exec(10000, "/bin/sh", "-c", "chmod 777 " + dumpFileDirectory);
        }
        catch (Exception ignore) {
            // ignore
        }
    }

    @Override
    public void doAction(ActionContext context) throws Exception {

        try {
            UAVHttpMessage data = (UAVHttpMessage) context.getParam("msg");

            if (!controlConcurrency(data)) {
                data.putResponse("rs", "ERR");
                data.putResponse("msg", "ERR:THREAD DUMP IS RUNNING");
                return;
            }

            String user = data.getRequest("user");
            if (StringHelper.isEmpty(user)) {
                user = "UNKNOWN";
            }

            String url = data.getRequest("server") + SERVICE_POSTFIX;
            if (!url.startsWith("http")) {
                url = "http://" + url;
            }

            String param = data.getRequest("actparam");
            @SuppressWarnings("unchecked")
            Map<String, Object> paramMap = JSONHelper.toObject(param, Map.class);
            @SuppressWarnings("unchecked")
            List<Object> paramsList = (List<Object>) paramMap.get("param");
            paramsList.add(this.dumpFileDirectory);
            paramMap.put("param", paramsList);

            ActionContext ac = new ActionContext();
            ac.putParam("user", user);
            ac.putParam("url", url);
            ac.putParam("paramMap", paramMap);

            if ("true".equals(data.getRequest("multiple"))) {
                ac.putParam("multiple", true);
                int duration = DataConvertHelper.toInt(data.getRequest("duration"), 0);
                int interval = DataConvertHelper.toInt(data.getRequest("interval"), 5);
                int times = duration / interval + 1;
                ac.putParam("times", times);
                ac.putParam("suspendTime", interval * 1000);
            }
            else {
                ac.putParam("multiple", false);
                ac.putParam("times", 1);
                ac.putParam("suspendTime", 0);
            }

            ac = getActionEngineMgr().getActionEngine("JTAActionEngine").execute("DumpThreadAction", ac);

            String ret = (String) ac.getParam("msg");

            if (ret.contains("ERR:")) {
                data.putResponse("rs", "ERR");
                data.putResponse("msg", ret);
            }
            else {
                data.putResponse("rs", "OK");
                data.putResponse("msg", ret);
            }
        }
        catch (Exception e) {
            log.err(this, "do thread analysis FAILED.", e);
            throw e;
        }
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

    /**
     * controlConcurrency
     * 
     * @param pid
     * @param exectime
     * @return
     */
    private boolean controlConcurrency(UAVHttpMessage data) {

        String server = data.getRequest("server");
        long exectime = System.currentTimeMillis();
        long duration = 1000L * DataConvertHelper.toInt(data.getRequest("duration"), 0);
        // initial
        if (!timeIntervalMap.containsKey(server)) {
            synchronized (timeIntervalMap) {
                if (!timeIntervalMap.containsKey(server)) {
                    // 在exectimeMap记录进程号和执行时间
                    timeIntervalMap.put(server, exectime + duration);
                    return true;
                }
            }
        }
        // only one can entrance
        if ((exectime - timeIntervalMap.get(server)) > timeInterval) {
            synchronized (timeIntervalMap) {
                if ((exectime - timeIntervalMap.get(server)) > timeInterval) {
                    // 在exectimeMap记录进程号和执行时间
                    timeIntervalMap.put(server, exectime + duration);
                    return true;
                }
            }
        }
        // thread analysis is running, abandon
        return false;
    }
}
