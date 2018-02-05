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

import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.DataStoreHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.uav.datastore.api.DataStoreAdapter;
import com.creditease.uav.datastore.api.DataStoreConnection;
import com.creditease.uav.datastore.api.DataStoreMsg;
import com.creditease.uav.datastore.api.DataStoreProtocol;
import com.creditease.uav.datastore.source.AbstractDataSource;
import com.creditease.uav.datastore.source.OpentsdbDataSource;
import com.creditease.uav.httpasync.HttpAsyncClient;
import com.creditease.uav.httpasync.HttpAsyncException;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;

/**
 * Monitor Data Store insert & Query
 * 
 * @author hongqiang
 */
public class OpentsdbDataStore extends AbstractDataStore<HttpAsyncClient> {

    // insertCallBack inner class to obtain insert thread safe
    private static class InsertCallBack implements HttpClientCallback {

        private byte[] batchData = new byte[0];
        private OpentsdbDataStore ds;
        private String addressEntry;

        public InsertCallBack(OpentsdbDataStore ds, String addressEntry, byte[] batchData) {

            this.batchData = batchData;
            this.ds = ds;
            this.addressEntry = addressEntry;
        }

        @Override
        public void completed(HttpClientCallbackResult result) {

            String retmsg = result.getReplyDataAsString();
            int retcode = result.getRetCode();

            if (log.isTraceEnable()) {
                log.info(this, "DataStore INSERT SUCCESS: datalen=" + this.batchData.length + ",rc=" + retcode
                        + ",rmsg=" + retmsg);
            }
        }

        @Override
        public void failed(HttpClientCallbackResult result) {

            int retcode = result.getRetCode();

            if (retcode == 204) {

                if (log.isTraceEnable()) {
                    log.info(this, "DataStore INSERT SUCCESS: datalen=" + this.batchData.length + ", rc=" + retcode);
                }

                return;
            }

            HttpAsyncException exception = result.getException();
            String retmsg = result.getReplyDataAsString();

            log.err(this, "DataStore INSERT FAIL with Exception: rmsg=" + retmsg + ",", exception);

            if (exception != null && (exception.getMessage().indexOf("java.net.ConnectException") > -1
                    || exception.getCause() instanceof SocketTimeoutException)) {

                /**
                 * NOTE: 只有网络错误时才插入黑名单
                 */
                ds.datasource.putAddressToUnavalibleMap(this.addressEntry);

                ds.tryToInsert(this.batchData, null);

                return;
            }
        }

    }

    // queryCallBack innner class to obtain query thread safe
    private static class QueryCallBack implements HttpClientCallback {

        private CountDownLatch cdl;
        @SuppressWarnings("rawtypes")
        private List list = Collections.emptyList();
        @SuppressWarnings("unused")
        private String queryJson;
        @SuppressWarnings("unused")
        private OpentsdbDataStore dataStore;
        private DataStoreMsg msg;
        private int queryState = ACTIONFAIL;

        public QueryCallBack(DataStoreMsg msg, OpentsdbDataStore dataStore, CountDownLatch cdl, String queryJson) {

            this.cdl = cdl;
            this.queryJson = queryJson;
            this.dataStore = dataStore;
            this.msg = msg;
        }

        @Override
        public void failed(HttpClientCallbackResult result) {

            log.err(this, "OpenTSDB query FAILED: ", result.getException());

            if (result.getException() != null
                    && result.getException().getMessage().indexOf("java.net.ConnectException") > -1) {
                queryState = NETWORKFAIL;
            }
            else {
                queryState = ACTIONFAIL;
            }

            String retmsg = result.getReplyDataAsString();
            int retcode = result.getRetCode();

            msg.put(DataStoreProtocol.OPENTSDB_QUERY_RETCODE, retcode);
            msg.put(DataStoreProtocol.OPENTSDB_QUERY_RETMSG, retmsg);

            cdl.countDown();
        }

        @Override
        public void completed(HttpClientCallbackResult result) {

            queryState = ACTIONOK;

            String reply = result.getReplyDataAsString();
            int retcode = result.getRetCode();

            msg.put(DataStoreProtocol.OPENTSDB_QUERY_RETCODE, retcode);

            if (reply != null) {

                reply = DataStoreHelper.decodeForOpenTSDB(reply);

                try {
                    list = JSONHelper.toObject(reply, List.class, true);
                }
                catch (Exception e) {
                    log.err(this, "OPENTSDB QUERY DATA CONVERT INTO LIST FAIL:data=" + reply, e);
                }
            }

            cdl.countDown();
        }

        @SuppressWarnings("rawtypes")
        public List getResultList() {

            return list;
        }

        public int getState() {

            return queryState;
        }
    }

    private static final String HTTP_HEAD = "http://";
    private static final String HTTP_QUERY = "/api/query";
    private static final String HTTP_PUT_PREFIX = "/api/put";

    private static final int ACTIONOK = 2;
    private static final int ACTIONFAIL = 0;
    private static final int NETWORKFAIL = 1;

    public OpentsdbDataStore(DataStoreConnection connectObj, DataStoreAdapter adaptor, String feature) {

        super(connectObj, adaptor, feature);
    }

    @Override
    protected AbstractDataSource<HttpAsyncClient> getDataSource(DataStoreConnection obj) {

        OpentsdbDataSource opentsdbDataSource = new OpentsdbDataSource(obj);
        return opentsdbDataSource;
    }

