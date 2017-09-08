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

package com.creditease.uav.hook.httpclients.sync;

import java.util.Map;

import com.creditease.monitor.appfra.hook.spi.HookConstants;
import com.creditease.monitor.appfra.hook.spi.HookContext;
import com.creditease.monitor.appfra.hook.spi.HookProxy;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;
import com.creditease.uav.monitorframework.dproxy.DynamicProxyInstaller;
import com.creditease.uav.monitorframework.dproxy.DynamicProxyProcessor;
import com.creditease.uav.util.MonitorServerUtil;

import javassist.CtMethod;

/**
 * 
 * HttpClientHookProxy description: helps to install proxy class to http clients
 *
 */
public class HttpClientHookProxy extends HookProxy {

    private final DynamicProxyInstaller dpInstall;

    @SuppressWarnings("rawtypes")
    public HttpClientHookProxy(String id, Map config) {
        super(id, config);
        dpInstall = new DynamicProxyInstaller();

    }

    @Override
    public void start(HookContext context, ClassLoader webapploader) {

        Event evt = context.get(Event.class);

        switch (evt) {
            case WEBCONTAINER_INIT:
                InsertInterceptToClients(context, webapploader);
                break;
            // when servlet init
            case AFTER_SERVET_INIT:
                break;
            case BEFORE_SERVLET_DESTROY:
                break;
            case WEBCONTAINER_STARTED:

                break;
            case WEBCONTAINER_STOPPED:
                break;
            default:
                break;
        }
    }

    public void InsertInterceptToClients(HookContext context, ClassLoader webapploader) {

        InterceptContext ic = (InterceptContext) context.get(HookConstants.INTERCEPTCONTEXT);

        String contextPath = (String) ic.get(InterceptConstants.CONTEXTPATH);
        String basePath = (String) ic.get(InterceptConstants.BASEPATH);

        final String appid = MonitorServerUtil.getApplicationId(contextPath, basePath);

        doInstallDProxy(webapploader, appid);
    }

    public void doInstallDProxy(ClassLoader webapploader, final String appid) {

        /**
         * set the webapploader is the target classloader
         */
        dpInstall.setTargetClassLoader(webapploader);

        /**
         * install proxy to AbstractHttpClient before 4.3
         */
        dpInstall.installProxy("org.apache.http.impl.client.AbstractHttpClient",
                new String[] { "com.creditease.uav.hook.httpclients.sync.interceptors" }, new DynamicProxyProcessor() {

                    @Override
                    public void process(CtMethod m) throws Exception {

                        if ("execute".equals(m.getName())) {
                            if (m.getParameterTypes().length == 3
                                    && m.getReturnType().getSimpleName().equals("HttpResponse")) {

                                m.insertBefore("{ApacheHttpClientIT.start(\"" + appid + "\",new Object[]{$1,$2,$3});}");
                                m.insertAfter("{ApacheHttpClientIT.end(new Object[]{$_});}");
                                dpInstall.addCatch(m, "ApacheHttpClientIT.end(new Object[]{$e});");
                            }
                        }
                    }
                }, false);

        /**
         * install proxy to InternalHttpClient after 4.3
         */
        dpInstall.installProxy("org.apache.http.impl.client.InternalHttpClient",
                new String[] { "com.creditease.uav.hook.httpclients.sync.interceptors" }, new DynamicProxyProcessor() {

                    @Override
                    public void process(CtMethod m) throws Exception {

                        if ("doExecute".equals(m.getName())) {
                            m.insertBefore("{ApacheHttpClientIT.start(\"" + appid + "\",new Object[]{$1,$2,$3});}");
                            m.insertAfter("{ApacheHttpClientIT.end(new Object[]{$_});}");
                            dpInstall.addCatch(m, "ApacheHttpClientIT.end(new Object[]{$e});");
                        }
                    }
                }, false);

        /**
         * install proxy to MinimalHttpClient after 4.3
         */
        dpInstall.installProxy("org.apache.http.impl.client.MinimalHttpClient",
                new String[] { "com.creditease.uav.hook.httpclients.sync.interceptors" }, new DynamicProxyProcessor() {

                    @Override
                    public void process(CtMethod m) throws Exception {

                        if ("doExecute".equals(m.getName())) {
                            m.insertBefore("{ApacheHttpClientIT.start(\"" + appid + "\",new Object[]{$1,$2,$3});}");
                            m.insertAfter("{ApacheHttpClientIT.end(new Object[]{$_});}");
                            dpInstall.addCatch(m, "ApacheHttpClientIT.end(new Object[]{$e});");
                        }
                    }
                }, false);

        // release loader
        dpInstall.releaseTargetClassLoader();
    }

    @Override
    public void stop(HookContext context, ClassLoader webapploader) {

        Event evt = context.get(Event.class);

        switch (evt) {
            case AFTER_SERVET_INIT:
                break;
            case BEFORE_SERVLET_DESTROY:
                break;
            case WEBCONTAINER_STARTED:
                break;
            case WEBCONTAINER_STOPPED:
                break;
            default:
                break;

        }
    }

}
