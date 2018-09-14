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

package com.creditease.uav.apm.invokechain.jee;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.creditease.agent.helpers.ReflectionHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.globalfilter.jee.AbsJEEGlobalFilterHandler;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.uav.apm.RewriteIvcRequestWrapper;
import com.creditease.uav.apm.RewriteIvcResponseWrapper;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.apm.slowoper.adapter.ServerSpanAdapter;
import com.creditease.uav.util.MonitorServerUtil;

/**
 * 
 * JEEServiceRunGlobalFilterHandler description: for JEEService Service Span START & END
 *
 */
public class JEEServiceRunGlobalFilterHandler extends AbsJEEGlobalFilterHandler {

    public JEEServiceRunGlobalFilterHandler(String id) {

        super(id);
    }

    @Override
    public String getContext() {

        return "";
    }

    @Override
    public boolean isBlockHandlerChain() {

        return false;
    }

    @Override
    public boolean isBlockFilterChain() {

        return false;
    }

    @Override
    protected void doRequest(HttpServletRequest request, HttpServletResponse response, InterceptContext context) {

        StringBuffer bf = request.getRequestURL();

        if (bf == null) {
            return;
        }

        String urlInfo = bf.toString();

        if ((!MonitorServerUtil.isIncludeMonitorURLForService(urlInfo)
                && !MonitorServerUtil.isIncludeMonitorURLForPage(urlInfo))
                || "/com.creditease.uav".equals(getReqContextPath(request))) {
            return;
        }

        if (logger.isDebugable()) {
            logger.debug("JEEServiceStartInvokeChainHandler called for url:" + urlInfo, null);
        }
        Object args[];
        // 重调用链开启时添加warpper
        if (UAVServer.instance().isExistSupportor("com.creditease.uav.apm.supporters.SlowOperSupporter")) {

            RewriteIvcRequestWrapper requestWrapper = new RewriteIvcRequestWrapper(request, "IVC_DAT");
            context.put(InterceptConstants.HTTPREQUEST, requestWrapper);

            RewriteIvcResponseWrapper responseWrapper = new RewriteIvcResponseWrapper(response, "IVC_DAT");
            context.put(InterceptConstants.HTTPRESPONSE, responseWrapper);

            args = new Object[] { requestWrapper, responseWrapper };
        }
        else {
            args = new Object[] { request, response };
        }

        Map<String, Object> params = new HashMap<String, Object>();

        /**
         * try to get SPAN_KEY_HEAD from http request header
         */
        HttpServletRequest httprequest = (HttpServletRequest) context.get(InterceptConstants.HTTPREQUEST);

        String traceContext = httprequest.getHeader(InvokeChainConstants.PARAM_HTTPHEAD_SPANINFO);
        String method = httprequest.getHeader(InvokeChainConstants.CLIENT_IT_METHOD);
        if (!StringHelper.isEmpty(traceContext)) {
            params.put(InvokeChainConstants.PARAM_HTTPHEAD_SPANINFO, traceContext);
        }
        if (!StringHelper.isEmpty(method)) {
            params.put(InvokeChainConstants.CLIENT_IT_METHOD, method);
        }

        String clientip = MonitorServerUtil.getClientIP(httprequest.getRemoteAddr(),
                httprequest.getHeader("X-Forwarded-For"));

        // put intercept context
        params.put(InvokeChainConstants.PARAM_INTECEPTCONTEXT, context);
        params.put(InvokeChainConstants.PARAM_REMOTE_SRC_INFO, clientip);

        // put request url
        params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_REQUEST_URL, request.getRequestURL().toString());

        // register adapter
        UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "registerAdapter",
                ServerSpanAdapter.class);

        UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                InvokeChainConstants.CHAIN_APP_SERVICE, InvokeChainConstants.CapturePhase.PRECAP, params,
                ServerSpanAdapter.class, args);
    }

    @Override
    protected void doResponse(HttpServletRequest request, HttpServletResponse response, InterceptContext ic) {

		StringBuffer bf = request.getRequestURL();

        if (bf == null) {
            return;
        }

        String urlInfo = bf.toString();

        if ((!MonitorServerUtil.isIncludeMonitorURLForService(urlInfo)
                && !MonitorServerUtil.isIncludeMonitorURLForPage(urlInfo))
                || "/com.creditease.uav".equals(getReqContextPath(request))) {
            return;
        }
	
        Map<String, Object> params = new HashMap<String, Object>();

        params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_REQUEST_URL, request.getRequestURL().toString());
        params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_CONTEXT, getReqContextPath(request));
        params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_CONTEXT_REALPATH, getReqRealPath(request));
        params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_RESPONSECODE, getRespRetStatus(response));

        Object args[] = { request, response };

        // invoke chain
        UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                InvokeChainConstants.CHAIN_APP_SERVICE, InvokeChainConstants.CapturePhase.DOCAP, params,
                ServerSpanAdapter.class, args);

    }

    /**
     * 解决低版本Servlet兼容问题，My God！！！
     * 
     * @param response
     * @return
     */
    private int getRespRetStatus(HttpServletResponse response) {

        try {
            return response.getStatus();
        }
        catch (Error e) {

            Object resp = response;
            // 重调用链开启后这里的response是RewriteIvcResponseWrapper
            if ("com.creditease.uav.apm.RewriteIvcResponseWrapper".equals(response.getClass().getName())) {
                resp = ReflectionHelper.getField(response.getClass(), response, "response");
                if (resp == null) {
                    return 0;
                }
            }

            if (resp == null) {
                return 0;
            }
            Object result = null;
            // for tomcat 6.0.4x
            if ("org.apache.catalina.connector.ResponseFacade".equals(resp.getClass().getName())) {
                resp = ReflectionHelper.getField(resp.getClass(), resp, "response");
                if (resp != null) {
                    result = ReflectionHelper.invoke(resp.getClass().getName(), resp, "getStatus", null, null,
                            response.getClass().getClassLoader());
                }
            }

            if (result == null) {
                return 0;
            }

            return (Integer) result;
        }
    }

    @SuppressWarnings("deprecation")
    private String getReqRealPath(HttpServletRequest request) {

        try {
            return request.getServletContext().getRealPath("");
        }
        catch (Error e) {
            return request.getRealPath("");
        }
    }

    private String getReqContextPath(HttpServletRequest request) {

        try {
            return request.getServletContext().getContextPath();
        }
        catch (Error e) {
            return request.getContextPath();
        }
    }
}
