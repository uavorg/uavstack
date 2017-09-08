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

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.creditease.monitor.jee.servlet30.DynamicServletContextProcessor;

public class ServletContextHandlerWrapper extends ServletContextHandler {

    /**
     * 
     * JettyContextWrapper description:
     * 由于Jetty实现ServletContext的类是内部类，而且Jetty可能自己使用ServletContextHandler.Context，所以不能简单的加一个ServletContext的Proxy，
     * 这里采用了装饰器模式，加个Wrapper，因为我们只需关于addFilter,addServlet,addListener
     *
     */
    public class JettyContextWrapper extends ServletContextHandler.Context {

        private DynamicServletContextProcessor proc;

        private ServletContextHandler.Context context;

        public JettyContextWrapper(ServletContextHandler.Context context) {

            proc = new DynamicServletContextProcessor();

            this.context = context;
        }

        @Override
        public Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {

            Dynamic d = context.addFilter(filterName, filterClass);

            proc.collectDynInfo("addFilter", d, null);

            return d;
        }

        @Override
        public Dynamic addFilter(String filterName, Filter filter) {

            Dynamic d = context.addFilter(filterName, filter);

            proc.collectDynInfo("addFilter", d, null);

            return d;
        }

        @Override
        public Dynamic addFilter(String filterName, String className) {

            Dynamic d = context.addFilter(filterName, className);

            proc.collectDynInfo("addFilter", d, null);

            return d;
        }

        @Override
        public void addListener(Class<? extends EventListener> listenerClass) {

            context.addListener(listenerClass);

            proc.collectDynInfo("addListener", null, new Object[] { listenerClass.getName() });
        }

        @Override
        public void addListener(String className) {

            context.addListener(className);

            proc.collectDynInfo("addListener", null, new Object[] { className });
        }

        @Override
        public <T extends EventListener> void addListener(T t) {

            context.addListener(t);

            proc.collectDynInfo("addListener", null, new Object[] { t.getClass().getName() });
        }

        @Override
        public javax.servlet.ServletRegistration.Dynamic addServlet(String servletName,
                Class<? extends Servlet> servletClass) {

            javax.servlet.ServletRegistration.Dynamic d = context.addServlet(servletName, servletClass);

            proc.collectDynInfo("addServlet", d, null);

            return d;
        }

        @Override
        public javax.servlet.ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {

            javax.servlet.ServletRegistration.Dynamic d = context.addServlet(servletName, servlet);

            proc.collectDynInfo("addServlet", d, null);

            return d;
        }

        @Override
        public javax.servlet.ServletRegistration.Dynamic addServlet(String servletName, String className) {

            javax.servlet.ServletRegistration.Dynamic d = context.addServlet(servletName, className);

            proc.collectDynInfo("addServlet", d, null);

            return d;
        }

        // --------------------------------All Replace Methods--------------------------------------

        @Override
        public RequestDispatcher getNamedDispatcher(String name) {

            return context.getNamedDispatcher(name);
        }

        @Override
        public boolean setInitParameter(String name, String value) {

            return context.setInitParameter(name, value);
        }

        @Override
        public <T extends Filter> T createFilter(Class<T> c) throws ServletException {

            return context.createFilter(c);
        }

        @Override
        public <T extends Servlet> T createServlet(Class<T> c) throws ServletException {

            return context.createServlet(c);
        }

        @Override
        public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {

            return context.getDefaultSessionTrackingModes();
        }

        @Override
        public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {

            return context.getEffectiveSessionTrackingModes();
        }

        @Override
        public FilterRegistration getFilterRegistration(String filterName) {

            return context.getFilterRegistration(filterName);
        }

        @Override
        public Map<String, ? extends FilterRegistration> getFilterRegistrations() {

            return context.getFilterRegistrations();
        }

        @Override
        public ServletRegistration getServletRegistration(String servletName) {

            return context.getServletRegistration(servletName);
        }

        @Override
        public Map<String, ? extends ServletRegistration> getServletRegistrations() {

            return context.getServletRegistrations();
        }

        @Override
        public SessionCookieConfig getSessionCookieConfig() {

            return context.getSessionCookieConfig();
        }

        @Override
        public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {

            context.setSessionTrackingModes(sessionTrackingModes);
        }

