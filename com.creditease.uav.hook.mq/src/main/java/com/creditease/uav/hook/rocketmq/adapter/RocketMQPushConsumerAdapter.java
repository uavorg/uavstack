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
package com.creditease.uav.hook.rocketmq.adapter;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.alibaba.rocketmq.common.message.MessageExt;
import com.creditease.monitor.UAVServer;
import com.creditease.uav.apm.invokechain.span.Span;
import com.creditease.uav.apm.invokechain.spi.InvokeChainAdapter;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.apm.invokechain.spi.InvokeChainContext;
import com.creditease.uav.apm.slowoper.spi.SlowOperConstants;
import com.creditease.uav.apm.slowoper.spi.SlowOperContext;

public class RocketMQPushConsumerAdapter extends InvokeChainAdapter {

    @Override
    public void beforePreCap(InvokeChainContext context, Object[] args) {

    }

    @Override
    public void afterPreCap(InvokeChainContext context, Object[] args) {

        if (UAVServer.instance().isExistSupportor("com.creditease.uav.apm.supporters.SlowOperSupporter")) {
            String storeKey = (String) context.get(InvokeChainConstants.CLIENT_SPAN_THREADLOCAL_STOREKEY);
            Span span = this.spanFactory.getSpanFromContext(storeKey);

            if(span == null) {
                return;
            }
            
            @SuppressWarnings("unchecked")
            List<MessageExt> exts = (List<MessageExt>) args[0];
            String content;
            try {
                content = new String(exts.get(0).getBody(), "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                content = "unsupported encoding,defalut is utf-8.try to set ContentEncoding to fit.";
            }
            SlowOperContext slowOperContext = new SlowOperContext();
            slowOperContext.put(SlowOperConstants.PROTOCOL_MQ_RABBIT_BODY, content);
            Object params[] = { span, slowOperContext };
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.SlowOperSupporter", "runCap",
                    span.getEndpointInfo().split(",")[0], InvokeChainConstants.CapturePhase.PRECAP, context, params);
        }
    }

    @Override
    public void beforeDoCap(InvokeChainContext context, Object[] args) {

        String storeKey = (String) context.get(InvokeChainConstants.CLIENT_SPAN_THREADLOCAL_STOREKEY);
        Span span = this.spanFactory.getSpanFromContext(storeKey);
        if (span != null) {
            span.setClassName((String) context.get(InvokeChainConstants.CLIENT_IT_CLASS));
            span.setMethodName((String) context.get(InvokeChainConstants.CLIENT_IT_METHOD));
        }
    }

    @Override
    public void afterDoCap(InvokeChainContext context, Object[] args) {

        if (UAVServer.instance().isExistSupportor("com.creditease.uav.apm.supporters.SlowOperSupporter")) {

            Span span = (Span) context.get(InvokeChainConstants.PARAM_SPAN_KEY);
            
            if(span == null) {
                return;
            }
            
            Object params[] = { span };
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.SlowOperSupporter", "runCap",
                    span.getEndpointInfo().split(",")[0], InvokeChainConstants.CapturePhase.DOCAP, context, params);
        }
    }
}
