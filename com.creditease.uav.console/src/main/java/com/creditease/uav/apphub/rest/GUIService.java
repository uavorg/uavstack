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

package com.creditease.uav.apphub.rest;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

import com.creditease.agent.helpers.EncryptionHelper;
import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.NetworkHelper;
import com.creditease.agent.helpers.PropertiesHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.spi.ComponentMonitor;
import com.creditease.uav.apphub.core.AppHubBaseRestService;
import com.creditease.uav.apphub.core.AppHubInit;
import com.creditease.uav.apphub.sso.GUISSOClient;
import com.creditease.uav.apphub.sso.GUISSOClientFactory;
import com.creditease.uav.apphub.tools.CaptchaTools;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.cache.api.CacheManagerFactory;
import com.creditease.uav.exception.ApphubException;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;

/**
 * GUIService 为前端GUI提供Restful Service：提供界面渲染和展现所需要的数据
 *
 * @author zhen zhang
 */
@Singleton
@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Path("gui")
public class GUIService extends AppHubBaseRestService {

    private static final String METRIC_24HOURUSERLOGIN = "M24hr_UsrLog";

    // code
    public enum RespCode {
        FAIL, SUCCESS, NOLOGIN
    }

    private HashMap<String, String> whiteList = new HashMap<String, String>();

    private HashMap<String, String> guiTempCache = new HashMap<String, String>();

    private String appBaseUrl;

    protected CacheManager cm = null;

    private ComponentMonitor monitor = ComponentMonitor.getMonitor("GUIService");

    @SuppressWarnings("unchecked")
    @Override
    public void init() {

        String port = request.getServletContext().getInitParameter("uav.server.port");
        if (port == null) {
            port = String.valueOf(request.getServerPort());
        }
        appBaseUrl = request.getScheme() + "://" + NetworkHelper.getLocalIP() + ":" + port + "/"
                + getWebProName(request) + "/";

        // redis
        String redisAddrStr = request.getServletContext().getInitParameter("uav.app.gui.redis.store.addr");
        Map<String, Object> redisParamsMap = JSONHelper
                .toObject(request.getServletContext().getInitParameter("uav.app.gui.redis.store.params"), Map.class);
        cm = CacheManagerFactory.build(redisAddrStr, Integer.valueOf(String.valueOf(redisParamsMap.get("min"))),
                Integer.valueOf(String.valueOf(redisParamsMap.get("max"))),
                Integer.valueOf(String.valueOf(redisParamsMap.get("queue"))),
                String.valueOf(redisParamsMap.get("pwd")));

        // http client
        Map<String, Integer> httpParamsMap = JSONHelper
                .toObject(request.getServletContext().getInitParameter("uav.app.gui.http.client.params"), Map.class);
        initHttpClient(httpParamsMap.get("max.con"), httpParamsMap.get("max.tot.con"),
                httpParamsMap.get("sock.time.out"), httpParamsMap.get("con.time.out"),
                httpParamsMap.get("req.time.out"));

        // apphub 默认权限自动注册
        AppHubInit appHubInit = new AppHubInit(this);
        appHubInit.run();

        // 白名单
        String whiteListStr = request.getServletContext().getInitParameter("uav.apphub.sso.white.list");
        whiteList = JSONHelper.toObject(whiteListStr, HashMap.class);

        // memory cache init
        createTempCache(request, "login");
        createTempCache(request, "loginOut");
        createTempCache(request, "main");
        createTempCache(request, "app");
        createMainMenuCache(request);

        try {
            /**
             * redis cache init :等待AppHubInit执行完成（1秒为估计值）
             */
            Thread.sleep(1000L);
            /**
             * 先清空redis：app group 授权信息
             */
            cm.del("apphub.gui.cache", "manage.app");
            cm.del("apphub.gui.cache", "manage.group");
            /**
             * 然后将mongodb数据缓存到redis
             */
            createGroupInfoCache();
            createAppInfoCache();
        }
        catch (Exception e) {
            logger.err(this, "UAV管理信息 redis cache init FAIL: ", e);
        }

        // 设置Monitor指标
        monitor.setValueSumBySeconds(METRIC_24HOURUSERLOGIN, (long) 24 * 3600);
    }