    /**
     * http insert method ,every time insert batchSize ,if remain less than batchSize then insert remain
     * 
     * @param msg
     *            :source data
     */
    @Override
    protected boolean insert(DataStoreMsg msg) {

        boolean isOK = false;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> points = (List<Map<String, Object>>) adaptor.prepareInsertObj(msg,
                this.datasource.getDataStoreConnection());

        String addressEntry = this.datasource.getAvalibleAddressEntry();

        /**
         * FAST FAILURE: when the addressEntry is null, return false
         */
        if (addressEntry == null) {
            log.warn(this, "DataStore INSERT FAIL as No Address Entry.");
            return false;
        }

        /**
         * 
         * NOTE: tags & metric ":" was not permitted in opentsdb
         */
        int pointsSize = points.size();

        if (pointsSize == 0) {
            log.warn(this, "DataStore INSERT FAIL as No Point Data.");
            return false;
        }

        int batchCount = 0;

        int batchSize = DataConvertHelper.toInt(msg.get(DataStoreProtocol.OPENTSDB_INSERT_BATCHSIZE), 40);

        List<Map<String, Object>> batchData = new ArrayList<Map<String, Object>>();

        for (int i = 0; i < pointsSize; i++) {

            batchCount++;

            batchData.add(points.get(i));

            if (i == pointsSize - 1 || batchCount == batchSize) {

                String batchDataStr = JSONHelper.toString(batchData);

                byte[] data = prepareDataBytes(batchDataStr);

                if (data == null) {
                    break;
                }

                /**
                 * NOTE: will not block the insert data, to put the data as many as possible
                 */
                isOK = tryToInsert(data, addressEntry);

                if (isOK == false) {
                    break;
                }

                batchCount = 0;

                batchData.clear();

                batchData = new ArrayList<Map<String, Object>>();
            }
        }

        return adaptor.handleInsertResult(isOK, msg, this.datasource.getDataStoreConnection());
    }

    /**
     * tryToInsert
     * 
     * @param insertdata
     *            : prepared insertdata
     */
    private boolean tryToInsert(byte[] datab, String addressEntryWanted) {

        String addressEntry = addressEntryWanted;

        if (addressEntryWanted == null) {
            addressEntry = this.datasource.getAvalibleAddressEntry();
        }

        /**
         * when the addressEntry is null, return false
         */
        if (addressEntry == null) {
            log.warn(this, "DataStore INSERT FAIL as No Address Entry.");
            return false;
        }

        String insertURL = getHttpRequestURL(addressEntry) + HTTP_PUT_PREFIX;

        InsertCallBack insertCallBack = new InsertCallBack(this, addressEntry, datab);

        this.datasource.getSourceConnect().doAsyncHttpPost(insertURL, datab, "application/json", "utf-8",
                insertCallBack);

        return true;
    }

    private byte[] prepareDataBytes(String insertData) {

        byte[] datab = null;
        try {
            datab = insertData.getBytes("utf-8");
        }
        catch (UnsupportedEncodingException e) {
            log.err(this, "INSERT CONVERT INTO BYTES FAIL:data=" + insertData, e);
        }
        return datab;
    }

    /**
     * http query method,provide for query service for http request
     */
    @SuppressWarnings("rawtypes")
    @Override
    protected List query(DataStoreMsg msg) {

        String queryJson = (String) adaptor.prepareQueryObj(msg, this.datasource.getDataStoreConnection());

        byte[] datab = null;

        try {
            datab = queryJson.getBytes("utf-8");
        }
        catch (UnsupportedEncodingException e) {
            log.err(this, "DataStore QUERY CONVERT INTO BYTES FAIL:data=" + queryJson, e);
            return null;
        }

        List list = query(msg, datab, queryJson);

        return this.adaptor.handleQueryResult(list, msg, this.datasource.getDataStoreConnection());
    }

    /**
     * real
     * 
     * @param queryJson
     * @param addressEntry
     * @return
     */
    @SuppressWarnings("rawtypes")
    protected List query(DataStoreMsg msg, byte[] datab, String queryJson) {

        List list = Collections.emptyList();

        String addressEntry = this.datasource.getAvalibleAddressEntry();

        if (addressEntry == null) {
            log.err(this, "DataStore QUERY FAIL as No Address Entry: data=" + queryJson);
            return list;
        }

        String queryURL = getHttpRequestURL(addressEntry) + HTTP_QUERY;

        CountDownLatch cdl = new CountDownLatch(1);
        QueryCallBack queryCallBack = new QueryCallBack(msg, this, cdl, queryJson);
        this.datasource.getSourceConnect().doAsyncHttpPost(queryURL, datab, "application/json", "utf-8", queryCallBack);

        try {
            cdl.await(10, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            log.err(this, "DataStore QUERY InterruptedException.", e);
            return list;
        }

        list = queryCallBack.getResultList();

        int queryState = queryCallBack.getState();

        switch (queryState) {
            case ACTIONOK:
                if (log.isTraceEnable()) {
                    log.info(this, "DataStore QUERY SUCCESS: state=" + queryState + ",count=" + list.size());
                }
                break;
            case NETWORKFAIL:
                /**
                 * NOTE: 只有网络错误时才插入黑名单
                 */
                this.datasource.putAddressToUnavalibleMap(addressEntry);
                /**
                 * retry
                 */
                return query(msg, datab, queryJson);
            case ACTIONFAIL:
                log.err(this, "DataStore QUERY FAIL: state=" + queryState);
                break;
        }

        return list;
    }

    @Override
    protected boolean update(DataStoreMsg msg) {

        return false;
    }

    private String getHttpRequestURL(String addressEntry) {

        String httpURL = null;
        if (addressEntry != null) {
            httpURL = HTTP_HEAD + addressEntry;
        }
        return httpURL;
    }

}
