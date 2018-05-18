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

package com.creditease.uav.profiling.handlers;

import java.net.URI;
import java.util.Map;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.uav.common.BaseComponent;
import com.creditease.uav.profiling.spi.ProfileConstants;
import com.creditease.uav.profiling.spi.ProfileContext;
import com.creditease.uav.profiling.spi.ProfileElement;
import com.creditease.uav.profiling.spi.ProfileElementInstance;
import com.creditease.uav.profiling.spi.ProfileHandler;
import com.creditease.uav.util.LimitConcurrentHashMap;
import com.creditease.uav.util.MonitorServerUtil;

public class IPLinkProfileHandler extends BaseComponent implements ProfileHandler {

    @Override
    public void doProfiling(ProfileElement elem, ProfileContext context) {

        if (!ProfileConstants.PROELEM_IPLINK.equals(elem.getElemId())) {
            return;
        }

        /**
         * step 1:check if the request is from a browser(human) or an application
         */

        String clientAddr = (String) context.get(ProfileConstants.PC_ARG_CLIENTADDR);

        // in case the wizard thing
        if (clientAddr == null) {
            return;
        }

        String usrAgent = (String) context.get(ProfileConstants.PC_ARG_IPLINK_USRAGENT);

        clientAddr = MonitorServerUtil.getLocalHostToIP(clientAddr);

        String uavClientTag = (String) context.get(ProfileConstants.PC_ARG_UAVCLIENT_TAG);

        String ipLink = (String) context.get(ProfileConstants.PC_ARG_IPLNK);

        String proxyHost = (String) context.get(ProfileConstants.PC_ARG_PROXYHOST);

        String proxyAddress = (proxyHost == null) ? clientAddr : MonitorServerUtil.getLocalHostToIP(proxyHost);

        // proxyAddress = MonitorServerUtil.tryGetIPbyDNS(proxyAddress);

        boolean isFromHttpProxy = (StringHelper.isEmpty(ipLink)) ? false : true;

        boolean isFromBrowser = MonitorServerUtil.isFromBrowser(usrAgent);

        long curTime = System.currentTimeMillis();

        String ipLinkKey = "";

        String realClientIP = null;
        /**
         * there are 4 types: god!!!
         */
        // User + Proxy
        if (isFromBrowser == true && isFromHttpProxy == true) {

            realClientIP = MonitorServerUtil.getClientIP(clientAddr, ipLink);

            realClientIP = "user://" + realClientIP;

            // proxy ip address
            ipLinkKey = "proxy://" + proxyAddress;
        }
        // User
        else if (isFromBrowser == true && isFromHttpProxy == false) {

            realClientIP = "user://" + clientAddr;

            String appid = (String) context.get(ProfileConstants.PC_ARG_APPID);
            String svrhost = (String) context.get(ProfileConstants.PC_ARG_SVRHOST);
            // client ip
            ipLinkKey = "browser://" + svrhost + "/" + appid;
        }
        // App + Proxy
        else if (isFromBrowser == false && isFromHttpProxy == true) {

            realClientIP = MonitorServerUtil.getClientIP(clientAddr, ipLink);

            realClientIP = "app://" + realClientIP;

            // if there UAV-Client-Src, use it instead of IP address
            realClientIP = (uavClientTag == null) ? realClientIP : uavClientTag;

            // proxy ip address
            ipLinkKey = "proxy://" + proxyAddress;
        }
        // App
        else if (isFromBrowser == false && isFromHttpProxy == false) {

            clientAddr = "app://" + clientAddr;

            // if there UAV-Client-Src, use it instead of IP address
            ipLinkKey = (uavClientTag == null) ? clientAddr : uavClientTag;
        }

        /**
         * step 1: tracking ip link
         */
        ProfileElementInstance pei = elem.getInstance(ipLinkKey);

        pei.setValue("ts", curTime);

        /**
         * step 2: see if should exclud some url, such as js, css, image as they are the part of page url NOTE: but
         * still we record this access to app page, but the timstamp may be the last one(js,css,image), they all go to
         * app page
         */
        getAccessTargetInfo(context, curTime, pei);

        /**
         * step 3: only work for those requests for http proxy
         */
        getClientInfo(curTime, pei, realClientIP);
    }

    private void getClientInfo(long curTime, ProfileElementInstance pei, String realClientIP) {

        if (realClientIP == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Long> urlset = (Map<String, Long>) pei.getValues().get(ProfileConstants.PEI_CLIENTS);

        if (urlset == null) {
            int limit = DataConvertHelper.toInt(System.getProperty("com.creditease.uav.iplink.clients.limit"), 500);
            urlset = new LimitConcurrentHashMap<String, Long>(limit);
            pei.setValue(ProfileConstants.PEI_CLIENTS, urlset);
        }

        urlset.put(realClientIP, curTime);
    }

    private void getAccessTargetInfo(ProfileContext context, long curTime, ProfileElementInstance pei) {

        String iplinkTargetUrl = (String) context.get(ProfileConstants.PC_ARG_IPLINK_TARGETURL);

        if (MonitorServerUtil.isIncludeMonitorURL(iplinkTargetUrl) == false) {
            return;
        }

        String appid = (String) context.get(ProfileConstants.PC_ARG_APPID);

        /**
         * we only need record the relative path url to this app, this helps to reduce the size
         */
        URI iplnkTargetURI = DataConvertHelper.toURI(iplinkTargetUrl);

        String path;
        if (iplnkTargetURI == null) {
            path = iplinkTargetUrl;
        }
        else {
            path = iplnkTargetURI.getPath();
        }
        path = MonitorServerUtil.cleanIplnkTargetURL(path);
        if (path.indexOf("/" + appid) == 0) {
            path = path.substring(("/" + appid).length());
        }

        /**
         * step 3: record the access url index into the ip_link's url set
         */
        @SuppressWarnings("unchecked")
        Map<String, Long> urlset = (Map<String, Long>) pei.getValues().get(ProfileConstants.PEI_URLS);

        if (urlset == null) {
            int limit = DataConvertHelper.toInt(System.getProperty("com.creditease.uav.iplink.urls.limit"), 500);
            urlset = new LimitConcurrentHashMap<String, Long>(limit);
            pei.setValue(ProfileConstants.PEI_URLS, urlset);
        }

        urlset.put(path, curTime);
    }

}
