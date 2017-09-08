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

package com.creditease.uav.apm.uem;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.creditease.monitor.globalfilter.jee.AbsJEEGlobalFilterHandler;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.log.DataLogger;
import com.creditease.uav.util.MonitorServerUtil;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;

/**
 * 
 * UEMServiceGFHandler description: collect the hook.js's submit data and log the UEM data
 *
 */
public class UEMServiceGFHandler extends AbsJEEGlobalFilterHandler {

    public UEMServiceGFHandler(String id) {
        super(id);
    }

    @Override
    public String getContext() {

        return "com.creditease.uav/apm/uem";
    }

    /**
     * getUA
     * 
     * @param fdata
     * @param request
     */
    private void getUA(StringBuilder fdata, HttpServletRequest request) {

        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));

        String bwrVersion = userAgent.getBrowserVersion().getMajorVersion();
        Browser bwr = userAgent.getBrowser();
        String bwrType = bwr.getBrowserType().getName();
        String bwrName = bwr.getName();
        String bwrEngine = bwr.getRenderingEngine().name();

        fdata.append(bwrName).append(";");
        fdata.append(bwrType).append(";");
        fdata.append(bwrEngine).append(";");
        fdata.append(bwrVersion).append(";");

        OperatingSystem os = userAgent.getOperatingSystem();

        String osName = os.getName();
        String deType = os.getDeviceType().getName();
        fdata.append(osName).append(";");
        fdata.append(deType).append(";");
    }

    /**
     * getRealClientIP
     * 
     * @param fdata
     * @param request
     */
    private void getRealClientIP(StringBuilder fdata, HttpServletRequest request) {

        String xforwardf = request.getHeader("X-Forwarded-For");

        String clientIP = request.getRemoteAddr();

        String realClientIP = MonitorServerUtil.getClientIP(clientIP, xforwardf);

        fdata.append(realClientIP).append(";");
    }

    private void logData(String appid, String data) {

        DataLogger dl = this.getSupporter(UEMSupporter.class).getDataLogger("uem", appid);

        if (dl == null) {
            return;
        }

        dl.logData(data);
    }

    @Override
    protected void doRequest(HttpServletRequest request, HttpServletResponse response, InterceptContext ic) {

        String contextPath = request.getServletContext().getContextPath();
        String readlPath = request.getServletContext().getRealPath("");

        String appid = MonitorServerUtil.getApplicationId(contextPath, readlPath);
        try {
            request.setCharacterEncoding("utf-8");
        }
        catch (UnsupportedEncodingException e) {
            // ignore
        }

        StringBuilder fdata = new StringBuilder();

        getRealClientIP(fdata, request);

        getUA(fdata, request);

        String data = this.getRequestBodyAsString(request, "utf-8");

        fdata.append(data);

        logData(appid, fdata.toString());

        // allow-cross domain
        response.setHeader("Access-Control-Allow-Origin", "*");
    }

    @Override
    protected void doResponse(HttpServletRequest request, HttpServletResponse response, InterceptContext ic) {

        // not implement
    }

}
