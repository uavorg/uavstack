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

package com.creditease.uav.apm.invokechain.spi;

import java.util.HashMap;
import java.util.Map;

public class InvokeChainContext extends HashMap<String, Object> {

    private static final long serialVersionUID = 8758694368762576773L;

    public <T> void put(Class<T> key, T value) {

        if (key == null || value == null)
            return;

        put(key.getName(), value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> key) {

        if (key == null)
            return null;

        return (T) get(key.getName());
    }

    public void put(Map<String, Object> map) {

        if (map == null) {
            return;
        }

        super.putAll(map);
    }
}
