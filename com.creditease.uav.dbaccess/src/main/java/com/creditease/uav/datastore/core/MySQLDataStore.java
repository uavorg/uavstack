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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import com.creditease.uav.datastore.api.DataStoreAdapter;
import com.creditease.uav.datastore.api.DataStoreConnection;
import com.creditease.uav.datastore.api.DataStoreMsg;
import com.creditease.uav.datastore.source.AbstractDataSource;
import com.creditease.uav.datastore.source.MySQLDAODataSource;
import com.creditease.uav.datastore.sql.DAOFactory;
import com.creditease.uav.datastore.sql.DAOFactory.QueryHelper;
import com.creditease.uav.datastore.sql.DAOFactory.UpdateHelper;

public class MySQLDataStore extends AbstractDataStore<DAOFactory> {

    public MySQLDataStore(DataStoreConnection connectObj, DataStoreAdapter adaptor, String feature) {
        super(connectObj, adaptor, feature);
    }

    @Override
    protected AbstractDataSource<DAOFactory> getDataSource(DataStoreConnection conn) {

        return new MySQLDAODataSource(conn);
    }

    @Override
    protected boolean insert(DataStoreMsg msg) {

        String insertSql = (String) this.adaptor.prepareInsertObj(msg, this.datasource.getDataStoreConnection());

        UpdateHelper helper = this.datasource.getSourceConnect().getUpdateHelper(insertSql);

        boolean isSuccess = false;

        try {
            // TODO: insertSql need change to addInsertParams???
            helper.update(insertSql);
            isSuccess = true;
        }
        catch (SQLException e) {
            log.err(this, "DataStore[" + this.getClass().getName() + "] INSERT Fail.", e);
        }
        finally {
            helper.free();
        }

        return this.adaptor.handleInsertResult(isSuccess, msg, this.datasource.getDataStoreConnection());
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected List query(DataStoreMsg msg) {

        String selSql = (String) this.adaptor.prepareQueryObj(msg, this.datasource.getDataStoreConnection());

        QueryHelper helper = this.datasource.getSourceConnect().getQueryHelper(selSql);

        try {
            ResultSet rs = helper.query(selSql);
            return this.adaptor.handleQueryResult(rs, msg, this.datasource.getDataStoreConnection());
        }
        catch (SQLException e) {
            log.err(this, "DataStore[" + this.getClass().getName() + "] QUERY Fail.", e);
        }
        finally {
            helper.free();
        }

        return Collections.emptyList();
    }

    @Override
    protected boolean update(DataStoreMsg msg) {

        String updateSql = (String) this.adaptor.prepareUpdateObj(msg, this.datasource.getDataStoreConnection());
        UpdateHelper helper = this.datasource.getSourceConnect().getUpdateHelper(updateSql);

        boolean isSuccess = false;
        try {
            helper.update(updateSql);
            isSuccess = true;
        }
        catch (SQLException e) {
            log.err(this, "DataStore[" + this.getClass().getName() + "] UPDATE Fail.", e);
        }
        finally {
            helper.free();
        }

        return this.adaptor.handleUpdateResult(isSuccess, msg, this.datasource.getDataStoreConnection());
    }
}
