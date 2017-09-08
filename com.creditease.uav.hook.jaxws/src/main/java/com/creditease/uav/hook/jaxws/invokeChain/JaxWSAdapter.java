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

package com.creditease.uav.hook.jaxws.invokeChain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.uav.apm.invokechain.span.Span;
import com.creditease.uav.apm.invokechain.spi.InvokeChainAdapter;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.apm.invokechain.spi.InvokeChainContext;

public class JaxWSAdapter extends InvokeChainAdapter {

    @Override
    public void beforePreCap(InvokeChainContext params, Object[] args) {

        // 查找应用类信息所需的关键类名和相隔层数
        params.put(InvokeChainConstants.CLIENT_IT_KEY,
                DataConvertHelper.toInt(System.getProperty("com.creditease.uav.invokechain.code.ws.key"), 0));
        params.put(InvokeChainConstants.CLIENT_IT_CLASS,
                System.getProperty("com.creditease.uav.invokechain.code.ws.class"));
    }

    @Override
    public void afterPreCap(InvokeChainContext context, Object[] args) {

        String url = (String) context.get(InvokeChainConstants.CLIENT_SPAN_THREADLOCAL_STOREKEY);

        Span span = this.spanFactory.getSpanFromContext(url);

        String spanMeta = this.spanFactory.getSpanMeta(span);
        List<String> spanValue = new ArrayList<String>(1);
        spanValue.add(spanMeta);
        Map<String, List<String>> header = new HashMap<String, List<String>>(1);
        header.put(InvokeChainConstants.PARAM_HTTPHEAD_SPANINFO, spanValue);

        String method = (String) context.get(CaptureConstants.INFO_CLIENT_REQUEST_ACTION);
        List<String> methodValue = new ArrayList<String>(1);
        methodValue.add(method);
        header.put(InvokeChainConstants.CLIENT_IT_METHOD, methodValue);
        context.put(InvokeChainConstants.PARAM_HTTPHEAD_SPANINFO, header);
    }

    @Override
    public void beforeDoCap(InvokeChainContext context, Object[] args) {

    }

    @Override
    public void afterDoCap(InvokeChainContext context, Object[] args) {

    }

}
