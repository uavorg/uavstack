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

package com.creditease.agent.monitor.api;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class StandardMonitorElementInstance {

    public enum CompareSetOperation {
        MAX, MIN
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

    private final String id;
    private final Map<String, Object> values = new ConcurrentHashMap<String, Object>();
    private final Map<String, SumBySeconds> elemSumBySeconds = new ConcurrentHashMap<String, SumBySeconds>();
    private final StandardMonitorElement parent;

    public StandardMonitorElementInstance(String id, StandardMonitorElement parent) {
        this.id = id;
        this.parent = parent;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })

    public String toJSONString() {

        StringBuilder sb = new StringBuilder("{");

        sb.append("id:\"" + id + "\",values:{");

        Set<Entry<String, Object>> entry = values.entrySet();

        int count = 0;
        for (Entry<String, Object> e : entry) {

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

            sb.append(",");
            count++;
        }

        if (count > 0) {
            sb = sb.deleteCharAt(sb.length() - 1);
        }

        sb.append("}");
        return sb.append("}").toString();
    }

    public String getInstanceId() {

        return id;
    }

    public Map<String, Object> getValues() {

        return values;
    }

    public void putValues(Map<String, Object> m) {

        if (m == null) {
            return;
        }

        values.putAll(m);
    }

    public void setValue(String key, Object value) {

        if (key == null || value == null)
            return;

        values.put(key, value);
    }

    public void destroy() {

        values.clear();
    }

    public Object getValue(String key) {

        if (key == null)
            return null;

        return values.get(key);
    }

    /**
     * sumValue to to keep the Atomic Increment Number operation on one instance value
     * 
     * @param key
     * @param addValue
     */

    public long sumValue(String key, long addValue) {

        Object obj = values.get(key);

        if (obj == null) {
            synchronized (values) {
                obj = values.get(key);
                if (obj == null) {
                    AtomicLong incre = new AtomicLong(addValue);
                    values.put(key, incre);
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

            values.remove(key);

            synchronized (values) {

                obj = values.get(key);
                if (obj == null) {
                    AtomicLong newincre = new AtomicLong(addValue);
                    values.put(key, newincre);
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
     * IncreValue to keep the Atomic Increment operation on one instance value
     */

    public long increValue(String key) {

        return sumValue(key, 1);
    }

    /**
     * compareSet to do Atomic Comparison operation the newValue to oldValue and set the comparison result
     */

    public boolean compareSet(String key, long newValue, CompareSetOperation operation) {

        Object obj = values.get(key);

        if (obj == null) {
            synchronized (values) {
                obj = values.get(key);
                if (obj == null) {
                    AtomicLong incre = new AtomicLong(newValue);
                    values.put(key, incre);
                    return true;
                }
            }
        }

        AtomicLong incre = AtomicLong.class.cast(obj);
        synchronized (incre) {
            long curValue = incre.get();
            switch (operation) {
                case MAX:
                    if (curValue < newValue) {
                        incre.set(newValue);
                        return true;
                    }
                    break;
                case MIN:
                    if (curValue > newValue) {
                        incre.set(newValue);
                        return true;
                    }
                    break;
                default:
                    break;
            }

        }

        return false;
    }

    /**
     * get the value as long type
     */

    public long getValueLong(String key) {

        Object o = this.getValue(key);
        if (o == null)
            return 0;

        if (AtomicLong.class.isAssignableFrom(o.getClass())) {
            return AtomicLong.class.cast(o).get();
        }
        else {
            return 0;
        }
    }

    public StandardMonitorElement getMonitorElement() {

        return this.parent;
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
     *            记录过去记录的个数
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

}
