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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;

import com.creditease.agent.helpers.DataStoreHelper;
import com.creditease.agent.log.SystemLogger;

/**
 * @author:ph 测试驱动入口: testCase
 * 
 */

public class DataStoreUnitTest {

    enum dataType4Test {
        profile, notfication, monitor, log
    }

    public static void main(String args[]) {

        System.out.println("# test start ...");

        SystemLogger.init("INFO", true, 1000);

        // profileDataTestCase();
        // notifiationDataTestCase();
        // monitorDataTestCase();
        logDataTestCase();

        System.out.println("# test end ... ");
    }

    public static void profileDataTestCase() {

        /*****
         * 
         * Profile Data
         ****/
        // TestProfileData.testInsertMongoDB();
        DoTestProfileData.testquerytMongoDB();
        // TestProfileData.testupdateMongoDB();
        // HttpTestQuery.executeQuery(dataType4Test.profile);
    }

    public static void notifiationDataTestCase() {

        /*****
         * 
         * Notification Data
         ****/
        DoTestNotifyData4Mongo.testInsertNotifyMongoDB();
        DoTestNotifyData4Mongo.testquerytNotifyMongoDB();
        HttpTestQuery.executeQuery(dataType4Test.notfication);
    }

    public static void monitorDataTestCase() {
        /*****
         * 
         * Monitor Data
         ****/

    }

    public static void otherDataTestCase() {

        /*****
         * 
         * other Data
         ****/
        // TestInsertMysqlDAOFactory();
        // TestInsertOpenfalcon();
        // TestInsertElasticSearch();
    }

    public static void logDataTestCase() {

        DoTestLogData.testInsertHBase();
        // TestLogData.testQueryHBase();
        // HttpTestQuery.executeQuery(dataType4Test.log);
    }

    /***
     * 
     * 
     * testCase[0]: the Test case title testCase[1]: the Test case result testCase[2]: the Test case query/update/delete
     * contion
     * 
     **/
    @SuppressWarnings({ "rawtypes", "unchecked", "unused" })
    public static void printTestResult(Object... testCase) {

        System.out.println("################### TEST CASE:: " + testCase[0] + "  ######################## \n");

        /**
         * 
         * print the query/update/delete conditon
         **/
        if (testCase.length > 2 && testCase[2] != null) {
            System.out.println("the Test condtion is ::" + testCase[2].toString() + "\n");
        }

        /**
         * 
         * print the test result
         **/
        if (testCase.length > 1 && testCase[1] != null) {
            if (testCase[0].toString().contains("MongoDB")) {
                if (testCase[1] instanceof Boolean) {
                    boolean result = (Boolean) testCase[1];
                    if (result) {
                        System.out.println("Final MongoDataStore insert/update success! \n");
                    }
                    else {
                        System.out.println("Final MongoDataStore insert/update failed! \n");
                    }

                }
                else {
                    List list = (List) testCase[1];
                    if (list != null) {
                        for (Object item : list) {
                            String decodeValue = DataStoreHelper.decodeForMongoDB(item.toString());

                            System.out.println("Final MongoDataStore result:" + decodeValue.toString() + "\n");
                        }
                    }
                }

            }
            else if (testCase[0].toString().contains("Influxdb")) {
                if (testCase[1] instanceof Boolean) {
                    boolean result = (Boolean) testCase[1];
                    if (result) {
                        System.out.println("Final Influxdb insert/update success! \n");
                    }
                    else {
                        System.out.println("Final Influxdb insert/update failed!  \n");
                    }

                }
                else {
                    List<List<Map>> list = (List<List<Map>>) testCase[1];
                    for (List<Map> ls : list) {
                        for (Map item : ls) {
                            // System.out.println("ifxdb.ser.name: "
                            // + item.get(DataStoreProtocol.INFLUXDB_QUERY_RS_KEY_SERIES_NAME));
                            // System.out.println("ifxdb.ser.cols: "
                            // + item.get(DataStoreProtocol.INFLUXDB_QUERY_RS_KEY_SERIES_COLS));
                            // System.out.println("ifxdb.ser.tags: "
                            // + item.get(DataStoreProtocol.INFLUXDB_QUERY_RS_KEY_SERIES_TAGS));
                            // System.out.println("ifxdb.ser.vals: "
                            // + item.get(DataStoreProtocol.INFLUXDB_QUERY_RS_KEY_SERIES_VALS));
                        }
                    }
                }
            }
            else if (testCase[0].toString().contains("HBase")) {
                if (testCase[1] instanceof Boolean) {
                    boolean result = (Boolean) testCase[1];
                    if (result) {
                        System.out.println("Final HBase insert/update success! \n");
                    }
                    else {
                        System.out.println("Final HBase insert/update failed!  \n");
                    }

                }
                else {
                    List<NavigableMap<byte[], byte[]>> list = (List<NavigableMap<byte[], byte[]>>) testCase[1];
                    System.out.println("query info size is " + list.size());
                    for (NavigableMap<byte[], byte[]> ls : list) {
                        System.out.println("return info :");
                        for (Entry<byte[], byte[]> e : ls.entrySet()) {
                            System.out.println(new String(e.getKey()) + ":" + new String(e.getValue()));
                        }
                    }
                }
            }
        }

    }

    public static String getData(String fireDir) {

        StringBuffer buffer = new StringBuffer();

        try {

            File file = new File(fireDir);
            BufferedReader reader = null;
            reader = new BufferedReader(new FileReader(file));

            String tempString = null;
            while ((tempString = reader.readLine()) != null) {

                buffer.append(tempString + "\n");
            }
            reader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        String rawData = buffer.toString();

        return rawData;
    }

}
