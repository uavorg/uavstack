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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.uavstack.resources.common.messaging.StandardMessagingBuilder;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.agent.profile.api.StandardProfileModeler;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.agent.spi.IConfigurationManager;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.cache.api.CacheManagerFactory;
import com.creditease.uav.datastore.api.DataStoreAdapter;
import com.creditease.uav.datastore.api.DataStoreConnection;
import com.creditease.uav.datastore.api.DataStoreFactory;
import com.creditease.uav.datastore.api.DataStoreFactory.DataStoreType;
import com.creditease.uav.datastore.api.DataStoreMsg;
import com.creditease.uav.datastore.api.DataStoreProtocol;
import com.creditease.uav.datastore.core.AbstractDataStore;
import com.creditease.uav.feature.healthmanager.HealthManagerConstants;
import com.creditease.uav.feature.healthmanager.HealthManagerProfileDataLifeKeeper;
import com.creditease.uav.feature.healthmanager.HealthManagerServerWorker;
import com.creditease.uav.feature.healthmanager.datastore.adaptors.LogDataAdapter;
import com.creditease.uav.feature.healthmanager.datastore.adaptors.MonitorDataAdapter;
import com.creditease.uav.feature.healthmanager.datastore.adaptors.NodeInfoDataAdapter;
import com.creditease.uav.feature.healthmanager.datastore.adaptors.NotifyDataAdpater;
import com.creditease.uav.feature.healthmanager.datastore.adaptors.ProfileDataAdpater;
import com.creditease.uav.messaging.api.MessageConsumer;

public class HealthManager extends AgentFeatureComponent {

    private MessageConsumer monitorDataConsumer;

    private MessageConsumer notificationConsumer;

    private MessageConsumer profileDataConsumer;

    private MessageConsumer logDataConsumer;

    private MessageConsumer nodeinfoDataConsumer;

    private HealthManagerServerWorker healthServerListenWorker;

    private boolean isStartLifeKeeper = false;

