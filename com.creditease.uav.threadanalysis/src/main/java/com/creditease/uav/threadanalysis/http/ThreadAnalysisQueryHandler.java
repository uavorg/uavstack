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

package com.creditease.uav.threadanalysis.http;

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
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.spi.AbstractHttpHandler;
import com.creditease.uav.elasticsearch.client.ESClient;
import com.creditease.uav.threadanalysis.server.ThreadAnalyser;
import com.creditease.uav.threadanalysis.server.ThreadAnalysisIndexMgr;
import com.creditease.uav.threadanalysis.server.da.ThreadObject;

public class ThreadAnalysisQueryHandler extends AbstractHttpHandler<UAVHttpMessage> {

    private ESClient client;

    private long timeout = 5000;

    private ThreadAnalysisIndexMgr indexMgr;

    public ThreadAnalysisQueryHandler(String cName, String feature) {

        super(cName, feature);
        client = (ESClient) this.getConfigManager().getComponent(this.feature, "ESClient");

        timeout = DataConvertHelper
                .toLong(this.getConfigManager().getFeatureConfiguration(this.feature, "es.query.timeout"), 5000);

        indexMgr = (ThreadAnalysisIndexMgr) this.getConfigManager().getComponent(this.feature,
                "ThreadAnalysisIndexMgr");
    }

    @Override
    public String getContextPath() {

        return "/jta/q";
    }

    @Override
    public void handle(UAVHttpMessage data) {

        if (log.isDebugEnable()) {
            log.debug(this, "JTA Server Query Handler start : " + data.getIntent() + ", request: "
                    + JSONHelper.toString(data.getRequest()));
        }
        String cmd = data.getIntent();

        switch (cmd) {

            /**
             * 所有 字段查询
             */
            case "qField":
                queryByField(data);
                break;
            /**
             * 单字段去重计数
             */
            case "qDistinct":
                queryDistinct(data);
                break;
            /**
             * 线程概况：总CPU、线程状态、是否死锁等
             */
            case "queryDumpInfo":
                queryDumpInfo(data);
                break;
            /**
             * 线程锁依赖
             */
            case "queryThreadChain":
                queryThreadChain(data);
                break;
            /**
             * 生成线程有向图
             */
            case "queryThreadDigraph":
                queryThreadDigraph(data);
                break;
            /**
             * 
             */
            case "queryMultiDumpInfo":
                queryMultiDumpInfo(data);
                break;
            /**
             * 
             */
            case "queryMultiDumpGraph":
                queryMultiDumpGraph(data);
                break;
            default:
                log.warn(this, "No action with cmd: " + cmd);
                break;
        }

        if (log.isDebugEnable()) {
            log.debug(this, "JTA Server Query Handler end : " + data.getIntent() + ", response: "
                    + data.getResponseAsJsonString());
        }
    }

