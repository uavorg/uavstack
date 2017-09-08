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

package com.creditease.uav.cache.redis.api;

import com.creditease.uav.cache.redis.AredisAsyncService;

public class CacheFactory {

    public enum CacheClientMode {
        AREDIS
    }

    private static CacheFactory factory = new CacheFactory();

    public static CacheFactory instance() {

        return factory;
    }

    private CacheFactory() {

    }

    /**
     * 创建CacheService
     * 
     * @param mode
     * @param redisServerAddress
     * @param minConcurrent
     * @param maxConcurrent
     * @param QueueSize
     * @return
     */
    public CacheService createCacheService(CacheClientMode mode, String redisServerAddress, int minConcurrent,
            int maxConcurrent, int QueueSize, String password) {

        CacheService service = null;

        switch (mode) {
            case AREDIS:
            default:
                service = new AredisAsyncService(redisServerAddress, minConcurrent, maxConcurrent, QueueSize, password);
                break;
        }

        return service;
    }

    public CacheService createCacheService(CacheClientMode mode, String redisServerAddress, int minConcurrent,
            int maxConcurrent, int QueueSize) {

        return createCacheService(mode, redisServerAddress, minConcurrent, maxConcurrent, QueueSize, null);
    }

}
