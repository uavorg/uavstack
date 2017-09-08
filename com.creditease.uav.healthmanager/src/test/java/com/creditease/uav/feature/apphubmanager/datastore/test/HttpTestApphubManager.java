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

package com.creditease.uav.feature.apphubmanager.datastore.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.uav.datastore.api.DataStoreProtocol;
import com.creditease.uav.httpasync.HttpAsyncClient;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;

/**
 * @author Created by lbay on 2016/4/25.
 */
public class HttpTestApphubManager {

    private static final Logger LOG = Logger.getLogger(HttpTestApphubManager.class.getName());
    private static final String appRestServerUrl = "http://localhost:8011/ah/app";
    private static final String groupRestServerUrl = "http://localhost:8011/ah/group";

    private static String k_dataStoreName = "dataStoreName";
    private static String k_conllectionName = "conllectionName";
    private static String DataStore_App_Manage = "appManageDataStore";
    private static String DataStore_Group_Manage = "groupManageDataStore";
    private static String MONGO_COLLECTION_APPINFO = "uav_appinfo";
    private static String MONGO_COLLECTION_GROUPINFP = "uav_groupinfo";

    private enum bussinessType {
        APP, GROUP
    }

    // test
    private static String fileUrl = "D:\\workspace\\ce-datamonitorsystem\\com.creditease.uav.healthmanager\\src\\test\\java\\testData\\AppHubManager\\";

    public static void main(String[] args) {

        testManage();
    }

    public static void testManage() {

        System.out.println("################### HttpTestApphub testManage begin  ####### \n");
        HttpAsyncClient.build(1000, 1000, 100000, 100000, 100000);

        // createApp();
        // modifyApp();
        // queryApp();

        // createGroup();
        // modifyGroup();
        queryGroup();
    }

    public static void createApp() {

        String paramUrl = fileUrl + "createApp.json";
        byte[] datas = getRequestData(bussinessType.APP, paramUrl);
        httpClientPost(appRestServerUrl, datas);
    }

    public static void modifyApp() {

        String paramUrl = fileUrl + "modifyApp.json";
        byte[] datas = getRequestData(bussinessType.APP, paramUrl);
        httpClientPost(appRestServerUrl, datas);
    }

    public static void queryApp() {

        String paramUrl = fileUrl + "queryApp.json";
        byte[] datas = getRequestData(bussinessType.APP, paramUrl);
        httpClientPost(appRestServerUrl, datas);
    }

    public static void createGroup() {

        String paramUrl = fileUrl + "createGroup.json";
        byte[] datas = getRequestData(bussinessType.GROUP, paramUrl);
        httpClientPost(groupRestServerUrl, datas);
    }

    public static void modifyGroup() {

        String paramUrl = fileUrl + "modifyGroup.json";
        byte[] datas = getRequestData(bussinessType.GROUP, paramUrl);
        httpClientPost(groupRestServerUrl, datas);
    }

    public static void queryGroup() {

        String paramUrl = fileUrl + "queryGroup.json";
        byte[] datas = getRequestData(bussinessType.GROUP, paramUrl);
        httpClientPost(groupRestServerUrl, datas);
    }

    // tools---------------------------------------------------》

    /**
     * 打包请求数据
     * 
     * @param type
     * @param paramPath
     * @return
     */
    private static byte[] getRequestData(bussinessType type, String paramPath) {

        Map<String, String> dbInfo = getDBInfo(type);
        UAVHttpMessage request = new UAVHttpMessage();
        String dataStr = getFileData(paramPath);
        request.putRequest(DataStoreProtocol.MONGO_REQUEST_DATA, dataStr);
        request.putRequest(DataStoreProtocol.DATASTORE_NAME, dbInfo.get(k_dataStoreName));
        request.putRequest(DataStoreProtocol.MONGO_COLLECTION_NAME, dbInfo.get(k_conllectionName));
        String jsonStr = JSONHelper.toString(request);
        byte[] datab = null;
        try {
            datab = jsonStr.getBytes("utf-8");
        }
        catch (UnsupportedEncodingException e) {
            LOG.info("HttpTestApphub getRequestData \n" + e.getMessage());
        }

        return datab;
    }

    /**
     * 打包数据库操作信息
     * 
     * @param type
     * @return
     */
    private static Map<String, String> getDBInfo(bussinessType type) {

        HashMap<String, String> result = new HashMap<String, String>();
        if (bussinessType.APP.equals(type)) {
            result.put(k_dataStoreName, DataStore_App_Manage);
            result.put(k_conllectionName, MONGO_COLLECTION_APPINFO);
        }
        else if (bussinessType.GROUP.equals(type)) {
            result.put(k_dataStoreName, DataStore_Group_Manage);
            result.put(k_conllectionName, MONGO_COLLECTION_GROUPINFP);
        }
        return result;
    }

    private static void httpClientPost(final String url, byte[] datab) {

        HttpAsyncClient.instance().doAsyncHttpPost(url, datab, "application/json", "utf-8", new HttpClientCallback() {

            @Override
            public void completed(HttpClientCallbackResult result) {

                LOG.info(url + " getReplyDataAsString. \n" + result.getReplyDataAsString());

                // LOG.info(url+" getReplyData. \n" + result.getReplyData());
            }

            @Override
            public void failed(HttpClientCallbackResult result) {

                LOG.info(url + " failed. \n");
            }

        });
    }

    private static String getFileData(String fireDir) {

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
