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

package com.creditease.uav.cache.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.ThreadHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.uav.cache.redis.api.AbstractAsyncHandler;
import com.creditease.uav.cache.redis.api.CacheFactory;
import com.creditease.uav.cache.redis.api.CacheFactory.CacheClientMode;
import com.creditease.uav.cache.redis.api.CacheService;
import com.creditease.uav.cache.redis.api.CommandInfo;

/**
 * CacheManager是缓存管理类， 提供了3种数据类型的存储和读取 1. String：存储是string，读取string 2. JSONString：存储是string，读取Map<String,String>或Class
 * 3. Hash值：存储hashs，读取Map<String,String>值对
 *
 * CacheManager提供了同步和异步方法 1. 所有写操作都是异步 2. 读取操作有同步/异步(带AsyncCacheCallback)
 *
 * CacheManager支持批量操作（只支持异步方法的批量） 只需要在批量开始调用beginBatch，批量结束调用submitBatch，中间按照正常方式调用异步方法即可 例子： //开始batch
 * CacheManager.instance().beginBatch();
 * 
 * CacheManager.instance().put("test", "key", "this is a test"); CacheManager.instance().get("test", "key", new
 * AsyncCacheCallback<String>(){
 * 
 * @Override public void onResult(String t) { System.out.println("3"+t); } });
 * 
 *           CacheManager.instance().putJSON("test","key1",tm); CacheManager.instance().getJSON("test","key1", new
 *           AsyncCacheCallback<Map<String,String>>(){
 * 
 * @Override public void onResult(Map<String,String> t) { System.out.println("3"+JSONHelper.toString(t)); } });
 * 
 *           CacheManager.instance().putJSON("test","key2", pu); CacheManager.instance().getJSON("test","key2", new
 *           AsyncCacheCallback<PublicUser>(){
 * 
 * @Override public void onResult(PublicUser t) { System.out.println("3"+JSONHelper.toString(t)); } },PublicUser.class);
 * 
 *           //提交batch CacheManager.instance().submitBatch();
 *
 * @author zhenzhang
 *
 */
public class CacheManager {

    /**
     * 
     * CacheLock description: 多活流程控制锁
     *
     */
    public class CacheLock {

        private static final String LOCK_RELEASE = "OPEN";

        private String lockName;

        private String lockRegion;

        private CacheManager cm;

        private long lockTimeout;

        private long curTimestamp = -1;

        public CacheLock(String lockRegion, String lockName, long lockTimeout, CacheManager cm) {
            this.lockRegion = lockRegion;
            this.lockName = lockName;
            this.lockTimeout = lockTimeout;
            this.cm = cm;
        }

        public boolean getLock() {

            curTimestamp = System.currentTimeMillis();

            String value = cm.get(lockRegion, lockName);

            if (value != null && !LOCK_RELEASE.equals(value)) {

                try {
                    if (curTimestamp - Long.parseLong(value) < this.lockTimeout) {
                        return false;
                    }
                }
                catch (NumberFormatException e) {
                    return false;
                }
            }

            cm.put(lockRegion, lockName, String.valueOf(curTimestamp));

            ThreadHelper.suspend(50);

            return isLockInHand();
        }

        public void releaseLock() {

            if (isLockInHand()) {
                cm.put(lockRegion, lockName, LOCK_RELEASE);
            }
            curTimestamp = -1;
        }

        public boolean isLockInHand() {

            String value = cm.get(lockRegion, lockName);
            return String.valueOf(curTimestamp).equals(value);
        }
    }

    enum L1CacheState {
        NOEXISTS, GOOD, EXPIRED
    }

    class L1CacheObj {

        private Object obj;
        private L1CacheState state;

        public L1CacheObj(Object obj, L1CacheState state) {
            this.obj = obj;
            this.state = state;
        }

        public Object getObj() {

            return obj;
        }

        public L1CacheState getState() {

            return state;
        }
    }

    /**
     * L1Cache 读缓存，对读操作可以在jvm heap中缓存数据 适用数据类型： 1）使用频率特别高 2）单个数据量不大 3）数据的属性改变频率很低
     *
     */
    class L1Cache {

        Map<String, Integer> cacheToken = new ConcurrentHashMap<String, Integer>();
        Map<String, Long> cacheExpireSet = new ConcurrentHashMap<String, Long>();
        Map<String, Object> cacheObjs = new ConcurrentHashMap<String, Object>();

        /**
         * 注册需要L1Cache的Key
         * 
         * @param key
         * @param expireMins
         *            过期时间，当过期时间抵达时会主动去从缓存中取值
         */
        public void register(String key, int expireSecs) {

            if (expireSecs <= 0) {
                expireSecs = 300;
            }

            this.cacheToken.put(key, expireSecs);
        }

        /**
         * 取消L1Cache的Key
         * 
         * @param key
         */
        public void unregister(String key) {

            this.cacheToken.remove(key);
            this.cacheExpireSet.remove(key);
            this.cacheObjs.remove(key);
        }

        /**
         * 存放L1 Cache
         * 
         * @param key
         * @param obj
         */
        public void put(String key, Object obj) {

            if (null == key || null == obj) {
                return;
            }
            if (this.cacheToken.containsKey(key)) {
                this.cacheObjs.put(key, obj);
                this.cacheExpireSet.put(key, System.currentTimeMillis() + this.cacheToken.get(key) * 1000);
            }
        }

        /**
         * 过期expire
         * 
         * @param key
         * @param exp
         */
        public void expire(String key, long exp) {

            if (null == key) {
                return;
            }
            if (this.cacheToken.containsKey(key) && this.cacheObjs.containsKey(key)) {
                this.cacheExpireSet.put(key, System.currentTimeMillis() + exp);
            }
        }

        /**
         * 删除L1 Cache
         * 
         * @param key
         */
        public void del(String key) {

            if (null == key) {
                return;
            }
            if (this.cacheToken.containsKey(key)) {
                this.cacheObjs.remove(key);
                this.cacheExpireSet.remove(key);
            }
        }

