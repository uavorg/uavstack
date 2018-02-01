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

package com.creditease.uav.hook.dubbo.interceptors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.config.spring.ServiceBean;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import com.creditease.agent.helpers.NetworkHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.appfra.hook.spi.HookConstants;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.monitor.interceptframework.InterceptSupport;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.hook.dubbo.invokeChain.DubboConsumerAdapter;
import com.creditease.uav.hook.dubbo.invokeChain.DubboProviderAdapter;
import com.creditease.uav.profiling.handlers.dubbo.DubboServiceProfileInfo;

public class DubboIT {

    private static ThreadLocal<DubboIT> consumerTL = new ThreadLocal<DubboIT>();
    private static ThreadLocal<DubboIT> providerTL = new ThreadLocal<DubboIT>();
    private Map<String, Object> ivcContextParams = new HashMap<String, Object>();

    public static void doMonitorStart(String appid, Object[] args, boolean isDoCap, Throwable e) {

        DubboIT dIT = getThreadLocalIT(appid, args);
        dIT.doMonitor(args, isDoCap, e);
    }

    public static void doMonitorEnd(Object[] args, boolean isDoCap, Throwable e) {

        DubboIT dIT = getThreadLocalIT(null, args);
        if (dIT == null) {
            return;
        }

        dIT.doMonitor(args, isDoCap, e);
    }

    private static DubboIT getThreadLocalIT(String appid, Object[] args) {

        Invoker<?> invoker = (Invoker<?>) args[0];

        DubboIT dIT = null;

        // consumer
        if (Constants.CONSUMER_SIDE.equals(invoker.getUrl().getParameter(Constants.SIDE_KEY))) {
            if (appid != null) {
                dIT = new DubboIT(appid);
                consumerTL.set(dIT);
            }
            else {
                dIT = consumerTL.get();
                consumerTL.remove();
            }
        }
        // provider
        else {
            if (appid != null) {
                dIT = new DubboIT(appid);
                providerTL.set(dIT);
            }
            else {
                dIT = providerTL.get();
                providerTL.remove();
            }
        }

        return dIT;
    }

    private String appId;

    public DubboIT(String appid) {

        this.appId = appid;
    }

    public void doMonitor(Object[] args, boolean isDoCap, Throwable e) {

        Invoker<?> invoker = (Invoker<?>) args[0];
        Invocation invocation = (Invocation) args[1];
        Result result = null;
        if (isDoCap && args.length == 3) {
            result = (Result) args[2];
        }

        // consumer
        if (Constants.CONSUMER_SIDE.equals(invoker.getUrl().getParameter(Constants.SIDE_KEY))) {
            consumerCap(isDoCap, e, invoker, invocation, result);
        }
        // provider
        else {
            providerCap(isDoCap, e, invoker, invocation, result);
        }
    }

    /**
     * consumerCap
     * 
     * @param isDoCap
     * @param e
     * @param invoker
     * @param invocation
     */
    @SuppressWarnings("unchecked")
    private void consumerCap(boolean isDoCap, Throwable e, Invoker<?> invoker, Invocation invocation, Result result) {

        if (isDoCap == false) {

            URL url = invoker.getUrl();
            // String application = url.getParameter(Constants.APPLICATION_KEY);
            // String service = invoker.getInterface().getName(); // 获取服务名称
            String method = RpcUtils.getMethodName(invocation); // 获取方法名
            int localPort = url.getPort();
            String protocol = url.getProtocol();
            String path = url.getPath();

            Map<String, Object> params = new HashMap<String, Object>();

            String requestURL = getRequestURL(url, method, localPort, protocol, path);

            params.put(CaptureConstants.INFO_CLIENT_REQUEST_URL, requestURL);
            params.put(CaptureConstants.INFO_CLIENT_REQUEST_ACTION, method);
            params.put(CaptureConstants.INFO_CLIENT_APPID, this.appId);
            params.put(CaptureConstants.INFO_CLIENT_TYPE, "dubbo.consumer");
            UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                    Monitor.CapturePhase.PRECAP, params);

            // register adapter
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter",
                    "registerAdapter", DubboConsumerAdapter.class);

