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

package com.creditease.uav.datastore.api;

/**
 * provide all Protocols
 * 
 * @author zhen zhang
 *
 */
public class DataStoreProtocol {

    private DataStoreProtocol() {
    }

    /**
     * DataStore related
     */
    public static final String DATASTORE_NAME = "datastore.name";

    /**
     * MONGO related
     */
    public static final String MONGO_COLLECTION_NAME = "mgo.coll.name";
    public static final String MONGO_QUERY_SQL = "mgo.sql";

    public static final String MONGO_REQUEST_DATA = "mrd.data";

    // operation constant
    public static final String WHERE = "where";
    public static final String FIND = "find";
    public static final String UNWIND = "unwind";
    public static final String GROUP = "group";
    public static final String MATCH = "match";
    public static final String UPDATE = "update";

    public static final String COUNT = "count";
    public static final String RESULT = "result";
    public static final String DISTINCT = "distinct";
    public static final String VALUE = "value";
    public static final String VALUES = "values";
    public static final String SKIP = "skip";
    public static final String LIMIT = "limit";

    // other configuration param
    public static final String PAGEINDEX = "pageindex";
    public static final String PAGESIZE = "pagesize";
    public static final String FIELDS = "fields";
    public static final String SORT = "sort";
    public static final String SORTORDER = "sortorder";

    // Mongodb http operation type
    public static final String MONGO_OPERATION = "mongo.opt";
    public static final String MONGO_INSERT = "insert";
    public static final String MONGO_QUERY = "query";
    public static final String MONGO_UPDATE = "update";

    /**
     * Mysql related
     */
    public static final String MYSQL_DATABASE_NAME = "mysql.db.name";
    public static final String MYSQL_TABLE_NAME = "mysql.table.name";

    /**
     * HBase related
     */
    public static final String HBASE_TABLE_NAME = "hbase.table.name";
    public static final String HBASE_ZK_PORT = "hbase.zookeeper.property.clientPort";
    public static final String HBASE_ZK_QUORUM = "hbase.zookeeper.quorum";
    public static final String HBASE_QUERY_CACHING = "hbase.query.caching";
    public static final String HBASE_QUERY_MAXRESULTSIZE = "hbase.query.maxResultSize";
    public static final String HBASE_QUERY_REVERSE = "hbase.query.reverse";
    public static final String HBASE_QUERY_JSON_KEY = "hbase.query.json";
    public static final String HBASE_FAMILY_NAME = "hbase.family.name";
    public static final String HBASE_QUERY_STARTROW = "hbase.query.startrow";
    public static final String HBASE_QUERY_ENDROW = "hbase.query.endrow";
    public static final String HBASE_QUERY_ROW_KEYVALUE = "hbase.query.row.kv";
    public static final String HBASE_QUERY_COL_KEYVALUE = "hbase.query.col.kv";
    public static final String HBASE_QUERY_PAGESIZE = "hbase.query.page.size";
    // public static final String HBASE_ZK_DATADIR = "hbase.zookeeper.property.dataDir";

    /**
     * opentsdb related
     */
    public static final String OPENTSDB_CLIENT_MAXPERROUTE = "opentsdb.httpclient.maxConnectionPerRoute";
    public static final String OPENTSDB_CLIENT_MAXROUTE = "opentsdb.httpclient.maxTotalConnection";
    public static final String OPENTSDB_CLIENT_SOTIMEOUT = "opentsdb.httpclient.sockTimeout";
    public static final String OPENTSDB_CLIENT_CONNTIMEOUT = "opentsdb.httpclient.connTimeout";
    public static final String OPENTSDB_CLIENT_REQTIMEOUT = "opentsdb.httpclient.reqTimeout";
    public static final String OPENTSDB_QUERY_KEY = "opentsdb.query.json";
    public static final String OPENTSDB_QUERY_RETCODE = "opentsdb.query.retcode";
    public static final String OPENTSDB_QUERY_RETMSG = "opentsdb.query.retmsg";
    public static final String OPENTSDB_INSERT_RETCODE = "opentsdb.insert.retcode";
    public static final String OPENTSDB_INSERT_RETMSG = "opentsdb.insert.retmsg";
    public static final String OPENTSDB_INSERT_BATCHSIZE = "opentsdb.insert.batchsize";
}
