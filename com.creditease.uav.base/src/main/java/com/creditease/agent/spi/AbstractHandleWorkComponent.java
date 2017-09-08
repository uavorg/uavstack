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

package com.creditease.agent.spi;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractHandleWorkComponent<T, V extends AbstractHandler<T>>
        extends AbstractQueueWorkComponent<T> {

    @SuppressWarnings("serial")
    protected List<V> handlers = new CopyOnWriteArrayList<V>() {

        @SuppressWarnings("unchecked")
        @Override
        public boolean contains(Object o) {

            V handler = (V) o;

            for (V v : this) {

                if (handler.getFeature().equalsIgnoreCase(v.getFeature()) && handler.getName().equals(v.getName())) {
                    return true;
                }
            }

            return false;
        }
    };

    @SuppressWarnings("unchecked")
    public AbstractHandleWorkComponent(String cName, String feature, String initHandlerKey) {
        super(cName, feature);

        // init handlers in configuration
        String handlerConfigStr = this.getConfigManager().getFeatureConfiguration(feature, initHandlerKey);

        if (null != handlerConfigStr && !"".equalsIgnoreCase(handlerConfigStr)) {

            String[] handlerClassesStr = handlerConfigStr.split(",");

            if (null == handlerClassesStr || handlerClassesStr.length == 0) {
                return;
            }

            ClassLoader cl = this.getClass().getClassLoader();
            for (String handlerClassStr : handlerClassesStr) {
                try {
                    Class<?> c = cl.loadClass(handlerClassStr);

                    Constructor<?> con = c.getConstructor(new Class<?>[] { String.class, String.class });

                    V handler = (V) con.newInstance(new Object[] { c.getSimpleName(), feature });

                    this.registerHandler(handler);

                }
                catch (Exception e) {
                    log.err(this, "Feature [" + feature + "] Handler Class[" + handlerClassStr + "] new instance FAIL.",
                            e);
                }
            }
        }
    }

    public void registerHandler(V handler) {

        if (null == handler) {
            return;
        }

        if (!handlers.contains(handler)) {
            handlers.add(handler);
        }
    }

    @SuppressWarnings({ "rawtypes" })
    public void unregisterHandler(V handler) {

        if (null == handler) {
            return;
        }

        handlers.remove(handler);

        AbstractHandler ah = handler;

        // unregister handler
        this.getConfigManager().unregisterComponent(ah.getFeature(), ah.getName());
    }

    @Override
    protected void handle(List<T> data) {

        this.runHandlers(data);
    }

    public void runHandlers(T data) {

        List<T> dataList = new ArrayList<T>();

        dataList.add(data);

        runHandlers(dataList);
    }

    public void runHandlers(List<T> data) {

        for (V handler : handlers) {
            try {
                handler.handle(data);
            }
            catch (Exception e) {
                log.err(this, "Feature [" + handler.getFeature() + "] Handler [" + handler.getName() + "] handle FAIL.",
                        e);
            }
        }
    }

    public List<V> getHandlers() {

        return handlers;
    }
}
