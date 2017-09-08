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

package com.creditease.uav.hook.redis.jedis;

import java.io.IOException;
import java.util.HashMap;

import com.creditease.monitor.UAVServer;
import com.creditease.monitor.UAVServer.ServerVendor;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.log.ConsoleLogger;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class DoTestJedisHookProxy {

    private static String ip = "localhost";
    private static int port = 6379;

    @SuppressWarnings("rawtypes")
    public static void main(String[] args) {

        ConsoleLogger cl = new ConsoleLogger("test");

        cl.setDebugable(true);
        UAVServer.instance().setLog(cl);

        UAVServer.instance().putServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR, ServerVendor.TOMCAT);

        JedisHookProxy p = new JedisHookProxy("test", new HashMap());

        p.doProxyInstall(null, "test");

        foo();
        foo2();
        // foo3();
    }

    private static void foo() {

        System.out.println("TEST Jedis ======================================================");
        Jedis jedis = new Jedis(ip, port);
        jedis.set("foo", "bar");

        jedis.get("foo");

        jedis.lpush("lll", "a");
        jedis.lpush("lll", "b");
        jedis.lpush("lll", "c");
        jedis.lpop("lll");
        jedis.lpop("lll");
        jedis.lpop("lll");

        jedis.hset("mmm", "abc", "123");
        jedis.hset("mmm", "def", "456");
        jedis.hgetAll("mmm");

        jedis.close();
    }

    private static void foo2() {

        System.out.println("TEST JedisPool ======================================================");
        JedisPoolConfig cfg = new JedisPoolConfig();
        cfg.setMaxTotal(5);
        cfg.setMaxIdle(1);
        cfg.setMaxWaitMillis(10000L);

        JedisPool jp = new JedisPool(cfg, ip, port);
        Jedis jedis = jp.getResource();

        jedis.set("foo", "bar");
        // jedis.close();
        jedis = jp.getResource();

        jedis.get("foo");
        // jedis.close();
        jedis = jp.getResource();

        jedis.lpush("lll", "a");
        jedis.lpush("lll", "b");
        jedis.lpush("lll", "c");
        jedis.lpop("lll");
        jedis.lpop("lll");
        jedis.lpop("lll");
        // jedis.close();
        jedis = jp.getResource();

        jedis.hset("mmm", "abc", "123");
        jedis.hset("mmm", "def", "456");
        jedis.hgetAll("mmm");

        jp.close();
    }

    @SuppressWarnings("unused")
    private static void foo3() {

        System.out.println("TEST JedisCluster ======================================================");

        JedisCluster jc = new JedisCluster(new HostAndPort("127.0.0.1", 6380));
        jc.set("foo", "bar");
        String val = jc.get("foo");
        System.out.println(val);

        jc.set("foo1", "bar");
        jc.set("foo2", "bar");
        jc.set("foo3", "bar");
        jc.set("foo4", "bar");
        jc.set("foo5", "bar");

        jc.del("foo");
        jc.del("foo1");
        jc.del("foo2");
        jc.del("foo3");
        jc.del("foo4");
        jc.del("foo5");

        try {
            jc.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
