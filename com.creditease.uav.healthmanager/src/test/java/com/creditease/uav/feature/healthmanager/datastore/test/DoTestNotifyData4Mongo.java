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

import java.util.ArrayList;
import java.util.List;

import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.uav.datastore.api.DataStoreConnection;
import com.creditease.uav.datastore.api.DataStoreFactory;
import com.creditease.uav.datastore.api.DataStoreFactory.DataStoreType;
import com.creditease.uav.datastore.api.DataStoreMsg;
import com.creditease.uav.datastore.api.DataStoreProtocol;
import com.creditease.uav.datastore.core.AbstractDataStore;
import com.creditease.uav.feature.healthmanager.datastore.adaptors.NotifyDataAdpater;

public class DoTestNotifyData4Mongo {

    private static String host = "127.0.0.1";

    private static int mongos1port = 27017;

    // private static int mongos1port = 30001;

    @SuppressWarnings("unused")
    private static int mongos2port = 30002;

    private static String dbName = "notifyDataStore";

    private static String userName = "root";

    private static String password = "root";

    private static String insertJson = "src/test/java/testData/Insert/notify.json";

    private static String queryJson = "src/test/java/testData/MongoDBQuery/DSAggregate4Notification.json";

    private static List<String> serverlist = new ArrayList<String>();

    static {
        serverlist.add(new String(host + ":" + mongos1port));
        serverlist.add(new String(host + ":" + mongos1port));
    }

    @SuppressWarnings({ "rawtypes" })
    public static void testInsertNotifyMongoDB() {

        DataStoreMsg msg = new DataStoreMsg();
        // MongoDBHandler
        String rawData = DataStoreUnitTest.getData(insertJson);

        msg.put(MonitorDataFrame.MessageType.Notification.toString(), rawData);
        msg.put(DataStoreProtocol.MONGO_COLLECTION_NAME, HealthManagerConstants.MONGO_COLLECTION_NOTIFY);

        DataStoreConnection obj = new DataStoreConnection(userName, password, dbName, serverlist,
                DataStoreType.MONGODB);

        AbstractDataStore store = DataStoreFactory.getInstance().build(HealthManagerConstants.DataStore_Notification,
                obj, new NotifyDataAdpater(), "");

        store.start();
        boolean rst = store.doInsert(msg);
        store.stop();

        DataStoreUnitTest.printTestResult("testInsertNotifyMongoDB", rst);
    }

    @SuppressWarnings("rawtypes")
    public static void testquerytNotifyMongoDB() {

        String query = DataStoreUnitTest.getData(queryJson);

        DataStoreMsg request = new DataStoreMsg();

        request.put(DataStoreProtocol.MONGO_QUERY_SQL, query.toString());

        request.put(DataStoreProtocol.MONGO_COLLECTION_NAME, HealthManagerConstants.MONGO_COLLECTION_NOTIFY);

        DataStoreConnection obj = new DataStoreConnection(userName, password, dbName, serverlist,
                DataStoreType.MONGODB);

        AbstractDataStore store = DataStoreFactory.getInstance().build(HealthManagerConstants.DataStore_Notification,
                obj, new NotifyDataAdpater(), "");

        store.start();

        @SuppressWarnings("unchecked")
        List<Object> list = store.doQuery(request);
        store.stop();

        DataStoreUnitTest.printTestResult("testquerytNotifyMongoDB", list, query);

    }

}
