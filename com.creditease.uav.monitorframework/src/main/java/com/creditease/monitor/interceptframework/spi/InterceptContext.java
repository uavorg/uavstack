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

package com.creditease.monitor.interceptframework.spi;

import java.util.Map;

public interface InterceptContext {

    public enum Event {
        WEBCONTAINER_RESOURCE_INIT, WEBCONTAINER_RESOURCE_CREATE, WEBCONTAINER_INIT, WEBCONTAINER_STARTED, WEBCONTAINER_STOPPED, AFTER_SERVET_INIT, BEFORE_SERVLET_DESTROY, GLOBAL_FILTER_REQUEST, GLOBAL_FILTER_RESPONSE
    }

    public <T> T get(Class<T> c);

    public <T> void put(Class<T> c, T obj);

    public Object get(String name);

    public void put(String name, Object obj);

    public Event getEvent();

    public Map<String, Object> getAll();

    public void putAll(Map<String, Object> m);

    public int size();
}
