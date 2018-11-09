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

package com.creditease.monitor.jee.servlet30;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import com.creditease.monitor.interceptframework.InterceptSupport;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;
import com.creditease.monitor.proxy.spi.JDKProxyInvokeProcessor;

/**
 * 
 * DynamicServletContextProcessor description: helps to figure out the dynamic servlets, filters, listeners
 *
 */
public class DynamicServletContextProcessor extends JDKProxyInvokeProcessor<ServletContext> {

    private InterceptContext context = InterceptSupport.instance().getThreadLocalContext(Event.WEBCONTAINER_STARTED);

    private List<FilterRegistration.Dynamic> filters = new LinkedList<FilterRegistration.Dynamic>();
    private List<ServletRegistration.Dynamic> servlets = new LinkedList<ServletRegistration.Dynamic>();
    private List<String> listeners = new LinkedList<String>();

    public DynamicServletContextProcessor() {
        context.put("dyn.servlets", servlets);
        context.put("dyn.filters", filters);
        context.put("dyn.listeners", listeners);
    }

    @Override
    public void preProcess(ServletContext t, Object proxy, Method method, Object[] args) {

        // no implementation
    }

    @Override
    public Object postProcess(Object res, ServletContext t, Object proxy, Method method, Object[] args) {

        // if (method.getName().equals("addServlet")) {
        //
        // servlets.add((ServletRegistration.Dynamic) res);
        // }
        // else if (method.getName().equals("addFilter")) {
        //
        // filters.add((FilterRegistration.Dynamic) res);
        // }
        // else if (method.getName().equals("addListener")) {
        //
        // String listenerClassName;
        // if (args[0].getClass().isAssignableFrom(String.class)) {
        // listenerClassName = args[0].toString();
        // }
        // else if (args[0].getClass().isAssignableFrom(Class.class)) {
        // listenerClassName = ((Class<?>) args[0]).getName();
        // }
        // else {
        // listenerClassName = args[0].getClass().getName();
        // }
        //
        // listeners.add(listenerClassName);
        // }

        collectDynInfo(method.getName(), res, args);

        return null;
    }

    public void collectDynInfo(String methodName, Object res, Object[] args) {

        if (methodName.equals("addServlet")) {

            if (res != null) {
                servlets.add((ServletRegistration.Dynamic) res);
            }
        }
        else if (methodName.equals("addFilter")) {

            // if the filter has already been registered,the res will be null;
            if (res != null) {
                filters.add((FilterRegistration.Dynamic) res);
            }
        }
        else if (methodName.equals("addListener")) {

            String listenerClassName;
            if (args[0].getClass().isAssignableFrom(String.class)) {
                listenerClassName = args[0].toString();
            }
            else if (args[0].getClass().isAssignableFrom(Class.class)) {
                listenerClassName = ((Class<?>) args[0]).getName();
            }
            else {
                listenerClassName = args[0].getClass().getName();
            }

            listeners.add(listenerClassName);
        }
    }

}
