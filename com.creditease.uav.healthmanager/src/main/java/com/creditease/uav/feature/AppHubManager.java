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

package com.creditease.uav.feature;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.uav.datastore.api.DataStoreAdapter;
import com.creditease.uav.datastore.api.DataStoreConnection;
import com.creditease.uav.datastore.api.DataStoreFactory;
import com.creditease.uav.feature.apphubmanager.AppHubManagerServerWorker;
import com.creditease.uav.feature.apphubmanager.datastore.adaptors.AppDataAdpater;
import com.creditease.uav.feature.apphubmanager.datastore.adaptors.FeedbackDataAdpater;
import com.creditease.uav.feature.apphubmanager.datastore.adaptors.GroupDataAdpater;

/**
 * @author Created by lbay on 2016/4/21.
 */
public class AppHubManager extends AgentFeatureComponent {

    private AppHubManagerServerWorker apphubManagerServerListenWorker = null;

    public AppHubManager(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public void start() {

        // start AppHubMangerServerWorker
        int port = Integer.parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.port"));
        int backlog = Integer.parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.backlog"));
        int core = Integer.parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.core"));
        int max = Integer.parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.max"));
        int bqsize = Integer.parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.bqsize"));
        apphubManagerServerListenWorker = new AppHubManagerServerWorker("ApphubMangerServerWorker", this.feature,
                "appHubManagerHandlers");
        @SuppressWarnings({ "rawtypes", "unchecked" })
        ThreadPoolExecutor exe = new ThreadPoolExecutor(core, max, 30000, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue(bqsize));
        apphubManagerServerListenWorker.start(exe, port, backlog);

        if (log.isTraceEnable()) {
            log.info(this, "AppHubManager DataStore HttpServer started");
        }

        buildDBService("AppHub.app", new AppDataAdpater());
        buildDBService("AppHub.group", new GroupDataAdpater());
        buildDBService("AppHub.feedback", new FeedbackDataAdpater());
        // start datastore
        DataStoreFactory.getInstance().startAll(this.feature);
    }

    @Override
    public void stop() {

        // stop healthServerListenWorker
        apphubManagerServerListenWorker.stop();

        if (log.isTraceEnable()) {
            log.info(this, "AppHubManager DataStore HttpServer shutdown");
        }

        // stop datastore
        DataStoreFactory.getInstance().stopAll(this.feature);
        super.stop();
    }

    @Override
    public Object exchange(String eventKey, Object... data) {

        return null;
    }

    /**
     * 构建db服务
     * 
     * @param dataStoreName
     * @param adapter
     */
    private void buildDBService(String dataStoreName, DataStoreAdapter adapter) {

        // 验证是否可用
        Boolean check = Boolean
                .parseBoolean(this.getConfigManager().getFeatureConfiguration(this.feature, "ds.enable"));
        if (!check) {
            return;
        }
        String serverlist = this.getConfigManager().getFeatureConfiguration(this.feature, "ds.servers");
        String dbName = this.getConfigManager().getFeatureConfiguration(this.feature, "ds.db");
        String dbPower = this.getConfigManager().getFeatureConfiguration(this.feature, "ds.power");
        String userName = this.getConfigManager().getFeatureConfiguration(this.feature, "ds.usr");
        String password = this.getConfigManager().getFeatureConfiguration(this.feature, "ds.pwd");
        List<String> servers = DataConvertHelper.toList(serverlist, ",");
        DataStoreConnection obj_mongo = new DataStoreConnection(userName, password, dbName, dbPower, servers,
                DataStoreFactory.DataStoreType.MONGODB);
        DataStoreFactory.getInstance().build(dataStoreName, obj_mongo, adapter, this.feature);
    }

}
