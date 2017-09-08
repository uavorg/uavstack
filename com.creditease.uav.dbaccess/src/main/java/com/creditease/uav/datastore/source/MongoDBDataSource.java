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

import java.util.ArrayList;
import java.util.List;

import com.creditease.agent.helpers.StringHelper;
import com.creditease.uav.datastore.api.DataStoreConnection;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

public class MongoDBDataSource extends AbstractDataSource<MongoDatabase> {

    private MongoClient mongo = null;

    public MongoDBDataSource(DataStoreConnection connection) {
        super(connection);
    }

    /**
     * 
     * mongo cluster model
     */
    @Override
    public MongoDatabase initSourceConnect() throws Exception {

        List<MongoCredential> auths = new ArrayList<MongoCredential>();

        if (!StringHelper.isEmpty(connection.getUsername()) && !StringHelper.isEmpty(connection.getPassword())) {

            MongoCredential credential = MongoCredential.createCredential(connection.getUsername(),
                    connection.getDbPower(), connection.getPassword().toCharArray());

            auths.add(credential);
        }
        List<ServerAddress> addres = new ArrayList<ServerAddress>();

        List<String> addrList = connection.getAddressList();

        for (String add : addrList) {
            String[] hosts = add.split(":");
            ServerAddress sa = new ServerAddress(hosts[0], Integer.parseInt(hosts[1]));
            addres.add(sa);
        }

        mongo = new MongoClient(addres, auths);

        MongoDatabase db = mongo.getDatabase(connection.getDbName());

        if (db != null) {
            if (log.isTraceEnable()) {
                log.info(this, "INIT MongoDBDataSource SUCCESS:db=" + connection.getDbName() + ",connection="
                        + mongo.getConnectPoint());
            }
        }
        else {
            throw new Exception(
                    "INIT MongoDBDataSource FAIL: No db object for database[" + connection.getDbName() + "]");
        }

        return db;
    }

    @Override
    public void stop() {

        mongo.close();
        super.stop();
    }

}
