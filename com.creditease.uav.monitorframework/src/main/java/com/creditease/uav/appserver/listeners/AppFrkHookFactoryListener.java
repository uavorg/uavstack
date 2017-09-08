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

package com.creditease.uav.appserver.listeners;

import java.util.List;
import java.util.Map;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.monitor.UAVServer.ServerVendor;
import com.creditease.monitor.appfra.hook.spi.HookConstants;
import com.creditease.monitor.appfra.hook.spi.HookContext;
import com.creditease.monitor.appfra.hook.spi.HookFactory;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;
import com.creditease.monitor.interceptframework.spi.InterceptEventListener;

public class AppFrkHookFactoryListener extends InterceptEventListener {

    private ServerVendor vendor;
    private HookFactory hookfactory = null;
    @SuppressWarnings("rawtypes")
    private List<Map> hookConfig;

    public AppFrkHookFactoryListener() {
        vendor = (ServerVendor) this.getServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR);
        initHookFactory();
    }

    @Override
    public void handleEvent(InterceptContext context) {

        // get webapp classloader
        ClassLoader webapploader = (ClassLoader) context.get(InterceptConstants.WEBAPPLOADER);

        // swtich event
        Event evt = context.getEvent();

        switch (evt) {
            case WEBCONTAINER_RESOURCE_INIT:
            case WEBCONTAINER_RESOURCE_CREATE:
            case WEBCONTAINER_INIT:
            case AFTER_SERVET_INIT:
            case WEBCONTAINER_STARTED:
                startAppFrkHook(evt, webapploader, context);

                break;
            case BEFORE_SERVLET_DESTROY:
            case WEBCONTAINER_STOPPED:
                stopAppFrkHook(evt, webapploader, context);

                break;
            default:
                break;

        }
    }

    private void initHookFactory() {

        String config = System.getProperty("com.creditease.uav.hookfactory.config");

        hookConfig = JSONHelper.toObjectArray(config, Map.class);

        String hookFactoryClassStr = System
                .getProperty("com.creditease.uav." + vendor.toString().toLowerCase() + ".hookfactory");

        if (hookFactoryClassStr == null) {
            hookfactory = new HookFactory();
            return;
        }

        /**
         * use customzied hookfactory
         */
        Class<?> hookFactoryClass = null;
        try {
            hookFactoryClass = this.getClass().getClassLoader().loadClass(hookFactoryClassStr);

            this.logger.info("Customized HookFactory[" + hookFactoryClassStr + "] is loaded.");
        }
        catch (ClassNotFoundException e) {
            // ignore
        }

        try {
            hookfactory = (HookFactory) hookFactoryClass.newInstance();
        }
        catch (InstantiationException e) {
            this.logger.error("create HookFactory Instance [" + hookFactoryClassStr + "] FAIL", e);
        }
        catch (IllegalAccessException e) {
            this.logger.error("create HookFactory Instance [" + hookFactoryClassStr + "] FAIL", e);
        }
    }

    private void stopAppFrkHook(Event evt, ClassLoader webapploader, InterceptContext context) {

        if (hookfactory == null) {
            return;
        }

        String contextPath = (String) context.get(InterceptConstants.CONTEXTPATH);

        if (contextPath.equals("/com.creditease.uav")) {
            return;
        }

        // create hook context
        HookContext hcontext = hookfactory.createHookContext();

        hcontext.put(Event.class, evt);

        hcontext.put(HookConstants.INTERCEPTCONTEXT, context);

        boolean shouldRemove = false;

        /**
         * when webapp stopped, we should remove the hook instance
         */
        if (evt == Event.WEBCONTAINER_STOPPED) {
            shouldRemove = true;
        }

        for (@SuppressWarnings("rawtypes")
        Map config : this.hookConfig) {
            hookfactory.stopHook(webapploader, (String) config.get("proxy"), hcontext, shouldRemove);
        }
    }

    private void startAppFrkHook(Event evt, ClassLoader webapploader, InterceptContext context) {

        if (hookfactory == null) {
            return;
        }

        String contextPath = (String) context.get(InterceptConstants.CONTEXTPATH);

        if (contextPath.equals("/com.creditease.uav")) {
            return;
        }

        // create hook context
        HookContext hcontext = hookfactory.createHookContext();

        hcontext.put(Event.class, evt);

        // add InterceptContext to HookContext
        hcontext.put(HookConstants.INTERCEPTCONTEXT, context);

        hcontext.put(HookConstants.HOOKCONFIG, this.hookConfig);

        // start hooks
        for (@SuppressWarnings("rawtypes")
        Map config : this.hookConfig) {
            hookfactory.startHook(webapploader, (String) config.get("detect"), (String) config.get("jar"),
                    (String) config.get("proxy"), config, hcontext);
        }
    }

    @Override
    public boolean isEventListener(Event event) {

        switch (event) {
            case WEBCONTAINER_INIT:
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
        return true;
    }

    /**
     * getHookFactory
     * 
     * @return
     */
    public HookFactory getHookFactory() {

        return this.hookfactory;
    }
}
