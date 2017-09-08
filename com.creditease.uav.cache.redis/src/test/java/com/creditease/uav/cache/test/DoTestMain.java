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

package com.creditease.uav.cache.test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.creditease.agent.helpers.ThreadHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.uav.cache.api.CacheManager;

public class DoTestMain {

    public static void main(String[] args) {

        SystemLogger.init("DEBUG", true, 5);
        String ip = "localhost:6379";
        CacheManager.build(ip, 0, 0, 0);
        CacheManager cm = CacheManager.instance();
        Map<String, String> hash = new HashMap<String, String>();
        hash.put("age", "1");
        hash.put("name", "zz");
        cm.del("test", "123");
        cm.putHash("test", "123", hash, 3600, TimeUnit.SECONDS);// "1#360121199403085855#774889"

        ThreadHelper.suspend(500);

        Map<String, String> m = cm.getHashAll("test", "123");

        System.out.println(m);
    }

}
