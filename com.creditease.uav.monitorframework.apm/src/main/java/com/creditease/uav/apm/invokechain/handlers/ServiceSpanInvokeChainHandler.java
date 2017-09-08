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

package com.creditease.uav.apm.invokechain.handlers;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.creditease.agent.helpers.StringHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.log.DataLogger;
import com.creditease.uav.apm.invokechain.span.Span;
import com.creditease.uav.apm.invokechain.spi.InvokeChainCapHandler;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.apm.invokechain.spi.InvokeChainContext;
import com.creditease.uav.profiling.spi.ProfileServiceMapMgr;
import com.creditease.uav.profiling.spi.ProfileServiceMapMgr.ServiceMapBinding;
import com.creditease.uav.util.MonitorServerUtil;

/**
 * 
 * ServerEnd Point InvokeChainHandler usage of server request/response Handler
 * 
 * 
 */

public class ServiceSpanInvokeChainHandler extends InvokeChainCapHandler {

    /**
     * create Span , need check Http-Head if there is traceID, if it has traceId need reuse it
     * 
     */
    @Override
    public void preCap(InvokeChainContext context) {

        String spanMeta = null;
        Span span = null;

        String remote_src = (String) context.get(InvokeChainConstants.PARAM_REMOTE_SRC_INFO);

        if (context.get(CaptureConstants.INFO_CLIENT_APPID) != null
                && !StringHelper.isEmpty(context.get(CaptureConstants.INFO_CLIENT_APPID).toString())) {
            spanMeta = (String) context.get(InvokeChainConstants.PARAM_MQHEAD_SPANINFO);
            span = this.spanFactory.buildSpan(spanMeta, Span.SpanEndpointType.Service, "mq.service");
        }
        else if (context.get(CaptureConstants.INFO_APPSERVER_APPID) != null
                && !StringHelper.isEmpty(context.get(CaptureConstants.INFO_APPSERVER_APPID).toString())) {
            spanMeta = (String) context.get(InvokeChainConstants.PARAM_RPCHEAD_SPANINFO);
            String epinfo = (String) context.get(InvokeChainConstants.PARAM_RPCHEAD_INFO);

            if (context.get(InvokeChainConstants.PARAM_INTECEPTCONTEXT) != null && HttpServletRequest.class
                    .isAssignableFrom(context.get(InvokeChainConstants.PARAM_INTECEPTCONTEXT).getClass())) {
                @SuppressWarnings("rawtypes")
                String httpAction = ((HttpServletRequest) ((Map) context
                        .get(InvokeChainConstants.PARAM_INTECEPTCONTEXT)).get(InterceptConstants.HTTPREQUEST))
                                .getMethod();
                epinfo = epinfo + ",mtd=" + httpAction;
            }

            if (!StringHelper.isEmpty(remote_src)) {
                epinfo = epinfo + ",src=" + remote_src;
            }
            span = this.spanFactory.buildSpan(spanMeta, Span.SpanEndpointType.Service, epinfo);
        }
        else {
            spanMeta = (String) context.get(InvokeChainConstants.PARAM_HTTPHEAD_SPANINFO);

            /**
             * http service can track the remote client ip
             */
            String epinfo = "http.service";

            @SuppressWarnings("rawtypes")
            String httpAction = ((HttpServletRequest) ((Map) context.get(InvokeChainConstants.PARAM_INTECEPTCONTEXT))
                    .get(InterceptConstants.HTTPREQUEST)).getMethod();
            epinfo = epinfo + ",mtd=" + httpAction;
            if (!StringHelper.isEmpty(remote_src)) {
                epinfo = epinfo + ",src=" + remote_src;
            }

            span = this.spanFactory.buildSpan(spanMeta, Span.SpanEndpointType.Service, epinfo);
            if (context.get(InvokeChainConstants.CLIENT_IT_METHOD) != null
                    && !StringHelper.isEmpty(context.get(InvokeChainConstants.CLIENT_IT_METHOD).toString())) {
                span.setMethodName(context.get(InvokeChainConstants.CLIENT_IT_METHOD).toString());
            }
        }

        if (logger.isDebugable() && !StringHelper.isEmpty(spanMeta)) {
            logger.debug("Service InvokeChain SpanMeta:" + spanMeta, null);
        }

        /**
         * store span in thread local for DoCap
         */
        String url = (String) context.get(CaptureConstants.INFO_APPSERVER_CONNECTOR_REQUEST_URL);

        /**
         * for all clients' span creation
         * 
         * TODO: should remove span from thread local ??? we need remove it, but if start another thread, then we can't
         * remove it from thread local, as another thread may access from some kind of client, which trigger a new
         * span~~~
         */
        // 由于像dubbo这类rpc框架会存在injvm调用的情况，若清除会造成调用链断裂
        if (context.get(CaptureConstants.INFO_APPSERVER_APPID) == null) {

            this.spanFactory.removeCurrentThreadValue();
        }
        this.spanFactory.setSpanToContext(url, span);
        this.spanFactory.setSpanToContext("main", span);

        if (logger.isDebugable()) {
            logger.debug("preCap:span=" + span.toString(), null);
        }
    }

