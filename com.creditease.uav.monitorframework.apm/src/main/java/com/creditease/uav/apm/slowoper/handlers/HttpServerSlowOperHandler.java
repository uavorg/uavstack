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
 * 处理http协议的server类型
 *
 */
public class HttpServerSlowOperHandler extends AbstractSlowOperHandler {

    private final String lineSeparator = System.getProperty("line.separator", "/n");
    private final int headerLength = DataConvertHelper.toInt(System.getProperty("com.creditease.uav.ivcdat.rpc.header"),
            2000);
    private final int bodyLength = DataConvertHelper.toInt(System.getProperty("com.creditease.uav.ivcdat.rpc.body"),
            2000);

    @Override
    public void buildSpanContent(SlowOperContext slowOperContext, SlowOperSpan slowOperSpan) {

        if (!slowOperContext.containsKey(SlowOperConstants.PROTOCOL_HTTP_REQ_HEADER)) {
            return;
        }

        String reqHeader = (String) slowOperContext.get(SlowOperConstants.PROTOCOL_HTTP_REQ_HEADER);
        String reqBody = (String) slowOperContext.get(SlowOperConstants.PROTOCOL_HTTP_REQ_BODY);
        reqBody = reqBody.replace(lineSeparator, "").trim();

        String respHeader = (String) slowOperContext.get(SlowOperConstants.PROTOCOL_HTTP_RSP_HEADER);
        String respBody = (String) slowOperContext.get(SlowOperConstants.PROTOCOL_HTTP_RSP_BODY);
        respBody = respBody.replace(lineSeparator, "").trim();

        slowOperSpan.appendContent(reqHeader, headerLength, true);
        slowOperSpan.appendContent(reqBody, bodyLength, true);
        slowOperSpan.appendContent(respHeader, headerLength, true);
        slowOperSpan.appendContent(respBody, bodyLength, true);
    }

}