        /**
         * 检测L1 Cache是否存在
         * 
         * @param key
         * @return
         */
        public boolean exists(String key) {

            if (null == key) {
                return false;
            }
            if (!this.cacheToken.containsKey(key)) {
                return false;
            }
            Long expire = this.cacheExpireSet.get(key);
            if (expire == null) {
                return false;
            }
            if (expire - System.currentTimeMillis() > 0) {
                return true;
            }
            else {
                this.cacheObjs.remove(key);
                this.cacheExpireSet.remove(key);
                return false;
            }
        }

        /**
         * 取L1 Cache
         * 
         * @param key
         * @return
         */
        public L1CacheObj get(String key) {

            Long expire = this.cacheExpireSet.get(key);
            /**
             * CASE 1：不存在L1 Cache
             */
            if (expire == null) {
                return new L1CacheObj(null, L1CacheState.NOEXISTS);
            }
            /**
             * CASE 2: 存在L1 Cache，且米有过期
             */
            if (expire - System.currentTimeMillis() > 0) {
                return new L1CacheObj(this.cacheObjs.get(key), L1CacheState.GOOD);
            }
            /**
             * CASE 3: 存在L1 Cache，但过期了
             */
            else {
                this.cacheObjs.remove(key);
                this.cacheExpireSet.remove(key);
                return new L1CacheObj(null, L1CacheState.EXPIRED);
            }
        }

        /**
         * 释放L1 Cache
         */
        public void release() {

            this.cacheToken.clear();
            this.cacheObjs.clear();
            this.cacheExpireSet.clear();
        }

        /**
         * 获取L1 Cache的对象数目
         * 
         * @return
         */
        public int getCacheObjectCount() {

            return this.cacheObjs.size();
        }
    }

    /**
     * 批量操作Context
     * 
     * @author zhenzhang
     *
     */
    @SuppressWarnings("rawtypes")
    class BatchContext {

        Map<CommandInfo, AbstractAsyncHandler<CommandInfo>> commands = new LinkedHashMap<CommandInfo, AbstractAsyncHandler<CommandInfo>>();

        public void addCommand(CommandInfo c, AbstractAsyncHandler<CommandInfo> cbk) {

            if (null == c) {
                return;
            }

            if (null == cbk) {
                commands.put(c, new AbstractAsyncHandler<CommandInfo>() {

                    @Override
                    public void process(CommandInfo[] command, Object[] result, Throwable throwable) {

                        // Do nothing but must pass sonar check
                    }
                });
            }
            else {
                commands.put(c, cbk);
            }
        }

        public AbstractAsyncHandler getAsyncCacheCallback(CommandInfo c) {

            if (null == c) {
                return null;
            }
            return commands.get(c);
        }

        public CommandInfo[] getCommands() {

            CommandInfo[] cmds = new CommandInfo[commands.size()];

            commands.keySet().toArray(cmds);

            return cmds;
        }
    }

    private static ThreadLocal<BatchContext> threadBatchContext = new ThreadLocal<BatchContext>();

    protected static final ISystemLogger logger = SystemLogger.getLogger(CacheManager.class);

    private static CacheManager cacheMgr = null;

    /**
     * 需先调用build方法，方能使用
     * 
     * @return
     */
    public static CacheManager instance() {

        return cacheMgr;
    }

    /**
     * 需要先初始化方能使用instance()
     * 
     * @param cacheServerAddress
     * @param minConcurrent
     * @param maxConcurrent
     * @param queueSize
     * @param password
     */
    public static void build(String cacheServerAddress, int minConcurrent, int maxConcurrent, int queueSize,
            String password) {

        if (cacheMgr == null) {
            cacheMgr = new CacheManager(cacheServerAddress, minConcurrent, maxConcurrent, queueSize, password);
        }
    }

    /**
     * 需要先初始化方能使用instance()
     * 
     * @param cacheServerAddress
     * @param minConcurrent
     * @param maxConcurrent
     * @param queueSize
     */
    public static void build(String cacheServerAddress, int minConcurrent, int maxConcurrent, int queueSize) {

        build(cacheServerAddress, minConcurrent, maxConcurrent, queueSize, null);
    }

    private CacheService service;

    private L1Cache l1cache;
    
    private static CacheClientMode clientMode;
    
    protected CacheManager(String cacheServerAddress, int minConcurrent, int maxConcurrent, int queueSize,
            String password) {
    	
    	if (cacheServerAddress.contains(",")) {
           clientMode = CacheClientMode.LETTUCE;
        }
        else {
           clientMode = CacheClientMode.AREDIS;
        }
    	
        service = CacheFactory.instance().createCacheService(clientMode, cacheServerAddress, minConcurrent,
                maxConcurrent, queueSize, password);
        l1cache = new L1Cache();
    }

    protected CacheManager(String cacheServerAddress, int minConcurrent, int maxConcurrent, int queueSize) {
        this(cacheServerAddress, minConcurrent, maxConcurrent, queueSize, null);
    }

    /**
     * new CacheLock
     * 
     * @param lockRegion
     * @param lockName
     * @param lockTimeout
     * @return
     */
    public CacheLock newCacheLock(String lockRegion, String lockName, long lockTimeout) {

        return new CacheLock(lockRegion, lockName, lockTimeout, this);
    }

    private String getRedisKey(String region, String key) {

        return "CACHE:" + region + ":" + key;
    }

    /**
     * 注册需要L1 Cache的Key
     * 
     * @param region
     * @param key
     * @param expireSeconds
     *            以分钟为秒，抵达过期时限时会主动从remote缓存拉去值
     */
    public void enableL1Cache(String region, String key, int expireSeconds) {

        String rkey = getRedisKey(region, key);

        this.l1cache.register(rkey, expireSeconds);
    }

    /**
     * 取消L1 Cache
     * 
     * @param region
     * @param key
     */
    public void disableL1Cache(String region, String key) {

        String rkey = getRedisKey(region, key);

        this.l1cache.unregister(rkey);
    }

    /**
     * 获取L1Cache的个数
     * 
     * @return
     */
    public int getL1CacheCount() {

        return this.l1cache.getCacheObjectCount();
    }

