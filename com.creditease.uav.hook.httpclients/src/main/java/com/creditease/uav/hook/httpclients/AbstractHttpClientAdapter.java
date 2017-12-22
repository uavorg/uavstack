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

package com.creditease.uav.hook.httpclients;

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

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.uav.apm.invokechain.span.Span;
import com.creditease.uav.apm.invokechain.spi.InvokeChainAdapter;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.apm.invokechain.spi.InvokeChainContext;
import com.creditease.uav.apm.slowoper.spi.SlowOperConstants;
import com.creditease.uav.apm.slowoper.spi.SlowOperContext;

public abstract class AbstractHttpClientAdapter extends InvokeChainAdapter {

    protected void handleSlowOperSupporter(HttpRequest request, Span span, InvokeChainContext context) {

        if (UAVServer.instance().isExistSupportor("com.creditease.uav.apm.supporters.SlowOperSupporter")) {
            SlowOperContext slowOperContext = new SlowOperContext();
            String queryParams = getParamsFromUri(request.getRequestLine().getUri());
            slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_HEADER, getRequestHeaders(request) + queryParams);
            try {
                // 通过此种方式判断是否存在body，通过httpclient类继承确定
                if (HttpEntityEnclosingRequestBase.class.isAssignableFrom(request.getClass())) {
                    HttpEntityEnclosingRequestBase req = (HttpEntityEnclosingRequestBase) request;

                    HttpEntity entity = req.getEntity();
                    // 兼容不正当使用
                    if (entity == null) {
                        slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_BODY, "");
                    }
                    else {
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
                }
                else {
                    slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_BODY, "");
                }
            }
            catch (Exception e) {
                // 由于会存在无法缓冲的情况，故此处添加捕获
                slowOperContext.put(SlowOperConstants.PROTOCOL_HTTP_BODY, e.toString());
            }

            Object params[] = { span, slowOperContext };
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.SlowOperSupporter", "runCap",
                    span.getEndpointInfo().split(",")[0], InvokeChainConstants.CapturePhase.PRECAP, context, params);
        }
    }

    /**
     * 从uri中提取出参数
     * 
     * @param uri
     * @return
     */
    protected String getParamsFromUri(String uri) {

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

    /**
     * 从entity中获取content
     * 
     * @param entity
     * @param encoding
     * @return
     */
    protected String getHttpEntityContent(HttpEntity entity, String encoding) {

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
    protected String getRequestHeaders(HttpRequest request) {

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
    protected String getResponHeaders(HttpResponse response) {

        Header[] headers = response.getAllHeaders();
        Map<String, String> result = new HashMap<String, String>();
        for (Header header : headers) {
            result.put(header.getName(), header.getValue());
        }
        return JSONHelper.toString(result);
    }
}
