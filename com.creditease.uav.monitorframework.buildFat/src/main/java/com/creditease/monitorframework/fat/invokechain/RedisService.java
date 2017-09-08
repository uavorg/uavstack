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

package com.creditease.monitorframework.fat.invokechain;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.creditease.agent.log.SystemLogger;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.cache.api.CacheManagerFactory;
import com.lambdaworks.redis.RedisAsyncConnection;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * service中与redis有交互的
 *
 */
@Singleton
@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Path("redis")
public class RedisService {

    @GET
    @Path("aredistest")
    @Produces(MediaType.TEXT_HTML + ";charset=utf-8")
    public String aredistest() {

        SystemLogger.init("DEBUG", true, 0);
        System.out.println("TEST aredis ======================================================");
        CacheManager cm = CacheManagerFactory.build("localhost:6379", 1, 5, 5);
        cm.put("TEST", "foo", "bar");
        String v = cm.get("TEST", "foo");
        System.out.println(v);
        cm.shutdown();
        return "aredistest";
    }

    @GET
    @Path("jedistest")
    @Produces(MediaType.TEXT_HTML + ";charset=utf-8")
    public String jedistest() {

        System.out.println("TEST Jedis ======================================================");
        Jedis jedis = new Jedis("localhost", 6379);
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
        return "jedistest";
    }

    @GET
    @Path("jedispooltest")
    @Produces(MediaType.TEXT_HTML + ";charset=utf-8")
    public String jedispolltest() {

        System.out.println("TEST JedisPool ======================================================");
        JedisPoolConfig cfg = new JedisPoolConfig();
        cfg.setMaxTotal(5);
        cfg.setMaxIdle(1);
        cfg.setMaxWaitMillis(10000L);

        JedisPool jp = new JedisPool(cfg, "localhost", 6379);
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
        return "jedispooltest";
    }

    @GET
    @Path("lettucetest")
    @Produces(MediaType.TEXT_HTML + ";charset=utf-8")
    public String lettucetest() {

        System.out.println("TEST Lettuce sync ======================================================");
        RedisClient redisClient = RedisClient.create("redis://localhost:6379/0");
        RedisConnection<String, String> conn = redisClient.connect();

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
        redisClient.shutdown();
        return "lettucetest";
    }

    @GET
    @Path("lettuce_async_test")
    @Produces(MediaType.TEXT_HTML + ";charset=utf-8")
    public String lettuceAsyncTest() {

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
        return "lettuce_async_test";
    }
}
