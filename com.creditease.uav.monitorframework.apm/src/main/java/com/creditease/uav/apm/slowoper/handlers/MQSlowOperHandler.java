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

package com.creditease.uav.apm.slowoper.handlers;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.monitor.log.DataLogger;
import com.creditease.uav.apm.invokechain.span.Span;
import com.creditease.uav.apm.invokechain.spi.InvokeChainContext;
import com.creditease.uav.apm.slowoper.span.SlowOperSpan;
import com.creditease.uav.apm.slowoper.spi.SlowOperConstants;
import com.creditease.uav.apm.slowoper.spi.SlowOperContext;

/**
 * MQSlowOperHandler description: MQ common slow operation handler
 *
 */
public class MQSlowOperHandler extends AbstractSlowOperHandler {

    @Override
    public void doCap(InvokeChainContext context, Object[] args) {

        Span span = (Span) args[0];
        DataLogger invokeChainLogger = this.getAppInvokeChainLogger(span.getAppid());
        if (invokeChainLogger == null) {
            return;
        }
        SlowOperSpan slowOperSpan = this.spanFactory.getRemoveSlowOperSpanFromContext(this.getSlowOperSpanKey(span));
        if (slowOperSpan == null) {
            return;
        }
        invokeChainLogger.logData(slowOperSpan.toString());
    }

    @Override
    public void buildSpanContent(SlowOperContext slowOperContext, SlowOperSpan slowOperSpan) {

        String body = (String) slowOperContext.get(SlowOperConstants.PROTOCOL_MQ_RABBIT_BODY);
        // 限定采集的协议体的大小
        int bodyLength = DataConvertHelper.toInt(System.getProperty("com.creditease.uav.ivcdat.mq.body"), 2000);
        slowOperSpan.appendContent(body, bodyLength, true);
    }

}
