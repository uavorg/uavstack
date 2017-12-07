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

import java.text.DecimalFormat;

import com.creditease.agent.helpers.StringHelper;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.log.DataLogger;
import com.creditease.uav.apm.invokechain.span.Span;
import com.creditease.uav.apm.invokechain.spi.InvokeChainCapHandler;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.apm.invokechain.spi.InvokeChainContext;

public class MethodSpanInvokeChainHandler extends InvokeChainCapHandler {

    private DecimalFormat mSeqCountFormat = new DecimalFormat("0000");

    @Override
    public void preCap(InvokeChainContext context) {

        /**
         * Step 1: get parent span from thread local
         */
        Span parentSpan = this.spanFactory.getSpanFromContext("main");

        /**
         * Step 2: build a new sub span
         */
        Span span = this.spanFactory.buildSpan(parentSpan, Span.SpanEndpointType.Method, "");

        String cls = (String) context.get(InvokeChainConstants.METHOD_SPAN_CLS);
        String method = (String) context.get(InvokeChainConstants.METHOD_SPAN_MTD);
        String sign = (String) context.get(InvokeChainConstants.METHOD_SPAN_MTDSIGN);
        span.setClassName(cls);
        span.setMethodName(method + ((StringHelper.isEmpty(sign)) ? "" : "(" + sign + ")"));
        span.setEndpointInfo(mSeqCountFormat.format(parentSpan.incrementAndGetSpanMethodSeqCounter()));
        span.setUrl("");

        String storekey = new StringBuilder().append(cls).append(".").append(method).append(".").append(sign)
                .toString();

        context.put(InvokeChainConstants.METHOD_SPAN_STOREKEY, storekey);
        context.put(storekey, span);

        if (logger.isDebugable()) {
            logger.debug("preCap: span=" + span.toString(), null);
        }
    }

    @Override
    public void doCap(InvokeChainContext context) {

        String storeKey = (String) context.get(InvokeChainConstants.METHOD_SPAN_STOREKEY);

        if (storeKey == null) {
            return;
        }

        Span span = (Span) context.get(storeKey);

        if (span == null) {
            return;
        }

        String appid = (String) context.get(InvokeChainConstants.METHOD_SPAN_APPID);

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
