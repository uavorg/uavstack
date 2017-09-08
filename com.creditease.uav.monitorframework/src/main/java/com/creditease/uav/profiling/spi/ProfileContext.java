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

package com.creditease.uav.profiling.spi;

import java.util.Map;
import java.util.Set;

public interface ProfileContext {

    public <T> void put(Class<T> key, T value);

    public <T> T get(Class<T> key);

    public void put(String key, Object value);

    public Object get(String key);

    public void put(Map<String, Object> map);

    public void addPE(String profileElemId);

    public Set<String> getPE();
}
