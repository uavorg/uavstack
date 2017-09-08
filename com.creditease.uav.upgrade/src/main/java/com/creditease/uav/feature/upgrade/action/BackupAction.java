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

package com.creditease.uav.feature.upgrade.action;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.creditease.agent.helpers.ZIPHelper;
import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.uav.feature.upgrade.beans.UpgradeContext;
import com.creditease.uav.feature.upgrade.beans.UpgradeContext.UpgradePhase;
import com.creditease.uav.feature.upgrade.common.UpgradeConstants;

public class BackupAction extends AbstractUpgradeBaseAction {

    public BackupAction(String feature, UpgradeContext upgradeContext, IActionEngine engine) {
        super(BackupAction.class.getSimpleName(), feature, upgradeContext, engine);

    }

    @Override
    public void doAction(ActionContext context) throws Exception {

        setUpgradePhase(UpgradePhase.BACKUP);
        if (log.isTraceEnable()) {
            log.info(this, "Need to backup current uav files firstly");
        }

        backup();
        writeProcessRecord(this.cName);
        context.setSucessful(true);

    }

    @Override
    public String getSuccessNextActionId() {

        return UpgradeConstants.getActionName(false, UpgradeConstants.DOWNLOAD_ACTION_KEY);
    }

    @Override
    public String getFailureNextActionId() {

        return UpgradeConstants.getActionName(false, UpgradeConstants.END_ACTION_KEY);
    }

    @Override
    public String getExceptionNextActionId() {

        return getFailureNextActionId();
    }

    /**
     * Backup necessary current uav files, including folders as "bin", "lib" and "config" in ${backupDir}
     * 
     * @throws IOException
     */
    private void backup() throws Exception {

        Path path = Paths.get(this.getBackupDir());
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }

        if (log.isTraceEnable()) {
            log.info(this, "Start backuping current version");
        }

        Path targetBackupZipPath = getBackupZipPath();
        if (Files.exists(targetBackupZipPath)) {
            if (log.isTraceEnable()) {
                log.info(this, "The backup zip: " + targetBackupZipPath + " already exist.");
            }
            return;
        }

        ZIPHelper.compressFilesToZip(getAgentName(), getBackupSourceDirs(), targetBackupZipPath.toString());
        if (log.isTraceEnable()) {
            log.info(this, "Finished backuping current version to " + getBackupZipPath());
        }
    }

    /***
     * Get backup source directories
     * 
     * @return String array, e.g., ["/app/uav/uavagent/bin", "/app/uav/uavagent/lib", "/app/uav/uavagent/config"]
     */
    private String[] getBackupSourceDirs() {

        List<String> backupDirSet = this.upgradeContext.getUpgradeConfig().getBackupFilesList();
        String[] backupDirs = new String[backupDirSet.size()];
        int i = 0;
        for (String backupDir : backupDirSet) {
            backupDirs[i] = this.getRootDir() + File.separator + backupDir;
            i++;
        }

        return backupDirs;
    }

}
