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

package com.creditease.uav.invokechain;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.creditease.agent.helpers.DateTimeHelper;
import com.creditease.agent.spi.AbstractComponent;
import com.creditease.uav.elasticsearch.client.ESClient;

/**
 * 
 * InvokeChainIndexMgr description: 用于管理调用链的ES索引
 * 
 * 管理方式：
 * 
 * 每隔一个自然周（7天）自动建立一个全局的新IVC（调用链）索引，命名为uav_ivc_db_+<yyyy-MM-<以周日开头的日期>>，则全部调用链数据会流入这个新的索引。
 * 
 * 调用链在实时运维层面是以跨应用查询为主，与按时间跨度的查询关系不大，所以要保证当前索引的查询速度足够快
 * 
 * 1. 实现了索引分割，避免数据过大，造成查询性能不断下降
 * 
 * 2. 便于索引清理，可以清除掉哪些看来很早之前的索引
 *
 */
public class InvokeChainIndexMgr extends AbstractComponent {

    public static final String IVC_DB = "uav_ivc_db";
    public static final String IVC_Table = "uav_ivc_table";

    private ESClient client;

    public InvokeChainIndexMgr(String cName, String feature) {

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

        return IVC_DB + "_" + DateTimeHelper.toFormat("yyyy-MM-dd", d.getTime());
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

            Map<String, String> set = new HashMap<String, String>();
            set.put("index.number_of_shards", "5");
            set.put("index.number_of_replicas", "0");

            /**
             * 设置不需要分词的索引字段spanid，parentid，traceid，epinfo(方法级排序使用), appuuid（精确查找应用实例）
             */
            Map<String, Map<String, Object>> mapping = new HashMap<>();

            Map<String, Object> fields = new HashMap<>();

            fields.put("type", "keyword");

            mapping.put("traceid", fields);
            mapping.put("spanid", fields);
            mapping.put("parentid", fields);
            mapping.put("epinfo", fields);
            mapping.put("appuuid", fields);
            mapping.put("ipport", fields);
            mapping.put("appid", fields);

            /**
             * 时间字段多类型设置
             */
            Map<String, Object> timestamp = new HashMap<>();
            Map<String, Object> timefields = new HashMap<>();
            Map<String, Object> longType = new HashMap<>();
            mapping.put("stime", timestamp);
            timestamp.put("type", "date");
            timestamp.put("fields", timefields);
            timefields.put("long", longType);
            longType.put("type", "long");

            try {
                client.creatIndex(currentIndex, IVC_Table, set, mapping);
            }
            catch (Exception e) {
                log.err(this, "create ES Index FAIL: ", e);
            }

            /**
             * 变更别名到当前新的Index
             */
            String previousIndex = this.getCurrentIndex(Calendar.getInstance(), -1);
            client.addIndexAlias(currentIndex, IVC_DB);
            client.removeIndexAlias(previousIndex, IVC_DB);
        }

        return currentIndex;
    }

}
