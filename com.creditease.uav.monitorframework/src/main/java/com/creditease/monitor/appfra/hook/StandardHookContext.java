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

package com.creditease.monitor.appfra.hook;

import java.util.HashMap;

import com.creditease.monitor.appfra.hook.spi.HookContext;

@SuppressWarnings("rawtypes")
public class StandardHookContext extends HashMap implements HookContext {

    private static final long serialVersionUID = 2219639655747004139L;

    @Override
    public <T> void put(Class<T> key, T value) {

        if (key == null || value == null)
            return;

        put(key.getName(), value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> key) {

        if (key == null)
            return null;

        return (T) get(key.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void put(String key, Object value) {

        if (condition(key) || value == null)
            return;

        super.put(key, value);
    }

    /**
     * @param key
     * @return
     */
    private boolean condition(String key) {

        return key == null || "".equals(key);
    }

    @Override
    public Object get(String key) {

        if (condition(key))
            return null;

        return super.get(key);
    }

}
