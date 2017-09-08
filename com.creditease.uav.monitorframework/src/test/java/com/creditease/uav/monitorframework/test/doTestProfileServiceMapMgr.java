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

package com.creditease.uav.monitorframework.test;

import java.util.Arrays;

import com.creditease.uav.profiling.spi.ProfileServiceMapMgr;
import com.creditease.uav.profiling.spi.ProfileServiceMapMgr.ServiceMapBinding;

public class doTestProfileServiceMapMgr {

    public static void main(String[] args) {

        // prepare
        ProfileServiceMapMgr psmm = new ProfileServiceMapMgr();

        String[] servlet_1_urls = new String[] { "/test" };

        psmm.addServiceMapBinding("test", "servlet_1", "service", Arrays.asList(servlet_1_urls), 2);

        String[] filter_1_urls = new String[] { "/test" };

        psmm.addServiceMapBinding("test", "filter_1", "doFilter", Arrays.asList(filter_1_urls), 1);

        String[] jaxrs_1_urls = new String[] { "/app1/m1" };

        psmm.addServiceMapBinding("test", "jaxrs_1", "m1", Arrays.asList(jaxrs_1_urls), 0);

        String[] jaxrs_2_urls = new String[] { "/app1/m2/{id}/{age}" };

        psmm.addServiceMapBinding("test", "jaxrs_1", "m2", Arrays.asList(jaxrs_2_urls), 0);

        String[] jaxrs_3_urls = new String[] { "/app1/{id}" };

        psmm.addServiceMapBinding("test", "jaxrs_1", "m3", Arrays.asList(jaxrs_3_urls), 0);

        // 精确匹配filter
        test(psmm, "http://localhost:8080/myapp/test");
        // 模糊匹配filter
        test(psmm, "http://localhost:8080/myapp/test/1");
        // 精确匹配jaxrs
        test(psmm, "http://localhost:8080/myapp/test/app1/m1");
        // 精确匹配jaxrs, 但无法匹配，会匹配到filter
        test(psmm, "http://localhost:8080/myapp/test/app1/m1/1");
        // 精确匹配jaxrs带pathParam
        test(psmm, "http://localhost:8080/myapp/test/app1/m2/1/2");
        // 精确匹配jaxrs带pathParam
        test(psmm, "http://localhost:8080/myapp/test/app1/1");

        double tcost = 0;
        int count = 10000;
        for (int i = 0; i < count; i++) {
            tcost += test(psmm, "http://localhost:8080/myapp/test/app1/m2/1/2");
        }
        System.out.println(tcost / count);
    }

    private static long test(ProfileServiceMapMgr psmm, String test1) {

        long st = System.currentTimeMillis();
        ServiceMapBinding smb = psmm.searchServiceMapBinding("test", test1);
        long cost = System.currentTimeMillis() - st;
        System.out.println(smb.toString() + ":" + cost);
        return cost;
    }

}
