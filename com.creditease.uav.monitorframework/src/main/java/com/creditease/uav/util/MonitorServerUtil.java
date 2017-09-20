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

package com.creditease.uav.util;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import com.creditease.agent.helpers.NetworkHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.UAVServer.ServerVendor;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.MonitorElement;
import com.creditease.monitor.captureframework.spi.MonitorElementInstance;

public class MonitorServerUtil {

    /**
     * 
     * OracleTokenBuffer description: 解析Oracle数据库连接字符串，God
     *
     */
    public static class OracleTokenBuffer {

        private StringBuilder curStr;

        private boolean isMatch = false;

        private List<String> iphosts = new ArrayList<String>();

        private String dbName;

        public OracleTokenBuffer(String str) {
            this.curStr = new StringBuilder(str);
        }

        public void match() {

            // single ip
            if (this.curStr.indexOf("=") == -1) {
                int st = this.curStr.indexOf("@");
                String ipstr = this.curStr.substring(st + 1);
                String[] ipInfo = ipstr.split(":");
                iphosts.add(ipInfo[0] + ":" + ipInfo[1]);
                this.dbName = ipInfo[2];
                return;
            }

            // cluster
            while (true) {

                match("HOST");

                if (isMatch == false) {
                    break;
                }
            }

            match("SERVICE_NAME");
        }

        @Override
        public String toString() {

            this.match();

            StringBuilder sb = new StringBuilder();

            sb.append("jdbc:oracle://");

            int index = 0;
            for (String iphost : iphosts) {
                sb.append(iphost);

                if (index < iphosts.size() - 1) {
                    sb.append(",");
                }

                index++;
            }

            sb.append("/").append(this.dbName);

            return sb.toString();
        }

        private void match(String token) {

            String str = curStr.toString();

            int index = str.indexOf(token);

            if (index == -1) {
                index = str.indexOf(token.toLowerCase());
            }

            if (index == -1) {
                this.isMatch = false;
                return;
            }

            str = str.substring(index);

            int end = str.indexOf(")");

            if (end == -1) {
                this.isMatch = false;
                return;
            }

            String match = str.substring(0, end);

            String val = match.split("=")[1].trim();

            if (token.equalsIgnoreCase("HOST")) {
                iphosts.add(val);
            }
            else if (token.equalsIgnoreCase("PORT")) {
                String host = iphosts.get(iphosts.size() - 1);
                iphosts.remove(iphosts.size() - 1);
                iphosts.add(host + ":" + val);
            }
            else if (token.equalsIgnoreCase("SERVICE_NAME")) {
                this.dbName = val;
            }

            str = str.substring(end + 1);

            this.curStr = new StringBuilder(str);

            this.isMatch = true;

            if (token.equalsIgnoreCase("HOST")) {

                match("PORT");
            }
        }
    }

    private static String[] filterExts = new String[] { ".js", ".css", ".jpg", ".jpeg", ".gif", ".png", ".bmp", ".xlsx",
            ".pptx", ".docx", ".doc", ".woff2", ".ico", ".txt" };

    private static String[] filterPageExts = new String[] { ".jsp", ".html", "*.htm" };

    private static String[] browserUAFeatures = new String[] { "Mozilla", "Opera", "Trident", "AppleWebKit", "Chrome",
            "Firefox", "Safari" };

    private MonitorServerUtil() {

    }