    public HealthManager(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public void start() {

        // init cache manager
        String cacheServerAddress = this.getConfigManager().getFeatureConfiguration(this.feature, "store.addr");
        int minConcurrent = Integer
                .parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "store.concurrent.min"));
        int maxConcurrent = Integer
                .parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "store.concurrent.max"));
        int queueSize = Integer
                .parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "store.concurrent.bqsize"));
        String password = this.getConfigManager().getFeatureConfiguration(this.feature, "store.concurrent.pwd");

        CacheManager cm = CacheManagerFactory.build(cacheServerAddress, minConcurrent, maxConcurrent, queueSize,
                password);

        this.getConfigManager().registerComponent(this.feature, "HMCacheManager", cm);

        // start HealthManagerProfileDataLifeKeeper
        isStartLifeKeeper = Boolean
                .parseBoolean(this.getConfigManager().getFeatureConfiguration(this.feature, "lifekeeper.enable"));

        if (isStartLifeKeeper == true) {

            // init HealthManagerProfileDataLifeKeeper
            HealthManagerProfileDataLifeKeeper profileDataLifeKeepWorker = new HealthManagerProfileDataLifeKeeper(
                    "HealthManagerProfileDataLifeKeeper", this.feature);

            long interval = Long
                    .parseLong(this.getConfigManager().getFeatureConfiguration(this.feature, "lifekeeper.interval"));

            long randomDely = new Random().nextInt(3) + 3;

            this.getTimerWorkManager().scheduleWorkInPeriod("HealthManagerProfileDataLifeKeeper",
                    profileDataLifeKeepWorker, randomDely * 1000, interval);

            if (log.isTraceEnable()) {
                log.info(this, "HealthManagerProfileDataLifeKeeper started");
            }
        }

        /**
         * Start the DBStore service NOTE: this must be the first to start
         */
        buildDataStores(this.getConfigManager());

        // start all datastores
        DataStoreFactory.getInstance().startAll(this.feature);

        if (log.isTraceEnable()) {
            log.info(this, "HealthManager DataStore Factory started");
        }

        /**
         * Start the HealthManger Http service
         */
        int port = Integer.parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.port"));
        int backlog = Integer.parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.backlog"));
        int core = Integer.parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.core"));
        int max = Integer.parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.max"));
        int bqsize = Integer.parseInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.bqsize"));

        healthServerListenWorker = new HealthManagerServerWorker("HealthMangerServerWorker", this.feature,
                "healthMangerHandlers");

        @SuppressWarnings({ "rawtypes", "unchecked" })
        ThreadPoolExecutor exe = new ThreadPoolExecutor(core, max, 30000, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue(bqsize));

        healthServerListenWorker.start(exe, port, backlog);

        if (log.isTraceEnable()) {
            log.info(this, "HealthManager DataStore HttpServer started");
        }

        StandardMessagingBuilder smb = new StandardMessagingBuilder("HMCommonMsgBuilder", this.feature);

        try {
            smb.init("com.creditease.uav.feature.healthmanager.messaging.handlers");
        }
        catch (IOException e) {
            log.err(this, "Read msgtype2topic.properties FAILs, HealthManager can not START", e);
            return;
        }

        monitorDataConsumer = smb.buildConsumer(MonitorDataFrame.MessageType.Monitor.toString());

        notificationConsumer = smb.buildConsumer(MonitorDataFrame.MessageType.Notification.toString());

        profileDataConsumer = smb.buildConsumer(MonitorDataFrame.MessageType.Profile.toString());

        logDataConsumer = smb.buildConsumer(MonitorDataFrame.MessageType.Log.toString());

        nodeinfoDataConsumer = smb.buildConsumer(MonitorDataFrame.MessageType.NodeInfo.toString());

        // start monitorDataConsumer
        if (monitorDataConsumer != null) {

            monitorDataConsumer.start();

            this.getConfigManager().registerComponent(this.feature, HealthManagerConstants.COMSUMER_MONITOR,
                    monitorDataConsumer);

            if (log.isTraceEnable()) {
                log.info(this, "HealthManager MDFConsumer started");
            }
        }

        // start notificationConsumer
        if (this.notificationConsumer != null) {
            notificationConsumer.start();

            this.getConfigManager().registerComponent(this.feature, HealthManagerConstants.COMSUMER_NOTIFY,
                    notificationConsumer);

            if (log.isTraceEnable()) {
                log.info(this, "HealthManager NotifyConsumer started");
            }
        }

        // start profileDataConsumer
        if (this.profileDataConsumer != null) {

            /**
             * INIT StandardProfileModelingEngine & StandardProfileModeler
             */
            IActionEngine engine = this.getActionEngineMgr().newActionEngine("StandardProfileModelingEngine", feature);

            new StandardProfileModeler("StandardProfileModeler", feature, engine);

            // start profile consumer
            profileDataConsumer.start();

            this.getConfigManager().registerComponent(this.feature, HealthManagerConstants.COMSUMER_PROFILE,
                    profileDataConsumer);

            if (log.isTraceEnable()) {
                log.info(this, "HealthManager ProfileConsumer started");
            }
        }

        // start logDataConsumer
        if (this.logDataConsumer != null) {
            logDataConsumer.start();

            this.getConfigManager().registerComponent(this.feature, HealthManagerConstants.COMSUMER_LOG,
                    logDataConsumer);

            if (log.isTraceEnable()) {
                log.info(this, "HealthManager LogConsumer started");
            }
        }

        // start nodeinfoDataConsumer
        if (nodeinfoDataConsumer != null) {

            nodeinfoDataConsumer.start();

            this.getConfigManager().registerComponent(this.feature, HealthManagerConstants.COMSUMER_NODE,
                    nodeinfoDataConsumer);

            if (log.isTraceEnable()) {
                log.info(this, "HealthManager NodeInfoConsumer started");
            }
        }
    }

    @Override
    public void stop() {

        // stop monitorDataConsumer
        if (this.monitorDataConsumer != null) {
            monitorDataConsumer.shutdown();
            this.getConfigManager().unregisterComponent(this.feature, HealthManagerConstants.COMSUMER_MONITOR);
            if (log.isTraceEnable()) {
                log.info(this, "HealthManager MdfConsumer shutdown");
            }
        }

        // stop notificationConsumer
        if (this.notificationConsumer != null) {
            notificationConsumer.shutdown();
            this.getConfigManager().unregisterComponent(this.feature, HealthManagerConstants.COMSUMER_NOTIFY);
            if (log.isTraceEnable()) {
                log.info(this, "HealthManager NotifyConsumer shutdown");
            }
        }

        // stop profileDataConsumer
        if (this.profileDataConsumer != null) {

            // shut down StandardProfileModelingEngine
            this.getActionEngineMgr().shutdown("StandardProfileModelingEngine");

            profileDataConsumer.shutdown();
            this.getConfigManager().unregisterComponent(this.feature, HealthManagerConstants.COMSUMER_PROFILE);
            if (log.isTraceEnable()) {
                log.info(this, "HealthManager ProfileConsumer shutdown");
            }
        }

        // stop logDataConsumer
        if (this.logDataConsumer != null) {
            logDataConsumer.shutdown();
            this.getConfigManager().unregisterComponent(this.feature, HealthManagerConstants.COMSUMER_LOG);
            if (log.isTraceEnable()) {
                log.info(this, "HealthManager LogConsumer shutdown");
            }
        }

        // stop nodeinfoDataConsumer
        if (this.nodeinfoDataConsumer != null) {
            nodeinfoDataConsumer.shutdown();
            this.getConfigManager().unregisterComponent(this.feature, HealthManagerConstants.COMSUMER_NODE);
            if (log.isTraceEnable()) {
                log.info(this, "HealthManager NodeInfoConsumer shutdown");
            }
        }
        // stop healthServerListenWorker
        healthServerListenWorker.stop();

        if (log.isTraceEnable()) {
            log.info(this, "HealthManager DataStore HttpServer shutdown");
        }

        // NOTE: this should be the last to stop
        DataStoreFactory.getInstance().stopAll(this.feature);

        if (log.isTraceEnable()) {
            log.info(this, "HealthManager DataStore Factory shutdown");
        }

        if (isStartLifeKeeper == true) {

            // stop HealthManagerProfileDataLifeKeeper
            this.getTimerWorkManager().cancel("HealthManagerProfileDataLifeKeeper");

            if (log.isTraceEnable()) {
                log.info(this, "HealthManagerProfileDataLifeKeeper stopped");
            }
        }

        // shutdown CacheManager
        CacheManager HMCacheManager = (CacheManager) this.getConfigManager().getComponent(this.feature,
                "HMCacheManager");
        HMCacheManager.shutdown();

        super.stop();
    }

    private void buildDataStores(IConfigurationManager icm) {

        // log
        String logKey = MonitorDataFrame.MessageType.Log.toString();
        // monitor
        String monitorKey = MonitorDataFrame.MessageType.Monitor.toString();

        // profile
        String profileKey = MonitorDataFrame.MessageType.Profile.toString();

        // notification
        String notifyKey = MonitorDataFrame.MessageType.Notification.toString();

        // nodeinfo
        String nodeinfoKey = MonitorDataFrame.MessageType.NodeInfo.toString();

        String caching = icm.getFeatureConfiguration(this.feature, logKey + ".ds.cache");
        String maxResultSize = icm.getFeatureConfiguration(this.feature, logKey + ".ds.maxResultSize");
        String reverse = icm.getFeatureConfiguration(this.feature, logKey + ".ds.reverse");
        String psize = icm.getFeatureConfiguration(this.feature, logKey + ".ds.pagesize");

        Map<String, Object> lctx = new HashMap<String, Object>();
        lctx.put(DataStoreProtocol.HBASE_QUERY_CACHING, caching);
        lctx.put(DataStoreProtocol.HBASE_QUERY_MAXRESULTSIZE, maxResultSize);
        lctx.put(DataStoreProtocol.HBASE_QUERY_REVERSE, DataConvertHelper.toBoolean(Boolean.valueOf(reverse), true));
        lctx.put(DataStoreProtocol.HBASE_QUERY_PAGESIZE, DataConvertHelper.toLong(Long.valueOf(psize), 3000));
        // build log ds
        buildDataStore(icm, logKey, DataStoreType.HBASE, new LogDataAdapter(), lctx);

        Map<String, Object> mctx = new HashMap<String, Object>();

        mctx.put(DataStoreProtocol.OPENTSDB_CLIENT_MAXPERROUTE,
                DataConvertHelper.toInt(icm.getFeatureConfiguration(this.feature, monitorKey + ".ds.maxPerRoute"), 10));
        mctx.put(DataStoreProtocol.OPENTSDB_CLIENT_MAXROUTE,
                DataConvertHelper.toInt(icm.getFeatureConfiguration(this.feature, monitorKey + ".ds.maxRoute"), 50));
        mctx.put(DataStoreProtocol.OPENTSDB_CLIENT_SOTIMEOUT,
                DataConvertHelper.toInt(icm.getFeatureConfiguration(this.feature, monitorKey + ".ds.soTimeout"), 5000));
        mctx.put(DataStoreProtocol.OPENTSDB_CLIENT_CONNTIMEOUT, DataConvertHelper
                .toInt(icm.getFeatureConfiguration(this.feature, monitorKey + ".ds.connTimeout"), 5000));
        mctx.put(DataStoreProtocol.OPENTSDB_CLIENT_REQTIMEOUT, DataConvertHelper
                .toInt(icm.getFeatureConfiguration(this.feature, monitorKey + ".ds.reqTimeout"), 5000));

        // build monitor ds
        buildDataStore(icm, monitorKey, DataStoreType.OPENTSDB, new MonitorDataAdapter(), mctx);

        // build profile ds
        buildDataStore(icm, profileKey, DataStoreType.MONGODB, new ProfileDataAdpater(), null);

        // build notify ds
        buildDataStore(icm, notifyKey, DataStoreType.MONGODB, new NotifyDataAdpater(), null);

        Map<String, Object> nctx = new HashMap<String, Object>();
        mctx.put(DataStoreProtocol.OPENTSDB_CLIENT_MAXPERROUTE, DataConvertHelper
                .toInt(icm.getFeatureConfiguration(this.feature, nodeinfoKey + ".ds.maxPerRoute"), 10));
        mctx.put(DataStoreProtocol.OPENTSDB_CLIENT_MAXROUTE,
                DataConvertHelper.toInt(icm.getFeatureConfiguration(this.feature, nodeinfoKey + ".ds.maxRoute"), 50));
        mctx.put(DataStoreProtocol.OPENTSDB_CLIENT_SOTIMEOUT, DataConvertHelper
                .toInt(icm.getFeatureConfiguration(this.feature, nodeinfoKey + ".ds.soTimeout"), 5000));
        mctx.put(DataStoreProtocol.OPENTSDB_CLIENT_CONNTIMEOUT, DataConvertHelper
                .toInt(icm.getFeatureConfiguration(this.feature, nodeinfoKey + ".ds.connTimeout"), 5000));
        mctx.put(DataStoreProtocol.OPENTSDB_CLIENT_REQTIMEOUT, DataConvertHelper
                .toInt(icm.getFeatureConfiguration(this.feature, nodeinfoKey + ".ds.reqTimeout"), 5000));

        // build nodeinfo ds
        buildDataStore(icm, nodeinfoKey, DataStoreType.OPENTSDB, new NodeInfoDataAdapter(), nctx);

    }

    /**
     * buildDataStore
     * 
     * @param icm
     * @param dsName
     * @param type
     * @param adaptor
     * @param context
     */
    private void buildDataStore(IConfigurationManager icm, String dsName, DataStoreType type, DataStoreAdapter adaptor,
            Map<String, Object> context) {

        boolean enable = false;

        enable = Boolean.parseBoolean(icm.getFeatureConfiguration(this.feature, dsName + ".ds.enable"));

        if (enable) {

            String serverlist = icm.getFeatureConfiguration(this.feature, dsName + ".ds.servers");

            if (serverlist == null) {
                log.warn(this, "Config[ds.servers] of DataStore[" + dsName + "] is NULL.");
                return;
            }

            String dbName = icm.getFeatureConfiguration(this.feature, dsName + ".ds.db");

            String dbPower = icm.getFeatureConfiguration(this.feature, dsName + ".ds.power");

            String userName = icm.getFeatureConfiguration(this.feature, dsName + ".ds.usr");

            String password = icm.getFeatureConfiguration(this.feature, dsName + ".ds.pwd");

            int retry = DataConvertHelper.toInt(icm.getFeatureConfiguration(this.feature, dsName + ".ds.retry"), 3);

            int expire = DataConvertHelper.toInt(icm.getFeatureConfiguration(this.feature, dsName + ".ds.expire"), 3);

            List<String> servers = DataConvertHelper.toList(serverlist, ",");

            DataStoreConnection conn = new DataStoreConnection(userName, password, dbName, dbPower, servers, type);

            conn.setRetryTimes(retry);

            conn.setBlackExpireTime(expire);

            if (context != null) {
                conn.putContextAll(context);
            }

            DataStoreFactory.getInstance().build(dsName, conn, adaptor, this.feature);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object exchange(String eventKey, Object... data) {

        log.info(this, "HM got exchange event");

        if (null == data) {
            log.err(this, "the exchange data is empty");
            return null;
        }
        DataStoreMsg msg = (DataStoreMsg) data[0];

        AbstractDataStore dataStore = DataStoreFactory.getInstance()
                .get((String) msg.get(DataStoreProtocol.DATASTORE_NAME));

        Object rst = null;

        switch (eventKey) {
            case HealthManagerConstants.QUERY:

                rst = dataStore.doQuery(msg);
                return rst;
            case HealthManagerConstants.INSERT:

                rst = dataStore.doUpdate(msg);
                return rst;
            case HealthManagerConstants.UPDATE:

                rst = dataStore.doUpdate(msg);
                return rst;
        }
        throw new RuntimeException("Exchange Event [" + eventKey + "] handle FAIL: data=" + data);
    }
}
