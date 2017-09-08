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

package com.creditease.uav.jetty.plus.interceptor;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.DispatcherType;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;

import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.providers.ScanningAppProvider;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import com.creditease.agent.helpers.ReflectHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.monitor.interceptframework.InterceptSupport;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;
import com.creditease.monitor.jee.servlet30.DynamicServletContextProcessor;
import com.creditease.monitor.proxy.spi.JDKProxyInvokeHandler;
import com.creditease.uav.jetty.plus.util.JettyLog;
import com.creditease.uav.util.JDKProxyInvokeUtil;

public class JettyPlusIT {

    /**
     * startUAVServer
     */
    public void startServer(Object... args) {

        Server server = (Server) args[0];

        // integrate Tomcat log
        UAVServer.instance().setLog(new JettyLog("MonitorServer"));
        // start Monitor Server when server starts
        UAVServer.instance().start(new Object[] { UAVServer.ServerVendor.JETTY });

        // get server port
        if (UAVServer.instance().getServerInfo(CaptureConstants.INFO_APPSERVER_LISTEN_PORT) == null) {
            // set port
            ServerConnector sc = (ServerConnector) server.getConnectors()[0];

            String protocol = sc.getDefaultProtocol();
            if (protocol.toLowerCase().indexOf("http") >= 0) {
                UAVServer.instance().putServerInfo(CaptureConstants.INFO_APPSERVER_LISTEN_PORT, sc.getPort());
            }
        }

    }

