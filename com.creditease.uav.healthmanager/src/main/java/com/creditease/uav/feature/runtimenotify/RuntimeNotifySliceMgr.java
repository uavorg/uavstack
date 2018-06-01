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

package com.creditease.uav.feature.runtimenotify;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.spi.AbstractComponent;
import com.creditease.uav.cache.api.CacheManager;

/**
 * 负责对运行时预警数据的管理及存取
 * 
 * @author zhuang
 *
 */
public class RuntimeNotifySliceMgr extends AbstractComponent {

    private static final String UAV_CACHE_REGION = "store.region.uav";
    private static final String CACHE_PREFIX = "SLICE_";

    // storage cm
    private CacheManager cm;

    private Map<String, Long> sliceTimerange = new HashMap<>();
    private Map<String, Long> sliceExpire = new HashMap<>();

    public RuntimeNotifySliceMgr(String cName, String feature) {
        super(cName, feature);
    }

    public RuntimeNotifySliceMgr(String cName, String feature, CacheManager cm) {
        super(cName, feature);
        this.cm = cm;
    }

    public void setStorageCacheManager(CacheManager cm) {

        this.cm = cm;
    }

    @SuppressWarnings("unchecked")
    public void loadSliceConfig(String json) {

        Map<String, Object> jo = JSONHelper.toObject(json, Map.class);
        for (Entry<String, Object> en : jo.entrySet()) {

            Map<String, String> m = (Map<String, String>) en.getValue();
            // transfer to ms
            long timerange = Long.parseLong(m.get("timerange"));
            // still in ms
            long expire = Long.parseLong(m.get("expire"));
            sliceTimerange.put(en.getKey(), timerange);
            sliceExpire.put(en.getKey(), expire);
        }
    }

    @Deprecated
    public void storeSlices(List<Slice> slices) {

        long st = 0;
        if (log.isDebugEnable()) {
            st = System.currentTimeMillis();
            log.debug(this, "RuntimeNotify Store Slices START: slices len=" + slices.size());
        }

        cm.beginBatch();

        for (Slice slice : slices) {
            storeSlice(slice);
        }

        cm.submitBatch();

        if (log.isDebugEnable()) {
            st = System.currentTimeMillis() - st;
            log.debug(this, "RuntimeNotify Store Slices END(" + st + ")");
        }

    }

    @Deprecated
    public void storeSlice(Slice slice) {

        long expire = sliceExpire.get(slice.getMdf().getTag());

        String sliceKey = genSliceQueueKey(slice);

        cm.rpush(UAV_CACHE_REGION, sliceKey, slice.toJSONString());
        cm.expire(UAV_CACHE_REGION, sliceKey, expire, TimeUnit.MILLISECONDS);

        if (log.isDebugEnable()) {
            log.debug(this, "RuntimeNotify PUSH Slice: slice key=" + slice.getKey());
        }
    }

    @Deprecated
    @SuppressWarnings("unchecked")
    public List<Slice> getSlice(Slice slice, long start, long end) {

        List<Slice> list = new ArrayList<>();

        String cacheKey = genSliceQueueKey(slice);

        int cacheLen = cm.llen(UAV_CACHE_REGION, cacheKey);

        if (log.isDebugEnable()) {
            log.debug(this, "RuntimeNotify Get Slices START: slice key=" + slice.getKey() + ",curlen=" + cacheLen);
        }

        List<String> sl = null;
        if (cacheLen < 10) {
            sl = cm.lrange(UAV_CACHE_REGION, cacheKey, 0, cacheLen);
        }
        else {
            int low = 0;
            int high = cacheLen - 1;
            int mid = 0;
            long[] tmp = new long[cacheLen];

            while (low >= 0 && high >= 0 && getSliceTime(cacheKey, low, tmp) <= getSliceTime(cacheKey, high, tmp)) {
                mid = low + (high - low) / 2;
                if (start < getSliceTime(cacheKey, mid, tmp)) {
                    high = mid - 1;
                }
                else if (start > getSliceTime(cacheKey, mid, tmp)) {
                    low = mid + 1;
                }
                else {
                    break;
                }
            }

            sl = cm.lrange(UAV_CACHE_REGION, cacheKey, mid, cacheLen);
        }

        for (String sj : sl) {
            Slice r = new Slice(sj);

            if (r.getTime() <= end && r.getTime() >= start) {
                list.add(r);
                continue;
            }
        }

        // for (int index = cacheLen - 1; index > 0; index--) {
        //
        // String sj = cm.lindex(UAV_CACHE_REGION, cacheKey, index);
        //
        // Slice r = new Slice(sj);
        //
        // if (r.getTime() <= end && r.getTime() >= start) {
        // list.add(r);
        // continue;
        // }
        //
        // if (r.getTime() < start) {
        // break;
        // }
        // }
        //
        // Collections.reverse(list);

        if (log.isDebugEnable()) {
            log.debug(this, "RuntimeNotify Get Slices END: slice key=" + slice.getKey() + ",getlen=" + list.size());
        }

        return list;
    }

