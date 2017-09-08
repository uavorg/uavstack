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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;

@SuppressWarnings("rawtypes")
public class GrafanaHttpCallBack implements HttpClientCallback {

    protected ISystemLogger logger = SystemLogger.getLogger(GrafanaHttpCallBack.class);
    protected String requestType = null;
    protected String requestUrl = null;
    protected String requestData = null;
    protected Map params = new HashMap();

    public GrafanaHttpCallBack() {
    }

    @SuppressWarnings("unchecked")
    public GrafanaHttpCallBack(Map _params) {
        if (null != _params && !_params.isEmpty()) {
            this.params.putAll(_params);
        }
    }

    public String getRequestType() {

        return requestType;
    }

    public void setRequestType(String requestType) {

        this.requestType = requestType;
    }

    public String getRequestUrl() {

        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {

        this.requestUrl = requestUrl;
    }

    public String getRequestData() {

        return requestData;
    }

    public void setRequestData(String requestData) {

        this.requestData = requestData;
    }

    @SuppressWarnings("unchecked")
    public void appendParams(Map _params) {

        if (null != _params && !_params.isEmpty()) {
            this.params.putAll(_params);
        }
    }

    public Map getParams() {

        return params;
    }

    @Override
    public void completed(HttpClientCallbackResult result) {

        String resp = result.getReplyDataAsString();// 此方法是流读取，只能获取一次
        completedLog(result, resp);
    }

    @Override
    public void failed(HttpClientCallbackResult result) {

        String resp = result.getReplyDataAsString();
        failedLog(result, resp);

    }

    protected void completedLog(HttpClientCallbackResult result, String respStr) {

        logger.info(this, getLogFormat(result, respStr));
    }

    protected void failedLog(HttpClientCallbackResult result, String respStr) {

        logger.err(this, getLogFormat(result, respStr), result.getException());
    }

    private String getLogFormat(HttpClientCallbackResult result, String respStr) {

        return "GrafanaHttpCallBack. " + " returnCode:" + result.getRetCode() + " requestType:" + requestType
                + " requestUrl:" + requestUrl + " requestData:" + requestData + " requestResp:" + respStr;
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
class ApiOrgsCallBack extends GrafanaHttpCallBack {

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
                GrafanaHttpUtils.doAsyncHttp("post", path, orgUserData, null);
                break;
            }
        }

    }

}

/**
 * 添加用户到所有组织架构
 *
 */
class ApiOrgsAllCallBack extends GrafanaHttpCallBack {

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
            GrafanaHttpUtils.doAsyncHttp("post", path, orgUserData, null);

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
class DelOrAddUserCallBack extends GrafanaHttpCallBack {

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
                GrafanaHttpUtils.doAsyncHttp("delete", path, null, new DelOrAddUserCallBack_2(this.params));
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
            GrafanaHttpUtils.doAsyncHttp("post", "/api/admin/users", userData, new AddUserCallBack());
        }
    }
}

/**
 * 添加用户
 *
 */
class DelOrAddUserCallBack_2 extends GrafanaHttpCallBack {

    @SuppressWarnings("rawtypes")
    public DelOrAddUserCallBack_2(Map _params) {
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
        GrafanaHttpUtils.doAsyncHttp("post", "/api/admin/users", userData, new AddUserCallBack());

    }

}

@SuppressWarnings("unchecked")
class AddUserCallBack extends GrafanaHttpCallBack {

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
        GrafanaHttpUtils.doAsyncHttp("get", path, null, new GetOrgsNameCallBack(resultMap));
    }
}

class GetOrgsNameCallBack extends GrafanaHttpCallBack {

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
        GrafanaHttpUtils.doAsyncHttp("delete", path, null, null);
    }
}

/**
 * 根据组织名获取组织id，并根据获取id的用途选择 </br>
 * 1.创建dashboard</br>
 * 2.创建datasource</br>
 * 3.获取dashboard添加panel
 */
