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

package com.creditease.uav.hook.httpclients3.sync;

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
public class HttpClient3HookProxy extends HookProxy {

    private final DynamicProxyInstaller dpInstall;

    @SuppressWarnings("rawtypes")
    public HttpClient3HookProxy(String id, Map config) {
        super(id, config);
        dpInstall = new DynamicProxyInstaller();

    }

    @Override
    public void start(HookContext context, ClassLoader webapploader) {

        Event evt = context.get(Event.class);

        switch (evt) {
            case WEBCONTAINER_RESOURCE_INIT:
                break;
            case WEBCONTAINER_INIT:
                InsertInterceptToClients(context, webapploader);
                break;
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
         * install proxy to httpclient 3.x
         */
        dpInstall.installProxy("org.apache.commons.httpclient.HttpClient",
                new String[] { "com.creditease.uav.hook.httpclients3.sync.interceptors" }, new DynamicProxyProcessor() {

                    @Override
                    public void process(CtMethod m) throws Exception {

                        if ("executeMethod".equals(m.getName()) && m.getParameterTypes().length == 3) {

                            m.insertBefore("{ApacheHttpClient3IT.start(\"" + appid + "\",new Object[]{$1,$2,$3});}");
                            m.insertAfter("{ApacheHttpClient3IT.end(new Object[]{$2});}");
                            dpInstall.addCatch(m, "ApacheHttpClient3IT.end(new Object[]{$e});");
                        }
                    }
                }, false);
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
