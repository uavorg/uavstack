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

package com.creditease.uav.feature.upgrade.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.creditease.uav.feature.upgrade.action.BackupAction;
import com.creditease.uav.feature.upgrade.action.EndAction;
import com.creditease.uav.feature.upgrade.action.PackageDownloadAction;
import com.creditease.uav.feature.upgrade.action.thirdparty.ThirdpartySoftwareOverrideFileAction;
import com.creditease.uav.feature.upgrade.action.uav.StartUAVProcessAction;
import com.creditease.uav.feature.upgrade.action.uav.StopUAVProcessAction;
import com.creditease.uav.feature.upgrade.action.uav.UAVOverrideFileAction;

public class UpgradeConstants {

    public static final String BACKUP_FOLDER = "backup";
    public static final String UPGRADE_INFO = "JAppUpgradeInfo";
    public static final String UPGRADE_FILE_LOCK_NAME = "upgrade.lock";
    public static final String UPGRADE_RECORD_FILE_NAME = "upgrade.record";
    public static final String UPGRADE_THIRDPARTY_VERSION_FILE_NAME = "current_version.txt";
    public static final String UPGRADE_PACKAGE_NAME_FORMAT = "\\w[\\w-_]+_(\\d+.\\d+(.\\d+)?(_\\d+)?).zip";

    public static final String DOWNLOAD_REQUEST_PARAM_KEY = "target";
    public static final String DOWNLOAD_TARGET_SOFTWARE = "softwareId";
    public static final int DEFAULT_HTTP_PORT = 8888;
    public static final long DEFAULT_CMD_EXEC_TIME = 5000L;
    public static final String DEFAULT_SH_FILE_PERMISSION = "751";
    public static final int DEFAULT_BACKUP_COUNT = 3;

    public static final int HTTP_CODE_TOO_MANY_REQUESTS = 429;

    public static final String UPGRADE_1N_QUEUE_NAME = "Upgrade_Download_Server_1N_Queue_Worker";

    public static final String UAV_BOOTSTRAP_CLASS = "com.creditease.mscp.boot.MSCPBoot";

    private static Map<Integer, String> UAVActionMap = new HashMap<Integer, String>();
    private static Map<Integer, String> ThirdPartySoftwareActionMap = new HashMap<Integer, String>();

    public static final int BACKUP_ACTION_KEY = 1;
    public static final int DOWNLOAD_ACTION_KEY = 2;

    public static final int STOP_UAV_PROCESS_ACTION_KEY = 3;
    public static final int UAV_OVERRIDE_FILE_ACTION_KEY = 4;
    public static final int START_UAV_PROCESS_ACTION_KEY = 5;

    public static final int THIRDPARTY_SOFTWARE_OVERRIDE_ACTION_KEY = 6;

    public static final int END_ACTION_KEY = 7;

    static {
        UAVActionMap.put(BACKUP_ACTION_KEY, BackupAction.class.getSimpleName());
        UAVActionMap.put(DOWNLOAD_ACTION_KEY, PackageDownloadAction.class.getSimpleName());
        UAVActionMap.put(STOP_UAV_PROCESS_ACTION_KEY, StopUAVProcessAction.class.getSimpleName());
        UAVActionMap.put(UAV_OVERRIDE_FILE_ACTION_KEY, UAVOverrideFileAction.class.getSimpleName());
        UAVActionMap.put(START_UAV_PROCESS_ACTION_KEY, StartUAVProcessAction.class.getSimpleName());
        UAVActionMap.put(END_ACTION_KEY, EndAction.class.getSimpleName());
    }

    static {
        ThirdPartySoftwareActionMap.put(BACKUP_ACTION_KEY, BackupAction.class.getSimpleName());
        ThirdPartySoftwareActionMap.put(DOWNLOAD_ACTION_KEY, PackageDownloadAction.class.getSimpleName());
        ThirdPartySoftwareActionMap.put(THIRDPARTY_SOFTWARE_OVERRIDE_ACTION_KEY,
                ThirdpartySoftwareOverrideFileAction.class.getSimpleName());
        ThirdPartySoftwareActionMap.put(END_ACTION_KEY, EndAction.class.getSimpleName());
    }

    public static String getActionName(boolean is4ThirdParty, Integer key) {

        if (is4ThirdParty) {
            return ThirdPartySoftwareActionMap.get(key);
        }
        else {
            return UAVActionMap.get(key);
        }
    }

    public static List<String> getUAVDefaultBackupSourceDir() {

        List<String> sourceDirs = new ArrayList<String>();
        sourceDirs.add("bin");
        sourceDirs.add("lib");
        sourceDirs.add("config");
        return Collections.unmodifiableList(sourceDirs);
    }
}