    /**
     * 获取模板
     * 
     * @param tempName
     * @return
     */
    @GET
    @Path("loadTemp")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public String loadTemp(@QueryParam("tempName") String tempName) throws Exception {

        StringBuilder tempHtml = new StringBuilder();
        HttpSession session = request.getSession(false);
        Object userId = null == session ? null : session.getAttribute("apphub.gui.session.login.user.id");
        if (null != userId) {
            Object userGroup = null == session ? null : session.getAttribute("apphub.gui.session.login.user.group");
            Object emailList = null == session ? null : session.getAttribute("apphub.gui.session.login.user.emailList");
            Object emailAuthList = null == session ? null
                    : session.getAttribute("apphub.gui.session.login.user.authorize.emailList");
            Object systemAuthList = null == session ? null
                    : session.getAttribute("apphub.gui.session.login.user.authorize.systems");
            
            /**
             * 模板中，自动填充登录用户信息：模板内spa页面可以获取此资源;
             */
            String userJs = "<script> var loginUser = {" + "'userId':'" + userId + "','groupId':'" + userGroup
                    + "','emailList':'" + emailList + "','emailAuthList':'" + emailAuthList + "','systemAuthList':'"
                    + systemAuthList + "'};</script>";
            tempHtml.append(userJs);

        }

        tempHtml.append(guiTempCache.get("apphub.gui.cache.temp." + tempName));

        return createResponeJson(RespCode.SUCCESS, "", tempHtml.toString());
    }

    /**
     * 登录
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    @POST
    @Path("login")
    @Produces(MediaType.TEXT_HTML + ";charset=utf-8")
    public String login(String userLoginInfoJson) {

        Map<String, String> input = JSONHelper.toObject(userLoginInfoJson, Map.class);
        String loginId = input.get("loginId");
        String loginPwd = input.get("loginPwd");
        String captcha = input.get("captcha");
        CaptchaTools vcTools = new CaptchaTools();
        if (!vcTools.checkVCAnswer(request.getSession(), captcha)) {
            logger.info(this, "uav apphub login fail. info=验证信息错误,loginId=" + loginId);
            return createResponeJson(RespCode.FAIL, "验证信息错误", "");
        }
        else if (loginRegister(loginId, loginPwd, request)) {
            monitor.increValue(METRIC_24HOURUSERLOGIN);
            monitor.flushToSystemProperties();

            String ip = request.getRemoteAddr();
            String xip = request.getHeader("X-Forwarded-For");
            String userip = getClientIP(ip, xip);
            Map<String, String> userInfo = new HashMap<String, String>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String time = sdf.format(new Date());
            userInfo.put("key", "ulog");
            userInfo.put("time", time);
            userInfo.put("uid", loginId);
            userInfo.put("uip", userip);
            userInfo.put("rs", "/rs/gui/login");
            userInfo.put("type", "login");
            userInfo.put("url", "");
            userInfo.put("desc", "登录成功");
            userInfo.put("authemails", String.valueOf(
                    request.getSession(false).getAttribute("apphub.gui.session.login.user.authorize.emailList")));
            userInfo.put("authsystems", String.valueOf(
                    request.getSession(false).getAttribute("apphub.gui.session.login.user.authorize.systems")));
            logger.info(this, JSONHelper.toString(userInfo));

            return createResponeJson(RespCode.SUCCESS, "", "");
        }
        else {
            logger.info(this, "uav apphub login fail. info=账号或密码错误,loginId=" + loginId);
            return createResponeJson(RespCode.FAIL, "账号或密码错误", "");
        }
    }

    /**
     * 退出
     *
     * @return
     */
    @POST
    @Path("loginOut")
    @Produces(MediaType.TEXT_HTML + ";charset=utf-8")
    public void loginOut() {

        HttpSession session = request.getSession(false);
        if (null != session) {

            Object obj = request.getSession(false).getAttribute("apphub.gui.session.login.user.id");
            String userId = null == obj ? "" : String.valueOf(obj);
            String ip = request.getRemoteAddr();
            String xip = request.getHeader("X-Forwarded-For");
            String userip = getClientIP(ip, xip);
            Map<String, String> userInfo = new HashMap<String, String>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String time = sdf.format(new Date());
            userInfo.put("key", "ulog");
            userInfo.put("time", time);
            userInfo.put("uid", userId);
            userInfo.put("uip", userip);
            userInfo.put("rs", "/rs/gui/loginOut");
            userInfo.put("type", "logout");
            userInfo.put("url", "");
            userInfo.put("desc", "登出成功");
            userInfo.put("authemails", String.valueOf(
                    request.getSession(false).getAttribute("apphub.gui.session.login.user.authorize.emailList")));
            userInfo.put("authsystems", String.valueOf(
                    request.getSession(false).getAttribute("apphub.gui.session.login.user.authorize.systems")));
            logger.info(this, JSONHelper.toString(userInfo));

            // 销毁会话
            session.invalidate();

        }
    }

