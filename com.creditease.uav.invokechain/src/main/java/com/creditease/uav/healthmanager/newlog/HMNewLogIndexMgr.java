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

package com.creditease.uav.healthmanager.newlog;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.creditease.agent.spi.AbstractComponent;
import com.creditease.uav.elasticsearch.client.ESClient;
import com.creditease.uav.elasticsearch.index.ESIndexHelper;

/**
 * HMNewLogIndexMgr description: 日志索引的管理
 * 
 * 每天自动建立一个全局索引，命名为uav_applog_yyyy-MM-dd，type与日志文件名相关。
 * 
 */
public class HMNewLogIndexMgr extends AbstractComponent {

    private static final String AppLog = "applog";
    /**
     * Strings longer than the ignore_above setting will not be indexed This option is also useful for protecting
     * against Lucene’s term byte-length limit of 32766 The value for ignore_above is the character count, but Lucene
     * counts bytes. If you use UTF-8 text with many non-ASCII characters, you may want to set the limit to 32766 / 3 =
     * 10922 since UTF-8 characters may occupy at most 3 bytes
     */
    private static final int IGNORE_ABOVE = 32766 / 3;

    private ESClient client;

    public HMNewLogIndexMgr(String cName, String feature) {

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

        return ESIndexHelper.getIndexOfDay(AppLog, date);
    }

    /**
     * 获取当前的索引
     * 
     * @return
     */
    public String getCurrentIndex() {

        return ESIndexHelper.getIndexOfDay(AppLog);
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

            try {
                client.creatIndex(currentIndex, null, set, null);
            }
            catch (Exception e) {
                log.err(this, "create ES Index FAIL: ", e);
            }

            /**
             * 变更别名到当前新的Index
             */
            String previousIndex = this.getPreviousIndex();
            client.addIndexAlias(currentIndex, AppLog);
            client.removeIndexAlias(previousIndex, AppLog);

        }

        return currentIndex;
    }

    /**
     * 检查type是否存在，如果不存在就创建Mapping
     * 
     * @param index
     * @param type
     */
    public void prepareIndexType(String index, String type) {

        boolean check = client.existType(index, type);

        if (check == true) {
            return;
        }

        synchronized (this) {

            check = client.existType(index, type);

            if (check == true) {
                return;
            }

            Map<String, Map<String, Object>> mapping = new HashMap<>();
            Map<String, Object> fields = new HashMap<>();

            fields.put("type", "keyword");
            mapping.put("ipport", fields);
            mapping.put("appid", fields);

            // 设置分词器
            Map<String, Object> multiFields = new HashMap<>();
            Map<String, Object> afields = new HashMap<>();
            Map<String, String> fmapping = new HashMap<>();
            Map<String, Object> sfields = new HashMap<>();

            fmapping.put("type", "text");
            fmapping.put("include_in_all", "true");
            fmapping.put("analyzer", "ik_max_word");
            fmapping.put("search_analyzer", "ik_max_word");

            sfields.put("type", "keyword");
            sfields.put("ignore_above", IGNORE_ABOVE);

            /**
             * field: astring, 可以按字符串匹配
             */
            afields.put("asstring", sfields);
            afields.put("chinese", fmapping);
            multiFields.put("fields", afields);
            multiFields.put("type", "text");
            mapping.put("content", multiFields);

            /**
             * 时间字段多类型设置
             */
            Map<String, Object> timestamp = new HashMap<>();
            Map<String, Object> timefields = new HashMap<>();
            Map<String, Object> longType = new HashMap<>();

            longType.put("type", "long");
            timefields.put("long", longType);
            timestamp.put("fields", timefields);
            timestamp.put("type", "date");
            mapping.put("l_timestamp", timestamp);

            try {
                client.setIndexTypeMapping(index, type.toLowerCase(), mapping);
            }
            catch (IOException e) {
                log.err(this, "Set ES Index Type Mapping FAIL: ", e);
            }
        }
    }

    /**
     * 获取前一天的索引
     * 
     * @return
     */
    private String getPreviousIndex() {

        return ESIndexHelper.getIndexOfDay(AppLog, -1);
    }
}
