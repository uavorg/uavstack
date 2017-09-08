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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.BufferedHttpEntity;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.uav.apm.invokechain.span.Span;
import com.creditease.uav.apm.invokechain.spi.InvokeChainAdapter;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.apm.invokechain.spi.InvokeChainContext;
import com.creditease.uav.apm.slowoper.spi.SlowOperConstants;
import com.creditease.uav.apm.slowoper.spi.SlowOperContext;

public class ApacheHttpClientAdapter extends InvokeChainAdapter {

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

        if (UAVServer.instance().isExistSupportor("com.creditease.uav.apm.supporters.SlowOperSupporter")) {
            SlowOperContext slowOperContext = new SlowOperContext();
            String queryParams = getParamsFromUri(request.getRequestLine().getUri());
            slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_HEADER, getRequestHeaders(request) + queryParams);

            // 通过此种方式判断是否存在body，通过httpclient类继承确定
            if (HttpEntityEnclosingRequestBase.class.isAssignableFrom(request.getClass())) {
                HttpEntityEnclosingRequestBase req = (HttpEntityEnclosingRequestBase) request;

                HttpEntity entity = req.getEntity();
                Header header = entity.getContentEncoding();
                String encoding = header == null ? "utf-8" : header.getValue();
                try {
                    BufferedHttpEntity httpEntityWrapper = new BufferedHttpEntity(entity);
                    req.setEntity(httpEntityWrapper);
                    slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_BODY,
                            getHttpEntityContent(httpEntityWrapper, encoding));
                }
                catch (IOException e) {
                    logger.warn("HttpEntityWrapper failed!", e);
                    slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_BODY, e.toString());
                }
            }
            else {
                slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_BODY, "");
            }
            Object params[] = { span, slowOperContext };
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.SlowOperSupporter", "runCap",
                    span.getEndpointInfo().split(",")[0], InvokeChainConstants.CapturePhase.PRECAP, context, params);
        }
    }

    @Override
    public void beforeDoCap(InvokeChainContext params, Object[] args) {

        if (UAVServer.instance().isExistSupportor("com.creditease.uav.apm.supporters.SlowOperSupporter")) {
            if (Throwable.class.isAssignableFrom(args[0].getClass())) {

            }
            else {
                HttpResponse response = (HttpResponse) args[0];
                HttpEntity entity = response.getEntity();
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
                // 由于存在读取失败和无法缓存的大entity会使套壳失败，故此处添加如下判断
                if (BufferedHttpEntity.class.isAssignableFrom(entity.getClass())) {
                    Header header = entity.getContentEncoding();
                    String encoding = header == null ? "utf-8" : header.getValue();
                    slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_BODY, getHttpEntityContent(entity, encoding));
                }
                else {
                    slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_BODY,
                            "HttpEntityWrapper failed! Maybe HTTP entity too large to be buffered in memory");
                }
            }

            Object params[] = { span, slowOperContext };
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.SlowOperSupporter", "runCap",
                    span.getEndpointInfo().split(",")[0], InvokeChainConstants.CapturePhase.DOCAP, context, params);
        }

    }

    private String getHttpEntityContent(HttpEntity entity, String encoding) {

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), encoding));
            StringBuilder body = new StringBuilder();
            String str;
            while ((str = reader.readLine()) != null) {
                body.append(str);
            }
            return body.toString();
        }
        catch (UnsupportedOperationException e) {
            return e.toString();
        }
        catch (IOException e) {
            return e.toString();
        }
    }

    /**
     * 根据HttpRequest获取指定格式的headers
     * 
     * @param request
     * @return
     */
    private String getRequestHeaders(HttpRequest request) {

        Header[] headers = request.getAllHeaders();
        Map<String, String> result = new HashMap<String, String>();
        for (Header header : headers) {
            result.put(header.getName(), header.getValue());
        }
        return JSONHelper.toString(result);
    }

    /**
     * 根据HttpResponse获取指定格式的headers
     * 
     * @param request
     * @return
     */
    private String getResponHeaders(HttpResponse response) {

        Header[] headers = response.getAllHeaders();
        Map<String, String> result = new HashMap<String, String>();
        for (Header header : headers) {
            result.put(header.getName(), header.getValue());
        }
        return JSONHelper.toString(result);
    }

    /**
     * 从uri中提取出参数
     * 
     * @param uri
     * @return
     */
    private String getParamsFromUri(String uri) {

        try {
            URI temp = new URI(uri);
            String result = temp.getQuery();
            if (result == null) {
                return "{}";
            }
            return result;
        }
        catch (URISyntaxException e1) {
            return "{}";
        }
    }
}
