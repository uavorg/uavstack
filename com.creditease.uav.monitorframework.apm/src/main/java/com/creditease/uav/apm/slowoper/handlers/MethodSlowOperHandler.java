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
            slowOperSpan.appendContent(";" + methodParams);
        }
        else {
            String methodReturn = (String) slowOperContext.get(SlowOperConstants.PROTOCOL_METHOD_RETURN);
            slowOperSpan.appendContent(";o;" + methodReturn.length() + ";" + methodReturn);
        }
    }
}
