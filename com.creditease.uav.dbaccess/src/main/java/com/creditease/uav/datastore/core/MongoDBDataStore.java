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

package com.creditease.uav.datastore.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.creditease.agent.helpers.DataStoreHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.uav.datastore.api.DataStoreAdapter;
import com.creditease.uav.datastore.api.DataStoreConnection;
import com.creditease.uav.datastore.api.DataStoreMsg;
import com.creditease.uav.datastore.api.DataStoreProtocol;
import com.creditease.uav.datastore.mongo.MongodbAggregateStrategy.GroupStrategy;
import com.creditease.uav.datastore.mongo.MongodbAggregateStrategy.MatchStrategy;
import com.creditease.uav.datastore.mongo.MongodbAggregateStrategy.ProjectStrategy;
import com.creditease.uav.datastore.mongo.MongodbAggregateStrategy.UnwindStrategy;
import com.creditease.uav.datastore.mongo.MongodbImplStrategy.QueryStrategy;
import com.creditease.uav.datastore.mongo.MongodbImplStrategy.UpdateStrategy;
import com.creditease.uav.datastore.source.AbstractDataSource;
import com.creditease.uav.datastore.source.MongoDBDataSource;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

/**
 * @author peihua
 */

public class MongoDBDataStore extends AbstractDataStore<MongoDatabase> {

    public MongoDBDataStore(DataStoreConnection connectObj, DataStoreAdapter adaptor, String feature) {
        super(connectObj, adaptor, feature);
    }

