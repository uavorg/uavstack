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

package com.creditease.monitor.handlers;

import com.creditease.agent.helpers.NetworkHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.MonitorUrlFilterMgr;
import com.creditease.monitor.captureframework.MonitorUrlFilterMgr.ListType;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.CaptureContext;
import com.creditease.monitor.captureframework.spi.MonitorElemCapHandler;
import com.creditease.monitor.captureframework.spi.MonitorElement;
import com.creditease.monitor.captureframework.spi.MonitorElementInstance;
import com.creditease.uav.profiling.StandardProfileContext;
import com.creditease.uav.profiling.spi.Profile;
import com.creditease.uav.profiling.spi.ProfileConstants;
import com.creditease.uav.profiling.spi.ProfileContext;
import com.creditease.uav.profiling.spi.ProfileFactory;
import com.creditease.uav.util.MonitorServerUtil;

/**
 * @author zhen zhang
 */
public class ServerEndRespTimeCapHandler extends AbsServerRespTimeCapHandler implements MonitorElemCapHandler {

    private String serverAddress = null;

    private NoHttpServiceRespTimeCapHandler noHttpServRespHandler;

    public ServerEndRespTimeCapHandler() {
        noHttpServRespHandler = new NoHttpServiceRespTimeCapHandler();
    }

    @Override
    public void preCap(MonitorElement elem, CaptureContext context) {

        long st = System.currentTimeMillis();
        context.put("ServerEndRespTime.startTime", st);
    }

    @Override
    public void doCap(MonitorElement elem, CaptureContext context) {

        if (elem == null || context == null) {
            return;
        }

        MonitorElementInstance inst = null;

        String urlInfo = (String) context.get(CaptureConstants.INFO_APPSERVER_CONNECTOR_REQUEST_URL);

        if (urlInfo == null) {
            return;
        }

        /**
         * For none http service
         */
        if (urlInfo.startsWith("http") == false) {
            // as preCap is done by ServerEndRespTimeCapHandler, only doCap is need
            noHttpServRespHandler.doCap(elem, context);
            return;
        }

        /**
         * For application server http service
         */
        if (serverAddress == null) {
            String localPort = context.get(CaptureConstants.INFO_APPSERVER_CONNECTOR_LISTENPORT).toString();
            String localIP = NetworkHelper.getLocalIP();
            serverAddress = localIP + ":" + localPort;
        }

        String[] infos = rewriteServerAddress(urlInfo);

        if (infos == null) {
            return;
        }

        if (CaptureConstants.MOELEM_SERVER_RESPTIME_SYSTEM.equals(elem.getMonitorElemId())) {

            String appContext = (String) context.get(CaptureConstants.INFO_APPSERVER_CONNECTOR_CONTEXT);
            if (!StringHelper.isEmpty(appContext) && "/com.creditease.uav".equalsIgnoreCase(appContext)) {
                return;
            }

            inst = elem.getInstance(infos[1]);
        }
        else if (CaptureConstants.MOELEM_SERVER_RESPTIME_URL.equals(elem.getMonitorElemId())) {

            String urlId = infos[2];

            /**
             * NOTE: page need iplink profiling, then we can find out users
             */
            if (MonitorServerUtil.isIncludeMonitorURLForService(urlId) == false
                    && MonitorServerUtil.isIncludeMonitorURLForPage(urlId) == false) {
                return;
            }

            String appContext = (String) context.get(CaptureConstants.INFO_APPSERVER_CONNECTOR_CONTEXT);
            String realpath = (String) context.get(CaptureConstants.INFO_APPSERVER_CONNECTOR_CONTEXT_REALPATH);
            String appId = MonitorServerUtil.getApplicationId(appContext, realpath);
            if (appContext != null || realpath != null) {

                // get client ip invoking link for this application url

                /**
                 * NOTE: as the internal application, we will not monitor it
                 */
                if ("com.creditease.uav".equalsIgnoreCase(appId)) {
                    return;
                }

                doProfileIPLink(context, appId, urlId);
            }

            if (needMonitor(appId, infos) == false) {
                return;
            }

            urlId = infos[2];
            inst = elem.getInstance(urlId);
        }
        else if (CaptureConstants.MOELEM_SERVER_RESPTIME_APP.equals(elem.getMonitorElemId()))

        {

            String appContext = (String) context.get(CaptureConstants.INFO_APPSERVER_CONNECTOR_CONTEXT);
            String realpath = (String) context.get(CaptureConstants.INFO_APPSERVER_CONNECTOR_CONTEXT_REALPATH);
            if (appContext == null) {
                return;
            }

            appContext = (appContext.equalsIgnoreCase("")) ? "" : appContext.substring(1);

            String appurl = infos[1] + "/" + appContext;
            String appId = MonitorServerUtil.getApplicationId(appContext, realpath);

            /**
             * NOTE: as the internal application, we will not monitor it
             */
            if ("com.creditease.uav".equalsIgnoreCase(appId)) {
                return;
            }

            String appuuId = appurl + "---" + appId;
            inst = elem.getInstance(appuuId);

        }

        if (MonitorUrlFilterMgr.getInstance().isMatchingUrlByType(ListType.SERVERURL_IGNORELIST, infos[3]) == true
                && MonitorUrlFilterMgr.getInstance().isMatchingUrlByType(ListType.SERVERURL_WHITELIST,
                        infos[3]) == false) {
            // if in Ignorelist and not in whitelist, do not collect
            return;
        }

        recordCounters(context, inst);
    }