    /**
     * 跳转main页面
     */
    @GET
    @Path("jumpMainPage")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void jumpMainPage() throws IOException {

        String url = "/" + getWebProName(request) + "/" + "main.html";
        response.sendRedirect(url);
    }

    /**
     * 跳转APP页面
     *
     * @param APPID
     */
    @GET
    @Path("jumpAppPage")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void jumpAppPage() throws IOException {

        String url = "/" + getWebProName(request) + "/" + "app.html";
        response.sendRedirect(url);
    }

    /**
     * 获取主页菜单
     *
     * @return
     */
    @GET
    @Path("loadMainMenu")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public String loadMainMenu() throws IOException {

        return createResponeJson(RespCode.SUCCESS, "", guiTempCache.get("apphub.gui.cache.menu.main"));
    }

    /**
     * 获取主页用户管理信息
     * 
     * @return
     */
    @SuppressWarnings("rawtypes")
    @GET
    @Path("loadUserManageInfo")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void loadUserManageInfo(@Suspended AsyncResponse response) {

        String userGroup = String
                .valueOf(request.getSession(false).getAttribute("apphub.gui.session.login.user.group"));
        Map<String, String> manageGroup = cm.getHashAll("apphub.gui.cache", "manage.group");

        /**
         * 获取用户ldap信息 begin
         */
        GUISSOClient guissoClient = GUISSOClientFactory.getGUISSOClient(request);
        String userLoginId = String.valueOf(request.getSession(false).getAttribute("apphub.gui.session.login.user.id"));
        Object userLdapInfo = guissoClient.getUserByQuery(userLoginId);
        String userLdapInfoStr = JSONHelper.toString(userLdapInfo);
        /**
         * 获取用户ldap信息 end
         */

        /**
         * 获取匹配授权信息，去重
         * 
         * 只要匹配上，权限都能得到
         */
        Set<String> appids = new HashSet<String>();
        Iterator manageGroupI = manageGroup.keySet().iterator();
        while (manageGroupI.hasNext()) {
            String key = String.valueOf(manageGroupI.next());
            String[] keys = key.split(",");
            String groupId = keys[0].replace("groupId:", "");
            String ldapKey = keys[1].replace("ldappKey:", "");

            boolean keyCheck = false;
            if (ldapKey.length() == 0) {
                keyCheck = true; // 没有则不验证
            }
            else {
                keyCheck = userLdapInfoStr.contains(ldapKey); // 关键字匹配
            }

            if (userGroup.equals(groupId) && keyCheck) {
                // 授权匹配
                String[] userManAppids = manageGroup.get(key).split(",");
                for (String appid : userManAppids) {
                    appids.add(appid);
                }
            }
        }

        String result = "";
        if (appids.size() > 0) {
            // 赋值
            Iterator appidsI = appids.iterator();
            Map<String, String> userManageInfo = new HashMap<String, String>();
            while (appidsI.hasNext()) {
                String appid = String.valueOf(appidsI.next());
                Map<String, String> appinfo = cm.getHash("apphub.gui.cache", "manage.app", appid);
                userManageInfo.putAll(appinfo);
            }

            result = JSONHelper.toString(userManageInfo);
        }

        logger.info(this, "\r\nloadUserManageInfo->\r\nuserGroup:" + userGroup + "\r\nmapping:" + appids.toString());

        response.resume(result);

    }

