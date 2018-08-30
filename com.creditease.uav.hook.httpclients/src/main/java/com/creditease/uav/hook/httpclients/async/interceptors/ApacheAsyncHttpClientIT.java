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

package com.creditease.uav.hook.httpclients.async.interceptors;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.RequestLine;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;

import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.CaptureContext;
import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.monitor.proxy.spi.JDKProxyInvokeHandler;
import com.creditease.monitor.proxy.spi.JDKProxyInvokeProcessor;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.common.BaseComponent;
import com.creditease.uav.hook.httpclients.async.invokeChian.ApacheAsyncHttpClientAdapter;
import com.creditease.uav.util.JDKProxyInvokeUtil;
import com.creditease.uav.util.MonitorServerUtil;

/**
 * 
 * HttpClientProxy description: this is the real work class who does all interceptions
 *
 */
public class ApacheAsyncHttpClientIT extends BaseComponent {

    /**
     * 
     * FutureCallbackProxyInvokeProcessor description:
     *
     */
    @SuppressWarnings("rawtypes")
    private class FutureCallbackProxyInvokeProcessor extends JDKProxyInvokeProcessor<FutureCallback> {

        private Map<String, Object> contextInAsync;

        public FutureCallbackProxyInvokeProcessor(Map<String, Object> contextInAsync) {
            this.contextInAsync = contextInAsync;
        }

        @Override
        public void preProcess(FutureCallback t, Object proxy, Method method, Object[] args) {

            installHttpEntityWrapper(args);
        }

        @Override
        public void catchInvokeException(FutureCallback t, Object proxy, Method method, Object[] args, Throwable e) {

            doEnd(method, args, e);
        }

        @Override
        public Object postProcess(Object res, FutureCallback t, Object proxy, Method method, Object[] args) {

            doEnd(method, args, null);

            return null;
        }

        private void doEnd(Method method, Object[] args, Throwable e) {

            // for both good and exception(fail)
            if (method.getName().equals("completed") || method.getName().equals("failed")) {

                int rc = (method.getName().equals("completed") == true) ? 1 : -1;

                Map<String, Object> params = new HashMap<String, Object>();

                params.put(CaptureConstants.INFO_CLIENT_TARGETSERVER, targetServer);
                params.put(CaptureConstants.INFO_CLIENT_RESPONSECODE, rc);
                params.put(CaptureConstants.INFO_CLIENT_REQUEST_URL, targetURL);
                params.put(CaptureConstants.INFO_CLIENT_APPID, applicationId);
                params.put(CaptureConstants.INFO_CLIENT_TYPE, "apache.http.AsyncClient");

                String responseState = "";
                if (e != null) {
                    responseState = e.toString();
                }
                else if (Exception.class.isAssignableFrom(args[0].getClass())) {
                    responseState = ((Exception) args[0]).toString();
                }
                else if (HttpResponse.class.isAssignableFrom(args[0].getClass())) {
                    responseState = ((HttpResponse) args[0]).getStatusLine().getStatusCode() + "";
                }

                params.put(CaptureConstants.INFO_CLIENT_RESPONSESTATE, responseState);

                if (logger.isDebugable()) {
                    logger.debug("Invoke END:" + rc + "," + targetServer, null);
                }

                if (contextInAsync != null) {
                    contextInAsync.putAll(params);
                }
                Object[] objs = { e, args[0] };
                UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                        InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.DOCAP, contextInAsync,
                        ApacheAsyncHttpClientAdapter.class, objs);

                UAVServer.instance().runMonitorAsyncCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                        Monitor.CapturePhase.DOCAP, params, ccMap);

