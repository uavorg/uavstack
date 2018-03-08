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

package com.creditease.uav.invokechain.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.spi.AbstractHttpHandler;
import com.creditease.uav.elasticsearch.client.ESClient;
import com.creditease.uav.invokechain.SlowOperIndexMgr;

public class SlowOperQueryHandler extends AbstractHttpHandler<UAVHttpMessage> {

    private ESClient client;

    private long timeout = 5000;

    private SlowOperIndexMgr indexMgr;

    // epinfo与es中type对应关系
    private static Map<String, String> typeMap = new HashMap<>();

    // type中对应的协议体字段
    private static Map<String, String[]> typeBodyMap = new HashMap<>();
    static {
        typeMap.put("http.service", "rpc");
        typeMap.put("apache.http.Client", "rpc");
        typeMap.put("apache.http.AsyncClient", "rpc");
        typeMap.put("mq.service", "mq");
        typeMap.put("rabbitmq.client", "mq");
        typeMap.put("rocketmq.client", "mq");
        typeMap.put("jdbc.client", "jdbc");
        typeMap.put("method", "method");
        typeMap.put("dubbo.provider", "dubbo");
        typeMap.put("dubbo.consumer", "dubbo");

        typeBodyMap.put("rpc", new String[] { "rpc_req_body", "rpc_rsp_body", "rpc_rsp_exception" });
        typeBodyMap.put("mq", new String[] { "mq_body" });
        typeBodyMap.put("method", new String[] { "method_req", "method_ret" });
        typeBodyMap.put("jdbc", new String[] { "sql_req", "sql_ret" });
        typeBodyMap.put("dubbo", new String[] { "method_req", "method_ret" });
    }

    public SlowOperQueryHandler(String cName, String feature) {

        super(cName, feature);
        client = (ESClient) this.getConfigManager().getComponent(this.feature, "ESClient");

        timeout = DataConvertHelper
                .toLong(this.getConfigManager().getFeatureConfiguration(this.feature, "es.query.timeout"), 5000);

        indexMgr = (SlowOperIndexMgr) this.getConfigManager().getComponent(this.feature, "SlowOperIndexMgr");
    }

    @Override
    public String getContextPath() {

        return "/slw/q";
    }

    @Override
    public void handle(UAVHttpMessage data) {

        String cmd = data.getIntent();

        switch (cmd) {
            case "qParams":
                queryByParams(data);
                break;
            case "qBodyContent":
                queryByBodyContent(data);
                break;
        }
    }

    /**
     * 根据协议的报文体内容查询
     * 
     * @param data
     */
    @SuppressWarnings("rawtypes")
    private void queryByBodyContent(UAVHttpMessage data) {

        String[] types = buildTypes(data);
        String content = data.getRequest("content");
        if (types.length == 0) {
            types = typeMap.values().toArray(types);
        }
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        for (String type : types) {
            for (String field : typeBodyMap.get(type)) {
                queryBuilder.should(QueryBuilders.matchQuery(field, content));
            }
        }
        SortBuilder[] sorts = buildSort(data);

        queryToList(data, queryBuilder, null, sorts);
    }

    /**
     * 自由组合参数查询
     * 
     * @param data
     */
    @SuppressWarnings("rawtypes")
    private void queryByParams(UAVHttpMessage data) {

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        if (data.getRequest("traceid") != null) {
            queryBuilder.must(QueryBuilders.matchQuery("traceid", data.getRequest("traceid")));
        }
        if (data.getRequest("spanid") != null) {
            queryBuilder.must(QueryBuilders.matchQuery("spanid", data.getRequest("spanid")));
        }
        if (data.getRequest("epinfo") != null) {
            queryBuilder.must(QueryBuilders.matchQuery("epinfo", data.getRequest("epinfo")));
        }

        BoolQueryBuilder queryBuilderInner = QueryBuilders.boolQuery();
        if (data.getRequest("rpc_req_head") != null) {
            queryBuilderInner.should(QueryBuilders.matchQuery("rpc_req_head", data.getRequest("rpc_req_head")));
        }
        if (data.getRequest("rpc_req_body") != null) {
            queryBuilderInner.should(QueryBuilders.matchQuery("rpc_req_body", data.getRequest("rpc_req_body")));
        }
        if (data.getRequest("rpc_rsp_head") != null) {
            queryBuilderInner.should(QueryBuilders.matchQuery("rpc_rsp_head", data.getRequest("rpc_rsp_head")));
        }
        if (data.getRequest("rpc_rsp_body") != null) {
            queryBuilderInner.should(QueryBuilders.matchQuery("rpc_rsp_body", data.getRequest("rpc_rsp_body")));
        }
        if (data.getRequest("rpc_rsp_exception") != null) {
            queryBuilderInner
                    .should(QueryBuilders.matchQuery("rpc_rsp_exception", data.getRequest("rpc_rsp_exception")));
        }
        if (data.getRequest("sql_req") != null) {
            queryBuilderInner.should(QueryBuilders.matchQuery("sql_req", data.getRequest("sql_req")));
        }
        if (data.getRequest("sql_ret") != null) {
            queryBuilderInner.should(QueryBuilders.matchQuery("sql_ret", data.getRequest("sql_ret")));
        }
        if (data.getRequest("method_req") != null) {
            queryBuilderInner.should(QueryBuilders.matchQuery("method_req", data.getRequest("method_req")));
        }
        if (data.getRequest("method_ret") != null) {
            queryBuilderInner.should(QueryBuilders.matchQuery("method_ret", data.getRequest("method_ret")));
        }
        if (data.getRequest("mq_head") != null) {
            queryBuilderInner.should(QueryBuilders.matchQuery("mq_head", data.getRequest("mq_head")));
        }
        if (data.getRequest("mq_body") != null) {
            queryBuilderInner.should(QueryBuilders.matchQuery("mq_body", data.getRequest("mq_body")));
        }
        queryBuilder.must(queryBuilderInner);

        SortBuilder[] sorts = buildSort(data);

        this.queryToList(data, queryBuilder, null, sorts);
    }