    /**
     * 获取验证码
     *
     * @return
     */
    @GET
    @Path("vc/new")
    public void createValidataCode() throws Exception {

        CaptchaTools vcTools = new CaptchaTools();
        vcTools.newVC(request.getSession(), response.getOutputStream());
    }
    
    /**
     * 获取apphub相关信息，并且经行AES CBC加密
     *
     * 目前封装信息有：用户信息
     * 
     * @return
     */
    @GET
    @Path("loadApphubInfoByAES")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public String loadApphubInfoByAES() throws IOException {

        HttpSession session = request.getSession(false);
        Object userId = null == session ? null : session.getAttribute("apphub.gui.session.login.user.id");
        Map<String, String> apphubInfo = new HashMap<String, String>();
        if (null != userId) {
            Object userGroup = null == session ? null : session.getAttribute("apphub.gui.session.login.user.group");
            Object emailList = null == session ? null : session.getAttribute("apphub.gui.session.login.user.emailList");
            Object emailAuthList = null == session ? null
                    : session.getAttribute("apphub.gui.session.login.user.authorize.emailList");
            Object systemAuthList = null == session ? null
                    : session.getAttribute("apphub.gui.session.login.user.authorize.systems");

            apphubInfo.put("userId", String.valueOf(userId));
            apphubInfo.put("groupId", String.valueOf(userGroup));
            apphubInfo.put("emailList", String.valueOf(emailList));
            apphubInfo.put("emailAuthList", String.valueOf(emailAuthList));
            apphubInfo.put("systemAuthList", String.valueOf(systemAuthList));
            apphubInfo.put("timeStamp", String.valueOf(new Date().getTime()));

        }

        String strMsg = JSONHelper.toString(apphubInfo);
        String resultStr = EncryptionHelper.encryptByAesCBC(strMsg, "UavappHubaEs_keY", false, "UavappHubaEs_keY",
                "utf-8");
        return createResponeJson(RespCode.SUCCESS, "", resultStr);
    }
    
    // ------------------------------------------------以下为api支持----------------------------------------------------------->

    /**
     * 获取Web项目发布名称
     * 
     * @param request
     * @return
     */
    protected String getWebProName(HttpServletRequest request) {

        String requestUrl = request.getRequestURI().substring(1);
        int index = requestUrl.indexOf("/");
        return requestUrl.substring(0, index);
    }

    /**
     * 登录并且注册会话,保存会话信息
     * 
     * @param loginId
     * @param loginPwd
     * @param request
     * @return （false：失败，true：成功）
     */
    protected boolean loginRegister(String loginId, String loginPwd, HttpServletRequest request) {

        GUISSOClient guissoClient = GUISSOClientFactory.getGUISSOClient(request);
        Map<String, String> userInfo = guissoClient.getUserByLogin(loginId, loginPwd);

        // 登录成功，sesion入口，保存会话信息
        if (null != userInfo && !userInfo.isEmpty()) {

            // 用户信息会话创建，信息保存
            HttpSession session = request.getSession();

            // 白名单
            whiteListFilter(loginId, userInfo);

            session.setAttribute("apphub.gui.session.login.user.id", userInfo.get("loginId"));
            session.setAttribute("apphub.gui.session.login.user.group", userInfo.get("groupId"));
            session.setAttribute("apphub.gui.session.login.user.emailList", userInfo.get("emailList"));
            setUAuthInfoToSession();
            return true;
        }
        else {
            return false;
        }
    }

