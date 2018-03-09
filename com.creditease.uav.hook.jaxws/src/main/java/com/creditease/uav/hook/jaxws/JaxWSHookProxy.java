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

package com.creditease.uav.hook.jaxws;

import java.util.Map;

import com.creditease.monitor.appfra.hook.spi.HookConstants;
import com.creditease.monitor.appfra.hook.spi.HookContext;
import com.creditease.monitor.appfra.hook.spi.HookProxy;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;
import com.creditease.uav.hook.jaxws.interceptors.JaxWSHookIT;
import com.creditease.uav.monitorframework.dproxy.DynamicProxyInstaller;
import com.creditease.uav.monitorframework.dproxy.DynamicProxyProcessor;
import com.creditease.uav.monitorframework.dproxy.bytecode.DPMethod;
import com.creditease.uav.util.MonitorServerUtil;

public class JaxWSHookProxy extends HookProxy {

    private final DynamicProxyInstaller dpInstall;

    @SuppressWarnings("rawtypes")
    public JaxWSHookProxy(String id, Map config) {
        super(id, config);
        dpInstall = new DynamicProxyInstaller();

    }

    @Override
    public void start(HookContext context, ClassLoader webapploader) {

        Event event = context.get(Event.class);
        switch (event) {
            case SPRING_BEAN_REGIST:
            case WEBCONTAINER_INIT:
                insertIntercepter(context, webapploader);
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

    private void insertIntercepter(HookContext context, ClassLoader webapploader) {

        if (isHookEventDone("InsertInterceptToClients")) {
            return;
        }

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
         * install proxy to javax.xml.ws.Service
         */
        dpInstall.installProxy("javax.xml.ws.Service", new String[] { "com.creditease.uav.hook.jaxws.interceptors" },
                new DynamicProxyProcessor() {

                    @Override
                    public void process(DPMethod m) throws Exception {

                        if ("getPort".equals(m.getName())) {

                            dpInstall.defineLocalVal(m, "mObj", JaxWSHookIT.class);
                            m.insertBefore("{mObj=new JaxWSHookIT(\"" + appid + "\");}");
                            m.insertAfter("{$_=mObj.getPort($_,this,$args);}");

                        }

                        if ("createDispatch".equals(m.getName())) {

                            dpInstall.defineLocalVal(m, "mObj", JaxWSHookIT.class);
                            m.insertBefore("{mObj=new JaxWSHookIT(\"" + appid + "\");}");
                            m.insertAfter("{$_=mObj.createDispatch($_,this,$args);}");
                        }
                    }
                }, false);

        /**
         * adapts: 实现对JDKProxyInvokeHandler封装的Proxy对象的替换，替换为原来的对象，因为在有些场景下对方原来的对象有一些非接口的方法需要调用
         * 比如CXF中ClientProxy.getClient(Object o), 需要原来的对象才能使用
         */
        dpInstall.doAdapts(this.getAdapts());

        // release loader
        dpInstall.releaseTargetClassLoader();
    }

    @Override
    public void stop(HookContext context, ClassLoader webapploader) {

        Event event = context.get(Event.class);
        switch (event) {
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
