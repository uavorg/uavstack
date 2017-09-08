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

package com.creditease.uav.cache.api;

/**
 * Provide Cache Manager Support
 * 
 * @author zhen zhang
 *
 */
public class CacheManagerFactory {

    /**
     * build
     * 
     * @param cacheServerAddress
     * @param minConcurrent
     * @param maxConcurrent
     * @param queueSize
     * @param password
     * @return
     */
    public static CacheManager build(String cacheServerAddress, int minConcurrent, int maxConcurrent, int queueSize,
            String password) {

        return new CacheManager(cacheServerAddress, minConcurrent, maxConcurrent, queueSize, password);
    }

    /**
     * build
     * 
     * @param cacheServerAddress
     * @param minConcurrent
     * @param maxConcurrent
     * @param queueSize
     * @return
     */
    public static CacheManager build(String cacheServerAddress, int minConcurrent, int maxConcurrent, int queueSize) {

        return build(cacheServerAddress, minConcurrent, maxConcurrent, queueSize, null);
    }
}
