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

import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.loader.WebappClassLoader;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.ReflectHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.interceptframework.InterceptSupport;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;
import com.creditease.tomcat.plus.util.TomcatLog;
import com.creditease.uav.util.MonitorServerUtil;

public class SpringBootTomcatPlusIT extends TomcatPlusIT {

    /**
     * startUAVServer
     */
    public void startServer(String port, String contextPath, Object arg) {

        if (!"AnnotationConfigEmbeddedWebApplicationContext".equals(arg.getClass().getSimpleName())) {
            return;
        }
        // integrate Tomcat log
        UAVServer.instance().setLog(new TomcatLog("MonitorServer"));
        // start Monitor Server when server starts
        UAVServer.instance().start(new Object[] { UAVServer.ServerVendor.SPRINGBOOT });
        // set appid
        setAppid(contextPath);
        // set the connector port
        UAVServer.instance().putServerInfo(CaptureConstants.INFO_APPSERVER_LISTEN_PORT,
                DataConvertHelper.toInt(port, 8080));
        InterceptSupport iSupport = InterceptSupport.instance();
        // this context will be transmited from springboot mainThread to webcontainerInit thread then back to mainThread
        iSupport.getThreadLocalContext(Event.WEBCONTAINER_STARTED);
    }

    /**
     * setAppid
     * 
     * @param contextPath
     */
    public void setAppid(String contextPath) {

        if (contextPath == null) {
            contextPath = "";
        }
        else if (contextPath.indexOf("/") == 0) {
            contextPath = contextPath.substring(1);
        }

        System.setProperty("com.creditease.uav.appid", MonitorServerUtil.getApplicationId(contextPath, ""));

    }

    /**
     * onAppStarting
     * 
     * @param args
     */
    @Override
    public void onAppStarting(Object... args) {

        StandardContext sc = (StandardContext) args[0];
        InterceptSupport iSupport = InterceptSupport.instance();
        InterceptContext context = iSupport.createInterceptContext(Event.WEBCONTAINER_INIT);
        /**
         * NOTE: spring boot rewrite the tomcat webappclassloader, makes the addURL for nothing, then we can't do
         * anything on this we may use its webappclassloader's parent as the classloader
         */
        context.put(InterceptConstants.WEBAPPLOADER, sc.getLoader().getClassLoader().getParent());
        context.put(InterceptConstants.WEBWORKDIR, sc.getWorkPath());

        String contextPath = (String) ReflectHelper.getField(StandardContext.class, sc, "encodedPath", true);
        context.put(InterceptConstants.CONTEXTPATH, contextPath);

        context.put(InterceptConstants.APPNAME, ReflectHelper.getField(StandardContext.class, sc, "displayName", true));

        ServletContext sContext = (ServletContext) ReflectHelper.getField(StandardContext.class, sc, "context", true);

        context.put(InterceptConstants.SERVLET_CONTEXT, sContext);

        String basePath = sContext.getRealPath("");

        /*
         * NOTE: springboot couldn't get the basePath through method "getRealPath", temporary process
         */
        if (basePath == null) {
            basePath = "";
        }
        else if (basePath.lastIndexOf("/") == (basePath.length() - 1)
                || basePath.lastIndexOf("\\") == (basePath.length() - 1)) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }

        context.put(InterceptConstants.BASEPATH, basePath);