    private boolean needMonitor(String appId, String urlInfos[]) {

        String reUrl = urlInfos[3];

        /**
         * NOTE: do not collect static file, e.g.(*.jpg, *.doc, *.css) ;
         */

        if (MonitorServerUtil.isIncludeMonitorURL(reUrl) == false) {
            return false;
        }

        /**
         * NOTE: if in whitelist, collect it ;
         */
        if (MonitorUrlFilterMgr.getInstance().isMatchingUrlByType(ListType.SERVERURL_WHITELIST, reUrl) == true) {

            return true;
        }

        /**
         * NOTE: only collect monitor data
         */
        if (MonitorServerUtil.isIncludeMonitorURLForService(reUrl) == false
                || MonitorServerUtil.isIncludeMonitorURLForPage(reUrl) == true) {

            return false;
        }

        /**
         * NOTE: if in blacklist, do not collect;
         */
        if (MonitorUrlFilterMgr.getInstance().isMatchingUrlByType(ListType.SERVERURL_IGNORELIST, reUrl) == true) {

            return false;
        }

        /**
         * NOTE: MSCP no need profile validation as it is 更加简约比复杂的JEE
         */
        if (UAVServer.ServerVendor.MSCP == UAVServer.instance().getServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR)) {
            return true;
        }
        /**
         * NOTE: only JEE App need profile validation,if not pass the profile validate, do not collect;
         */
        if (MonitorUrlFilterMgr.getInstance().serviceValidate(appId, reUrl, urlInfos) == false) {
            return false;
        }

        return true;

    }

    /**
     * NOTE: as client ip links are generated by runtime invoking, we have to do this in monitor
     * 
     * @param context
     * @param appName
     * @param url
     */
    private void doProfileIPLink(CaptureContext context, String appId, String url) {

        String clientAddr = (String) context.get(CaptureConstants.INFO_APPSERVER_CONNECTOR_CLIENTADDR);

        if (StringHelper.isEmpty(clientAddr)) {
            return;
        }

        Profile p = ProfileFactory.instance().getProfile(appId);

        if (p == null) {
            return;
        }

        ProfileContext pc = new StandardProfileContext();

        pc.put(ProfileConstants.PC_ARG_CLIENTADDR, clientAddr);

        String uavClientTag = (String) context.get(CaptureConstants.INFO_APPSERVER_UAVCLIENT_TAG);

        if (!StringHelper.isEmpty(uavClientTag)) {
            pc.put(ProfileConstants.PC_ARG_UAVCLIENT_TAG, uavClientTag);
        }

        String forwardIPLink = (String) context.get(CaptureConstants.INFO_APPSERVER_CONNECTOR_FORWARDADDR);

        if (!StringHelper.isEmpty(forwardIPLink)) {
            pc.put(ProfileConstants.PC_ARG_IPLNK, forwardIPLink);
        }

        if (!StringHelper.isEmpty(url)) {
            pc.put(ProfileConstants.PC_ARG_IPLINK_TARGETURL, url);
        }

        String userAgent = (String) context.get(CaptureConstants.INFO_APPSERVER_CLIENT_USRAGENT);

        if (!StringHelper.isEmpty(userAgent)) {
            pc.put(ProfileConstants.PC_ARG_IPLINK_USRAGENT, userAgent);
        }

        String proxyhost = (String) context.get(CaptureConstants.INFO_APPSERVER_PROXY_HOST);

        if (!StringHelper.isEmpty(proxyhost)) {
            pc.put(ProfileConstants.PC_ARG_PROXYHOST, proxyhost);
        }

        pc.put(ProfileConstants.PC_ARG_APPID, appId);
        pc.put(ProfileConstants.PC_ARG_SVRHOST, this.serverAddress);

        pc.addPE(ProfileConstants.PROELEM_IPLINK);

        p.doProfiling(pc);

    }

    private String[] rewriteServerAddress(String urlInfo) {

        String[] args = new String[4];

        int st = urlInfo.indexOf("//");

        // http schema
        String httpS = urlInfo.substring(0, st);

        args[0] = httpS;

        // http server address
        args[1] = httpS + "//" + this.serverAddress;

        // url
        String temp = urlInfo.substring(st + 2);

        int st2 = temp.indexOf("/");

        // get the real schema://host:port for this url

        String reUrl = temp.substring(st2);

        reUrl = MonitorServerUtil.cleanRelativeURL(reUrl);

        args[3] = reUrl;
        /**
         * note: for JEE Application, the port is the same, but for MSCP Application , each URL may has its own port
         */
        if (UAVServer.ServerVendor.MSCP == UAVServer.instance().getServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR)) {
            String urlHostPort = temp.substring(0, st2);

            String[] hostPortInfo = urlHostPort.split(":");

            if (hostPortInfo.length != 2) {
                return null;
            }

            hostPortInfo[0] = MonitorServerUtil.getLocalHostToIP(hostPortInfo[0]);

            if (hostPortInfo[0] == null) {
                return null;
            }

            args[2] = httpS + "//" + hostPortInfo[0] + ":" + hostPortInfo[1] + reUrl;

        }
        else {
            args[2] = args[1] + reUrl;
        }
        return args;
    }

    @Override
    public void preStore(MonitorElementInstance instance) {

        long sumRespTime = instance.getValueLong(CaptureConstants.MEI_RESP_SUMTIME);
        long sumRespCounter = instance.getValueLong(CaptureConstants.MEI_COUNTER);

        long avgRespTime = -1;
        if (sumRespTime > 0 && sumRespCounter > 0) {
            avgRespTime = sumRespTime / sumRespCounter;
        }

        instance.setValue(CaptureConstants.MEI_RESP_AVGTIME, avgRespTime);

    }

}
