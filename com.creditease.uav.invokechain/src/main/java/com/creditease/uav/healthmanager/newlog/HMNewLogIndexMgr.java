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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.creditease.agent.helpers.DateTimeHelper;
import com.creditease.agent.spi.AbstractComponent;
import com.creditease.uav.elasticsearch.client.ESClient;

/**
 * 
 * HMNewLogIndexMgr description: 日志索引的管理
 * 
 * 日志的索引以应用类型区分，每个应用类型为一个日志索引（Index），应用类型就是appid
 * 
 * 每个应用类型下不同的日志文件，以Type区分
 * 
 * 每个应用类型的日志索引也是按一个自然周（7天）一次切分，即每周日，产生一个新的索引来保存所有新的日志
 * 
 * 实际从实时运维角度日志也是时效性高，对时间跨度范围查询要求不高，只有在统计的时候才需要
 * 
 * 便于对不同的应用类型进行日志清理的策略
 *
 */
public class HMNewLogIndexMgr extends AbstractComponent {

    private static final String AppLog = "applog_";

    private ESClient client;

    public HMNewLogIndexMgr(String cName, String feature) {
        super(cName, feature);

        client = (ESClient) this.getConfigManager().getComponent(this.feature, "ESClient");
    }

    /**
     * 获取当前正在使用的索引名称AppLog_<appid>_<所属周的周日日期>
     * 
     * 判断当前是属于那个周的索引，命名是以周日开头的那天日期，比如2017-06-25（周日）
     * 
     * 举例： 2017-06-24的数据实际是在2017-06-18（周日）的索引里面
     * 
     * @param appid
     * @return
     */
    public String getIndexByDate(String date, String appid) {

        Date d = DateTimeHelper.convertToDate(date);

        Calendar c = Calendar.getInstance();

        c.setTime(d);

        return this.getCurrentIndex(c, appid, 0);
    }

    /**
     * getCurrentIndex
     * 
     * @param c
     * @param appid
     * @param when
     * @return
     */
    private String getCurrentIndex(Calendar c, String appid, int when) {

        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 1;

        Date d = DateTimeHelper.dateAdd(DateTimeHelper.INTERVAL_DAY, c.getTime(), -dayOfWeek);

        if (when != 0) {
            d = DateTimeHelper.dateAdd(DateTimeHelper.INTERVAL_WEEK, d, when);
        }

        return AppLog + formatAppid(appid) + "_" + DateTimeHelper.toFormat("yyyy-MM-dd", d.getTime());
    }

    /**
     * 格式化一些不符合ES规范的字符
     * 
     * @param appid
     * @return
     */
    private String formatAppid(String appid) {

        return appid.toLowerCase().replace('.', '_');
    }

    /**
     * getCurrentIndex
     * 
     * @param appid
     * @return
     */
    public String getCurrentIndex(String appid) {

        return this.getCurrentIndex(Calendar.getInstance(), appid, 0);
    }

    /**
     * 准备索引，没有就创建
     * 
     * @param appid
     * @return
     */
    public String prepareIndex(String appid) {

        String currentIndex = getCurrentIndex(appid);

        if (client.existIndex(currentIndex) == true) {
            return currentIndex;
        }

        synchronized (this) {

            if (client.existIndex(currentIndex) == true) {
                return currentIndex;
            }

            client.creatIndex(currentIndex);
            Map<String, Object> set = new HashMap<String, Object>();
            set.put("index.number_of_shards", 2);
            set.put("index.number_of_replicas", 0);
            client.updateIndexSetting(currentIndex, set);

            String sappid = formatAppid(appid);

            /**
             * 变更别名到当前新的Index
             */
            String previousIndex = this.getCurrentIndex(Calendar.getInstance(), appid, -1);
            client.addIndexAlias(currentIndex, AppLog + sappid);
            client.removeIndexAlias(previousIndex, AppLog + sappid);

        }

        return currentIndex;
    }

    /**
     * 检查type是否存在，如果不存在就创建Mapping
     * 
     * @param appid
     * @param type
     */
    public void prepareIndexType(String appid, String type) {

        boolean check = client.existType(appid, type);

        if (check == true) {
            return;
        }

        synchronized (this) {

            check = client.existType(appid, type);

            if (check == true) {
                return;
            }

            Map<String, Map<String, Object>> mapping = new HashMap<>();

            // 设置
            Map<String, Object> sfields = new HashMap<>();

            sfields.put("type", "string");
            sfields.put("index", "not_analyzed");

            mapping.put("ipport", sfields);

            // 设置分词器
            Map<String, Object> multiFields = new HashMap<>();

            multiFields.put("type", "text");

            Map<String, Object> afields = new HashMap<>();

            multiFields.put("fields", afields);

            Map<String, String> fmapping = new HashMap<>();

            afields.put("chinese", fmapping);

            fmapping.put("type", "text");
            fmapping.put("include_in_all", "true");
            fmapping.put("analyzer", "ik_max_word");
            fmapping.put("search_analyzer", "ik_max_word");

            /**
             * field: astring, 可以按字符串匹配
             */
            afields.put("asstring", sfields);

            mapping.put("content", multiFields);

            /**
             * 时间字段多类型设置
             */
            Map<String, Object> timestamp = new HashMap<>();
            Map<String, Object> timefields = new HashMap<>();
            Map<String, Object> longType = new HashMap<>();
            mapping.put("l_timestamp", timestamp);
            timestamp.put("type", "date");
            timestamp.put("fields", timefields);
            timefields.put("long", longType);
            longType.put("type", "long");

            try {
                client.setIndexTypeMapping(appid, type.toLowerCase(), mapping);
            }
            catch (IOException e) {
                log.err(this, "Set ES Index Type Mapping FAIL: ", e);
            }
        }
    }
}
