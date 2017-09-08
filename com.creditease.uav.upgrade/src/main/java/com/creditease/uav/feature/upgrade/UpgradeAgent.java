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

package com.creditease.uav.feature.upgrade;

import static com.creditease.uav.feature.upgrade.common.UpgradeConstants.UPGRADE_INFO;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.helpers.ThreadHelper;
import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.agent.spi.IConfigurationManager;
import com.creditease.uav.feature.upgrade.action.BackupAction;
import com.creditease.uav.feature.upgrade.action.EndAction;
import com.creditease.uav.feature.upgrade.action.OverrideFileAction;
import com.creditease.uav.feature.upgrade.action.PackageDownloadAction;
import com.creditease.uav.feature.upgrade.action.thirdparty.ThirdpartySoftwareOverrideFileAction;
import com.creditease.uav.feature.upgrade.action.uav.StartUAVProcessAction;
import com.creditease.uav.feature.upgrade.action.uav.StopUAVProcessAction;
import com.creditease.uav.feature.upgrade.action.uav.UAVOverrideFileAction;
import com.creditease.uav.feature.upgrade.beans.UpgradeConfig;
import com.creditease.uav.feature.upgrade.beans.UpgradeContext;
import com.creditease.uav.feature.upgrade.beans.UpgradeContext.UpgradePhase;
import com.creditease.uav.feature.upgrade.common.UpgradeConstants;
import com.creditease.uav.feature.upgrade.common.UpgradeUtil;
import com.creditease.uav.feature.upgrade.record.UpgradeOperationRecordConsumer;

public class UpgradeAgent extends AgentFeatureComponent implements Runnable {

    private UpgradeContext upgradeContext;
    private IActionEngine engine;
    private Thread workThread;
    private EndAction endAction;

    public UpgradeAgent(String cName, String feature) {
        super(cName, feature);
    }

    @SuppressWarnings("unused")
    @Override
    public void start() {

        // initial ActionEngine
        this.engine = this.getActionEngineMgr().newActionEngine("UpgradeActionEngine", this.feature);

        String restart = System.getProperty("StartByCronTask");
        String upgradeInfo = System.getProperty(UPGRADE_INFO);

        if (log.isTraceEnable()) {
            log.info(this, "Upgrade param info is " + upgradeInfo);
            log.info(this, System.getProperty("java.class.path"));
        }

        if (DataConvertHelper.toInt(restart, Integer.MIN_VALUE) == 1) {
            if (log.isTraceEnable()) {
                log.info(this, "This upgrade process was restarted by cron task ");
            }

            ThreadHelper.suspend(3000);
            UpgradeOperationRecordConsumer consumer = new UpgradeOperationRecordConsumer(
                    "UpgradeOperationRecordConsumer", this.feature, this.engine);
            consumer.handleOperationRecord(upgradeInfo);
            return;
        }

        this.upgradeContext = new UpgradeContext(new UpgradeConfig(upgradeInfo));
        this.endAction = new EndAction(this.feature, upgradeContext, engine);

        checkVersion4Upgrade(endAction);

        String host = this.getConfigManager().getFeatureConfiguration(this.feature, "http.server.host");
        int port = DataConvertHelper.toInt(
                this.getConfigManager().getFeatureConfiguration(this.feature, "http.server.port"),
                UpgradeConstants.DEFAULT_HTTP_PORT);

        if (StringHelper.isEmpty(host)) {
            if (log.isTraceEnable()) {
                log.warn(this, "Invalid http server host");
            }

            endAction.stopUpgradeProcess();
            return;
        }

        if (!this.upgradeContext.getFileLock()) {
            if (log.isTraceEnable()) {
                log.warn(this, "Can not get the lock of " + UpgradeConstants.UPGRADE_FILE_LOCK_NAME
                        + ", another upgrade process is running");
            }

            endAction.stopUpgradeProcess();
            return;
        }

        BackupAction backupAction = new BackupAction(this.feature, upgradeContext, engine);
        PackageDownloadAction downloadAction = new PackageDownloadAction(this.feature, upgradeContext, engine, host,
                port);

        registerActionsAccordingUpgradeTarget();
        workThread = new Thread(this, "Upgrade Thread");
        workThread.start();
    }

    @Override
    public void run() {

        if (log.isTraceEnable()) {
            log.info(this, "Sleep 5 seconds for waiting other features, and then start upgrade process");
        }

        ThreadHelper.suspend(5000);

        this.upgradeContext.initProcessRecordFile();

        ActionContext context = new ActionContext();
        this.engine.execute(UpgradeConstants.getActionName(false, UpgradeConstants.BACKUP_ACTION_KEY), context);
    }

    @Override
    public void stop() {

        if (log.isTraceEnable()) {
            log.info(this, "Stoping upgrade process");
        }

        this.getActionEngineMgr().shutdown("UpgradeActionEngine");

        if (upgradeContext.getCurrentPhase() != UpgradePhase.END) {
            // handle the case which receive shutdown cmd from outside.
            this.endAction.doAction(new ActionContext());
        }

        super.stop();
    }

    @Override
    public Object exchange(String eventKey, Object... data) {

        return null;
    }

    @SuppressWarnings("unused")
    private void registerActionsAccordingUpgradeTarget() {

        if (this.upgradeContext.isUAVContext()) {
            StopUAVProcessAction stopAction = new StopUAVProcessAction(this.feature, this.upgradeContext, engine);
            OverrideFileAction overrideFileAction = new UAVOverrideFileAction(this.feature, this.upgradeContext,
                    engine);
            StartUAVProcessAction startAction = new StartUAVProcessAction(this.feature, this.upgradeContext, engine);
        }
        else {
            OverrideFileAction overrideFileAction = new ThirdpartySoftwareOverrideFileAction(this.feature,
                    this.upgradeContext, engine);
        }
    }

    /**
     * if target version equals current version, no need to do upgrade.
     */
    private void checkVersion4Upgrade(EndAction endAction) {

        String currentVersion = null;
        if (this.upgradeContext.isUAVContext()) {
            currentVersion = this.getConfigManager().getContext(IConfigurationManager.NODEAPPVERSION);
        }
        else {
            currentVersion = this.upgradeContext.getThirdPartySoftwareCurrentVersion();
        }

        if (currentVersion.equals(
                UpgradeUtil.getVersionFromPackageName(this.upgradeContext.getUpgradeConfig().getSoftwarePackage()))) {
            if (log.isTraceEnable()) {
                log.warn(this, "No need to upgrade, current version equals target version.");
            }

            endAction.stopUpgradeProcess();
        }
    }

}
