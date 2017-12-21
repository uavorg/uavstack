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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.uav.apphub.core.AppHubBaseRestService;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.cache.api.CacheManagerFactory;

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

    private CacheManager cm = null;

    private DashboardManagement dashboardManagement = null;
    private GrafanaClient grafanaClient = null;

    @SuppressWarnings("unchecked")
    @Override
    protected void init() {

        grafanaClient = new GrafanaClient(request);
        dashboardManagement = new DashboardManagement();
        dashboardManagement.setGrafanaClient(grafanaClient);
        // cache manager
        String redisAddrStr = request.getServletContext().getInitParameter("uav.app.godeye.redis.store.addr");
        Map<String, Object> redisParamsMap = JSONHelper
                .toObject(request.getServletContext().getInitParameter("uav.app.godeye.redis.store.params"), Map.class);
        cm = CacheManagerFactory.build(redisAddrStr, Integer.valueOf(String.valueOf(redisParamsMap.get("min"))),
                Integer.valueOf(String.valueOf(redisParamsMap.get("max"))),
                Integer.valueOf(String.valueOf(redisParamsMap.get("queue"))),
                String.valueOf(redisParamsMap.get("pwd")));
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
        if (dashboardManagement.getGrafanaClient().getConfigValue("authorization.loginId").equals(userName)
                || "admin".equals(userName)) {
            userName = userName + "Apphub";
        }
        /**
         * check end
         */

        this.logger.info("GrafanaRestService register [delOrAddUser]", userName);
        dashboardManagement.delOrAddUser(userName, request);

        Map<String, String> userGroups = getGroupListByReqData(data);

        StringBuilder sb = new StringBuilder();
        sb.append("\r\n").append("register userName:").append(userName);
        sb.append("\r\n").append("register req:").append(data);
        sb.append("\r\n").append("register apiGroups:").append(userGroups.toString());
        this.logger.info("GrafanaRestService register [addUserOrgs]", sb.toString());

        List<GrafanaHttpCallBack> callbacks = new ArrayList<GrafanaHttpCallBack>();
        if ("ALL".equals(data)) {
            GrafanaHttpCallBack cbObj = dashboardManagement.addUserOrgsAll(userName, "Editor");
            callbacks.add(cbObj);
        }
        else {
            callbacks = dashboardManagement.addUserOrgs(userName, userGroups);
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
        loginInfo.put("url", dashboardManagement.getGrafanaClient().getConfigValue("web.url"));
        loginInfo.put("loginid", userName);
        loginInfo.put("password",
                dashboardManagement.getGrafanaClient().getConfigValue("authorization.register.defPwd"));

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

    /**
     * 根据配置创建dashboard
     */
    @POST
    @Path("dashboard/create")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void createDashboards(String data, @Suspended AsyncResponse response) {

        dashboardManagement.createDashboards(data,
                request.getServletContext().getRealPath("/uavapp_godcompass/grafana"), request, this.cm, response);
    }

    /**
     * 根据配置修改dashboard
     */
    @POST
    @Path("dashboard/modify")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void modifyDashboard(String data, @Suspended AsyncResponse response) {

        dashboardManagement.modifyDashboard(data, request.getServletContext().getRealPath("/uavapp_godcompass/grafana"),
                request, this.cm, response);
    }

    /**
     * 根据配置得到所有dashboards
     */
    @GET
    @Path("dashboard/getdashboards")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @SuppressWarnings("unchecked")
    public void getDashboards(@Suspended AsyncResponse response) {

        CacheManager cacheManager = this.cm;
        String result = dashboardManagement.getDashboards(cacheManager);
        Map resultMap = new HashMap();
        String codeString = "00";
        String msgString = "获取配置信息成功";
        if (StringHelper.isEmpty(result)) {
            codeString = "01";
            msgString = "获取配置信息失败或配置信息不存在";
        }
        resultMap.put("code", codeString);
        resultMap.put("msg", msgString);
        resultMap.put("data", result);
        String resultMsg = JSONHelper.toString(resultMap);
        response.resume(resultMsg);
    }

    /**
     * 根据配置删除dashboard
     */
    @POST
    @Path("dashboard/delete")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void deleteDashboard(String data, @Suspended AsyncResponse response) {

        dashboardManagement.deleteDashboard(data, this.cm, response);
    }

}
