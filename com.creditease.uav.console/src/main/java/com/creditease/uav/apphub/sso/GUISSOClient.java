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
package com.creditease.uav.apphub.sso;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.cache.api.CacheManagerFactory;

public abstract class GUISSOClient {

    protected static final List<Map<String, String>> systemUser = new ArrayList<Map<String, String>>();
    protected static char[] hex = "0123456789abcdef".toCharArray();
    protected static CacheManager cm = null;
    protected static MessageDigest md = null;
    protected ISystemLogger logger = SystemLogger.getLogger(GUISSOLdapClient.class);

    protected GUISSOClient() {

    }

    public GUISSOClient(HttpServletRequest request) {

        initMessageMD5();
        initSystemUser(request);
        initRedis(request);

    }

    public Map<String, String> getUserByLogin(String loginId, String loginPwd) {

        Map<String, String> userInfo = new HashMap<String, String>();

        userInfo = getUserBySystem(loginId, loginPwd);

        if (userInfo.isEmpty()) {
            userInfo = getUserByCache(loginId, loginPwd);
        }

        if (userInfo.isEmpty()) {
            userInfo = getUserByLoginImpl(loginId, loginPwd);
            if (!userInfo.isEmpty()) {
                userInfo.put("loginPwd", bytes2Hex(md.digest(loginPwd.getBytes())));
                cm.putJSON("apphub.ldap.cache", loginId, userInfo, 7L, TimeUnit.DAYS);
                loggerInfo("用户信息缓存 ", "保存7天", "成功", loginId);
            }
        }

        if (!userInfo.isEmpty()) {
            userInfo.remove("loginPwd");
            loggerInfo("用户登录 ", "信息获取", "成功", userInfo.toString());
        }

        return userInfo;
    }

    /**
     * return Map key schema :
     * 
     * "loginId":loginId
     * 
     * "groupId":所属组织架构字符串
     * 
     * "emailList":所属业务组字符串
     */
    protected abstract Map<String, String> getUserByLoginImpl(String loginId, String loginPwd);

    /**
     * return List<Map> key schema :
     * 
     * "name":用户姓名
     * 
     * "email":用户邮箱(登录账户)
     * 
     * "groupId":所属组织架构字符串
     * 
     * "emailList":所属业务组字符串
     */
    public abstract List<Map<String, String>> getUserByQuery(String email);

    /**
     * return Map key schema :
     * 
     * "name":业务组名(String类型)
     * 
     * "email":业务组邮箱或标识(String类型)
     * 
     * "groupIdFiltered":业务组成员所属的所有组织架构(Set<String>类型)
     * 
     * "emailList_Filtered":业务组成员所属的所有业务组 (Set<String>类型)
     * 
     * "userInfo":业务组成员信息，包含name、email、groupID、emailList。(List<Map<String,String>>类型)
     */
    public abstract Map<String, Object> getEmailListByQuery(String email);

    // ======================================init begin========================================
    private void initMessageMD5() {

        try {
            if (null == md) {
                md = MessageDigest.getInstance("MD5");
            }
        }
        catch (NoSuchAlgorithmException e) {
            loggerError("initMessageMD5", "", e);
        }

    }

    private void initSystemUser(HttpServletRequest request) {

        if (systemUser.isEmpty()) {

            String adminLoginid = request.getServletContext().getInitParameter("uav.apphub.sso.admin.loginid");
            String adminPassword = request.getServletContext().getInitParameter("uav.apphub.sso.admin.password");
            Map<String, String> adminInfo = new HashMap<String, String>();
            adminInfo.put("loginId", adminLoginid);
            adminInfo.put("loginPwd", adminPassword);
            adminInfo.put("groupId", "uav_admin");
            adminInfo.put("emailList", "UAV.ADMIN.EMAIL.LIST");
            systemUser.add(adminInfo);

            String guestLoginid = request.getServletContext().getInitParameter("uav.apphub.sso.guest.loginid");
            String guestPassword = request.getServletContext().getInitParameter("uav.apphub.sso.guest.password");
            Map<String, String> guestInfo = new HashMap<String, String>();
            guestInfo.put("loginId", guestLoginid);
            guestInfo.put("loginPwd", guestPassword);
            guestInfo.put("groupId", "uav_guest");
            guestInfo.put("emailList", "UAV.GUEST.EMAIL.LIST");
            systemUser.add(guestInfo);
        }

    }

    @SuppressWarnings("unchecked")
    private void initRedis(HttpServletRequest request) {

        if (null == cm) {
            String redisAddrStr = request.getServletContext().getInitParameter("uav.app.ldap.redis.store.addr");
            Map<String, Object> redisParamsMap = JSONHelper.toObject(
                    request.getServletContext().getInitParameter("uav.app.ldap.redis.store.params"), Map.class);
            cm = CacheManagerFactory.build(redisAddrStr, Integer.valueOf(String.valueOf(redisParamsMap.get("min"))),
                    Integer.valueOf(String.valueOf(redisParamsMap.get("max"))),
                    Integer.valueOf(String.valueOf(redisParamsMap.get("queue"))),
                    String.valueOf(redisParamsMap.get("pwd")));
        }

    }

    private Map<String, String> getUserBySystem(String loginId, String loginPwd) {

        Map<String, String> result = new HashMap<String, String>();

        for (Map<String, String> user : systemUser) {
            if (user.get("loginId").equals(loginId) && user.get("loginPwd").equals(loginPwd)) {
                result.putAll(user);
                break;
            }
        }

        if (!result.isEmpty()) {
            loggerInfo("系统用户 ", "校验", "成功", loginId);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getUserByCache(String loginId, String loginPwd) {

        Map<String, String> result = new HashMap<String, String>();

        if (cm.exists("apphub.ldap.cache", loginId)) {
            Map<String, String> cache = cm.getJSON("apphub.ldap.cache", loginId, Map.class);
            if (bytes2Hex(md.digest(loginPwd.getBytes())).equals(cache.get("loginPwd"))) {
                result = cache;
            }
        }

        if (!result.isEmpty()) {
            loggerInfo("缓存用户 ", "校验", "成功", loginId);
        }
        return result;

    }

    private String bytes2Hex(byte[] bys) {

        char[] chs = new char[bys.length * 2];
        int stopCondition = bys.length;
        int offset = 0;
        for (int i = 0; i < stopCondition; i++) {
            chs[offset++] = hex[bys[i] >> 4 & 0xf];
            chs[offset++] = hex[bys[i] & 0xf];
        }
        return new String(chs);
    }

    protected void loggerInfo(String title, String action, String result, String msg) {

        logger.info(this, title + "[" + action + "]" + result + " " + msg);
    }

    protected void loggerError(String title, String msg, Exception e) {

        logger.err(this, title + "[错误] " + msg, e.getMessage(), e);
    }

}
