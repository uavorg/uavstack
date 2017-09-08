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
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.creditease.agent.SystemStarter;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.JVMToolHelper;
import com.creditease.agent.helpers.RuntimeHelper;
import com.creditease.agent.helpers.ThreadHelper;
import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.agent.spi.IConfigurationManager;
import com.creditease.uav.feature.upgrade.beans.UpgradeContext;
import com.creditease.uav.feature.upgrade.beans.UpgradeContext.UpgradePhase;
import com.creditease.uav.feature.upgrade.common.UpgradeConstants;

public class EndAction extends AbstractUpgradeBaseAction {

    public EndAction(String feature, UpgradeContext upgradeContext, IActionEngine engine) {
        super(EndAction.class.getSimpleName(), feature, upgradeContext, engine);
    }

    @Override
    public void doAction(ActionContext context) {

        setUpgradePhase(UpgradePhase.END);

        cleanupFiles();

        trimBackupZipFiles();

        releaseFileLock();

        if (this.upgradeContext.canWriteOperationRecord()) {
            this.writeProcessRecord(this.cName);
        }

        stopUpgradeProcess();
        context.setSucessful(true);

    }

    @Override
    public String getSuccessNextActionId() {

        return null;
    }

    @Override
    public String getFailureNextActionId() {

        return null;
    }

    @Override
    public String getExceptionNextActionId() {

        return null;
    }

    /**
     * Stop current upgrade process
     */
    public void stopUpgradeProcess() {

        if (log.isTraceEnable()) {
            log.info(this, "Stopping current upgrade process");
        }

        SystemStarter starter = this.getSystemStarter();
        starter.stop();

        if (JVMToolHelper.isWindows()) {
            // For windows, currently, no cron task
            System.exit(0);
        }
        else {
            ThreadHelper.suspend(3000);
            if (this.upgradeContext.alreadyGotFileLock()) {
                // For those upgrade processes which succeeded to get file lock, need to stop cron job
                stopCronTaskAndCurrentProcess();
            }

            System.exit(0);
        }
    }

    private void stopCronTaskAndCurrentProcess() {

        String rootDir = this.getConfigManager().getContext(IConfigurationManager.ROOT);
        String profile = this.getConfigManager().getContext(IConfigurationManager.PROFILENAME);

        StringBuffer sbf = new StringBuffer().append("cd ").append(Paths.get(rootDir, "bin").toString());
        sbf.append(" && sh stop_upgrade.sh ").append(profile);
        try {
            if (log.isTraceEnable()) {
                log.info(this, "Execute cmd " + sbf.toString());
            }
            RuntimeHelper.exec(UpgradeConstants.DEFAULT_CMD_EXEC_TIME, "sh", "-c", sbf.toString());
        }
        catch (Exception e) {
            if (log.isTraceEnable()) {
                log.err(this, "Failed to stop crontask and current upgrade process. " + e.getMessage());
            }
        }
    }

    /**
     * trim the backup files according to property "backup.count"
     */
    private void trimBackupZipFiles() {

        int backupCount = DataConvertHelper.toInt(
                this.getConfigManager().getFeatureConfiguration(this.feature, "backup.count"),
                UpgradeConstants.DEFAULT_BACKUP_COUNT);

        List<File> fileList = IOHelper.getFiles(this.getBackupDir());
        Iterator<File> fileIter = fileList.iterator();
        while (fileIter.hasNext()) {
            // exclude files not end with ".zip"
            File file = fileIter.next();
            if (!file.getName().endsWith(".zip")) {
                fileIter.remove();
            }
        }

        if (fileList.size() <= backupCount) {
            if (log.isTraceEnable()) {
                log.info(this, "Current backup file count is " + fileList.size() + ", no need to delete any one");
            }

            return;
        }
        else {

            int fileCountToDelete = fileList.size() - backupCount;
            if (log.isTraceEnable()) {
                log.info(this, "Current backup file count is " + fileList.size() + ", should delete "
                        + fileCountToDelete + " backup zip file(s)");
            }

            SortedMap<Long, File> fileMap = new TreeMap<Long, File>();
            for (File file : fileList) {
                fileMap.put(file.lastModified(), file);
            }

            for (int i = 0; i < fileCountToDelete; i++) {
                Long firstKey = fileMap.firstKey();
                File file = fileMap.get(firstKey);
                fileMap.remove(firstKey);
                if (log.isTraceEnable()) {
                    log.info(this, "Deleting backup file: " + file.getAbsolutePath());
                }

                try {
                    Files.deleteIfExists(Paths.get(file.getAbsolutePath()));
                }
                catch (IOException e) {
                }
            }

        }

    }

    /*
     * 1)Delete all the new files downloaded in this upgrade process 2) Delete upgrade temporary dir 3) delete process
     * record file
     */
    private void cleanupFiles() {

        Path path = Paths.get(this.getBackupDir(), "new");
        if (Files.exists(path)) {

            if (log.isTraceEnable()) {
                log.info(this, "Delete all the new files downloaded in temporary new folder: " + path);
            }

            IOHelper.deleteFolder(path.toString());
        }

        path = Paths.get(this.getRootDir(), "upgrade");
        if (Files.exists(path)) {
            if (log.isTraceEnable()) {
                log.info(this, "Delete upgrade temporary dir: " + path);
            }

            IOHelper.deleteFolder(path.toString());
        }

        if (log.isTraceEnable()) {
            log.info(this, "Delete process record file");
        }

        this.upgradeContext.deleteProcessRecordFile();
    }

}
