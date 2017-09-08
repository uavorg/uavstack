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

package com.creditease.uav.feature.upgrade.beans;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.agent.spi.IConfigurationManager;
import com.creditease.uav.feature.upgrade.action.AbstractUpgradeBaseAction;
import com.creditease.uav.feature.upgrade.common.UpgradeConstants;
import com.creditease.uav.feature.upgrade.common.UpgradeUtil;
import com.creditease.uav.feature.upgrade.record.UpgradeOperationRecord;

/**
 * 
 * UpgradeContext description: This class acts as a global context in upgrade process
 *
 */
public class UpgradeContext {

    protected ISystemLogger log = SystemLogger.getLogger(UpgradeContext.class);

    private UpgradeConfig upgradeConfig;

    // stands for current upgrade phase
    private volatile UpgradePhase currentPhase;

    // if current process is at rollback phase
    private volatile boolean isRollback;

    // This file lock is a upgrade process mutex lock. Only the upgrade process which get this file lock can run.
    private UpgradeProcessLock processLock;

    private volatile boolean alreadyGotFileLock;

    // Before upgrade, find the process list which will be affected
    private List<TargetProcess> affectedProcessList = null;

    // Use this variable to check if current upgrade process was started by cron task
    private boolean isStartByCronTask;

    public Map<String, AbstractUpgradeBaseAction> upgradeActionMap = new HashMap<String, AbstractUpgradeBaseAction>();
    public Map<String, AbstractUpgradeBaseAction> rollbackActionMap = new HashMap<String, AbstractUpgradeBaseAction>();

    public UpgradeContext(UpgradeConfig upgradeConfig) {
        this.upgradeConfig = upgradeConfig;
        this.currentPhase = UpgradePhase.START;
    }

    public UpgradeContext(UpgradeConfig upgradeConfig, boolean isStartByCronTask) {
        this(upgradeConfig);
        this.isStartByCronTask = isStartByCronTask;
    }

    public enum UpgradePhase {
        START, BACKUP, DOWNLOAD, PROCESS_STOP, OVERRIDE_FILE, PROCESS_START, END
    }

    private static Set<UpgradePhase> needRollbackUpgradePhaseSet = new HashSet<UpgradePhase>();
    private static Set<UpgradePhase> notNeedRollbackUpgradePhaseSet = new HashSet<UpgradePhase>();
    static {
        needRollbackUpgradePhaseSet.add(UpgradePhase.PROCESS_START);
        needRollbackUpgradePhaseSet.add(UpgradePhase.OVERRIDE_FILE);
        needRollbackUpgradePhaseSet.add(UpgradePhase.PROCESS_STOP);

        notNeedRollbackUpgradePhaseSet.add(UpgradePhase.START);
        notNeedRollbackUpgradePhaseSet.add(UpgradePhase.BACKUP);
        notNeedRollbackUpgradePhaseSet.add(UpgradePhase.DOWNLOAD);
        notNeedRollbackUpgradePhaseSet.add(UpgradePhase.END);

    }

    public boolean getFileLock() {

        Path path = Paths.get(ConfigurationManager.getInstance().getContext(IConfigurationManager.ROOT),
                UpgradeConstants.UPGRADE_FILE_LOCK_NAME);

        processLock = new UpgradeProcessLock(path);
        this.alreadyGotFileLock = processLock.getFileLock();

        return this.alreadyGotFileLock;
    }

    public void releaseFileLock() {

        if (processLock != null) {
            processLock.releaseFileLock();
        }
    }

    public boolean needRollback() {

        return needRollbackUpgradePhaseSet.contains(currentPhase);
    }

    public boolean needRollback(String phase) {

        boolean needRollback = false;
        for (UpgradePhase uPhase : needRollbackUpgradePhaseSet) {
            if (uPhase.toString().equals(phase)) {
                needRollback = true;
                break;
            }
        }

        return needRollback;
    }