    /**
     * 判断一个缓存对象是否存在 SYNC操作
     * 
     * @param region
     * @param key
     * @return
     */
    public boolean exists(String region, String key) {

        String rkey = getRedisKey(region, key);

        /**
         * 先检查是否有L1Cache, 如果存在且没过期则返回true
         */
        if (this.l1cache.exists(rkey) == true) {
            return true;
        }

        Object[] results = service.submitCommands(new CommandInfo(CommandInfo.RedisCommand.EXISTS, rkey));

        boolean check = false;

        if (null != results && results.length > 0 && results[0] != null
                && "1".equalsIgnoreCase(results[0].toString())) {
            check = true;
        }

        return check;
    }

    /**
     * 删除一个缓存对象 默认ASYNC操作
     * 
     * @param region
     * @param key
     */
    public void del(String region, String key) {

        del(region, key, null);
    }

    /**
     * 默认ASYNC操作
     * 
     * @param region
     * @param key
     * @param handler
     */
    public void del(String region, String key, final AsyncCacheCallback<Boolean> handler) {

        final String rkey = getRedisKey(region, key);
        final L1Cache fl1cache = this.l1cache;
        CommandInfo ci = new CommandInfo(CommandInfo.RedisCommand.DEL, rkey);
        doCacheCommand(ci, new AbstractAsyncHandler<CommandInfo>() {

            @Override
            public void process(CommandInfo[] command, Object[] result, Throwable throwable) {

                boolean check = command[0].isSuccess();
                /**
                 * 如果远程删除，L1Cache也应该删除
                 */
                if (check == true) {
                    fl1cache.del(rkey);
                }
                if (handler == null) {
                    return;
                }
                handler.onResult(check);
            }
        });

    }

    @SuppressWarnings("unchecked")
    private void doCacheCommand(CommandInfo ci, @SuppressWarnings("rawtypes") AbstractAsyncHandler handler) {

        BatchContext bc = threadBatchContext.get();
        if (bc != null) {
            bc.addCommand(ci, handler);
        }
        else {
            service.submitCommands(handler, ci);
        }
    }

    /**
     * 设置过期时间 默认ASYNC操作
     * 
     * @param region
     * @param key
     * @param expireTime
     * @param tu
     */
    public void expire(String region, String key, long expireTime, TimeUnit tu) {

        expire(region, key, expireTime, tu, null);
    }

    /**
     * 设置过期时间 ASYNC操作
     * 
     * @param region
     * @param key
     * @param expireTime
     * @param tu
     * @param handler
     */
    public void expire(String region, String key, long expireTime, TimeUnit tu,
            final AsyncCacheCallback<Boolean> handler) {

        if (expireTime < 0)
            return;

        final String rkey = getRedisKey(region, key);

        final L1Cache fl1cache = this.l1cache;

        final long expireTimeLong;

        switch (tu) {
            case DAYS:
                expireTimeLong = expireTime * 3600 * 24;
                break;
            case HOURS:
                expireTimeLong = expireTime * 3600;
                break;
            case MICROSECONDS:
                expireTimeLong = expireTime / 1000000;
                break;
            case MILLISECONDS:
                expireTimeLong = expireTime / 1000;
                break;
            case MINUTES:
                expireTimeLong = expireTime * 60;
                break;
            case NANOSECONDS:
                expireTimeLong = expireTime / 1000000000;
                break;
            case SECONDS:
            default:
                expireTimeLong = expireTime;
                break;
        }

        CommandInfo ci = new CommandInfo(CommandInfo.RedisCommand.EXPIRE, rkey, String.valueOf(expireTimeLong));

        doCacheCommand(ci, new AbstractAsyncHandler<CommandInfo>() {

            @Override
            public void process(CommandInfo[] command, Object[] result, Throwable throwable) {

                boolean isOK = command[0].isSuccess();
                /**
                 * 如果某个Key被设置了expire，则L1Cache也应该设置相同的expire
                 */
                if (isOK) {
                    fl1cache.expire(rkey, expireTimeLong);
                }
                if (handler == null) {
                    return;
                }
                handler.onResult(isOK);
            }
        });
    }

    /**
     * 缓存类型 1：存储string 取值string
     */
    /**
     * 默认ASYNC操作
     * 
     * @param region
     * @param key
     * @param value
     */
    public void put(String region, String key, String value) {

        put(region, key, value, null);
    }

    /**
     * 默认ASYNC操作
     * 
     * @param region
     * @param key
     * @param value
     * @param handler
     */
    public void put(String region, String key, final String value, final AsyncCacheCallback<Boolean> handler) {

        final String rkey = getRedisKey(region, key);

        final L1Cache fl1cache = this.l1cache;

        CommandInfo ci = new CommandInfo(CommandInfo.RedisCommand.SET, rkey, value);

        doCacheCommand(ci, new AbstractAsyncHandler<CommandInfo>() {

            @Override
            public void process(CommandInfo[] command, Object[] result, Throwable throwable) {

                if (handler == null)
                    return;
                boolean check = command[0].isSuccess();
                /**
                 * 如果写远程成功，则更新
                 */
                if (check == true) {
                    fl1cache.put(rkey, value);
                }
                handler.onResult(check);
            }
        });
    }

    /**
     * BATCH操作
     * 
     * @param region
     * @param key
     * @param value
     * @param expireTime
     */
    public void put(String region, String key, String value, long expireTime, TimeUnit tu) {

        this.beginBatch();
        this.put(region, key, value);
        this.expire(region, key, expireTime, tu);
        this.submitBatch();
    }

    /**
     * SYNC操作
     * 
     * @param region
     * @param key
     * @return
     */
    public String get(String region, String key) {

        String rkey = getRedisKey(region, key);

        L1CacheObj l1c = this.l1cache.get(rkey);

        if (l1c.state == L1CacheState.GOOD) {
            return (String) l1c.getObj();
        }

        CommandInfo ci = new CommandInfo(CommandInfo.RedisCommand.GET, rkey);

        Object[] results = service.submitCommands(ci);

        if (null != results && results.length > 0) {

            String value = (String) results[0];

            this.l1cache.put(rkey, value);

            return value;
        }

        return null;
    }

