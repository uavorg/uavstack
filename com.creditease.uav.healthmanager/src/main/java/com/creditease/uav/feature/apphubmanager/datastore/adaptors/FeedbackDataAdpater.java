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

package com.creditease.uav.feature.apphubmanager.datastore.adaptors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.uav.datastore.api.DataStoreAdapter;
import com.creditease.uav.datastore.api.DataStoreConnection;
import com.creditease.uav.datastore.api.DataStoreMsg;
import com.creditease.uav.datastore.api.DataStoreProtocol;

/**
 * @author Created by lbay on 2016/4/21.
 */
public class FeedbackDataAdpater extends DataStoreAdapter {

    @SuppressWarnings({ "unchecked" })
    @Override
    public Object prepareInsertObj(DataStoreMsg msg, DataStoreConnection connection) {

        String jsonStr = String.valueOf(msg.get(DataStoreProtocol.MONGO_REQUEST_DATA));
        Map<String, Object> dataStr = JSONHelper.toObject(jsonStr, Map.class);
        List<Map<String, Object>> documents = new ArrayList<Map<String, Object>>();
        documents.add(dataStr);
        return documents;
    }

    @Override
    public boolean handleInsertResult(Object result, DataStoreMsg msg, DataStoreConnection connection) {

        return (Boolean) result;
    }

    @Override
    public Object prepareQueryObj(DataStoreMsg msg, DataStoreConnection connection) {

        String qSql = (String) msg.get(DataStoreProtocol.MONGO_REQUEST_DATA);
        return JSONHelper.toObject(qSql, Object.class);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List handleQueryResult(Object result, DataStoreMsg msg, DataStoreConnection connection) {

        return (List<Map>) result;
    }

    @Override
    public Object prepareUpdateObj(DataStoreMsg msg, DataStoreConnection connection) {

        String jsonStr = String.valueOf(msg.get(DataStoreProtocol.MONGO_REQUEST_DATA));
        return jsonStr;
    }

    @Override
    public boolean handleUpdateResult(Object result, DataStoreMsg msg, DataStoreConnection connection) {

        return (Boolean) result;
    }
}
