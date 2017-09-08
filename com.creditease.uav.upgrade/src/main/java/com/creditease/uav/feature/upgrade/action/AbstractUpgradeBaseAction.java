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

import static com.creditease.uav.feature.upgrade.common.UpgradeConstants.BACKUP_FOLDER;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedMap;
import java.util.TreeMap;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.JVMToolHelper;
import com.creditease.agent.spi.AbstractBaseAction;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.agent.spi.IConfigurationManager;
import com.creditease.uav.feature.upgrade.beans.UpgradeContext;
import com.creditease.uav.feature.upgrade.beans.UpgradeContext.UpgradePhase;
import com.creditease.uav.feature.upgrade.record.UpgradeOperationRecord;

public abstract class AbstractUpgradeBaseAction extends AbstractBaseAction {

    private String rootDir;
    private String backupDir;
    private Path backupZipPath;
    protected UpgradeContext upgradeContext;

    public AbstractUpgradeBaseAction(String cName, String feature, UpgradeContext upgradeContext,
            IActionEngine engine) {
        super(cName, feature, engine);
        if (upgradeContext != null) {
            this.upgradeContext = upgradeContext;
            this.rootDir = this.upgradeContext.getUpgradeConfig().getTargetDir() == null
                    ? this.getConfigManager().getContext(IConfigurationManager.ROOT)
                    : this.upgradeContext.getUpgradeConfig().getTargetDir();
            this.backupDir = this.rootDir + File.separator + BACKUP_FOLDER;
        }
    }

    protected Path getUpgradePackagePath() {

        return Paths.get(this.backupDir, "new", this.upgradeContext.getUpgradeConfig().getSoftwarePackage());
    }

    /**
     * Get the zip path for backup
     * 
     */
    protected Path getBackupZipPath() {

        if (this.backupZipPath != null) {
            return backupZipPath;
        }

        if (this.upgradeContext.isUAVContext()) {
            this.backupZipPath = Paths.get(this.backupDir, this.getAgentName() + "_"
                    + this.getConfigManager().getContext(IConfigurationManager.NODEAPPVERSION) + ".zip");
        }
        else {
            // For third-party software
            String version = this.upgradeContext.getThirdPartySoftwareCurrentVersion();

            this.backupZipPath = Paths.get(this.backupDir, this.getAgentName() + "_" + version + ".zip");
        }

        return backupZipPath;
    }

    public void setBackupZipPath(Path path) {

        this.backupZipPath = path;
    }

    protected void writeProcessRecord(String cName, Object... object) {

        UpgradePhase phase = getUpgradePhase();

        SortedMap<String, Object> recordMap = new TreeMap<String, Object>();
        recordMap.put("phase", phase);
        recordMap.put("cName", cName);
        recordMap.put("timestamp", System.currentTimeMillis());

        SortedMap<String, Object> actionMap = new TreeMap<String, Object>();
        SortedMap<String, String> paramMap = new TreeMap<String, String>();

        switch (phase) {
            case START:
                break;

            case BACKUP:
                actionMap.put("backup", getBackupZipPath().toString());
                break;

            case DOWNLOAD:
                actionMap.put("download", getUpgradePackagePath().toString());
                break;

            case PROCESS_STOP:
                actionMap.put("stop_process", JSONHelper.toString(object[0]));
                actionMap.put("ratio", String.valueOf(object[1]));
                actionMap.put("processes", object[2]);
                break;

            case OVERRIDE_FILE:
                paramMap.put("source", String.valueOf(object[0]));
                paramMap.put("target", getRootDir());
                paramMap.put("backup", getBackupZipPath().toString());
                actionMap.put("override", paramMap);
                break;

            case PROCESS_START:
                actionMap.put("start_process", JSONHelper.toString(object[0]));
                actionMap.put("ratio", String.valueOf(object[1]));
                actionMap.put("processes", object[2]);
                break;

            case END:
            default:
                break;

        }

        recordMap.put("action", actionMap);

        this.upgradeContext.appendProcessRecord(new UpgradeOperationRecord(recordMap));
    }

    protected String getAgentName() {

        return Paths.get(this.rootDir).getFileName().toString();
    }

    protected Path getBinPath() {

        return Paths.get(this.rootDir, "bin");
    }

    public String getSoftwarePackage() {

        return this.upgradeContext.getUpgradeConfig().getSoftwarePackage();
    }

    public String getSoftwareId() {

        return this.upgradeContext.getUpgradeConfig().getSoftwareId();
    }

    public String getRootDir() {

        return this.rootDir;
    }

    public String getBackupDir() {

        return this.backupDir;
    }

    public UpgradePhase getUpgradePhase() {

        return this.upgradeContext.getCurrentPhase();
    }

    public void setUpgradePhase(UpgradePhase phase) {

        if (log.isTraceEnable()) {
            log.info(this, "Current upgrade process phase is " + phase);
        }

        this.upgradeContext.setCurrentPhase(phase);
    }

    public void releaseFileLock() {

        this.upgradeContext.releaseFileLock();
    }

    protected String getParentDirOfRoot() {

        return Paths.get(this.getRootDir()).getParent().toString();
    }

    /**
     * Get os execution command according to current os
     * 
     * @return String array with two elements
     */
    protected String[] getOSExecuteCmd() {

        return JVMToolHelper.isWindows() ? new String[] { "cmd.exe", "/c" } : new String[] { "/bin/sh", "-c" };
    }

}