    /**
     * 官网上的 【Top Hits Aggregation】 JAVA API 运行报错，
     * 
     * @see https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/_metrics_aggregations.html
     * 
     * 
     *      <pre>
     * 
     * {@code
    * AggregationBuilder aggregation =
        AggregationBuilders
            .terms("agg").field("gender")
            .subAggregation(
                    AggregationBuilders.topHits("top")
                        .explain(true)
                        .size(1)
                        .from(10)
            );
    * }
     *      </pre>
     * 
     *      Caused by: java.lang.IllegalArgumentException: An SPI class of type org.apache.lucene.codecs.PostingsFormat
     *      with name 'Lucene50' does not exist. You need to add the corresponding JAR file supporting this SPI to your
     *      classpath. The current classpath supports the following names: [completion, completion090]
     *      <p>
     *      以下满足张真要求的ES的查询可以工作，但找不到对应的JAVA API（原因在上）？？！！！
     * 
     *      <pre>
     * 
     * {@code
    * {
    "aggs": {
        "time": {
            "terms": {
                "field": "time",
                "order":{"_term":"desc"},
                "size": 1000
            },
            "aggs": {
                "example": {
                    "top_hits": {
                        "sort": [
                            {
                                "percpu": {
                                    "order": "desc"
                                }
                            }
                        ],
                        "size": 1
                    }
                }
            }
        }
    },
    "from": 0,
    "size": 0
    
    }
    * }
     *      </pre>
     * 
     * @param data
     */
    private void queryDistinct(UAVHttpMessage data) {

        try {
            String ipport = data.getRequest("ipport");
            // TODO ES aggregation 默认最多查10条, 这里暂时改到1000，待refine
            AggregationBuilder agg = AggregationBuilders.terms("unique_time").field("time").size(1000)
                    .order(Terms.Order.term(false))
                    .subAggregation(AggregationBuilders.terms("unique_user").field("user").size(1000));

            String date = data.getRequest("indexdate");
            String currentIndex;
            if (date != null) {
                // 指定index
                currentIndex = this.indexMgr.getIndexByDate(date);
            }
            else {
                // current index
                currentIndex = this.indexMgr.getCurrentIndex();
            }

            SearchResponse sResponse = client.getClient().prepareSearch(currentIndex)
                    .setTypes(ThreadAnalysisIndexMgr.JTA_TABLE)
                    .setQuery(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("ipport", ipport))).setSize(0)
                    .addAggregation(agg).execute().actionGet();

            // sr is here your SearchResponse object
            Terms aggs = sResponse.getAggregations().get("unique_time");

            List<Map<String, Object>> records = new ArrayList<Map<String, Object>>();
            // For each entry
            for (Terms.Bucket entry : aggs.getBuckets()) {
                String key = entry.getKey().toString(); // bucket key
                long docCount = entry.getDocCount(); // Doc count
                Map<String, Object> record = new HashMap<String, Object>();
                record.put("time", key);
                record.put("threadcount", docCount);
                Terms userAggs = entry.getAggregations().get("unique_user");
                List<Terms.Bucket> users = userAggs.getBuckets();
                if (!users.isEmpty()) {
                    record.put("user", users.get(0).getKey().toString());
                }
                records.add(record);
            }
            data.putResponse("rs", JSONHelper.toString(records));
            // 返回总的条数
            data.putResponse("count", aggs.getBuckets().size() + "");
        }
        catch (Exception e) {
            if (e.getMessage().indexOf("no such index") >= 0) {
                data.putResponse("rs", "NO_INDEX");
            }
            else {
                data.putResponse("rs", "ERR");
                log.err(this, "query distinct FAILED. " + JSONHelper.toString(data), e);
            }
        }
    }

    /**
     * 所有 字段查询
     * 
     * @param data
     */
    private void queryByField(UAVHttpMessage data) {

        QueryBuilder query = buildQuery(data);
        if (query == null) {
            return;
        }

        SearchResponse sr = query(data, query, null, buildSorts(data));

        List<Map<String, Object>> records = getRecords(sr);
        long count = getCount(sr);

        data.putResponse("rs", JSONHelper.toString(records));
        data.putResponse("count", count + ""); // 返回总的条数
    }

    /**
     * 
     * @param data
     * @return josnString: {cpu: '50', threadCount: '123', runnableCount: '23', blockedCount: '34', waitingCount: '45',
     *         deadlock: []}
     */
    private void queryDumpInfo(UAVHttpMessage data) {

        QueryBuilder query = buildQuery(data);
        if (query == null) {
            return;
        }
        SearchResponse sr = query(data, query, null, buildSorts(data));
        List<Map<String, Object>> records = getRecords(sr);

        ThreadAnalyser ta = (ThreadAnalyser) getConfigManager().getComponent(feature, "ThreadAnalyser");
        Map<String, Object> map = ta.queryDumpInfo(records);

        data.putResponse("rs", JSONHelper.toString(map));
    }

    /**
     * 查找线程依赖
     * 
     * @param data
     */
    private void queryThreadChain(UAVHttpMessage data) {

        QueryBuilder query = buildQuery(data);
        if (query == null) {
            return;
        }

        SearchResponse sr = query(data, query, null, buildSorts(data));

        List<Map<String, Object>> records = getRecords(sr);

        String threadId = data.getRequest("threadId");
        ThreadAnalyser ta = (ThreadAnalyser) getConfigManager().getComponent(feature, "ThreadAnalyser");
        List<ThreadObject> chain = ta.findThreadChain(records, threadId);
        List<String> infoChain = new ArrayList<>();
        for (ThreadObject to : chain) {
            infoChain.add(to.getInfo());
        }
        data.putResponse("rs", JSONHelper.toString(infoChain));
    }

    /**
     * 
     * @param data
     * @return {nodes: [{id:0, name:'', type: '', state: ''}], edges: [{from:0, to:1}]}
     */
    private void queryThreadDigraph(UAVHttpMessage data) {

        QueryBuilder query = buildQuery(data);
        if (query == null) {
            return;
        }

        SearchResponse sr = query(data, query, null, buildSorts(data));

        List<Map<String, Object>> records = getRecords(sr);
        ThreadAnalyser ta = (ThreadAnalyser) getConfigManager().getComponent(feature, "ThreadAnalyser");
        Map<String, Object> rs = ta.buildThreadDigraph(records);
        data.putResponse("rs", JSONHelper.toString(rs));
    }

    private void queryMultiDumpInfo(UAVHttpMessage data) {

        String ipport = data.getRequest("ipport");
        String timesStr = data.getRequest("times");
        List<String> times = JSONHelper.toObjectArray(timesStr, String.class);
        List<List<Map<String, Object>>> records = new ArrayList<>();
        for (String time : times) {
            long timestamp = DataConvertHelper.toLong(time, -1L);
            // build query builder
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.must(QueryBuilders.rangeQuery("time").gte(timestamp).lte(timestamp));
            queryBuilder.must(QueryBuilders.termQuery("ipport", ipport));
            SearchResponse sr = query(data, queryBuilder, null, buildSorts(data));
            List<Map<String, Object>> record = getRecords(sr);

            records.add(record);
        }

        ThreadAnalyser ta = (ThreadAnalyser) getConfigManager().getComponent(feature, "ThreadAnalyser");
        List<Map<String, String>> rs = ta.queryMutilDumpInfo(times, records);
        data.putResponse("rs", JSONHelper.toString(rs));
    }

    private void queryMultiDumpGraph(UAVHttpMessage data) {

        String ipport = data.getRequest("ipport");
        String timesStr = data.getRequest("times");
        String threadIdsStr = data.getRequest("threadIds");
        List<String> times = JSONHelper.toObjectArray(timesStr, String.class);
        List<String> threadIds = JSONHelper.toObjectArray(threadIdsStr, String.class);
        List<List<Map<String, Object>>> records = new ArrayList<>();
        for (String time : times) {
            long timestamp = DataConvertHelper.toLong(time, -1L);
            // build query builder
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.must(QueryBuilders.rangeQuery("time").gte(timestamp).lte(timestamp));
            queryBuilder.must(QueryBuilders.termQuery("ipport", ipport));
            SearchResponse sr = query(data, queryBuilder, null, buildSorts(data));
            List<Map<String, Object>> record = getRecords(sr);

            records.add(record);
        }

        ThreadAnalyser ta = (ThreadAnalyser) getConfigManager().getComponent(feature, "ThreadAnalyser");
        Map<String, Object> rs = ta.queryMutilDumpGraph(threadIds, records);
        data.putResponse("rs", JSONHelper.toString(rs));
    }

    private QueryBuilder buildQuery(UAVHttpMessage data) {

        long startTime = DataConvertHelper.toLong(data.getRequest("stime"), -1L);
        long endTime = DataConvertHelper.toLong(data.getRequest("etime"), -1L);

        if (startTime == -1L || endTime == -1L || endTime < startTime) {
            data.putResponse("rs", "ERR");
            data.putResponse("msg", "The Time Range Error: startTime=" + startTime + ",endTime=" + endTime);
            return null;
        }

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

        // time
        queryBuilder.must(QueryBuilders.rangeQuery("time").gte(startTime).lte(endTime));
        // user
        String user = data.getRequest("user");
        if (user != null) {
            queryBuilder.must(QueryBuilders.termQuery("user", user));
        }
        // pname，已做分词
        String pname = data.getRequest("pname");
        if (pname != null) {
            queryBuilder.must(QueryBuilders.matchQuery("pname", pname));
        }
        // ipport
        String ipport = data.getRequest("ipport");
        if (ipport != null) {
            queryBuilder.must(QueryBuilders.termQuery("ipport", ipport));
        }
        // pid
        String pid = data.getRequest("pid");
        if (pid != null) {
            queryBuilder.must(QueryBuilders.termQuery("pid", pid));
        }
        // appgroup
        String appgroup = data.getRequest("appgroup");
        if (appgroup != null) {
            queryBuilder.must(QueryBuilders.termQuery("appgroup", appgroup));
        }
        // tid
        String tid = data.getRequest("tid");
        if (tid != null) {
            queryBuilder.must(QueryBuilders.termQuery("tid", tid));
        }
        // state
        String state = data.getRequest("state");
        if (state != null) {
            queryBuilder.must(QueryBuilders.termQuery("state", state));
        }
        // timeadd
        String timeadd = data.getRequest("timeadd");
        if (timeadd != null) {
            queryBuilder.must(QueryBuilders.termQuery("timeadd", timeadd));
        }
        final double PRECISION = 0.0000001F;
        // percpu
        double spercpu = DataConvertHelper.toDouble(data.getRequest("spercpu"), -1.0F);
        double epercpu = DataConvertHelper.toDouble(data.getRequest("epercpu"), -1.0F);
        RangeQueryBuilder percpu = QueryBuilders.rangeQuery("percpu");
        if (spercpu + 1.0F > PRECISION) {
            percpu.gte(spercpu);
        }
        if (epercpu + 1.0F > PRECISION) {
            percpu.lte(epercpu);
        }
        if (epercpu < spercpu) {
            data.putResponse("rs", "ERR");
            data.putResponse("msg", "The percpu Range Error: spercpu=" + spercpu + ",epercpu=" + epercpu);
            return null;
        }
        queryBuilder.must(percpu);
        // permem
        double spermem = DataConvertHelper.toDouble(data.getRequest("spermem"), -1.0F);
        double epermem = DataConvertHelper.toDouble(data.getRequest("epermem"), -1.0F);
        RangeQueryBuilder permem = QueryBuilders.rangeQuery("permem");
        if (spermem + 1.0F > PRECISION) {
            permem.gte(spermem);
        }
        if (epermem + 1.0F > PRECISION) {
            permem.lte(epermem);
        }
        if (epermem < spermem) {
            data.putResponse("rs", "ERR");
            data.putResponse("msg", "The permem Range Error: spercpu=" + spermem + ",epercpu=" + epermem);
            return null;
        }
        queryBuilder.must(permem);
        // info，已做分词
        String info = data.getRequest("info");
        if (info != null) {
            queryBuilder.must(QueryBuilders.matchQuery("info", info));
        }

        return queryBuilder;
    }

    @SuppressWarnings("rawtypes")
    private List<SortBuilder> buildSorts(UAVHttpMessage data) {

        List<SortBuilder> list = new ArrayList<>();

        String sort = data.getRequest("sort");

        if (sort != null) {
            String[] sortFieldStrs = sort.split(",");
            for (String sortFieldStr : sortFieldStrs) {
                String[] sortExp = sortFieldStr.split("=");
                SortBuilder stimeSort = new FieldSortBuilder(sortExp[0]);
                stimeSort.order(SortOrder.fromString(sortExp[1]));
                list.add(stimeSort);
            }
        }
        else {
            SortBuilder stimeSort = new FieldSortBuilder("time");
            stimeSort.order(SortOrder.DESC);
            list.add(stimeSort);
        }
        return list;
    }

    @SuppressWarnings("rawtypes")
    private SearchResponse query(UAVHttpMessage data, QueryBuilder queryBuilder, QueryBuilder postFilter,
            List<SortBuilder> sorts) {

        String date = data.getRequest("indexdate");
        String currentIndex;
        if (date != null) {
            // 指定index
            currentIndex = this.indexMgr.getIndexByDate(date);
        }
        else {
            // current index
            currentIndex = this.indexMgr.getCurrentIndex();
        }

        SearchRequestBuilder srb = client.getClient().prepareSearch(currentIndex)
                .setTypes(ThreadAnalysisIndexMgr.JTA_TABLE).setSearchType(SearchType.DFS_QUERY_THEN_FETCH);

        int from = DataConvertHelper.toInt(data.getRequest("from"), -1);
        int size = DataConvertHelper.toInt(data.getRequest("size"), -1);

        if (from != -1 && size != -1) {
            srb = srb.setFrom(from).setSize(size);
        }

        srb.setQuery(queryBuilder);

        if (postFilter != null) {
            srb.setPostFilter(postFilter);
        }

        for (SortBuilder sb : sorts) {
            srb.addSort(sb);
        }

        SearchResponse sr = srb.get(TimeValue.timeValueMillis(timeout));

        return sr;
    }

    private List<Map<String, Object>> getRecords(SearchResponse sr) {

        SearchHits shits = sr.getHits();

        List<Map<String, Object>> records = new ArrayList<Map<String, Object>>();

        for (SearchHit sh : shits) {
            Map<String, Object> record = sh.getSourceAsMap();
            if (record == null) {
                continue;
            }
            records.add(record);
        }

        return records;
    }

    private long getCount(SearchResponse sr) {

        return sr.getHits().getTotalHits();
    }

}
