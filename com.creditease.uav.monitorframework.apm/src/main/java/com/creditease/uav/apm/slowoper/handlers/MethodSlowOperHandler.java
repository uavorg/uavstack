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
import com.creditease.uav.apm.slowoper.span.SlowOperSpan;
import com.creditease.uav.apm.slowoper.spi.SlowOperConstants;
import com.creditease.uav.apm.slowoper.spi.SlowOperContext;

/**
 * 
 * 处理方法级协议的类型
 *
 */
public class MethodSlowOperHandler extends AbstractSlowOperHandler {

    @Override
    public void buildSpanContent(SlowOperContext slowOperContext, SlowOperSpan slowOperSpan) {

        if (slowOperContext.containsKey(SlowOperConstants.PROTOCOL_METHOD_PARAMS)) {
            String methodParams = (String) slowOperContext.get(SlowOperConstants.PROTOCOL_METHOD_PARAMS);
            // 限定采集的协议体的大小 当length为小于0时不限制长度，为0时则直接为空（不去获取）
            int methodParamsLength = DataConvertHelper.toInt(System.getProperty("com.creditease.uav.ivcdat.method.req"),
                    2000);
            if (methodParams.length() > methodParamsLength && methodParamsLength > 0) {
                methodParams = methodParams.substring(0, methodParamsLength);
            }
            else if (methodParamsLength == 0) {
                methodParams = "";
            }
            slowOperSpan.appendContent(";" + methodParams);
        }
        else {
            String methodReturn = (String) slowOperContext.get(SlowOperConstants.PROTOCOL_METHOD_RETURN);
            // 限定采集的协议体的大小 当length为小于0时不限制长度，为0时则直接为空（不去获取）
            int methodReturnLength = DataConvertHelper.toInt(System.getProperty("com.creditease.uav.ivcdat.method.ret"),
                    2000);
            if (methodReturn.length() > methodReturnLength && methodReturnLength > 0) {
                methodReturn = methodReturn.substring(0, methodReturnLength);
            }
            else if (methodReturnLength == 0) {
                methodReturn = "";
            }
            slowOperSpan.appendContent(";o;" + methodReturn.length() + ";" + methodReturn);
        }
    }
}
