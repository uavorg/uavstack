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

package com.creditease.uav.feature.healthmanager.datastore.adaptors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.creditease.agent.helpers.DataStoreHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.uav.datastore.api.DataStoreAdapter;
import com.creditease.uav.datastore.api.DataStoreConnection;
import com.creditease.uav.datastore.api.DataStoreMsg;
import com.creditease.uav.datastore.api.DataStoreProtocol;

/**
 * @author peihua
 */
public abstract class AbstractMongoDataAdpater extends DataStoreAdapter {

    /**
     * 
     * */
    public List<Map<String, Object>> defaultInsert(String ntfListStr) {

        List<Map<String, Object>> documents = new ArrayList<Map<String, Object>>();

        List<String> ntfList = JSONHelper.toObjectArray(ntfListStr, String.class);

        for (String ntfStr : ntfList) {

            @SuppressWarnings("rawtypes")
            Map m = JSONHelper.toObject(ntfStr, Map.class);
            Map<String, Object> document = new LinkedHashMap<String, Object>();

            for (Object key : m.keySet()) {
                document.put("" + key, m.get(key));
            }
            document.put("createtime", System.currentTimeMillis());

            documents.add(document);
        }

        return documents;
    }

    @Override
    public Object prepareQueryObj(DataStoreMsg msg, DataStoreConnection connection) {

        String qSql = (String) msg.get(DataStoreProtocol.MONGO_QUERY_SQL);

        Object query = JSONHelper.toObject(DataStoreHelper.decorateInForMongoDB(qSql), Object.class);
        return query;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List handleQueryResult(Object result, DataStoreMsg msg, DataStoreConnection connection) {

        // just return
        List<Map> results = (List<Map>) result;

        return results;
    }

    @Override
    public Object prepareUpdateObj(DataStoreMsg msg, DataStoreConnection connection) {

        String qSql = (String) msg.get(DataStoreProtocol.MONGO_QUERY_SQL);
        return qSql;
    }

    @Override
    public boolean handleUpdateResult(Object result, DataStoreMsg msg, DataStoreConnection connection) {

        Boolean isOK = (Boolean) result;

        if (isOK) {
            if (log.isDebugEnable()) {
                log.debug(this, "UPDATE DATA SUCCESS:" + msg.toJSONString());
            }
        }
        else {
            if (log.isDebugEnable()) {
                log.debug(this, "UPDATE DATA FAIL:" + msg.toJSONString());
            }
        }

        return isOK;
    }
}