    public boolean notNeedRollback() {

        return notNeedRollbackUpgradePhaseSet.contains(currentPhase);
    }

    public boolean isUAVContext() {

        return this.upgradeConfig.isUAV();
    }

    public String getThirdPartySoftwareCurrentVersion() {

        if (this.isUAVContext()) {
            return null;
        }

        Path path = Paths.get(this.getUpgradeConfig().getTargetDir(), UpgradeConstants.BACKUP_FOLDER,
                UpgradeConstants.UPGRADE_THIRDPARTY_VERSION_FILE_NAME);
        String version = "1.0";
        if (Files.exists(path)) {
            try {
                List<String> lines = Files.readAllLines(path, Charset.forName("utf-8"));
                if (lines != null && lines.size() > 0) {
                    version = lines.get(0);
                }
            }
            catch (IOException e) {
                if (log.isTraceEnable()) {
                    log.err(this, "Failed to read version from file " + path);
                }
            }
        }

        return version;
    }

    public boolean initProcessRecordFile() {

        if (isStartByCronTask) {
            return false;
        }

        Path processRecordPath = Paths.get(upgradeConfig.getTargetDir(), UpgradeConstants.UPGRADE_RECORD_FILE_NAME);
        if (Files.notExists(processRecordPath)) {
            try {
                Files.createFile(processRecordPath);
            }
            catch (IOException e) {
                return false;
            }
        }

        if (Files.isWritable(processRecordPath)) {
            try {
                Files.write(processRecordPath,
                        (upgradeConfig.toJSONString() + System.lineSeparator()).getBytes("utf-8"));
            }
            catch (IOException e) {
                return false;
            }
        }

        return true;
    }

    public void deleteProcessRecordFile() {

        IOHelper.deleteFile(
                (Paths.get(upgradeConfig.getTargetDir(), UpgradeConstants.UPGRADE_RECORD_FILE_NAME).toString()));
    }

    public void appendProcessRecord(UpgradeOperationRecord record) {

        Path processRecordPath = Paths.get(upgradeConfig.getTargetDir(), UpgradeConstants.UPGRADE_RECORD_FILE_NAME);
        if (Files.notExists(processRecordPath) || record == null) {
            return;
        }

        try {
            Files.write(processRecordPath, (record.toJSONString() + System.lineSeparator()).getBytes("utf-8"),
                    StandardOpenOption.APPEND);
        }
        catch (IOException e) {
            if (log.isTraceEnable()) {
                log.err(this, "Failed to append process record. " + e.getMessage());
            }
        }
    }

    public boolean canWriteOperationRecord() {

        return !isRollback && !isStartByCronTask;
    }

    public synchronized List<TargetProcess> getAffectedProcessList() {

        if (this.affectedProcessList != null) {
            return this.affectedProcessList;
        }

        this.affectedProcessList = UpgradeUtil.findCurrentUAVTargetProcess();
        return this.affectedProcessList;
    }

    // --------getter and setter method------

    public boolean alreadyGotFileLock() {

        return this.alreadyGotFileLock;
    }

    public void setAffectedProcessList(List<TargetProcess> affectedProcessList) {

        this.affectedProcessList = affectedProcessList;
    }

    public UpgradeConfig getUpgradeConfig() {

        return upgradeConfig;
    }

    public void setUpgradeConfig(UpgradeConfig upgradeConfig) {

        this.upgradeConfig = upgradeConfig;
    }

    public UpgradePhase getCurrentPhase() {

        return currentPhase;
    }

    public synchronized void setCurrentPhase(UpgradePhase currentPhase) {

        this.currentPhase = currentPhase;
    }

    public boolean isStartByCronTask() {

        return isStartByCronTask;
    }

    public boolean isRollback() {

        return isRollback;
    }

    public synchronized void setRollback(boolean isRollback) {

        if (log.isTraceEnable()) {
            log.info(this, "Failed to do upgrade, begin to run rollback program");
        }

        this.isRollback = isRollback;
    }

}