    protected String createResponeJson(RespCode respCode, String respMsg, Object respData) {

        HashMap<String, Object> resp = new HashMap<String, Object>();
        resp.put("CODE", respCode);
        resp.put("MSG", respMsg);
        resp.put("DATA", respData);
        return JSONHelper.toString(resp);
    }

    // ----------------cache业务处理 BEGIN----------------------------------------------------->

    /**
     * 白名单过滤
     * 
     * @param loginId
     * @param userInfo
     */
    private void whiteListFilter(String loginId, Map<String, String> userInfo) {

        if (null != whiteList && !whiteList.isEmpty()) {

            for (String groupName : whiteList.keySet()) {

                boolean exists = whiteListSet(loginId, groupName, userInfo);
                if (exists) {
                    break;
                }
            }
        }

    }

    /**
     * 白名单设置
     * 
     * @param memberNames
     * @param groupName
     * @param userInfo
     * @return
     */
    private boolean whiteListSet(String loginId, String groupName, Map<String, String> userInfo) {
 
        boolean result = false;
        String[] memberNames = whiteList.get(groupName).split(",");
        for (String memberName : memberNames) {
            if (loginId.toLowerCase().equals(memberName.toLowerCase())) {
                userInfo.remove("groupId");
                userInfo.put("groupId", groupName);
                result = true;
                break;
            }
        }

        return result;
    }

    private String createTempCache(HttpServletRequest request, String type) {

        String tempPath = null;
        switch (type) {
            case "login":
                tempPath = "/apphub/temp/login_temp.html";
                break;
            case "loginOut":
                tempPath = "/apphub/temp/login_temp.html";
                break;
            case "main":
                tempPath = "/apphub/temp/main_temp.html";
                break;
            case "app":
                tempPath = "/apphub/temp/app_temp.html";
                break;
            default:
                tempPath = "/apphub/temp/login_temp.html";
                break;
        }

        String tempAbsPath = request.getServletContext().getRealPath("/") + tempPath;
        String tempMsg = IOHelper.readTxtFile(tempAbsPath, "UTF-8");

        guiTempCache.put("apphub.gui.cache.temp." + type, tempMsg);

        return tempMsg;
    }

    private String createMainMenuCache(HttpServletRequest request) {

        // 读取资源
        Properties properties = new Properties();
        InputStream input = null;
        InputStreamReader in = null;
        try {
            String mainPath = request.getServletContext().getRealPath("") + "/uavapp_main/config.properties";
            input = new BufferedInputStream(new FileInputStream(mainPath));
            in = new InputStreamReader(input, "UTF-8");
            properties.load(in);
        }
        catch (Exception e) {
            logger.err(this, "Error :" + e.getMessage(), e);
        }
        finally {
            try {
                in.close();
                input.close();
            }
            catch (Exception e) {
                logger.err(this, "Error :" + e.getMessage(), e);
            }
        }

        // format 主页菜单
        String mainMenusStr = properties.getProperty("app.menu");
        @SuppressWarnings("rawtypes")
        List<Map> mainMenusJsonArr = JSONHelper.toObjectArray(mainMenusStr, Map.class);

        // 标题
        String mainTitleStr = properties.getProperty("app.title");

        // 打包
        Map<String, Object> resultJson = new LinkedHashMap<String, Object>();
        resultJson.put("menu", mainMenusJsonArr);
        resultJson.put("title", mainTitleStr);
        String result = JSONHelper.toString(resultJson);
        guiTempCache.put("apphub.gui.cache.menu.main", result);

        return result;
    }

