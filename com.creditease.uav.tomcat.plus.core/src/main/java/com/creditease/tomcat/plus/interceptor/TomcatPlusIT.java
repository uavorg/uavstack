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

package com.creditease.tomcat.plus.interceptor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.apache.catalina.Host;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.loader.WebappClassLoader;
import org.apache.catalina.startup.HostConfig;
import org.apache.catalina.util.ContextName;

import com.creditease.agent.helpers.ReflectionHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.monitor.interceptframework.InterceptSupport;
import com.creditease.monitor.interceptframework.StandardInterceptContextHelper;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;
import com.creditease.monitor.jee.servlet30.DynamicServletContextProcessor;
import com.creditease.monitor.proxy.spi.JDKProxyInvokeHandler;
import com.creditease.tomcat.plus.util.TomcatLog;
import com.creditease.uav.util.JDKProxyInvokeUtil;

public class TomcatPlusIT {

    /**
     * startUAVServer
     */
    public void startServer() {

        // integrate Tomcat log
        UAVServer.instance().setLog(new TomcatLog("MonitorServer"));
        // start Monitor Server when server starts
        UAVServer.instance().start(new Object[] { UAVServer.ServerVendor.TOMCAT });
    }

    /**
     * onServiceInit
     * 
     * @param args
     */
    public void onServiceInit(Object... args) {

        // get server port
        if (UAVServer.instance().getServerInfo(CaptureConstants.INFO_APPSERVER_LISTEN_PORT) == null) {
            Connector connector = (Connector) args[0];

            String protocol = connector.getProtocol();

            if (protocol.toLowerCase().indexOf("http") >= 0) {
                UAVServer.instance().putServerInfo(CaptureConstants.INFO_APPSERVER_LISTEN_PORT, connector.getPort());
            }
        }

    }

