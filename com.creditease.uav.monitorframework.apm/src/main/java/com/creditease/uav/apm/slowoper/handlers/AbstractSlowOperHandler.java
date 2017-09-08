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

import com.creditease.monitor.log.DataLogger;
import com.creditease.uav.apm.invokechain.span.Span;
import com.creditease.uav.apm.invokechain.spi.InvokeChainContext;
import com.creditease.uav.apm.slowoper.span.SlowOperSpan;
import com.creditease.uav.apm.slowoper.spi.SlowOperCapHandler;
import com.creditease.uav.apm.slowoper.spi.SlowOperContext;

/**
 * 
 * 重调用链抽象handler
 *
 */
public abstract class AbstractSlowOperHandler extends SlowOperCapHandler {

    @Override
    public void preCap(InvokeChainContext context, Object[] args) {

        Span span = (Span) args[0];
        SlowOperSpan slowOperSpan = this.spanFactory.buildSlowOperSpan(span);
        SlowOperContext slowOperContext = (SlowOperContext) args[1];

        buildSpanContent(slowOperContext, slowOperSpan);
        this.spanFactory.setSlowOperSpanToContext(this.getSlowOperSpanKey(span), slowOperSpan);
    }

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
        SlowOperContext slowOperContext = (SlowOperContext) args[1];

        buildSpanContent(slowOperContext, slowOperSpan);
        invokeChainLogger.logData(slowOperSpan.toString());
    }

    @Override
    public abstract void buildSpanContent(SlowOperContext slowOperContext, SlowOperSpan slowOperSpan);
}
