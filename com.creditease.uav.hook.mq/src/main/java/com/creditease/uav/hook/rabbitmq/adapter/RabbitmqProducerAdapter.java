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

package com.creditease.uav.hook.rabbitmq.adapter;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.uav.apm.invokechain.span.Span;
import com.creditease.uav.apm.invokechain.spi.InvokeChainAdapter;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.apm.invokechain.spi.InvokeChainContext;
import com.creditease.uav.apm.slowoper.spi.SlowOperConstants;
import com.creditease.uav.apm.slowoper.spi.SlowOperContext;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;

public class RabbitmqProducerAdapter extends InvokeChainAdapter {

    @Override
    public void beforePreCap(InvokeChainContext context, Object[] args) {

        // 查找应用类信息所需的关键类名和相隔层数(特别说明，由于当前服务流拦截使用的java原生代理方式，故当前选取的查找应用类信息所需的关键类名为UAV自己的类)
        context.put(InvokeChainConstants.CLIENT_IT_KEY,
                DataConvertHelper.toInt(System.getProperty("com.creditease.uav.invokechain.code.mq.rabbitmq.key"), 0));
        context.put(InvokeChainConstants.CLIENT_IT_CLASS,
                System.getProperty("com.creditease.uav.invokechain.code.mq.rabbitmq.class"));
    }

    @Override
    public void afterPreCap(InvokeChainContext context, Object[] args) {

        AMQP.BasicProperties props = (BasicProperties) args[0];
        Map<String, Object> header = new HashMap<String, Object>();

        String storeKey = (String) context.get(InvokeChainConstants.CLIENT_SPAN_THREADLOCAL_STOREKEY);

        Span span = this.spanFactory.getSpanFromContext(storeKey);

        String spanMeta = this.spanFactory.getSpanMeta(span);

        header.put(InvokeChainConstants.PARAM_MQHEAD_SPANINFO, spanMeta);

        if (props == null) {
            props = new AMQP.BasicProperties.Builder().headers(header).build();
        }
        else {
            header.putAll(props.getHeaders());
            props = new AMQP.BasicProperties.Builder().appId(props.getAppId()).clusterId(props.getClusterId())
                    .contentEncoding(props.getContentEncoding()).contentType(props.getContentType())
                    .correlationId(props.getCorrelationId()).deliveryMode(props.getDeliveryMode())
                    .expiration(props.getExpiration()).headers(header).messageId(props.getMessageId())
                    .priority(props.getPriority()).replyTo(props.getReplyTo()).timestamp(props.getTimestamp())
                    .type(props.getType()).userId(props.getUserId()).build();
        }
        context.put(InvokeChainConstants.PARAM_MQHEAD_INFO, props);

        if (UAVServer.instance().isExistSupportor("com.creditease.uav.apm.supporters.SlowOperSupporter")) {
            byte[] body = (byte[]) args[1];
            SlowOperContext slowOperContext = new SlowOperContext();
            if (props == null || StringHelper.isEmpty(props.getContentEncoding())) {
                String content = "";
                try {
                    content = new String(body, "UTF-8");
                }
                catch (UnsupportedEncodingException e) {
                    content = "unsupported encoding,defalut is utf-8.try to set ContentEncoding to fit.";
                }
                slowOperContext.put(SlowOperConstants.PROTOCOL_MQ_RABBIT_BODY, content);
            }
            else {
                String encoding = props.getContentEncoding();
                String content = "";
                try {
                    content = new String(body, encoding);
                }
                catch (UnsupportedEncodingException e) {
                    content = "ContentEncoding is " + encoding + " .but content is unsupported this encoding.";
                }
                slowOperContext.put(SlowOperConstants.PROTOCOL_MQ_RABBIT_BODY, content);
            }
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
