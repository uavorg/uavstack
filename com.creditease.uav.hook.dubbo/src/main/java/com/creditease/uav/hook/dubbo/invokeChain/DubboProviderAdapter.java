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

package com.creditease.uav.hook.dubbo.invokeChain;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Result;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.EncodeHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.uav.apm.invokechain.span.Span;
import com.creditease.uav.apm.invokechain.spi.InvokeChainAdapter;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.apm.invokechain.spi.InvokeChainContext;
import com.creditease.uav.apm.slowoper.spi.SlowOperConstants;
import com.creditease.uav.apm.slowoper.spi.SlowOperContext;

public class DubboProviderAdapter extends InvokeChainAdapter {

    @Override
    public void beforePreCap(InvokeChainContext params, Object[] args) {

    }

    @Override
    public void afterPreCap(InvokeChainContext context, Object[] args) {

        String url = (String) context.get(CaptureConstants.INFO_APPSERVER_CONNECTOR_REQUEST_URL);
        Span span = this.spanFactory.getSpanFromContext(url);
        if (UAVServer.instance().isExistSupportor("com.creditease.uav.apm.supporters.SlowOperSupporter")) {
            SlowOperContext slowOperContext = new SlowOperContext();
            // dubbo虽属于rpc，但从其使用方式上属于方法级
            Invocation invocation = (Invocation) args[1];
            slowOperContext.put(SlowOperConstants.PROTOCOL_METHOD_PARAMS, parseParams(invocation.getArguments()));

            Object params[] = { span, slowOperContext };
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.SlowOperSupporter", "runCap",
                    span.getEndpointInfo().split(",")[0], InvokeChainConstants.CapturePhase.PRECAP, context, params);
        }
    }

    @Override
    public void beforeDoCap(InvokeChainContext context, Object[] args) {

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
            // 根据返回码确定当前是否有异常(由于IT位置已经解析过，此处直接使用)
            String respCode = (String) context.get(CaptureConstants.INFO_APPSERVER_CONNECTOR_RESPONSECODE);
            if (respCode.equals("-1")) {

                slowOperContext.put(SlowOperConstants.PROTOCOL_METHOD_RETURN,
                        parseReturn(context.get(CaptureConstants.INFO_APPSERVER_CONNECTOR_RESPONSESTATE)));
            }
            else {
                Result result = (Result) args[2];
                slowOperContext.put(SlowOperConstants.PROTOCOL_METHOD_RETURN, parseReturn(result.getValue()));
            }

            Object params[] = { span, slowOperContext };
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.SlowOperSupporter", "runCap",
                    span.getEndpointInfo().split(",")[0], InvokeChainConstants.CapturePhase.DOCAP, context, params);
        }
    }

    /**
     * 解析入参
     * 
     * @param args
     * @return
     */
    private String parseParams(Object[] args) {

        if (args == null || args.length == 0) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Object temp : args) {
            if (temp == null) {
                temp = "null";
            }
            String tempStr = temp.toString();
            // 限定采集的协议体的大小 当length为小于0时不限制长度，为0时则直接为空（不去获取）
            int methodParamsLength = DataConvertHelper.toInt(System.getProperty("com.creditease.uav.ivcdat.method.req"),
                    2000);
            if (tempStr.toString().length() > methodParamsLength && methodParamsLength > 0) {
                tempStr = tempStr.substring(0, methodParamsLength);
            }
            else if (methodParamsLength == 0) {
                tempStr = "";
            }
            tempStr = EncodeHelper.urlEncode(tempStr);
            stringBuilder.append(tempStr.length() + ";" + tempStr.toString() + ";");
        }
        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }

    /**
     * 解析出参
     * 
     * @param arg
     * @return
     */
    private String parseReturn(Object result) {

        Object temp = result;
        if (temp == null) {
            temp = "null";
        }
        String tempStr = temp.toString();
        // 限定采集的协议体的大小 当length为小于0时不限制长度，为0时则直接为空（不去获取）
        int methodReturnLength = DataConvertHelper.toInt(System.getProperty("com.creditease.uav.ivcdat.method.ret"),
                2000);
        if (tempStr.toString().length() > methodReturnLength && methodReturnLength > 0) {
            tempStr = tempStr.substring(0, methodReturnLength);
        }
        else if (methodReturnLength == 0) {
            tempStr = "";
        }
        return EncodeHelper.urlEncode(tempStr);
    }
}
