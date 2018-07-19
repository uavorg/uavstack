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

package com.creditease.uav.cache.redis;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.aredis.cache.AsyncHandler;
import org.aredis.cache.AsyncRedisClient;
import org.aredis.cache.AsyncRedisFactory;
import org.aredis.cache.RedisCommand;
import org.aredis.cache.RedisCommandInfo;
import org.aredis.cache.RedisCommandInfo.CommandStatus;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.uav.cache.redis.api.AbstractAsyncHandler;
import com.creditease.uav.cache.redis.api.CacheService;
import com.creditease.uav.cache.redis.api.CommandInfo;

public class AredisAsyncService implements CacheService {

    static ISystemLogger logger = SystemLogger.getLogger(AredisAsyncService.class);

    private static Map<CommandInfo.RedisCommand, org.aredis.cache.RedisCommand> enumMap = new EnumMap<CommandInfo.RedisCommand, org.aredis.cache.RedisCommand>(
            CommandInfo.RedisCommand.class);

    static {
        enumMap.put(CommandInfo.RedisCommand.SET, RedisCommand.SET);
        enumMap.put(CommandInfo.RedisCommand.HSET, RedisCommand.HSET);
        enumMap.put(CommandInfo.RedisCommand.GET, RedisCommand.GET);
        enumMap.put(CommandInfo.RedisCommand.HGET, RedisCommand.HGET);
        enumMap.put(CommandInfo.RedisCommand.HGETALL, RedisCommand.HGETALL);
        enumMap.put(CommandInfo.RedisCommand.MGET, RedisCommand.MGET);
        enumMap.put(CommandInfo.RedisCommand.MSET, RedisCommand.MSET);
        enumMap.put(CommandInfo.RedisCommand.HMSET, RedisCommand.HMSET);
        enumMap.put(CommandInfo.RedisCommand.HMGET, RedisCommand.HMGET);
        enumMap.put(CommandInfo.RedisCommand.DEL, RedisCommand.DEL);
        enumMap.put(CommandInfo.RedisCommand.EXPIRE, RedisCommand.EXPIRE);
        enumMap.put(CommandInfo.RedisCommand.EXISTS, RedisCommand.EXISTS);
        enumMap.put(CommandInfo.RedisCommand.INCR, RedisCommand.INCR);
        enumMap.put(CommandInfo.RedisCommand.DECR, RedisCommand.DECR);
        enumMap.put(CommandInfo.RedisCommand.HKEYS, RedisCommand.HKEYS);
        enumMap.put(CommandInfo.RedisCommand.HDEL, RedisCommand.HDEL);

        // LIST
        enumMap.put(CommandInfo.RedisCommand.LPUSH, RedisCommand.LPUSH);
        enumMap.put(CommandInfo.RedisCommand.LPOP, RedisCommand.LPOP);
        enumMap.put(CommandInfo.RedisCommand.RPUSH, RedisCommand.RPUSH);
        enumMap.put(CommandInfo.RedisCommand.RPOP, RedisCommand.RPOP);
        enumMap.put(CommandInfo.RedisCommand.LRANGE, RedisCommand.LRANGE);
        enumMap.put(CommandInfo.RedisCommand.LINDEX, RedisCommand.LINDEX);
        enumMap.put(CommandInfo.RedisCommand.LREM, RedisCommand.LREM);
        enumMap.put(CommandInfo.RedisCommand.LSET, RedisCommand.LSET);
        enumMap.put(CommandInfo.RedisCommand.LLEN, RedisCommand.LLEN);
    }

    private AsyncRedisFactory factory;

    private String redisServerAddress;

    private ThreadPoolExecutor executor;

    public AredisAsyncService(String redisServerAddress, int minConcurrent, int maxConcurrent, int queueSize) {
        this(redisServerAddress, minConcurrent, maxConcurrent, queueSize, null);
    }