    /**
     * onAppInit
     * 
     * @param args
     */
    public void onAppInit(Object... args) {

        App sc = (App) args[0];
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
     * onAppStarting
     * 
     * @param args
     */
    public void onAppStarting(Object... args) {

        WebAppContext sc = (WebAppContext) args[0];

        InterceptSupport iSupport = InterceptSupport.instance();
        InterceptContext context = iSupport.createInterceptContext(Event.WEBCONTAINER_INIT);
        context.put(InterceptConstants.WEBAPPLOADER, sc.getClassLoader());
        context.put(InterceptConstants.WEBWORKDIR, sc.getServletContext().getRealPath(""));
        context.put(InterceptConstants.CONTEXTPATH, sc.getContextPath());
        context.put(InterceptConstants.APPNAME, sc.getDisplayName());

        ServletContext sContext = sc.getServletContext();

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

        WebAppContext sc = getWebAppContext(args);

        if (sc == null) {
            return;
        }

        InterceptSupport iSupport = InterceptSupport.instance();
        InterceptContext context = iSupport.createInterceptContext(Event.WEBCONTAINER_STARTED);
        context.put(InterceptConstants.WEBAPPLOADER, sc.getClassLoader());
        context.put(InterceptConstants.WEBWORKDIR, sc.getServletContext().getRealPath(""));
        context.put(InterceptConstants.CONTEXTPATH, sc.getContextPath());
        context.put(InterceptConstants.APPNAME, sc.getDisplayName());

        ServletContext sContext = sc.getServletContext();

        context.put(InterceptConstants.SERVLET_CONTEXT, sContext);

        getBasePath(context, sContext);

        iSupport.doIntercept(context);

        // GlobalFilter
        sc.addFilter("com.creditease.monitor.jee.filters.GlobalFilter", "/*", EnumSet.of(DispatcherType.REQUEST));
    }

    /**
     * onAppStop
     * 
     * @param args
     */
    public void onAppStop(Object... args) {

        System.out.println("---------------->onAppStop");

        WebAppContext sc = getWebAppContext(args);

        if (sc == null) {
            return;
        }

        InterceptSupport iSupport = InterceptSupport.instance();
        InterceptContext context = iSupport.createInterceptContext(Event.WEBCONTAINER_STOPPED);
        context.put(InterceptConstants.WEBAPPLOADER, sc.getClassLoader());
        context.put(InterceptConstants.WEBWORKDIR, sc.getServletContext().getRealPath(""));
        context.put(InterceptConstants.CONTEXTPATH, sc.getContextPath());
        context.put(InterceptConstants.APPNAME, sc.getDisplayName());

        ServletContext sContext = sc.getServletContext();

        context.put(InterceptConstants.SERVLET_CONTEXT, sContext);

        getBasePath(context, sContext);

        iSupport.doIntercept(context);
    }

    /**
     * on Deploy UAV Application
     * 
     * @param args
     */

    public void onDeployUAVApp(Object... args) {

        if (System.getProperty("com.creditease.uav.iapp.install") != null) {
            return;
        }

        ScanningAppProvider sap = (ScanningAppProvider) args[0];
        String mofRoot = (String) args[1];

        ReflectHelper.invoke(sap.getClass().getName(), sap, "fileAdded", new Class<?>[] { String.class },
                new Object[] { mofRoot + "/com.creditease.uav" }, this.getClass().getClassLoader());

        System.setProperty("com.creditease.uav.iapp.install", "true");
    }

    /**
     * on Resource Init
     * 
     * @param args
     */
    public void onResourceInit(Object... args) {

    }

    /**
     * on Resource Create
     */
    public Object onResourceCreate(Object... args) {

        return args;

    }

    /**
     * onServiceInit
     * 
     * @param args
     */
    public void onServiceInit(Object... args) {

        // DO Nothing as startServer is done
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
    public void onServiceEnd(Object... args) {

        final Request request = (Request) args[0];
        final Response response = (Response) args[1];

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
     * 
     * onServletRegist
     * 
     * @param args
     * @return
     */
    public ServletContext onServletRegist(Object... args) {

        ServletContext servletContext;
        ServletContextEvent sce;
        if (ServletContextEvent.class.isAssignableFrom(args[0].getClass())) {
            sce = (ServletContextEvent) args[0];
            servletContext = sce.getServletContext();
        }
        else {
            servletContext = (ServletContext) args[0];
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
     * onWrapServletContext Jetty的场景下需要采用一些Wrapper来替代Proxy，因为不全是接口
     * 
     * @param args
     */
    public ContextHandler.Context onWrapServletContext(Object... args) {

        ServletContextHandler.Context oContext = (ServletContextHandler.Context) args[0];
        ServletContextHandlerWrapper.JettyContextWrapper ctx = new ServletContextHandlerWrapper().new JettyContextWrapper(
                oContext);
        return ctx;
    }

    /**
     * onServletStart
     * 
     * @param args
     */
    public void onServletStart(Object... args) {

        ServletHolder sh = (ServletHolder) args[0];
        Servlet servlet;
        try {
            servlet = sh.getServlet();
        }
        catch (ServletException e) {
            // ignore
            return;
        }

        InterceptSupport iSupport = InterceptSupport.instance();
        InterceptContext context = iSupport.createInterceptContext(Event.AFTER_SERVET_INIT);
        context.put(InterceptConstants.SERVLET_INSTANCE, servlet);
        context.put(InterceptConstants.WEBAPPLOADER, Thread.currentThread().getContextClassLoader());

        context.put(InterceptConstants.CONTEXTPATH, sh.getContextPath());

        iSupport.doIntercept(context);
    }

    /**
     * onServletStop
     * 
     * @param args
     */
    public void onServletStop(Object... args) {

        ServletHolder sh = (ServletHolder) args[0];
        Servlet servlet;
        try {
            servlet = sh.getServlet();
        }
        catch (ServletException e) {
            // ignore
            return;
        }

        InterceptSupport iSupport = InterceptSupport.instance();
        InterceptContext context = iSupport.createInterceptContext(Event.BEFORE_SERVLET_DESTROY);
        context.put(InterceptConstants.SERVLET_INSTANCE, servlet);
        context.put(InterceptConstants.WEBAPPLOADER, Thread.currentThread().getContextClassLoader());
        context.put(InterceptConstants.CONTEXTPATH, sh.getContextPath());
        iSupport.doIntercept(context);
    }

    /**
     * getBasePath
     * 
     * @param context
     * @param sContext
     */
    private void getBasePath(InterceptContext context, ServletContext sContext) {

        String basePath = sContext.getRealPath("");

        if (basePath == null) {
            return;
        }

        if (basePath.lastIndexOf("/") == (basePath.length() - 1)
                || basePath.lastIndexOf("\\") == (basePath.length() - 1)) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }

        context.put(InterceptConstants.BASEPATH, basePath);
    }

    /**
     * getWebAppContext
     * 
     * @param args
     * @return
     */
    private WebAppContext getWebAppContext(Object... args) {

        App app = (App) args[0];
        WebAppContext sc = null;

        try {
            sc = (WebAppContext) app.getContextHandler();
        }
        catch (Exception e) {
            // ignore
        }
        return sc;
    }

}
