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

package com.creditease.uav.apphub.filter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.NetworkHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;

/**
 * 
 * SessionAsyncFilter description: 会话拦截
 * 
 * @author lbay
 *
 */
public class SessionAsyncFilter implements Filter {

    private String[] exResSs;
    private ISystemLogger logger = null;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        String exResStr = filterConfig.getInitParameter("excludeResource");
        exResSs = exResStr.split(",");

        /**
         * init gloabl mainlog
         */
        String logLevel = filterConfig.getInitParameter("log.level");
        String logDebug = filterConfig.getInitParameter("log.debug");

        File appPath = new File(filterConfig.getServletContext().getRealPath(""));

        String logPath = appPath.getParentFile().getParentFile().getAbsolutePath();

        String appLogEnable = filterConfig.getInitParameter("log.app.enable");
        if ("true".equals(appLogEnable)) {
            System.setProperty("JAppLogsEnable", "true");
        }

        SystemLogger.init("apphub", logPath, logLevel, Boolean.parseBoolean(logDebug), 10 * 1024 * 1024);

        if (null == logger) {
            logger = SystemLogger.getLogger(SessionAsyncFilter.class);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.addHeader("Set-Cookie", "HttpOnly");

        userLoggerInfo(req);

        String requestSource = getRequestSource(req);
        // 会话判断,判断是否有登录用户信息
        boolean userIsLogin = checkUserIsLogin(req);
        if (userIsLogin && requestSource.startsWith("/ping")) { // 快速返回ping请求
            respPrintOut(response, "1");
            return;
        }

        if (userIsLogin) {
            chain.doFilter(request, response);
            return;
        }

        // 权限判断
        boolean dofilter = false;

        for (String s : exResSs) {
            if (checkReplaceSymbol(requestSource, s)) {
                dofilter = true;
                break;
            }
        }

        // 可访问
        if (dofilter) {
            chain.doFilter(request, response);
            return;
        }

        // 拦截访问
        logger.info(this.getClass().getSimpleName(), "访问被拦截:" + requestSource);
        String jumpPage = request.getServletContext().getContextPath() + "/main.html";

        // ajax请求
        if (null != req.getHeader("x-requested-with") && "XMLHttpRequest".equals(req.getHeader("x-requested-with"))) {
            respPrintOut(response, "SESSION_CHECK_FAIL," + jumpPage);
        }
        else {
            respRedirect(response, jumpPage);
        }

    }

    @Override
    public void destroy() {

        /**
         * Auto-generated method stub
         */

    }

    /**
     * 校验替换符号：替换符结束则视为全部、不能以替换符开始，否则开始的替换符将被忽略、只支持一个替换符
     * 
     * @param source
     * @param ex:跳过监测的规则值
     * @return
     */
    private boolean checkReplaceSymbol(String source, String ex) {

        String rs = "*";
        // 没有替换符号
        if (ex.indexOf(rs) == -1) {
            return source.equals(ex);
        }

        // 不能以替换符开始
        if (ex.startsWith(rs)) {
            ex = ex.substring(1);
        }

        // 最后一位是替换符
        if (ex.substring(ex.length() - 1).equals(rs)) {
            return source.startsWith(ex.substring(0, ex.length() - 1));
        }

        // 一个替换符
        String[] ss = ex.split("\\" + rs);
        if (source.startsWith(ss[0]) && source.endsWith(ss[1])) {
            return true;
        }

        return false;
    }

    /**
     * 返回数据
     * 
     * @param response
     * @param msg
     */
    private void respPrintOut(ServletResponse response, String msg) {

        PrintWriter out = null;
        try {
            HttpServletResponse resp = (HttpServletResponse) response;
            out = resp.getWriter();
            out.write(msg);
            out.flush();
        }
        catch (IOException e) {
            logger.err(this, e.getMessage(), e);
        }
        finally {
            if (null != out) {
                out.close();
            }
        }
    }

    /**
     * 返回跳转
     * 
     * @param response
     * @param url
     */
    private void respRedirect(ServletResponse response, String url) {

        try {
            HttpServletResponse resp = (HttpServletResponse) response;
            resp.setHeader("Cache-Control", "no-store");
            resp.setDateHeader("Expires", 0);
            resp.setHeader("Prama", "no-cache");
            resp.sendRedirect(url);
        }
        catch (IOException e) {
            logger.err(this, e.getMessage(), e);
        }

    }

