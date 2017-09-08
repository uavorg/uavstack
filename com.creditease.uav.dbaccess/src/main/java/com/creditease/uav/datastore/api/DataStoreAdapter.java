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

import java.util.List;

import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.uav.datastore.source.AbstractDataSource;

/**
 * @author peihua DataStoreAdapter: Mysql/Nosql data common parent Class
 */
public abstract class DataStoreAdapter {

    protected static final ISystemLogger log = SystemLogger.getLogger(DataStoreAdapter.class);

    @SuppressWarnings("rawtypes")
    protected AbstractDataSource dataSource;

    /**
     * 准备插入数据
     * 
     * @param msg
     * @param connection
     * @return
     */
    public abstract Object prepareInsertObj(DataStoreMsg msg, DataStoreConnection connection);

    /**
     * 处理插入结果
     * 
     * @param result
     * @param msg
     * @param connection
     */
    public abstract boolean handleInsertResult(Object result, DataStoreMsg msg, DataStoreConnection connection);

    /**
     * 准备查询数据
     * 
     * @param msg
     * @param connection
     * @return
     */
    public abstract Object prepareQueryObj(DataStoreMsg msg, DataStoreConnection connection);

    /**
     * 处理查询结果
     * 
     * @param result
     * @param msg
     * @param connection
     * @return 返回查询结果 as 列表
     */
    @SuppressWarnings("rawtypes")
    public abstract List handleQueryResult(Object result, DataStoreMsg msg, DataStoreConnection connection);

    /**
     * 准备更新数据
     * 
     * @param msg
     * @param connection
     * @return
     */
    public abstract Object prepareUpdateObj(DataStoreMsg msg, DataStoreConnection connection);

    /**
     * 处理更新结果
     * 
     * @param result
     * @param msg
     * @param connection
     */
    public abstract boolean handleUpdateResult(Object result, DataStoreMsg msg, DataStoreConnection connection);

    @SuppressWarnings("rawtypes")
    public void setDataSource(AbstractDataSource ds) {

        this.dataSource = ds;
    }
}
