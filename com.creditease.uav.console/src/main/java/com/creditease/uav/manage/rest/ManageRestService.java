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

package com.creditease.uav.manage.rest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.NetworkHelper;
import com.creditease.agent.helpers.PropertiesHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.uav.apphub.core.AppHubBaseRestService;
import com.creditease.uav.apphub.sso.GUISSOClient;
import com.creditease.uav.apphub.sso.GUISSOClientFactory;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.cache.api.CacheManagerFactory;
import com.creditease.uav.exception.ApphubException;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;
import com.creditease.uav.manage.rest.entity.AppEntity;
import com.creditease.uav.manage.rest.entity.GroupEntity;

/**
 * AppManage 应用服务端入口 Created by lbay on 2016/4/14.
 */
@Singleton
@Path("manage")
@Produces("application/json;charset=utf-8")
public class ManageRestService extends AppHubBaseRestService {

    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    protected CacheManager cm = null;

    private String manageTypeApp = "app";
    private String manageTypeGroup = "group";
    private String bussinessTypeCreate = "create";
    private String bussinessTypeModify = "modify";
    private String bussinessTypeQuery = "query";

    private String appBaseUrl;

    @SuppressWarnings("unchecked")
    @Override
    public void init() {

        String port = request.getServletContext().getInitParameter("uav.server.port");
        if (port == null) {
            port = String.valueOf(request.getServerPort());
        }
        appBaseUrl = request.getScheme() + "://" + NetworkHelper.getLocalIP() + ":" + port + "/"
                + getWebProName(request) + "/";

        Map<String, Integer> httpParamsMap = JSONHelper
                .toObject(request.getServletContext().getInitParameter("uav.app.manage.http.client.params"), Map.class);
        initHttpClient(httpParamsMap.get("max.con"), httpParamsMap.get("max.tot.con"),
                httpParamsMap.get("sock.time.out"), httpParamsMap.get("con.time.out"),
                httpParamsMap.get("req.time.out"));

        String redisAddrStr = request.getServletContext().getInitParameter("uav.app.manage.redis.store.addr");
        Map<String, Object> redisParamsMap = JSONHelper
                .toObject(request.getServletContext().getInitParameter("uav.app.manage.redis.store.params"), Map.class);
        cm = CacheManagerFactory.build(redisAddrStr, Integer.valueOf(String.valueOf(redisParamsMap.get("min"))),
                Integer.valueOf(String.valueOf(redisParamsMap.get("max"))),
                Integer.valueOf(String.valueOf(redisParamsMap.get("queue"))),
                String.valueOf(redisParamsMap.get("pwd")));
    }

    @GET
    @Override
    public String ping() {

        return super.ping();
    }

    @POST
    @Path("loadAppByid")
    public void loadAppByid(AppEntity appEntity, @Suspended AsyncResponse response) {

        Map<String, Object> where = new HashMap<String, Object>();
        where.put("appid", appEntity.getAppid());
        where.put("state", 1);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("where", where);

        Map<String, Object> jsonRequest = createHttpMapRequest(bussinessTypeQuery, data);
        Map<String, String> callBackInput = createAppInput(null, null, "query");
        manageHttpAsyncClientPost(manageTypeApp, jsonRequest, new AppHttpCallBack(response, callBackInput));
    }

    @POST
    @Path("loadApps")
    public void loadApps(AppEntity appEntity, @Suspended AsyncResponse response) {

        Map<String, Object> regex = new HashMap<String, Object>();
        regex.put("appurl", appEntity.getAppurl());

        Map<String, Object> where = new HashMap<String, Object>();
        where.put("regex", regex);
        where.put("state", 1);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("where", where);
        if (null != appEntity.getPageindex()) {
            data.put("pageindex", appEntity.getPageindex());
        }
        if (null != appEntity.getPagesize()) {
            data.put("pagesize", appEntity.getPagesize());
        }

        HashMap<String, Object> sort = new HashMap<String, Object>();
        sort.put("values", "createtime");
        sort.put("sortorder", "-1");

        data.put("sort", sort);

        Map<String, Object> jsonRequest = createHttpMapRequest(bussinessTypeQuery, data);
        Map<String, String> callBackInput = createAppInput(null, null, "query");
        manageHttpAsyncClientPost(manageTypeApp, jsonRequest, new AppHttpCallBack(response, callBackInput));
    }

