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

import com.creditease.agent.helpers.StringHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.uav.apm.invokechain.span.Span;
import com.creditease.uav.apm.invokechain.spi.InvokeChainAdapter;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.apm.invokechain.spi.InvokeChainContext;
import com.creditease.uav.apm.slowoper.spi.SlowOperConstants;
import com.creditease.uav.apm.slowoper.spi.SlowOperContext;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;

public class RabbitmqConsumerAdapter extends InvokeChainAdapter {

    @Override
    public void beforePreCap(InvokeChainContext context, Object[] args) {

    }

    @Override
    public void afterPreCap(InvokeChainContext context, Object[] args) {

        if (UAVServer.instance().isExistSupportor("com.creditease.uav.apm.supporters.SlowOperSupporter")) {
            AMQP.BasicProperties props = (BasicProperties) args[2];
            byte[] body = (byte[]) args[3];
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
            String url = (String) context.get(CaptureConstants.INFO_APPSERVER_CONNECTOR_REQUEST_URL);
            Span span = this.spanFactory.getSpanFromContext(url);
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

            Object params[] = { span };
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.SlowOperSupporter", "runCap",
                    span.getEndpointInfo().split(",")[0], InvokeChainConstants.CapturePhase.DOCAP, context, params);
        }
    }

}