    @SuppressWarnings({ "rawtypes" })
    private SortBuilder[] buildSort(UAVHttpMessage data) {

        SortBuilder[] sorts = null;

        String sort = data.getRequest("sort");

        if (sort != null) {
            String[] sortFieldStrs = sort.split(",");
            List<SortBuilder> ls = new ArrayList<SortBuilder>();
            for (String sortFieldStr : sortFieldStrs) {
                String[] sortExp = sortFieldStr.split("=");
                SortBuilder stimeSort = new FieldSortBuilder(sortExp[0]);
                stimeSort.order(SortOrder.fromString(sortExp[1]));
                ls.add(stimeSort);
            }
            sorts = new SortBuilder[ls.size()];
            sorts = ls.toArray(sorts);
        }
        else {
            return null;
        }
        return sorts;
    }

    private String[] buildTypes(UAVHttpMessage data) {

        List<String> types = new ArrayList<>();
        if (data.getRequest("epinfo") != null) {
            String[] strs = data.getRequest("epinfo").split(",");
            for (String str : strs) {
                if (DataConvertHelper.toLong(str, -1) != -1) {
                    types.add("method");
                }
                else {
                    types.add(typeMap.get(str));
                }
            }
        }
        return types.toArray(new String[] {});
    }

    /**
     * 
     * @param data
     * @param queryBuilder
     * @param postFilter
     */
    @SuppressWarnings("rawtypes")
    private void queryToList(UAVHttpMessage data, QueryBuilder queryBuilder, QueryBuilder postFilter,
            SortBuilder[] sorts) {

        SearchResponse sr = query(data, queryBuilder, postFilter, sorts);
        if (sr == null) {
            return;
        }

        SearchHits shits = sr.getHits();

        List<Map<String, Object>> records = new ArrayList<Map<String, Object>>();

        for (SearchHit sh : shits) {
            Map<String, Object> record = sh.getSourceAsMap();

            if (record == null) {
                continue;
            }

            records.add(record);
        }

        data.putResponse("rs", JSONHelper.toString(records));
        // 返回总的条数
        data.putResponse("count", shits.getTotalHits() + "");
    }

    @SuppressWarnings("rawtypes")
    private SearchResponse query(UAVHttpMessage data, QueryBuilder queryBuilder, QueryBuilder postFilter,
            SortBuilder[] sorts) {

        String appid = data.getRequest("appid");
        if (appid == null) {
            data.putResponse("rs", "ERR");
            data.putResponse("msg", "appid is required");
            return null;
        }

        String indexDate = data.getRequest("indexdate");
        String currentIndex;
        if (indexDate != null) {
            // 获得指定的index
            currentIndex = this.indexMgr.getIndexByDate(indexDate, appid);
        }
        else {
            // get current index
            currentIndex = this.indexMgr.getCurrentIndex(appid);
        }

        SearchRequestBuilder srb = client.getClient().prepareSearch(currentIndex).setTypes(buildTypes(data))
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH);

        int from = DataConvertHelper.toInt(data.getRequest("from"), -1);
        int size = DataConvertHelper.toInt(data.getRequest("size"), -1);

        if (from != -1 && size != -1) {
            srb = srb.setFrom(from).setSize(size);
        }

        srb.setQuery(queryBuilder);

        if (postFilter != null) {
            srb.setPostFilter(postFilter);
        }

        if (sorts != null && sorts.length > 0) {
            for (SortBuilder sb : sorts) {
                srb.addSort(sb);
            }
        }

        SearchResponse sr = srb.get(TimeValue.timeValueMillis(timeout));

        return sr;
    }
}