    /**
     * get the real client ip
     * 
     * @param remoteAddr
     * @param xForwardHeader
     * @param xRealIPHeader
     * @return
     */
    public static String getClientIP(String remoteAddr, String xForwardHeader) {

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
    public static String getLocalHostToIP(String ip) {

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

    /**
     * get JMX MBeanServer
     * 
     * @return
     */
    public static MBeanServer getMBeanServer() {

        MBeanServer server;

        if (!MBeanServerFactory.findMBeanServer(null).isEmpty()) {
            server = MBeanServerFactory.findMBeanServer(null).get(0);

        }
        else {
            server = ManagementFactory.getPlatformMBeanServer();
        }

        return server;
    }

    /**
     * doSumTimeAndCounter
     * 
     * @param inst
     * @param respTime
     */
    public static void doSumTimeAndCounter(MonitorElementInstance inst, long respTime) {

        if (null == inst || respTime < 0) {
            return;
        }

        long sumValue = inst.sumValue(CaptureConstants.MEI_RESP_SUMTIME, respTime);
        /**
         * we should consider if the number is over Long.MAX_VALUE at that time, the number will become minus number
         * then we set the base number to the new value so there should be jump in Sum Value & Counter, we should handle
         * this jump in Health Manager
         */
        if (sumValue < 0) {
            inst.setValue(CaptureConstants.MEI_RESP_SUMTIME, new AtomicLong(respTime));
            inst.setValue(CaptureConstants.MEI_COUNTER, new AtomicLong(1));
        }
        else {
            inst.increValue(CaptureConstants.MEI_COUNTER);
        }
    }

    /**
     * get the standard action tag
     * 
     * @param action
     * @return
     */
    public static String getActionTag(String action) {

        return CaptureConstants.MEI_AC + action.toLowerCase();
    }

    /**
     * get the standard action err tag
     * 
     * @param action
     * @return
     */
    public static String getActionErrorTag(String action) {

        return CaptureConstants.MEI_AC_ERROR + action.toLowerCase();
    }

    /**
     * get application id through context root or basePath
     * 
     * @param contextroot
     * @param basePath
     * @return
     */
    public static String getApplicationId(String contextroot, String basePath) {

        String appid = "";

        if ("".equals(contextroot)) {

			/*
             * NOTE: springboot's basePath is a random temp directory,so we use main(usually the jar name) as the appid
             */
            if (UAVServer.instance().getServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR)
                    .equals(UAVServer.ServerVendor.SPRINGBOOT)) {

                String javaCommand = System.getProperty("sun.java.command");

                appid = javaCommand.split(" ")[0];

            }
            else {
                String tmp = basePath.replace("\\", "/");
                int index = tmp.lastIndexOf("/");

                appid = tmp.substring(index + 1);
            }
            
        }
        else {
            appid = contextroot;
        }

        if (appid.indexOf("/") == 0) {
            appid = appid.substring(1);
        }

        return appid;
    }

    /**
     * 针对服务监控而言，去除哪些js，css，各种图片之类
     * 
     * @param targetUrl
     * @return
     */
    public static boolean isIncludeMonitorURL(String targetUrl) {

        for (String filterExt : filterExts) {

            if (targetUrl.endsWith(filterExt)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 针对服务监控而言，MonitorData只关注服务，对服务以外的URL不记录Monitor数据，也避免由于恶意攻击导致Monitor数据过大，这个与MonitorRepository的个数控制是配合进行的
     * 
     * @param targetUrl
     * @return
     */
    @SuppressWarnings("unchecked")
    public static boolean isIncludeMonitorURLForService(String targetUrl) {

        HashSet<String> murls = (HashSet<String>) UAVServer.instance().getServerInfo("monitor.urls");

        for (String url : murls) {
            /**
             * Step 1: detect if match servlet pattern such as /xxxx/
             */
            if (targetUrl.indexOf(url) > -1) {
                return true;
            }

            /**
             * Step 2: possible is accessing a servlet such as /xxxx
             */
            String tmpUrl = url.substring(0, url.length() - 1);

            if (targetUrl.endsWith(tmpUrl) == true) {
                return true;
            }

        }

        return false;
    }

    /**
     * isIncludeMonitorURLForPage
     * 
     * @param targetUrl
     * @return
     */
    public static boolean isIncludeMonitorURLForPage(String targetUrl) {

        for (String filterPageExt : filterPageExts) {

            if (targetUrl.endsWith(filterPageExt)) {
                return true;
            }
        }

        return false;
    }

    /**
     * sometimes we meet the url like:
     * http://com.creditease.apphub/checkSec;jsessinoid=xxxx-9999-8888-87777&http://192.168.1.3:9080/test
     * 
     * although I don't think he knows about URL, have to clean the URL in a normal way
     * 
     * @param reUrl
     *            which doesn't include the base http://<host>:<port> or https://<host>:<port> for example:
     *            /com.creditease.apphub/checkSec;jsessinoid=xxxx-9999-8888-87777&http://192.168.1.3:9080/test
     * @return
     */
    public static String cleanRelativeURL(String reUrl) {

        reUrl = StringHelper.getSubStrBeforeToken(reUrl, "?");
        reUrl = StringHelper.getSubStrBeforeToken(reUrl, ";");
        reUrl = StringHelper.getSubStrBeforeToken(reUrl, "=");
        reUrl = StringHelper.getSubStrBeforeToken(reUrl, ":");
        reUrl = StringHelper.getSubStrBeforeToken(reUrl, "&");

        return reUrl;
    }

    /**
     * check if the request is from browser
     * 
     * @param ua
     * @return
     */
    public static boolean isFromBrowser(String ua) {

        if (StringHelper.isEmpty(ua)) {
            return false;
        }

        for (String bF : browserUAFeatures) {
            if (ua.indexOf(bF) > -1) {
                return true;
            }
        }

        return false;
    }

    /**
     * get ServerHost & Port
     * 
     * @return
     */
    public static String getServerHostPort() {

        ServerVendor vendor = (ServerVendor) UAVServer.instance().getServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR);

        switch (vendor) {
            case JBOSS:
            case JETTY:
            case TOMCAT:
            case SPRINGBOOT:
                return NetworkHelper.getLocalIP() + ":"
                        + UAVServer.instance().getServerInfo(CaptureConstants.INFO_APPSERVER_LISTEN_PORT);

            case MSCP:

                return NetworkHelper.getLocalIP() + ":"
                        + UAVServer.instance().getServerInfo(CaptureConstants.INFO_APPSERVER_LISTEN_PORT);

            case JSE:
            default:
                // TODO ????
                break;
        }

        return "";
    }

    /**
     * in order to pass the source jvm type to the target, we will add header to http or other protocol
     * 
     * UAV-Client-Src, then we can know who is calling from the server side, hehe
     * 
     * @param appid
     * @return
     */
    public static String getUAVClientSrc(String appid) {

        ServerVendor vendor = (ServerVendor) UAVServer.instance().getServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR);

        switch (vendor) {
            case JBOSS:
            case JETTY:
            case TOMCAT:
            case SPRINGBOOT:
                return "http://" + NetworkHelper.getLocalIP() + ":"
                        + UAVServer.instance().getServerInfo(CaptureConstants.INFO_APPSERVER_LISTEN_PORT) + "/" + appid;
            case MSCP:
                return "http://" + NetworkHelper.getLocalIP() + ":"
                        + UAVServer.instance().getServerInfo(CaptureConstants.INFO_APPSERVER_LISTEN_PORT);

            case JSE:
            default:
                // TODO ????
                break;
        }

        return "";
    }

    /**
     * formatJDBCURL
     * 
     * @param oriJDBCURI
     * @return
     */
    public static String formatJDBCURL(String oriJDBCURI) {

        // mysql
        if (oriJDBCURI.indexOf("mysql") > -1) {
            return oriJDBCURI;
        }
        // oracle
        else if (oriJDBCURI.indexOf("oracle") > -1) {
            OracleTokenBuffer otb = new OracleTokenBuffer(oriJDBCURI);

            return otb.toString();
        }

        return oriJDBCURI;
    }

    /**
     * 将127.0.0.1或localhost替换成IP地址
     * 
     * @param clientRequestURL
     * @return
     */
    public static String rewriteURLForLocalHost(String clientRequestURL) {

        if (clientRequestURL.indexOf("localhost") > -1 || clientRequestURL.indexOf("127.0.0.1") > -1) {

            String ip = NetworkHelper.getLocalIP();

            clientRequestURL = clientRequestURL.replace("localhost", ip).replace("127.0.0.1", ip);
        }
        return clientRequestURL;
    }

    /**
     * 匹配client url对应的MonitorElementInstance
     * 
     * @param me
     * @param jdbcURL
     * @param appid
     * @return
     */
    public static MonitorElementInstance matchClientUrl(MonitorElement me, String jdbcURL, String appid) {

        jdbcURL = MonitorServerUtil.rewriteURLForLocalHost(jdbcURL);

        String clientId = MonitorServerUtil.getServerHostPort() + "#" + appid + "#" + jdbcURL;

        MonitorElementInstance inst = me.getInstance(clientId);

        return inst;
    }

}
