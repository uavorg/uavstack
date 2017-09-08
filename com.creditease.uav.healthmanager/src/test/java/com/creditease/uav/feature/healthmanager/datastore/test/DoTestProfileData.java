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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.uav.datastore.api.DataStoreConnection;
import com.creditease.uav.datastore.api.DataStoreFactory;
import com.creditease.uav.datastore.api.DataStoreFactory.DataStoreType;
import com.creditease.uav.datastore.api.DataStoreMsg;
import com.creditease.uav.datastore.api.DataStoreProtocol;
import com.creditease.uav.datastore.core.AbstractDataStore;
import com.creditease.uav.feature.healthmanager.datastore.adaptors.ProfileDataAdpater;

public class DoTestProfileData {

    private static String host = "127.0.0.1";
    private static int mongos1port = 27017;

    // private static int mongos1port = 30001;

    @SuppressWarnings("unused")
    private static int mongos2port = 50002;

    private static String dbName = "profileDataStore";

    private static String userName = "root";

    private static String password = "root";

    private static String insertJson = "src/test/java/testData/Insert/profile.json";

    private static String queryJsonDir = "src/test/java/testData/MongoDBQuery/";

    private static String[] updatefileName = { "DSupdate.json" };

    private static List<String> serverlist = new ArrayList<String>();

    private static String elapsetime = "src/test/java/testData/Insert/";

    static long time = System.currentTimeMillis();

    static {
        serverlist.add(new String(host + ":" + mongos1port));
        // serverlist.add(new String(host + ":" + mongos1port));
    }

    public static void fileWrite(StringBuffer sb) throws IOException {

        File file = new File(elapsetime + time + ".txt");
        if (!file.exists())
            file.createNewFile();
        FileOutputStream out = new FileOutputStream(file, true);
        sb.append("\n");
        out.write(sb.toString().getBytes("utf-8"));

        out.close();
    }

    @SuppressWarnings({ "rawtypes" })
    public static void testInsertMongoDB() {

        // MongoDBHandler
        DataStoreMsg msg = new DataStoreMsg();
        String rawData = DataStoreUnitTest.getData(insertJson);
        msg.put(MonitorDataFrame.MessageType.Profile.toString(), rawData);
        msg.put(DataStoreProtocol.MONGO_COLLECTION_NAME, HealthManagerConstants.MONGO_COLLECTION_PROFILE);

        DataStoreConnection obj = new DataStoreConnection(userName, password, dbName, serverlist,
                DataStoreType.MONGODB);

        AbstractDataStore store = DataStoreFactory.getInstance().build(HealthManagerConstants.DataStore_Profile, obj,
                new ProfileDataAdpater(), "");

        store.start();
        long start = System.currentTimeMillis();
        // boolean rst = store.doInsert(msg);
        for (int i = 0; i < 1; i++) {
            boolean rst = store.doInsert(msg);
            if (false == rst)
                break;
        }
        long stop = System.currentTimeMillis();
        StringBuffer sb = new StringBuffer();
        sb.append("insert time : " + (stop - start));
        try {
            fileWrite(sb);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        store.stop();
        // DataStoreUnitTest.printTestResult("testInsertMongoDB",rst);
    }

    @SuppressWarnings("rawtypes")
    public static void testquerytMongoDB() {

        // String[] fileName = { "DSAggregatequery.json", "DSAggregatequeryGtLt.json", "DSAggregatequeryMKV.json",
        // "DSAggregatequeryRegex_GtInOR.json", "DSAggregatequeryRegex.json", "DSAggregatequeryRegexInOR.json",
        // "DSAggregatequeryRegexMKVInOR.json", "DSfind.json" };

        String[] fileName = { "DSfind.json" };
        // "DSAggregatequeryMKV.json", "DSAggregatequeryRegex_GtInOR.json",
        // "DSAggregatequeryRegex.json", "DSAggregatequeryRegexInOR.json",
        // "DSAggregatequeryRegexMKVInOR.json" ,"DSfind.json" };
        // String[] fileName = { "group.json" };
        long total = 0;
        for (int i = 0; i < fileName.length; i++) {
            // System.out.println("test query : " + fileName[i]);
            String query = DataStoreUnitTest.getData(queryJsonDir + fileName[i]);

            DataStoreMsg request = new DataStoreMsg();

            request.put(DataStoreProtocol.MONGO_QUERY_SQL, query.toString());
            request.put(DataStoreProtocol.MONGO_COLLECTION_NAME, HealthManagerConstants.MONGO_COLLECTION_PROFILE);

            DataStoreConnection obj = new DataStoreConnection(userName, password, dbName, serverlist,
                    DataStoreType.MONGODB);

            AbstractDataStore store = DataStoreFactory.getInstance().build(HealthManagerConstants.DataStore_Profile,
                    obj, new ProfileDataAdpater(), "");

            store.start();

            long start = System.currentTimeMillis();
            @SuppressWarnings("unchecked")
            List<Object> list = store.doQuery(request);

            long stop = System.currentTimeMillis();
            total = total + (stop - start);
            StringBuffer sb = new StringBuffer();
            sb.append("query " + fileName[i] + ": " + (stop - start));
            try {
                fileWrite(sb);
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            store.stop();

            DataStoreUnitTest.printTestResult("MongoDB:" + fileName[i], list, queryJsonDir);
        }
        StringBuffer sb = new StringBuffer();
        sb.append("query total: " + total);
        try {
            fileWrite(sb);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @SuppressWarnings({ "rawtypes", "unused" })
    public static void testupdateMongoDB() {

        for (int i = 0; i < updatefileName.length; i++) {
            System.out.println("test update : " + updatefileName[i]);
            String update = DataStoreUnitTest.getData(queryJsonDir + updatefileName[i]);

            DataStoreMsg request = new DataStoreMsg();

            request.put(DataStoreProtocol.MONGO_QUERY_SQL, update.toString());
            request.put(DataStoreProtocol.MONGO_COLLECTION_NAME, HealthManagerConstants.MONGO_COLLECTION_PROFILE);

            DataStoreConnection obj = new DataStoreConnection(userName, password, dbName, serverlist,
                    DataStoreType.MONGODB);

            AbstractDataStore store = DataStoreFactory.getInstance().build(HealthManagerConstants.DataStore_Profile,
                    obj, new ProfileDataAdpater(), "");

            store.start();
            long start = System.currentTimeMillis();
            boolean rst = store.doUpdate(request);
            long stop = System.currentTimeMillis();
            StringBuffer sb = new StringBuffer();
            sb.append("update " + updatefileName[i] + ": " + (stop - start));
            try {
                fileWrite(sb);
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            store.stop();

            // DataStoreUnitTest.printTestResult("MongoDB:"+updatefileName[i], rst);
        }

    }

}
