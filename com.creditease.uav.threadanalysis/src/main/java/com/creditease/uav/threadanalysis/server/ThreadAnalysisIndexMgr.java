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

package com.creditease.uav.threadanalysis.server;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.creditease.agent.helpers.DateTimeHelper;
import com.creditease.agent.spi.AbstractComponent;
import com.creditease.uav.elasticsearch.client.ESClient;

/**
 * 
 * ThreadAnalysisIndexMgr description: 用于管理线程分析的ES索引
 * 
 * 
 */
public class ThreadAnalysisIndexMgr extends AbstractComponent {

    public static final String JTA_DB = "uav_jta_db";
    public static final String JTA_TABLE = "uav_jta_table";

    private ESClient client;

    public ThreadAnalysisIndexMgr(String cName, String feature) {
        super(cName, feature);

        client = (ESClient) this.getConfigManager().getComponent(this.feature, "ESClient");
    }

    /**
     * 获取当前正在使用的索引名称
     * 
     * 判断当前是属于那个周的索引，命名是以周日开头的那天日期，比如2017-06-25（周日）
     * 
     * 举例： 2017-06-24的数据实际是在2017-06-18（周日）的索引里面
     * 
     * @return
     */
    public String getIndexByDate(String date) {

        Date d = DateTimeHelper.convertToDate(date);

        Calendar c = Calendar.getInstance();

        c.setTime(d);

        return this.getCurrentIndex(c, 0);
    }

    /**
     * getCurrentIndex
     * 
     * @param c
     * @param when
     * @return
     */
    private String getCurrentIndex(Calendar c, int when) {

        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 1;

        Date d = DateTimeHelper.dateAdd(DateTimeHelper.INTERVAL_DAY, c.getTime(), -dayOfWeek);

        if (when != 0) {
            d = DateTimeHelper.dateAdd(DateTimeHelper.INTERVAL_WEEK, d, when);
        }

        return JTA_DB + "_" + DateTimeHelper.toFormat("yyyy-MM-dd", d.getTime());
    }

    /**
     * getCurrentIndex
     * 
     * @return
     */
    public String getCurrentIndex() {

        return this.getCurrentIndex(Calendar.getInstance(), 0);
    }

    /**
     * 准备索引，没有就创建
     */
    public String prepareIndex() {

        String currentIndex = getCurrentIndex();

        if (client.existIndex(currentIndex) == true) {
            return currentIndex;
        }

        synchronized (this) {

            if (client.existIndex(currentIndex) == true) {
                return currentIndex;
            }

            client.creatIndex(currentIndex);

            Map<String, Object> set = new HashMap<String, Object>();
            set.put("index.number_of_shards", 5);
            set.put("index.number_of_replicas", 0);
            client.updateIndexSetting(currentIndex, set);
            /**
             * 变更别名到当前新的Index
             */
            String previousIndex = this.getCurrentIndex(Calendar.getInstance(), -1);
            client.addIndexAlias(currentIndex, JTA_DB);
            client.removeIndexAlias(previousIndex, JTA_DB);

            /**
             * 就只有pname、info做分词，其他属性不做分词
             */
            Map<String, Map<String, Object>> mapping = new HashMap<String, Map<String, Object>>();

            Map<String, Object> stringFields = new HashMap<String, Object>();
            stringFields.put("type", "string");
            stringFields.put("index", "not_analyzed");

            mapping.put("ipport", stringFields);
            mapping.put("pid", stringFields);
            mapping.put("appgroup", stringFields);
            mapping.put("tid", stringFields);
            mapping.put("state", stringFields);

            mapping.put("timeadd", stringFields);
            mapping.put("user", stringFields);

            /**
             * pname、info设置分词器
             */
            Map<String, Object> analyzedFields = new HashMap<String, Object>();
            analyzedFields.put("type", "text");
            analyzedFields.put("index", "analyzed");
            analyzedFields.put("analyzer", "standard");

            mapping.put("info", analyzedFields);
            mapping.put("pname", analyzedFields);

            /**
             * percpu permem 设置 double
             */
            Map<String, Object> doubleFields = new HashMap<String, Object>();
            doubleFields.put("type", "double");
            doubleFields.put("index", "not_analyzed");

            mapping.put("percpu", doubleFields);
            mapping.put("permem", doubleFields);
            /**
             * 时间字段多类型设置
             */
            Map<String, Object> timestamp = new HashMap<String, Object>();
            Map<String, Object> timefields = new HashMap<String, Object>();
            Map<String, Object> longType = new HashMap<String, Object>();
            timestamp.put("type", "date");
            timestamp.put("fields", timefields);
            timefields.put("long", longType);
            longType.put("type", "long");

            mapping.put("time", timestamp);

            try {
                client.setIndexTypeMapping(currentIndex, JTA_TABLE, mapping);
            }
            catch (IOException e) {
                log.err(this, "Set ES Index Type Mapping FAIL: ", e);
            }
        }

        return currentIndex;
    }

}
