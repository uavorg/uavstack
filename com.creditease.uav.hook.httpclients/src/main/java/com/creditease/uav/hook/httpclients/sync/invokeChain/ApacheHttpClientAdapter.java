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

package com.creditease.uav.hook.httpclients.sync.invokeChain;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BufferedHttpEntity;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.uav.apm.invokechain.span.Span;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.apm.invokechain.spi.InvokeChainContext;
import com.creditease.uav.apm.slowoper.spi.SlowOperConstants;
import com.creditease.uav.apm.slowoper.spi.SlowOperContext;
import com.creditease.uav.hook.httpclients.AbstractHttpClientAdapter;

public class ApacheHttpClientAdapter extends AbstractHttpClientAdapter {

    @Override
    public void beforePreCap(InvokeChainContext params, Object[] args) {

        // 查找应用类信息所需的关键类名和相隔层数
        params.put(InvokeChainConstants.CLIENT_IT_KEY,
                DataConvertHelper.toInt(System.getProperty("com.creditease.uav.invokechain.code.http.sync.key"), 0));
        params.put(InvokeChainConstants.CLIENT_IT_CLASS,
                System.getProperty("com.creditease.uav.invokechain.code.http.sync.class"));
    }

    @Override
    public void afterPreCap(InvokeChainContext context, Object[] args) {

        if (args.length < 2) {
            return;
        }

        /**
         * after precap the client's span is created, set the span meta into http request header
         */
        String url = (String) context.get(InvokeChainConstants.CLIENT_SPAN_THREADLOCAL_STOREKEY);

        Span span = this.spanFactory.getSpanFromContext(url);

        String spanMeta = this.spanFactory.getSpanMeta(span);

        HttpRequest request = (HttpRequest) args[1];

        request.removeHeaders(InvokeChainConstants.PARAM_HTTPHEAD_SPANINFO);
        request.addHeader(InvokeChainConstants.PARAM_HTTPHEAD_SPANINFO, spanMeta);

        handleSlowOperSupporter(request, span, context);
    }

    @Override
    public void beforeDoCap(InvokeChainContext params, Object[] args) {

        if (UAVServer.instance().isExistSupportor("com.creditease.uav.apm.supporters.SlowOperSupporter")) {
            if (Throwable.class.isAssignableFrom(args[0].getClass())) {

            }
            else {
                HttpResponse response = (HttpResponse) args[0];
                HttpEntity entity = response.getEntity();
                /**
                 * NOTE:the entity may be null. eg:in the springCloud when registering application into eureka, the
                 * com.netflix.discovery.DiscoveryClient may return a null.
                 */
                if (entity == null) {
                    return;
                }
                try {
                    BufferedHttpEntity httpEntityWrapper = new BufferedHttpEntity(entity);
                    response.setEntity(httpEntityWrapper);
                }
                catch (IOException e) {
                    logger.error("HttpEntityWrapper failed!", e);
                }
                catch (Exception e) {
                    logger.warn("HttpEntityWrapper failed!", e);
                }
            }
        }
    }

    @Override
    public void afterDoCap(InvokeChainContext context, Object[] args) {

        if (UAVServer.instance().isExistSupportor("com.creditease.uav.apm.supporters.SlowOperSupporter")) {
            Span span = (Span) context.get(InvokeChainConstants.PARAM_SPAN_KEY);
            SlowOperContext slowOperContext = new SlowOperContext();
            if (Throwable.class.isAssignableFrom(args[0].getClass())) {

                Throwable e = (Throwable) args[0];
                slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_EXCEPTION, e.toString());

            }
            else {
                HttpResponse response = (HttpResponse) args[0];

                slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_HEADER, getResponHeaders(response));
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    // 由于存在读取失败和无法缓存的大entity会使套壳失败，故此处添加如下判断
                    if (BufferedHttpEntity.class.isAssignableFrom(entity.getClass())) {
                        Header header = entity.getContentEncoding();
                        String encoding = header == null ? "utf-8" : header.getValue();
                        slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_BODY,
                                getHttpEntityContent(entity, encoding));
                    }
                    else {
                        slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_BODY,
                                "HttpEntityWrapper failed! Maybe HTTP entity too large to be buffered in memory");
                    }
                }
                else {
                    slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_BODY, "");
                }
            }

            Object params[] = { span, slowOperContext };
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.SlowOperSupporter", "runCap",
                    span.getEndpointInfo().split(",")[0], InvokeChainConstants.CapturePhase.DOCAP, context, params);
        }

    }

}
