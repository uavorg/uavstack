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

package com.creditease.uav.apphub.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.uav.exception.ApphubException;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;

/**
 * @author Created by lbay on 2016/5/10.
 */
public class AppHubInit {

    private String manageTypeApp = "app";
    private String manageTypeGroup = "group";
    private String bussinessTypeCreate = "create";
    private String bussinessTypeModify = "modify";
    private String bussinessTypeQuery = "query";

    private static class ManageTestCallback implements HttpClientCallback {

        ISystemLogger logger = SystemLogger.getLogger(ManageTestCallback.class);

        @Override
        public void completed(HttpClientCallbackResult result) {

            logger.info(this, "AppHubInit SUCCESS:" + result.getReplyDataAsString());
        }

        @Override
        public void failed(HttpClientCallbackResult result) {

            logger.info(this, result.getException().toString());
        }
    }

    private String prefixUavApp = "uavapp_";
    private ISystemLogger logger;
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String ctime = df.format(new Date());
    private String operationuser = "system";

    private AppHubBaseRestService service;

    public AppHubInit(AppHubBaseRestService service) {

        this.service = service;
        this.logger = this.service.getLogger();
    }

    public void run() {

        try {
            initCheckApp("1000000001", prefixUavApp + "comptest");
            initCheckApp("1000000002", prefixUavApp + "godeye");
            initCheckApp("1000000003", prefixUavApp + "manage");
            initCheckApp("1538364490", prefixUavApp + "baseclassmgt");
            initCheckApp("2097707360", prefixUavApp + "godcompass");
            initCheckApp("4051451871", prefixUavApp + "betatest");
            initCheckApp("2094373133", prefixUavApp + "godfilter");
            
            initCheckGroup("uav_admin");
            initCheckGroup("vipgroup");
        }
        catch (Exception e) {
            logger.err(this, "AppHubInit RUN FAIL: ", e);
            throw new ApphubException(e);
        }
    }

    private void initCheckApp(final String id, final String appName) {

        logger.info(this, "AppHubInit CHECK APP:uavapp_" + appName);

        Map<String, Object> query = new HashMap<String, Object>();
        Map<String, Object> where = new HashMap<String, Object>();
        where.put("appid", id);
        where.put("state", 1);
        query.put("where", where);
        byte[] queryData = getRequestData(manageTypeApp, createHttpMapRequest(bussinessTypeQuery, query));
        run(manageTypeApp, queryData, new ManageTestCallback() {

            @Override
            public void completed(HttpClientCallbackResult result) {

                if (checkResultIsEmpty(result)) {
                    initApp(id, appName);
                }
                else {
                    logger.info(this, "AppHubInit CHECK APP done :uavapp_" + appName + " EXIST");
                }
            }
        });

    }

    private void initApp(final String id, final String appName) {

        try {
            Map<String, Object> modify = new HashMap<String, Object>();
            Map<String, Object> where = new HashMap<String, Object>();
            where.put("appid", id);
            modify.put("where", where);
            byte[] modifyData = getRequestData(manageTypeApp, createHttpMapRequest(bussinessTypeModify, modify));
            run(manageTypeApp, modifyData, new ManageTestCallback() {

                @Override
                public void completed(HttpClientCallbackResult result) {

                    logger.info(this, "AppHubInit ADD APP:uavapp_" + appName);

                    Map<String, Object> app1 = new HashMap<String, Object>();
                    String appUrlStr = appName;
                    app1.put("appid", id);
                    app1.put("appurl", appUrlStr);
                    app1.put("state", 1);
                    app1.put("createtime", ctime);
                    app1.put("operationtime", ctime);
                    app1.put("operationuser", operationuser);
                    byte[] addData = getRequestData(manageTypeApp, createHttpMapRequest(bussinessTypeCreate, app1));
                    run(manageTypeApp, addData, new ManageTestCallback());
                }
            });
        }
        catch (Exception e) {
            logger.err(this, e.getMessage(), e);
            throw new ApphubException(e);
        }

    }

    private void initCheckGroup(final String groupid) {

        logger.info(this, "AppHubInit CHECK GROUP:" + groupid);

        Map<String, Object> query = new HashMap<String, Object>();
        Map<String, Object> where = new HashMap<String, Object>();
        where.put("groupid", groupid);
        where.put("state", 1);
        query.put("where", where);
        byte[] queryData = getRequestData(manageTypeGroup, createHttpMapRequest(bussinessTypeQuery, query));

        GroupCheckCallBack groupCheckCb = new GroupCheckCallBack();
        groupCheckCb.setGroupid(groupid);
        run(manageTypeGroup, queryData, groupCheckCb);

    }

    /**
     * 
     * @param groupid
     * @param appids
     * @param isAdd
     *            多个用,号隔开
     * @throws Exception
     */
    protected void initGroup(final String groupid, final String appids, final boolean isAdd) {

        try {

            Map<String, Object> modify = new HashMap<String, Object>();
            Map<String, Object> where = new HashMap<String, Object>();
            where.put("groupid", groupid);
            modify.put("where", where);
            byte[] modifyData = getRequestData(manageTypeGroup, createHttpMapRequest(bussinessTypeModify, modify));

            GroupCallBack groupGb = new GroupCallBack();
            groupGb.setAdd(isAdd);
            groupGb.setAppids(appids);
            groupGb.setGroupid(groupid);

            run(manageTypeGroup, modifyData, groupGb);
        }
        catch (Exception e) {
            logger.err(this, e.getMessage(), e);
            throw new ApphubException(e);
        }

    }