    /**
     * Finish Span and Logger the Span metrics
     * 
     */
    @Override
    public void doCap(InvokeChainContext context) {

        /**
         * Step 1: try to get service span,
         * 
         */
        // 移除main，防止uav没有劫持到的请求发生调用链连接现象
        this.spanFactory.getRemoveSpanFromContext("main");
        String url = (String) context.get(CaptureConstants.INFO_APPSERVER_CONNECTOR_REQUEST_URL);
        Span span;
        if (UAVServer.instance().isExistSupportor("com.creditease.uav.apm.supporters.SlowOperSupporter")) {
            span = this.spanFactory.getSpanFromContext(url);
        }
        else {
            span = this.spanFactory.getRemoveSpanFromContext(url);
        }

        if (span == null) {
            return;
        }

        String appid = null;
        if (context.get(CaptureConstants.INFO_CLIENT_APPID) != null
                && !StringHelper.isEmpty(context.get(CaptureConstants.INFO_CLIENT_APPID).toString())) {
            appid = (String) context.get(CaptureConstants.INFO_CLIENT_APPID);
            span.setClassName(context.get(InvokeChainConstants.CLIENT_IT_CLASS).toString());
            span.setMethodName(context.get(InvokeChainConstants.CLIENT_IT_METHOD).toString());
            String responseState = (String) context.get(CaptureConstants.INFO_CLIENT_RESPONSESTATE);
            span.setState(context.get(CaptureConstants.INFO_CLIENT_RESPONSECODE).toString(), responseState);
        }
        else if (context.get(CaptureConstants.INFO_APPSERVER_APPID) != null
                && !StringHelper.isEmpty(context.get(CaptureConstants.INFO_APPSERVER_APPID).toString())) {
            appid = (String) context.get(CaptureConstants.INFO_APPSERVER_APPID);
            span.setClassName(context.get(InvokeChainConstants.CLIENT_IT_CLASS).toString());
            span.setMethodName(context.get(InvokeChainConstants.CLIENT_IT_METHOD).toString());
            String responseState = (String) context.get(CaptureConstants.INFO_CLIENT_RESPONSESTATE);
            span.setState(context.get(CaptureConstants.INFO_APPSERVER_CONNECTOR_RESPONSECODE).toString(),
                    responseState);
        }
        else {
            String appContext = (String) context.get(CaptureConstants.INFO_APPSERVER_CONNECTOR_CONTEXT);
            String realpath = (String) context.get(CaptureConstants.INFO_APPSERVER_CONNECTOR_CONTEXT_REALPATH);

            if (null == appContext && null == realpath) {
                return;
            }
            appid = MonitorServerUtil.getApplicationId(appContext, realpath);

            if (appid == null) {
                return;
            }
            /**
             * match service class & method
             */
            ProfileServiceMapMgr smgr = (ProfileServiceMapMgr) UAVServer.instance()
                    .getServerInfo("profile.servicemapmgr");
            ServiceMapBinding smb = smgr.searchServiceMapBinding(appid, url);

            if (smb != null) {
                span.setClassName(smb.getClazz());
                if (StringHelper.isEmpty(span.getMethodName())) {
                    span.setMethodName(smb.getMethod());
                }
            }

            span.setState(context.get(CaptureConstants.INFO_APPSERVER_CONNECTOR_RESPONSECODE).toString(), null);
        }
        context.put(CaptureConstants.INFO_APPSERVER_APPID, appid);
        /**
         * Step 2: end span
         */
        span.end();

        /**
         * Step 3: set service span info
         */

        span.setAppid(appid);
        span.setUrl(url);

        String spanLog = span.toString();

        if (logger.isDebugable()) {
            logger.debug("doCap:span=" + spanLog, null);
        }

        /**
         * Step 3: get the related invokechain logger of this application to record span
         */
        DataLogger invokeChainLogger = this.getAppInvokeChainLogger(appid);

        if (invokeChainLogger == null) {
            return;
        }

        invokeChainLogger.logData(spanLog);
    }

}
