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

package com.creditease.uav.hook.rocketmq;

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
public class RocketmqHookProxy extends HookProxy {

    private final DynamicProxyInstaller dpInstall;

    @SuppressWarnings("rawtypes")
    public RocketmqHookProxy(String id, Map config) {
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
         * install proxy to rocketmqClient
         */
        DynamicProxyProcessor processor = new DynamicProxyProcessor() {

            @Override
            public void process(CtMethod m) throws Exception {

                if ((m.getExceptionTypes().length != 0 || "shutdown".equals(m.getName())
                        || "unsubscribe".equals(m.getName()) || "registerMessageListener".equals(m.getName()))
                        && /*
                            * this method could be blocked for a long time, the statics is meaningless, so we ignore it.
                            */
                !("pullBlockIfNotFound").equals(m.getName())) {
                    if ("registerMessageListener".equals(m.getName())) {
                        if ("MessageListenerConcurrently".equals(m.getParameterTypes()[0].getSimpleName())) {

                            m.insertBefore("{$1=(MessageListenerConcurrently)RocketmqIT.start(\"" + appid + "\",this,\""
                                    + m.getName() + "\",$args);}");
                        }
                        else if ("MessageListenerOrderly".equals(m.getParameterTypes()[0].getSimpleName())) {

                            m.insertBefore("{$1=(MessageListenerOrderly)RocketmqIT.start(\"" + appid + "\",this,\""
                                    + m.getName() + "\",$args);}");
                        }
                        m.insertAfter("{RocketmqIT.end(-1,\"" + m.getName() + "\");}");

                    }
                    else if (m.getName().equals("pull") && !m.getReturnType().getSimpleName().equals("PullResult")) {
                        m.insertBefore("{$5=(PullCallback)RocketmqIT.start(\"" + appid + "\",this,\"" + m.getName()
                                + "\",$args);}");
                        m.insertAfter("{RocketmqIT.end(-1,\"" + m.getName() + "\");}");

                    }
                    else {
                        m.insertBefore("{RocketmqIT.start(\"" + appid + "\",this,\"" + m.getName() + "\",$args);}");
                        m.insertAfter("{RocketmqIT.end(1,\"" + m.getName() + "\");}");
                    }
                    dpInstall.addCatch(m, "RocketmqIT.end(0,\"" + m.getName() + "\");");
                }

            }
        };

        dpInstall.installProxy("com.alibaba.rocketmq.client.producer.DefaultMQProducer", new String[] {
                "com.creditease.uav.hook.rocketmq.interceptors", "com.alibaba.rocketmq.client.consumer.listener" },
                processor, false);

        dpInstall.installProxy("com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer",
                new String[] { "com.creditease.uav.hook.rocketmq.interceptors" }, processor, false);

        dpInstall.installProxy(
                "com.alibaba.rocketmq.client.consumer.DefaultMQPullConsumer", new String[] {
                        "com.creditease.uav.hook.rocketmq.interceptors", "com.alibaba.rocketmq.client.consumer" },
                processor, false);

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
