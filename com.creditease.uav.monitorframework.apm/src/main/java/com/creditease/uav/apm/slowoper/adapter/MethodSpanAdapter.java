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

import com.creditease.agent.helpers.EncodeHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.uav.apm.invokechain.span.Span;
import com.creditease.uav.apm.invokechain.spi.InvokeChainAdapter;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.apm.invokechain.spi.InvokeChainContext;
import com.creditease.uav.apm.slowoper.spi.SlowOperConstants;
import com.creditease.uav.apm.slowoper.spi.SlowOperContext;

public class MethodSpanAdapter extends InvokeChainAdapter {

    @Override
    public void beforePreCap(InvokeChainContext context, Object[] args) {

        // do noting
    }

    @Override
    public void afterPreCap(InvokeChainContext context, Object[] args) {

        if (UAVServer.instance().isExistSupportor("com.creditease.uav.apm.supporters.SlowOperSupporter")) {

            String storekey = (String) context.get(InvokeChainConstants.METHOD_SPAN_STOREKEY);
            Span span = (Span) context.get(storekey);

            SlowOperContext slowOperContext = new SlowOperContext();
            slowOperContext.put(SlowOperConstants.PROTOCOL_METHOD_PARAMS, parseParams(args));

            Object params[] = { span, slowOperContext };
            // 由于使用Endpoint Info不能区分方法级，故此处使用常量
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.SlowOperSupporter", "runCap",
                    SlowOperConstants.SLOW_OPER_METHOD, InvokeChainConstants.CapturePhase.PRECAP, context, params);
        }
    }

    @Override
    public void beforeDoCap(InvokeChainContext context, Object[] args) {

        // do nothing
    }

    @Override
    public void afterDoCap(InvokeChainContext context, Object[] args) {

        if (UAVServer.instance().isExistSupportor("com.creditease.uav.apm.supporters.SlowOperSupporter")) {

            String storekey = (String) context.get(InvokeChainConstants.METHOD_SPAN_STOREKEY);

            if (storekey == null) {
                return;
            }

            Span span = (Span) context.get(storekey);

            if (span == null) {
                return;
            }

            SlowOperContext slowOperContext = new SlowOperContext();
            slowOperContext.put(SlowOperConstants.PROTOCOL_METHOD_RETURN, parseReturn(args));

            Object params[] = { span, slowOperContext };
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.SlowOperSupporter", "runCap",
                    SlowOperConstants.SLOW_OPER_METHOD, InvokeChainConstants.CapturePhase.DOCAP, context, params);
        }
    }

    /**
     * 解析入参
     * 
     * @param args
     * @return
     */
    private String parseParams(Object[] args) {

        if (args == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Object temp : args) {
            temp = EncodeHelper.urlDecode(temp.toString());
            stringBuilder.append(temp.toString().length() + ";" + temp.toString() + ";");
        }
        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }

    /**
     * 解析出参
     * 
     * @param arg
     * @return
     */
    private String parseReturn(Object[] args) {

        if (args[0] == null) {
            return "";
        }
        else {
            return EncodeHelper.urlDecode(args[0].toString());
        }
    }
}
