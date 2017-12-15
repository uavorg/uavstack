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

package com.creditease.uav.grafana;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.container.AsyncResponse;

import org.apache.http.HttpResponse;

import com.creditease.agent.helpers.DataStoreHelper;
import com.creditease.agent.helpers.DateTimeHelper;
import com.creditease.agent.helpers.EncodeHelper;
import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.httpasync.HttpClientCallbackResult;

public class DashboardManagement {

    private String dashboardTemp = null;
    private String datasourceTemp = null;
    private String esPanelTemp = null;
    private String opentsdbPanelTemp = null;
    private GrafanaClient grafanaClient = null;

    // ----------------------------------------------------------//
    /**
     * store region key
     */
    private String STORE_REGION_GODCOMPASSCACHE = "apphub.godcompass.cache";
    // ----------------------------------------------------------//
    /**
     * store key
     */
    private String STORE_KEY_GRAFANACONFIG = "grafana.config";

    public GrafanaClient getGrafanaClient() {

        return grafanaClient;
    }

    public void setGrafanaClient(GrafanaClient grafanaClient) {

        this.grafanaClient = grafanaClient;
    }

    public enum DSType {
        OPENTSDB("opentsdb"), ES("es");

        private String type;

        DSType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {

            return type;
        }
    }

    /**
     * 获取orgId
     */
    private class GetOrgIdCallBack extends GrafanaHttpCallBack {

        @SuppressWarnings("rawtypes")
        public GetOrgIdCallBack(Map _params, CountDownLatch latch) {

            super(_params, latch);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public void completed(HttpClientCallbackResult result) {

            String reStr = result.getReplyDataAsString();
            completedLog(result, reStr);
            Map resultmap = JSONHelper.toObject(reStr, Map.class);
            String orgId = String.valueOf(resultmap.get("id"));
            Map orgIdMap = (Map) params.get("orgIdMap");
            orgIdMap.put("orgId", orgId);
            String retCode = Integer.toString(result.getRetCode());
            orgIdMap.put("retCode", retCode);
            latch.countDown();
        }
    }

    /**
     * 创建org
     */
    private class CreateOrgCallBack extends GrafanaHttpCallBack {

        @SuppressWarnings("rawtypes")
        public CreateOrgCallBack(Map _params, CountDownLatch latch) {
            super(_params, latch);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public void completed(HttpClientCallbackResult result) {

            String reStr = result.getReplyDataAsString();
            completedLog(result, reStr);

            Map resultmap = JSONHelper.toObject(reStr, Map.class);
            String orgId = String.valueOf(resultmap.get("orgId"));
            Map orgIdMap = (Map) params.get("orgIdMap");
            orgIdMap.put("orgId", orgId);
            String retCode = Integer.toString(result.getRetCode());
            orgIdMap.put("retCode", retCode);
            latch.countDown();
        }

    }

    /**
     * 获取dashboard
     */
    private class GetDashboardTemplateCallBack extends GrafanaHttpCallBack {

        @SuppressWarnings("rawtypes")
        public GetDashboardTemplateCallBack(Map _params, CountDownLatch latch) {

            super(_params, latch);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public void completed(HttpClientCallbackResult result) {

            String reStr = result.getReplyDataAsString();
            completedLog(result, reStr);
            Map dashTemplMap = (Map) params.get("dashTemplMap");
            dashTemplMap.put("dashTempl", reStr);
            String retCode = Integer.toString(result.getRetCode());
            dashTemplMap.put("retCode", retCode);
            latch.countDown();
        }
    }

    /**
     * 
     * 不存在用户信息：完成处理
     * 
     * 存在用户信息：
     * 
     * 1：获取组织架构返回信息：如果存在则将用户添加到指定组织架构的，并赋予操作权限
     * 
     */
    @SuppressWarnings("rawtypes")
    private class ApiOrgsCallBack extends GrafanaHttpCallBack {

        public ApiOrgsCallBack(Map _params) {
            super(_params);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void completed(HttpClientCallbackResult result) {

            String reStr = result.getReplyDataAsString();
            completedLog(result, reStr);

            if (!this.params.containsKey("userName") || !this.params.containsKey("orgName")
                    || !this.params.containsKey("orgRole")) {
                return;
            }

            List<Map> groups = JSONHelper.toObjectArray(reStr, Map.class);
            String userName = String.valueOf(this.params.get("userName"));
            String orgName = String.valueOf(this.params.get("orgName"));
            String orgRole = String.valueOf(this.params.get("orgRole"));

            for (Map group : groups) {
                String id = String.valueOf(group.get("id"));
                String groupName = String.valueOf(group.get("name"));

                if (groupName.equals(orgName) && !"Main Org.".equals(orgName)) {
                    this.params.put("switchOrgId", id);
                    this.params.put("switchOrgName", orgName);
                }

                if (groupName.equals(orgName)) {
                    Map<String, Object> orgUserReq = new HashMap<String, Object>();
                    orgUserReq.put("loginOrEmail", userName);
                    orgUserReq.put("role", orgRole);
                    String orgUserData = JSONHelper.toString(orgUserReq);
                    String path = "/api/orgs/" + id + "/users";

                    // 对指定组织架构添加用户访问权限
                    grafanaClient.doAsyncHttp("post", path, orgUserData, null);
                    break;
                }
            }

        }

    }

    /**
     * 添加用户到所有组织架构
     *
     */
    private class ApiOrgsAllCallBack extends GrafanaHttpCallBack {

