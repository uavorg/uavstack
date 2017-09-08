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

package com.creditease.uav.feature.healthmanager.http.handlers;

import java.util.List;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.spi.AbstractHttpHandler;
import com.creditease.uav.datastore.api.DataStoreFactory;
import com.creditease.uav.datastore.api.DataStoreMsg;
import com.creditease.uav.datastore.api.DataStoreProtocol;
import com.creditease.uav.datastore.core.AbstractDataStore;

public class HMDataStoreQueryHandler extends AbstractHttpHandler<UAVHttpMessage> {

    public HMDataStoreQueryHandler(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public String getContextPath() {

        return "/hm/query";
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public void handle(UAVHttpMessage data) {

        String storeName = data.getRequest(DataStoreProtocol.DATASTORE_NAME);

        AbstractDataStore store = DataStoreFactory.getInstance().get(storeName);

        if (store != null) {

            if (!store.isStarted()) {

                store.start();

                if (!store.isStarted()) {

                    String warnMsg = "DataStore[" + storeName + "] CAN NOT START.";
                    if (log.isTraceEnable()) {
                        log.warn(this, warnMsg);
                    }
                    // put err to response
                    data.putResponse(UAVHttpMessage.ERR, warnMsg);

                    return;
                }
            }

            DataStoreMsg dsmsg = new DataStoreMsg();
            // put all request params into DataStoreMsg for adaptor usage
            dsmsg.putAll(data.getRequest());

            List result = store.doQuery(dsmsg);

            String jsonString = JSONHelper.toString(result);
            // put result to response
            data.putResponse(UAVHttpMessage.RESULT, jsonString);
        }
        else {
            String warnMsg = "DataStore[" + storeName + "] NO EXIST.";
            if (log.isTraceEnable()) {
                log.warn(this, warnMsg);
            }
            // put err to response
            data.putResponse(UAVHttpMessage.ERR, warnMsg);
        }
    }

}
