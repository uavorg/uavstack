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

package com.creditease.uav.hook.redis.aredis;

import java.util.Map;

import com.creditease.monitor.appfra.hook.spi.HookConstants;
import com.creditease.monitor.appfra.hook.spi.HookContext;
import com.creditease.monitor.appfra.hook.spi.HookProxy;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;
import com.creditease.uav.hook.redis.aredis.interceptors.AredisCommandObjectIT;
import com.creditease.uav.monitorframework.dproxy.DynamicProxyInstaller;
import com.creditease.uav.monitorframework.dproxy.DynamicProxyProcessor;
import com.creditease.uav.util.MonitorServerUtil;

import javassist.CtMethod;

public class AredisHookProxy extends HookProxy {

    private DynamicProxyInstaller dpInstaller;

    public AredisHookProxy(String id, @SuppressWarnings("rawtypes") Map config) {
        super(id, config);
        dpInstaller = new DynamicProxyInstaller();

        // InvokeChainServer.instance().regiterAdapter("redis.client.Aredis", new RedisClientAdapter());
    }

    protected void insertIntercepter(HookContext context, ClassLoader webapploader) {

        InterceptContext ic = (InterceptContext) context.get(HookConstants.INTERCEPTCONTEXT);

        String contextPath = (String) ic.get(InterceptConstants.CONTEXTPATH);
        String basePath = (String) ic.get(InterceptConstants.BASEPATH);

        final String appid = MonitorServerUtil.getApplicationId(contextPath, basePath);

        doProxyInstall(webapploader, appid);

    }

    public void doProxyInstall(ClassLoader webapploader, final String appid) {

        /**
         * set the webapploader is the target classloader
         */
        dpInstaller.setTargetClassLoader(webapploader);

        dpInstaller.defineField("mMap", Map.class, "org.aredis.cache.RedisCommandObject", "new java.util.HashMap()");

        dpInstaller.installProxy("org.aredis.cache.RedisCommandObject",
                new String[] { "com.creditease.uav.hook.redis.aredis.interceptors" }, new DynamicProxyProcessor() {

                    @Override
                    public void process(CtMethod m) throws Exception {

                        if ("sendRequest".equals(m.getName())) {
                            dpInstaller.defineLocalVal(m, "mObj", AredisCommandObjectIT.class);
                            m.insertBefore("{mObj = new AredisCommandObjectIT(\"" + appid + "\");"
                                    + "mObj.doAsyncStart(new Object[]{$1, $2, $3, commandInfo});"
                                    + "mMap.put(\"mObj\",mObj);}");
                        }

                        if ("receiveResponse".equals(m.getName())) {
                            m.insertAfter("{AredisCommandObjectIT mObj = (AredisCommandObjectIT)mMap.get(\"mObj\");"
                                    + "mObj.doAsyncEnd(new Object[]{commandInfo});}");
                        }
                    }

                }, false);
    }

    @Override
    public void start(HookContext context, ClassLoader webapploader) {

        Event evt = context.get(Event.class);

        switch (evt) {
            case WEBCONTAINER_INIT:
                insertIntercepter(context, webapploader);
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

    @Override
    public void stop(HookContext context, ClassLoader webapploader) {

        Event evt = context.get(Event.class);

        switch (evt) {
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

}
