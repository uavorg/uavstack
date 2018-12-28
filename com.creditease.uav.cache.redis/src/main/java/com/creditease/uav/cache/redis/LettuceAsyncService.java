/*-
 * <<
 * UAVStack
 * ==
 * Copyright (C) 2016 - 2018 UAVStack
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.uav.cache.redis.api.AbstractAsyncHandler;
import com.creditease.uav.cache.redis.api.CacheService;
import com.creditease.uav.cache.redis.api.CommandInfo;

import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;

/**
 * LettuceAsyncService description: lettuce实现类
 * 
 * @param <V>
 *
 */
public class LettuceAsyncService implements CacheService {

    static ISystemLogger logger = SystemLogger.getLogger(LettuceAsyncService.class);
    private static StatefulRedisClusterConnection<String, String> connect;
    private static RedisClusterClient client;
    private static long expireTimeLong = 10;

    public enum EnumMethod {
        HKEYS() {

            @Override
            public Object send(String[] params) {
                LettuceCommandResult lcr = new LettuceCommandResult();
                RedisAdvancedClusterAsyncCommands<String, String> commandAsync = connect.async();
                RedisFuture<List<String>> result = commandAsync.hkeys(params[0]);
                List<String> res = null;
                try {
                    res = result.get(expireTimeLong, TimeUnit.SECONDS);
                    lcr.setResult(res);
                    lcr.setRunState(true);
                }
                catch (Exception e) {
                    lcr.setRunState(false);
                }
                return lcr;
            }
        },

