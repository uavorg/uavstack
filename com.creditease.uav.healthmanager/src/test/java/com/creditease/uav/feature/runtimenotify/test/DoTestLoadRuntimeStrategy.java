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

package com.creditease.uav.feature.runtimenotify.test;

import java.util.Map;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.feature.runtimenotify.scheduler.RuntimeNotifyStrategyMgr;

public class DoTestLoadRuntimeStrategy {

    public static void main(String[] args) {

        SystemLogger.init("DEBUG", true, 5);

        // CacheManager.build("localhost:6379", 5, 5, 5);

        CacheManager.build("127.0.0.1:6379", 5, 5, 5);

        loadRuntimeStrategy(CacheManager.instance());
    }

    private static void loadRuntimeStrategy(CacheManager cm) {

        Map<String, String> strategyMap = cm.getHashAll(RuntimeNotifyStrategyMgr.UAV_CACHE_REGION,
                RuntimeNotifyStrategyMgr.RT_STRATEGY_KEY);

        System.out.println(JSONHelper.toString(strategyMap));
    }

    @SuppressWarnings("unused")
    private static void cleanAllNCInfo(CacheManager cm) {

        cm.del(RuntimeNotifyStrategyMgr.UAV_CACHE_REGION, RuntimeNotifyStrategyMgr.RT_STRATEGY_KEY);

        loadRuntimeStrategy(cm);
    }
}