    @POST
    @Path("loadAllApps")
    public void loadAllApps(@Suspended AsyncResponse response) {

        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, Object> where = new HashMap<String, Object>();
        data.put("where", where);

        HashMap<String, Object> sort = new HashMap<String, Object>();
        sort.put("values", "createtime");
        sort.put("sortorder", "-1");
        data.put("sort", sort);

        Map<String, Object> jsonRequest = createHttpMapRequest(bussinessTypeQuery, data);
        Map<String, String> callBackInput = createAppInput(null, null, "query");
        manageHttpAsyncClientPost(manageTypeApp, jsonRequest, new AppHttpCallBack(response, callBackInput));
    }

    @POST
    @Path("loadGroupByInfo")
    public void loadGroupByInfo(GroupEntity groupEntity, @Suspended AsyncResponse response) {

        Map<String, Object> where = new HashMap<String, Object>();
        where.put("groupid", groupEntity.getGroupid());
        where.put("ldapkey", groupEntity.getLdapkey());
        where.put("state", 1);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("where", where);

        Map<String, Object> jsonRequest = createHttpMapRequest(bussinessTypeQuery, data);
        Map<String, String> callBackInput = createGroupInput(null, null, null, "query");
        manageHttpAsyncClientPost(manageTypeGroup, jsonRequest, new GroupHttpCallBack(response, callBackInput));
    }

    @POST
    @Path("loadGroupByid")
    public void loadGroupByid(GroupEntity groupEntity, @Suspended AsyncResponse response) {

        Map<String, Object> where = new HashMap<String, Object>();
        where.put("_id", groupEntity.getId());
        where.put("state", 1);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("where", where);

        Map<String, Object> jsonRequest = createHttpMapRequest(bussinessTypeQuery, data);
        Map<String, String> callBackInput = createGroupInput(null, null, null, "query");
        manageHttpAsyncClientPost(manageTypeGroup, jsonRequest, new GroupHttpCallBack(response, callBackInput));
    }

    @POST
    @Path("loadAllGroups")
    public void loadAllGroups(GroupEntity groupEntity, @Suspended AsyncResponse response) {

        Map<String, Object> regex = new HashMap<String, Object>();
        regex.put("groupid", groupEntity.getGroupid());

        Map<String, Object> where = new HashMap<String, Object>();
        where.put("regex", regex);
        where.put("state", 1);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("where", where);
        if (null != groupEntity.getPageindex()) {
            data.put("pageindex", groupEntity.getPageindex());
        }
        if (null != groupEntity.getPagesize()) {
            data.put("pagesize", groupEntity.getPagesize());
        }

        HashMap<String, Object> sort = new HashMap<String, Object>();
        sort.put("values", "createtime");
        sort.put("sortorder", "-1");
        data.put("sort", sort);

        Map<String, Object> jsonRequest = createHttpMapRequest(bussinessTypeQuery, data);
        Map<String, String> callBackInput = createGroupInput(null, null, null, "query");
        manageHttpAsyncClientPost(manageTypeGroup, jsonRequest, new GroupHttpCallBack(response, callBackInput));
    }

