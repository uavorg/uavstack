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

import com.alibaba.rocketmq.common.message.Message;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.uav.apm.invokechain.span.Span;
import com.creditease.uav.apm.invokechain.spi.InvokeChainAdapter;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.apm.invokechain.spi.InvokeChainContext;
import com.creditease.uav.apm.slowoper.spi.SlowOperConstants;
import com.creditease.uav.apm.slowoper.spi.SlowOperContext;

/**
 * RocketMQProducerAdapter description: RocketMQ Producer Adapter
 *
 */
public class RocketMQProducerAdapter extends InvokeChainAdapter {

    @Override
    public void beforePreCap(InvokeChainContext context, Object[] args) {

        context.put(InvokeChainConstants.CLIENT_IT_KEY,
                DataConvertHelper.toInt(System.getProperty("com.creditease.uav.invokechain.code.mq.rocketmq.key"), 1));
        context.put(InvokeChainConstants.CLIENT_IT_CLASS,
                System.getProperty("com.creditease.uav.invokechain.code.mq.rocketmq.class"));
    }

    @Override
    public void afterPreCap(InvokeChainContext context, Object[] args) {

        String storeKey = (String) context.get(InvokeChainConstants.CLIENT_SPAN_THREADLOCAL_STOREKEY);
        Span span = this.spanFactory.getSpanFromContext(storeKey);
        String spanMeta = this.spanFactory.getSpanMeta(span);

        Message msg = (Message) args[0];
        msg.putUserProperty(InvokeChainConstants.PARAM_MQHEAD_SPANINFO, spanMeta);

        if (UAVServer.instance().isExistSupportor("com.creditease.uav.apm.supporters.SlowOperSupporter")) {
            SlowOperContext slowOperContext = new SlowOperContext();
            String content;
            try {
                content = new String(msg.getBody(), "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                content = "unsupported encoding,defalut is utf-8.try to set ContentEncoding to fit.";
            }
            slowOperContext.put(SlowOperConstants.PROTOCOL_MQ_RABBIT_BODY, content);
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

            Span span = (Span) context.get(InvokeChainConstants.PARAM_SPAN_KEY);

            Object params[] = { span };
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.SlowOperSupporter", "runCap",
                    span.getEndpointInfo().split(",")[0], InvokeChainConstants.CapturePhase.DOCAP, context, params);
        }
    }

}
