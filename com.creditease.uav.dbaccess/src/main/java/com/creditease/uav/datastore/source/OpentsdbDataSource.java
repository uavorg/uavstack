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

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.uav.datastore.api.DataStoreConnection;
import com.creditease.uav.datastore.api.DataStoreProtocol;
import com.creditease.uav.httpasync.HttpAsyncClient;
import com.creditease.uav.httpasync.HttpAsyncClientFactory;

/**
 * OpentsdbDataSource provide httpAsyncClient
 * 
 * @author hongqiang
 */
public class OpentsdbDataSource extends AbstractDataSource<HttpAsyncClient> {

    private int maxConnectionPerRoute;

    private int maxTotalConnection;

    private int sockTimeout;

    private int connectTimeout;

    private int requestTimeout;

    public OpentsdbDataSource(DataStoreConnection connection) {

        super(connection);

        this.maxConnectionPerRoute = DataConvertHelper
                .toInt(connection.getContext(DataStoreProtocol.OPENTSDB_CLIENT_MAXPERROUTE), 10);

        this.maxTotalConnection = DataConvertHelper.toInt(connection.getContext(DataStoreProtocol.OPENTSDB_CLIENT_MAXROUTE),
                50);

        this.sockTimeout = DataConvertHelper.toInt(connection.getContext(DataStoreProtocol.OPENTSDB_CLIENT_SOTIMEOUT), 5000);

        this.connectTimeout = DataConvertHelper.toInt(connection.getContext(DataStoreProtocol.OPENTSDB_CLIENT_CONNTIMEOUT),
                5000);

        this.requestTimeout = DataConvertHelper.toInt(connection.getContext(DataStoreProtocol.OPENTSDB_CLIENT_REQTIMEOUT),
                5000);

    }

    @Override
    public HttpAsyncClient initSourceConnect() throws Exception {

        HttpAsyncClient client = HttpAsyncClientFactory.build(maxConnectionPerRoute, maxTotalConnection, sockTimeout,
                connectTimeout, requestTimeout);

        if (log.isDebugEnable()) {
            StringBuilder builder = new StringBuilder();
            builder.append("OpentsdbConnect HttpAsyncClient init success:");
            builder.append("maxConnectionPerRoute=" + maxConnectionPerRoute + ",");
            builder.append("maxTotalConnection=" + maxTotalConnection + ",");
            builder.append("sockTimeout=" + sockTimeout + ",");
            builder.append("connectTimeout=" + connectTimeout + ",");
            builder.append("requestTimeout=" + requestTimeout);
            log.debug(this, builder.toString());

        }

        return client;
    }

    @Override
    public void start() {

        super.start();

    }

    @Override
    public void stop() {

        if (sourceConnect != null) {

            sourceConnect.shutdown();

            if (log.isDebugEnable()) {
                log.debug(this, "OpentsdbConnect apacheAsyncClient Close.");
            }
        }
        super.stop();
    }
}
