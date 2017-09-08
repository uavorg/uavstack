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

package com.creditease.uav.hook.httpclients3.sync.invokeChain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.uav.apm.invokechain.span.Span;
import com.creditease.uav.apm.invokechain.spi.InvokeChainAdapter;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.apm.invokechain.spi.InvokeChainContext;
import com.creditease.uav.apm.slowoper.spi.SlowOperConstants;
import com.creditease.uav.apm.slowoper.spi.SlowOperContext;

public class ApacheHttpClient3Adapter extends InvokeChainAdapter {

    @Override
    public void beforePreCap(InvokeChainContext params, Object[] args) {

        params.put(InvokeChainConstants.CLIENT_IT_KEY,
                DataConvertHelper.toInt(System.getProperty("com.creditease.uav.invokechain.code.http3.sync.key"), 0));
        params.put(InvokeChainConstants.CLIENT_IT_CLASS,
                System.getProperty("com.creditease.uav.invokechain.code.http3.sync.class"));
    }

    @Override
    public void afterPreCap(InvokeChainContext context, Object[] args) {

        /**
         * after precap the client's span is created, set the span meta into http request header
         */
        String url = (String) context.get(InvokeChainConstants.CLIENT_SPAN_THREADLOCAL_STOREKEY);
        Span span = this.spanFactory.getSpanFromContext(url);
        String spanMeta = this.spanFactory.getSpanMeta(span);
        HttpMethod method = (HttpMethod) args[1];

        method.removeRequestHeader(InvokeChainConstants.PARAM_HTTPHEAD_SPANINFO);
        method.addRequestHeader(InvokeChainConstants.PARAM_HTTPHEAD_SPANINFO, spanMeta);

        if (UAVServer.instance().isExistSupportor("com.creditease.uav.apm.supporters.SlowOperSupporter")) {

            // httpclient3好像没有encoding参数
            SlowOperContext slowOperContext = new SlowOperContext();
            String quertParams = method.getQueryString();
            if (quertParams == null) {
                quertParams = "{}";
            }
            slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_HEADER,
                    getHeadersContent(method.getRequestHeaders()) + quertParams);
            // 通过此种方式判断是否存在body，通过httpclient类继承确定
            String requestBody = "";
            if (EntityEnclosingMethod.class.isAssignableFrom(method.getClass())) {
                EntityEnclosingMethod entityMethod = (EntityEnclosingMethod) method;
                RequestEntity requestEntity = entityMethod.getRequestEntity();

                // 根据不同类型entity采取不同读取方式(由于httpclient3没有显视encoding，故此处使用默认的编码格式)
                if (ByteArrayRequestEntity.class.isAssignableFrom(requestEntity.getClass())) {
                    requestBody = new String(((ByteArrayRequestEntity) requestEntity).getContent());
                }
                else if (InputStreamRequestEntity.class.isAssignableFrom(requestEntity.getClass())) {

                    InputStreamRequestEntity inputStreamRequestEntity = (InputStreamRequestEntity) requestEntity;
                    if (!inputStreamRequestEntity.isRepeatable()) {
                        inputStreamRequestEntity.getContentLength();
                    }
                    try {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(inputStreamRequestEntity.getContent()));
                        StringBuilder body = new StringBuilder();
                        String str;
                        while ((str = reader.readLine()) != null) {
                            body.append(str);
                        }
                        requestBody = body.toString();
                    }
                    catch (UnsupportedOperationException e) {
                        logger.warn("body type is not supported!", e);
                        requestBody = "body type is not supported!" + e.toString();
                    }
                    catch (IOException e) {
                        logger.error("IOException!", e);
                        requestBody = e.toString();
                    }
                }
                else if (MultipartRequestEntity.class.isAssignableFrom(requestEntity.getClass())) {
                    // MultipartRequestEntity暂时不支持
                    requestBody = "MultipartRequestEntity is not supported";
                }
                else if (StringRequestEntity.class.isAssignableFrom(requestEntity.getClass())) {
                    requestBody = ((StringRequestEntity) requestEntity).getContent();
                }
                else {
                    requestBody = "unsupported type";
                }
            }
            slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_BODY, requestBody);
            Object params[] = { span, slowOperContext };
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.SlowOperSupporter", "runCap",
                    span.getEndpointInfo().split(",")[0], InvokeChainConstants.CapturePhase.PRECAP, context, params);
        }
    }

    @Override
    public void beforeDoCap(InvokeChainContext params, Object[] args) {

    }

    @Override
    public void afterDoCap(InvokeChainContext context, Object[] args) {

        if (UAVServer.instance().isExistSupportor("com.creditease.uav.apm.supporters.SlowOperSupporter")) {
            Span span = (Span) context.get(InvokeChainConstants.PARAM_SPAN_KEY);
            SlowOperContext slowOperContext = new SlowOperContext();
            if (Throwable.class.isAssignableFrom(args[0].getClass())) {
                slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_EXCEPTION, args[0].toString());
            }
            else {
                HttpMethod method = (HttpMethod) args[0];
                slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_HEADER,
                        getHeadersContent(method.getRequestHeaders()));
                try {
                    slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_BODY, method.getResponseBodyAsString());
                }
                catch (IOException e) {
                    slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_EXCEPTION, e.toString());
                }
            }
            Object params[] = { span, slowOperContext };
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.SlowOperSupporter", "runCap",
                    span.getEndpointInfo().split(",")[0], InvokeChainConstants.CapturePhase.DOCAP, context, params);
        }
    }

    /**
     * 根据HttpRequest获取指定格式的headers
     * 
     * @param request
     * @return
     */
    private String getHeadersContent(Header[] headers) {

        Map<String, String> result = new HashMap<String, String>();
        for (Header header : headers) {
            result.put(header.getName(), header.getValue());
        }
        return JSONHelper.toString(result);
    }

}