    @SuppressWarnings("rawtypes")
    @POST
    @Path("loadCount")
    public void loadCount(String paramVlue, @Suspended AsyncResponse response) {

        String coulum = null;
        String mType = null;
        ManageHttpClientCallback messageCallBack = null;
        Map obj = JSONHelper.toObject(paramVlue, Map.class);
        String type = (String) obj.get("type");
        if ("APP".equals(type)) {
            coulum = "appurl";
            mType = manageTypeApp;
            messageCallBack = new AppHttpCallBack(response, createAppInput(null, null, "query"));
        }
        else if ("GROUP".equals(type)) {
            coulum = "groupid";
            mType = manageTypeGroup;
            Map<String, String> callBackInput = createGroupInput(null, null, null, "query");
            messageCallBack = new GroupHttpCallBack(response, callBackInput);
        }
        Map<String, Object> regex = new HashMap<String, Object>();
        regex.put(coulum, obj.get("paramVlue"));

        Map<String, Object> where = new HashMap<String, Object>();
        where.put("regex", regex);
        where.put("state", 1);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("where", where);
        data.put("count", "true");

        Map<String, Object> jsonRequest = createHttpMapRequest(bussinessTypeQuery, data);
        manageHttpAsyncClientPost(mType, jsonRequest, messageCallBack);
    }

    @POST
    @Path("addApp")
    public void addApp(AppEntity appEntity, @Suspended AsyncResponse response) throws Exception {

        Map<String, Object> data = new HashMap<String, Object>();

        String appurl = XSSFilter(appEntity.getAppurl());
        String id = XSSFilter(appEntity.getAppid());
        String configpath = XSSFilter(appEntity.getConfigpath());
        String ctime = df.format(new Date());

        data.put("appid", id);
        data.put("appurl", appurl);
        data.put("configpath", configpath);
        data.put("state", 1);
        data.put("createtime", ctime);
        data.put("operationtime", ctime);
        data.put("operationuser", request.getSession(false).getAttribute("apphub.gui.session.login.user.id"));

        Map<String, Object> jsonRequest = createHttpMapRequest(bussinessTypeCreate, data);
        Map<String, String> callBackInput = createAppInput(id, appurl, "create");
        callBackInput.put("configpath", configpath);
        manageHttpAsyncClientPost(manageTypeApp, jsonRequest, new AppHttpCallBack(response, callBackInput));

    }

    @POST
    @Path("addGroup")
    public void addGroup(GroupEntity groupEntity, @Suspended AsyncResponse response) throws Exception {

        String groupId = XSSFilter(groupEntity.getGroupid());
        String ldapkey = XSSFilter(groupEntity.getLdapkey());
        String appIds = XSSFilter(groupEntity.getAppids());
        String time = df.format(new Date());
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("groupid", groupId);
        data.put("ldapkey", ldapkey);
        data.put("appids", appIds);
        data.put("state", 1);
        data.put("createtime", time);
        data.put("operationtime", time);
        data.put("operationuser", request.getSession(false).getAttribute("apphub.gui.session.login.user.id"));

        Map<String, Object> jsonRequest = createHttpMapRequest(bussinessTypeCreate, data);
        Map<String, String> callBackInput = createGroupInput(groupId, ldapkey, appIds, "create");
        manageHttpAsyncClientPost(manageTypeGroup, jsonRequest, new GroupHttpCallBack(response, callBackInput));
    }

    @POST
    @Path("updateApp")
    public void updateApp(AppEntity appEntity, @Suspended AsyncResponse response) throws Exception {

        String id = XSSFilter(appEntity.getAppid());
        String newurl = XSSFilter(appEntity.getAppurl());
        String configpath = XSSFilter(appEntity.getConfigpath());
        String ctime = df.format(new Date());
        Map<String, Object> update = new HashMap<String, Object>();
        Map<String, Object> set = new HashMap<String, Object>();
        Map<String, Object> modify = new HashMap<String, Object>();
        Map<String, Object> where = new HashMap<String, Object>();

        where.put("appid", id);
        modify.put("where", where);
        set.put("appurl", newurl);
        set.put("configpath", configpath);
        set.put("operationtime", ctime);
        set.put("operationuser", request.getSession(false).getAttribute("apphub.gui.session.login.user.id"));
        update.put("set", set);
        modify.put("update", update);

        Map<String, Object> jsonRequest = createHttpMapRequest(bussinessTypeModify, modify);
        Map<String, String> callBackInput = createAppInput(id, newurl, "modify");
        callBackInput.put("configpath", configpath);
        manageHttpAsyncClientPost(manageTypeApp, jsonRequest, new AppHttpCallBack(response, callBackInput));
    }

