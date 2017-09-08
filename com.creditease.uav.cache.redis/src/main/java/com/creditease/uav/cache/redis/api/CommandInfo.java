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

package com.creditease.uav.cache.redis.api;

public class CommandInfo {

    public enum RedisCommand {
        APPEND, AUTH, BGREWRITEAOF, BGSAVE, BITCOUNT, BITOP, BITPOS, BLPOP, BRPOP, BRPOPLPUSH, CLIENT, CONFIG, DBSIZE, DECR, DECRBY, DEL, DISCARD, DUMP, ECHO, EVAL, EVALCHECK, EVALSHA, EXEC, EXISTS, EXPIRE, EXPIREAT, FLUSHALL, FLUSHDB, GET, GETBIT, GETRANGE, GETSET, HDEL, HEXISTS, HGET, HGETALL, HINCRBY, HINCRBYFLOAT, HKEYS, HLEN, HMGET, HMSET, HSCAN, HSET, HSETNX, HVALS, INCR, INCRBY, INCRBYFLOAT, INFO, KEYS, LASTSAVE, LINDEX, LINSERT, LLEN, LPOP, LPUSH, LPUSHX, LRANGE, LREM, LSET, LTRIM, MGET, MIGRATE, MOVE, MSET, MSETNX, MULTI, OBJECT, PERSIST, PEXPIRE, PEXPIREAT, PFADD, PFCOUNT, PFMERGE, PING, PSETEX, PSUBSCRIBE, PTTL, PUBLISH, PUBSUB, PUNSUBSCRIBE, RANDOMKEY, RENAME, RENAMENX, RESTORE, RPOP, RPOPLPUSH, RPUSH, RPUSHX, SADD, SAVE, SCAN, SCARD, SCRIPT, SDIFF, SDIFFSTORE, SELECT, SET, SETBIT, SETEX, SETNX, SETRANGE, SHUTDOWN, SINTER, SINTERSTORE, SISMEMBER, SLAVEOF, SLOWLOG, SMEMBERS, SMOVE, SORT, SPOP, SRANDMEMBER, SREM, SSCAN, STRLEN, SUBSCRIBE, SUNION, SUNIONSTORE, TIME, TTL, TYPE, UNSUBSCRIBE, UNWATCH, WATCH, ZADD, ZCARD, ZCOUNT, ZINCRBY, ZINTERSTORE, ZLEXCOUNT, ZRANGE, ZRANGEBYLEX, ZRANGEBYSCORE, ZRANK, ZREM, ZREMRANGEBYLEX, ZREMRANGEBYRANK, ZREMRANGEBYSCORE, ZREVRANGE, ZREVRANGEBYSCORE, ZREVRANK, ZSCAN, ZSCORE, ZUNIONSTORE
    }

    private RedisCommand command;

    private String[] params;

    // true: means operation is done successfully
    private boolean isSuccess;

    public boolean isSuccess() {

        return isSuccess;
    }

    public void setState(boolean state) {

        this.isSuccess = state;
    }

    public CommandInfo(RedisCommand com, String... param) {
        this.command = com;
        this.params = param;
    }

    public RedisCommand getCommand() {

        return command;
    }

    public void setCommand(RedisCommand command) {

        this.command = command;
    }

    public String[] getParam() {

        return params;
    }

    public void setParam(String... params) {

        this.params = params;
    }
}