class GetOrgIdbyNameCallBack extends GrafanaHttpCallBack {

    @SuppressWarnings("rawtypes")
    public GetOrgIdbyNameCallBack(Map _params) {
        super(_params);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void completed(HttpClientCallbackResult result) {

        String reStr = result.getReplyDataAsString();
        completedLog(result, reStr);

        if (result.getRetCode() == 200) {
            Map resultmap = JSONHelper.toObject(reStr, Map.class);
            Map dataMap = null;
            String orgId = String.valueOf(resultmap.get("id"));
            switch ((String) params.get("usage")) {
                case "dashboardCreate":
                    Map<String, String> header = new HashMap<String, String>();
                    header.put("X-Grafana-Org-Id", orgId);
                    GrafanaHttpUtils.doAsyncHttp("post", "/api/dashboards/db", (String) params.get("template"), header,
                            null);
                    break;
                case "datasourceCreate":
                    dataMap = (Map) params.get("dataMap");
                    DashboardManagement.getInstance().datasourceCreate((String) params.get("contextPath"), orgId,
                            (Map) dataMap.get("config"));
                    break;
                case "addPanel":
                    header = new HashMap<String, String>();
                    dataMap = (Map) params.get("dataMap");
                    header.put("X-Grafana-Org-Id", orgId);
                    params.put("orgId", orgId);
                    GrafanaHttpUtils.doAsyncHttp("get", "/api/dashboards/db/" + dataMap.get("dashboardName"), null,
                            header, new GetDashboardCallback(params));
                    break;

                default:
                    break;
            }

        }
        else if ("dashboardCreate".equals(params.get("usage"))) {
            String data = "{" + "\"name\":" + "\"" + params.get("orgName") + "\"" + "}";

            GrafanaHttpUtils.doAsyncHttp("post", "/api/orgs", data, new CreateOrgCallBack(params));
        }

    }

}

/**
 * 创建组织
 */
class CreateOrgCallBack extends GrafanaHttpCallBack {

    @SuppressWarnings("rawtypes")
    public CreateOrgCallBack(Map _params) {
        super(_params);
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public void completed(HttpClientCallbackResult result) {

        String reStr = result.getReplyDataAsString();
        completedLog(result, reStr);

        if (result.getRetCode() == 200) {
            Map resultmap = JSONHelper.toObject(reStr, Map.class);
            String orgId = String.valueOf(resultmap.get("id"));
            // createDatasource
            Map<String, Object> config = new HashMap<String, Object>();
            DashboardManagement.getInstance().datasourceCreate((String) params.get("contextPath"), orgId, config);
            // create dashboard
            Map<String, String> header = new HashMap<String, String>();
            header = new HashMap<String, String>();
            header.put("X-Grafana-Org-Id", orgId);
            GrafanaHttpUtils.doAsyncHttp("post", "/api/dashboards/db", (String) params.get("template"), header, null);

        }

    }
}

/**
 * 获取dashboard内容并添加panel
 */
class GetDashboardCallback extends GrafanaHttpCallBack {

    @SuppressWarnings("rawtypes")
    public GetDashboardCallback(Map _params) {
        super(_params);
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public void completed(HttpClientCallbackResult result) {

        String reStr = result.getReplyDataAsString();
        completedLog(result, reStr);

        if (result.getRetCode() == 200) {
            Map dataMap = (Map) params.get("dataMap");
            Map template = JSONHelper.toObject(reStr, Map.class);
            Map newDB = DashboardManagement.getInstance().addPanel((String) dataMap.get("panelType"), template,
                    (Map) dataMap.get("config"), (String) params.get("contextPath"));
            HashMap<String, String> header = new HashMap<String, String>();
            header.put("X-Grafana-Org-Id", (String) params.get("orgId"));
            GrafanaHttpUtils.doAsyncHttp("post", "/api/dashboards/db", JSONHelper.toString(newDB), header, null);
        }

    }

}