    /**
     * onServiceStart
     * 
     * @param ca
     */
    public void onServiceStart(Object... args) {

        // on service start pre-cap
        UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_SERVER_CONNECTOR,
                Monitor.CapturePhase.PRECAP, null);
    }

    /**
     * onServiceEnd
     * 
     * @param args
     */
    @SuppressWarnings("deprecation")
    public void onServiceEnd(Object... args) {

        Request request = (Request) args[0];
        Response response = (Response) args[1];

        Map<String, Object> params = new HashMap<String, Object>();
        try {
            params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_REQUEST_URL, request.getRequestURL().toString());
        }
        catch (Exception e) {
        }
        try {
            params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_SERVLET, request.getServletPath());
        }
        catch (Exception e) {
        }
        try {
            params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_CONTEXT, request.getContextPath());
        }
        catch (Exception e) {
        }
        try {
            params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_CONTEXT_REALPATH,
                    request.getServletContext().getRealPath(""));
        }
        catch (NoSuchMethodError er) {
            params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_CONTEXT_REALPATH, request.getRealPath(""));
        }
        catch (Exception e) {
        }
        try {
            params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_RESPONSECODE, response.getStatus());
        }
        catch (Exception e) {
        }
        try {
            params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_FORWARDADDR, request.getHeader("X-Forwarded-For"));
        }
        catch (Exception e) {
        }
        try {
            params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_LISTENPORT, request.getLocalPort());
        }
        catch (Exception e) {
        }
        try {
            params.put(CaptureConstants.INFO_APPSERVER_CLIENT_USRAGENT, request.getHeader("User-Agent"));
        }
        catch (Exception e) {
        }
        try {
            params.put(CaptureConstants.INFO_APPSERVER_UAVCLIENT_TAG, request.getHeader("UAV-Client-Src"));
        }
        catch (Exception e) {
        }
        try {
            params.put(CaptureConstants.INFO_APPSERVER_PROXY_HOST, request.getHeader("Host"));
        }
        catch (Exception e) {
        }
        try {
            params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_CLIENTADDR, request.getRemoteAddr());
        }
        catch (Exception e) {
        }

        UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_SERVER_CONNECTOR,
                Monitor.CapturePhase.DOCAP, params);

    }

    /**
     * onAppInit
     * 
     * @param args
     */
    public void onAppInit(Object... args) {

        StandardContext sc = (StandardContext) args[0];
        InterceptSupport iSupport = InterceptSupport.instance();
        InterceptContext ic = iSupport.getThreadLocalContext(Event.WEBCONTAINER_RESOURCE_INIT);
        InterceptContext ic2 = iSupport.getThreadLocalContext(Event.WEBCONTAINER_RESOURCE_CREATE);
        /**
         * NOTE: onAppInit, we put the Context Object into threadlocal, then all other later process for
         * PRE_WEBCONTAINER_INIT, which can get the object, as not everywhere we can get the object
         * 
         * for example, the DataSource related injection
         */
        ic.put(InterceptConstants.CONTEXTOBJ, sc);
        ic2.put(InterceptConstants.CONTEXTOBJ, sc);
    }

    /**
     * on Resource Init
     * 
     * @param args
     */
    public void onResourceInit(Object... args) {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        try {
            /**
             * after tomcat8, tomcat use ParallelWebappClassLoader instead of WebappClassLoader as it's webapp's
             * classloader, both of them are extends WebappClassLoaderBase
             */
            Class<?> cls = cl.loadClass("org.apache.catalina.loader.WebappClassLoaderBase");
            if (!cls.isAssignableFrom(cl.getClass())) {
                return;
            }
        }
        catch (ClassNotFoundException e) {
            /**
             * before tomcat7.0.64(include), WebappClassLoaderBase doesn't exist
             */
            if (!WebappClassLoader.class.isAssignableFrom(cl.getClass())) {
                return;
            }
        }

        /**
         * for Application Starting's Resource Init
         */
        InterceptSupport iSupport = InterceptSupport.instance();
        InterceptContext context = iSupport.getThreadLocalContext(Event.WEBCONTAINER_RESOURCE_INIT);
        if (context == null) {
            return;
        }

        StandardContext sc = (StandardContext) context.get(InterceptConstants.CONTEXTOBJ);

        if (sc == null) {
            return;
        }

        context.put(InterceptConstants.WEBAPPLOADER, sc.getLoader().getClassLoader());

        context.put(InterceptConstants.WEBWORKDIR, sc.getWorkPath());
        context.put(InterceptConstants.CONTEXTPATH,
                ReflectionHelper.getField(StandardContext.class, sc, "encodedPath", true));
        context.put(InterceptConstants.APPNAME, ReflectionHelper.getField(StandardContext.class, sc, "displayName", true));

        ServletContext sContext = (ServletContext) ReflectionHelper.getField(StandardContext.class, sc, "context", true);

        context.put(InterceptConstants.SERVLET_CONTEXT, sContext);

        getBasePath(context, sContext);

        iSupport.doIntercept(context);

        InterceptContext ic = iSupport.getThreadLocalContext(Event.WEBCONTAINER_RESOURCE_INIT);

        ic.put(InterceptConstants.CONTEXTOBJ, sc);
    }

    /**
     * on Resource Create
     */
    public Object onResourceCreate(Object... args) {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            /**
             * after tomcat8, tomcat use ParallelWebappClassLoader instead of WebappClassLoader as it's webapp's
             * classloader, both of them are extends WebappClassLoaderBase
             */
            Class<?> cls = cl.loadClass("org.apache.catalina.loader.WebappClassLoaderBase");
            if (!cls.isAssignableFrom(cl.getClass())) {
                return args[0];
            }
        }
        catch (ClassNotFoundException e) {
            /**
             * before tomcat7.0.64(include), WebappClassLoaderBase doesn't exist
             */
            if (!WebappClassLoader.class.isAssignableFrom(cl.getClass())) {
                return args[0];
            }
        }

        /**
         * for Application Starting's Resource Create
         */
        InterceptSupport iSupport = InterceptSupport.instance();
        InterceptContext context = iSupport.getThreadLocalContext(Event.WEBCONTAINER_RESOURCE_CREATE, false);
        if (context == null) {
            return args[0];
        }

        StandardContext sc = (StandardContext) context.get(InterceptConstants.CONTEXTOBJ);

        if (sc == null) {
            return args[0];
        }

        context.put(InterceptConstants.WEBAPPLOADER, sc.getLoader().getClassLoader());

        context.put(InterceptConstants.WEBWORKDIR, sc.getWorkPath());
        context.put(InterceptConstants.CONTEXTPATH,
                ReflectionHelper.getField(StandardContext.class, sc, "encodedPath", true));
        context.put(InterceptConstants.APPNAME, ReflectionHelper.getField(StandardContext.class, sc, "displayName", true));

        ServletContext sContext = (ServletContext) ReflectionHelper.getField(StandardContext.class, sc, "context", true);

        context.put(InterceptConstants.SERVLET_CONTEXT, sContext);

        getBasePath(context, sContext);

        context.put(InterceptConstants.RESOURCEOBJ, args[0]);
        context.put(InterceptConstants.RESOURCECFG, args[1]);

        iSupport.doIntercept(context);

        InterceptContext ic = iSupport.getThreadLocalContext(Event.WEBCONTAINER_RESOURCE_CREATE);

        ic.put(InterceptConstants.CONTEXTOBJ, sc);

        return context.get(InterceptConstants.RESOURCEOBJ);
    }

    /**
     * onAppStarting
     * 
     * @param args
     */
    public void onAppStarting(Object... args) {

        // release the left contexts
        StandardInterceptContextHelper.releaseContext(Event.WEBCONTAINER_RESOURCE_INIT);
        StandardInterceptContextHelper.releaseContext(Event.WEBCONTAINER_RESOURCE_CREATE);

        StandardContext sc = (StandardContext) args[0];
        InterceptSupport iSupport = InterceptSupport.instance();
        InterceptContext context = iSupport.createInterceptContext(Event.WEBCONTAINER_INIT);
        context.put(InterceptConstants.WEBAPPLOADER, sc.getLoader().getClassLoader());
        context.put(InterceptConstants.WEBWORKDIR, sc.getWorkPath());
        context.put(InterceptConstants.CONTEXTPATH,
                ReflectionHelper.getField(StandardContext.class, sc, "encodedPath", true));
        context.put(InterceptConstants.APPNAME, ReflectionHelper.getField(StandardContext.class, sc, "displayName", true));

        ServletContext sContext = (ServletContext) ReflectionHelper.getField(StandardContext.class, sc, "context", true);

        context.put(InterceptConstants.SERVLET_CONTEXT, sContext);

        getBasePath(context, sContext);

        iSupport.doIntercept(context);
    }

    /**
     * onAppStart
     * 
     * @param args
     */
    public void onAppStart(Object... args) {

        StandardContext sc = (StandardContext) args[0];
        InterceptSupport iSupport = InterceptSupport.instance();
        InterceptContext context = iSupport.createInterceptContext(Event.WEBCONTAINER_STARTED);
        context.put(InterceptConstants.WEBAPPLOADER, sc.getLoader().getClassLoader());
        context.put(InterceptConstants.WEBWORKDIR, sc.getWorkPath());
        context.put(InterceptConstants.CONTEXTPATH,
                ReflectionHelper.getField(StandardContext.class, sc, "encodedPath", true));
        context.put(InterceptConstants.APPNAME, ReflectionHelper.getField(StandardContext.class, sc, "displayName", true));

        ServletContext sContext = (ServletContext) ReflectionHelper.getField(StandardContext.class, sc, "context", true);

        context.put(InterceptConstants.SERVLET_CONTEXT, sContext);

        getBasePath(context, sContext);

        iSupport.doIntercept(context);
    }

    private void getBasePath(InterceptContext context, ServletContext sContext) {

        String basePath = sContext.getRealPath("");

        if (basePath.lastIndexOf("/") == (basePath.length() - 1)
                || basePath.lastIndexOf("\\") == (basePath.length() - 1)) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }

        context.put(InterceptConstants.BASEPATH, basePath);
    }

    /**
     * onAppStop
     * 
     * @param args
     */
    public void onAppStop(Object... args) {

        StandardContext sc = (StandardContext) args[0];
        InterceptSupport iSupport = InterceptSupport.instance();
        InterceptContext context = iSupport.createInterceptContext(Event.WEBCONTAINER_STOPPED);
        context.put(InterceptConstants.WEBAPPLOADER, sc.getLoader().getClassLoader());
        context.put(InterceptConstants.WEBWORKDIR, sc.getWorkPath());
        context.put(InterceptConstants.CONTEXTPATH,
                ReflectionHelper.getField(StandardContext.class, sc, "encodedPath", true));
        context.put(InterceptConstants.APPNAME, ReflectionHelper.getField(StandardContext.class, sc, "displayName", true));

        ServletContext sContext = (ServletContext) ReflectionHelper.getField(StandardContext.class, sc, "context", true);

        context.put(InterceptConstants.SERVLET_CONTEXT, sContext);

        getBasePath(context, sContext);

        iSupport.doIntercept(context);
    }

    /**
     * onServletStart
     * 
     * @param args
     */
    public void onServletStart(Object... args) {

        StandardWrapper sw = (StandardWrapper) args[0];
        Servlet servlet = (Servlet) args[1];

        InterceptSupport iSupport = InterceptSupport.instance();
        InterceptContext context = iSupport.createInterceptContext(Event.AFTER_SERVET_INIT);
        context.put(InterceptConstants.SERVLET_INSTANCE, servlet);
        context.put(InterceptConstants.WEBAPPLOADER, Thread.currentThread().getContextClassLoader());

        context.put(InterceptConstants.CONTEXTPATH, sw.getServletContext().getContextPath());

        iSupport.doIntercept(context);
    }

    /**
     * 
     * onServletRegist
     * 
     * @param args
     * @return
     */
    public ServletContext onServletRegist(Object... args) {

        ServletContext servletContext = (ServletContext) args[0];
        
        //uav's inner app doesn't need Profiling,just return origin servletContext here. 
        if("/com.creditease.uav".equals(servletContext.getContextPath())) {
            return servletContext;
        }
          
        ServletContext scProxy = (ServletContext) servletContext
                .getAttribute("com.creditease.uav.mof.tomcat.servletcontext");

        if (scProxy == null) {
            scProxy = JDKProxyInvokeUtil.newProxyInstance(ServletContext.class.getClassLoader(),
                    new Class<?>[] { ServletContext.class },
                    new JDKProxyInvokeHandler<ServletContext>(servletContext, new DynamicServletContextProcessor()));

            servletContext.setAttribute("com.creditease.uav.mof.tomcat.servletcontext", scProxy);
        }

        return scProxy;
    }

    /**
     * onServletStop
     * 
     * @param args
     */
    public void onServletStop(Object... args) {

        StandardWrapper sw = (StandardWrapper) args[0];
        Servlet servlet = (Servlet) args[1];
        InterceptSupport iSupport = InterceptSupport.instance();
        InterceptContext context = iSupport.createInterceptContext(Event.BEFORE_SERVLET_DESTROY);
        context.put(InterceptConstants.SERVLET_INSTANCE, servlet);
        context.put(InterceptConstants.WEBAPPLOADER, Thread.currentThread().getContextClassLoader());
        context.put(InterceptConstants.CONTEXTPATH, sw.getServletContext().getContextPath());
        iSupport.doIntercept(context);
    }

    /**
     * on Deploy UAV Application
     * 
     * @param args
     */
    @SuppressWarnings("unused")
    public void onDeployUAVApp(Object... args) {

        if (System.getProperty("com.creditease.uav.iapp.install") != null) {
            return;
        }

        final HostConfig hc = (HostConfig) args[0];
        Host host = (Host) args[1];
        final File appBase = (File) args[2];
        final String mofRoot = (String) args[3];
        String curVersion = (String) args[4];
        String currentVersionDetailed = (String) args[5];
        String[] versions = currentVersionDetailed.split("\\.");

        int action = 0;

        if (curVersion.equals("6")) {
            /**
             * tomcat6
             */
            action = 0;
        }
        else if (curVersion.equals("7") && versions[1].equals("0")
                && (Integer.parseInt(versions[2].substring(0, 1)) < 3)) {
            /**
             * tomcat7.并且小版本0.30.0 (不包含)以下。小版本号使用substring是因为存在beta版本，取第一位即可。
             */
            action = 0;
        }
        else {
            action = 1;
        }

        switch (action) {
            case 0:
                File dir = new File(mofRoot + "/com.creditease.uav");

                ReflectionHelper.invoke("org.apache.catalina.startup.HostConfig", hc, "deployDirectory",
                        new Class<?>[] { String.class, File.class, String.class },
                        new Object[] { "/com.creditease.uav", dir, mofRoot + "/com.creditease.uav" },
                        hc.getClass().getClassLoader());
                break;
            case 1:
                ExecutorService es = host.getStartStopExecutor();

                Future<?> f = es.submit(new Runnable() {

                    @Override
                    public void run() {

                        ContextName cn = new ContextName("com.creditease.uav", "");

                        ReflectionHelper.setField(ContextName.class, cn, "baseName", mofRoot + "/com.creditease.uav");

                        File dir = new File(mofRoot + "/com.creditease.uav");

                        ReflectionHelper.invoke("org.apache.catalina.startup.HostConfig", hc, "deployDirectory",
                                new Class<?>[] { ContextName.class, File.class }, new Object[] { cn, dir },
                                hc.getClass().getClassLoader());

                    }

                });

                try {
                    f.get();
                }
                catch (Exception e) {
                    // ignore
                }
                break;
        }

        System.setProperty("com.creditease.uav.iapp.install", "true");
    }
    
    /**
     *  when use embeddedTomcat and the webappclassloader is undefined, the webappclassloader will use systemclassloader as it's parentclassloader which coundn't load mof jars.
     *  we chg it's parentclassloader to currentThread's contextclassloader(normally it counld load uavmof jars).
     */
    public ClassLoader chgParentClassloader(Object... args) {

        ClassLoader cl = (ClassLoader) args[0];

        StandardContext sc = (StandardContext) args[1];
        if (cl != ClassLoader.getSystemClassLoader()) {
            return cl;
        }
        else {
            sc.setDelegate(true);
            return Thread.currentThread().getContextClassLoader();
        }
    }
}