    @POST
    @Path("updateGroup")
    public void updateGroup(GroupEntity groupEntity, @Suspended AsyncResponse response) throws Exception {

        String id = XSSFilter(groupEntity.getId());
        String groupId = XSSFilter(groupEntity.getGroupid());
        String ldapKey = XSSFilter(groupEntity.getLdapkey());
        String appIds = XSSFilter(groupEntity.getAppids());
        String time = df.format(new Date());
        Map<String, Object> where = new HashMap<String, Object>();
        where.put("_id", id);

        Map<String, Object> cloumns = new HashMap<String, Object>();
        cloumns.put("operationtime", time);
        cloumns.put("operationuser", request.getSession(false).getAttribute("apphub.gui.session.login.user.id"));
        cloumns.put("appids", appIds);

        Map<String, Object> set = new HashMap<String, Object>();
        set.put("set", cloumns);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("where", where);
        data.put("update", set);
        Map<String, Object> jsonRequest = createHttpMapRequest(bussinessTypeModify, data);
        Map<String, String> callBackInput = createGroupInput(groupId, ldapKey, appIds, "modify");
        manageHttpAsyncClientPost(manageTypeGroup, jsonRequest, new GroupHttpCallBack(response, callBackInput));
    }

    @POST
    @Path("delApp")
    public void delApp(AppEntity appEntity, @Suspended AsyncResponse response) throws Exception {

        String id = appEntity.getAppid();
        String ctime = df.format(new Date());
        Map<String, Object> update = new HashMap<String, Object>();
        Map<String, Object> set = new HashMap<String, Object>();
        Map<String, Object> modify = new HashMap<String, Object>();
        Map<String, Object> where = new HashMap<String, Object>();

        where.put("appid", id);
        modify.put("where", where);
        set.put("state", 0);
        set.put("operationtime", ctime);
        set.put("operationuser", request.getSession(false).getAttribute("apphub.gui.session.login.user.id"));
        update.put("set", set);
        modify.put("update", update);

        Map<String, Object> jsonRequest = createHttpMapRequest(bussinessTypeModify, modify);
        Map<String, String> callBackInput = createAppInput(id, null, "delete");
        manageHttpAsyncClientPost(manageTypeApp, jsonRequest, new AppHttpCallBack(response, callBackInput));
    }

    @POST
    @Path("delGroup")
    public void delGroup(GroupEntity groupEntity, @Suspended AsyncResponse response) throws Exception {

        String id = XSSFilter(groupEntity.getId());
        String groupId = XSSFilter(groupEntity.getGroupid());
        String ldapKey = XSSFilter(groupEntity.getLdapkey());
        String time = df.format(new Date());
        Map<String, Object> where = new HashMap<String, Object>();
        where.put("_id", id);

        Map<String, Object> cloumns = new HashMap<String, Object>();
        cloumns.put("state", 0);
        cloumns.put("operationtime", time);
        cloumns.put("operationuser", request.getSession(false).getAttribute("apphub.gui.session.login.user.id"));

        Map<String, Object> set = new HashMap<String, Object>();
        set.put("set", cloumns);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("where", where);
        data.put("update", set);
        Map<String, Object> jsonRequest = createHttpMapRequest(bussinessTypeModify, data);
        Map<String, String> callBackInput = createGroupInput(groupId,ldapKey, null, "delete");
        manageHttpAsyncClientPost(manageTypeGroup, jsonRequest, new GroupHttpCallBack(response, callBackInput));
    }