        @SuppressWarnings("rawtypes")
        public ApiOrgsAllCallBack(Map _params) {
            super(_params);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public void completed(HttpClientCallbackResult result) {

            String reStr = result.getReplyDataAsString();
            completedLog(result, reStr);

            if (!this.params.containsKey("userName") || !this.params.containsKey("orgRole")) {
                return;
            }
            String orgRole = String.valueOf(this.params.get("orgRole"));
            String userName = String.valueOf(this.params.get("userName"));

            List<Map> groups = JSONHelper.toObjectArray(reStr, Map.class);
            for (Map group : groups) {
                String id = String.valueOf(group.get("id"));
                String name = String.valueOf(group.get("name"));

                if ("Main Org.".equals(name)) {
                    continue;
                }

                this.params.put("switchOrgId", id);
                this.params.put("switchOrgName", name);

                Map<String, Object> orgUserReq = new HashMap<String, Object>();
                orgUserReq.put("loginOrEmail", userName);
                orgUserReq.put("role", orgRole);
                String orgUserData = JSONHelper.toString(orgUserReq);
                String path = "/api/orgs/" + id + "/users";
                // 对指定组织架构添加用户访问权限
                grafanaClient.doAsyncHttp("post", path, orgUserData, null);

            }

        }
    }

    /**
     * 查询所有用户,获取当前用户信息:
     * 
     * 1、存在则删除当前用户（组织架构下该用户：grafana会自动移除该用户），然后再添加用户
     * 
     * 2、不存在，添加用户
     * 
     */
    private class DelOrAddUserCallBack extends GrafanaHttpCallBack {

        @SuppressWarnings("rawtypes")
        public DelOrAddUserCallBack(Map _params) {
            super(_params);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public void completed(HttpClientCallbackResult result) {

            String reStr = result.getReplyDataAsString();
            completedLog(result, reStr);

            if (!this.params.containsKey("userName")) {
                return;
            }

            String userName = String.valueOf(this.params.get("userName"));
            List<Map> resultList = JSONHelper.toObjectArray(reStr, Map.class);

            boolean noExists = true;
            for (Map map : resultList) {
                String ulogin = String.valueOf(map.get("login"));
                // 找到当前用户
                if (userName.equals(ulogin)) {
                    noExists = false;
                    String uId = String.valueOf(map.get("id"));
                    // 删除
                    String path = "/api/admin/users/" + uId;
                    grafanaClient.doAsyncHttp("delete", path, null, new DelThenAddUserCallBack(this.params));
                    break;
                }
            }

            if (noExists) {
                Map<String, Object> userReq = new HashMap<String, Object>();
                userReq.put("name", this.params.get("userName"));
                userReq.put("login", this.params.get("userName"));
                userReq.put("password", this.params.get("password"));
                userReq.put("email", this.params.get("userName"));
                String userData = JSONHelper.toString(userReq);
                // 添加用户
                grafanaClient.doAsyncHttp("post", "/api/admin/users", userData, new AddUserCallBack());
            }
        }
    }

    /**
     * 添加用户
     *
     */
    private class DelThenAddUserCallBack extends GrafanaHttpCallBack {

        @SuppressWarnings("rawtypes")
        public DelThenAddUserCallBack(Map _params) {
            super(_params);
        }

        @Override
        public void completed(HttpClientCallbackResult result) {

            String reStr = result.getReplyDataAsString();
            completedLog(result, reStr);

            if (!this.params.containsKey("userName") || !this.params.containsKey("password")) {
                return;
            }

            Map<String, Object> userReq = new HashMap<String, Object>();
            userReq.put("name", this.params.get("userName"));
            userReq.put("login", this.params.get("userName"));
            userReq.put("password", this.params.get("password"));
            userReq.put("email", this.params.get("userName"));
            String userData = JSONHelper.toString(userReq);
            // 添加用户
            grafanaClient.doAsyncHttp("post", "/api/admin/users", userData, new AddUserCallBack());

        }
    }

    @SuppressWarnings("unchecked")
    private class AddUserCallBack extends GrafanaHttpCallBack {

        @Override
        public void completed(HttpClientCallbackResult result) {

            String reStr = result.getReplyDataAsString();
            completedLog(result, reStr);
            // 添加用户的返回信息
            Map<String, String> resultMap = JSONHelper.toObject(reStr, Map.class);

            if (!resultMap.containsKey("id")) {
                return;
            }
            // 查询默认的组织
            String path = "/api/orgs/name/Main%20Org%2E";
            grafanaClient.doAsyncHttp("get", path, null, new GetOrgsNameCallBack(resultMap));
        }
    }

    private class GetOrgsNameCallBack extends GrafanaHttpCallBack {

        @SuppressWarnings("rawtypes")
        public GetOrgsNameCallBack(Map _params) {
            super(_params);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void completed(HttpClientCallbackResult result) {

            String reStr = result.getReplyDataAsString();
            completedLog(result, reStr);

            // 组织信息
            Map<String, String> resultMap = JSONHelper.toObject(reStr, Map.class);
            String orgid = String.valueOf(resultMap.get("id"));
            String userId = String.valueOf(this.params.get("id"));

            // 删除默认组织下用户
            String path = "/api/orgs/" + orgid + "/users/" + userId;
            grafanaClient.doAsyncHttp("delete", path, null, null);
        }
    }

    /**
     * 指定组织架构添加用户访问权限
     * 
     * 先添加组织架构：详情处理见ApiOrgsCallBack
     * 
     * @param userName
     * @param organNames
     */
    public List<GrafanaHttpCallBack> addUserOrgs(String userName, Map userGroups) {

        Object[] groups = userGroups.keySet().toArray();
        List<GrafanaHttpCallBack> listCallBack = new ArrayList<GrafanaHttpCallBack>();
        for (Object group : groups) {
            // 回调参数
            String role = String.valueOf(userGroups.get(group));
            Map<String, String> cbParams = new HashMap<String, String>();
            cbParams.put("userName", userName);
            cbParams.put("orgName", String.valueOf(group));
            cbParams.put("orgRole", role);

            GrafanaHttpCallBack callback = new ApiOrgsCallBack(cbParams);
            listCallBack.add(callback);
            grafanaClient.doAsyncHttp("get", "/api/orgs", null, callback);
        }

        threadSleep();
        return listCallBack;

    }