    /**
     * ASYNC操作
     * 
     * @param region
     * @param key
     * @param callback
     */
    public void get(String region, String key, AsyncCacheCallback<String> callback) {

        if (null == callback)
            return;

        final String rkey = getRedisKey(region, key);

        L1CacheObj l1c = this.l1cache.get(rkey);

        if (l1c.state == L1CacheState.GOOD) {
            callback.onResult((String) l1c.getObj());
            return;
        }

        final AsyncCacheCallback<String> fcallback = callback;

        final L1Cache fl1cache = l1cache;

        CommandInfo ci = new CommandInfo(CommandInfo.RedisCommand.GET, rkey);

        doCacheCommand(ci, new AbstractAsyncHandler<CommandInfo>() {

            @Override
            public void process(CommandInfo[] command, Object[] result, Throwable throwable) {

                if (fcallback != null && null != result && result.length > 0) {
                    String value = (String) result[0];

                    fl1cache.put(rkey, value);

                    fcallback.onResult(value);
                }
            }

        });
    }

    /**
     * 缓存类型 2：存储jsonString 取值Map<String,String>
     */
    /**
     * ASYNC操作
     * 
     * @param region
     * @param key
     * @param mObj
     */

    public void putJSON(String region, String key, Map<String, String> mObj) {

        putJSON(region, key, mObj, null);
    }

    /**
     * ASYNC操作
     * 
     * @param region
     * @param key
     * @param mObj
     * @param handler
     */
    public void putJSON(String region, String key, final Map<String, String> mObj,
            final AsyncCacheCallback<Boolean> handler) {

        if (null == mObj || mObj.size() == 0)
            return;

        final String rkey = getRedisKey(region, key);

        final L1Cache fl1cache = this.l1cache;

        CommandInfo ci = new CommandInfo(CommandInfo.RedisCommand.SET, rkey, JSONHelper.toString(mObj));

        doCacheCommand(ci, new AbstractAsyncHandler<CommandInfo>() {

            @Override
            public void process(CommandInfo[] command, Object[] result, Throwable throwable) {

                boolean check = command[0].isSuccess();
                /**
                 * 如果远程调用成功, 则放L1Cache
                 */
                if (check) {
                    fl1cache.put(rkey, mObj);
                }
                if (handler == null) {
                    return;
                }
                handler.onResult(check);
            }

        });
    }

    /**
     * BATCH操作
     * 
     * @param region
     * @param key
     * @param mObj
     * @param expireTime
     */
    public void putJSON(String region, String key, Map<String, String> mObj, long expireTime, TimeUnit tu) {

        this.beginBatch();
        this.putJSON(region, key, mObj);
        this.expire(region, key, expireTime, tu);
        this.submitBatch();
    }

    /**
     * ASYNC操作
     * 
     * @param region
     * @param key
     * @param t
     */
    public <T> void putJSON(String region, String key, T t) {

        putJSON(region, key, t, null);
    }

    /**
     * ASYNC操作
     * 
     * @param region
     * @param key
     * @param t
     * @param handler
     */
    public <T> void putJSON(String region, String key, final T t, final AsyncCacheCallback<Boolean> handler) {

        if (null == t)
            return;

        final String rkey = getRedisKey(region, key);

        final L1Cache fl1cache = this.l1cache;

        service.submitCommands(new AbstractAsyncHandler<CommandInfo>() {

            @Override
            public void process(CommandInfo[] command, Object[] result, Throwable throwable) {

                boolean check = command[0].isSuccess();
                /**
                 * 如果远程调用成功, 则放L1Cache
                 */
                if (check) {
                    fl1cache.put(rkey, t);
                }
                if (handler == null)
                    return;
                handler.onResult(check);
            }
        }, new CommandInfo(CommandInfo.RedisCommand.SET, rkey, JSONHelper.toString(t)));
    }

    /**
     * BATCH操作
     * 
     * @param region
     * @param key
     * @param t
     * @param expireTime
     * @param tu
     */
    public <T> void putJSON(String region, String key, T t, long expireTime, TimeUnit tu) {

        this.beginBatch();
        this.putJSON(region, key, t);
        this.expire(region, key, expireTime, tu);
        this.submitBatch();
    }

    /**
     * SYNC操作
     * 
     * @param region
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getJSON(String region, String key) {

        String rkey = getRedisKey(region, key);

        L1CacheObj l1c = this.l1cache.get(rkey);

        if (l1c.state == L1CacheState.GOOD) {
            return (Map<String, String>) l1c.getObj();
        }

        Object[] results = service.submitCommands(new CommandInfo(CommandInfo.RedisCommand.GET, rkey));

        if (null != results && results.length > 0) {

            Map<String, String> value = JSONHelper.toObject((String) results[0], Map.class);

            this.l1cache.put(rkey, value);

            return value;
        }

        return Collections.emptyMap();
    }

    /**
     *
     * @param region
     * @param key
     * @param c
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getJSON(String region, String key, Class<T> c) {

        if (c == null)
            return null;

        String rkey = getRedisKey(region, key);

        L1CacheObj l1c = this.l1cache.get(rkey);

        if (l1c.state == L1CacheState.GOOD) {
            return (T) l1c.getObj();
        }

        Object[] results = service.submitCommands(new CommandInfo(CommandInfo.RedisCommand.GET, rkey));

        if (null != results && results.length > 0) {

            T value = JSONHelper.toObject((String) results[0], c);

            this.l1cache.put(rkey, value);

            return value;
        }

        return null;
    }

    /**
     * ASYNC操作
     * 
     * @param region
     * @param key
     * @param callback
     */
    @SuppressWarnings("unchecked")
    public void getJSON(String region, String key, AsyncCacheCallback<Map<String, String>> callback) {

        if (null == callback) {
            return;
        }

        final String rkey = getRedisKey(region, key);

        L1CacheObj l1c = this.l1cache.get(rkey);

        if (l1c.state == L1CacheState.GOOD) {
            callback.onResult((Map<String, String>) l1c.getObj());
            return;
        }

        final L1Cache fl1cache = l1cache;

        final AsyncCacheCallback<Map<String, String>> fcallback = callback;

        CommandInfo ci = new CommandInfo(CommandInfo.RedisCommand.GET, rkey);

        doCacheCommand(ci, new AbstractAsyncHandler<CommandInfo>() {

            @Override
            public void process(CommandInfo[] command, Object[] result, Throwable throwable) {

                if (fcallback != null && null != result && result.length > 0) {
                    Map<String, String> value = JSONHelper.toObject((String) result[0], Map.class);

                    fl1cache.put(rkey, value);

                    fcallback.onResult(value);
                }
            }

        });
    }

