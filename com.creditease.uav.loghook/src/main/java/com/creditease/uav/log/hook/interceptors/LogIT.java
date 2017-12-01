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

package com.creditease.uav.log.hook.interceptors;

import com.creditease.monitor.UAVServer;
import com.creditease.uav.apm.invokechain.spi.InvokeChainSpanContext;
import com.creditease.uav.common.BaseComponent;

public class LogIT extends BaseComponent {

    public String formatLog(String str) {

        if (UAVServer.instance().isExistSupportor("com.creditease.uav.apm.supporters.InvokeChainSupporter")) {
            if (UAVServer.instance().isExistSupportor("com.creditease.uav.apm.supporters.LogTraceSupporter")) {
                InvokeChainSpanContext context = (InvokeChainSpanContext) UAVServer.instance()
                        .runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "getSpanContext");
                if (context != null) {
                    String traceId = context.getMainSpan().getTraceId();
                    return new StringBuilder().append("uav_").append(traceId).append(" ").append(str).toString();
                }
            }
        }
        return str;
    }

}
