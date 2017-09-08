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

package com.creditease.uav.hook.redis.aredis;

import java.util.HashMap;

import com.creditease.agent.log.SystemLogger;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.UAVServer.ServerVendor;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.log.ConsoleLogger;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.cache.api.CacheManagerFactory;

public class DoTestAredisHookProxy {

    public static void main(String[] args) throws InterruptedException {

        ConsoleLogger cl = new ConsoleLogger("test");

        cl.setDebugable(true);
        UAVServer.instance().setLog(cl);

        UAVServer.instance().putServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR, ServerVendor.TOMCAT);

        @SuppressWarnings("rawtypes")
        AredisHookProxy p = new AredisHookProxy("test", new HashMap());

        p.doProxyInstall(null, "test");

        SystemLogger.init("DEBUG", true, 0);

        CacheManager cm = CacheManagerFactory.build("localhost:6379", 1, 5, 5);
        cm.put("TEST", "foo", "bar");
        String v = cm.get("TEST", "foo");
        System.out.println(v);

        Thread.sleep(1000l);
        cm.shutdown();
    }

}
