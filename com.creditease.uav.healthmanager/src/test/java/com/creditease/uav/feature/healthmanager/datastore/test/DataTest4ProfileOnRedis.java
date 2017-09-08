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

package com.creditease.uav.feature.healthmanager.datastore.test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.feature.healthmanager.HealthManagerConstants;

public class DataTest4ProfileOnRedis {

    public static void main(String[] args) {

        ConfigurationManager.build(new HashMap<String, String>());

        CacheManager.build("127.0.0.1:6379", 10, 100, 10);

        testLoad();
    }

    @SuppressWarnings("unused")
    private static void testPutProfile() {

        CacheManager.instance().beginBatch();
        Map<String, Object> ni = new LinkedHashMap<String, Object>();
        ni.put("appid", "test.app");
        ni.put("ip", "127.0.0.1");
        ni.put("svrid", "/app/t7-dss::/app/t7-dss");
        ni.put("host", "test.host");
        ni.put("time", System.currentTimeMillis());
        ni.put("appname", "dss");
        ni.put("webapproot", "/app/t7-dss/webapps/dss");
        ni.put("appurl", "http://127.0.0.1:8080/dss");

        System.out.println(JSONHelper.toString(ni));

        String filedKey = ni.get("ip") + "@" + ni.get("svrid") + "@" + ni.get("appid");

        CacheManager.instance().putHash(HealthManagerConstants.STORE_REGION_UAV,
                HealthManagerConstants.STORE_KEY_PROFILEINFO, filedKey, JSONHelper.toString(ni));

        CacheManager.instance().put(HealthManagerConstants.STORE_REGION_UAV, filedKey, "" + ni.get("time"));

        CacheManager.instance().submitBatch();
    }

    public static void testLoad() {

        SystemLogger.init("DEBUG", true, 5);

        CacheManager cm = CacheManager.instance();

        Map<String, String> data = cm.getHashAll(HealthManagerConstants.STORE_REGION_UAV,
                HealthManagerConstants.STORE_KEY_PROFILEINFO);

        System.out.println(JSONHelper.toString(data));
    }

}
