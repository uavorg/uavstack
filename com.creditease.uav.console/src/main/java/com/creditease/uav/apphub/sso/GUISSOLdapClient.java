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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.servlet.http.HttpServletRequest;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.uav.exception.ApphubException;

/**
 * @author Created by lbay on 2016/1/25.
 */
public class GUISSOLdapClient extends GUISSOClient {

    private static Map<String, String> ldapConfig = new HashMap<String, String>();
    private static Map<String, Properties> ldapParams = new HashMap<String, Properties>();
    private static Map<String, LdapContext> ldapContexts = new HashMap<String, LdapContext>();

    @SuppressWarnings("unused")
    private GUISSOLdapClient() {

    }

    protected GUISSOLdapClient(HttpServletRequest request) {
        super(request);
        initLdapConfig(request);
    }

    @SuppressWarnings("unchecked")
    private void initLdapConfig(HttpServletRequest request) {

        if (ldapConfig.isEmpty()) {

            String ldapConInfoStr = request.getServletContext().getInitParameter("uav.apphub.sso.ldap.connection.info");
            ldapConfig = JSONHelper.toObject(ldapConInfoStr, Map.class);
        }
    }

    private void initLdapParams(String action) {

        if (!ldapParams.containsKey(action)) {

            Properties param = new Properties();

            String baseKey = action + "basedn";
            String ldapConBasedn = ldapConfig.get(baseKey);
            String ldapConUrl = ldapConfig.get("url");
            String ldapConUser = ldapConfig.get("user");
            String ldapConPassWord = ldapConfig.get("password");
            String ldapConTimeout = ldapConfig.get("contimeout");
            param.put(Context.PROVIDER_URL, ldapConUrl + ldapConBasedn);
            param.put(Context.SECURITY_PRINCIPAL, ldapConUser);
            param.put(Context.SECURITY_CREDENTIALS, ldapConPassWord);
            param.put(Context.SECURITY_AUTHENTICATION, "simple");
            param.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            param.put("com.sun.jndi.ldap.connect.timeout", ldapConTimeout);

            /**
             * ldap以下字段转码需要特殊处理：不显示乱码，但也不会显示为可识别的string,以binary string显示
             */
            param.put("java.naming.ldap.attributes.binary",
                    "msExchMailboxGuid objectSid objectGUID msExchMailboxSecurityDescriptor ");


            ldapParams.put(action, param);
        }

    }

    private void initLdapContext(String action) {

        if (!ldapContexts.containsKey(action)) {

            try {
                loggerInfo("LDAPContext", "初始化", "开始", action);

                initLdapParams(action);

                Properties actionParam = ldapParams.get(action);

                LdapContext newContext = new InitialLdapContext(actionParam, null);
                ldapContexts.put(action, newContext);

                loggerInfo("LDAPContext", "初始化", "完成", action);
            }
            catch (Exception e) {
                loggerError("LDAPContext初始化", action, e);
            }
        }

    }

    private void clearLdapContext(String action) {

        try {
            loggerInfo("LDAPContext", "清空", "开始", action);

            if (ldapContexts.containsKey(action)) {
                LdapContext context = ldapContexts.get(action);
                context.close();
                context = null;
                ldapContexts.remove(action);
            }

            loggerInfo("LDAPContext", "清空", "完成", action);
        }
        catch (Exception e) {
            loggerError("LDAPContext清空", action, e);
        }

    }

    // ======================================init end========================================

    // ======================================ldap api begin========================================
    private boolean ldapApiCheck(String loginId, String password) {

        boolean result = false;
        String action = "login";
        LdapContext newContext = null;
        try {
            initLdapContext(action);
            Properties actionParam = ldapParams.get(action);

            // 替换参数，账号密码验证
            actionParam.put(Context.SECURITY_PRINCIPAL, loginId);
            actionParam.put(Context.SECURITY_CREDENTIALS, password);
            // 密码验证,不报错则为验证成功
            newContext = new InitialLdapContext(actionParam, null);
            result = true;
            loggerInfo("LDAP信息", "登陆校验", "成功", loginId);
        }
        catch (AuthenticationException e) {
            // 此异常为用户验证失败
            loggerInfo("LDAP信息", "登陆校验", "失败", loginId);
        }
        catch (Exception e1) {
            loggerError("LDAP信息校验", loginId, e1);
            clearLdapContext(action);
        }
        finally {

            try {
                if (null != newContext) {
                    newContext.close();
                }
            }
            catch (NamingException e) {
                loggerError("LDAP信息校验,链接关闭", loginId, e);
            }
        }

        return result;

    }

