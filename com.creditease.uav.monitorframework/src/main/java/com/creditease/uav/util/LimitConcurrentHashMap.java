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

package com.creditease.uav.util;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * LimitConcurrentHashMap description: limit the capacity of K-V pairs for ConcurrentHashMap
 *
 * @param <K>
 * @param <V>
 */
public class LimitConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V> {

    private static final long serialVersionUID = -7573867171798556834L;

    protected volatile int limitCapacity;

    public LimitConcurrentHashMap(int limit) {
        this.limitCapacity = limit;
    }

    @Override
    public V put(K key, V value) {

        if (this.size() >= this.limitCapacity) {
            return value;
        }

        V tv = super.put(key, value);

        return tv;
    }

    @Override
    public V putIfAbsent(K key, V value) {

        if (this.size() >= this.limitCapacity) {
            return value;
        }

        V tv = super.putIfAbsent(key, value);

        return tv;
    }
}
