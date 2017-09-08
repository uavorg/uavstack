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

package com.creditease.uav.profiling.handlers;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.creditease.agent.helpers.IOHelper;
import com.creditease.monitor.appfra.hook.spi.HookConstants;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.uav.common.BaseComponent;
import com.creditease.uav.profiling.handlers.log.LogProfileInfo;
import com.creditease.uav.profiling.spi.ProfileConstants;
import com.creditease.uav.profiling.spi.ProfileContext;
import com.creditease.uav.profiling.spi.ProfileElement;
import com.creditease.uav.profiling.spi.ProfileElementInstance;
import com.creditease.uav.profiling.spi.ProfileHandler;

public class LogProfileHandler extends BaseComponent implements ProfileHandler {

    @Override
    public void doProfiling(ProfileElement elem, ProfileContext context) {

        if (!ProfileConstants.PROELEM_LOGS.equals(elem.getElemId())) {
            return;
        }

        InterceptContext ic = context.get(InterceptContext.class);

        if (ic == null) {
            this.logger.warn("Profile:LOGS FAILs as No InterceptContext available", null);
            return;
        }

        ClassLoader webappclsLoader = (ClassLoader) ic.get(InterceptConstants.WEBAPPLOADER);

        if (null == webappclsLoader) {
            this.logger.warn("Profile:LOGS FAILs as No webappclsLoader available", null);
            return;
        }

        String appid = (String) ic.get(InterceptConstants.CONTEXTPATH);

        if (appid == null) {
            this.logger.warn("Profile:LOGS FAILs as No appid get", null);
            return;
        }

        // Log Source 1: LogProfileInfo
        profileFromLogProfileInfo(elem, ic, appid);

        // Log Source 2: JAppLogs
        profileFromJAppLogs(elem);
    }

    /**
     * profileFromJAppLogs
     * 
     * @param elem
     */
    private void profileFromJAppLogs(ProfileElement elem) {

        String JAppLogs = System.getProperty("JAppLogs");

        if (JAppLogs == null) {
            return;
        }

        /**
         * JAppLogs: goes to log4j
         */
        ProfileElementInstance pei = elem.getInstance(LogProfileInfo.LogType.Log4j.toString());

        String[] logFilesStr = JAppLogs.split(";");

        for (String logFileStr : logFilesStr) {

            int index = logFileStr.indexOf("*");

            // normal file path
            if (index == -1) {
                pei.setValue(logFileStr, Collections.emptyMap());
            }
            // file patterns under folder
            else {
                String folder = logFileStr.substring(0, index);

                if (!IOHelper.exists(folder)) {
                    continue;
                }

                List<File> files = IOHelper.getFiles(folder);

                for (File file : files) {
                    pei.setValue(file.getAbsolutePath(), Collections.emptyMap());
                }
            }
        }
    }

    /**
     * profileFromLogProfileInfo
     * 
     * @param elem
     * @param ic
     * @param appid
     */
    private void profileFromLogProfileInfo(ProfileElement elem, InterceptContext ic, String appid) {

        @SuppressWarnings("unchecked")
        LinkedList<LogProfileInfo> list = (LinkedList<LogProfileInfo>) ic.get(HookConstants.LOG_PROFILE_LIST);

        if (list == null || list.size() == 0) {
            this.logger.warn("Profile:LOGS FAILs as LogProfileInfo List is null or its size 0", null);
            return;
        }

        // Log Source 1: AUTO Profiling
        for (LogProfileInfo logProfileInfo : list) {

            if (appid.equals(logProfileInfo.getAppId())) {

                ProfileElementInstance pei = elem.getInstance(logProfileInfo.getLogType().toString());

                String logPath = logProfileInfo.getFilePath();
                // set the log file path
                if (null != logPath) {
                    pei.setValue(logPath, logProfileInfo.getAttributes());
                }
            }
        }
    }

}