            Object[] args = { invoker, invocation, result };
            ivcContextParams = (Map<String, Object>) UAVServer.instance().runSupporter(
                    "com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                    InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.PRECAP, params,
                    DubboConsumerAdapter.class, args);
        }
        else {
            int respCode = 1;
            if (e != null || (result != null && result.getException() != null)) {
                respCode = -1;
            }
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(CaptureConstants.INFO_CLIENT_TARGETSERVER, "dubbo");
            params.put(CaptureConstants.INFO_CLIENT_RESPONSECODE, respCode);

            UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                    Monitor.CapturePhase.DOCAP, params);

            if (respCode == -1) {
                String exceptionStr = "";
                if (e == null) {
                    exceptionStr = result.getException().toString();
                }
                else {
                    exceptionStr = e.toString();
                }
                params.put(CaptureConstants.INFO_CLIENT_RESPONSESTATE, exceptionStr);
            }
            if (ivcContextParams != null) {
                ivcContextParams.putAll(params);
            }

            Object[] args = { invoker, invocation, result };

            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                    InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.DOCAP, ivcContextParams,
                    DubboConsumerAdapter.class, args);
        }
    }

    /**
     * providerCap
     * 
     * @param isDoCap
     * @param e
     * @param invoker
     * @param invocation
     */
    private void providerCap(boolean isDoCap, Throwable e, Invoker<?> invoker, Invocation invocation, Result result) {

        // on service start pre-cap
        if (isDoCap == false) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(CaptureConstants.INFO_CAPCONTEXT_TAG, "dubbo");
            UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_SERVER_CONNECTOR,
                    Monitor.CapturePhase.PRECAP, params);

            URL url = invoker.getUrl();
            String method = RpcUtils.getMethodName(invocation); // 获取方法名
            int localPort = url.getPort();
            String protocol = url.getProtocol();
            String path = url.getPath();
            String requestURL = getRequestURL(url, method, localPort, protocol, path);
            params.put(CaptureConstants.INFO_APPSERVER_APPID, this.appId);
            params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_REQUEST_URL, requestURL);
            params.put(InvokeChainConstants.PARAM_RPCHEAD_SPANINFO,
                    RpcContext.getContext().getAttachment(InvokeChainConstants.PARAM_RPCHEAD_SPANINFO));
            params.put(InvokeChainConstants.PARAM_RPCHEAD_INFO, "dubbo.provider");
            params.put(InvokeChainConstants.PARAM_REMOTE_SRC_INFO,
                    RpcContext.getContext().getLocalAddress().toString());

            // register adapter
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter",
                    "registerAdapter", DubboProviderAdapter.class);

            Object[] args = { invoker, invocation, result };
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                    InvokeChainConstants.CHAIN_APP_SERVICE, InvokeChainConstants.CapturePhase.PRECAP, params,
                    DubboProviderAdapter.class, args);
        }
        // on service start do-cap
        else {
            Map<String, Object> params = new HashMap<String, Object>();
            URL url = invoker.getUrl();
            // String application = url.getParameter(Constants.APPLICATION_KEY);
            String service = invoker.getInterface().getName(); // 获取服务名称
            String method = RpcUtils.getMethodName(invocation); // 获取方法名
            int localPort = url.getPort();
            String protocol = url.getProtocol();
            String path = url.getPath();

            String requestURL = getRequestURL(url, method, localPort, protocol, path);
            params.put(CaptureConstants.INFO_CAPCONTEXT_TAG, "dubbo");
            params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_REQUEST_URL, requestURL);
            params.put(CaptureConstants.INFO_APPSERVER_APPID, this.appId);
            int respCode = 1;
            if (e != null || (result != null && result.getException() != null)) {
                respCode = -1;
            }
            params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_RESPONSECODE, String.valueOf(respCode));

            UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_SERVER_CONNECTOR,
                    Monitor.CapturePhase.DOCAP, params);

            if (respCode == -1) {
                String exceptionStr = "";
                if (e == null) {
                    exceptionStr = result.getException().toString();
                }
                else {
                    exceptionStr = e.toString();
                }
                params.put(CaptureConstants.INFO_CLIENT_RESPONSESTATE, exceptionStr);
            }
            params.put(InvokeChainConstants.CLIENT_IT_CLASS, service);
            params.put(InvokeChainConstants.CLIENT_IT_METHOD, method);

            Object[] args = { invoker, invocation, result };
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                    InvokeChainConstants.CHAIN_APP_SERVICE, InvokeChainConstants.CapturePhase.DOCAP, params,
                    DubboProviderAdapter.class, args);
        }
    }

    private String getRequestURL(URL url, String method, int localPort, String protocol, String path) {

        StringBuilder requestURL = new StringBuilder();

        requestURL.append(protocol).append("://").append(NetworkHelper.getLocalIP()).append(":").append(localPort);

        String group = url.getParameter(Constants.GROUP_KEY);

        if (!StringHelper.isEmpty(group)) {
            requestURL.append(":").append(group);
        }

        requestURL.append("/").append(path);

        String version = url.getParameter(Constants.VERSION_KEY);

        if (!StringHelper.isEmpty(version)) {
            requestURL.append(".").append(version);
        }

        requestURL.append("/").append(method);

        return requestURL.toString();
    }

    /**
     * collect all ServiceBeans for later profiling
     * 
     * @param args
     */
    @SuppressWarnings("rawtypes")
    public void doProfiling(Object[] args) {

        InterceptContext ic = InterceptSupport.instance().getThreadLocalContext(Event.WEBCONTAINER_STARTED);

        @SuppressWarnings("unchecked")
        Map<String, DubboServiceProfileInfo> list = (Map<String, DubboServiceProfileInfo>) ic
                .get(HookConstants.DUBBO_PROFILE_LIST);

        if (null == list) {

            list = new HashMap<String, DubboServiceProfileInfo>();

            ic.put(HookConstants.DUBBO_PROFILE_LIST, list);
        }

        ServiceBean sb = (ServiceBean) args[0];

        String serviceClass = sb.getInterface();

        String serviceImplClass = sb.getRef().getClass().getName();
        // the refclass is enhance by SpringCGLIB, className like "ServiceImplClass$$EnhancerBySpringCGLIB$$2a862c3d"
        if (serviceImplClass.indexOf("$$") > -1) {
            serviceImplClass = serviceImplClass.substring(0, serviceImplClass.indexOf("$$"));
        }

        String group = (sb.getGroup() == null) ? "" : sb.getGroup();
        if (sb.getGroup() == null && sb.getProvider() != null && sb.getProvider().getGroup() != null) {
            group = sb.getProvider().getGroup();
        }

        String version = (sb.getVersion() == null) ? "" : sb.getVersion();
        if (sb.getVersion() == null && sb.getProvider() != null && sb.getProvider().getVersion() != null) {
            version = sb.getProvider().getVersion();
        }

        String serviceKey = serviceClass;

        if (!"".equals(group) && !"".equals(version)) {
            serviceKey += ":" + group + ":" + version;
        }
        else if (!"".equals(group) && "".equals(version)) {
            serviceKey += ":" + group;
        }
        else if ("".equals(group) && !"".equals(version)) {
            serviceKey += ":none:" + version;
        }

        if (list.containsKey(serviceKey)) {
            return;
        }

        DubboServiceProfileInfo dspi = new DubboServiceProfileInfo();

        String dbAppId = sb.getApplication().getName();

        if (sb.getApplication().getVersion() != null) {
            dbAppId += "-" + sb.getApplication().getVersion();
        }

        dspi.setAppId(appId);
        dspi.setDbAppId(dbAppId);
        dspi.setServiceClass(serviceClass);
        dspi.setServiceImplClass(serviceImplClass);
        dspi.setGroup(group);
        dspi.setVersion(version);

        @SuppressWarnings("unchecked")
        List<URL> urlList = sb.getExportedUrls();

        if (urlList != null) {
            for (URL url : urlList) {
                DubboServiceProfileInfo.Protocol pro = new DubboServiceProfileInfo.Protocol();
                pro.setPort(url.getPort());
                pro.setpName(url.getProtocol());
                int pathLength = url.getPath().length();
                // 长度加1为了去除反斜杠/
                int interfaceLength = url.getServiceInterface().length() + 1;
                String contextpath = null;
                if (pathLength > interfaceLength + 1) {
                    contextpath = url.getPath().substring(0, pathLength - interfaceLength);
                }
                pro.setContextpath(contextpath);
                pro.setCharset(url.getParameter("charset"));
                // dubbo当前默认的序列化方式为hessian2，不知道日后会不会变，故此处不进行默认赋值
                pro.setSerialization(url.getParameter("serialization"));
                dspi.addProtocol(pro);
            }
        }

        list.put(serviceKey, dspi);
    }
}