    /**
     * 回调begin
     */

    public class GroupCheckCallBack extends ManageTestCallback {

        private String groupid;

        public void setGroupid(String groupid) {

            this.groupid = groupid;
        }

        @Override
        public void completed(HttpClientCallbackResult result) {

            if (checkResultIsEmpty(result)) {
                if ("uav_admin".equals(groupid)) {
                    initGroup(groupid, "1000000001,1000000002,1000000003,1538364490,2097707360,4051451871,2094373133", true);
                }
                else if ("uav_guest".equals(groupid)) {
                    initGroup(groupid, "", false);
                }
                else if ("vipgroup".equals(groupid)) {
                    initGroup(groupid, "1000000001,1000000002,1000000003,1538364490,2097707360,4051451871,2094373133", true);
                }
            }
            else {
                logger.info(this, "AppHubInit CHECK GROUP:" + groupid + " EXIST");
            }
        }
    }

    public class GroupCallBack extends ManageTestCallback {

        private String groupid;
        private String appids;
        private boolean isAdd;

        public void setGroupid(String groupid) {

            this.groupid = groupid;
        }

        public void setAppids(String appids) {

            this.appids = appids;
        }

        public void setAdd(boolean isAdd) {

            this.isAdd = isAdd;
        }

        @Override
        public void completed(HttpClientCallbackResult result) {

            if (isAdd) {
                logger.info(this, "AppHubInit ADD GROUP:" + groupid);
                Map<String, Object> group1 = new HashMap<String, Object>();
                group1.put("groupid", groupid);
                group1.put("appids", appids);
                group1.put("state", 1);
                group1.put("createtime", ctime);
                group1.put("operationtime", ctime);
                group1.put("operationuser", operationuser);
                byte[] addGroupData = getRequestData(manageTypeGroup,
                        createHttpMapRequest(bussinessTypeCreate, group1));
                run(manageTypeGroup, addGroupData, new ManageTestCallback());
            }

        }

    }

    /**
     * 回调end
     */

    /*
     * --------------------------tools
     */

    protected Map<String, Object> createHttpMapRequest(String bussinesstype, Map<String, Object> paramMap) {

        String data = JSONHelper.toString(paramMap);
        if (null == data) {
            data = "{}";
        }

        Map<String, Object> mapObject = new HashMap<String, Object>();
        mapObject.put("type", bussinesstype);
        mapObject.put("data", data);
        return mapObject;
    }

    protected void run(String type, byte[] data, ManageTestCallback callBack) {

        this.service.doHttpPost("uav.app.manage.apphubmanager.http.addr", "/ah/" + type, data, "application/json",
                "utf8", callBack);
    }

    protected String getWebProName(HttpServletRequest request) {

        String requestUrl = request.getRequestURI().substring(1);
        int index = requestUrl.indexOf("/");
        return requestUrl.substring(0, index);
    }

    protected byte[] getRequestData(String type, Map<String, Object> mapParam) {

        Map<String, String> dbInfo = getDBInfo(type);
        UAVHttpMessage request = new UAVHttpMessage();
        request.putRequest("mrd.data", JSONHelper.toString(mapParam));
        request.putRequest("datastore.name", dbInfo.get("dataStoreName"));
        request.putRequest("mgo.coll.name", dbInfo.get("conllectionName"));
        String jsonStr = JSONHelper.toString(request);
        byte[] datab = null;
        try {
            datab = jsonStr.getBytes("utf8");
        }
        catch (Exception e) {
            logger.err(this, "ManageBaseRestService getRequestData " + e.getMessage());
            throw new ApphubException(e);
        }

        return datab;
    }

    protected Map<String, String> getDBInfo(String type) {

        HashMap<String, String> result = new HashMap<String, String>();
        if (manageTypeApp.equals(type)) {
            result.put("dataStoreName", "AppHub.app");
            result.put("conllectionName", "uav_appinfo");
        }
        else if (manageTypeGroup.equals(type)) {
            result.put("dataStoreName", "AppHub.group");
            result.put("conllectionName", "uav_groupinfo");
        }
        return result;
    }

    @SuppressWarnings({ "unchecked" })
    private boolean checkResultIsEmpty(HttpClientCallbackResult result) {

        HashMap<String, Object> groupInfo = JSONHelper.toObject(result.getReplyDataAsString(), HashMap.class);
        String rs = String.valueOf(groupInfo.get("rs"));
        HashMap<String, Object> rsInfo = JSONHelper.toObject(rs, HashMap.class);
        String data = String.valueOf(rsInfo.get("data"));
        ArrayList<Object> dataInfo = JSONHelper.toObject(data, ArrayList.class);
        return dataInfo.isEmpty();
    }
}
