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

package com.creditease.uav.hook.dubbo;

import java.util.Map;

import com.creditease.monitor.appfra.hook.spi.HookConstants;
import com.creditease.monitor.appfra.hook.spi.HookContext;
import com.creditease.monitor.appfra.hook.spi.HookProxy;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;
import com.creditease.uav.hook.dubbo.interceptors.DubboIT;
import com.creditease.uav.monitorframework.dproxy.DynamicProxyInstaller;
import com.creditease.uav.monitorframework.dproxy.DynamicProxyProcessor;
import com.creditease.uav.util.MonitorServerUtil;

import javassist.CtMethod;

public class DubboHookProxy extends HookProxy {

    private final DynamicProxyInstaller dpInstall;

    @SuppressWarnings("rawtypes")
    public DubboHookProxy(String id, Map config) {
        super(id, config);
        dpInstall = new DynamicProxyInstaller();
    }

    @Override
    public void start(HookContext context, ClassLoader webapploader) {

        Event evt = context.get(Event.class);

        switch (evt) {
            case SPRING_BEAN_REGIST:
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

    private void InsertInterceptToClients(HookContext context, ClassLoader webapploader) {

        if (isHookEventDone("InsertInterceptToClients")) {
            return;
        }
        InterceptContext ic = (InterceptContext) context.get(HookConstants.INTERCEPTCONTEXT);

        String contextPath = (String) ic.get(InterceptConstants.CONTEXTPATH);
        String basePath = (String) ic.get(InterceptConstants.BASEPATH);

        final String appid = MonitorServerUtil.getApplicationId(contextPath, basePath);

        /**
         * set the webapploader is the target classloader
         */
        dpInstall.setTargetClassLoader(webapploader);

        /**
         * inject ServiceBean for profiling
         */
        dpInstall.installProxy("com.alibaba.dubbo.config.spring.ServiceBean",
                new String[] { "com.creditease.uav.hook.dubbo.interceptors" }, new DynamicProxyProcessor() {

                    @Override
                    public void process(CtMethod m) throws Exception {

                        if ("onApplicationEvent".equals(m.getName())) {

                            dpInstall.defineLocalVal(m, "mObj", DubboIT.class);
                            m.insertAfter(
                                    "{mObj=new DubboIT(\"" + appid + "\");mObj.doProfiling(new Object[]{this,$1});}");

                        }
                    }
                }, false);

        /**
         * inject Dubbo's MonitorFilter to get Service & Consumer Perf Data
         */
        dpInstall.installProxy("com.alibaba.dubbo.monitor.support.MonitorFilter",
                new String[] { "com.creditease.uav.hook.dubbo.interceptors" }, new DynamicProxyProcessor() {

                    @Override
                    public void process(CtMethod m) throws Exception {

                        if ("invoke".equals(m.getName())) {

                            dpInstall.defineLocalVal(m, "mObj", DubboIT.class);
                            m.insertBefore(
                                    "{DubboIT.doMonitorStart(\"" + appid + "\",new Object[]{$1,$2},false,null);}");
                            m.insertAfter("{DubboIT.doMonitorEnd(new Object[]{$1,$2,$_},true,null);}");
                            dpInstall.addCatch(m, "{DubboIT.doMonitorEnd(new Object[]{$1,$2},true,$e);throw $e;}");

                        }
                    }
                }, false);

        // release loader
        dpInstall.releaseTargetClassLoader();
    }

    @Override
    public void stop(HookContext context, ClassLoader webapploader) {

    }

}
