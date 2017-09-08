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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.spi.IConfigurationManager;
import com.creditease.uav.feature.upgrade.common.UpgradeConstants;

/**
 * 
 * A bean for storing parameters of upgrade request
 * 
 * format is as below,
 * {"uav":1,"softwareId":"uavagent","softwarePackage":"uavagent_1.0_20161220140510.zip","targetDir":"/app/uav/uavagent",
 * "includeBackups":["bin","lib","config","test.txt"]}
 * 
 */
public class UpgradeConfig extends JSONObject {

    private static final long serialVersionUID = 1L;

    // this attribute means if this upgrade is for uav feature or third party software.
    private boolean isUAV;

    // this id is also used as the directory name in upgrade download center
    private String softwareId;

    // the upgrade software package
    private String softwarePackage;

    // The absolute path of upgrade target
    private String targetDir;

    // The dirs or files to be backup
    private List<String> backupFilesList;

    @SuppressWarnings("unchecked")
    public UpgradeConfig(Map<String, Object> paramMap) {
        super(paramMap);
        this.isUAV = this.getBooleanValue("uav");
        this.softwareId = this.getString("softwareId");
        this.softwarePackage = this.getString("softwarePackage");
        this.targetDir = this.getString("targetDir");
        if (StringHelper.isEmpty(this.targetDir) && this.isUAV) {
            this.targetDir = ConfigurationManager.getInstance().getContext(IConfigurationManager.ROOT);
            this.put("targetDir", this.targetDir);
        }

        this.backupFilesList = (List<String>) this.get("includeBackups");
        if (this.backupFilesList == null || this.backupFilesList.size() == 0) {
            this.backupFilesList = new ArrayList<String>();
            if (this.isUAV) {
                this.backupFilesList = UpgradeConstants.getUAVDefaultBackupSourceDir();
            }
            else {
                // For third party software, by default, backup all the files under rootDir
                if (Files.exists(Paths.get(this.targetDir))) {
                    File[] files = new File(this.targetDir).listFiles();
                    for (File file : files) {
                        if (UpgradeConstants.BACKUP_FOLDER.equals(file.getName())) {
                            continue;
                        }

                        this.backupFilesList.add(file.getName());
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public UpgradeConfig(String jsonString) {
        this(JSONHelper.toObject(jsonString, Map.class));
    }

    public boolean isUAV() {

        return this.isUAV;
    }

    public String getSoftwareId() {

        return softwareId;
    }

    public String getSoftwarePackage() {

        return softwarePackage;
    }

    public String getTargetDir() {

        return targetDir;
    }

    public List<String> getBackupFilesList() {

        return backupFilesList;
    }

    public void setBackupFilesList(List<String> backupFilesList) {

        this.backupFilesList = backupFilesList;
    }

    @Override
    public String toString() {

        return this.toJSONString();
    }

    public static void main(String[] args) {

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("uav", 1);
        paramMap.put("softwareId", "uavagent");
        paramMap.put("softwarePackage", "uavagent_1.0_20161220140510.zip");
        paramMap.put("targetDir", "/app/uav/uavagent");

        List<String> backupFiles = new ArrayList<String>();
        backupFiles.add("config");
        backupFiles.add("test.txt");
        paramMap.put("includeBackups", backupFiles);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = JSONHelper.toObject(JSONHelper.toString(paramMap), Map.class);

        UpgradeConfig config = new UpgradeConfig(map);
        config.toJSONString();
        // System.out.println(config.toJSONString());
    }
}
