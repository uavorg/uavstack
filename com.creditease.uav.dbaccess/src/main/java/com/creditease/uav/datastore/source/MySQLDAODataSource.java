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

import java.util.List;

import com.creditease.uav.datastore.api.DataStoreConnection;
import com.creditease.uav.datastore.sql.DAOFactory;

public class MySQLDAODataSource extends AbstractDataSource<DAOFactory> {

    public MySQLDAODataSource(DataStoreConnection connection) {
        super(connection);
    }

    @Override
    public DAOFactory initSourceConnect() throws Exception {

        List<String> addrList = connection.getAddressList();

        String url = "jdbc:mysql://" + addrList.get(0) + "/" + connection.getDbName();

        int initPoolSize = 200;
        int minPoolSize = 10;
        int maxPoolSize = 30;
        int maxIdleTime = 300;
        int idleConnTestPeriod = 30;
        String testQuerySQL = "select 1 from DUAL";
        DAOFactory dao = DAOFactory.buildDAOFactory(connection.getDbName(), "com.mysql.jdbc.Driver", url,
                connection.getUsername(), connection.getPassword(), initPoolSize, minPoolSize, maxPoolSize, maxIdleTime,
                idleConnTestPeriod, testQuerySQL);

        if (log.isTraceEnable()) {
            log.info(this, "INIT MySQLDAODataSource SUCCESS: " + url);
        }

        return dao;
    }

    @Override
    public void stop() {

        this.sourceConnect.shutdown();
        DAOFactory.removeDAOFactory(DAOFactory.DEFAULTNAME);
        super.stop();
    }
}
