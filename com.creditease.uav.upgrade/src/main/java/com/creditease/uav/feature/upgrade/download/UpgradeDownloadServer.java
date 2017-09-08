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

package com.creditease.uav.feature.upgrade.download;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.agent.spi.I1NQueueWorker;
import com.creditease.uav.feature.upgrade.common.UpgradeConstants;

public class UpgradeDownloadServer extends AgentFeatureComponent {

    public UpgradeDownloadServer(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public void start() {

        initDownloadServerWorker();
        init1NQueueWorker();
    }

    private void initDownloadServerWorker() {

        int port = DataConvertHelper.toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.port"),
                UpgradeConstants.DEFAULT_HTTP_PORT);

        int backlog = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.backlog"), 10);

        int bossGroupSize = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.bossgroup.size"), 2);

        int workerGroupSize = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "http.wokergroup.size"), 10);

        UpgradeDownloadServerWorker downloadServerWorker = new UpgradeDownloadServerWorker(
                UpgradeDownloadServerWorker.class.getSimpleName(), this.feature, "drhandlers");

        downloadServerWorker.start(port, backlog, bossGroupSize, workerGroupSize);

        if (log.isTraceEnable()) {
            log.info(this,
                    String.format(
                            UpgradeDownloadServerWorker.class.getSimpleName()
                                    + " started: port:%d, backlog:%d, boss group size:%d, worker group size: %d",
                            port, backlog, bossGroupSize, workerGroupSize));
        }
    }

    private void init1NQueueWorker() {

        int coreSize = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "1nqworker.coresize"), 10);
        int maxSize = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "1nqworker.maxsize"), 10);

        int bqSize = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "1nqworker.bqsize"), 1);

        int timeout = DataConvertHelper.toInt(
                this.getConfigManager().getFeatureConfiguration(this.feature, "1nqworker.keepalivetimeout"), 60000);

        I1NQueueWorker queueWoker = get1NQueueWorkerMgr().newQueueWorker(UpgradeConstants.UPGRADE_1N_QUEUE_NAME,
                this.feature, coreSize, maxSize, bqSize, timeout);

        new Thread(queueWoker, UpgradeConstants.UPGRADE_1N_QUEUE_NAME).start();

        if (log.isTraceEnable()) {
            log.info(this,
                    String.format(
                            UpgradeConstants.UPGRADE_1N_QUEUE_NAME
                                    + " started: coresize:%d,maxsize:%d,bqsize:%d,keepalivetimeout:%d",
                            coreSize, maxSize, bqSize, timeout));
        }
    }

    @Override
    public Object exchange(String eventKey, Object... data) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void stop() {

        if (log.isTraceEnable()) {
            log.info(this, "Stopping 1N queue: " + UpgradeConstants.UPGRADE_1N_QUEUE_NAME);
        }

        this.get1NQueueWorkerMgr().shutdown(this.feature, UpgradeConstants.UPGRADE_1N_QUEUE_NAME);

        if (log.isTraceEnable()) {
            log.info(this, "Stopping Download server worker: " + UpgradeDownloadServerWorker.class.getSimpleName());
        }
        UpgradeDownloadServerWorker downloadServerWorker = (UpgradeDownloadServerWorker) this.getConfigManager()
                .getComponent(this.feature, "UpgradeDownloadServerWorker");
        downloadServerWorker.stop();

        super.stop();
    }

}
