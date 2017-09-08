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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.servlet.http.HttpSession;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

import com.creditease.agent.helpers.EncodeHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.uav.apphub.core.AppHubBaseRestService;

/**
 * 
 * GrafanaApi description: 所有api动作结果均见callback的log
 *
 */
@Singleton
@Path("grafana")
@SuppressWarnings("rawtypes")
public class GrafanaRestService extends AppHubBaseRestService {

    private enum code {
        success, fail
    }

    @Override
    protected void init() {

        GrafanaHttpUtils.init(request);
    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("register")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void register(String data, @Suspended AsyncResponse resp) {

        /**
         * check begin
         */
        if ("NOMAPPING".equals(data)) {
            resp.resume(createResult(code.fail, "nomapping."));
        }

        String userName = "";
        HttpSession session = request.getSession(false);
        if (null != session) {
            userName = String.valueOf(session.getAttribute("apphub.gui.session.login.user.id"));
        }

        if ("".equals(userName) || "null".equals(userName)) {
            resp.resume(createResult(code.fail, "not find userName."));
        }

        /**
         * 避免删除api admin用户
         */
        if (GrafanaHttpUtils.getConfigValue("authorization.loginId").equals(userName) || "admin".equals(userName)) {
            userName = userName + "Apphub";
        }
        /**
         * check end
         */

        this.logger.info("GrafanaRestService register [delOrAddUser]", userName);
        delOrAddUser(userName);

        Map<String, String> userGroups = getGroupListByReqData(data);

        StringBuilder sb = new StringBuilder();
        sb.append("\r\n").append("register userName:").append(userName);
        sb.append("\r\n").append("register req:").append(data);
        sb.append("\r\n").append("register apiGroups:").append(userGroups.toString());
        this.logger.info("GrafanaRestService register [addUserOrgs]", sb.toString());

        List<GrafanaHttpCallBack> callbacks = new ArrayList<GrafanaHttpCallBack>();
        if ("ALL".equals(data)) {
            GrafanaHttpCallBack cbObj = addUserOrgsAll(userName, "Editor");
            callbacks.add(cbObj);
        }
        else {
            callbacks = addUserOrgs(userName, userGroups);
        }

        /**
         * 获取回调参数，判断grafana是否已经有用户权限下的组织，有的话默认获取一个，用于前台页面跳转
         */
        Map josnResp = createGarfanaInfo(userName);
        for (GrafanaHttpCallBack callback : callbacks) {
            if (callback.getParams().containsKey("switchOrgId")) {
                String switchOrgId = String.valueOf(callback.getParams().get("switchOrgId"));
                josnResp.put("switchOrgId", switchOrgId);
                break;
            }
        }

        resp.resume(createResult(code.success, "", josnResp));
    }

    private Map<String, String> getGroupListByReqData(String reqData) {

        if ("ALL".equals(reqData)) {
            return Collections.emptyMap();
        }

        String json = JSONHelper.toString(reqData);
        Map<String, String> reuslt = new HashMap<String, String>();
        List<Map> reqs = JSONHelper.toObjectArray(json, Map.class);
        for (Map req : reqs) {
            Map groups = JSONHelper.toObject(String.valueOf(req.get("groupList")), Map.class);
            mergeGroup(reuslt, groups);
        }
        return reuslt;
    }

    /**
     * 将现有的数据填充：如果不存在则添加，如果存在已权限大的为准
     * 
     * @param result
     * @param info
     * @return
     */
    private void mergeGroup(Map<String, String> result, Map info) {

        Iterator i = info.keySet().iterator();
        while (i.hasNext()) {
            String addkey = String.valueOf(i.next());
            String body = String.valueOf(info.get(addkey));

            /**
             * body为空,没分配权限 = 添加最小权限
             */
            if ("{}".equals(body) && !result.containsKey(addkey)) {
                result.put(addkey, "Viewer"); // 只读
                continue;
            }

            /**
             * body为空，已分配权限 = 不做任何处理
             */
            if ("{}".equals(body) && result.containsKey(addkey)) {
                continue;
            }

            /**
             * 已分配编辑权限 = 不做任何处理
             */
            if (result.containsKey(addkey) && "Editor".equals(result.get(addkey))) {
                continue;
            }

            /**
             * 获取body权限
             */
            String grafana = "grafana";
            String role = "Viewer";
            Map bodyMap = JSONHelper.toObject(String.valueOf(info.get(addkey)), Map.class);
            if (!bodyMap.containsKey(grafana)) {
                role = "Viewer"; // 只读
            }
            else if (bodyMap.containsKey(grafana) && "T".equals(String.valueOf(bodyMap.get(grafana)))) {
                role = "Editor"; // 编辑
            }
            else if (bodyMap.containsKey(grafana) && !"T".equals(String.valueOf(bodyMap.get(grafana)))) {
                role = "Viewer"; // 只读
            }

            /**
             * 还没有分配权限 = 直接分配
             */
            if (!result.containsKey(addkey)) {
                result.put(addkey, role);
                continue;
            }

            /**
             * 已经分配只读，body为编辑 = 修改为最高权限
             */
            if (result.containsKey(addkey) && "Viewer".equals(result.get(addkey)) && "Editor".equals(role)) {
                result.put(addkey, "Editor");
                continue;
            }

            /**
             * 已经分配只读，body为只读 = 不做任何处理
             */
            if (result.containsKey(addkey) && "Viewer".equals(result.get(addkey)) && "Viewer".equals(role)) {
                continue;
            }

        }

    }

    private String createResult(code code, String msg) {

        return createResult(code, msg, null);
    }

    private Map createGarfanaInfo(String userName) {

        Map<String, Object> loginInfo = new HashMap<String, Object>();
        loginInfo.put("url", GrafanaHttpUtils.getConfigValue("web.url"));
        loginInfo.put("loginid", userName);
        loginInfo.put("password", GrafanaHttpUtils.getConfigValue("authorization.register.defPwd"));

        return loginInfo;
    }

    private String createResult(code code, String msg, Object data) {

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("code", code.toString());
        result.put("msg", msg);
        if (null != data) {
            result.put("data", data);
        }

        return JSONHelper.toString(result);
    }

    private void threadSleep() {

        try {
            Thread.sleep(Long.valueOf(GrafanaHttpUtils.getConfigValue("authorization.register.sleep.time")));
        }
        catch (InterruptedException e) {
            this.logger.err(this, e.getMessage(), e);
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

            GrafanaHttpUtils.doAsyncHttp("get", "/api/orgs", null, callback);
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
        GrafanaHttpUtils.doAsyncHttp("get", "/api/orgs", null, callback);

        threadSleep();
        return callback;
    }

    /**
     * 存在则删除，不存在直接添加
     * 
     * @param userName
     */
    @SuppressWarnings("unchecked")
    public void delOrAddUser(String userName) {

        Map params = new HashMap();
        params.put("request", request);
        params.put("userName", userName);
        params.put("password", GrafanaHttpUtils.getConfigValue("authorization.register.defPwd")); // 自动注册用户密码都统一一样

        // 获取用户信息：回调处理详情见ApiUsersCallBack
        GrafanaHttpUtils.doAsyncHttp("get", "/api/users", null, new DelOrAddUserCallBack(params));
        threadSleep();
    }

    /**
     * 根据配置创建dashboard
     */
    @POST
    @Path("dashboard/create")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public String dashboardCreate(String data) {

        DashboardManagement.getInstance().dashboardCreate(data,
                request.getServletContext().getRealPath("/uavapp_godcompass/grafana"));
        return createResult(code.success, ",");
    }

    /**
     * 根据配置在指定组织中创建datasource
     */
    @POST
    @Path("dashboard/datasource/create")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public String datasourceCreate(String data) {

        @SuppressWarnings("unchecked")
        Map<String, String> dataMap = JSONHelper.toObject(data, Map.class);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contextPath", request.getServletContext().getRealPath("/uavapp_godcompass/grafana"));
        params.put("usage", "datasourceCreate");
        params.put("dataMap", dataMap);
        GrafanaHttpUtils.doAsyncHttp("get", "/api/orgs/name/" + EncodeHelper.urlEncode(dataMap.get("orgName")), null,
                new GetOrgIdbyNameCallBack(params));

        return createResult(code.success, ",");
    }

    /**
     * 根据配置添加panel到指定dashboard
     */
    @POST
    @Path("dashboard/addpanel")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public String addPanel(String data) {

        @SuppressWarnings("unchecked")
        Map<String, String> dataMap = JSONHelper.toObject(data, Map.class);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contextPath", request.getServletContext().getRealPath("/uavapp_godcompass/grafana"));
        params.put("usage", "addPanel");
        params.put("dataMap", dataMap);
        GrafanaHttpUtils.doAsyncHttp("get", "/api/orgs/name/" + EncodeHelper.urlEncode(dataMap.get("orgName")), null,
                new GetOrgIdbyNameCallBack(params));

        return createResult(code.success, ",");
    }
}