    /**
     * 添加用户到所有组织架构
     * 
     * @param userName
     * @param orgRole
     * @return
     */
    public GrafanaHttpCallBack addUserOrgsAll(String userName, String orgRole) {

        Map<String, String> cbParams = new HashMap<String, String>();
        cbParams.put("userName", userName);
        cbParams.put("orgRole", orgRole);

        GrafanaHttpCallBack callback = new ApiOrgsAllCallBack(cbParams);
        grafanaClient.doAsyncHttp("get", "/api/orgs", null, callback);

        threadSleep();
        return callback;
    }

    /**
     * 存在则删除，不存在直接添加
     * 
     * @param userName
     */
    @SuppressWarnings("unchecked")
    public void delOrAddUser(String userName, HttpServletRequest request) {

        Map params = new HashMap();
        params.put("request", request);
        params.put("userName", userName);
        params.put("password", grafanaClient.getConfigValue("authorization.register.defPwd")); // 自动注册用户密码都统一一样

        // 获取用户信息：回调处理详情见ApiUsersCallBack
        DelOrAddUserCallBack callback = new DelOrAddUserCallBack(params);
        grafanaClient.doAsyncHttp("get", "/api/users", null, callback);
        threadSleep();
    }

    private void threadSleep() {

        try {
            Thread.sleep(Long.valueOf(grafanaClient.getConfigValue("authorization.register.sleep.time")));
        }
        catch (InterruptedException e) {
        }
    }

