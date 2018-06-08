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

package com.creditease.uav.hook.kafka;

import java.util.Map;

import com.creditease.monitor.appfra.hook.spi.HookConstants;
import com.creditease.monitor.appfra.hook.spi.HookContext;
import com.creditease.monitor.appfra.hook.spi.HookProxy;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;
import com.creditease.uav.hook.kafka.interceptors.KafkaIT;
import com.creditease.uav.monitorframework.dproxy.DynamicProxyInstaller;
import com.creditease.uav.monitorframework.dproxy.DynamicProxyProcessor;
import com.creditease.uav.monitorframework.dproxy.bytecode.DPClass;
import com.creditease.uav.monitorframework.dproxy.bytecode.DPMethod;
import com.creditease.uav.util.MonitorServerUtil;

public class KafkaHookProxy extends HookProxy {

    private DynamicProxyInstaller dpInstaller;

    public KafkaHookProxy(String id, @SuppressWarnings("rawtypes") Map config) {

        super(id, config);
        dpInstaller = new DynamicProxyInstaller();
    }

    @Override
    public void start(HookContext context, ClassLoader webapploader) {

        Event evt = context.get(Event.class);

        switch (evt) {
            case SPRING_BEAN_REGIST:
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

    protected void insertIntercepter(HookContext context, ClassLoader webapploader) {

        if (isHookEventDone("insertIntercepter")) {
            return;
        }

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
        // KafkaConsumer
        dpInstaller.installProxy("org.apache.kafka.clients.consumer.internals.Fetcher",
                new String[] { "com.creditease.uav.hook.kafka.interceptors" }, new DynamicProxyProcessor() {

                    @Override
                    public void process(DPMethod m) throws Exception {

                        if ("parseRecord".equals(m.getName())) {
                            m.insertBefore("{KafkaIT.startPoll(\"" + appid
                                    + "\",\"poll\", new Object[]{$1,$2,this.metadata});}");
                            m.insertAfter("{KafkaIT.endPoll(\"poll\", new Object[]{$1,$2});}");
                            dpInstaller.addCatch(m, "KafkaIT.endPoll(\"poll\",new Object[]{$e});");

                        }
                    }

                }, false);
        // KafkaProducer
        dpInstaller.installProxy("org.apache.kafka.clients.producer.KafkaProducer",
                new String[] { "com.creditease.uav.hook.kafka.interceptors" }, new DynamicProxyProcessor() {

                    @Override
                    public void process(DPMethod m) throws Exception {
                        if ("doSend".equals(m.getName())) {
                            DPClass[] dp = m.getParameterTypes();
                            if (dp.length == 2) {
                                dpInstaller.defineLocalVal(m, "mObj", KafkaIT.class);
                                m.insertBefore("mObj=new KafkaIT(\"" + appid
                                        + "\");{if($2 ==null){mObj.syncSendStart(\"" + appid + "\",\"" + m.getName()
                                        + "\", new Object[]{this.producerConfig,$1});}else{$2=mObj.asyncSend(\"" + appid
                                        + "\",\"" + m.getName() + "\", new Object[]{this.producerConfig,$1,$2});}}");

                                m.insertAfter("{mObj.syncSendEnd(new Object[]{$_,$2},\"" + m.getName() + "\");}");
                            }
                        }
                    }

                }, false);

        dpInstaller.releaseTargetClassLoader();
    }

}