    /**
     * ASYNC操作
     * 
     * @param region
     * @param key
     * @param callback
     * @param c
     */
    @SuppressWarnings("unchecked")
    public <T> void getJSON(String region, String key, AsyncCacheCallback<T> callback, Class<T> c) {

        if (null == c || null == callback) {
            return;
        }

        final String rkey = getRedisKey(region, key);

        L1CacheObj l1c = this.l1cache.get(rkey);

        if (l1c.state == L1CacheState.GOOD) {
            callback.onResult((T) l1c.getObj());
            return;
        }

        final L1Cache fl1cache = l1cache;

        final AsyncCacheCallback<T> fcallback = callback;
        final Class<T> fc = c;

        CommandInfo ci = new CommandInfo(CommandInfo.RedisCommand.GET, rkey);

        doCacheCommand(ci, new AbstractAsyncHandler<CommandInfo>() {

            @Override
            public void process(CommandInfo[] command, Object[] result, Throwable throwable) {

                if (fcallback != null && null != result && result.length > 0) {

                    T value = JSONHelper.toObject((String) result[0], fc);

                    fl1cache.put(rkey, value);

                    fcallback.onResult(value);
                }
            }

        });
    }

    /**
     * 缓存类型 3：存储hashs, 取值是hashes的field
     */
    /**
     * ASYNC操作
     * 
     * @param region
     * @param key
     * @param fieldValues
     */
    public void putHash(String region, String key, Map<String, String> fieldValues) {

        putHash(region, key, fieldValues, null);
    }

    /**
     * ASYNC操作
     * 
     * @param region
     * @param key
     * @param fieldName
     * @param fieldValue
     */
    public void putHash(String region, String key, String fieldName, String fieldValue) {

        Map<String, String> fieldValues = new HashMap<String, String>();

        fieldValues.put(fieldName, fieldValue);

        putHash(region, key, fieldValues, null);
    }

    /**
     * ASYNC操作
     * 
     * @param region
     * @param key
     * @param fieldValues
     * @param handler
     */
    public void putHash(String region, String key, final Map<String, String> fieldValues,
            final AsyncCacheCallback<Boolean> handler) {

        if (null == fieldValues || fieldValues.size() == 0)
            return;

        final String rkey = getRedisKey(region, key);

        final L1Cache fl1cache = this.l1cache;

        Set<Entry<String, String>> sets = fieldValues.entrySet();

        String[] args = new String[sets.size() * 2 + 1];

        args[0] = rkey;

        int i = 1;
        for (Entry<String, String> entry : sets) {
            args[i] = entry.getKey();
            args[i + 1] = entry.getValue();
            i += 2;
        }

        CommandInfo ci = new CommandInfo(CommandInfo.RedisCommand.HMSET, args);

        doCacheCommand(ci, new AbstractAsyncHandler<CommandInfo>() {

            @Override
            public void process(CommandInfo[] command, Object[] result, Throwable throwable) {

                boolean check = command[0].isSuccess();
                if (check) {// 如果远程操作成功，则写L1Cache
                    L1CacheObj cobj = fl1cache.get(rkey);
                    switch (cobj.state) {
                        case EXPIRED:// 已经过期了，则什么都不做，等到下次使用getHashAll时会写入L1Cache
                            break;
                        case GOOD:
                            mergeMap(fieldValues, rkey, fl1cache, cobj);
                            break;
                        case NOEXISTS:// 都不存在，则什么都不做，等到下次使用getHashAll时会写入L1Cache
                            break;
                        default:// 都不存在，则什么都不做，等到下次使用getHashAll时会写入L1Cache
                            break;
                    }
                }
                if (handler == null)
                    return;
                handler.onResult(check);
            }

            /**
             * @param fieldValues
             * @param rkey
             * @param fl1cache
             * @param cobj
             */
            private void mergeMap(final Map<String, String> fieldValues, final String rkey, final L1Cache fl1cache,
                    L1CacheObj cobj) {

                @SuppressWarnings("unchecked")
                Map<String, String> map = (Map<String, String>) cobj.getObj();
                map.putAll(fieldValues);
                fl1cache.put(rkey, map);
            }
        });
    }

    /**
     * BATCH操作
     * 
     * @param region
     * @param key
     * @param fieldValues
     * @param expireTime
     */
    public void putHash(String region, String key, Map<String, String> fieldValues, long expireTime, TimeUnit tu) {

        this.beginBatch();
        this.putHash(region, key, fieldValues);
        this.expire(region, key, expireTime, tu);
        this.submitBatch();
    }

    /**
     * SYNC操作，不支持L1Cache
     * 
     * @param region
     * @param key
     * @param fieldNames
     * @return
     */
    @SuppressWarnings("unchecked")
	public Map<String, String> getHash(String region, String key, String... fieldNames) {

        if (null == fieldNames) {
            Collections.emptyMap();
        }

        String rkey = getRedisKey(region, key);

        String[] args = genHMGetArgs(rkey, fieldNames);

        Object[] results = service.submitCommands(new CommandInfo(CommandInfo.RedisCommand.HMGET, args));

        if (null != results && results.length > 0) {

        	Map<String, String> value = new HashMap<String, String>();
            if (clientMode.equals(CacheClientMode.LETTUCE) && results[0] instanceof Map) {
                value = (Map<String, String>) results[0];
            }
            else {
                value = genHMGetResults(fieldNames, results);
            }
            return value;
        }

        return Collections.emptyMap();
    }