    private long getSliceTime(String key, int index, long[] cache) {

        if (cache[index] != 0) {
            return cache[index];
        }
        String s = cm.lindex(UAV_CACHE_REGION, key, index);
        long time = new Slice(s).getTime();
        cache[index] = time;
        return time;
    }

    @Deprecated
    public String genSliceQueueKey(Slice slice) {

        long timerange = sliceTimerange.get(slice.getMdf().getTag());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Calendar cl = Calendar.getInstance();

        int year = cl.get(Calendar.YEAR);
        int month = cl.get(Calendar.MONTH) + 1;
        int day = cl.get(Calendar.DATE);

        long cTime = 0;
        try {
            Date d = sdf.parse(year + "-" + month + "-" + day + " 00:00:00");
            cTime = d.getTime();
        }
        catch (ParseException e) {
            // ignore
        }

        long seconds = new Date().getTime() - cTime;

        long timeRangeIndex = seconds / timerange;

        return CACHE_PREFIX + slice.getKey() + "_" + timeRangeIndex;
    }

    // optimized ......................................................................

    public void storeSlices(List<Slice> slices, String tag) {

        long expire = sliceExpire.get(tag);

        if (expire <= 0) {
            return;
        }

        long st = 0;
        if (log.isDebugEnable()) {
            st = System.currentTimeMillis();
            log.debug(this, "RuntimeNotify Store Slices START: slices len=" + slices.size() + " expre=" + expire);
        }

        cm.beginBatch();

        for (Slice slice : slices) {
            storeSlice(slice, expire);
        }

        cm.submitBatch();

        if (log.isDebugEnable()) {
            st = System.currentTimeMillis() - st;
            log.debug(this, "RuntimeNotify Store Slices END(" + st + ")");
        }
    }

    private void storeSlice(Slice slice, long expire) {

        String sliceKey = genSliceKey(slice);

        cm.lpush(UAV_CACHE_REGION, sliceKey, slice.toJSONString());
        cm.expire(UAV_CACHE_REGION, sliceKey, expire, TimeUnit.MILLISECONDS);

        if (log.isDebugEnable()) {
            log.debug(this, "RuntimeNotify PUSH Slice: slice key=" + sliceKey);
        }
    }

    @SuppressWarnings("deprecation")
    private String genSliceKey(Slice slice) {

        Date d = new Date(slice.getTime());
        return CACHE_PREFIX + slice.getKey() + "_" + d.getMinutes();
    }

    public List<Slice> getSlices(Slice cur, long range) {

        List<Integer> rangePoints = getPreRangePoint(cur, range);
        List<Slice> slices = new ArrayList<>();
        for (int point : rangePoints) {
            String key = CACHE_PREFIX + cur.getKey() + "_" + point;
            @SuppressWarnings("unchecked")
            List<String> rangeSlicesStr = cm.lrange(UAV_CACHE_REGION, key, 0, -1);
            if (rangeSlicesStr.isEmpty()) {
                continue;
            }
            for (String ss : rangeSlicesStr) {
                Slice s = new Slice(ss);
                slices.add(s);
            }
        }
        Collections.reverse(slices);
        return slices;
    }

    private List<Integer> getPreRangePoint(Slice slice, long range) {

        int points = (int) Math.ceil(range / 1000 / 60.0);
        List<Integer> list = new ArrayList<>(points + 1);
        @SuppressWarnings("deprecation")
        int pn = new Date(slice.getTime()).getMinutes();
        list.add(pn);
        for (int i = 0; i < points; i++) {
            pn = lastPointNum(pn);
            list.add(pn);
        }

        return list;
    }

    private int lastPointNum(int pointNum) {

        if (pointNum == 0) {
            return 59;
        }
        return pointNum - 1;
    }
}