                targetServer = "";
                targetURL = "";
            }
        }

    }

    /**
     * 重调用情况下为response的HttpEntity安装Wrapper
     * 
     * @param args
     */
    private void installHttpEntityWrapper(Object[] args) {

        if (UAVServer.instance().isExistSupportor("com.creditease.uav.apm.supporters.SlowOperSupporter")) {

            if (Exception.class.isAssignableFrom(args[0].getClass())) {
                // do nothing
            }
            else if (HttpResponse.class.isAssignableFrom(args[0].getClass())) {
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

    /**
     * 
     * FutureProxyInvokeProcessor description:
     *
     */
    @SuppressWarnings("rawtypes")
    private class FutureProxyInvokeProcessor extends JDKProxyInvokeProcessor<Future> {

        private Map<String, Object> contextInAsync;

        public FutureProxyInvokeProcessor(Map<String, Object> contextInAsync) {
            this.contextInAsync = contextInAsync;
        }

        @Override
        public void preProcess(Future t, Object proxy, Method method, Object[] args) {

            installHttpEntityWrapper(args);
        }

        @Override
        public void catchInvokeException(Future t, Object proxy, Method method, Object[] args, Throwable e) {

            // for expcetion
            if (method.getName().equals("get")) {

                Map<String, Object> params = new HashMap<String, Object>();

                params.put(CaptureConstants.INFO_CLIENT_TARGETSERVER, targetServer);
                params.put(CaptureConstants.INFO_CLIENT_RESPONSECODE, -1);
                params.put(CaptureConstants.INFO_CLIENT_REQUEST_URL, targetURL);
                params.put(CaptureConstants.INFO_CLIENT_APPID, applicationId);
                params.put(CaptureConstants.INFO_CLIENT_TYPE, "apache.http.AsyncClient");
                params.put(CaptureConstants.INFO_CLIENT_RESPONSESTATE, e.toString());

                if (logger.isDebugable()) {
                    logger.debug("Invoke END:" + -1 + "," + targetServer, null);
                }

                if (contextInAsync != null) {
                    contextInAsync.putAll(params);
                }

                Object[] objs = { e, null };
                UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                        InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.DOCAP, contextInAsync,
                        ApacheAsyncHttpClientAdapter.class, objs);

                UAVServer.instance().runMonitorAsyncCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                        Monitor.CapturePhase.DOCAP, params, ccMap);

                targetServer = "";
                targetURL = "";
            }
        }

        @Override
        public Object postProcess(Object res, Future t, Object proxy, Method method, Object[] args) {

            // for good
            if (method.getName().equals("get")) {
                Map<String, Object> params = new HashMap<String, Object>();

                params.put(CaptureConstants.INFO_CLIENT_TARGETSERVER, targetServer);
                params.put(CaptureConstants.INFO_CLIENT_RESPONSECODE, 1);
                params.put(CaptureConstants.INFO_CLIENT_REQUEST_URL, targetURL);
                params.put(CaptureConstants.INFO_CLIENT_APPID, applicationId);
                params.put(CaptureConstants.INFO_CLIENT_TYPE, "apache.http.AsyncClient");

                String responseState = "";
                if (HttpResponse.class.isAssignableFrom(res.getClass())) {
                    HttpResponse hr = (HttpResponse) res;
                    responseState = hr.getStatusLine().getStatusCode() + "";
                }

                params.put(CaptureConstants.INFO_CLIENT_RESPONSESTATE, responseState);

                if (logger.isDebugable()) {
                    logger.debug("Invoke END:" + 1 + "," + targetServer, null);
                }

                if (ccMap4Chain != null) {
                    ccMap4Chain.putAll(params);
                }

                Object[] objs = { null, res };
                UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter",
                        "runAsyncCap", InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.DOCAP,
                        ccMap4Chain, ApacheAsyncHttpClientAdapter.class, objs);

                UAVServer.instance().runMonitorAsyncCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                        Monitor.CapturePhase.DOCAP, params, ccMap);

                targetServer = "";
                targetURL = "";
            }

            return null;
        }

    }

    private Map<String, CaptureContext> ccMap;

    private Map<String, Object> ccMap4Chain;

    private String applicationId;

    private String targetServer = "";

    private String targetURL = "";

    public ApacheAsyncHttpClientIT(String appid) {
        this.applicationId = appid;
    }

    @SuppressWarnings("rawtypes")
    public HttpAsyncResponseConsumer makeConsumer(final HttpAsyncResponseConsumer r) {

        return JDKProxyInvokeUtil.newProxyInstance(HttpContext.class.getClassLoader(),
                new Class<?>[] { HttpAsyncResponseConsumer.class },
                new JDKProxyInvokeHandler<HttpAsyncResponseConsumer>(r,
                        new JDKProxyInvokeProcessor<HttpAsyncResponseConsumer>() {

                            @Override
                            public void preProcess(HttpAsyncResponseConsumer t, Object proxy, Method method,
                                    Object[] args) {

                            }

                            @Override
                            public Object postProcess(Object res, HttpAsyncResponseConsumer t, Object proxy,
                                    Method method, Object[] args) {

                                if (method.getName().equals("responseReceived")) {

                                    HttpResponse response = (HttpResponse) args[0];

                                    Header sheader = response.getLastHeader("Server");

                                    if (sheader != null) {
                                        targetServer = sheader.getValue();
                                    }
                                }

                                return null;
                            }

                        }));
    }

    @SuppressWarnings("rawtypes")
    public List<HttpAsyncResponseConsumer> makeConsumers(List<HttpAsyncResponseConsumer> list) {

        List<HttpAsyncResponseConsumer> ls = new ArrayList<HttpAsyncResponseConsumer>();

        for (HttpAsyncResponseConsumer r : list) {
            ls.add(makeConsumer(r));
        }

        return ls;
    }

    /**
     * for async http client
     * 
     * @param args
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unused", "unchecked" })
    public FutureCallback doAsyncStart(Object[] args) {

        HttpAsyncRequestProducer requestProducer = null;
        HttpAsyncResponseConsumer responseConsumer = null;
        HttpContext context = null;
        FutureCallback callback = null;
        Map mObj = null;
        if (args.length == 4) {
            requestProducer = (HttpAsyncRequestProducer) args[0];
            responseConsumer = (HttpAsyncResponseConsumer) args[1];
            context = (HttpContext) args[2];
            callback = (FutureCallback) args[3];
        }
        else if (args.length == 5) {
            requestProducer = (HttpAsyncRequestProducer) args[1];
            responseConsumer = (HttpAsyncResponseConsumer) args[2];
            context = (HttpContext) args[3];
            callback = (FutureCallback) args[4];
        }

        String httpAction = null;
        try {
            HttpRequest hr = requestProducer.generateRequest();

            /**
             * 呵呵，就是把UAV的客户端标记加到http header里面，接收方就知道是哪个东东调用的了，便于实现来源快速匹配，这个模式只适合直连模式
             * 
             * 对于代理模式，只匹配代理源即可
             */
            hr.addHeader("UAV-Client-Src", MonitorServerUtil.getUAVClientSrc(this.applicationId));

            RequestLine rl = hr.getRequestLine();
            httpAction = rl.getMethod();
            targetURL = rl.getUri();
            
            // 部分HttpRequest中没有ip:port，需要从httpHost中拿到再拼接
            if (!targetURL.startsWith("http")) {
                targetURL = requestProducer.getTarget().toURI() + targetURL;
            }
        }
        catch (IOException e) {
            // ignore thie exception
            return null;
        }
        catch (HttpException e) {
            // ignore thie exception
            return null;
        }

        Map<String, Object> params = new HashMap<String, Object>();

        params.put(CaptureConstants.INFO_CLIENT_REQUEST_URL, targetURL);
        params.put(CaptureConstants.INFO_CLIENT_REQUEST_ACTION, httpAction);
        params.put(CaptureConstants.INFO_CLIENT_APPID, this.applicationId);
        params.put(CaptureConstants.INFO_CLIENT_TYPE, "apache.http.AsyncClient");

        if (logger.isDebugable()) {
            logger.debug("Invoke START:" + targetURL + "," + httpAction + "," + this.applicationId, null);
        }

        /**
         * for async, as not in the same thread
         */
        ccMap = UAVServer.instance().runMonitorAsyncCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                Monitor.CapturePhase.PRECAP, params, null);

        // register invokechain adapter
        UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "registerAdapter",
                ApacheAsyncHttpClientAdapter.class);

        ccMap4Chain = (Map<String, Object>) UAVServer.instance().runSupporter(
                "com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.PRECAP, params,
                ApacheAsyncHttpClientAdapter.class, args);

        if (callback == null) {
            return null;
        }

        callback = JDKProxyInvokeUtil.newProxyInstance(HttpContext.class.getClassLoader(),
                new Class<?>[] { FutureCallback.class }, new JDKProxyInvokeHandler<FutureCallback>(callback,
                        new FutureCallbackProxyInvokeProcessor(ccMap4Chain)));

        return callback;
    }

    /**
     * for async http client
     * 
     * @param args
     * @return
     */
    @SuppressWarnings({ "rawtypes" })
    public Future doAsyncEnd(Object[] args) {

        Future f = (Future) args[0];

        f = JDKProxyInvokeUtil.newProxyInstance(HttpContext.class.getClassLoader(), new Class<?>[] { Future.class },
                new JDKProxyInvokeHandler<Future>(f, new FutureProxyInvokeProcessor(ccMap4Chain)));

        return f;
    }
}
