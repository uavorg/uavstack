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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.uav.datastore.api.DataStoreProtocol;
import com.creditease.uav.feature.healthmanager.datastore.test.DataStoreUnitTest.dataType4Test;
import com.creditease.uav.httpasync.HttpAsyncClient;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;

public class HttpTestQuery {

    private static final Logger LOG = Logger.getLogger(HttpTestQuery.class.getName());

    private static final String jsonQuery = "src/test/java/testData/MongoDBQuery/DSAggregatequery.json";

    private static final String jsonQuery4notify = "src/test/java/testData/MongoDBQuery/DSAggregate4Notification.json";

    @SuppressWarnings("unused")
    private static String querysql = "select *  from jvm  where time > now()-1d limit 500";

    private static String queryJsonDir = "src/test/java/testData/MongoDBQuery/";

    private static String hbquery = "{\"starttime\": 1441812645015, \"endtime\" : 1462413645015,  \"appid\": \"com.creditease.uav.monitorframework.buildFat\"}";

    @SuppressWarnings("unused")
    private static String prepareMongoProfileDBdata(UAVHttpMessage request) {

        String query = DataStoreUnitTest.getData(jsonQuery);

        request.putRequest(DataStoreProtocol.MONGO_QUERY_SQL, query.toString());

        request.putRequest(DataStoreProtocol.DATASTORE_NAME, MonitorDataFrame.MessageType.Profile.toString());
        request.putRequest(DataStoreProtocol.MONGO_COLLECTION_NAME, HealthManagerConstants.MONGO_COLLECTION_PROFILE);
        return JSONHelper.toString(request);
    }

    private static String prepareMongoProfileDBdata(UAVHttpMessage request, String query) {

        request.putRequest(DataStoreProtocol.MONGO_QUERY_SQL, query.toString());

        request.putRequest(DataStoreProtocol.DATASTORE_NAME, MonitorDataFrame.MessageType.Profile.toString());
        request.putRequest(DataStoreProtocol.MONGO_COLLECTION_NAME, HealthManagerConstants.MONGO_COLLECTION_PROFILE);
        return JSONHelper.toString(request);
    }

    private static String prepareNotifcationData(UAVHttpMessage request) {

        String query = DataStoreUnitTest.getData(jsonQuery4notify);

        request.putRequest(DataStoreProtocol.MONGO_QUERY_SQL, query.toString());

        request.putRequest(DataStoreProtocol.DATASTORE_NAME, "MT_Notify");
        request.putRequest(DataStoreProtocol.MONGO_COLLECTION_NAME, HealthManagerConstants.MONGO_COLLECTION_NOTIFY);

        return JSONHelper.toString(request);
    }

    private static String prepareLogData(UAVHttpMessage request) {

        request.putRequest(DataStoreProtocol.HBASE_QUERY_JSON_KEY, hbquery);
        request.putRequest(DataStoreProtocol.DATASTORE_NAME, MonitorDataFrame.MessageType.Log.toString());
        request.putRequest(DataStoreProtocol.HBASE_TABLE_NAME, HealthManagerConstants.HBASE_TABLE_LOGDATA);

        return JSONHelper.toString(request);
    }

    public static void executeQuery(dataType4Test type) {

        System.out.println("################### TEST CASE::  " + jsonQuery + " ####### \n");
        HttpAsyncClient.build(1000, 1000, 100000, 100000, 100000);
        String url = "http://localhost:8765/hm/query";
        UAVHttpMessage request = new UAVHttpMessage();
        String content = null;
        switch (type) {
            case profile:
                // content = prepareMongoProfileDBdata(request);
                // executeHttpCommand(url, content);
                loopTest();
                break;

            case notfication:
                content = prepareNotifcationData(request);
                executeHttpCommand("NotifcationData", url, content);
                break;

            case monitor:
                // content = prepareInfluxDBdata(request);
                // executeHttpCommand("InfluxDBdata",url, content);
                break;
            case log:
                content = prepareLogData(request);
                executeHttpCommand("HBaseLogdata", url, content);
                break;
            default:
                System.out.println("not support data type");
                break;
        }

    }

    public static void loopTest() {

        try {
            // HttpAsyncClient.build(1000, 1000, 100000, 100000, 100000);
            String url = "http://localhost:8765/hm/query";
            UAVHttpMessage request = new UAVHttpMessage();
            String content = null;

            String[] fileName = { "DSAggregatequery.json", "DSAggregatequeryGtLt.json", "DSAggregatequeryMKV.json",
                    "DSAggregatequeryRegex_GtInOR.json", "DSAggregatequeryRegex.json", "DSAggregatequeryRegexInOR.json",
                    "DSAggregatequeryRegexMKVInOR.json", "DSfind.json" };
            // String[] fileName = { "DSAggregatequery.json"};

            for (int i = 0; i < fileName.length; i++) {
                // System.out.println("test query : " + fileName[i]);
                String query = DataStoreUnitTest.getData(queryJsonDir + fileName[i]);
                content = prepareMongoProfileDBdata(request, query);

                executeHttpCommand(fileName[i], url, content);
                Thread.sleep(100);

            }
        }
        catch (Exception e) {
            return;
        }
    }

    public static void executeHttpCommand(final String fileName, String url, String content) {

        byte[] datab = null;
        try {
            datab = content.getBytes("utf-8");
        }
        catch (UnsupportedEncodingException e) {
            return;
        }

        HttpAsyncClient.instance().doAsyncHttpPost(url, datab, "application/json", "utf-8", new HttpClientCallback() {

            @Override
            public void completed(HttpClientCallbackResult result) {

                String results = result.getReplyDataAsString();
                LOG.info("getReplyDataAsString. \n" + results);

                LOG.info("getReplyData. \n" + result.getReplyData());

                try {
                    BufferedWriter writer = null;
                    try {
                        File file = new File("logs/" + fileName + ".query");
                        System.out.println("filename:::::::::::::::::::" + fileName);

                        writer = new BufferedWriter(new FileWriter(file));

                        writer.write(results);
                    }
                    catch (IOException e) {
                    }
                    finally {
                        writer.flush();
                        writer.close();
                    }
                }
                catch (IOException e) {
                }
            }

            @Override
            public void failed(HttpClientCallbackResult result) {

                LOG.info("failed. \n");
            }

        });
    }

}
