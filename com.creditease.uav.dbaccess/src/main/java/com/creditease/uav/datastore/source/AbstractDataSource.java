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

package com.creditease.uav.datastore.source;

import java.util.concurrent.atomic.AtomicBoolean;

import com.creditease.agent.helpers.ConnectionFailoverMgrHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.uav.datastore.api.DataStoreConnection;
import com.creditease.uav.helpers.connfailover.ConnectionFailoverMgr;

/**
 * @author peihua
 */
public abstract class AbstractDataSource<T> {

    protected static final ISystemLogger log = SystemLogger.getLogger(AbstractDataSource.class);

    private AtomicBoolean isStart = new AtomicBoolean(false);

    protected DataStoreConnection connection;

    protected T sourceConnect = null;

    protected ConnectionFailoverMgr connMgr;

    public AbstractDataSource(DataStoreConnection connection) {

        this.connection = connection;

        connMgr = ConnectionFailoverMgrHelper.getConnectionFailoverMgr(connection.getAddressList(), 30000);
    }

    /**
     * start DataSource connection
     */
    public void start() {

        try {
            this.sourceConnect = initSourceConnect();
        }
        catch (Exception e) {
            log.err(this, "DataSource[" + this.getClass().getName() + "] Init Fail.", e);
            return;
        }

        if (this.sourceConnect != null) {
            isStart.compareAndSet(false, true);
        }

        if (log.isTraceEnable()) {
            log.info(this, "DataSource[" + this.getClass().getName() + "] Started");
        }
    }

    /**
     * stop DataSource connection
     */
    public void stop() {

        isStart.compareAndSet(true, false);

        if (log.isTraceEnable()) {
            log.info(this, "DataSource[" + this.getClass().getName() + "] Stopped");
        }
    }

    public boolean isStarted() {

        return isStart.get();
    }

    public DataStoreConnection getDataStoreConnection() {

        return connection;
    }

    public T getSourceConnect() {

        return this.sourceConnect;
    }

    /**
     * 获取当前可用连接,判断黑名单中地址是否已经过期--hongqiang
     */

    public String getAvalibleAddressEntry() {

        return this.connMgr.getConnection();
    }

    /**
     * 把不能使用的地址放置在黑名单中---hongqiang
     */
    public void putAddressToUnavalibleMap(String addressEntry) {

        this.connMgr.putFailConnection(addressEntry);
    }

    /**
     * initialization the DB connection
     */
    protected abstract T initSourceConnect() throws Exception;
}