    @Override
    protected AbstractDataSource<MongoDatabase> getDataSource(DataStoreConnection obj) {

        return new MongoDBDataSource(obj);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean insert(DataStoreMsg msg) {

        boolean isSuccess = false;

        String collectionName = (String) msg.get(DataStoreProtocol.MONGO_COLLECTION_NAME);

        MongoCollection<Document> collection = this.datasource.getSourceConnect().getCollection(collectionName);

        // collection no exist
        if (null == collection) {

            if (log.isTraceEnable()) {
                log.warn(this, "MongoDB[" + this.datasource.getDataStoreConnection().getDbName() + "] Collection["
                        + collectionName + "] NO EXIST.");
            }

            return adaptor.handleInsertResult(isSuccess, msg, this.datasource.getDataStoreConnection());

        }

        // prepare documents
        List<Map<String, Object>> documents = (List<Map<String, Object>>) adaptor.prepareInsertObj(msg,
                this.datasource.getDataStoreConnection());

        if (null != documents) {

            // convert to Document Object
            List<Document> docs = new ArrayList<Document>();

            for (Map<String, Object> dMap : documents) {

                Document doc = new Document();

                for (String key : dMap.keySet()) {
                    doc.append(key, dMap.get(key));
                }

                docs.add(doc);
            }

            // insert documents
            try {
                collection.insertMany(docs);

                isSuccess = true;

            }
            catch (MongoException e) {
                log.err(this, "INSERT MongoDB[" + this.datasource.getDataStoreConnection().getDbName() + "] Collection["
                        + collectionName + "] Documents FAIL.", e);
            }
        }

        return adaptor.handleInsertResult(isSuccess, msg, this.datasource.getDataStoreConnection());
    }

    /**
     * 1）满足隔离性，adaptor里面不能使用MongoClient的类 2）使得datastore中query逻辑更加通用
     */
    @SuppressWarnings({ "rawtypes" })
    @Override
    protected List query(DataStoreMsg msg) {

        // init
        String collectionName = (String) msg.get(DataStoreProtocol.MONGO_COLLECTION_NAME);
        DataStoreConnection connection = this.datasource.getDataStoreConnection();
        MongoCollection<Document> collection = this.datasource.getSourceConnect().getCollection(collectionName);
        if (null == collection) {
            log.warn(this,
                    "MongoDBDataStore [" + connection.getDbName() + "] Collection[" + collectionName + "] NO EXIST.");
            return adaptor.handleQueryResult(Collections.emptyList(), msg, connection);
        }
        else {
            log.info(this, "MongoDBDataStore [" + connection.getDbName() + "] Collection[" + collectionName + "] ");
        }

        // action
        Object queryObj = adaptor.prepareQueryObj(msg, connection);
        List<Map> result = Collections.emptyList();
        if (queryObj instanceof List) {
            result = aggregateAction(queryObj, collection);
        }
        else if (queryObj instanceof Map) {
            Map queryparmes = (Map) queryObj;
            if (queryparmes.containsKey(DataStoreProtocol.COUNT)) {
                result = countAction(msg, queryparmes, collection);
            }
            else {
                result = findAction(queryparmes, collection);
            }

        }
        else {
            log.warn(this, "MongoDBDataStore Query Action Is Not Find. ");
        }

        // return result
        if (log.isDebugEnable()) {
            log.debug(this, "MongoDBDataStore Query Result :" + result.toString());
        }

        return adaptor.handleQueryResult(result, msg, connection);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected boolean update(DataStoreMsg msg) {

        boolean isSuccess = false;
        String collectionName = (String) msg.get(DataStoreProtocol.MONGO_COLLECTION_NAME);

        MongoCollection<Document> collection = this.datasource.getSourceConnect().getCollection(collectionName);

        // collection no exist
        if (null == collection) {

            if (log.isTraceEnable()) {
                log.warn(this, "MongoDB[" + this.datasource.getDataStoreConnection().getDbName() + "] Collection["
                        + collectionName + "] NO EXIST.");
            }

            return adaptor.handleUpdateResult(isSuccess, msg, this.datasource.getDataStoreConnection());
        }
        // prepare query
        String updateObj = (String) adaptor.prepareUpdateObj(msg, this.datasource.getDataStoreConnection());

        // @SuppressWarnings("rawtypes")
        Map params = JSONHelper.toObject(DataStoreHelper.decorateInForMongoDB(updateObj), Map.class);

        BasicDBObject condition = new BasicDBObject();
        BasicDBObject update = new BasicDBObject();
        boolean isRemove = true;
        for (Object keyObj : params.keySet()) {
            if (keyObj.toString().equals(DataStoreProtocol.WHERE)) {
                QueryStrategy qry = new QueryStrategy();
                qry.concretProcessor(DataStoreProtocol.WHERE, params, condition);

            }
            else if (keyObj.toString().equals(DataStoreProtocol.UPDATE)) {
                isRemove = false;
                UpdateStrategy up = new UpdateStrategy();
                up.concretProcessor(DataStoreProtocol.UPDATE, params, update);
            }
            else {
                log.err(this, "can not figure out, please check it out " + keyObj.toString());
            }

        }

        if (isRemove) {
            log.info(this, "condition: " + condition.toString());
            DeleteResult res = collection.deleteMany(condition);
            log.info(this, "DeletedCount:" + res.getDeletedCount());
            isSuccess = true;
        }
        else {
            log.info(this, "condition: " + condition.toString());
            log.info(this, "update: " + update.toString());
            UpdateResult res = collection.updateMany(condition, update);
            log.info(this, "ModifiedCount:" + res.getModifiedCount());
            isSuccess = true;
        }
        return adaptor.handleUpdateResult(isSuccess, msg, this.datasource.getDataStoreConnection());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private List<Map> countAction(DataStoreMsg msg, Map queryparmes, MongoCollection<Document> collection) {

        BasicDBObject query = new BasicDBObject();// output

        Map findparmes = (Map) queryparmes.get(DataStoreProtocol.WHERE);
        QueryStrategy qry = new QueryStrategy();
        Map express = new LinkedHashMap();
        express.put(DataStoreProtocol.FIND, findparmes);
        qry.concretProcessor(DataStoreProtocol.FIND, express, query);

        // for (Object qobj : query.keySet()) {
        // log.info(this, "shell in package:" + qobj.toString() + ":" + query.get(qobj));
        // }

        log.info(this, "MongoDBDataStore countAction toJson : " + query.toJson());

        long countN = collection.count(query);
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put(DataStoreProtocol.COUNT, countN);
        List<Map> res = new ArrayList<Map>();
        res.add(item);

        return res;

    }

    /**
     * 聚集: 1、SQL的执行顺序，按解析顺序封装。如有SQL优化，请调整入参前顺序 2、 默认排序:id降序 3、如果存在分页SQL封装,SQL分页执行顺序：最后
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private List<Map> aggregateAction(Object object, MongoCollection<Document> collection) {

        int pageIndex = 0; // 初始 index
        int pageSize = 0; // 初始pageSize
        boolean defalutSort = true;
        boolean doCount = false;
        List<Bson> list = new ArrayList<Bson>();

        List<Map> elemDatas = (List<Map>) object;
        for (Map elemData : elemDatas) {

            for (Object keyObj : elemData.keySet()) {
                if (keyObj.toString().equals(DataStoreProtocol.UNWIND)) {
                    UnwindStrategy process = new UnwindStrategy();
                    process.concretProcessor(DataStoreProtocol.UNWIND, elemData, list);

                }
                else if (keyObj.toString().equals(DataStoreProtocol.GROUP)) {
                    GroupStrategy process = new GroupStrategy();

                    process.concretProcessor(DataStoreProtocol.GROUP, elemData, list);

                    ProjectStrategy proj = new ProjectStrategy();
                    proj.concretProcessor(null, null, list);

                }
                else if (keyObj.toString().equals(DataStoreProtocol.MATCH)) {
                    MatchStrategy process = new MatchStrategy();
                    process.concretProcessor(DataStoreProtocol.MATCH, elemData, list);
                }
                else if (keyObj.toString().equals(DataStoreProtocol.FIELDS)) {
                    ProjectStrategy proj = new ProjectStrategy();
                    proj.concretProcessor(DataStoreProtocol.FIELDS, elemData, list);
                }
                else if (keyObj.toString().equals(DataStoreProtocol.COUNT)) {
                    doCount = true;
                    list.add(new BasicDBObject("$group", new BasicDBObject("_id", null).append(DataStoreProtocol.COUNT,
                            new BasicDBObject("$sum", 1))));
                }
                else if (keyObj.toString().equals(DataStoreProtocol.SORT)) {
                    defalutSort = false;
                    String sortJson = String.valueOf(elemData.get(DataStoreProtocol.SORT));
                    Map<String, String> sortMap = JSONHelper.toObject(sortJson, Map.class);
                    String sortParmes = sortMap.get(DataStoreProtocol.VALUES);
                    String sortOrder = sortMap.get(DataStoreProtocol.SORTORDER);

                    Document sortDocs = new Document();
                    String[] parames = sortParmes.split(",");
                    for (String parame : parames) {
                        sortDocs.append(parame, Integer.parseInt(sortOrder));
                    }
                    list.add(new BasicDBObject("$sort", sortDocs));
                }
                else if (keyObj.toString().equals(DataStoreProtocol.SKIP)) {
                    list.add(new BasicDBObject("$skip",
                            Integer.valueOf(String.valueOf(elemData.get(DataStoreProtocol.SKIP)))));
                }
                else if (keyObj.toString().equals(DataStoreProtocol.LIMIT)) {
                    list.add(new BasicDBObject("$limit",
                            Integer.valueOf(String.valueOf(elemData.get(DataStoreProtocol.LIMIT)))));
                }
                /**
                 * 源生语法以外：分页解析 begin
                 */
                else if (keyObj.toString().equals(DataStoreProtocol.PAGEINDEX)) {
                    String index = (String) elemData.get(DataStoreProtocol.PAGEINDEX);
                    pageIndex = Integer.parseInt(index);

                }
                else if (keyObj.toString().equals(DataStoreProtocol.PAGESIZE)) {
                    String size = (String) elemData.get(DataStoreProtocol.PAGESIZE);
                    pageSize = Integer.parseInt(size);
                }
                /**
                 * 源生语法以外：分页解析 end
                 */
                else {
                    log.err(this, "can not figure out, please check it out");
                }
            }
        }

        /**
         * 默认排序:id降序
         */
        if (defalutSort) {
            Document defaultDoc = new Document();
            defaultDoc.append("_id", -1);
            list.add(new BasicDBObject("$sort", defaultDoc));
        }

        /**
         * 分页SQL封装,SQL顺序：最后
         */
        if (pageIndex > 0 && pageSize > 0) {
            pageIndex = (pageIndex - 1) * pageSize;
            list.add(new BasicDBObject("$skip", pageIndex)); // 源生语义：跳过指定数量
            list.add(new BasicDBObject("$limit", pageSize)); // 源生语义：读取指定数量
        }

        log.info(this, "MongoDBDataStore aggregateAction toJson : " + JSONHelper.toString(list));

        AggregateIterable<Document> output = collection.aggregate(list).allowDiskUse(true);
        MongoCursor<Document> cursor = output.iterator();

        return queryResultFormat(cursor, doCount);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private List<Map> findAction(Map queryObj, MongoCollection<Document> collection) {

        Map queryparmes = queryObj; // input
        BasicDBObject query = new BasicDBObject();// output

        Map findparmes = (Map) queryparmes.get(DataStoreProtocol.WHERE);
        String fileds = String.valueOf(queryparmes.get(DataStoreProtocol.FIELDS));
        String sortJson = String.valueOf(queryparmes.get(DataStoreProtocol.SORT));
        String pageindex = String.valueOf(queryparmes.get(DataStoreProtocol.PAGEINDEX));
        String pagesize = String.valueOf(queryparmes.get(DataStoreProtocol.PAGESIZE));

        QueryStrategy qry = new QueryStrategy();
        Map express = new LinkedHashMap();
        express.put(DataStoreProtocol.FIND, findparmes);
        qry.concretProcessor(DataStoreProtocol.FIND, express, query);

        Document sorts = new Document();
        Document filterBson = new Document();

     // filterBson.append("_id", 0); //代码含义：查询返回结果中，不包含mongodb的_id字段

        if (!StringHelper.isEmpty(fileds)) {
            String[] filters = fileds.split(";");
            for (String filter : filters) {
                filterBson.append(filter, 1);
            }
        }
        if (!StringHelper.isEmpty(sortJson)) {
            Map<String, String> sortMap = JSONHelper.toObject(sortJson, Map.class);
            String sortParmes = sortMap.get(DataStoreProtocol.VALUES);
            String sortOrder = sortMap.get(DataStoreProtocol.SORTORDER);
            String[] parames = sortParmes.split(",");
            for (String parame : parames) {
                sorts.append(parame, Integer.parseInt(sortOrder));
            }
        }

        int pageIndex = 0; // 初始 index
        int pageSize = 0; // 初始pageSize
        if (!StringHelper.isEmpty(pageindex)) {
            pageIndex = Integer.parseInt(pageindex);
        }

        if (!StringHelper.isEmpty(pagesize)) {
            pageSize = Integer.parseInt(pagesize);
        }

        if (log.isDebugEnable()) {
            StringBuilder sb = new StringBuilder();
            for (Object qobj : query.keySet()) {
                sb.append("\r\nshell in package:" + qobj.toString() + ":" + query.get(qobj));
            }
            sb.append("\r\nfilterBson:" + filterBson);
            sb.append("\r\npageIndex:" + pageIndex);
            sb.append("\r\npageSize:" + pageSize);
            sb.append("\r\nsorts:" + sorts);

            log.debug(this, sb.toString());
        }

        log.info(this, "MongoDBDataStore findAction toJson : " + query.toJson());

        MongoCursor<Document> cursor = null;
        if (pageIndex > 0 && pageSize > 0) {
            cursor = collection.find(query).projection(filterBson).sort(sorts).skip((pageIndex - 1) * pageSize)
                    .limit(pageSize).iterator();
        }
        else {
            cursor = collection.find(query).projection(filterBson).sort(sorts).iterator();
        }

        return queryResultFormat(cursor, false);

    }

    @SuppressWarnings("rawtypes")
    private List<Map> queryResultFormat(MongoCursor<Document> cursor, boolean checkCount) {

        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            Map<String, Object> docInfo = new LinkedHashMap<String, Object>();
            for (String key : doc.keySet()) {
                if ("_id".equals(key) && doc.getObjectId("_id") != null) {
                    /**
                     * _id 是mongodb的对象，只获取_id的ObjectId String值（返回数据）
                     */
                    docInfo.put("_id", doc.getObjectId("_id").toString());
                    continue;
                }
                docInfo.put(key, doc.get(key));
            }
            results.add(docInfo);
        }

        /*
         * 在做计数时，如果没有数据，find会返回{count=0}，而aggregate什么都不返回 为了保证输出结果统一，做了一下适配
         */
        if (checkCount && results.isEmpty()) {
            Map<String, Object> docInfo = new LinkedHashMap<String, Object>();
            docInfo.put("count", 0);
            results.add(docInfo);
        }

        String decodeStr = DataStoreHelper.decodeForMongoDB(JSONHelper.toString(results));

        return JSONHelper.toObjectArray(decodeStr, Map.class);
    }
}
