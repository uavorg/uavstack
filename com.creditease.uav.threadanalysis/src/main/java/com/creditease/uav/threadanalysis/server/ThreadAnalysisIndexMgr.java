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

import java.util.HashMap;
import java.util.Map;

import com.creditease.agent.spi.AbstractComponent;
import com.creditease.uav.elasticsearch.client.ESClient;
import com.creditease.uav.elasticsearch.index.ESIndexHelper;

/**
 * 
 * ThreadAnalysisIndexMgr description: 用于管理线程分析的ES索引
 *
 * 每天自动建立一个全局索引，命名为uav_jta_yyyy-MM-dd，type为"uav_jta_table"。
 * 
 */
public class ThreadAnalysisIndexMgr extends AbstractComponent {

    public static final String JTA = "jta";
    public static final String JTA_TABLE = "uav_jta_table";

    private ESClient client;

    public ThreadAnalysisIndexMgr(String cName, String feature) {

        super(cName, feature);

        client = (ESClient) this.getConfigManager().getComponent(this.feature, "ESClient");
    }

    /**
     * 获取日期对应的索引
     * 
     * @param date
     *            yyyy-MM-dd
     * @return
     */
    public String getIndexByDate(String date) {

        return ESIndexHelper.getIndexOfDay(JTA, date);
    }

    /**
     * 获取当前的索引
     * 
     * @return
     */
    public String getCurrentIndex() {

        return ESIndexHelper.getIndexOfDay(JTA);
    }

    /**
     * 准备索引，没有就创建
     * 
     * @return
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

            Map<String, String> set = new HashMap<String, String>();
            set.put("index.number_of_shards", "5");
            set.put("index.number_of_replicas", "1");

            /**
             * 就只有pname、info做分词，其他属性不做分词
             */
            Map<String, Map<String, Object>> mapping = new HashMap<String, Map<String, Object>>();

            Map<String, Object> stringFields = new HashMap<String, Object>();
            stringFields.put("type", "keyword");

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

            longType.put("type", "long");
            timefields.put("long", longType);
            timestamp.put("fields", timefields);
            timestamp.put("type", "date");
            mapping.put("time", timestamp);

            try {
                client.creatIndex(currentIndex, JTA_TABLE, set, mapping);
            }
            catch (Exception e) {
                log.err(this, "create ES Index FAIL: ", e);
            }

            /**
             * 变更别名到当前新的Index
             */
            String previousIndex = this.getPreviousIndex();
            client.addIndexAlias(currentIndex, JTA);
            client.removeIndexAlias(previousIndex, JTA);
        }

        return currentIndex;
    }

    /**
     * 获取前一天的索引
     * 
     * @return
     */
    private String getPreviousIndex() {

        return ESIndexHelper.getIndexOfDay(JTA, -1);
    }

}
