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

package com.creditease.uav.apm.slowoper.spi;

import com.creditease.monitor.log.DataLogger;
import com.creditease.uav.apm.invokechain.span.Span;
import com.creditease.uav.apm.invokechain.spi.InvokeChainContext;
import com.creditease.uav.apm.slowoper.span.SlowOperSpan;
import com.creditease.uav.apm.slowoper.span.SlowOperSpanFactory;
import com.creditease.uav.apm.supporters.SlowOperSupporter;
import com.creditease.uav.common.BaseComponent;

public abstract class SlowOperCapHandler extends BaseComponent {

    protected SlowOperSpanFactory spanFactory = new SlowOperSpanFactory();

    /**
     * 获取应用InvokeChain日志实例
     * 
     * @param appid
     * @return
     */
    protected DataLogger getAppInvokeChainLogger(String appid) {

        return this.getSupporter(SlowOperSupporter.class).getDataLogger("ivcdat", appid);
    }

    /**
     * 根据轻调用链span获取重调用链唯一标示key
     * 
     * @param span
     * @return
     */
    protected String getSlowOperSpanKey(Span span) {

        return span.getTraceId() + ";" + span.getSpanId() + ";" + span.getEndpointInfo();
    }

    /**
     * Before Span Capture action
     * 
     * @param context
     */
    public abstract void preCap(InvokeChainContext context, Object[] args);

    /**
     * After Span Capture action
     * 
     * @param context
     */
    public abstract void doCap(InvokeChainContext context, Object[] args);

    /**
     * 根据上下文内容补全SlowOperSpan的信息
     * 
     * @param slowOperContext
     * @param slowOperSpan
     */
    public abstract void buildSpanContent(SlowOperContext slowOperContext, SlowOperSpan slowOperSpan);
}
