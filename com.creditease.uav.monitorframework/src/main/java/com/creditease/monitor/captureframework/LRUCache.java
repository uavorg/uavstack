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

package com.creditease.monitor.captureframework;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    private int cachesize;

    public LRUCache(int cachesize) {
        super(16, 0.75f, true);
        this.cachesize = cachesize;
    }

    public LRUCache(int initialCapacity, int cachesize) {
        super(initialCapacity, 0.75f, true);
        this.cachesize = cachesize;
    }

    public LRUCache(int initialCapacity, int cachesize, boolean accessOrderacity) {
        super(initialCapacity, 0.75f, accessOrderacity);
        this.cachesize = cachesize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {

        return size() > cachesize;
    }

}