    private List<SearchResult> ldapApiQuery(String action, String name, String filter) {

        String logMsg = action + " " + filter;
        List<SearchResult> result = new ArrayList<SearchResult>();
        try {
            initLdapContext(action);
            LdapContext ldapCtx = ldapContexts.get(action);

            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            
            String ldapConTimeout = ldapConfig.get("contimeout");
            constraints.setTimeLimit(Integer.valueOf(ldapConTimeout));
            NamingEnumeration<SearchResult> en = ldapCtx.search(name, filter, constraints);

            // means all nodes
            if (en == null) {
                loggerInfo("LDAP信息", "获取", "结果为空", logMsg);
                return Collections.emptyList();
            }
            if (!en.hasMoreElements()) {
                loggerInfo("LDAP信息", "获取", "结果为空", logMsg);
                return Collections.emptyList();
            }

            while (en != null && en.hasMoreElements()) {// maybe more than one element
                Object obj = en.nextElement();
                if (obj instanceof SearchResult) {
                    SearchResult si = (SearchResult) obj;
                    result.add(si);
                }
            }
        }
        catch (Exception e) {
            loggerError("LDAP用户信息获取", logMsg, e);
            clearLdapContext(action);
        }

        if (!result.isEmpty()) {
            loggerInfo("LDAP信息", "获取", "成功", logMsg);
        }
        return result;
    }

    // ======================================ldap api end========================================

    // ======================================get user begin========================================

    @Override
    protected Map<String, String> getUserByLoginImpl(String loginId, String password) {

        String suffix = ldapConfig.get("suffix");

        if (loginId.indexOf(suffix) == -1) {
            loginId += suffix;
        }

        boolean login = ldapApiCheck(loginId, password);
        String primaryKey = ldapConfig.get("primaryKey");
        if (!login) {
            return Collections.emptyMap();
        }

        String action = "login";
        String filter = primaryKey + "=" + loginId;

        List<SearchResult> sResultList = ldapApiQuery(action, "", filter);
        // filter userPrincipalName= 只能查询到一个结果
        SearchResult sResult = sResultList.get(0);

        String groupIdStr = formatGroupId(sResult);
        String emailListStr = formatEmailList(sResult);

        Map<String, String> result = new HashMap<String, String>();
        result.put("loginId", loginId);
        result.put("groupId", groupIdStr);
        result.put("emailList", emailListStr);

        return result;
    }

    @Override
    public List<Map<String, String>> getUserByQuery(String email) {

        if (StringHelper.isEmpty(email)) {
            return Collections.emptyList();
        }

        String suffix = ldapConfig.get("suffix");

        String userCNField = "cn";

        String userQueryField = ldapConfig.get("userQueryField");

        String email1 = email + suffix;

        String filter = "(|(" + userCNField + "=" + email + ")(" + userQueryField + "=" + email + ")(" + userQueryField
                + "=" + email1 + "))";

        String action = "query";
        /**
         * 查询ldap 获取list信息
         */
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        List<SearchResult> sResultList = ldapApiQuery(action, "", filter);
        if (sResultList.isEmpty()) {
            Map<String, String> msg = new HashMap<String, String>();
            msg.put("msg", "email query,result is empty.");
            result.add(msg);
            return result;
        }

        for (SearchResult sResult : sResultList) {
            /**
             * 遍历格式化用户信息
             */
            String groupIdStr = formatGroupId(sResult);
            String emailListStr = formatEmailList(sResult);

            Map<String, String> emailInfoMap = formatEmailInfo(sResult, userQueryField);
            Map<String, String> info = new HashMap<String, String>();
            info.putAll(emailInfoMap);
            info.put("groupId", groupIdStr);
            info.put("emailList", emailListStr);

            result.add(info);
        }

        return result;
    }

    // ======================================get user end========================================

