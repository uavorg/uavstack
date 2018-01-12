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

package com.creditease.monitor.appfra.hook.spi;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.creditease.agent.helpers.ReflectionHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.appfra.hook.StandardHookContext;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;
import com.creditease.monitor.log.Logger;

public class HookFactory {

    protected final Logger log;
    protected final Map<String, HookProxy> hooks = new ConcurrentHashMap<String, HookProxy>();

    protected final String mofRoot;

    public HookFactory() {
        this.log = UAVServer.instance().getLog();
        mofRoot = System.getProperty("com.creditease.uav.uavmof.root", "");
    }

    /**
     * create hook context
     * 
     * @return
     */
    public HookContext createHookContext() {

        return new StandardHookContext();
    }

    /**
     * start application framework hook
     * 
     * @param webapploader
     *            AppServer WebAppClassLoader
     * @param detectClass
     *            the class is used to defect if the application framework exists
     * @param loaderName
     *            the loader tag helps to load the hook jars
     * @param proxyClass
     *            the hook proxy class to run hook logic
     */
    @SuppressWarnings("rawtypes")
    public void startHook(ClassLoader webapploader, String detectClass, String loaderName, String proxyClass,
            Map hookConfig, HookContext cntx) {

        if (webapploader == null || detectClass == null || loaderName == null || proxyClass == null) {
            return;
        }

        // see if the app framework is there
        try {
            webapploader.loadClass(detectClass);
        }
        catch (ClassNotFoundException e) {
            // ignore
            if (log.isDebugable()) {
                log.debug("LOAD detectclass[" + detectClass + "] FAIL. ", e);
            }
            return;
        }

        InterceptContext context = (InterceptContext) cntx.get(HookConstants.INTERCEPTCONTEXT);

        Event evt = cntx.get(Event.class);

        if (log.isLogEnabled()) {
            log.info("START hook for proxy=" + proxyClass + ",detect=" + detectClass + ",jar=" + loaderName + ",evt="
                    + evt.toString());
        }

        /**
         * NOTE: there is only one HookProxy instance for each proxyClass and each webapploader
         */

        String contextPath = (String) context.get(InterceptConstants.CONTEXTPATH);

        String proxyKey = getHookProxyKey(contextPath, proxyClass);

        HookProxy proxy = null;

        boolean isNew = false;
        if (hooks.containsKey(proxyKey)) {
            proxy = hooks.get(proxyKey);
        }
        else {

            @SuppressWarnings("unchecked")
            List<String> supports = (List<String>) hookConfig.get("supports");

            // install hook jars
            this.installHookJars(webapploader, loaderName, supports);

            // new hookproxy instance
            try {

                Class<?> hookproxyClass = webapploader.loadClass(proxyClass);
                Constructor<?> con = hookproxyClass.getConstructor(new Class<?>[] { String.class, Map.class });
                proxy = (HookProxy) con.newInstance(new Object[] { proxyKey, hookConfig });
                isNew = true;
            }
            catch (Exception e) {
                log.error("CREATE hookproxy[" + proxyClass + "] instance FAIL. ", e);
                return;
            }
        }

        try {
            if (log.isDebugable()) {
                log.debug("START hookproxy[" + proxyClass + "] ", null);
            }

            // start hookproxy
            proxy.start(cntx, webapploader);

            // only start SUCCESS to store hookproxy
            if (isNew == true) {
                hooks.put(proxyKey, proxy);
            }

            if (log.isDebugable()) {
                log.debug("START hookproxy[" + proxyClass + "] END", null);
            }
        }
        catch (Exception e) {
            log.error("START hookproxy[" + proxyClass + "] FAIL. ", e);
        }
        catch (Error e) {
            log.error("START hookproxy[" + proxyClass + "] FAIL. ", e);
        }

    }

    protected String getHookProxyKey(String appName, String proxyClass) {

        String proxyKey = proxyClass + "@" + appName;
        return proxyKey;
    }

    /**
     * stop application framework hook
     * 
     * @param webapploader
     * @param proxyClass
     * @param cntx
     */
    public void stopHook(ClassLoader webapploader, String proxyClass, HookContext cntx, boolean shouldRemove) {

        try {
            if (log.isLogEnabled()) {
                log.info("STOP hookproxy[" + proxyClass + "] ");
            }

            InterceptContext context = (InterceptContext) cntx.get(HookConstants.INTERCEPTCONTEXT);

            String contextPath = (String) context.get(InterceptConstants.CONTEXTPATH);

            String proxyKey = getHookProxyKey(contextPath, proxyClass);

            HookProxy proxy = (!shouldRemove) ? hooks.get(proxyKey) : hooks.remove(proxyKey);

            if (proxy == null) {
                if (log.isDebugable()) {
                    log.debug("no hookproxy[" + proxyClass + "] should be STOP", null);
                }
                return;
            }

            // run stop hookproxy
            proxy.stop(cntx, webapploader);

            if (log.isDebugable()) {
                log.debug("STOP hookproxy[" + proxyClass + "]  END", null);
            }
        }
        catch (Exception e) {
            log.error("STOP hookproxy[" + proxyClass + "] FAIL. ", e);
            return;
        }
    }

    /**
     * install hook jars to webappclassloader
     * 
     * @param webapploader
     * @param loaderName
     * @param supports
     */
    public void installHookJars(ClassLoader webapploader, String loaderName, List<String> supports) {

        String loaderPath = this.mofRoot + "/com.creditease.uav.appfrk/" + loaderName;
        if (log.isDebugable()) {
            log.debug("loader path=" + loaderPath, null);
        }

        // add app framework hook jars to web app classloader
        try {

            if (supports != null) {
                for (String support : supports) {
                    ReflectionHelper.invoke(URLClassLoader.class.getName(), webapploader, "addURL",
                            new Class<?>[] { URL.class },
                            new Object[] {
                                    new URL("file:" + this.mofRoot + "/com.creditease.uav.appfrk/" + support) },
                            this.getClass().getClassLoader());
                }
            }

            ReflectionHelper.invoke(URLClassLoader.class.getName(), webapploader, "addURL", new Class<?>[] { URL.class },
                    new Object[] { new URL("file:" + loaderPath) }, this.getClass().getClassLoader());
        }
        catch (Exception e) {
            log.error("loader.addRepository fails, loaderPath=" + loaderPath, e);
        }
    }

    public Map<String, HookProxy> getHooks() {

        return this.hooks;
    }

    public HookProxy getHook(String hookName) {

        return this.hooks.get(hookName);
    }

    /**
     * runHook
     * 
     * @param context
     */
    public void runHook(HookContext context) {

        for (HookProxy proxy : this.hooks.values()) {
            if (proxy.isRun(context)) {
                try {
                    proxy.run(context);
                }
                catch (Exception e) {
                    log.error("RUN hook for proxy=" + proxy.getClass().getName() + "FAIL. ", e);
                }
            }
        }
    }
}
