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

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.shaded.com.google.protobuf.ServiceException;

import com.creditease.uav.datastore.api.DataStoreConnection;
import com.creditease.uav.datastore.api.DataStoreProtocol;

/**
 * 实现HBase的heavyweight联接 必须参数包括：zookeeper的联接地址以及cluster地址
 * 
 * @author 201211070016
 *
 */
public class HBaseDataSource extends AbstractDataSource<Connection> {

    private Connection conn = null;

    public HBaseDataSource(DataStoreConnection connection) {
        super(connection);
    }

    @Override
    protected Connection initSourceConnect() throws IOException, ServiceException {

        // 目前只有zklist转成serverlist和dbname
        Configuration config = HBaseConfiguration.create();
        String address = connection.toString(",");
        config.set(DataStoreProtocol.HBASE_ZK_QUORUM, address);
        config.set("hbase.client.scanner.caching",
                (String) connection.getContext(DataStoreProtocol.HBASE_QUERY_CACHING));
        config.set("hbase.client.scanner.max.result.size",
                (String) connection.getContext(DataStoreProtocol.HBASE_QUERY_MAXRESULTSIZE));
        config.set("zookeeper.recovery.retry", String.valueOf(connection.getRetryTimes()));

        // Failed to replace a bad datanode exception protection configuration
        config.set("dfs.client.block.write.replace-datanode-on-failure.policy", "NEVER");
        config.set("dfs.client.block.write.replace-datanode-on-failure.enable", "true");

        HBaseAdmin.checkHBaseAvailable(config);
        conn = ConnectionFactory.createConnection(config);
        // hbase.client.retries.number = 1 and zookeeper.recovery.retry = 1.
        return conn;
    }

    @Override
    public void stop() {

        try {
            conn.close();
        }
        catch (IOException e) {
        }
        super.stop();
    }

}
