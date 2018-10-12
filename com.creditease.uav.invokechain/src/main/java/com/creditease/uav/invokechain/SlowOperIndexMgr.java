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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.creditease.agent.spi.AbstractComponent;
import com.creditease.uav.elasticsearch.client.ESClient;
import com.creditease.uav.elasticsearch.index.ESIndexHelper;

/**
 * SlowOperIndexMgr description: 重调用链的索引管理
 * 
 * 每天自动建立一个全局的重调用链索引，命名为uav_ivcdat_yyyy-MM-dd，type为protocolType。
 * 
 */
public class SlowOperIndexMgr extends AbstractComponent {

    private static final String IvcDat = "ivcdat";

    private ESClient client;

    // 需要做分词处理的字段
    private static String[] fields;
    static {
        fields = new String[] { "rpc_req_head", "rpc_req_body", "rpc_rsp_head", "rpc_rsp_body", "rpc_rsp_exception",
                "sql_req", "sql_ret", "method_req", "method_ret", "mq_head", "mq_body" };
    }

    public SlowOperIndexMgr(String cName, String feature) {

        super(cName, feature);

        client = (ESClient) this.getConfigManager().getComponent(this.feature, "ESClient");
    }

    /**
     * 获取日期<yyyy-MM-dd>对应的索引名称
     * 
     * @param date
     *            yyyy-MM-dd
     * @return
     */
    public String getIndexByDate(String date) {

        return ESIndexHelper.getIndexOfDay(IvcDat, date);
    }

    /**
     * 获取当前的索引
     * 
     * @return
     */
    public String getCurrentIndex() {

        return ESIndexHelper.getIndexOfDay(IvcDat);
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
            client.addIndexAlias(currentIndex, IvcDat);
            client.removeIndexAlias(previousIndex, IvcDat);

        }

        return currentIndex;
    }

    /**
     * 检查type是否存在，如果不存在就创建Mapping
     * 
     * @param currentIndex
     * @param type
     */
    public void prepareIndexType(String currentIndex, String type) {

        boolean check = client.existType(currentIndex, type);

        if (check == true) {
            return;
        }

        synchronized (this) {

            check = client.existType(currentIndex, type);

            if (check == true) {
                return;
            }

            Map<String, Map<String, Object>> mapping = new HashMap<>();
            Map<String, Object> sfields = new HashMap<>();

            sfields.put("type", "keyword");
            mapping.put("traceid", sfields);
            mapping.put("spanid", sfields);
            mapping.put("epinfo", sfields);
            mapping.put("appid", sfields);

            // 设置分词器
            Map<String, Object> multiFields = new HashMap<>();
            Map<String, Object> afields = new HashMap<>();
            Map<String, String> fmapping = new HashMap<>();

            fmapping.put("type", "text");
            fmapping.put("include_in_all", "true");
            fmapping.put("analyzer", "ik_max_word");
            fmapping.put("search_analyzer", "ik_max_word");

            afields.put("chinese", fmapping);
            multiFields.put("fields", afields);
            multiFields.put("type", "text");

            /**
             * 不支持正则查询
             */
            for (String str : fields) {
                mapping.put(str, multiFields);
            }
            try {
                client.setIndexTypeMapping(currentIndex, type.toLowerCase(), mapping);
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

        return ESIndexHelper.getIndexOfDay(IvcDat, -1);
    }
}
