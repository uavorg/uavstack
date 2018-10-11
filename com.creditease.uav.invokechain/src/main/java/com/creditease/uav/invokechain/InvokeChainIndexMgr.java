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

import java.util.HashMap;
import java.util.Map;

import com.creditease.agent.spi.AbstractComponent;
import com.creditease.uav.elasticsearch.client.ESClient;
import com.creditease.uav.elasticsearch.index.ESIndexHelper;

/**
 * InvokeChainIndexMgr description: 用于管理调用链的ES索引
 * 
 * 每天自动建立一个全局的调用链索引，命名为uav_ivc_yyyy-MM-dd，type为"uav_ivc_table"。
 * 
 */
public class InvokeChainIndexMgr extends AbstractComponent {

    public static final String IVC = "ivc";
    public static final String IVC_Table = "uav_ivc_table";

    private ESClient client;

    public InvokeChainIndexMgr(String cName, String feature) {

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

        return ESIndexHelper.getIndexOfDay(IVC, date);
    }

    /**
     * 获取当前的索引
     * 
     * @return
     */
    public String getCurrentIndex() {

        return ESIndexHelper.getIndexOfDay(IVC);
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

            longType.put("type", "long");
            timefields.put("long", longType);
            timestamp.put("fields", timefields);
            timestamp.put("type", "date");
            mapping.put("stime", timestamp);

            try {
                client.creatIndex(currentIndex, IVC_Table, set, mapping);
            }
            catch (Exception e) {
                log.err(this, "create ES Index FAIL: ", e);
            }

            /**
             * 变更别名到当前新的Index
             */
            String previousIndex = this.getPreviousIndex();
            client.addIndexAlias(currentIndex, IVC);
            client.removeIndexAlias(previousIndex, IVC);
        }

        return currentIndex;
    }

    /**
     * 获取前一天的索引
     * 
     * @return
     */
    private String getPreviousIndex() {

        return ESIndexHelper.getIndexOfDay(IVC, -1);
    }

}