        iSupport.doIntercept(context);
    }

    @Override
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

    @Override
    public void onResourceInit(Object... args) {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        if (!WebappClassLoader.class.isAssignableFrom(cl.getClass())) {
            return;
        }

        /**
         * for Application Starting's Resource Init
         */
        InterceptSupport iSupport = InterceptSupport.instance();
        InterceptContext context = iSupport.getThreadLocalContext(Event.WEBCONTAINER_RESOURCE_INIT);

        StandardContext sc = (StandardContext) context.get(InterceptConstants.CONTEXTOBJ);

        /**
         * NOTE: spring boot rewrite the tomcat webappclassloader, makes the addURL for nothing, then we can't do
         * anything on this we may use its webappclassloader's parent as the classloader
         */
        context.put(InterceptConstants.WEBAPPLOADER, sc.getLoader().getClassLoader().getParent());

        context.put(InterceptConstants.WEBWORKDIR, sc.getWorkPath());

        String contextPath = (String) ReflectHelper.getField(StandardContext.class, sc, "encodedPath", true);
        context.put(InterceptConstants.CONTEXTPATH, contextPath);

        context.put(InterceptConstants.APPNAME, ReflectHelper.getField(StandardContext.class, sc, "displayName", true));

        ServletContext sContext = (ServletContext) ReflectHelper.getField(StandardContext.class, sc, "context", true);

        context.put(InterceptConstants.SERVLET_CONTEXT, sContext);

        String basePath = sContext.getRealPath("");

        /*
         * NOTE: springboot couldn't get the basePath through method "getRealPath", temporary process
         */
        if (basePath == null) {
            basePath = "";
        }
        else if (basePath.lastIndexOf("/") == (basePath.length() - 1)
                || basePath.lastIndexOf("\\") == (basePath.length() - 1)) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }

        context.put(InterceptConstants.BASEPATH, basePath);

        iSupport.doIntercept(context);
    }

    @Override
    public Object onResourceCreate(Object... args) {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        if (!WebappClassLoader.class.isAssignableFrom(cl.getClass())) {
            return args[0];
        }

        /**
         * for Application Starting's Resource Create
         */
        InterceptSupport iSupport = InterceptSupport.instance();
        InterceptContext context = iSupport.getThreadLocalContext(Event.WEBCONTAINER_RESOURCE_CREATE);

        StandardContext sc = (StandardContext) context.get(InterceptConstants.CONTEXTOBJ);

        /**
         * NOTE: spring boot rewrite the tomcat webappclassloader, makes the addURL for nothing, then we can't do
         * anything on this we may use its webappclassloader's parent as the classloader
         */
        context.put(InterceptConstants.WEBAPPLOADER, sc.getLoader().getClassLoader().getParent());

        context.put(InterceptConstants.WEBWORKDIR, sc.getWorkPath());

        String contextPath = (String) ReflectHelper.getField(StandardContext.class, sc, "encodedPath", true);
        context.put(InterceptConstants.CONTEXTPATH, contextPath);

        context.put(InterceptConstants.APPNAME, ReflectHelper.getField(StandardContext.class, sc, "displayName", true));

        ServletContext sContext = (ServletContext) ReflectHelper.getField(StandardContext.class, sc, "context", true);

        context.put(InterceptConstants.SERVLET_CONTEXT, sContext);

        String basePath = sContext.getRealPath("");

        /*
         * NOTE: springboot couldn't get the basePath through method "getRealPath", temporary process
         */
        if (basePath == null) {
            basePath = "";
        }
        else if (basePath.lastIndexOf("/") == (basePath.length() - 1)
                || basePath.lastIndexOf("\\") == (basePath.length() - 1)) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }

        context.put(InterceptConstants.BASEPATH, basePath);

        context.put(InterceptConstants.RESOURCEOBJ, args[0]);
        context.put(InterceptConstants.RESOURCECFG, args[1]);

        iSupport.doIntercept(context);

        return context.get(InterceptConstants.RESOURCEOBJ);
    }

    /**
     * onAppStart
     * 
     * @param args
     */
    @Override
    public void onAppStart(Object... args) {

        StandardContext sc = (StandardContext) args[0];
        InterceptSupport iSupport = InterceptSupport.instance();
        InterceptContext context = iSupport.getThreadLocalContext(Event.WEBCONTAINER_STARTED);

        /**
         * NOTE: spring boot rewrite the tomcat webappclassloader, makes the addURL for nothing, then we can't do
         * anything on this we may use its webappclassloader's parent as the classloader
         */
        context.put(InterceptConstants.WEBAPPLOADER, sc.getLoader().getClassLoader().getParent());

        context.put(InterceptConstants.WEBWORKDIR, sc.getWorkPath());

        String contextPath = (String) ReflectHelper.getField(StandardContext.class, sc, "encodedPath", true);
        context.put(InterceptConstants.CONTEXTPATH, contextPath);

        context.put(InterceptConstants.APPNAME, ReflectHelper.getField(StandardContext.class, sc, "displayName", true));

        ServletContext sContext = (ServletContext) ReflectHelper.getField(StandardContext.class, sc, "context", true);

        context.put(InterceptConstants.SERVLET_CONTEXT, sContext);

        String basePath = sContext.getRealPath("");

        /*
         * NOTE: springboot couldn't get the basePath through method "getRealPath", temporary process
         */
        if (basePath == null) {
            basePath = "";
        }
        else if (basePath.lastIndexOf("/") == (basePath.length() - 1)
                || basePath.lastIndexOf("\\") == (basePath.length() - 1)) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }

        context.put(InterceptConstants.BASEPATH, basePath);
        // we don't doIntercept here cause some pre-profile(like dubbo) not happen yet, profile will be done after
        // finishRefresh

    }

    /**
     * onAppStop
     * 
     * @param args
     */
    @Override
    public void onAppStop(Object... args) {

        StandardContext sc = (StandardContext) args[0];
        InterceptSupport iSupport = InterceptSupport.instance();
        InterceptContext context = iSupport.createInterceptContext(Event.WEBCONTAINER_STOPPED);

        if (null == context || null == sc) {
            return;
        }

        /**
         * NOTE: spring boot will reset tomcat webappclassloader to null when shutdown, we may use the currentThread's
         * classloader as the classloader
         */
        context.put(InterceptConstants.WEBAPPLOADER, Thread.currentThread().getContextClassLoader());
        context.put(InterceptConstants.WEBWORKDIR, sc.getWorkPath());

        String contextPath = (String) ReflectHelper.getField(StandardContext.class, sc, "encodedPath", true);
        context.put(InterceptConstants.CONTEXTPATH, contextPath);

        context.put(InterceptConstants.APPNAME, ReflectHelper.getField(StandardContext.class, sc, "displayName", true));
        context.put(InterceptConstants.SERVLET_CONTEXT,
                ReflectHelper.getField(StandardContext.class, sc, "context", true));

        iSupport.doIntercept(context);
    }

    /**
     * onServletStart
     * 
     * @param args
     */
    @Override
    public void onServletStart(Object... args) {

        StandardWrapper sw = (StandardWrapper) args[0];
        Servlet servlet = (Servlet) args[1];

        InterceptSupport iSupport = InterceptSupport.instance();
        InterceptContext context = iSupport.createInterceptContext(Event.AFTER_SERVET_INIT);
        context.put(InterceptConstants.SERVLET_INSTANCE, servlet);
        /**
         * NOTE: spring boot rewrite the tomcat webappclassloader, makes the addURL for nothing, then we can't do
         * anything on this we may use its webappclassloader's parent as the classloader
         */
        context.put(InterceptConstants.WEBAPPLOADER, Thread.currentThread().getContextClassLoader().getParent());

        context.put(InterceptConstants.CONTEXTPATH, sw.getServletContext().getContextPath());

        iSupport.doIntercept(context);
    }

    /**
     * onServletStop
     * 
     * @param args
     */
    @Override
    public void onServletStop(Object... args) {

        StandardWrapper sw = (StandardWrapper) args[0];
        Servlet servlet = (Servlet) args[1];
        InterceptSupport iSupport = InterceptSupport.instance();
        InterceptContext context = iSupport.createInterceptContext(Event.BEFORE_SERVLET_DESTROY);
        context.put(InterceptConstants.SERVLET_INSTANCE, servlet);
        /**
         * NOTE: spring boot rewrite the tomcat webappclassloader, makes the addURL for nothing, then we can't do
         * anything on this we may use its webappclassloader's parent as the classloader
         */
        context.put(InterceptConstants.WEBAPPLOADER, Thread.currentThread().getContextClassLoader().getParent());

        context.put(InterceptConstants.CONTEXTPATH, sw.getServletContext().getContextPath());
        iSupport.doIntercept(context);
    }

    /**
     * springboot load beans before web container start, hook opr should be done before beanRegist in case of duplicate
     * definition ,so we define SPRING_BEAN_REGIST event to trigger hook
     */
    public void onSpringBeanRegist(String contextPath) {

        if (contextPath == null) {
            contextPath = "";
        }
        InterceptSupport iSupport = InterceptSupport.instance();
        InterceptContext context = iSupport.createInterceptContext(Event.SPRING_BEAN_REGIST);
        context.put(InterceptConstants.WEBAPPLOADER, Thread.currentThread().getContextClassLoader());
        context.put(InterceptConstants.CONTEXTPATH, contextPath);
        context.put(InterceptConstants.BASEPATH, "");
        iSupport.doIntercept(context);
    }

    /**
     * ComponentProfile will be done after springboot finish it's context's refresh, cause every pre-profile(like dubbo)
     * is ready.
     * 
     */
    public void onSpringFinishRefresh() {

        InterceptSupport iSupport = InterceptSupport.instance();
        InterceptContext context = iSupport.getThreadLocalContext(Event.WEBCONTAINER_STARTED);

        iSupport.doIntercept(context);
    }
}
