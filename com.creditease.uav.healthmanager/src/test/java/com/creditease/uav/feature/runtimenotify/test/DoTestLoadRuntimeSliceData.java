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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.feature.runtimenotify.Slice;

public class DoTestLoadRuntimeSliceData {

    public static void main(String[] args) {

        SystemLogger.init("DEBUG", true, 5);

        // CacheManager.build("localhost:6379", 5, 5, 5);

        CacheManager.build("127.0.0.1:6379", 5, 5, 5);

        loadSlices(120, "SLICE_server@jvm@http://127.0.0.1:9090_99", CacheManager.instance());
    }

    public static void loadSlices(long range, String key, CacheManager cm) {

        long end = System.currentTimeMillis();
        long start = end - range * 1000;

        int cacheLen = cm.llen("store.region.uav", key);

        System.out.println("len:" + cacheLen);

        List<Slice> list = new ArrayList<>();

        for (int index = cacheLen - 1; index > 0; index--) {

            String sj = cm.lindex("store.region.uav", key, index);

            Slice r = new Slice(sj);

            if (r.getTime() <= end && r.getTime() >= start) {
                list.add(r);
                continue;
            }

            if (r.getTime() < start) {
                break;
            }
        }

        Collections.reverse(list);
        System.out.println("sellen:" + list.size());

        for (Slice slice : list) {
            System.out.println(JSONHelper.toString(slice));
        }
    }

}