    /**
     * 根据配置结合模板创建dashboard
     *
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void createDashboards(String configString, String contextPath, HttpServletRequest request, CacheManager cm,
            AsyncResponse response) {

        List<Map> configs = JSONHelper.toObjectArray(configString, Map.class);

        Map<String, Object> valueObjNewMap = getValueObjNewMap(configs);
        int countFailed = 0;
        List<String> listFailed = new ArrayList<String>();

        for (String appgroupName : valueObjNewMap.keySet()) {
            String orgId = getOrCreateOrgId(contextPath, appgroupName);

            Map<String, List> appidNewMap = (Map) valueObjNewMap.get(appgroupName);
            for (Map.Entry<String, List> entry : appidNewMap.entrySet()) {
                boolean createFd = callGrafanaDashBoardCreate(entry, request, contextPath, cm, appgroupName, orgId);
                if (createFd) {
                    countFailed++;
                    String strFailed = appgroupName + "___" + entry.getKey();
                    listFailed.add(strFailed);
                }
            }
        }

        Map<String, String> resultMap = new HashMap<String, String>();
        String msgFailed = JSONHelper.toString(listFailed);
        String msgString = "有" + countFailed + "个dashboard创建失败";
        resultMap.put("code", "00");
        resultMap.put("msg", msgString);
        resultMap.put("data", msgFailed);
        String resultMsg = JSONHelper.toString(resultMap);
        response.resume(resultMsg);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private boolean callGrafanaDashBoardCreate(Map.Entry<String, List> entry, HttpServletRequest request,
            String contextPath, CacheManager cm, String appgroupName, String orgId) {

        boolean createFailed = false;
        String timeStr = DateTimeHelper.dateFormat(new Date(), "yyyy-MM-dd HH:mm:ss");
        HttpSession session = request.getSession(false);
        String user = "";
        if (session != null) {
            user = JSONHelper.toString(session.getAttribute("apphub.gui.session.login.user.id"));
        }
        String appidName = entry.getKey();
        List<String> appurlsName = entry.getValue();
        String orgGroupKey = appgroupName + "___" + appidName;
        Map<String, Object> valueObjMap = new HashMap<String, Object>();
        valueObjMap.put("createtime", timeStr);
        valueObjMap.put("updatetime", timeStr);
        valueObjMap.put("owner", user);
        valueObjMap.put("appurls", appurlsName);
        valueObjMap.put("orgGroupKey", orgGroupKey);
        String orgGroupValue = JSONHelper.toString(valueObjMap);
        if (dashboardTemp == null) {
            dashboardTemp = IOHelper.readTxtFile(contextPath + "/config/dashboardTemp.json", "UTF-8");
        }
        // 根据配置创建模板
        String template = initTemplate(appidName, appurlsName);
        Map<String, String> header = new HashMap<String, String>();
        header.put("X-Grafana-Org-Id", orgId);
        HttpResponse creResult = grafanaClient.doHttp("post", "/api/dashboards/db", template, header, null);
        cm.putHash(STORE_REGION_GODCOMPASSCACHE, STORE_KEY_GRAFANACONFIG, orgGroupKey, orgGroupValue);

        if (creResult.getStatusLine().getStatusCode() == 404) {
            createFailed = true;
        }
        return createFailed;
    }

    @SuppressWarnings({ "rawtypes" })
    private String getOrCreateOrgId(String contextPath, String appgroupName) {

        String orgId = "0";
        Map<String, String> orgIdMap = new HashMap<String, String>();
        orgIdMap.put("orgId", "0");
        orgIdMap.put("retCode", "0");
        Map<String, Map> paramsOrg = new HashMap<String, Map>();
        paramsOrg.put("orgIdMap", orgIdMap);
        final CountDownLatch latch = new CountDownLatch(1);
        GetOrgIdCallBack orgCallback = new GetOrgIdCallBack(paramsOrg, latch);
        grafanaClient.doHttp("get", "/api/orgs/name/" + EncodeHelper.urlEncode(appgroupName), null, orgCallback);
        try {
            latch.await();
        }
        catch (InterruptedException e) {
        }
        String retCodeGetOrgId = orgIdMap.get("retCode");
        if (retCodeGetOrgId.equals("200")) {// org存在
            orgId = orgIdMap.get("orgId");
        }
        else if (retCodeGetOrgId.equals("404")) {
            String data = "{" + "\"name\":" + "\"" + appgroupName + "\"" + "}";
            final CountDownLatch createOrgLatch = new CountDownLatch(1);
            CreateOrgCallBack createOrgCallback = new CreateOrgCallBack(paramsOrg, createOrgLatch);
            grafanaClient.doHttp("post", "/api/orgs", data, createOrgCallback);// 创建org
            try {
                createOrgLatch.await();
            }
            catch (InterruptedException e) {
            }
            if (orgIdMap.get("retCode").equals("200")) {
                orgId = orgIdMap.get("orgId");
                Map<String, Object> config = new HashMap<String, Object>();// 创建datasource
                config.put("name", "OPENTSDB");
                config.put("type", "opentsdb");
                config.put("database", "DATABASE");
                createDatasource(contextPath, orgId, config);
            }
        }
        return orgId;
    }

    @SuppressWarnings("rawtypes")
    private Map<String, String> getRetCodeAndOrgId(String appgroupName) {

        Map<String, String> orgIdMap = new HashMap<String, String>();
        orgIdMap.put("orgId", "0");
        orgIdMap.put("retCode", "0");
        Map<String, Map> paramsOrg = new HashMap<String, Map>();
        paramsOrg.put("orgIdMap", orgIdMap);
        final CountDownLatch latch = new CountDownLatch(1);
        GetOrgIdCallBack orgCallback = new GetOrgIdCallBack(paramsOrg, latch);
        grafanaClient.doHttp("get", "/api/orgs/name/" + EncodeHelper.urlEncode(appgroupName), null, orgCallback);

        try {
            latch.await();
        }
        catch (InterruptedException e) {
        }
        return orgIdMap;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map<String, String> getDashboardTemplate(String dashboardName, Map headerMap) {

        Map<String, String> dashboardTemplateMap = new HashMap<String, String>();
        dashboardTemplateMap.put("dashTempl", "000");
        dashboardTemplateMap.put("retCode", "0");
        Map<String, Map> paramsOrg = new HashMap<String, Map>();
        paramsOrg.put("dashTemplMap", dashboardTemplateMap);
        final CountDownLatch modifyLatch = new CountDownLatch(1);
        GetDashboardTemplateCallBack getDashboardCallback = new GetDashboardTemplateCallBack(paramsOrg, modifyLatch);
        grafanaClient.doHttp("get", "/api/dashboards/db/" + dashboardName, null, headerMap, getDashboardCallback);

        try {
            modifyLatch.await();
        }
        catch (InterruptedException e) {
        }
        return dashboardTemplateMap;
    }

    /**
     * 重新构造configMap
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map getValueObjNewMap(List<Map> configs) {

        Map<String, Map> valueObjNewMap = new HashMap<String, Map>();
        for (Map config : configs) {
            String appgroup = (String) config.get("appgroup");
            String appid = (String) config.get("appid");
            List appurls = (List) config.get("appurls");
            if (valueObjNewMap == null || !valueObjNewMap.keySet().contains(appgroup)) {
                Map<String, List> appidNewMap = new HashMap<String, List>();
                appidNewMap.put(appid, appurls);
                valueObjNewMap.put(appgroup, appidNewMap);
            }
            else {
                valueObjNewMap.get(appgroup).put(appid, appurls);
            }
        }
        return valueObjNewMap;
    }

    /**
     * 根据配置初始化dashboard模板
     */
    private String initTemplate(String appId, List<String> appurls) {

        StringBuilder appInstidsBuilder = new StringBuilder();
        StringBuilder serverInstidsBuilder = new StringBuilder();

        for (String appurl : appurls) {
            if (appurl.split("/").length == 4) {
                appInstidsBuilder.append(DataStoreHelper.encodeForOpenTSDB(appurl.substring(0, appurl.length() - 1)))
                        .append("|");
                String tempString = appurl.substring(0, appurl.lastIndexOf("/"));
                serverInstidsBuilder
                        .append(DataStoreHelper.encodeForOpenTSDB(tempString.substring(0, tempString.lastIndexOf("/"))))
                        .append("|");

            }
            else if (appurl.split("/").length == 3) {
                appInstidsBuilder.append(DataStoreHelper.encodeForOpenTSDB(appurl)).append("|");
                serverInstidsBuilder
                        .append(DataStoreHelper.encodeForOpenTSDB(appurl.substring(0, appurl.lastIndexOf("/"))))
                        .append("|");
            }

        }
        String appInstids = appInstidsBuilder.substring(0, appInstidsBuilder.length() - 1);
        appInstids = "(" + appInstids + ")" + "---" + appId;

        String serverInstids = serverInstidsBuilder.substring(0, serverInstidsBuilder.length() - 1);
        serverInstids = "(" + serverInstids + ")";

        String template = dashboardTemp.replace("TITLE", appId).replace("servIds", serverInstids).replace("appIds",
                appInstids);

        return JSONHelper.toString(template);
    }

