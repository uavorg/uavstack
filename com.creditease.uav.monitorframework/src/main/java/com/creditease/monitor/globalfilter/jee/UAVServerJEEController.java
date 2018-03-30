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

package com.creditease.monitor.globalfilter.jee;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.NetworkHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.monitor.UAVMetaDataMgr;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.UAVServer.ServerVendor;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.datastore.DataObserver;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.uav.profiling.spi.Profile;

/**
 * 
 * UAVServeJEEController description: JEE Application Server internal app helps to provide the control of MOF
 *
 */
public class UAVServerJEEController extends AbsJEEGlobalFilterHandler {

    public UAVServerJEEController(String id) {
        super(id);
    }

    @Override
    public String getContext() {

        return "com.creditease.uav/server";
    }

    /**
     * controlSupporters
     * 
     * @param request
     * @param response
     * @param isStart
     */
    private void controlSupporters(HttpServletRequest request, HttpServletResponse response, boolean isStart) {

        String supportersStr = this.getRequestBodyAsString(request, "utf-8");

        try {
            List<String> supporters = JSONHelper.toObjectArray(supportersStr, String.class);

            String[] supporterClasses = new String[supporters.size()];

            supporterClasses = supporters.toArray(supporterClasses);

            String retMsg = "OK";
            if (isStart == true) {
                Map<String, Object> supportersStatus = UAVServer.instance().startSupporters(supporterClasses, true);
                if (supportersStatus.size() > 0) {
                    retMsg = JSONHelper.toString(supportersStatus);
                }

                String tag = request.getParameter("tag");
                if (tag != null) {
                    String val = System.getProperty(tag, "");
                    retMsg = "{\"rs\":\"" + retMsg.replace("\"", "\\\"") + "\",\"" + tag + "\":\"" + val + "\"}";
                }
            }
            else {
                UAVServer.instance().stopSupporters(supporterClasses, true);
            }
            this.writeResponseBody(response, retMsg, HttpServletResponse.SC_OK);
        }
        catch (Exception e) {
            this.writeResponseBody(response, "Err", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doRequest(HttpServletRequest request, HttpServletResponse response, InterceptContext ic) {

        String action = request.getParameter("action");

        /**
         * ping
         */
        if (action.equalsIgnoreCase("ping")) {

            ServerVendor sv = (UAVServer.ServerVendor) UAVServer.instance()
                    .getServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR);

            this.writeResponseBody(response, sv.toString(), HttpServletResponse.SC_OK);
        }
        /**
         * setSystemPro: set SystemProperties
         */
        if ("setSystemPro".equalsIgnoreCase(action)) {
            String jsonStr = this.getRequestBodyAsString(request, "utf-8");

            try {
                @SuppressWarnings("unchecked")
                Map<String, String> params = JSONHelper.toObject(jsonStr, Map.class);

                Map<String, Object> metaMap = new HashMap<String, Object>();

                for (String key : params.keySet()) {

                    System.setProperty(key, params.get(key));

                    if (UAVMetaDataMgr.SystemMeta.contains(key)) {
                        metaMap.put(key, params.get(key));
                    }
                }
                // flush systemMeta to metaData
                UAVServer.instance().getMetaMgr().addMetaData(metaMap);

                this.writeResponseBody(response, "OK", HttpServletResponse.SC_OK);
            }
            catch (Exception e) {
                this.writeResponseBody(response, "Err", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
        /**
         * getSystemPro
         */
        else if (action.equalsIgnoreCase("getSystemPro")) {

            this.writeResponseBody(response, JSONHelper.toString(System.getProperties()), HttpServletResponse.SC_OK);
        }
        /**
         * startSupporter: dynamic start supporters
         */
        else if ("startSupporter".equalsIgnoreCase(action)) {
            controlSupporters(request, response, true);
        }
        /**
         * stopSupporter
         */
        else if ("stopSupporter".equalsIgnoreCase(action)) {
            controlSupporters(request, response, false);
        }
        /**
         * runSupporter
         */
        else if ("runSupporter".equalsIgnoreCase(action)) {
            runSupporter(request, response);
        }
        /**
         * dump profile data
         */
        else if ("dumpProfile".equalsIgnoreCase(action)) {

            String targetPath = request.getParameter("path");

            if (StringHelper.isEmpty(targetPath)) {
                targetPath = (String) UAVServer.instance().getServerInfo(CaptureConstants.INFO_MOF_METAPATH);
            }

            String dumpFile = targetPath + "/p_" + NetworkHelper.getLocalIP() + "_"
                    + UAVServer.instance().getServerInfo(CaptureConstants.INFO_APPSERVER_LISTEN_PORT) + ".profile";

            Collection<Profile> profiles = DataObserver.instance().getProfiles();

            StringBuilder sb = new StringBuilder("{");

            for (Profile p : profiles) {

                sb.append("\"" + p.getId() + "\":" + p.getRepository().toJSONString() + ",");
            }

            if (sb.length() > 3) {
                sb = sb.deleteCharAt(sb.length() - 1);
            }

            String data = sb.append("}").toString();

            try {
                IOHelper.writeTxtFile(dumpFile, data, "utf-8", false);
            }
            catch (IOException e) {
                this.logger.warn("DUMP Profile Fail. ", e);
            }
        }
    }

    /**
     * runSupporter
     * 
     * {
     * 
     * supporter:"<supporter class>",
     * 
     * method:"<method name>",
     * 
     * param: [<data>,<data>]
     * 
     * }
     * 
     * @param request
     * @param response
     */
    @SuppressWarnings("unchecked")
    private void runSupporter(HttpServletRequest request, HttpServletResponse response) {

        String msgStr = this.getRequestBodyAsString(request, "utf-8");

        Map<String, Object> param = JSONHelper.toObject(msgStr, Map.class);

        String supporterName = (String) param.get("supporter");
        String methodName = (String) param.get("method");
        List<Object> paramsList = (List<Object>) param.get("param");

        if (StringHelper.isEmpty(supporterName) || StringHelper.isEmpty(methodName) || paramsList == null) {
            this.writeResponseBody(response, "Err", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        Object[] params = new Object[paramsList.size()];

        params = paramsList.toArray(params);

        Object res = UAVServer.instance().runSupporter(supporterName, methodName, params);
        if (null == res) {
            res = "Err";
        }
        // 线程分析supporter调用成功，则返回文件名，需要这个值作为collectAgent的输入
        this.writeResponseBody(response, res.toString(), HttpServletResponse.SC_OK);
    }

    @Override
    protected void doResponse(HttpServletRequest request, HttpServletResponse response, InterceptContext ic) {

        // not implement
    }

}