    private String[] genHMGetArgs(String rkey, String... fieldNames) {

        if (null == fieldNames || fieldNames.length == 0)
            return new String[0];

        String[] args = new String[fieldNames.length + 1];

        args[0] = rkey;

        System.arraycopy(fieldNames, 0, args, 1, fieldNames.length);
        return args;
    }

    private Map<String, String> genHMGetResults(String[] fieldNames, Object[] results) {

        if (null == results || results.length == 0)
            return Collections.emptyMap();

        Map<String, String> m = new LinkedHashMap<String, String>();

        Object[] values = (Object[]) results[0];

        if (null != fieldNames && values != null) {
            for (int i = 0; i < values.length; i++) {
                m.put(fieldNames[i], (String) values[i]);
            }
        }
        else {

            if (values != null) {
                for (int i = 0; i < values.length; i += 2) {
                    m.put((String) values[i], (String) values[i + 1]);
                }
            }
        }

        return m;
    }

    /**
     * SYNC操作
     * 
     * @param region
     * @param key
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getHashAll(String region, String key) {

        String rkey = getRedisKey(region, key);

        L1CacheObj l1c = this.l1cache.get(rkey);

        if (l1c.state == L1CacheState.GOOD) {
            return (Map<String, String>) l1c.getObj();
        }

        Object[] results = service.submitCommands(new CommandInfo(CommandInfo.RedisCommand.HGETALL, rkey));

        if (null != results && results.length > 0) {

        	Map<String, String> value = new HashMap<String, String>();
            if (clientMode.equals(CacheClientMode.LETTUCE) && results[0] instanceof Map) {
                value = (Map<String, String>) results[0];
            }
            else {
                value = genHMGetResults(null, results);
            }
            this.l1cache.put(rkey, value);

            return value;
        }

        return Collections.emptyMap();
    }

    /**
     * ASYNC操作
     * 
     * @param region
     * @param key
     * @param callback
     */
    @SuppressWarnings("unchecked")
    public void getHashAll(String region, String key, AsyncCacheCallback<Map<String, String>> callback) {

        final String rkey = getRedisKey(region, key);

        L1CacheObj l1c = this.l1cache.get(rkey);

        if (l1c.state == L1CacheState.GOOD) {
            callback.onResult((Map<String, String>) l1c.getObj());
            return;
        }

        final L1Cache fl1cache = l1cache;

        final AsyncCacheCallback<Map<String, String>> fcallback = callback;

        CommandInfo ci = new CommandInfo(CommandInfo.RedisCommand.HGETALL, rkey);

        doCacheCommand(ci, new AbstractAsyncHandler<CommandInfo>() {

            @Override
            public void process(CommandInfo[] command, Object[] result, Throwable throwable) {

                if (fcallback != null && null != result && result.length > 0) {
                	Map<String, String> mresult = new HashMap<String, String>();

                    if (clientMode.equals(CacheClientMode.LETTUCE) && result[0] instanceof Map) {
                        mresult = (Map<String, String>) result[0];
                    }
                    else {
                        mresult = genHMGetResults(null, result);
                    }

                    /**
                     * get hash all是可以放L1Cache
                     */
                    fl1cache.put(rkey, mresult);

                    fcallback.onResult(mresult);
                }
            }

        });

    }

    /**
     * ASYNC操作，不支持L1Cache
     * 
     * @param region
     * @param key
     * @param fieldName
     * @param callback
     */
    public void getHash(String region, String key, AsyncCacheCallback<Map<String, String>> callback,
            String... fieldName) {

        if (null == callback) {
            return;
        }

        String rkey = getRedisKey(region, key);

        final AsyncCacheCallback<Map<String, String>> fcallback = callback;

        String[] args = genHMGetArgs(rkey, fieldName);
        final String[] ffieldname = fieldName;

        CommandInfo ci = new CommandInfo(CommandInfo.RedisCommand.HMGET, args);

        doCacheCommand(ci, new AbstractAsyncHandler<CommandInfo>() {

            @SuppressWarnings("unchecked")
			@Override
            public void process(CommandInfo[] command, Object[] result, Throwable throwable) {

            	if (fcallback != null && null != result && result.length > 0) {
                    Map<String, String> mresult = new HashMap<String, String>();
                    if (clientMode.equals(CacheClientMode.LETTUCE) && result[0] instanceof Map) {
                        mresult = (Map<String, String>) result[0];
                    }
                    else {
                        mresult = genHMGetResults(ffieldname, result);
                    }
                    fcallback.onResult(mresult);
                }
            }

        });
    }

    /**
     * 开始Batch操作,只能用于ASYNC操作
     */
    public void beginBatch() {

        BatchContext bc = new BatchContext();

        threadBatchContext.set(bc);
    }

    /**
     * 提交Batch操作,只能用于ASYNC操作
     *
     */
    public void submitBatch() {

        final BatchContext bc = threadBatchContext.get();
        threadBatchContext.remove();

        service.submitCommands(new AbstractAsyncHandler<CommandInfo>() {

            @SuppressWarnings("unchecked")
            @Override
            public void process(CommandInfo[] command, Object[] result, Throwable throwable) {

                int index = 0;
                for (CommandInfo c : command) {
                    AbstractAsyncHandler<CommandInfo> abk = bc.getAsyncCacheCallback(c);
                    if (abk != null) {
                        abk.process(new CommandInfo[] { c }, new Object[] { result[index] }, throwable);
                    }
                    index++;
                }
            }

        }, bc.getCommands());
    }

    /**
     * 清理CacheManager的相关资源，特别是L1Cache
     */

    public void shutdown() {

        this.l1cache.release();

        service.shutdown();
    }

    /***************************** new APIs for 2.0 START *************************************/

    /**
     * SYNC atomic incre one key
     * 
     * @param region
     * @param key
     * @return
     */
    public Integer incre(String region, String key) {

        String rkey = getRedisKey(region, key);

        CommandInfo ci = new CommandInfo(CommandInfo.RedisCommand.INCR, rkey);

        Object[] results = service.submitCommands(ci);

        if (null != results && results.length > 0) {

            return Integer.parseInt((String) results[0]);
        }

        return null;
    }

