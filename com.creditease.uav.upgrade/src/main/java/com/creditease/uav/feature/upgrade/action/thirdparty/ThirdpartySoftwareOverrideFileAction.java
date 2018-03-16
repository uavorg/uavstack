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

package com.creditease.uav.feature.upgrade.action.thirdparty;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.ZIPHelper;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.uav.feature.upgrade.action.OverrideFileAction;
import com.creditease.uav.feature.upgrade.beans.UpgradeContext;
import com.creditease.uav.feature.upgrade.common.UpgradeConstants;
import com.creditease.uav.feature.upgrade.common.UpgradeUtil;
import com.creditease.uav.feature.upgrade.exception.UpgradeException;

public class ThirdpartySoftwareOverrideFileAction extends OverrideFileAction {

    public ThirdpartySoftwareOverrideFileAction(String feature, UpgradeContext upgradeContext, IActionEngine engine) {
        super(ThirdpartySoftwareOverrideFileAction.class.getSimpleName(), feature, upgradeContext, engine);
    }

    @Override
    public String getSuccessNextActionId() {

        return UpgradeConstants.getActionName(true, UpgradeConstants.END_ACTION_KEY);
    }

    @Override
    public String getFailureNextActionId() {

        if (this.upgradeContext.isRollback()) {
            return UpgradeConstants.getActionName(true, UpgradeConstants.END_ACTION_KEY);
        }

        this.upgradeContext.setRollback(true);
        return UpgradeConstants.getActionName(true, UpgradeConstants.THIRDPARTY_SOFTWARE_OVERRIDE_ACTION_KEY);
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
            this.writeProcessRecord(this.cName, source.toString());
        }

        this.addAppVersionToCurrentVersionFile(UpgradeUtil.getVersionFromPackageName(source.getFileName().toString()));
    }

    private void addAppVersionToCurrentVersionFile(String version) {

        Path path = Paths.get(this.getBackupDir(), UpgradeConstants.UPGRADE_THIRDPARTY_VERSION_FILE_NAME);
        try {
            Files.write(path, version.getBytes("utf-8"));
        }
        catch (IOException e) {
            if (log.isTraceEnable()) {
                log.warn(this, "Failed to add app version to " + path);
            }
        }
    }
}