    // ======================================get eamil begin========================================

    @Override
    public Map<String, Object> getEmailListByQuery(String email) {

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        String action = "query";
        String filter = "";
        String suffix = ldapConfig.get("suffix");
        try {
            String groupCNField = "cn";

            String groupQueryField = ldapConfig.get("groupQueryField");

            String email1 = email + suffix;

            filter = "(|(" + groupCNField + "=" + email + ")(" + groupQueryField + "=" + email + ")(" + groupQueryField
                    + "=" + email1 + "))";

            List<SearchResult> sResultList = ldapApiQuery(action, "", filter);
            // filter 只能查询到一个结果
            SearchResult sResult = sResultList.get(0);
            if (null == sResult) {
                result.put("msg", "emailList query,result is empty.");
                return result;
            }

            Map<String, String> emailInfoMap = formatEmailInfo(sResult, groupQueryField);
            List<String> userEnNameList = formatUserEnName(sResult);

            /**
             * 获取用户信息，排除不是当前查询邮箱组的用户
             */
            String emailEnName = emailInfoMap.get("name");
            List<Map<String, String>> userInfoList = filterUserByQuery(userEnNameList, emailEnName);

            result.putAll(emailInfoMap);
            if (!userInfoList.isEmpty()) {
                result.put("groupIdFiltered", formatGroupDuplicateRemoval(userInfoList));
                result.put("emailList_Filtered", formatEmailDuplicateRemoval(userInfoList));
                result.put("userInfo", userInfoList);
            }

        }
        catch (Exception e) {
            clearLdapContext(action);
            logger.err(this, e.getMessage(), e);
        }
        return result;
    }

    // ======================================get eamil end========================================

    // ======================================tools begin========================================

    private String formatGroupId(SearchResult sResult) {

        if (null == sResult) {
            return "";
        }

        String groupId = "";
        String userDN = sResult.getName();
        if (userDN.length() <= 0) {
            return "";
        }

        String[] strings = userDN.split(",");

        String loginbaseDN = ldapConfig.get("loginbasedn");
        String rootOU = loginbaseDN.substring(loginbaseDN.indexOf("=") + 1, loginbaseDN.indexOf(","));

        for (int i = 1; i < strings.length; i++) {

            String s = strings[i];
            s = s.substring(s.indexOf("=") + 1);

            if (s.equals(rootOU)) {
                break;
            }
            groupId = s + "/" + groupId;
        }

        groupId = groupId.substring(0, groupId.length() - 1);

        return groupId;
    }

    @SuppressWarnings("rawtypes")
    private String formatEmailList(SearchResult sResult) {

        if (null == sResult) {
            return "";
        }

        StringBuilder emailList = new StringBuilder();
        try {
            NamingEnumeration namingEnumeration = sResult.getAttributes().getAll();
            while (namingEnumeration.hasMoreElements()) {
                Attribute attr = (Attribute) namingEnumeration.next();
                emailList.append(formatEmailInfo(attr));
            }

        }
        catch (NamingException e) {
            loggerError("formatEmailList", "", e);
        }

        return emailList.toString();

    }

