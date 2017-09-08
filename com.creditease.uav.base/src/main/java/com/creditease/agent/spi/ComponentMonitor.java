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

package com.creditease.agent.spi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.creditease.agent.helpers.JSONHelper;

/**
 * ComponentMonitor 简单监控器
 */
public class ComponentMonitor {

    public enum Aggregation {

        Avg("avg"), Sum("sum"), Diff("diff");

        private String value;

        private Aggregation(String v) {
            value = v;
        }

        @Override
        public String toString() {

            return value;
        }
    }

    private static class SumBySeconds {

        private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        private long timespan;
        private long timestamp;
        private int lastRecordNumber = 1;
        private Map<String, Long> records = new LinkedHashMap<String, Long>();

        public void setLastRecordNumber(int lastRecordNumber) {

            this.lastRecordNumber = lastRecordNumber;
        }

        public long getTimespan() {

            return timespan;
        }

        public void setTimespan(long timespan) {

            this.timespan = timespan;
        }

        public long getTimestamp() {

            return timestamp;
        }

        public void setTimestamp(long timestamp) {

            this.timestamp = timestamp;
        }

        public void putHistoryRecord(long timestamp, long value) {

            if (records.size() >= lastRecordNumber) {
                String key = records.keySet().iterator().next();
                records.remove(key);
            }

            records.put(sdf.format(new Date(timestamp)), value);
        }

        public Map<String, Long> getHistoryRecords() {

            return this.records;
        }
    }

    private static Map<String, ComponentMonitor> smMap = new ConcurrentHashMap<String, ComponentMonitor>();

    /**
     * get simple monitor
     * 
     * @param name
     * @return
     */
    public static ComponentMonitor getMonitor(String name) {

        if (null == name || "".equals(name)) {
            return null;
        }

        if (smMap.containsKey(name)) {
            return smMap.get(name);
        }

        ComponentMonitor sm = new ComponentMonitor();

        smMap.put(name, sm);

        return sm;
    }

    private Map<String, Object> elems = new ConcurrentHashMap<String, Object>();
    private Map<String, SumBySeconds> elemSumBySeconds = new ConcurrentHashMap<String, SumBySeconds>();
    private Map<String, Map<String, String>> elemsAttrs = new ConcurrentHashMap<String, Map<String, String>>();

    /**
     * 
     * @param mode
     *            0 默认JSON格式，1 文本模式换行
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public String toJSONString(int mode) {

        String lineChar = (mode == 0) ? "" : "\n";
        int delChatLen = (mode == 0) ? 1 : 2;

        StringBuilder sb = new StringBuilder("{");

        Iterator<Entry<String, Object>> entry = elems.entrySet().iterator();

        int count = 0;
        while (entry.hasNext()) {
            Entry<String, Object> e = entry.next();
            sb.append("\"" + e.getKey() + "\":");

            Object val = e.getValue();
            Class<?> valClass = val.getClass();
            if (AtomicLong.class.isAssignableFrom(valClass) || Long.class.isAssignableFrom(valClass)
                    || Integer.class.isAssignableFrom(valClass) || Double.class.isAssignableFrom(valClass)) {
                sb.append(val);
            }
            else if (Map.class.isAssignableFrom(valClass)) {

                Map map = (Map) val;

                Iterator<Entry> newentry = map.entrySet().iterator();

                StringBuilder msb = new StringBuilder("{");

                int countMSB = 0;

                while (newentry.hasNext()) {

                    Entry entryElem = newentry.next();

                    msb.append("\"" + entryElem.getKey() + "\":" + entryElem.getValue() + ",");

                    countMSB++;
                }

                if (countMSB > 0) {
                    msb = msb.deleteCharAt(msb.length() - 1);
                }

                msb.append("}");

                sb.append(msb.toString());

            }
            else {
                sb.append("\"" + val + "\"");
            }

            sb.append("," + lineChar);
            count++;
        }

        if (count > 0) {
            sb = sb.deleteCharAt(sb.length() - delChatLen);
        }

        return sb.append("}").toString();
    }

    /**
     * 设置值
     * 
     * @param key
     * @param value
     */
    public void setValue(String key, Object value) {

        if (key == null || value == null)
            return;

        elems.put(key, value);
    }

    /**
     * remove metric
     * 
     * @param key
     */
    public void removeMetric(String key) {

        if (elems.containsKey(key)) {

            String skey = getSKey(key);

            System.getProperties().remove(skey);

            elems.remove(key);
            elemSumBySeconds.remove(key);
            elemsAttrs.remove(key);

        }

    }

    /**
     * 计数器
     * 
     * @param key
     * @return
     */
    public long increValue(String key) {

        return sumValue(key, 1);
    }

