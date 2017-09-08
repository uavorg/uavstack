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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RecursiveAction;

public abstract class AbstractPartitionJob extends RecursiveAction {

    private static final long serialVersionUID = -8059656093621763282L;

    protected Map<String, Object> contents = new HashMap<String, Object>();

    public <T> void put(Class<T> c, T obj) {

        if (null == c || null == obj) {
            return;
        }
        contents.put(c.getName(), obj);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> c) {

        if (null == c) {
            return null;
        }

        return (T) contents.get(c.getName());
    }

    public void put(String ckey, Object obj) {

        if (null == ckey || null == obj) {
            return;
        }

        contents.put(ckey, obj);
    }

    public Object get(String ckey) {

        if (null == ckey) {
            return null;
        }

        return contents.get(ckey);
    }

    @Override
    protected void compute() {

        work();
    }

    protected abstract void work();
}
