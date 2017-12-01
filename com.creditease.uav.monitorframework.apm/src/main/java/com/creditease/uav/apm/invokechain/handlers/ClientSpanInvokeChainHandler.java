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

import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.log.DataLogger;
import com.creditease.uav.apm.invokechain.span.Span;
import com.creditease.uav.apm.invokechain.spi.InvokeChainCapHandler;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.apm.invokechain.spi.InvokeChainContext;

/**
 * 
 * ClientEnd Point InvokeChainHandler usage of client request/response Handler
 * 
 * 
 */

public class ClientSpanInvokeChainHandler extends InvokeChainCapHandler {

    @Override
    public void preCap(InvokeChainContext context) {

        /**
         * Step 1: get parent span from thread local
         */
        Span parentSpan = this.spanFactory.getSpanFromContext("main");

        /**
         * Step 2: build a new sub span
         */
        String clientType = (String) context.get(CaptureConstants.INFO_CLIENT_TYPE);

        String httpAction = (String) context.get(CaptureConstants.INFO_CLIENT_REQUEST_ACTION);

        Span span = this.spanFactory.buildSpan(parentSpan, Span.SpanEndpointType.Client,
                clientType + ",mtd=" + httpAction);

        /**
         * Step 3: get client invoke context info: such as Class, Method, URL
         */
        int level = Integer.parseInt(String.valueOf(context.get(InvokeChainConstants.CLIENT_IT_KEY)));
        String clazz = String.valueOf(context.get(InvokeChainConstants.CLIENT_IT_CLASS));

        setCallerThreadInfo(span, level, clazz);

        String url = (String) context.get(CaptureConstants.INFO_CLIENT_REQUEST_URL);
        span.setUrl(url);

        /**
         * store span in thread local for DoCap
         */
        String storeKey = url + "@" + span.getSpanId();

        this.spanFactory.setSpanToContext(storeKey, span);

        context.put(InvokeChainConstants.CLIENT_SPAN_THREADLOCAL_STOREKEY, storeKey);

        if (logger.isDebugable()) {
            logger.debug("preCap: span=" + span.toString(), null);
        }
    }

    /**
     * Finish Span and Logger the Span Metrics
     */
    @Override
    public void doCap(InvokeChainContext context) {

        String storeKey = (String) context.get(InvokeChainConstants.CLIENT_SPAN_THREADLOCAL_STOREKEY);

        // in the same thread
        Span span = this.spanFactory.getRemoveSpanFromContext(storeKey);

        if (span == null) {
            // in async thread
            span = (Span) context.get(InvokeChainConstants.PARAM_SPAN_KEY);
        }

        if (span == null) {
            return;
        }

        context.put(InvokeChainConstants.PARAM_SPAN_KEY, span);

        String appid = (String) context.get(CaptureConstants.INFO_CLIENT_APPID);

        if (appid == null) {
            return;
        }

        span.end();

        span.setAppid(appid);

        String responseState = (String) context.get(CaptureConstants.INFO_CLIENT_RESPONSESTATE);

        span.setState(context.get(CaptureConstants.INFO_CLIENT_RESPONSECODE).toString(), responseState);

        DataLogger invokeChainLogger = this.getAppInvokeChainLogger(appid);

        String spanLog = span.toString();

        invokeChainLogger.logData(spanLog);

        if (logger.isDebugable()) {
            logger.debug("doCap:span=" + spanLog, null);
        }
    }

}