    private void createGroupInfoCache() {

        Map<String, Object> where = new HashMap<String, Object>();
        where.put("state", 1);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("where", where);
        String dataStr = JSONHelper.toString(data);
        Map<String, Object> jsonRequest = new HashMap<String, Object>();
        jsonRequest.put("type", "query");
        jsonRequest.put("data", dataStr);

        HashMap<String, String> dbInfo = new HashMap<String, String>();
        dbInfo.put("dataStoreName", "AppHub.group");
        dbInfo.put("conllectionName", "uav_groupinfo");

        UAVHttpMessage request = new UAVHttpMessage();
        request.putRequest("mrd.data", JSONHelper.toString(jsonRequest));
        request.putRequest("datastore.name", dbInfo.get("dataStoreName"));
        request.putRequest("mgo.coll.name", dbInfo.get("conllectionName"));
        String jsonStr = JSONHelper.toString(request);
        byte[] datab = null;
        try {
            datab = jsonStr.getBytes("utf-8");
        }
        catch (Exception e) {
            logger.err(this, "GUIService createGroupInfoCache \n" + e.getMessage());
            throw new ApphubException(e);
        }
        logger.info(this, "GUIService createGroupInfoCache");

        GroupCacheCallBack groupCacheCb = new GroupCacheCallBack();
        doHttpPost("uav.app.manage.apphubmanager.http.addr", "/ah/group", datab, "application/json", "utf-8",
                groupCacheCb);

    }

    private void createAppInfoCache() {

        Map<String, Object> where = new HashMap<String, Object>();
        where.put("state", 1);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("where", where);
        String dataStr = JSONHelper.toString(data);
        Map<String, Object> jsonRequest = new HashMap<String, Object>();
        jsonRequest.put("type", "query");
        jsonRequest.put("data", dataStr);

        HashMap<String, String> dbInfo = new HashMap<String, String>();
        dbInfo.put("dataStoreName", "AppHub.app");
        dbInfo.put("conllectionName", "uav_appinfo");

        UAVHttpMessage request = new UAVHttpMessage();
        request.putRequest("mrd.data", JSONHelper.toString(jsonRequest));
        request.putRequest("datastore.name", dbInfo.get("dataStoreName"));
        request.putRequest("mgo.coll.name", dbInfo.get("conllectionName"));
        String jsonStr = JSONHelper.toString(request);
        byte[] datab = null;
        try {
            datab = jsonStr.getBytes("utf-8");
        }
        catch (Exception e) {
            logger.err(this, "GUIService createAppInfoCache \n" + e.getMessage());
            throw new ApphubException(e);
        }
        logger.info(this, "GUIService createAppInfoCache");

        AppCacheCallBack appCacheCb = new AppCacheCallBack();
        doHttpPost("uav.app.manage.apphubmanager.http.addr", "/ah/app", datab, "application/json", "utf-8", appCacheCb);

    }
    // ----------------cache业务处理 END----------------------------------------------------->

    public class GroupCacheCallBack implements HttpClientCallback {

        @SuppressWarnings({ "unchecked" })
        @Override
        public void completed(HttpClientCallbackResult result) {

            Map<String, String> groupCache = new HashMap<String, String>();
            String respStr = result.getReplyDataAsString();
            respStr = respStr.substring(7, respStr.length() - 2).replaceAll("\\\\", "");
            Map<String, Object> respMap = JSONHelper.toObject(respStr, Map.class);
            List<Map<String, String>> respList = (List<Map<String, String>>) respMap.get("data");
            for (Map<String, String> map : respList) {
                String groupid = map.get("groupid");
                String value = map.get("appids");
                String ldapkey = map.get("ldapkey") == null ? "" : map.get("ldapkey");

                String key = "groupId:" + groupid + ",ldappKey:" + ldapkey;
                groupCache.put(key, value);
            }
            cm.putHash("apphub.gui.cache", "manage.group", groupCache);
        }

        @Override
        public void failed(HttpClientCallbackResult result) {

            logger.err(this, "GUIService createGroupInfoCache :" + result.getException().getMessage());
        }

    }

    public class AppCacheCallBack implements HttpClientCallback {

        @SuppressWarnings({ "unchecked" })
        @Override
        public void completed(HttpClientCallbackResult result) {

            String respStr = result.getReplyDataAsString();
            respStr = respStr.substring(7, respStr.length() - 2).replaceAll("\\\\", "");
            Map<String, Object> respMap = JSONHelper.toObject(respStr, Map.class);
            List<Map<String, String>> respList = (List<Map<String, String>>) respMap.get("data");
            AppInfoCacheThread appInfoCacheThread = new AppInfoCacheThread(respList);
            appInfoCacheThread.start();
        }