        @Override
        public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {

            return context.createListener(clazz);
        }

        @Override
        public JspConfigDescriptor getJspConfigDescriptor() {

            return context.getJspConfigDescriptor();
        }

        @Override
        public void setJspConfigDescriptor(JspConfigDescriptor d) {

            context.setJspConfigDescriptor(d);
        }

        @Override
        public void declareRoles(String... roleNames) {

            context.declareRoles(roleNames);
        }

        @Override
        public void checkListener(Class<? extends EventListener> arg0) throws IllegalStateException {

            context.checkListener(arg0);
        }

        @Override
        public <T> T createInstance(Class<T> clazz) throws Exception {

            return context.createInstance(clazz);
        }

        @Override
        public synchronized Object getAttribute(String name) {

            return context.getAttribute(name);
        }

        @Override
        public synchronized Enumeration<String> getAttributeNames() {

            return context.getAttributeNames();
        }

        @Override
        public ClassLoader getClassLoader() {

            return context.getClassLoader();
        }

        @Override
        public ServletContext getContext(String arg0) {

            return context.getContext(arg0);
        }

        @Override
        public ContextHandler getContextHandler() {

            return context.getContextHandler();
        }

        @Override
        public String getContextPath() {

            return context.getContextPath();
        }

        @Override
        public String getInitParameter(String name) {

            return context.getInitParameter(name);
        }

        @Override
        public Enumeration<String> getInitParameterNames() {

            return context.getInitParameterNames();
        }

        @Override
        public String getMimeType(String file) {

            return context.getMimeType(file);
        }

        @Override
        public String getRealPath(String arg0) {

            return context.getRealPath(arg0);
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String arg0) {

            return context.getRequestDispatcher(arg0);
        }

        @Override
        public URL getResource(String path) throws MalformedURLException {

            return context.getResource(path);
        }

        @Override
        public InputStream getResourceAsStream(String arg0) {

            return context.getResourceAsStream(arg0);
        }

        @Override
        public Set<String> getResourcePaths(String path) {

            return context.getResourcePaths(path);
        }

        @Override
        public String getServletContextName() {

            return context.getServletContextName();
        }

        @Override
        public String getVirtualServerName() {

            return context.getVirtualServerName();
        }

        @Override
        public boolean isEnabled() {

            return context.isEnabled();
        }

        @Override
        public boolean isExtendedListenerTypes() {

            return context.isExtendedListenerTypes();
        }

        @Override
        public void log(Exception exception, String msg) {

            context.log(exception, msg);
        }

        @Override
        public void log(String message, Throwable throwable) {

            context.log(message, throwable);
        }

        @Override
        public void log(String msg) {

            context.log(msg);
        }

        @Override
        public synchronized void removeAttribute(String arg0) {

            context.removeAttribute(arg0);
        }

        @Override
        public synchronized void setAttribute(String arg0, Object arg1) {

            context.setAttribute(arg0, arg1);
        }

        @Override
        public void setEnabled(boolean enabled) {

            context.setEnabled(enabled);
        }

        @Override
        public void setExtendedListenerTypes(boolean extended) {

            context.setExtendedListenerTypes(extended);
        }

        @Override
        public String toString() {

            return context.toString();
        }

        @Override
        public int getEffectiveMajorVersion() {

            return context.getEffectiveMajorVersion();
        }

        @Override
        public int getEffectiveMinorVersion() {

            return context.getEffectiveMinorVersion();
        }

        @Override
        public int getMajorVersion() {

            return context.getMajorVersion();
        }

        @Override
        public int getMinorVersion() {

            return context.getMinorVersion();
        }

        @Override
        public String getServerInfo() {

            return context.getServerInfo();
        }

        @Override
        public Servlet getServlet(String name) throws ServletException {

            return context.getServlet(name);
        }

        @Override
        public Enumeration<String> getServletNames() {

            return context.getServletNames();
        }

        @Override
        public Enumeration<Servlet> getServlets() {

            return context.getServlets();
        }

        @Override
        public void setEffectiveMajorVersion(int v) {

            context.setEffectiveMajorVersion(v);
        }

        @Override
        public void setEffectiveMinorVersion(int v) {

            context.setEffectiveMinorVersion(v);
        }
    }
}