    public AredisAsyncService(String redisServerAddress, int minConcurrent, int maxConcurrent, int queueSize,
            String password) {

        if (maxConcurrent <= 0) {
            maxConcurrent = 50;
        }

        if (minConcurrent <= 0) {
            minConcurrent = 10;
        }

        if (queueSize <= 0) {
            queueSize = 100;
        }

        this.redisServerAddress = redisServerAddress;

        if (password != null) {
            AsyncRedisFactory.setAuth(redisServerAddress, password);
        }

        /**
         * 初始化线程池
         */
        executor = new ThreadPoolExecutor(minConcurrent, maxConcurrent, 15, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(queueSize));
        executor.allowCoreThreadTimeOut(true);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        if (logger.isTraceEnable()) {
            logger.info(this,
                    "AredisAsyncService线程池设置：min=" + minConcurrent + ",max=" + maxConcurrent + ",queue=" + queueSize);
        }

        factory = new AsyncRedisFactory(executor);
    }

    @Override
    public Object[] submitCommands(CommandInfo... commands) {

        RedisCommandInfo[] commands1 = new RedisCommandInfo[commands.length];
        RedisCommandInfo[] futures = null;
        Object[] result = null;
        for (int i = 0; i < commands.length; i++) {
            commands1[i] = new RedisCommandInfo(enumMap.get(commands[i].getCommand()),
                    Object[].class.cast(commands[i].getParam()));
        }
        AsyncRedisClient client = getAredisClient();
        try {

            /**
             * 只等待10s，超过则认为Redis不可用
             */
            futures = client.submitCommands(commands1).get(10, TimeUnit.SECONDS);

            if (logger.isDebugEnable()) {
                logger.debug(this, "Redis操作" + JSONHelper.toString(commands) + "已提交");
            }

            result = new Object[futures.length];
            for (int i = 0; i < futures.length; i++) {
                result[i] = futures[i].getResult();

            }

        }
        catch (TimeoutException e) {
            logger.err(this, "Redis操作" + JSONHelper.toString(commands) + "超时", e);
            throw new RuntimeException(e);
        }
        catch (InterruptedException e) {
            logger.err(this, "Redis操作" + JSONHelper.toString(commands) + "失败", e);
        }
        catch (ExecutionException e) {
            logger.err(this, "Redis操作" + JSONHelper.toString(commands) + "失败", e);
        }

        return result;
    }

    @Override
    public void submitCommands(AbstractAsyncHandler<CommandInfo> handler, CommandInfo... commands) {

        RedisCommandInfo[] commands1 = new RedisCommandInfo[commands.length];
        for (int i = 0; i < commands.length; i++) {
            commands1[i] = new RedisCommandInfo(enumMap.get(commands[i].getCommand()),
                    Object[].class.cast(commands[i].getParam()));
        }
        AsyncRedisClient client = getAredisClient();
        final AbstractAsyncHandler<CommandInfo> fhandler = handler;
        final CommandInfo[] fcommands = commands;
        client.submitCommands(commands1, new AsyncHandler<RedisCommandInfo[]>() {

            @Override
            public void completed(RedisCommandInfo[] redisCommandInfos, Throwable e) {

                if (e != null) {
                    logger.err(this, "Redis操作" + JSONHelper.toString(fcommands) + "异常", e);
                }
                /**
                 * 允许AbstractAsyncHandler为空
                 */
                if (fhandler == null) {
                    return;
                }
                Object[] infos = new Object[redisCommandInfos.length];
                for (int i = 0; i < redisCommandInfos.length; i++) {
                    infos[i] = redisCommandInfos[i].getResult();
                    fcommands[i]
                            .setState((redisCommandInfos[i].getRunStatus() == CommandStatus.SUCCESS) ? true : false);
                }
                fhandler.process(fcommands, infos, e);
            }
        }, true, false);
        if (logger.isDebugEnable()) {
            logger.debug(this, "Redis操作" + JSONHelper.toString(commands) + "已提交");
        }
    }

    private AsyncRedisClient getAredisClient() {

        return factory.getClient(this.redisServerAddress);
    }

    @Override
    public void start() {

        // Do nothing but must pass sonar check
    }

    @Override
    public void shutdown() {

        this.executor.shutdown();
        this.executor.shutdownNow();
        try {
            this.executor.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            // ignore
        }

    }
}