        @Override
        public void failed(HttpClientCallbackResult result) {

            logger.err(this, "GUIService createAppInfoCache :" + result.getException().getMessage());
        }

    }

    public class AppInfoCacheThread extends Thread {

        private List<Map<String, String>> respList;

        public AppInfoCacheThread(List<Map<String, String>> respListParam) {
            this.respList = respListParam;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public void run() {

            for (Map<String, String> map : respList) {

                try {
                    String appId = map.get("appid");

                    String appUrl = map.get("appurl");

                    String configUrl = appUrl;

                    String configpath = map.get("configpath");
                    if (null != configpath && configpath.length() != 0) {
                        configUrl = configpath;
                    }
                    // 远程读取资源
                    if (configUrl.indexOf("http") == -1) {
                        configUrl = appBaseUrl + configUrl;
                    }

                    configUrl += "/config.properties";

                    logger.info(this, "初始化缓存处理 app 配置加载  :" + configUrl);
                    Properties properties = PropertiesHelper.downloadProperties(configUrl);

                    // 菜单封装
                    List jsonArray = menuSet(appUrl, properties);

                    // 打包
                    String appTitleStr = properties.getProperty("app.title");
                    Map resultJson = new LinkedHashMap();
                    resultJson.put("url", appUrl);
                    resultJson.put("title", appTitleStr);
                    resultJson.put("menu", jsonArray);
                    String dataStr = JSONHelper.toString(resultJson);

                    Map<String, String> appCache = new HashMap<String, String>();
                    appCache.put(appId, dataStr);
                    cm.putHash("apphub.gui.cache", "manage.app", appCache);

                }
                catch (Exception e) {
                    logger.err(this, "AppInfoCacheThread run :" + e.getMessage(), e);
                }

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
    
    /**
     * 设置会话：用户授权邮箱组
     */
    @SuppressWarnings("unchecked")
    private void setUAuthInfoToSession() {

        // 用户信息会话创建，信息保存
        HttpSession session = request.getSession(false);

        String[] emailList = String
                .valueOf(request.getSession(false).getAttribute("apphub.gui.session.login.user.emailList")).split(",");
        if ("UAV.ADMIN.EMAIL.LIST".equals(emailList[0])) {
            session.setAttribute("apphub.gui.session.login.user.authorize.emailList", "UAV.ADMIN.EMAIL.LIST");
            session.setAttribute("apphub.gui.session.login.user.authorize.systems", "UAV.ADMIN.SYSTEMS");
            return;
        }

        // 用户邮箱组权限
        StringBuilder emailLists = new StringBuilder();
        // 用户系统权限
        StringBuilder systems = new StringBuilder();

        for (String e : emailList) {
            Map<String, String> esistsMap = cm.getHash("apphub.app.godeye.filter.cache", "email.list.group", e);
            if (esistsMap.get(e) != null) {
                Map<String, Object> emailMap = JSONHelper.toObject(esistsMap.get(e), Map.class);// 因为嵌套了一层，还需要再取一次
                if (!"1".equals(String.valueOf(emailMap.get("state")))) { // 状态为可用
                    continue;
                }
                if (emailLists.length() > 0) {
                    emailLists.append(",");
                }
                emailLists.append(String.valueOf(emailMap.get("emailListName")));

                /**
                 * 系统组获取
                 */
                if (systems.length() > 0) {
                    systems.append(",");
                }
                Map<String, Object> groups = JSONHelper.toObject(String.valueOf(emailMap.get("groupList")), Map.class);// 因为嵌套了一层，还需要再取一次
                systems.append(groups.keySet().toString().substring(1, groups.keySet().toString().length() - 1));

            }
        }

        session.setAttribute("apphub.gui.session.login.user.authorize.emailList", emailLists.toString());
        session.setAttribute("apphub.gui.session.login.user.authorize.systems", systems.toString());

    }

}