    @SuppressWarnings("rawtypes")
    private String formatEmailInfo(Attribute attr) {

        if (null == attr) {
            return "";
        }

        StringBuilder values = new StringBuilder();
        boolean isFormat = false;
        try {

            String formatS = "=";
            String attrId = attr.getID();
            String groupKey = ldapConfig.get("groupKey");
            String groupTag = ldapConfig.get("groupTag");
            for (NamingEnumeration vals = attr.getAll(); vals.hasMore();) {
                String strValue = vals.next().toString();
                if (groupKey.equals(attrId) && strValue.indexOf(groupTag) >= 0) {

                    values.append(",");
                    isFormat = true;

                    if (strValue.indexOf(formatS) == -1) {
                        values.append(strValue);
                        continue;
                    }

                    int begin = strValue.indexOf(formatS) + formatS.length();
                    int end = strValue.indexOf(",");
                    values.append(strValue.substring(begin, end));

                }
            }
        }
        catch (Exception e) {
            loggerError("formatEmailInfo 555", "", e);
            throw new ApphubException(e);
        }
        /**
         * 去除第一个逗号
         */
        String result = "";
        if (isFormat) {
            result = values.toString().substring(1);
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    private Map<String, String> formatEmailInfo(SearchResult sResult, String targetKey) {

        if (null == sResult) {
            return Collections.emptyMap();
        }

        Map<String, String> result = new LinkedHashMap<String, String>();
        try {
            NamingEnumeration namingEnumeration = sResult.getAttributes().getAll();
            while (namingEnumeration.hasMoreElements()) {
                Attribute attr = (Attribute) namingEnumeration.next();
                String attrId = attr.getID();
                String attrValue = attr.getAll().next().toString();
                if (targetKey.equals(attrId)) {
                    result.put("email", attrValue);
                }
                if ("cn".equals(attrId)) {
                    result.put("name", attrValue);
                }

                result.put(attrId, attrValue);
            }

        }
        catch (Exception e) {
            loggerError("formatEmailInfo 591", "", e);
        }

        return result;
    }

    @SuppressWarnings("rawtypes")
    private List<String> formatUserEnName(SearchResult sResult) {

        if (null == sResult) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>();
        try {
            String memberKey = ldapConfig.get("memberKey");
            NamingEnumeration namingEnumeration = sResult.getAttributes().getAll();
            while (namingEnumeration.hasMoreElements()) {
                Attribute attr = (Attribute) namingEnumeration.next();
                String attrId = attr.getID();
                if (memberKey.equals(attrId)) {
                    List<String> userEnNames = formatUserEnName(attr);
                    result.addAll(userEnNames);
                }
            }

        }
        catch (Exception e) {
            loggerError("formatUserEnName 619", "", e);
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    private List<String> formatUserEnName(Attribute attr) {

        if (null == attr) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<String>();
        String formatCN = "=";
        try {

            NamingEnumeration members = attr.getAll();

            while (members.hasMore()) {
                String memberValue = members.next().toString();
                int indexStart = memberValue.indexOf(formatCN);
                if (indexStart == -1) {
                    result.add(memberValue);
                    continue;
                }
                int indexEnd = memberValue.indexOf(",");
                String memberCN = memberValue.substring(indexStart + formatCN.length(), indexEnd);

                result.add(memberCN);
            }

        }
        catch (

        Exception e) {

            loggerError("formatUserEnName 648", "", e);
        }
        return result;
    }

    /**
     * 去重提取
     * 
     * @param memberList
     * @return
     */
    private Set<String> formatGroupDuplicateRemoval(List<Map<String, String>> memberList) {

        Set<String> groupIds = new HashSet<String>();
        for (Map<String, String> map : memberList) {
            String groupId = map.get("groupId");
            groupIds.add(groupId);
        }

        return groupIds;
    }

    /**
     * 去重提取
     * 
     * @param memberList
     * @return
     */
    private Set<String> formatEmailDuplicateRemoval(List<Map<String, String>> memberList) {

        Set<String> emails = new LinkedHashSet<String>();
        for (Map<String, String> map : memberList) {
            String emailList = map.get("emailList");
            String[] splits = emailList.split(",");
            for (int i = 0; i < splits.length; i++) {
                emails.add(splits[i]);
            }
        }

        return emails;
    }

    /**
     * 过滤用户：用户查询可能会有重名，去除不是mapping邮箱的用户
     * 
     * @param userEnNameList
     * @param emailEnName
     * @return
     */
    private List<Map<String, String>> filterUserByQuery(List<String> userEnNameList, String mappingEmailKey) {

        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        for (String userEnName : userEnNameList) {
            List<Map<String, String>> userInfoListMapping = getUserByQuery(userEnName);// 查询用户信息
            result.addAll(filterUserByEmailList(userInfoListMapping, mappingEmailKey));
        }
        return result;
    }

    private List<Map<String, String>> filterUserByEmailList(List<Map<String, String>> userInfoListMapping,
            String mappingEmailKey) {

        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        for (Map<String, String> userInfoM : userInfoListMapping) {
            if (null != userInfoM && userInfoM.get("emailList").indexOf(mappingEmailKey) >= 0) {
                result.add(userInfoM);
            }
        }

        return result;
    }

    // ======================================tools end========================================

}