    private void manageHttpAsyncClientPost(String type, Map<String, Object> mapRequest,
            final ManageHttpClientCallback callback) {

        byte[] data = getRequestData(type, mapRequest);
        doHttpPost("uav.app.manage.apphubmanager.http.addr", "/ah/" + type, data, "application/json", "utf-8",
                callback);

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @POST
    @Path("loadInfoByEmail")
    public void loadInfoByEmail(String json, @Suspended AsyncResponse response) {

        Map<String, String> input = JSONHelper.toObject(json, Map.class);
        String email = input.get("value").trim();
        String type = input.get("type");
        String result = "";

        if (StringHelper.isEmpty(email)) {
            Map userInfo = new HashMap();
            userInfo.put("msg", "email is empty");
            result = JSONHelper.toString(userInfo);
        }
        else if ("list".equals(type)) {
            GUISSOClient guissoClient = GUISSOClientFactory.getGUISSOClient(request);
            Object info = guissoClient.getEmailListByQuery(email);
            result = JSONHelper.toString(info);
        }
        else if ("user".equals(type)) {
            GUISSOClient guissoClient = GUISSOClientFactory.getGUISSOClient(request);
            Object info = guissoClient.getUserByQuery(email);
            result = JSONHelper.toString(info);
        }
        response.resume(result);
    }

    // tools begin---------------------------------------------------------->

    /**
     * 打包请求数据
     * 
     * @param type
     * @param mapParam
     * @return
     */
    private byte[] getRequestData(String type, Map<String, Object> mapParam) {

        Map<String, String> dbInfo = getDBInfo(type);
        UAVHttpMessage request = new UAVHttpMessage();
        request.putRequest("mrd.data", JSONHelper.toString(mapParam));
        request.putRequest("datastore.name", dbInfo.get("dataStoreName"));
        request.putRequest("mgo.coll.name", dbInfo.get("conllectionName"));
        String jsonStr = JSONHelper.toString(request);
        byte[] datab = null;
        try {
            datab = jsonStr.getBytes("utf-8");
        }
        catch (Exception e) {
            logger.err(this, "ManageBaseRestService getRequestData \n" + e.getMessage());
            throw new ApphubException(e);
        }

        return datab;
    }

    /**
     * 打包数据库操作信息
     * 
     * @param type
     * @return
     */
    private Map<String, String> getDBInfo(String type) {

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

    private Map<String, Object> createHttpMapRequest(String bussinesstype, Map<String, Object> paramMap) {

        String data = JSONHelper.toString(paramMap);
        if (null == data) {
            data = "{}";
        }

        Map<String, Object> mapObject = new HashMap<String, Object>();
        mapObject.put("type", bussinesstype);
        mapObject.put("data", data);
        return mapObject;
    }

    private String getWebProName(HttpServletRequest request) {

        String requestUrl = request.getRequestURI().substring(1);
        int index = requestUrl.indexOf("/");
        return requestUrl.substring(0, index);
    }

    private Map<String, String> createAppInput(String id, String value, String bType) {

        Map<String, String> input = new HashMap<String, String>();
        input.put("region", "apphub.gui.cache");
        input.put("region.appKey", "manage.app");
        input.put("region.groupKey", "manage.group");
        input.put("id", id);
        input.put("value", value);
        input.put("bType", bType);
        return input;
    }

    private Map<String, String> createGroupInput(String groupId, String ldappKey, String value, String bType) {

        Map<String, String> input = new HashMap<String, String>();
        input.put("region", "apphub.gui.cache");
        input.put("region.appKey", "manage.app");
        input.put("region.groupKey", "manage.group");
        input.put("groupId", groupId);
        input.put("ldappKey", ldappKey);
        input.put("value", value);
        input.put("bType", bType);
        return input;
    }

    // tools end---------------------------------------------------------->

    /**
     * 
     * ManageHttpClientCallback description: manage 回调类
     *
     */
    abstract class ManageHttpClientCallback implements HttpClientCallback {

        protected Map<String, String> input = new HashMap<String, String>();
        protected AsyncResponse asyncResponse;

        public ManageHttpClientCallback(AsyncResponse asyncResponseParam, Map<String, String> inputParam) {
            this.asyncResponse = asyncResponseParam;
            this.input = inputParam;
        }

        @Override
        public void completed(HttpClientCallbackResult result) {

            String respStr = result.getReplyDataAsString();
            asyncResponse.resume(respStr);
            after(result);
        }

        @Override
        public void failed(HttpClientCallbackResult result) {

            logger.err(this, result.getException().toString());
            String resp = "ManageHttpClientCallback httpAsyncClientPost is failed ";
            asyncResponse.resume(resp);

        }

        abstract void after(HttpClientCallbackResult result);

    }

    class AppHttpCallBack extends ManageHttpClientCallback {

        public AppHttpCallBack(AsyncResponse asyncResponseParam, Map<String, String> inputParam) {
            super(asyncResponseParam, inputParam);
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        void after(HttpClientCallbackResult result) {

            String id = input.get("id");
            String region = input.get("region");
            String regionAppKey = input.get("region.appKey");
            String bType = input.get("bType");
            String appUrl = input.get("value");

            if ("delete".equals(bType)) {
                cm.delHash(region, regionAppKey, id);
            }
            else if ("modify".equals(bType) || "create".equals(bType)) {

                String configUrl = appUrl;

                String configpath = input.get("configpath");
                if (null != configpath && configpath.length() != 0) {
                    configUrl = configpath;
                }

                // 远程读取资源
                if (configUrl.indexOf("http") == -1) {
                    configUrl = appBaseUrl + configUrl;
                }

                configUrl += "/config.properties";
                Properties properties;
                try {

                    properties = PropertiesHelper.downloadProperties(configUrl);
                }
                catch (Exception e) {
                    logger.err(this, "AppHttpCallBack app 配置加载失败  :" + configUrl);
                    throw new ApphubException(e);
                }

                // 菜单封装
                List jsonArray = menuSet(appUrl, properties);

                // 打包
                String appTitleStr = properties.getProperty("app.title");
                Map resultJson = new LinkedHashMap();
                resultJson.put("url", appUrl);
                resultJson.put("title", appTitleStr);
                resultJson.put("menu", jsonArray);
                String dataStr = JSONHelper.toString(resultJson);

                cm.putHash(region, regionAppKey, id, dataStr);

            }

        }

    }

    class GroupHttpCallBack extends ManageHttpClientCallback {

        public GroupHttpCallBack(AsyncResponse asyncResponseParam, Map<String, String> inputParam) {
            super(asyncResponseParam, inputParam);
        }

        @Override
        void after(HttpClientCallbackResult result) {

            String region = input.get("region");
            String regionAppKey = input.get("region.groupKey");
            String bType = input.get("bType");

            String groupId = input.get("groupId");
            String ldappKey = input.get("ldappKey");
            String value = input.get("value");

            String cacheId = "groupId:" + groupId + ",ldappKey:" + ldappKey;
            if ("delete".equals(bType)) {
                cm.delHash(region, regionAppKey, cacheId);
            }
            else if ("modify".equals(bType) || "create".equals(bType)) {
                cm.putHash(region, regionAppKey, cacheId, value);
            }

        }

    }

    /**
     * 菜单设置
     * 
     * @param appUrl
     * @param properties
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private List menuSet(String appUrl, Properties properties) {

        List jsonArray = new ArrayList();

        Iterator<Object> i = properties.keySet().iterator();
        while (i.hasNext()) {
            String key = String.valueOf(i.next());
            if (!key.startsWith("app.@")) {
                continue;
            }
            Map funcJson = JSONHelper.toObject(properties.getProperty(key), Map.class);
            String menuTitle = (String) funcJson.get("title");
            String fileName = (String) funcJson.get("file");
            String index = (String) funcJson.get("index");
            String funcId = key.replaceFirst("app.@", "");
            String funcUrl = appUrl + "/" + funcId + "/" + fileName;
            Map func = new LinkedHashMap();
            func.put("functions", menuTitle);
            func.put("funcId", funcId);
            func.put("url", funcUrl);
            func.put("index", index);
            jsonArray.add(func);
        }

        // sort app menu
        Collections.sort(jsonArray, new Comparator<Map>() {

            @Override
            public int compare(Map o1, Map o2) {

                int index1 = Integer.parseInt((String) o1.get("index"));
                int index2 = Integer.parseInt((String) o2.get("index"));

                return index1 - index2;
            }

        });
        return jsonArray;
    }

}
