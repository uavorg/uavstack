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

package com.creditease.uav.monitorframework.webservice.listeners;

import java.util.List;

import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;
import com.creditease.monitor.interceptframework.spi.InterceptEventListener;
import com.creditease.uav.monitorframework.dproxy.DynamicProxyInstaller;
import com.creditease.uav.monitorframework.dproxy.DynamicProxyProcessor;
import com.creditease.uav.monitorframework.webservice.interceptors.WebServiceListenerIT;
import com.creditease.uav.util.MonitorServerUtil;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import javassist.CtMethod;

public class WebServiceListener extends InterceptEventListener {

    private final DynamicProxyInstaller dpInstall;

    public WebServiceListener() {
        super();
        dpInstall = new DynamicProxyInstaller();
    }

    @Override
    public boolean isEventListener(Event event) {

        switch (event) {
            case WEBCONTAINER_INIT:
                return true;
            case WEBCONTAINER_RESOURCE_CREATE:
                break;
            case AFTER_SERVET_INIT:
                break;
            case WEBCONTAINER_STARTED:
                break;
            case WEBCONTAINER_STOPPED:
            default:
                break;

        }
        return false;

    }

    public void insertWebServiceInterceptor(InterceptContext context, ClassLoader webapploader) {

        String contextPath = (String) context.get(InterceptConstants.CONTEXTPATH);
        String basePath = (String) context.get(InterceptConstants.BASEPATH);
        final String appid = MonitorServerUtil.getApplicationId(contextPath, basePath);

        doInstallDProxy(webapploader, appid, contextPath);
    }

    public void doInstallDProxy(ClassLoader webapploader, final String appid, String contextPath) {

        // like SMS, which uses the sun jax-ws RI/Metro-jax-ws RI bind webservices when the servlet starts should
        // profile in ComponentProfileHandler

        FastClasspathScanner fcs = new FastClasspathScanner(new ClassLoader[] { webapploader });
        fcs.scan();
        List<String> endPoints = fcs.getNamesOfDirectSubclassesOf("javax.xml.ws.Endpoint");

        if (!endPoints.isEmpty()) {
            dpInstall.setTargetClassLoader(webapploader);

            for (int i = 0; i < endPoints.size(); i++) {

                dpInstall.installProxy(endPoints.get(i),
                        new String[] { "com.creditease.uav.monitorframework.webservice.interceptors" },
                        new DynamicProxyProcessor() {

                            @Override
                            public void process(CtMethod m) throws Exception {

                                if ("publish".equals(m.getName()) && m.getParameterTypes().length > 0
                                        && m.getParameterTypes()[0].getSimpleName().equals("String")) {

                                    dpInstall.defineLocalVal(m, "mObj", WebServiceListenerIT.class);
                                    m.insertBefore("{mObj=new WebServiceListenerIT(\"" + appid
                                            + "\");mObj.obtainWsInfo($1,getImplementor());}");

                                }

                            }
                        }, false);
            }

            // release loader
            dpInstall.releaseTargetClassLoader();
        }

    }

    @Override
    public void handleEvent(InterceptContext context) {

        ClassLoader webapploader = (ClassLoader) context.get(InterceptConstants.WEBAPPLOADER);

        // switch event
        Event evt = context.getEvent();
        switch (evt) {
            case WEBCONTAINER_INIT:
                insertWebServiceInterceptor(context, webapploader);
                break;
            case AFTER_SERVET_INIT:
            case WEBCONTAINER_STARTED:
            case BEFORE_SERVLET_DESTROY:
            case WEBCONTAINER_STOPPED:
            default:
                break;

        }
    }

}
