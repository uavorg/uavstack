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

package com.creditease.uav.datastore.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.uav.datastore.core.AbstractDataStore;
import com.creditease.uav.datastore.core.HBaseDataStore;
import com.creditease.uav.datastore.core.MongoDBDataStore;
import com.creditease.uav.datastore.core.MySQLDataStore;
import com.creditease.uav.datastore.core.OpentsdbDataStore;

/**
 * 
 * @author peihua
 * 
 */

@SuppressWarnings("rawtypes")
public class DataStoreFactory {

    public enum DataStoreType {
        MONGODB, MYSQL, OPENTSDB, HBASE
    }

    // ----------------------------------------------------------
    private static DataStoreFactory instance;

    protected static final ISystemLogger log = SystemLogger.getLogger(DataStoreFactory.class);

    public static DataStoreFactory getInstance() {

        if (instance == null) {
            instance = new DataStoreFactory();

            return instance;
        }
        else
            return instance;
    }

    private Map<String, AbstractDataStore> dataStoreMap = new ConcurrentHashMap<String, AbstractDataStore>();

    private DataStoreFactory() {
    }

    /**
     * build data store
     * 
     * @param dataStoreName
     * @param connection
     * @param adapter
     * @return
     */
    public AbstractDataStore build(String dataStoreName, DataStoreConnection connection, DataStoreAdapter adapter,
            String feature) {

        if (StringHelper.isEmpty(dataStoreName) || null == connection || null == adapter) {
            throw new RuntimeException("Build DataStore Fail as no enough pre-staff.");
        }

        AbstractDataStore dataStoreInst = null;

        if (!dataStoreMap.containsKey(dataStoreName)) {
            switch (connection.getSourceType()) {
                case MONGODB:
                    dataStoreInst = new MongoDBDataStore(connection, adapter, feature);
                    break;

                case MYSQL:
                    dataStoreInst = new MySQLDataStore(connection, adapter, feature);
                    break;

                case OPENTSDB:
                    dataStoreInst = new OpentsdbDataStore(connection, adapter, feature);
                    break;

                case HBASE:
                    dataStoreInst = new HBaseDataStore(connection, adapter, feature);
                    break;
            }

            dataStoreMap.put(dataStoreName, dataStoreInst);
        }
        else {
            dataStoreInst = dataStoreMap.get(dataStoreName);
        }

        if (log.isTraceEnable()) {
            log.info(this, "DataStore[" + dataStoreName + "] Created.");
        }

        return dataStoreInst;
    };

    /**
     * get data store
     * 
     * @param dataStoreName
     * @return
     */
    public AbstractDataStore get(String dataStoreName) {

        if (StringHelper.isEmpty(dataStoreName)) {
            return null;
        }
        return dataStoreMap.get(dataStoreName);
    }

    /**
     * start all datastores
     */
    public void startAll(String feature) {

        if (null == feature) {
            return;
        }
        for (AbstractDataStore datastore : dataStoreMap.values()) {
            if (feature.equals(datastore.getFeature())) {
                datastore.start();
            }
        }
    }

    /**
     * stop all datastores
     */
    public void stopAll(String feature) {

        if (null == feature) {
            return;
        }
        for (AbstractDataStore datastore : dataStoreMap.values()) {
            if (feature.equals(datastore.getFeature())) {
                datastore.stop();
            }

        }
    }
}
