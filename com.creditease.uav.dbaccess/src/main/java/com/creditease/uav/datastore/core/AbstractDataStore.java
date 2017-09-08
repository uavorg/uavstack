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

import java.util.List;

import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.uav.datastore.api.DataStoreAdapter;
import com.creditease.uav.datastore.api.DataStoreConnection;
import com.creditease.uav.datastore.api.DataStoreMsg;
import com.creditease.uav.datastore.source.AbstractDataSource;

/**
 * @author peihua
 * @param <D>
 * @param <T>
 */
public abstract class AbstractDataStore<D> {

    protected static final ISystemLogger log = SystemLogger.getLogger(AbstractDataStore.class);

    protected AbstractDataSource<D> datasource;

    protected String feature;

    protected DataStoreAdapter adaptor;

    public AbstractDataStore(DataStoreConnection connectObj, DataStoreAdapter adaptor, String feature) {
        this.feature = feature;
        this.adaptor = adaptor;
        this.datasource = getDataSource(connectObj);
        this.adaptor.setDataSource(this.datasource);
    }

    /**
     * status check
     **/
    public boolean isStarted() {

        if (datasource != null) {
            return datasource.isStarted();
        }
        else {
            if (log.isTraceEnable()) {
                log.warn(this, "DataStore[" + this.getClass().getName() + "] NOT Started.");
            }
            return false;
        }
    }

    /**
     * Start DBService
     **/
    public void start() {

        try {
            // start datasource
            if (!isStarted()) {
                datasource.start();
            }
        }
        catch (Exception e) {
            log.err(this, "DataStore[" + this.getClass().getName() + "] START Fail.", e);
        }
    }

    /**
     * stop DBService
     **/
    public void stop() {

        try {
            // stop datasource
            datasource.stop();
        }
        catch (Exception e) {
            log.err(this, "DataStore[" + this.getClass().getName() + "] STOP Fail.", e);
        }
    }

    /**
     * 对外提供的接口
     * 
     */
    public boolean doInsert(DataStoreMsg msg) {

        return insert(msg);
    }

    @SuppressWarnings("rawtypes")
    public List doQuery(DataStoreMsg msg) {

        return query(msg);
    }

    public boolean doUpdate(DataStoreMsg msg) {

        return update(msg);
    }

    public String getFeature() {

        return this.feature;
    }

    /**
     * 对内要求实现的接口
     */
    protected abstract AbstractDataSource<D> getDataSource(DataStoreConnection obj);

    protected abstract boolean insert(DataStoreMsg msg);

    @SuppressWarnings("rawtypes")
    protected abstract List query(DataStoreMsg msg);

    protected abstract boolean update(DataStoreMsg msg);

}
