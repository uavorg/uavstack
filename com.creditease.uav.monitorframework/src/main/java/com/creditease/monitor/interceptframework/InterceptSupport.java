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

package com.creditease.monitor.interceptframework;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.creditease.monitor.UAVServer;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;
import com.creditease.monitor.interceptframework.spi.InterceptEventListener;
import com.creditease.monitor.log.Logger;

public class InterceptSupport {

    private final static InterceptSupport instance = new InterceptSupport();

    public static InterceptSupport instance() {

        return instance;
    }

    private List<InterceptEventListener> listeners = new CopyOnWriteArrayList<InterceptEventListener>();
    private ClassLoader contextClassLoader = null;
    private final Logger log;

    private InterceptSupport() {
        log = UAVServer.instance().getLog();
    }

    public InterceptEventListener getEventListener(Class<?> listenerClass) {

        for (InterceptEventListener ie : listeners) {
            if (ie.getClass().getName().equals(listenerClass.getName())) {
                return ie;
            }
        }

        return null;
    }

    public void addEventListener(InterceptEventListener listener) {

        if (listener == null) {
            return;
        }

        listeners.add(listener);
    }

    public void removeEventListener(InterceptEventListener listener) {

        if (listener == null) {
            return;
        }

        listeners.remove(listener);
    }

    public void doIntercept(InterceptContext context) {

        InterceptContext threadLocalContext = StandardInterceptContextHelper.getContext(context.getEvent(), false);

        mergeContext(threadLocalContext, context);

        for (InterceptEventListener listener : listeners) {

            if (listener.isEventListener(context.getEvent())) {

                doHandleEvent(context, listener);

            }
        }

        if (threadLocalContext != null) {
            StandardInterceptContextHelper.releaseContext(context.getEvent());
        }
    }

    /**
     * mergeContext
     * 
     * @param threadLocalContext
     * @param context
     */
    private void mergeContext(InterceptContext threadLocalContext, InterceptContext context) {

        if (threadLocalContext == null || threadLocalContext.size() == 0) {
            return;
        }

        Map<String, Object> params = threadLocalContext.getAll();

        context.putAll(params);
    }

    /**
     * @param context
     * @param listener
     */
    private void doHandleEvent(InterceptContext context, InterceptEventListener listener) {

        try {
            if (log.isDebugable()) {
                log.debug("listener[" + listener.getClass().getName() + "] handle START", null);
            }
            listener.handleEvent(context);
            if (log.isDebugable()) {
                log.debug("listener[" + listener.getClass().getName() + "] handle DONE", null);
            }
        }
        catch (Exception e) {
            log.error("listener[" + listener.getClass().getName() + "] handle FAILs", e);
        }
    }

    public InterceptEventListener createInterceptEventListener(String className) {

        return newInstance(className);
    }

    public InterceptContext createInterceptContext(InterceptContext.Event event) {

        return new StandardInterceptContext(event);
    }

    public void clearListeners() {

        listeners.clear();
    }

    public void setContextClassLoader(ClassLoader cl) {

        this.contextClassLoader = cl;
    }

    private InterceptEventListener newInstance(String lClassName) {

        InterceptEventListener listener = null;
        try {
            Class<?> lClass = null;
            List<ClassLoader> cls = new ArrayList<ClassLoader>();
            if (this.contextClassLoader != null) {
                cls.add(this.contextClassLoader);
            }
            cls.add(Thread.currentThread().getContextClassLoader());
            cls.add(this.getClass().getClassLoader());

            lClass = tryLoadClass(lClassName, lClass, cls);

            if (lClass == null) {
                log.error("InterceptEventListener[" + lClassName + "] is loaded FAIL", null);
                return listener;
            }

            Object inst = lClass.newInstance();
            listener = InterceptEventListener.class.cast(inst);

        }
        catch (Exception e) {
            // ignore
        }
        return listener;
    }

    /**
     * @param lClassName
     * @param lClass
     * @param cls
     * @return
     */
    private Class<?> tryLoadClass(String lClassName, Class<?> lClass, List<ClassLoader> cls) {

        for (ClassLoader cl : cls) {
            try {
                lClass = cl.loadClass(lClassName);
                if (lClass != null)
                    break;
            }
            catch (ClassNotFoundException e) {
                continue;
            }
        }
        return lClass;
    }

    /**
     * NOTE: only this API provides the CreateForNone to create ThreadLocal InterceptContext as mostly this should be
     * triggered by user
     * 
     * @param event
     * @return
     */
    public InterceptContext getThreadLocalContext(Event event) {

        return StandardInterceptContextHelper.getContext(event, true);
    }

    public InterceptContext getThreadLocalContext(Event event, boolean isCreateForNone) {

        return StandardInterceptContextHelper.getContext(event, isCreateForNone);
    }

}
