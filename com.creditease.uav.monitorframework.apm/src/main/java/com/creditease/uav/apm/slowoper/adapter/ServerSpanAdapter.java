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

package com.creditease.uav.apm.slowoper.adapter;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.uav.apm.RewriteIvcRequestWrapper;
import com.creditease.uav.apm.RewriteIvcResponseWrapper;
import com.creditease.uav.apm.invokechain.span.Span;
import com.creditease.uav.apm.invokechain.spi.InvokeChainAdapter;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.apm.invokechain.spi.InvokeChainContext;
import com.creditease.uav.apm.slowoper.spi.SlowOperConstants;
import com.creditease.uav.apm.slowoper.spi.SlowOperContext;

public class ServerSpanAdapter extends InvokeChainAdapter {

    @Override
    public void beforePreCap(InvokeChainContext context, Object[] args) {

        // do noting
    }

    @Override
    public void afterPreCap(InvokeChainContext context, Object[] args) {

        if (UAVServer.instance().isExistSupportor("com.creditease.uav.apm.supporters.SlowOperSupporter")) {

            String url = (String) context.get(CaptureConstants.INFO_APPSERVER_CONNECTOR_REQUEST_URL);
            Span span = this.spanFactory.getSpanFromContext(url);

            SlowOperContext slowOperContext = new SlowOperContext();

            HttpServletRequest request = (HttpServletRequest) args[0];
            // 由于会存在没有拦截住的情况，但当前无论拦截与未拦截的情况均可以使用HttpServletRequest获取header，故此处预留出来
            // if (RewriteIvcRequestWrapper.class.isAssignableFrom(args[0].getClass())) {
            // }
            // else {
            //
            // }
            // 当前将所有header信息打印出来，后续可能只打印用户自定义的将UAV的header过滤
            // 提取参数并放入header里
            String parameters = JSONHelper.toString(request.getParameterMap());
            if (parameters == null) {
                parameters = "{}";
            }
            slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_REQ_HEADER, getRequestHeaders(request) + parameters);
            Object params[] = { span, slowOperContext };
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.SlowOperSupporter", "runCap",
                    span.getEndpointInfo().split(",")[0], InvokeChainConstants.CapturePhase.PRECAP, context, params);

        }
    }

    @Override
    public void beforeDoCap(InvokeChainContext context, Object[] args) {

        // do nothing
    }

    @Override
    public void afterDoCap(InvokeChainContext context, Object[] args) {

        if (UAVServer.instance().isExistSupportor("com.creditease.uav.apm.supporters.SlowOperSupporter")) {

            String url = (String) context.get(CaptureConstants.INFO_APPSERVER_CONNECTOR_REQUEST_URL);
            Span span = this.spanFactory.getRemoveSpanFromContext(url);

            if (span == null) {
                return;
            }

            SlowOperContext slowOperContext = new SlowOperContext();
            // 防止有没有拦截住的请求
            if (RewriteIvcRequestWrapper.class.isAssignableFrom(args[0].getClass())
                    && RewriteIvcResponseWrapper.class.isAssignableFrom(args[1].getClass())) {
                RewriteIvcRequestWrapper request = (RewriteIvcRequestWrapper) args[0];
                slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_REQ_BODY, request.getContent().toString());
                request.clearBodyContent();

                RewriteIvcResponseWrapper response = (RewriteIvcResponseWrapper) args[1];
                slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_RSP_HEADER, getResponHeaders(response));
                slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_RSP_BODY, response.getContent().toString());
                response.clearBodyContent();
            }
            else {
                slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_REQ_BODY, "unsupported request");
                HttpServletResponse response = (HttpServletResponse) args[1];
                slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_RSP_HEADER, getResponHeaders(response));
                slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_RSP_BODY, "unsupported response");
            }

            Object params[] = { span, slowOperContext };
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.SlowOperSupporter", "runCap",
                    span.getEndpointInfo().split(",")[0], InvokeChainConstants.CapturePhase.DOCAP, context, params);
        }
    }

    /**
     * 根据HttpServletRequest获取指定格式的headers
     * 
     * @param request
     * @return
     */
    private String getRequestHeaders(HttpServletRequest request) {

        Map<String, String> result = new HashMap<String, String>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            result.put(key, request.getHeader(key));
        }
        return JSONHelper.toString(result);
    }

    /**
     * 根据HttpServletRequest获取指定格式的headers
     * 
     * @param request
     * @return
     */
    private String getResponHeaders(HttpServletResponse response) {

        Map<String, String> result = new HashMap<String, String>();
        for (String key : response.getHeaderNames()) {
            result.put(key, response.getHeader(key));
        }
        return JSONHelper.toString(result);
    }

}