    private void userLoggerInfo(HttpServletRequest request) {

        if (!checkUserIsLogin(request)) {
            return;
        }

        String requesturl = getRequestSource(request);
        String actiontype = getReqValueByGetUrl(request, "urltype");
        String actionurl = getReqValueByGetUrl(request, "url");
        String actiondesc = getReqValueByGetUrl(request, "desc");

        if (!"menuclick".equals(actiontype) && !"jumpmain".equals(actiontype) && !"jumpapp".equals(actiontype)) {
            return;
        }

        /**
         * login/logout见 GUIService.login()/loginOut()
         */

        String ip = request.getRemoteAddr();
        String xip = request.getHeader("X-Forwarded-For");

        String time = sdf.format(new Date());
        String userid = getUserIdBySession(request);
        String userip = getClientIP(ip, xip);

        Map<String, String> userInfo = new HashMap<String, String>();
        userInfo.put("key", "ulog");
        userInfo.put("time", time);
        userInfo.put("uid", userid);
        userInfo.put("uip", userip);
        userInfo.put("rs", requesturl);
        userInfo.put("type", actiontype);
        userInfo.put("url", actionurl);
        userInfo.put("desc", actiondesc);
        userInfo.put("authemails", String
                .valueOf(request.getSession(false).getAttribute("apphub.gui.session.login.user.authorize.emailList")));
        userInfo.put("authsystems", String
                .valueOf(request.getSession(false).getAttribute("apphub.gui.session.login.user.authorize.systems")));
        
        logger.info(this, JSONHelper.toString(userInfo));
    }

    private String getReqValueByGetUrl(HttpServletRequest request, String key) {

        String result = "";
        try {
            String values = request.getQueryString();
            if (null == values) {
                return result;
            }

            String[] ss = values.split("&");
            for (String s : ss) {
                String[] checks = s.split("=");
                if (null != checks && checks.length >= 2 && checks[0].equals(key)) {
                    result = checks[1];
                    result = URLDecoder.decode(URLDecoder.decode(result, "UTF-8"), "UTF-8");
                    break;
                }
            }

        }
        catch (UnsupportedEncodingException e) {

            logger.err(this, e.getMessage(), e);
        }
        return result;
    }

    private String getRequestSource(HttpServletRequest request) {

        String proName = request.getServletContext().getContextPath();
        String requestSource = request.getRequestURI();
        requestSource = requestSource.substring(requestSource.indexOf(proName) + proName.length());
        return requestSource;
    }

    private boolean checkUserIsLogin(HttpServletRequest request) {

        boolean isLogin = false;
        if (null != request.getSession(false)) {
            isLogin = !"".equals(getUserIdBySession(request));
        }
        return isLogin;
    }

    private String getUserIdBySession(HttpServletRequest request) {

        String userId = "";
        if (null != request.getSession(false)) {
            Object obj = request.getSession(false).getAttribute("apphub.gui.session.login.user.id");
            userId = null == obj ? "" : String.valueOf(obj);
        }
        return userId;
    }

    /**
     * getClientIP
     * 
     * @param remoteAddr
     * @param xForwardHeader
     * @return
     */
    private String getClientIP(String remoteAddr, String xForwardHeader) {

        String ip = xForwardHeader;
        if (ip != null && !"unKnown".equalsIgnoreCase(ip)) {
            int index = ip.indexOf(",");
            if (index != -1) {
                ip = ip.substring(0, index);
            }

            ip = getLocalHostToIP(ip);

        }
        else {
            ip = getLocalHostToIP(remoteAddr);
        }

        return ip;
    }

    /**
     * getLocalHostToIP
     * 
     * @param ip
     * @return
     */
    private String getLocalHostToIP(String ip) {

        if (ip == null) {
            return ip;
        }

        if (ip.equals("127.0.0.1") || "0:0:0:0:0:0:0:1".equals(ip) || "localhost".equals(ip)) {
            return NetworkHelper.getLocalIP();
        }

        if (ip.indexOf("127.0.0.1") > -1 || ip.indexOf("localhost") > -1 || ip.indexOf("0:0:0:0:0:0:0:1") > -1) {
            String localip = NetworkHelper.getLocalIP();
            ip = ip.replace("127.0.0.1", localip).replace("localhost", localip).replace("0:0:0:0:0:0:0:1", localip);
        }

        return ip;
    }

}