    /**
     * 计数器 by value
     * 
     * @param key
     * @param addValue
     * @return
     */
    public long sumValue(String key, long addValue) {

        Object obj = elems.get(key);

        // new init
        if (obj == null) {
            synchronized (elems) {
                obj = elems.get(key);
                if (obj == null) {
                    AtomicLong incre = new AtomicLong(addValue);
                    elems.put(key, incre);
                    return addValue;
                }
            }
        }

        AtomicLong incre = AtomicLong.class.cast(obj);
        long tmp = 0;

        SumBySeconds sbs = this.elemSumBySeconds.get(key);
        // sum by period
        if (sbs != null) {

            if (System.currentTimeMillis() - sbs.getTimestamp() >= sbs.getTimespan()) {

                synchronized (incre) {

                    if (System.currentTimeMillis() - sbs.getTimestamp() >= sbs.getTimespan()) {
                        // update timestamp
                        sbs.setTimestamp(System.currentTimeMillis());
                        // recore last value
                        sbs.putHistoryRecord(sbs.getTimestamp(), incre.get());
                        // set records value
                        setValue(key + ".rc", sbs.getHistoryRecords());

                        // return current value to 0
                        incre.set(addValue);

                        tmp = addValue;
                    }
                    else {
                        tmp = incre.addAndGet(addValue);
                    }
                }
            }
            else {
                tmp = incre.addAndGet(addValue);
            }
        }
        // normal incre
        else {

            tmp = incre.addAndGet(addValue);
        }

        // when exceed MAXVALUE
        if (tmp < 0) {

            elems.remove(key);

            synchronized (elems) {

                obj = elems.get(key);
                if (obj == null) {
                    AtomicLong newincre = new AtomicLong(addValue);
                    elems.put(key, newincre);
                    tmp = addValue;
                }
                else {
                    AtomicLong newincre = AtomicLong.class.cast(obj);
                    tmp = newincre.addAndGet(addValue);
                }
            }
        }

        return tmp;
    }

    /**
     * 设置某个计数器的统计周期
     * 
     * @param key
     * @param tu
     *            统计多少秒内的值，过后回0
     */
    public void setValueSumBySeconds(String key, Long tu) {

        setValueSumBySeconds(key, tu, 1);
    }

    /**
     * 设置某个计数器的统计周期
     * 
     * @param key
     * @param tu
     *            统计多少秒内的值，过后回0
     * @param lastRecordNumber
     *            保存过去记录的个数
     * 
     */
    public void setValueSumBySeconds(String key, Long tu, int lastRecordNumber) {

        if (null == key || tu <= 0 || lastRecordNumber <= 0) {
            return;
        }

        SumBySeconds sbs = new SumBySeconds();
        sbs.setTimespan(tu * 1000);
        sbs.setTimestamp(System.currentTimeMillis());
        sbs.setLastRecordNumber(lastRecordNumber);

        elemSumBySeconds.put(key, sbs);
    }

    /**
     * 清除某个计数器的统计周期
     * 
     * @param key
     */
    public void unsetValueSumBySeconds(String key) {

        if (null == key) {
            return;
        }

        elemSumBySeconds.remove(key);
    }

    /**
     * 将某个指标设定到一个指标group里面，可以实现合并显示
     * 
     * @param metricKey
     * @param groupName
     */
    public void setMetricGroup(String metricKey, String groupName) {

        if (!this.elemsAttrs.containsKey(metricKey)) {
            Map<String, String> m = new HashMap<String, String>();
            this.elemsAttrs.put(metricKey, m);
        }

        Map<String, String> m = this.elemsAttrs.get(metricKey);
        m.put("gp", groupName);

    }

    /**
     * 
     * 设置某个指标的聚合方式，在显示上体系
     * 
     * @param metricKey
     * @param agg
     */
    public void setMetricAggregation(String metricKey, Aggregation agg) {

        if (!this.elemsAttrs.containsKey(metricKey)) {
            Map<String, String> m = new HashMap<String, String>();
            this.elemsAttrs.put(metricKey, m);
        }

        Map<String, String> m = this.elemsAttrs.get(metricKey);
        m.put("agg", agg.toString());
    }

    /**
     * 将metric值flush到SystemProperties
     * 
     * @param includeMetrics
     *            为null则表示flush 所有metrics；如果给出metrics名字，则只flush这些metrics
     */
    public void flushToSystemProperties(String... includeMetrics) {

        Map<String, String> pro = new HashMap<String, String>();
        if (includeMetrics == null || includeMetrics.length == 0) {
            for (String key : this.elems.keySet()) {

                // ignore those history data
                if (key.endsWith(".rc") == true) {
                    continue;
                }

                String skey = getSKey(key);

                pro.put(skey, String.valueOf(elems.get(key)));

            }
        }
        else {

            for (String key : includeMetrics) {
                if (elems.containsKey(key)) {
                    String skey = getSKey(key);

                    pro.put(skey, String.valueOf(elems.get(key)));
                }
            }
        }

        System.getProperties().putAll(pro);
    }

    /**
     * get skey
     * 
     * @param key
     * @return
     */
    private String getSKey(String key) {

        String skey = "mo@" + key;

        if (this.elemsAttrs.containsKey(key)) {

            Map<String, String> attrs = this.elemsAttrs.get(key);

            if (attrs.size() > 0) {
                skey += "@" + JSONHelper.toString(attrs);
            }
        }

        return skey;
    }
}
