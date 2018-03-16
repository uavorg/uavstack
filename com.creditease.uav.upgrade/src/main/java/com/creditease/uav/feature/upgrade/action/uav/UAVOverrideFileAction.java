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

package com.creditease.uav.feature.upgrade.action.uav;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.JVMToolHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.helpers.ZIPHelper;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.agent.spi.IConfigurationManager;
import com.creditease.uav.feature.upgrade.action.OverrideFileAction;
import com.creditease.uav.feature.upgrade.beans.TargetProcess;
import com.creditease.uav.feature.upgrade.beans.UpgradeContext;
import com.creditease.uav.feature.upgrade.common.UpgradeConstants;
import com.creditease.uav.feature.upgrade.common.UpgradeUtil;
import com.creditease.uav.feature.upgrade.exception.UpgradeException;

public class UAVOverrideFileAction extends OverrideFileAction {

    public UAVOverrideFileAction(String feature, UpgradeContext upgradeContext, IActionEngine engine) {
        super(UAVOverrideFileAction.class.getSimpleName(), feature, upgradeContext, engine);
    }

    @Override
    public String getSuccessNextActionId() {

        return UpgradeConstants.getActionName(false, UpgradeConstants.START_UAV_PROCESS_ACTION_KEY);
    }

    @Override
    public String getFailureNextActionId() {

        if (this.upgradeContext.isRollback()) {
            return UpgradeConstants.getActionName(false, UpgradeConstants.END_ACTION_KEY);
        }

        this.upgradeContext.setRollback(true);
        return UpgradeConstants.getActionName(false, UpgradeConstants.UAV_OVERRIDE_FILE_ACTION_KEY);
    }

    @Override
    public String getExceptionNextActionId() {

        return getFailureNextActionId();
    }

    @Override
    protected void overrideFiles(Path source) throws Exception {

        if (log.isTraceEnable()) {
            log.info(this, "start overriding files from " + source + " to " + this.getParentDirOfRoot());
        }

        if (Files.notExists(source)) {
            throw new UpgradeException("The source of override file action does not exit");
        }
        
        // 解压前清除原目录文件
        String regex = "(" + UpgradeConstants.BACKUP_FOLDER
                        + "|" + UpgradeConstants.UPGRADE_FILE_LOCK_NAME
                        + "|" + UpgradeConstants.UPGRADE_RECORD_FILE_NAME
                        +")";

        File rootDir = new File(this.getRootDir());
        String[] files = rootDir.list();
        if (files != null && files.length > 0) {
            if (log.isTraceEnable()) {
                log.info(this, "Delete all the old files in the root folder: " + this.getRootDir());
            }

            for (int i = 0; i < files.length; i++) {
                File file = new File(this.getRootDir() + "/" + files[i]);
                if (!file.getName().matches(regex)) {
                    if (file.isDirectory()) {
                        IOHelper.deleteFolder(file.getAbsolutePath());
                    }
                    else {
                        IOHelper.deleteFile(file.getAbsolutePath());
                    }
                }
            }
        }

        ZIPHelper.decompressZipToDir(source.toString(), this.getParentDirOfRoot());

        if (this.upgradeContext.canWriteOperationRecord()) {
            addAppVersionToProfile();
            modifyFilePermission();
            this.writeProcessRecord(this.cName, source.toString());
        }

    }

    /**
     * Append upgrade target version to profile configuration file
     */
    private void addAppVersionToProfile() {

        String version = UpgradeUtil.getVersionFromPackageName(this.getSoftwarePackage());
        if (!StringHelper.isEmpty(version)) {
            for (TargetProcess process : this.upgradeContext.getAffectedProcessList()) {
                Path profilePath = Paths.get(this.getConfigManager().getContext(IConfigurationManager.CONFIGPATH),
                        process.getProfileName() + ".properties");

                if (log.isTraceEnable()) {
                    log.info(this, "Start adding new app version " + version + " to " + profilePath);
                }

                try {
                    List<String> lines = Files.readAllLines(profilePath, Charset.forName("UTF-8"));
                    StringBuffer sbf = new StringBuffer();
                    for (String line : lines) {
                        if (line.contains("meta.nodeappversion")) {
                            // ignore the old meta.nodeappversion
                            continue;
                        }

                        sbf.append(line).append(System.getProperty("line.separator"));

                        if (line.contains("meta.nodetype")) {
                            // append meta.nodeappversion after the line of meta.nodetype
                            sbf.append("meta.nodeappversion=" + version).append(System.getProperty("line.separator"));
                        }
                    }
                    Files.write(profilePath, sbf.toString().getBytes(Charset.forName("UTF-8")));
                }
                catch (IOException e) {
                    if (log.isTraceEnable()) {
                        log.err(this, "Failed to app version info to profile " + e.getMessage());
                    }
                }
            }

        }
        else {
            if (log.isTraceEnable()) {
                log.warn(this, "Did not get valid version from " + this.getSoftwarePackage());
            }
        }

    }

    /**
     * Modify file permission
     * 
     * @throws Exception
     */
    private void modifyFilePermission() throws Exception {

        if (log.isTraceEnable()) {
            log.info(this, "Start modifying file permission under directory: " + this.getBinPath());
        }

        String[] files = IOHelper.getFiles_STR(this.getBinPath().toString());
        if (files == null || files.length == 0) {
            if (log.isTraceEnable()) {
                log.info(this, "There is no file to modify privilage");
                return;
            }
        }

        for (String file : files) {
            if (JVMToolHelper.isWindows()) {
                if (file.endsWith(".bat") || file.endsWith(".vbs")) {
                    UpgradeUtil.changeFilePermisson4Win(file);
                }
            }
            else {
                if (file.endsWith(".sh")) {
                    UpgradeUtil.changeFilePermission4Linux(file);
                }
            }
        }

    }

}