    /**
     * SYNC atomic decre one key
     * 
     * @param region
     * @param key
     * @return
     */
    public Integer decre(String region, String key) {

        String rkey = getRedisKey(region, key);

        CommandInfo ci = new CommandInfo(CommandInfo.RedisCommand.DECR, rkey);

        Object[] results = service.submitCommands(ci);

        if (null != results && results.length > 0) {

            return Integer.parseInt((String) results[0]);
        }

        return null;
    }

    /**
     * SYNC操作，不支持L1Cache
     * 
     * @param region
     * @param key
     * @return
     */
    public List<String> getHashKeys(String region, String key) {

        String rkey = getRedisKey(region, key);

        Object[] results = service.submitCommands(new CommandInfo(CommandInfo.RedisCommand.HKEYS, rkey));

        if (null != results && results.length > 0) {

            Object[] values = (Object[]) results[0];

            if (null == values) {
                return Collections.emptyList();
            }

            List<String> keyList = new ArrayList<String>();

            for (Object val : values) {
                keyList.add((String) val);
            }

            return keyList;
        }

        return Collections.emptyList();
    }

    /**
     * 
     * @param region
     * @param key
     * @param field
     */
    public void delHash(String region, String key, String... field) {

        delHash(region, key, field, null);
    }

    public void delHash(String region, String key, String field) {

        delHash(region, key, new String[] { field }, null);
    }

    public void delHash(String region, String key, String[] field, final AsyncCacheCallback<Boolean> handler) {

        final String rkey = getRedisKey(region, key);

        String[] params = new String[field.length + 1];
        params[0] = rkey;

        for (int i = 1; i < field.length + 1; i++) {
            params[i] = field[i - 1];
        }

        CommandInfo ci = new CommandInfo(CommandInfo.RedisCommand.HDEL, params);

        doCacheCommand(ci, new AbstractAsyncHandler<CommandInfo>() {

            @Override
            public void process(CommandInfo[] command, Object[] result, Throwable throwable) {

                boolean check = command[0].isSuccess();

                if (handler == null) {
                    return;
                }
                handler.onResult(check);
            }
        });
    }

    /***************************** new APIs for 2.0 END *************************************/

    /***************************** new APIs for 3.0 START *************************************/
    /**
     * ASYNC
     * 
     * @param region
     * @param key
     * @param value
     */
    public void lpush(String region, String key, String value) {

        lpush(region, key, value, null);
    }

    /**
     * ASYNC
     * 
     * NO L1 Support
     * 
     * @param region
     * @param key
     * @param value
     * @param handler
     */
    public void lpush(String region, String key, final String value, final AsyncCacheCallback<Boolean> handler) {

        if (null == value)
            return;

        final String rkey = getRedisKey(region, key);

        this.doCacheCommand(new CommandInfo(CommandInfo.RedisCommand.LPUSH, rkey, value),
                new AbstractAsyncHandler<CommandInfo>() {

                    @Override
                    public void process(CommandInfo[] command, Object[] result, Throwable throwable) {

                        boolean check = command[0].isSuccess();

                        if (handler == null)
                            return;

                        handler.onResult(check);
                    }
                });
    }

    /**
     * SYNC
     * 
     * @param region
     * @param key
     * @return
     */
    public String lpop(String region, String key) {

        String rkey = getRedisKey(region, key);

        Object[] results = service
                .submitCommands(new CommandInfo(CommandInfo.RedisCommand.LPOP, new String[] { rkey }));

        if (null != results && results.length > 0) {
            return (String) results[0];
        }

        return null;
    }

    /**
     * ASYNC
     * 
     * @param region
     * @param key
     * @param handler
     */
    public void lpop(String region, String key, final AsyncCacheCallback<String> handler) {

        final String rkey = getRedisKey(region, key);

        final AsyncCacheCallback<String> fcallback = handler;

        CommandInfo ci = new CommandInfo(CommandInfo.RedisCommand.LPOP, rkey);

        doCacheCommand(ci, new AbstractAsyncHandler<CommandInfo>() {

            @Override
            public void process(CommandInfo[] command, Object[] result, Throwable throwable) {

                if (fcallback != null && null != result && result.length > 0) {

                    fcallback.onResult((String) result[0]);
                }
            }
        });
    }

    /**
     * ASYNC
     * 
     * @param region
     * @param key
     * @param value
     */
    public void rpush(String region, String key, String value) {

        rpush(region, key, value, null);
    }

    /**
     * ASYNC
     * 
     * @param region
     * @param key
     * @param value
     * @param handler
     */
    public void rpush(String region, String key, String value, final AsyncCacheCallback<Boolean> handler) {

        if (null == value)
            return;

        final String rkey = getRedisKey(region, key);

        this.doCacheCommand(new CommandInfo(CommandInfo.RedisCommand.RPUSH, rkey, value),
                new AbstractAsyncHandler<CommandInfo>() {

                    @Override
                    public void process(CommandInfo[] command, Object[] result, Throwable throwable) {

                        boolean check = command[0].isSuccess();

                        if (handler == null)
                            return;

                        handler.onResult(check);
                    }
                });
    }

    /**
     * SYNC
     * 
     * @param region
     * @param key
     * @return
     */
    public String rpop(String region, String key) {

        String rkey = getRedisKey(region, key);

        Object[] results = service.submitCommands(new CommandInfo(CommandInfo.RedisCommand.RPOP, rkey));

        if (null != results && results.length > 0) {
            return (String) results[0];
        }

        return null;
    }

    /**
     * ASYNC
     * 
     * @param region
     * @param key
     * @param handler
     */
    public void rpop(String region, String key, final AsyncCacheCallback<String> handler) {

        final String rkey = getRedisKey(region, key);

        final AsyncCacheCallback<String> fcallback = handler;

        CommandInfo ci = new CommandInfo(CommandInfo.RedisCommand.RPOP, rkey);

        doCacheCommand(ci, new AbstractAsyncHandler<CommandInfo>() {

            @Override
            public void process(CommandInfo[] command, Object[] result, Throwable throwable) {

                if (fcallback != null && null != result && result.length > 0) {

                    fcallback.onResult((String) result[0]);
                }
            }
        });
    }

