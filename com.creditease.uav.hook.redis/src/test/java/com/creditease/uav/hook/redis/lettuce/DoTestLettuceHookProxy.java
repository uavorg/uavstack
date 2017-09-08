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

package com.creditease.uav.hook.redis.lettuce;

import java.util.HashMap;

import com.creditease.monitor.UAVServer;
import com.creditease.monitor.UAVServer.ServerVendor;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.log.ConsoleLogger;
import com.lambdaworks.redis.RedisAsyncConnection;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;

public class DoTestLettuceHookProxy {

    @SuppressWarnings("rawtypes")
    public static void main(String[] args) {

        ConsoleLogger cl = new ConsoleLogger("test");

        cl.setDebugable(true);
        UAVServer.instance().setLog(cl);

        UAVServer.instance().putServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR, ServerVendor.TOMCAT);

        LettuceHookProxy p = new LettuceHookProxy("test", new HashMap());
        p.doProxyInstall(null, "test");

        // foo();
        // foo2();
        // foo3();
        testSync();

        testAsync();

    }

    private static void testSync() {

        System.out.println("TEST Lettuce sync ======================================================");
        RedisClient redisClient = RedisClient.create("redis://localhost:6379/0");
        RedisConnection<String, String> conn = redisClient.connect();

        System.out.println("Connected to Redis");

        conn.set("foo", "bar");
        String value = conn.get("foo");
        System.out.println(value);

        conn.close();
        redisClient.shutdown();
    }

    private static void testAsync() {

        System.out.println("TEST Lettuce async ======================================================");
        RedisClient client = RedisClient.create("redis://localhost:6379/0");
        RedisAsyncConnection<String, String> conn = client.connectAsync();
        conn.set("foo", "bar");

        conn.get("foo");

        conn.lpush("lll", "a");
        conn.lpush("lll", "b");
        conn.lpush("lll", "c");
        conn.lpop("lll");
        conn.lpop("lll");
        conn.lpop("lll");

        conn.hset("mmm", "abc", "123");
        conn.hset("mmm", "def", "456");
        conn.hgetall("mmm");

        conn.del("foo", "lll", "mmm");

        conn.close();
        client.shutdown();
    }

}