        HGETALL() {

            @Override
            public Object send(String[] params) {
                LettuceCommandResult lcr = new LettuceCommandResult();
                RedisAdvancedClusterAsyncCommands<String, String> commandAsync = connect.async();
                RedisFuture<Map<String, String>> result = commandAsync.hgetall(params[0]);
                try {
                    Map<String, String> res = result.get(expireTimeLong, TimeUnit.SECONDS);
                    lcr.setResult(res);
                    lcr.setRunState(true);
                }
                catch (Exception e) {
                    lcr.setRunState(false);
                }
                return lcr;
            }
        },
        HDEL() {

            @Override
            public Object send(String[] params) {
                // 异步不要求返回结果

                LettuceCommandResult lcr = new LettuceCommandResult();
                RedisAdvancedClusterAsyncCommands<String, String> commandAsync = connect.async();

                try {
                    RedisFuture<Long> result = commandAsync.hdel(params[0],
                            Arrays.copyOfRange(params, 1, params.length));
                    lcr.setResult(result);
                    lcr.setRunState(true);
                }
                catch (Exception e) {
                    lcr.setRunState(false);
                }
                return lcr;

            }
        },
        RPUSH() {

            @Override
            public Object send(String[] params) {
                // 异步不要求返回结果
                LettuceCommandResult lcr = new LettuceCommandResult();
                RedisAdvancedClusterAsyncCommands<String, String> commandAsync = connect.async();

                try {
                    RedisFuture<Long> results = commandAsync.rpush(params[0], new String[] { params[1] });
                    lcr.setResult(results);
                    lcr.setRunState(true);
                }
                catch (Exception e) {
                    lcr.setRunState(false);
                }
                return lcr;
            }
        },
        RPOP() {

            @Override
            public Object send(String[] params) {
                // 无论同步异步都要求返回结果
                LettuceCommandResult lcr = new LettuceCommandResult();
                RedisAdvancedClusterAsyncCommands<String, String> commandAsync = connect.async();
                RedisFuture<String> result = commandAsync.rpop(params[0]);
                try {
                    String res = result.get(expireTimeLong, TimeUnit.SECONDS);
                    lcr.setResult(res);
                    lcr.setRunState(true);
                }
                catch (Exception e) {
                    lcr.setRunState(false);
                }
                return lcr;
            }
        },
        LSET() {

            @Override
            public Object send(String[] params) {
                // 异步不要求返回结果

                LettuceCommandResult lcr = new LettuceCommandResult();
                RedisAdvancedClusterAsyncCommands<String, String> commandAsync = connect.async();

                try {
                    RedisFuture<String> result = commandAsync.lset(params[0], Long.parseLong(params[1]), params[2]);
                    lcr.setResult(result);
                    lcr.setRunState(true);
                }
                catch (Exception e) {
                    lcr.setRunState(false);
                }
                return lcr;
            }
        },
        LREM() {

            @Override
            public Object send(String[] params) {
                // 同步异步都要求返回结果

                LettuceCommandResult lcr = new LettuceCommandResult();
                RedisAdvancedClusterAsyncCommands<String, String> commandAsync = connect.async();
                try {
                    RedisFuture<Long> result = commandAsync.lrem(params[0], Long.parseLong(params[1]), params[2]);
                    long res = result.get(expireTimeLong, TimeUnit.SECONDS);
                    lcr.setResult((int) res);
                    lcr.setRunState(true);
                }
                catch (Exception e) {
                    lcr.setResult(0);
                    lcr.setRunState(false);
                }
                return lcr;
            }
        },
        LRANGE() {

            @Override
            public Object send(String[] params) {
                // 同步异步均需要返回结果

                LettuceCommandResult lcr = new LettuceCommandResult();
                RedisAdvancedClusterAsyncCommands<String, String> commandAsync = connect.async();

                RedisFuture<List<String>> result = commandAsync.lrange(params[0], Long.parseLong(params[1]),
                        Long.parseLong(params[2]));

                try {
                    List<String> res = result.get(expireTimeLong, TimeUnit.SECONDS);

                    if (res == null || res.isEmpty()) {
                        lcr.setResult(null);
                    }
                    else {
                        String[] resultArray = new String[res.size()];
                        for (int i = 0; i < res.size(); i++) {
                            resultArray[i] = res.get(i).toString();
                        }
                        lcr.setResult(resultArray);
                    }

                    lcr.setRunState(true);
                }
                catch (Exception e) {
                    lcr.setResult(Collections.emptyList());
                    lcr.setRunState(false);
                }
                return lcr;
            }
        },
        LPUSH() {

            @Override
            public Object send(String[] params) {
                // 只有异步操作 不需要返回值
                LettuceCommandResult lcr = new LettuceCommandResult();
                RedisAdvancedClusterAsyncCommands<String, String> commandAsync = connect.async();

                try {
                    RedisFuture<Long> result = commandAsync.lpush(params[0], new String[] { params[1] });
                    lcr.setResult(result);
                    lcr.setRunState(true);

                }
                catch (Exception e) {
                    lcr.setRunState(false);
                }
                return lcr;
            }
        },
        LPOP() {

            @Override
            public Object send(String[] params) {
                // 同步异步均需返回结果
                LettuceCommandResult lcr = new LettuceCommandResult();
                RedisAdvancedClusterAsyncCommands<String, String> commandAsync = connect.async();
                RedisFuture<String> results = commandAsync.lpop(params[0]);
                try {
                    String res = results.get(expireTimeLong, TimeUnit.SECONDS);
                    lcr.setResult(res);
                    lcr.setRunState(true);
                }
                catch (Exception e) {
                    lcr.setRunState(false);
                }
                return lcr;
            }
        },
        LLEN() {

            @Override
            public Object send(String[] params) {
                // 同步异步均需返回结果
                LettuceCommandResult lcr = new LettuceCommandResult();
                RedisAdvancedClusterAsyncCommands<String, String> commandAsync = connect.async();
                try {
                    RedisFuture<Long> result = commandAsync.llen(params[0]);
                    long res = result.get(expireTimeLong, TimeUnit.SECONDS);
                    lcr.setResult((int) res);
                    lcr.setRunState(true);
                }
                catch (Exception e) {
                    lcr.setResult(0);
                    lcr.setRunState(false);
                }
                return lcr;
            }
        },
        LINDEX() {

            @Override
            public Object send(String[] params) {
                // 同步异步均需返回结果
                LettuceCommandResult lcr = new LettuceCommandResult();
                RedisAdvancedClusterAsyncCommands<String, String> commandAsync = connect.async();
                RedisFuture<String> result = commandAsync.lindex(params[0], Long.parseLong(params[1]));
                String res = "";
                try {
                    res = result.get(expireTimeLong, TimeUnit.SECONDS);
                    lcr.setRunState(true);
                }
                catch (Exception e) {
                    lcr.setRunState(false);
                }
                lcr.setResult(res);
                return lcr;
            }
        },
        INCR() {

            @Override
            public Object send(String[] params) {
                // 只有同步操作
                LettuceCommandResult lcr = new LettuceCommandResult();
                RedisAdvancedClusterAsyncCommands<String, String> commandAsync = connect.async();
                RedisFuture<Long> result = commandAsync.incr(params[0]);
                String res = "";
                try {
                    res = String.valueOf(result.get(expireTimeLong, TimeUnit.SECONDS));
                    lcr.setRunState(true);
                }
                catch (Exception e) {
                    lcr.setRunState(false);
                }
                lcr.setResult(res);
                return lcr;
            }
        },

