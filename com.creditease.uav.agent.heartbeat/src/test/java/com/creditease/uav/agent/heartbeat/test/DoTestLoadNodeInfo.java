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

package com.creditease.uav.agent.heartbeat.test;

import java.util.Map;

import com.creditease.agent.heartbeat.api.HeartBeatProtocol;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.uav.cache.api.CacheManager;

public class DoTestLoadNodeInfo {

    public static void main(String[] args) {

        SystemLogger.init("DEBUG", true, 5);

        CacheManager.build("localhost:6379", 5, 5, 5);

        CacheManager cm = CacheManager.instance();

        // testDelNodeInfo(cm);
        testLoadAllNodeInfo(cm);
    }

    /**
     * load all node info
     * 
     * @param cm
     */
    private static void testLoadAllNodeInfo(CacheManager cm) {

        Map<String, String> data = cm.getHashAll(HeartBeatProtocol.STORE_REGION_UAV,
                HeartBeatProtocol.STORE_KEY_NODEINFO);

        System.out.println(JSONHelper.toString(data));
    }

    private static void testDelNodeInfo(CacheManager cm) {

        cm.delHash(HeartBeatProtocol.STORE_REGION_UAV, HeartBeatProtocol.STORE_KEY_NODEINFO, "749069247707303936");
    }
}
