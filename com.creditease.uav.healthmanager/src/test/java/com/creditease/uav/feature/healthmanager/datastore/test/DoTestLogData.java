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

package com.creditease.uav.feature.healthmanager.datastore.test;

import java.util.List;

import org.apache.hadoop.hbase.client.Connection;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.uav.datastore.api.DataStoreConnection;
import com.creditease.uav.datastore.api.DataStoreFactory;
import com.creditease.uav.datastore.api.DataStoreFactory.DataStoreType;
import com.creditease.uav.datastore.api.DataStoreMsg;
import com.creditease.uav.datastore.api.DataStoreProtocol;
import com.creditease.uav.datastore.core.AbstractDataStore;
import com.creditease.uav.feature.healthmanager.datastore.adaptors.LogDataAdapter;

public class DoTestLogData {

    static String zklist = "127.0.0.1:2181,127.0.0.1:2181";
    static String caching = "1000";
    static String maxResultSize = "1000";
    static String insertJson = "src/test/java/testData/Insert/log.json";
    static String queryJson = "{\"starttime\": 1441812645015, \"endtime\" : 1462413645015,  \"appid\": \"com.creditease.uav.monitorframework.buildFat\"}";
    // {"MT_Log":"[{time:1462329007990,host:\"09-201211070016\",ip:\"127.0.0.1\",svrid:\"F:/testenv/apache-tomcat-7.0.65::E:/eclipse/workspace3/.metadata/.plugins/org.eclipse.wst.server.core/tmp1\",tag:\"L\",frames:{\"com.creditease.uav.monitorframework.buildFat,F:/temp/log/FAT_log1.txt\":

    /**
     * [ { "time": 1456293824385, "host": "09-201509070105", "ip": "127.0.0.1", "svrid":
     * "D:/UAV/apache-tomcat-6.0.41::D:/eclipseProject/.metadata/.plugins/org.eclipse.wst.server.core/tmp0", "tag": "L",
     * "frames": { "WebTest": [ { "content": "[CE] aaaaa" } ] } } ]
     */
    @SuppressWarnings("unchecked")
    public static void testInsertHBase() {

        // MongoDBHandler
        DataStoreMsg msg = new DataStoreMsg();
        String rawData = DataStoreUnitTest.getData(insertJson);
        msg.put(MonitorDataFrame.MessageType.Log.toString(), rawData);
        msg.put(DataStoreProtocol.HBASE_TABLE_NAME, HealthManagerConstants.HBASE_TABLE_LOGDATA);
        List<String> servers = DataConvertHelper.toList(zklist, ",");
        DataStoreConnection obj = new DataStoreConnection(null, null, null, servers, DataStoreType.HBASE);
        obj.putContext(DataStoreProtocol.HBASE_ZK_QUORUM, zklist);
        obj.putContext(DataStoreProtocol.HBASE_QUERY_CACHING, caching);
        obj.putContext(DataStoreProtocol.HBASE_QUERY_MAXRESULTSIZE, maxResultSize);

        obj.putContext(DataStoreProtocol.HBASE_QUERY_REVERSE, true);
        obj.putContext(DataStoreProtocol.HBASE_QUERY_PAGESIZE, 3000);

        AbstractDataStore<Connection> store = DataStoreFactory.getInstance().build(HealthManagerConstants.DataStore_Log,
                obj, new LogDataAdapter(), "");

        store.start();
        boolean rst = store.doInsert(msg);
        store.stop();
        DataStoreUnitTest.printTestResult("testInsertHBase", rst);
    }

    /**
     * { "starttime": 145629382438, "endtime": 145629382438, //optional "ip": "127.0.0.1", "svrid":
     * "D:/UAV/apache-tomcat-6.0.41::D:/eclipseProject/.metadata/.plugins/org.eclipse.wst.server.core/tmp0", "appid":
     * "sms" }
     */
    @SuppressWarnings("unchecked")
    public static void testQueryHBase() {

        DataStoreMsg msg = new DataStoreMsg();
        msg.put(DataStoreProtocol.HBASE_QUERY_JSON_KEY, queryJson);
        List<String> servers = DataConvertHelper.toList(zklist, ",");
        DataStoreConnection obj = new DataStoreConnection(null, null, null, servers, DataStoreType.HBASE);
        obj.putContext(DataStoreProtocol.HBASE_ZK_QUORUM, zklist);
        obj.putContext(DataStoreProtocol.HBASE_QUERY_CACHING, caching);
        obj.putContext(DataStoreProtocol.HBASE_QUERY_MAXRESULTSIZE, maxResultSize);
        AbstractDataStore<Connection> store = DataStoreFactory.getInstance().build(HealthManagerConstants.DataStore_Log,
                obj, new LogDataAdapter(), "");
        store.start();
        List<String> rst = store.doQuery(msg);
        store.stop();
        DataStoreUnitTest.printTestResult("testqueryHBase", rst, queryJson);
    }
}