        DECR() {

            @Override
            public Object send(String[] params) {
                // 只有同步操作
                LettuceCommandResult lcr = new LettuceCommandResult();
                RedisAdvancedClusterAsyncCommands<String, String> commandAsync = connect.async();
                RedisFuture<Long> result = commandAsync.decr(params[0]);
                String res = "";
                try {
                    res = String.valueOf(result.get(expireTimeLong, TimeUnit.SECONDS));
                    lcr.setRunState(true);
                }
                catch (Exception e) {
                    lcr.setRunState(false);
                }
                lcr.setResult(res);
                return lcr;

            }
        },
        HMSET() {

            @Override
            public Object send(String[] params) {
                // 只有异步
                Map<String, String> mapValues = new HashMap<String, String>();
                for (int i = 1; i < params.length; i++) {
                    mapValues.put(params[i].toString(), params[i + 1].toString());
                    i++;
                }
                LettuceCommandResult lcr = new LettuceCommandResult();
                RedisAdvancedClusterAsyncCommands<String, String> commands = connect.async();

                try {
                    RedisFuture<String> result = commands.hmset(params[0], mapValues);
                    String res = result.get(expireTimeLong, TimeUnit.SECONDS);
                    lcr.setResult(result);
                    lcr.setRunState(res.equals("OK"));
                }
                catch (Exception e) {
                    lcr.setRunState(false);
                }
                return lcr;
            }
        },
        HMGET() {

            @Override
            public Object send(String[] params) {
                // 同步与异步均需要返回结果

                LettuceCommandResult lcr = new LettuceCommandResult();
                RedisAdvancedClusterAsyncCommands<String, String> commandAsync = connect.async();
                RedisFuture<List<KeyValue<String, String>>> result = commandAsync.hmget(params[0],
                        Arrays.copyOfRange(params, 1, params.length));
                Map<String, String> results = new HashMap<String, String>();
                List<KeyValue<String, String>> resultMap = null;
                try {
                    resultMap = result.get(expireTimeLong, TimeUnit.SECONDS);
                }
                catch (Exception e) {
                    lcr.setRunState(false);
                    return lcr;
                }

                for (int i = 0; i < resultMap.size(); i++) {
                    if (resultMap.get(i).hasValue() == true) {
                        results.put(resultMap.get(i).getKey(), resultMap.get(i).getValue());
                    }
                }
                lcr.setResult(results);
                lcr.setRunState(true);
                return lcr;
            }

        },
        EXPIRE() {

            @Override
            public Object send(String[] params) {
                // 只有异步不需要返回结果
                LettuceCommandResult lcr = new LettuceCommandResult();
                RedisAdvancedClusterAsyncCommands<String, String> commandAsync = connect.async();
                try {

                    RedisFuture<Boolean> result = commandAsync.expire(params[0], Long.parseLong(params[1]));
                    lcr.setResult(result);
                    lcr.setRunState(true);
                }
                catch (Exception e) {
                    lcr.setRunState(false);
                }

                return lcr;
            }
        },
        DEL() {

            @Override
            public Object send(String[] params) {
                // 只有异步不需要返回结果

                LettuceCommandResult lcr = new LettuceCommandResult();
                RedisAdvancedClusterAsyncCommands<String, String> commandAsync = connect.async();

                try {
                    RedisFuture<Long> result = commandAsync.del(params[0]);
                    lcr.setResult(result);
                    lcr.setRunState(true);
                }
                catch (Exception e) {
                    lcr.setRunState(false);
                }

                return lcr;
            }
        },
        GET() {

            @Override
            public Object send(String[] params) {
                // 同步异步都需要返回结果
                LettuceCommandResult lcr = new LettuceCommandResult();
                RedisAdvancedClusterAsyncCommands<String, String> commands = connect.async();
                RedisFuture<String> resultSync = commands.get(params[0]);
                String result = null;
                try {
                    result = resultSync.get(expireTimeLong, TimeUnit.SECONDS);
                    lcr.setResult(result);
                    lcr.setRunState(true);
                }
                catch (Exception e) {
                    lcr.setRunState(false);
                }

                return lcr;
            }
        },
        EXISTS() {

            @Override
            public Object send(String[] params) {
                // 只有同步
                LettuceCommandResult lcr = new LettuceCommandResult();
                RedisAdvancedClusterAsyncCommands<String, String> commandAsync = connect.async();
                RedisFuture<Long> result = commandAsync.exists(params[0]);
                String res = "";
                try {
                    res = String.valueOf(result.get(expireTimeLong, TimeUnit.SECONDS));
                    lcr.setRunState(true);
                }
                catch (Exception e) {
                    lcr.setRunState(false);
                }
                lcr.setResult(res);
                return lcr;

            }

        },
        SET() {

            @Override
            public Object send(String[] params) {
                // 异步不需要结果
                LettuceCommandResult lcr = new LettuceCommandResult();
                RedisAdvancedClusterAsyncCommands<String, String> commandAsync = connect.async();

                try {
                    RedisFuture<String> result = commandAsync.set(params[0], params[1]);
                    lcr.setResult(result);
                    lcr.setRunState(true);
                }
                catch (Exception e) {
                    lcr.setRunState(false);
                }
                return lcr;
            }

        };

