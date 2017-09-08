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

package com.creditease.uav.collect.client.copylogagent;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DefaultLogFilterAndRule implements LogFilterAndRule {

    public static final String READ_LINE_NUMBER = "line.number";
    public static final String READ_TIMESTAMP = "read.timestamp";

    protected Pattern filterPattern = null;

    protected Splitter separator = null;

    protected Integer[] SpecifiedFields = null;

    protected String[] fieldsName = null;

    protected int version = 0;
    // when timestamp needn't
    // private static final int TIMESTAMP_NONE = -1;
    // when timestamp is need, but not specified.
    protected static final int TIMESTAMP_NEED = 0;

    protected int timeStampField = 0;

    protected List<LogFilterAndRule> logAids = Lists.newArrayList();

    @SuppressWarnings("rawtypes")

    private ThreadLocal<List<Map>> mainlogs = new ThreadLocal<List<Map>>();

    // protected List<Map> mainlogs = Lists.newLinkedList();

    @SuppressWarnings("rawtypes")
    public List<Map> getMainlogs() {

        if (mainlogs.get() == null) {
            List<Map> mainlogs = Lists.newLinkedList();
            setMainlogs(mainlogs);
        }
        return mainlogs.get();
    }

    @SuppressWarnings({ "rawtypes" })
    public void setMainlogs(List<Map> logs) {

        mainlogs.set(logs);
    }

    /**
     * 构造默认的日志规则
     * 
     * @param filterregex
     *            日志过滤规则的正则表达式
     * @param separator
     *            日志字段分隔符
     * @param fields
     *            日志字段名以及对应在的列号
     * @param fieldNumber
     *            指定对应的列号值为时间戳
     * @param version
     *            规则当前的版本
     */
    public DefaultLogFilterAndRule(String filterregex, String separator, JSONObject fields, int fieldNumber,
            int version) {

        this.filterPattern = Pattern.compile(filterregex);

        this.separator = Splitter.on(separator).trimResults();
        this.SpecifiedFields = new Integer[fields.size()];
        this.fieldsName = new String[fields.size()];
        int i = 0;
        for (Entry<String, Object> entry : fields.entrySet()) {
            fieldsName[i] = entry.getKey();
            SpecifiedFields[i++] = (Integer) entry.getValue();
        }
        this.timeStampField = fieldNumber;

        this.version = version;

        @SuppressWarnings("rawtypes")
        List<Map> mainlogs = Lists.newLinkedList();

        setMainlogs(mainlogs);
    }

    public DefaultLogFilterAndRule addAidLogFilterAndRule(LogFilterAndRule far) {

        logAids.add(far);
        return this;
    }

    public List<LogFilterAndRule> getAllAidsLogFilterAndRule() {

        return logAids;
    }

    @Override
    public boolean isMatch(ReliableTaildirEventReader reader, List<Event> events, int num, String log) {

        if (filterPattern.matcher(log).matches()) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map doAnalysis(Map<String, String> header, String log) {

        List<String> logfields = Lists.newArrayList(separator.split(log));
        Map<String, String> resultMap = Maps.newHashMap();
        int i = 0;
        // collection specified fields
        for (int point : SpecifiedFields) {
            // add irregular process
            if (logfields.size() >= point)
                resultMap.put(fieldsName[i++], logfields.get(point - 1));
        }
        // add line number
        resultMap.put("_lnum", header.get(READ_LINE_NUMBER));
        // add timestamp
        if (timeStampField == TIMESTAMP_NEED) {
            resultMap.put("_timestamp", header.get(READ_TIMESTAMP));
        }
        else if (timeStampField > TIMESTAMP_NEED) {
            resultMap.put("_timestamp", logfields.get(timeStampField - 1));
        }
        this.getMainlogs().add(resultMap);
        return resultMap;

    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<Map> getResult(boolean isNeedCollection) {

        List<Map> ret = null;
        if (isNeedCollection) {
            ret = Lists.newLinkedList(this.getMainlogs());
        }
        this.getMainlogs().clear();
        return ret;
    }

    @Override
    public int getVersion() {

        return version;
    }
}
