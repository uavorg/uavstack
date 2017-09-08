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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.JVMToolHelper;
import com.creditease.agent.helpers.RuntimeHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.agent.spi.IConfigurationManager;
import com.creditease.uav.feature.upgrade.beans.TargetProcess;

public class UpgradeUtil {

    private final static ISystemLogger log = SystemLogger.getLogger(UpgradeUtil.class);

    /**
     * Get the version of target package, e.g., uavagent_1.0_20170103115755.zip -> 1.0_20170103115755
     * 
     * @return version string
     */
    public static String getVersionFromPackageName(String packageName) {

        String version = null;
        Pattern p = Pattern.compile(UpgradeConstants.UPGRADE_PACKAGE_NAME_FORMAT);
        Matcher m = p.matcher(packageName);

        if (m.matches()) {
            version = m.group(1);
        }

        return version;
    }

    /**
     * Find all of current UAV processes associated with this upgrade process. 1). Read all profile file names from
     * config directory 2). Fetch all the jvm processes in memory using JVMToolHelper 3). Filter all the jvm proceses to
     * generate a list of Process
     * 
     * @return a list of UAV process
     */
    public static List<TargetProcess> findCurrentUAVTargetProcess() {

        List<TargetProcess> procList = new ArrayList<TargetProcess>();
        String[] profileNames = IOHelper
                .getFileNames_STR(ConfigurationManager.getInstance().getContext(IConfigurationManager.CONFIGPATH));

        if (profileNames == null || profileNames.length == 0) {
            if (log.isTraceEnable()) {
                log.warn(UpgradeUtil.class, "No profile exists");
            }

            return procList;
        }

        Set<String> profileNameSet = new HashSet<String>();

        for (String profileName : profileNames) {
            if (profileName.endsWith("properties")) {
                profileNameSet.add(profileName.substring(0, profileName.lastIndexOf(".")));
            }
        }

        if (profileNameSet.size() == 0) {
            if (log.isTraceEnable()) {
                log.warn(UpgradeUtil.class, "No valid profile exists");
            }

            return procList;
        }

        List<Map<String, String>> processInfoList = JVMToolHelper.getAllJVMProcesses("localhost");
        String scriptExt = getScriptExtension();
        for (Map<String, String> processInfo : processInfoList) {
            if (!UpgradeConstants.UAV_BOOTSTRAP_CLASS.equals(processInfo.get("main"))) {
                continue;
            }

            String margs = processInfo.get("margs");
            if (StringHelper.isEmpty(margs) || !margs.contains("-p")) {
                continue;
            }

            String profileName = margs.split(" ")[1];
            if (StringHelper.isEmpty(profileName) || !profileNameSet.contains(profileName)) {
                continue;
            }

            String pid = processInfo.get("pid");
            procList.add(TargetProcess.buildProcess(pid, profileName, scriptExt));
        }

        if (log.isTraceEnable()) {
            log.info(UpgradeUtil.class, "Found " + procList.size()
                    + " running UAV process(es) which was(were) related with current upgrade action");
        }

        return procList;
    }

    /**
     * Construct command for uav start script or stop script
     * 
     * @param script
     * @return command string, e.g., "cd /app/uav/uavagent/bin && sh start_ma_test.sh"
     */
    public static String constructScriptCmd(String dirPath, String script) {

        StringBuilder sbd = new StringBuilder("cd ");
        sbd.append(dirPath);
        sbd.append(" && ");
        if (JVMToolHelper.isWindows()) {
            sbd.append("wscript ");
        }
        else {
            sbd.append("sh ");
        }

        sbd.append(script);

        return sbd.toString();
    }

    /**
     * Check if the uav process is alive
     * 
     * @param process
     * @return true or false, true means alive.
     * @throws Exception
     */
    public static boolean isUAVProcessAlive(TargetProcess process) throws Exception {

        List<Map<String, String>> processInfoList = JVMToolHelper.getAllJVMProcesses("localhost");

        boolean isAlive = false;
        for (Map<String, String> map : processInfoList) {

            if (!UpgradeConstants.UAV_BOOTSTRAP_CLASS.equals(map.get("main"))) {
                continue;
            }

            // Verify the uav process using profile name
            String margs = map.get("margs");
            if (!StringHelper.isEmpty(margs) && margs.contains(process.getProfileName())) {
                isAlive = true;
                break;
            }

        }

        return isAlive;
    }

    /**
     * Change the permission of the file to --Readable, Writable, Executable--
     * 
     * @param fileName
     */
    public static void changeFilePermisson4Win(String fileName) {

        File file = new File(fileName);
        if (!file.exists()) {
            return;
        }

        if (!file.canRead()) {
            file.setReadable(true);
        }

        if (!file.canWrite()) {
            file.setWritable(true);
        }

        if (!file.canExecute()) {
            file.setExecutable(true);
        }
    }

    /**
     * Change the permission of the file to "751"
     * 
     * @param fileName
     * @throws Exception
     */
    public static void changeFilePermission4Linux(String fileName) throws Exception {

        File file = new File(fileName);
        if (!file.exists()) {
            return;
        }

        RuntimeHelper.exec(UpgradeConstants.DEFAULT_CMD_EXEC_TIME,
                new String[] { "chmod", UpgradeConstants.DEFAULT_SH_FILE_PERMISSION, fileName });
    }

    /**
     * Get the extension for script according to current operating system
     * 
     * @return extension name
     */
    public static String getScriptExtension() {

        return JVMToolHelper.isWindows() ? ".vbs" : ".sh";
    }

    public static List<String> getUAVProcessJsonStrList(List<TargetProcess> processList) {

        List<String> jsonStrList = new ArrayList<String>();

        if (processList == null) {
            return jsonStrList;
        }

        for (TargetProcess process : processList) {
            jsonStrList.add(JSONHelper.toString(process));
        }

        return jsonStrList;
    }

    public static List<TargetProcess> generateUAVProcessListFromJsonStrList(List<String> jsonStrList) {

        List<TargetProcess> processList = new ArrayList<TargetProcess>();

        if (jsonStrList == null) {
            return processList;
        }

        for (String jsonStr : jsonStrList) {
            processList.add(JSONHelper.toObject(jsonStr, TargetProcess.class));
        }

        return processList;
    }
}