        private static Map<String, EnumMethod> map = new HashMap<String, EnumMethod>();

        static {
            for (EnumMethod legEnum : EnumMethod.values()) {
                map.put(legEnum.name(), legEnum);
            }
        }

        public static EnumMethod getMethod(String symbol) {
            return map.get(symbol);
        }

        public abstract Object send(String[] params);

    }

    /**
     * @param redisServerAddress
     * @param minConcurrent
     * @param maxConcurrent
     * @param queueSize
     * @param password
     */
    public LettuceAsyncService(String redisServerAddress, int minConcurrent, int maxConcurrent, int queueSize) {
        this(redisServerAddress, minConcurrent, maxConcurrent, queueSize, null);
    }

    public LettuceAsyncService(String redisServerAddress, int minConcurrent, int maxConcurrent, int queueSize,
            String password) {
        String[] redisCluster = redisServerAddress.split(",");
        List<RedisURI> nodes = new ArrayList<RedisURI>();
        for (int i = 0; i < redisCluster.length; i++) {
            String[] uriArray = redisCluster[i].split(":");
            Integer port = Integer.valueOf(uriArray[1]);
            if (!StringHelper.isEmpty(password)) {
                nodes.add(RedisURI.Builder.redis(uriArray[0], port).withPassword(password).build());
            }
            else {
                nodes.add(RedisURI.create(uriArray[0], port));
            }
        }

        client = RedisClusterClient.create(nodes);

        connect = client.connect();

    }

    @Override
    public void start() {
        // Do nothing but must pass sonar check

    }

    @Override
    public void shutdown() {
        connect.close();
        client.shutdown();
    }

    static class LettuceCommandResult {

        private Object result;
        private boolean runState;

        /**
         * @return the result
         */
        public Object getResult() {
            return result;
        }

        /**
         * @param result
         *            the result to set
         */
        public void setResult(Object result) {
            this.result = result;
        }

        /**
         * @return the runState
         */
        public boolean isRunState() {
            return runState;
        }

        /**
         * @param runState
         *            the runState to set
         */
        public void setRunState(boolean runState) {
            this.runState = runState;
        }

    }

    @Override
    public Object[] submitCommands(CommandInfo... commands) {
        Object[] result = new Object[commands.length];
        for (int i = 0; i < commands.length; i++) {
            LettuceCommandResult lcr = (LettuceCommandResult) EnumMethod.getMethod(commands[i].getCommand().name())
                    .send(commands[i].getParam());
            result[i] = lcr.getResult();

        }
        return result;
    }

    @Override
    public void submitCommands(AbstractAsyncHandler<CommandInfo> handler, CommandInfo... commands) {
        Object[] infos = new Object[commands.length];
        for (int i = 0; i < commands.length; i++) {
            LettuceCommandResult lcr = null;
            try {
                lcr = (LettuceCommandResult) EnumMethod.getMethod(commands[i].getCommand().name())
                        .send(commands[i].getParam());
                infos[i] = lcr.getResult();
                commands[i].setState(lcr.isRunState());
            }
            catch (Exception e) {
                infos[i] = null;
                commands[i].setState(false);
            }

        }
        handler.process(commands, infos, null);
    }

}
