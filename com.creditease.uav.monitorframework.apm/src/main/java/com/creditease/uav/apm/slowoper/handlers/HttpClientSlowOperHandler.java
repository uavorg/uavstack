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
 * 处理http协议的client类型
 *
 */
public class HttpClientSlowOperHandler extends AbstractSlowOperHandler {

    @Override
    public void buildSpanContent(SlowOperContext slowOperContext, SlowOperSpan slowOperSpan) {

        if (slowOperContext.containsKey(SlowOperConstants.PROTOCOL_HTTP_EXCEPTION)) {
            String exception = (String) slowOperContext.get(SlowOperConstants.PROTOCOL_HTTP_EXCEPTION);
            // 限定采集的协议体的大小
            int length = DataConvertHelper.toInt(System.getProperty("com.creditease.uav.ivcdat.rpc.body"), 2000);
            slowOperSpan.appendContent(exception, length, true);
        }
        else {
            String header = (String) slowOperContext.get(SlowOperConstants.PROTOCOL_HTTP_HEADER);
            String body = (String) slowOperContext.get(SlowOperConstants.PROTOCOL_HTTP_BODY);
            // 限定采集的协议体的大小
            int headerLength = DataConvertHelper.toInt(System.getProperty("com.creditease.uav.ivcdat.rpc.header"),
                    2000);
            int bodyLength = DataConvertHelper.toInt(System.getProperty("com.creditease.uav.ivcdat.rpc.body"), 2000);
            slowOperSpan.appendContent(header, headerLength, true);
            slowOperSpan.appendContent(body, bodyLength, true);
        }
    }
}
