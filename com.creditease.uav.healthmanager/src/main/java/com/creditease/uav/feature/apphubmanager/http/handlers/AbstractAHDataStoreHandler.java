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

package com.creditease.uav.feature.apphubmanager.http.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.agent.spi.AbstractHttpHandler;
import com.creditease.uav.datastore.api.DataStoreFactory;
import com.creditease.uav.datastore.api.DataStoreMsg;
import com.creditease.uav.datastore.api.DataStoreProtocol;
import com.creditease.uav.datastore.core.AbstractDataStore;

public abstract class AbstractAHDataStoreHandler extends AbstractHttpHandler<UAVHttpMessage> {

    protected enum requestType {
        create, modify, query
    }

    public AbstractAHDataStoreHandler(String cName, String feature) {
        super(cName, feature);
    }

    public abstract String getCollectionName();

    @Override
    @SuppressWarnings({ "rawtypes" })
    public void handle(UAVHttpMessage httpMsg) {

        String storeName = httpMsg.getRequest(DataStoreProtocol.DATASTORE_NAME);
        AbstractDataStore store = DataStoreFactory.getInstance().get(storeName);
        Object resp = null;
        if (null != store) {

            if (!store.isStarted()) {

                store.start();

                if (!store.isStarted()) {

                    resp = "DataStore[" + storeName + "] CAN NOT START.";

                    if (log.isTraceEnable()) {
                        log.warn(this, (String) resp);
                    }
                }
            }
            else {
                resp = run(httpMsg, store, getCollectionName(), log);
            }
        }
        else {
            resp = "DataStore[" + storeName + "] NO EXIST.";
        }

        httpMsg.putResponse(UAVHttpMessage.RESULT, createRespJson(resp));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Object run(UAVHttpMessage httpMsg, AbstractDataStore store, String collectionName, ISystemLogger log) {

        Object result = null;
        String requestData = httpMsg.getRequest(DataStoreProtocol.MONGO_REQUEST_DATA);
        Map<String, Object> requestJson = JSONHelper.toObject(requestData, Map.class);

        String type = String.valueOf(requestJson.get("type"));
        String data = String.valueOf(requestJson.get("data"));
        DataStoreMsg msg = new DataStoreMsg();
        msg.put(DataStoreProtocol.MONGO_COLLECTION_NAME, collectionName);
        msg.put(DataStoreProtocol.MONGO_REQUEST_DATA, data);

        try {
            if (requestType.create.toString().equals(type)) {
                result = store.doInsert(msg);
            }
            else if (requestType.modify.toString().equals(type)) {
                result = store.doUpdate(msg);
            }
            else if (requestType.query.toString().equals(type)) {
                result = store.doQuery(msg);
            }
        }
        catch (Exception e) {
            log.err(AbstractAHDataStoreHandler.class, " AppHubMgt DataStore do [" + type + "] FAIL: data=" + data, e);
        }

        return result;
    }

    protected String createRespJson(Object result) {

        String code = "0"; // 0:失败 1：成功
        String msg = "";

        Map<String, Object> respJson = new HashMap<String, Object>();
        if (result instanceof Boolean && ((Boolean) result)) {
            code = "1";
        }
        else if (result instanceof List) {
            code = "1";
            respJson.put("data", result);
        }

        respJson.put("code", code);
        respJson.put("msg", msg);
        return JSONHelper.toString(respJson);
    }

}