    /**
     * SYNC
     * 
     * @param region
     * @param key
     * @param start
     * @param end
     * @return
     */
    @SuppressWarnings("rawtypes")
    public List lrange(String region, String key, int start, int end) {

        String rkey = getRedisKey(region, key);

        Object[] results = service.submitCommands(
                new CommandInfo(CommandInfo.RedisCommand.LRANGE, rkey, String.valueOf(start), String.valueOf(end)));

        if (null != results && results.length > 0 && results[0] != null) {

            Object[] objs = (Object[]) results[0];

            List ls = Arrays.asList(objs);

            return ls;
        }

        return Collections.emptyList();
    }

    /**
     * ASYNC
     * 
     * @param region
     * @param key
     * @param start
     * @param end
     * @param handler
     */
    @SuppressWarnings("rawtypes")
    public void lrange(String region, String key, int start, int end, final AsyncCacheCallback<List> handler) {

        final String rkey = getRedisKey(region, key);

        final AsyncCacheCallback<List> fcallback = handler;

        CommandInfo ci = new CommandInfo(CommandInfo.RedisCommand.LRANGE, rkey, String.valueOf(start),
                String.valueOf(end));

        doCacheCommand(ci, new AbstractAsyncHandler<CommandInfo>() {

            @Override
            public void process(CommandInfo[] command, Object[] result, Throwable throwable) {

                if (fcallback != null && null != result && result.length > 0) {

                    fcallback.onResult(Arrays.asList((Object[]) result[0]));
                }
            }
        });
    }

    /**
     * SYNC
     * 
     * @param region
     * @param key
     * @param index
     * @return
     */
    public String lindex(String region, String key, int index) {

        String rkey = getRedisKey(region, key);

        Object[] results = service
                .submitCommands(new CommandInfo(CommandInfo.RedisCommand.LINDEX, rkey, String.valueOf(index)));

        if (null != results && results.length > 0) {
            return (String) results[0];
        }

        return null;
    }

    /**
     * ASYNC
     * 
     * @param region
     * @param key
     * @param index
     * @param handler
     */
    public void lindex(String region, String key, int index, final AsyncCacheCallback<String> handler) {

        final String rkey = getRedisKey(region, key);

        final AsyncCacheCallback<String> fcallback = handler;

        CommandInfo ci = new CommandInfo(CommandInfo.RedisCommand.LINDEX, rkey);

        doCacheCommand(ci, new AbstractAsyncHandler<CommandInfo>() {

            @Override
            public void process(CommandInfo[] command, Object[] result, Throwable throwable) {

                if (fcallback != null && null != result && result.length > 0) {

                    fcallback.onResult((String) result[0]);
                }
            }
        });
    }

    /**
     * SYNC
     * 
     * @param region
     * @param key
     * @param count
     * @param value
     * @return
     */
    public int lrem(String region, String key, int count, String value) {

        String rkey = getRedisKey(region, key);

        Object[] results = service
                .submitCommands(new CommandInfo(CommandInfo.RedisCommand.LREM, rkey, String.valueOf(count), value));

        if (null != results && results.length > 0) {
            Object r = results[0];
            return Integer.parseInt(r.toString());
        }

        return 0;
    }

    /**
     * ASYNC
     * 
     * @param region
     * @param key
     * @param count
     * @param value
     * @param handler
     */
    public void lrem(String region, String key, int count, String value, final AsyncCacheCallback<Integer> handler) {

        final String rkey = getRedisKey(region, key);

        final AsyncCacheCallback<Integer> fcallback = handler;

        CommandInfo ci = new CommandInfo(CommandInfo.RedisCommand.LREM, rkey);

        doCacheCommand(ci, new AbstractAsyncHandler<CommandInfo>() {

            @Override
            public void process(CommandInfo[] command, Object[] result, Throwable throwable) {

                if (fcallback != null && null != result && result.length > 0) {

                    fcallback.onResult(Integer.parseInt(result[0].toString()));
                }
            }
        });
    }

    /**
     * ASYNC
     * 
     * @param region
     * @param key
     * @param index
     * @param value
     * @return
     */
    public void lset(String region, String key, int index, String value) {

        lset(region, key, index, value, null);
    }

    /**
     * ASYNC
     * 
     * @param region
     * @param key
     * @param index
     * @param value
     * @param handler
     */
    public void lset(String region, String key, int index, String value, final AsyncCacheCallback<Boolean> handler) {

        if (null == value)
            return;

        final String rkey = getRedisKey(region, key);

        this.doCacheCommand(new CommandInfo(CommandInfo.RedisCommand.LSET, rkey, String.valueOf(index), value),
                new AbstractAsyncHandler<CommandInfo>() {

                    @Override
                    public void process(CommandInfo[] command, Object[] result, Throwable throwable) {

                        boolean check = command[0].isSuccess();

                        if (handler == null)
                            return;

                        handler.onResult(check);
                    }
                });
    }

    /**
     * SYNC llen
     * 
     * @param region
     * @param key
     * @return
     */
    public int llen(String region, String key) {

        String rkey = getRedisKey(region, key);

        Object[] results = service.submitCommands(new CommandInfo(CommandInfo.RedisCommand.LLEN, rkey));

        if (null != results && results.length > 0) {
            Object r = results[0];
            return Integer.parseInt(r.toString());
        }

        return 0;
    }

    /**
     * ASYNC llen
     * 
     * @param region
     * @param key
     * @param handler
     */
    public void llen(String region, String key, final AsyncCacheCallback<Integer> handler) {

        String rkey = getRedisKey(region, key);

        this.doCacheCommand(new CommandInfo(CommandInfo.RedisCommand.LLEN, rkey),
                new AbstractAsyncHandler<CommandInfo>() {

                    @Override
                    public void process(CommandInfo[] command, Object[] result, Throwable throwable) {

                        if (handler == null)
                            return;

                        handler.onResult(Integer.parseInt(result[0].toString()));
                    }
                });
    }

    /***************************** new APIs for 3.0 END *************************************/
}