    /**
     * 根据配置创建datasource
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void createDatasource(String contextPath, String orgId, Map config) {

        config.put("url", grafanaClient.getConfigValue("datasource." + config.get("type") + ".url"));
        Map<String, String> header = new HashMap<String, String>();
        if (datasourceTemp == null) {
            datasourceTemp = IOHelper.readTxtFile(contextPath + "/config/datasourceTemp.json", "UTF-8");
        }
        Map template = JSONHelper.toObject(datasourceTemp, Map.class);
        replaceMap(template, config);
        if (config != null) {
            header.put("X-Grafana-Org-Id", orgId);
            grafanaClient.doHttp("post", "/api/datasources", JSONHelper.toString(template), header, null);
        }
    }

    /**
     * 根据配置添加panel
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map addPanel(String panelType, Map template, Map panelConfig, String contextPath) {

        Map dashboard = (Map) template.get("dashboard");
        List<Map> rows = (List) dashboard.get("rows");

        // 计算新增panel id
        int maxid = 0;
        for (Map row : rows) {
            List<Map> panels = (List<Map>) row.get("panels");
            for (Map panel : panels) {
                int id = (int) panel.get("id");
                maxid = (id > maxid) ? id : maxid;
            }
        }
        panelConfig.put("id", maxid + 1);

        DSType datasource = null;
        int rowIndex = 0;
        switch (panelType) {
            case "service":
                datasource = DSType.OPENTSDB;
                rowIndex = 2;
                break;
            case "client":
                datasource = DSType.OPENTSDB;
                rowIndex = 3;
                break;
            case "custom":
                datasource = DSType.OPENTSDB;
                rowIndex = 4;
                break;
            case "log":
                datasource = DSType.ES;
                rowIndex = 5;
                break;
            case "ivc":
                datasource = DSType.ES;
                rowIndex = 6;
                break;
            default:
                break;
        }
        // 根据配置创建panel并添加到源dashboard中
        Map panelMap = buildPanel(contextPath, datasource, panelConfig);
        Map row = rows.get(rowIndex);
        List<Map> panels = (List<Map>) row.get("panels");
        panels.add(panelMap);
        return template;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Map deletePanel(String panelType, Map template, String url) {

        Map dashboard = (Map) template.get("dashboard");
        List<Map> rows = (List) dashboard.get("rows");
        int rowIndex = 0;
        switch (panelType) {
            case "service":
                rowIndex = 2;
                break;
            case "client":
                rowIndex = 3;
                break;
            case "custom":
                rowIndex = 4;
                break;
            case "log":
                rowIndex = 5;
                break;
            case "ivc":
                rowIndex = 6;
                break;
            default:
                break;

        }
        Map row = rows.get(rowIndex);
        List<Map> panelsList = (List<Map>) row.get("panels");
        Iterator<Map> it = panelsList.iterator();
        while (it.hasNext()) {
            Map panelMap = it.next();
            String panelName = (String) panelMap.get("title");
            if (panelName.contains(url + "---")) {
                it.remove();
            }
        }
        return template;
    }

    /**
     * 根据配置创建panel
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Map<String, Object> buildPanel(String contextPath, DSType dsType, Map configData) {

        Map<String, Object> template = null;
        switch (dsType) {
            case OPENTSDB:
                if (opentsdbPanelTemp == null) {
                    opentsdbPanelTemp = IOHelper.readTxtFile(contextPath + "/config/opentsdbPanelTemp.json", "UTF-8");
                }
                template = JSONHelper.toObject(opentsdbPanelTemp, Map.class);
                break;
            case ES:
                if (esPanelTemp == null) {
                    esPanelTemp = IOHelper.readTxtFile(contextPath + "/config/esPanelTemp.json", "UTF-8");
                }
                template = JSONHelper.toObject(esPanelTemp, Map.class);
                break;
            default:
                break;
        }
        replaceMap(template, configData);
        return template;
    }

    /**
     * 将config Map中的字段深度递归的替换到template Map中
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void replaceMap(Map template, Map config) {

        for (Object key : config.keySet()) {
            Object value = config.get(key);
            if (!template.containsKey(key)) {
                template.put(key, value);
                continue;
            }
            if (value instanceof Map) {
                replaceMap((Map<String, Object>) template.get(key), (Map<String, Object>) value);
            }
            else if (value instanceof List) {
                replaceList((List) template.get(key), (List) value);
            }
            else {
                template.put(key, value);
            }
        }
    }

    /**
     * 将config List中的元素深度递归的替换到template Map中
     * 
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void replaceList(List template, List config) {

        for (int i = 0; i < config.size(); i++) {
            Object value = config.get(i);

            if (value instanceof Map) {
                // when configList size is bigger than template,we copy the first obj in template to do the replace if
                // it exists
                if (template.size() <= i) {
                    if (template.size() == 0) {
                        template.add(value);
                        continue;
                    }
                    else {
                        String copy = JSONHelper.toString(template.get(0));
                        Map temp = JSONHelper.toObject(copy, Map.class);
                        template.add(temp);
                    }
                }
                replaceMap((Map) template.get(i), (Map<String, Object>) value);
            }
            else if (value instanceof List) {
                // when list in list and configList size is bigger, we just put it in template;
                if (template.size() <= i) {
                    template.add(value);
                    continue;
                }
                replaceList((List) template.get(i), (List) value);
            }
            else {
                if (template.size() <= i) {
                    template.add(value);
                    continue;
                }
                template.set(i, value);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void deleteDashboard(String data, CacheManager cm, final AsyncResponse response) {

        Map<String, String> dataMap = JSONHelper.toObject(data, Map.class);
        String orgName = dataMap.get("appgroup");
        String dashboardName = dataMap.get("appid");
        String orgGroupKey = orgName + "___" + dashboardName;

        Map<String, String> orgIdMap = getRetCodeAndOrgId(orgName);
        String resultMsg = "{\"code\":\"00\",\"msg\":\"该dashboard所属的org不存在,仅删除配置信息\"}";
        if (orgIdMap.get("retCode").equals("404")) {
            cm.delHash(STORE_REGION_GODCOMPASSCACHE, STORE_KEY_GRAFANACONFIG, orgGroupKey);
            if (response != null) {
                response.resume(resultMsg);
            }
        }
        else if (orgIdMap.get("retCode").equals("200")) {
            Map<String, String> deleteHeader = new HashMap<String, String>();
            deleteHeader.put("X-Grafana-Org-Id", orgIdMap.get("orgId"));
            HttpResponse resDel = grafanaClient.doHttp("delete", "/api/dashboards/db/" + dashboardName.toLowerCase(),
                    null, deleteHeader, null);

            if (resDel.getStatusLine().getStatusCode() == 200) {
                resultMsg = "{\"code\":\"00\",\"msg\":\"删除dashboard成功\"}";
                cm.delHash(STORE_REGION_GODCOMPASSCACHE, STORE_KEY_GRAFANACONFIG, orgGroupKey);
            }
            else if (resDel.getStatusLine().getStatusCode() == 404) {
                resultMsg = "{\"code\":\"00\",\"msg\":\"该dashboard不存在,仅删除配置信息\"}";
                cm.delHash(STORE_REGION_GODCOMPASSCACHE, STORE_KEY_GRAFANACONFIG, orgGroupKey);
            }
            if (response != null) {
                response.resume(resultMsg);
            }
        }

    }

    public String getDashboards(CacheManager cm) {

        Map<String, String> resultMap = cm.getHashAll(STORE_REGION_GODCOMPASSCACHE, STORE_KEY_GRAFANACONFIG);

        return JSONHelper.toString(resultMap);

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map modifyDashboardTemplate(Map template, Map existsMap, Map dataMapValueObj, String dashboardName,
            String contextPath, CacheManager cm) {

        String oldAppurlsName = JSONHelper.toString(existsMap.get("appurls"));

        String appurlsName = JSONHelper.toString(dataMapValueObj.get("appurls"));
        List<String> newAppurlsNameList = JSONHelper.toObject(appurlsName, List.class);
        Map<String, String> newAllInstidsPrefixMap = getAllInstidsPrefix(newAppurlsNameList);// 拿到最新的所有url前缀

        Map minusPanelsServiceMap = getMinusOrAddPanelsMap(existsMap, dataMapValueObj, "service");// 得到需要删除的ServicePanels
        if (minusPanelsServiceMap != null) {
            template = deletePanels(minusPanelsServiceMap, template, "service");// 删除ServicePanels
        }

        Map addPanelsServiceMap = getMinusOrAddPanelsMap(dataMapValueObj, existsMap, "service");// 得到需要添加的ServicePanels
        if (addPanelsServiceMap != null) {
            String newServicesPrefix = newAllInstidsPrefixMap.get("urlInstidsPrefix");// 得到serviceUrl前缀
            template = addPanels(addPanelsServiceMap, "service", template, newServicesPrefix, contextPath,
                    dashboardName);// 添加ServicePanels
        }

        Map minusPanelsClientMap = getMinusOrAddPanelsMap(existsMap, dataMapValueObj, "client");// 得到需要删除的ClientPanels
        if (minusPanelsClientMap != null) {
            template = deletePanels(minusPanelsClientMap, template, "client");// 删除ClientPanels
        }

        Map addPanelsClientMap = getMinusOrAddPanelsMap(dataMapValueObj, existsMap, "client");// 得到需要添加的ClientPanels
        if (addPanelsClientMap != null) {
            String newClientsPrefix = newAllInstidsPrefixMap.get("clientInstidsPrefix");// 得到clientUrl前缀
            template = addPanels(addPanelsClientMap, "client", template, newClientsPrefix, contextPath, dashboardName);// 添加ClientPanels
        }

        List<String> oldAppurlsNameList = JSONHelper.toObject(oldAppurlsName, List.class);
        template = addOrDeleteAppinstids(newAppurlsNameList, oldAppurlsNameList, template);// 替换所有的url前缀
        return template;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map getMinusOrAddPanelsMap(Map beforeMinusPanelsMap, Map afterMinusPanelsMap, String PanelType) {

        String bmMapString = JSONHelper.toString(beforeMinusPanelsMap.get(PanelType));
        Map<String, List> bmMap = JSONHelper.toObject(bmMapString, Map.class);
        String amMapString = JSONHelper.toString(afterMinusPanelsMap.get(PanelType));
        Map<String, List> amMap = JSONHelper.toObject(amMapString, Map.class);
        Map minusPanelsMap = getMinusResultMap(bmMap, amMap);// 得到Panels的差集
        return minusPanelsMap;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map getMinusResultMap(Map beforeMinusMap, Map afterMinusMap) {// 得到Map差集

        Map minusPanelsMap = new HashMap();
        if (beforeMinusMap != null && afterMinusMap != null) {
            Iterator minusSetIt = beforeMinusMap.keySet().iterator();
            while (minusSetIt.hasNext()) {
                String minusSetItString = JSONHelper.toString(minusSetIt.next());
                if (!afterMinusMap.keySet().contains(minusSetItString)) {
                    minusPanelsMap.put(minusSetItString, beforeMinusMap.get(minusSetItString));
                }
                else {
                    List minusPanelsList = getMinusList((List) beforeMinusMap.get(minusSetItString),
                            (List) afterMinusMap.get(minusSetItString));
                    minusPanelsMap.put(minusSetItString, minusPanelsList);

                }
            }
        }
        else if (beforeMinusMap != null && afterMinusMap == null) {
            minusPanelsMap = beforeMinusMap;
        }
        return minusPanelsMap;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List getMinusList(List beforeMinusList, List afterMinusList) {// 得到List差集

        List minusResultList = new ArrayList();
        if (beforeMinusList != null && afterMinusList != null) {
            for (int i = 0; i < beforeMinusList.size(); i++) {
                if (!afterMinusList.contains(beforeMinusList.get(i))) {
                    minusResultList.add(beforeMinusList.get(i));
                }
            }
        }
        else if (beforeMinusList != null && afterMinusList == null) {
            minusResultList = beforeMinusList;
        }
        return minusResultList;
    }

    @SuppressWarnings({ "rawtypes" })
    public void modifyDashboardCacheStore(Map existsMap, Map dataMapConfig, CacheManager cm, String owner) {

        String appurlsName = JSONHelper.toString(dataMapConfig.get("appurls"));
        String urlIds = JSONHelper.toString(dataMapConfig.get("service"));
        String clientIds = JSONHelper.toString(dataMapConfig.get("client"));
        String orgGroupKey = JSONHelper.toString(existsMap.get("orgGroupKey"));
        String timeStr = DateTimeHelper.dateFormat(new Date(), "yyyy-MM-dd HH:mm:ss");
        Map<String, String> valueObjMap = new HashMap<String, String>();
        valueObjMap.put("createtime", (String) existsMap.get("createtime"));
        valueObjMap.put("updatetime", timeStr);
        valueObjMap.put("owner", owner);
        valueObjMap.put("appurls", appurlsName);
        valueObjMap.put("service", urlIds);
        valueObjMap.put("client", clientIds);
        valueObjMap.put("orgGroupKey", orgGroupKey);

        String orgGroupValue = JSONHelper.toString(valueObjMap);
        cm.putHash(STORE_REGION_GODCOMPASSCACHE, STORE_KEY_GRAFANACONFIG, orgGroupKey, orgGroupValue);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map buildPanelConfigMap(String filterString, String metricString, String pannelTitleString) {

        Map<String, Object> configMap = new HashMap<String, Object>();
        List targetsList = new ArrayList<Map>();
        Map targetsMap = new HashMap<String, Object>();
        List filtersList = new ArrayList<Map>();
        Map filtersMap = new HashMap<String, Object>();

        filtersMap.put("filter", filterString);
        filtersList.add(filtersMap);
        targetsMap.put("filters", filtersList);
        targetsMap.put("metric", metricString);
        targetsList.add(targetsMap);
        configMap.put("targets", targetsList);
        configMap.put("title", pannelTitleString);
        return configMap;
    }

    @SuppressWarnings("rawtypes")
    public Map addPanels(Map plusMap, String panelType, Map template, String newPrefix, String contextPath,
            String appId) {

        Iterator plusSetIt = plusMap.keySet().iterator();
        while (plusSetIt.hasNext()) {
            List plusList = (List) plusMap.get(plusSetIt.next());
            for (int i = 0; i < plusList.size(); i++) {
                if (panelType.equals("service")) {
                    Map panelConfigTavg = buildPanelConfigMap(
                            newPrefix + DataStoreHelper.encodeForOpenTSDB("" + plusList.get(i)) + "$", "urlResp.tavg",
                            plusList.get(i) + "---服务访问平均响应时间");
                    addPanel(panelType, template, panelConfigTavg, contextPath);
                    Map panelConfigCount = buildPanelConfigMap(
                            newPrefix + DataStoreHelper.encodeForOpenTSDB("" + plusList.get(i)) + "$", "urlResp.count",
                            plusList.get(i) + "------------------服务访问量");
                    addPanel(panelType, template, panelConfigCount, contextPath);
                    Map panelConfigErr = buildPanelConfigMap(
                            newPrefix + DataStoreHelper.encodeForOpenTSDB("" + plusList.get(i)) + "$", "urlResp.err",
                            plusList.get(i) + "------------------服务错误数");
                    addPanel(panelType, template, panelConfigErr, contextPath);
                }
                else if (panelType.equals("client")) {
                    Map panelConfigTavg = buildPanelConfigMap(
                            newPrefix + DataStoreHelper.encodeForOpenTSDB("#" + appId + "#" + plusList.get(i)) + "$",
                            "clientResp.tavg", plusList.get(i) + "---调用平均响应时间");
                    addPanel(panelType, template, panelConfigTavg, contextPath);
                    Map panelConfigCount = buildPanelConfigMap(
                            newPrefix + DataStoreHelper.encodeForOpenTSDB("#" + appId + "#" + plusList.get(i)) + "$",
                            "clientResp.count", plusList.get(i) + "------------------调用量");
                    addPanel(panelType, template, panelConfigCount, contextPath);
                    Map panelConfigErr = buildPanelConfigMap(
                            newPrefix + DataStoreHelper.encodeForOpenTSDB("#" + appId + "#" + plusList.get(i)) + "$",
                            "clientResp.err", plusList.get(i) + "------------调用错误数");
                    addPanel(panelType, template, panelConfigErr, contextPath);
                }

            }
        }
        return template;
    }

    @SuppressWarnings("rawtypes")
    public Map deletePanels(Map minusMap, Map template, String panelType) {

        Iterator minusSetIt = minusMap.keySet().iterator();
        while (minusSetIt.hasNext()) {
            List minusList = (List) minusMap.get(minusSetIt.next());
            for (int i = 0; i < minusList.size(); i++) {
                deletePanel(panelType, template, (String) minusList.get(i));
            }
        }
        return template;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map addOrDeleteAppinstids(List<String> newAppurlsNameList, List<String> oldAppurlsNameList, Map template) {

        Map<String, String> oldAllInstidsPrefixMap = getAllInstidsPrefix(oldAppurlsNameList);
        Map<String, String> newAllInstidsPrefixMap = getAllInstidsPrefix(newAppurlsNameList);

        String templateString = JSONHelper.toString(template);
        String oldServerInstidsPrefix = JSONHelper.toString(oldAllInstidsPrefixMap.get("serverInstidsPrefix"));
        String oldAppInstidsPrefix = JSONHelper.toString(oldAllInstidsPrefixMap.get("appInstidsPrefix"));
        String oldUrlInstidsPrefix = JSONHelper.toString(oldAllInstidsPrefixMap.get("urlInstidsPrefix"));
        String oldClientInstidsPrefix = JSONHelper.toString(oldAllInstidsPrefixMap.get("clientInstidsPrefix"));

        String newServerInstidsPrefix = JSONHelper.toString(newAllInstidsPrefixMap.get("serverInstidsPrefix"));
        String newAppInstidsPrefix = JSONHelper.toString(newAllInstidsPrefixMap.get("appInstidsPrefix"));
        String newUrlInstidsPrefix = JSONHelper.toString(newAllInstidsPrefixMap.get("urlInstidsPrefix"));
        String newClientInstidsPrefix = JSONHelper.toString(newAllInstidsPrefixMap.get("clientInstidsPrefix"));

        String templateStringTemp = templateString.replace(oldServerInstidsPrefix, newServerInstidsPrefix)
                .replace(oldAppInstidsPrefix, newAppInstidsPrefix).replace(oldUrlInstidsPrefix, newUrlInstidsPrefix)
                .replace(oldClientInstidsPrefix, newClientInstidsPrefix);

        return JSONHelper.toObject(templateStringTemp, Map.class);
    }

    @SuppressWarnings("rawtypes")
    private Map getAllInstidsPrefix(List<String> appurlsNameList) {

        Map<String, String> allInstidsPrefixMap = new HashMap<String, String>();

        StringBuilder appInstidsBuilderPrefix = new StringBuilder();
        StringBuilder serverInstidsBuilderPrefix = new StringBuilder();
        StringBuilder clientInstidsBuilderPrefix = new StringBuilder();

        for (String appurl : appurlsNameList) {
            String appurlName = appurl.trim();
            if (appurlName.split("/").length == 4) {
                appInstidsBuilderPrefix
                        .append(DataStoreHelper.encodeForOpenTSDB(appurlName.substring(0, appurlName.length() - 1)))
                        .append("|");
                String tempString = appurlName.substring(0, appurlName.lastIndexOf("/"));
                serverInstidsBuilderPrefix
                        .append(DataStoreHelper.encodeForOpenTSDB(tempString.substring(0, tempString.lastIndexOf("/"))))
                        .append("|");
            }
            else if (appurlName.split("/").length == 3) {
                appInstidsBuilderPrefix.append(DataStoreHelper.encodeForOpenTSDB(appurlName)).append("|");
                serverInstidsBuilderPrefix
                        .append(DataStoreHelper.encodeForOpenTSDB(appurlName.substring(0, appurlName.lastIndexOf("/"))))
                        .append("|");

            }
            clientInstidsBuilderPrefix
                    .append(DataStoreHelper.encodeForOpenTSDB(appurlName.split("//")[1].split("/")[0])).append("|");
        }

        String appInstidsPrefix = appInstidsBuilderPrefix.substring(0, appInstidsBuilderPrefix.length() - 1);
        appInstidsPrefix = "(" + appInstidsPrefix + ")";

        String serverInstidsPrefix = serverInstidsBuilderPrefix.substring(0, serverInstidsBuilderPrefix.length() - 1);
        serverInstidsPrefix = "(" + serverInstidsPrefix + ")";

        String urlInstidsPrefix = appInstidsPrefix;

        String clientInstidsPrefix = clientInstidsBuilderPrefix.substring(0, clientInstidsBuilderPrefix.length() - 1);
        clientInstidsPrefix = "(" + clientInstidsPrefix + ")";

        allInstidsPrefixMap.put("appInstidsPrefix", appInstidsPrefix);
        allInstidsPrefixMap.put("serverInstidsPrefix", serverInstidsPrefix);
        allInstidsPrefixMap.put("urlInstidsPrefix", urlInstidsPrefix);
        allInstidsPrefixMap.put("clientInstidsPrefix", clientInstidsPrefix);

        return allInstidsPrefixMap;

    }

    @SuppressWarnings({ "rawtypes", "unchecked", "unused" })
    public void modifyDashboard(String data, String contextPath, HttpServletRequest request, CacheManager cm,
            AsyncResponse response) {

        Map<String, Object> dataMap = JSONHelper.toObject(data, Map.class);
        Map valueObjMap = (Map) dataMap.get("value");
        String orgName = JSONHelper.toString(dataMap.get("appgroup"));
        String dashboardName = JSONHelper.toString(dataMap.get("appid"));
        String appurls = JSONHelper.toString(valueObjMap.get("appurls"));

        String orgGroupKey = orgName + "___" + dashboardName;
        Map<String, String> cacheStoreMap = cm.getHash(STORE_REGION_GODCOMPASSCACHE, STORE_KEY_GRAFANACONFIG,
                orgGroupKey);
        Map existsMap = JSONHelper.toObject(cacheStoreMap.get(orgGroupKey), Map.class);

        Map<String, Object> params = new HashMap<String, Object>();
        HttpSession session = request.getSession(false);
        String user = "";
        if (session != null) {
            user = String.valueOf(session.getAttribute("apphub.gui.session.login.user.id"));
        }

        Map<String, String> orgIdMap = getRetCodeAndOrgId(orgName);// 获取orgId
        String resultMsg = "{\"code\":\"01\",\"msg\":\"该dashboard所属的org不存在,已删除配置信息，请重新创建\"}";
        if (orgIdMap.get("retCode").equals("404")) {
            cm.delHash(STORE_REGION_GODCOMPASSCACHE, STORE_KEY_GRAFANACONFIG, orgGroupKey);
        }
        else if (orgIdMap.get("retCode").equals("200")) {
            Map<String, String> modifyHeader = new HashMap<String, String>();
            modifyHeader.put("X-Grafana-Org-Id", orgIdMap.get("orgId"));

            Map<String, String> dashTemplMap = getDashboardTemplate(dashboardName, modifyHeader);// 获取该dashboard模板
            if (dashTemplMap.get("retCode").equals("200")) {

                Map template = JSONHelper.toObject(dashTemplMap.get("dashTempl"), Map.class);
                Map newDB = modifyDashboardTemplate(template, existsMap, valueObjMap, dashboardName, contextPath, cm);
                HttpResponse resultModify = grafanaClient.doHttp("post", "/api/dashboards/db",
                        JSONHelper.toString(newDB), modifyHeader, null);
                if (resultModify.getStatusLine().getStatusCode() == 200) {
                    resultMsg = "{\"code\":\"00\",\"msg\":\"修改dashboard成功\"}";
                    modifyDashboardCacheStore(existsMap, valueObjMap, cm, user);
                }
                else if (resultModify.getStatusLine().getStatusCode() == 404) {
                    resultMsg = "{\"code\":\"00\",\"msg\":\"修改dashboard失败\"}";
                }

            }
            else if (dashTemplMap.get("retCode").equals("404")) {
                resultMsg = "{\"code\":\"01\",\"msg\":\"该dashboard不存在,已删除配置信息，请重新创建\"}";
                cm.delHash(STORE_REGION_GODCOMPASSCACHE, STORE_KEY_GRAFANACONFIG, orgGroupKey);
            }

        }
        if (response != null) {
            response.resume(resultMsg);
        }

    }

}
