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

package com.creditease.uav.feature.upgrade.download;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.spi.Abstract1NTask;
import com.creditease.agent.spi.AbstractHttpHandler;
import com.creditease.agent.spi.HttpMessage;
import com.creditease.agent.spi.I1NQueueWorker;
import com.creditease.uav.feature.upgrade.common.UpgradeConstants;

public class UpgradeDownloadRequestHandler extends AbstractHttpHandler<UpgradeHttpMessage> {

    public UpgradeDownloadRequestHandler(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public String getContextPath() {

        return "/uav/upgrade";
    }

    @Override
    public void handle(UpgradeHttpMessage message) {

        HttpMessage httpMsg = (HttpMessage) message.getObjectParam("message");

        // Package downloading is time-consuming task, so use 1+n queue to execute it.
        I1NQueueWorker queueWorker = this.get1NQueueWorkerMgr().getQueueWorker(this.feature,
                UpgradeConstants.UPGRADE_1N_QUEUE_NAME);
        if (queueWorker == null) {
            if (log.isTraceEnable()) {
                log.warn(this, "Download 1N queue does not exit, so ignore this download request");
            }

            httpMsg.putResponseBodyInString("Internal error: 1N queue worker doese not exist", 500, "UTF-8");
            return;
        }

        String baseDir = this.getConfigManager().getFeatureConfiguration(feature, "download.dir");
        String target = httpMsg.getParameter(UpgradeConstants.DOWNLOAD_REQUEST_PARAM_KEY);
        String software = httpMsg.getParameter(UpgradeConstants.DOWNLOAD_TARGET_SOFTWARE);

        if ("list".equalsIgnoreCase(target)) { // get the list of upgrade package
            handleListFileRequest(baseDir, software, httpMsg);
            return;
        }
        else if ("listdir".equalsIgnoreCase(target)) {
            handleListDirRequest(baseDir, httpMsg);
            return;
        }

        int downloadThreshold = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "download.threshold"), 10);

        synchronized (this) {
            if (log.isTraceEnable()) {
                log.info(this, "Current download 1+N worker has " + queueWorker.getActiveCount() + " working tasks");
            }

            if (queueWorker.getActiveCount() >= downloadThreshold) {
                httpMsg.putResponseBodyInString("Too Many Requests", UpgradeConstants.HTTP_CODE_TOO_MANY_REQUESTS,
                        "UTF-8");
                return;
            }

            Abstract1NTask task = new UpgradeDownloadTask(cName, feature, httpMsg);
            queueWorker.put(task);
        }
    }

    /**
     * Handle the request for getting all softwares
     * 
     * @param baseDir:
     *            base directory of upgrade packages
     */
    private void handleListDirRequest(String baseDir, HttpMessage httpMsg) {

        Path path = Paths.get(baseDir);
        List<File> dirs = IOHelper.getDirs(path.toString());

        Set<String> dirNames = new HashSet<String>();
        for (File dir : dirs) {
            dirNames.add(dir.getName());
        }

        Map<String, Set<String>> result = new HashMap<String, Set<String>>();
        result.put("dirs", dirNames);

        httpMsg.putResponseBodyInString(JSONHelper.toString(result), 200, "UTF-8");
    }

    /**
     * Handle the request for getting all upgrade packages
     * 
     * @param baseDir:
     *            base directory of upgrade packages
     * 
     * @param software:
     *            sub directory for software
     */
    private void handleListFileRequest(String baseDir, String software, HttpMessage httpMsg) {

        Path path = Paths.get(baseDir, software);
        String[] fileNames = IOHelper.getFileNames_STR(path.toString());

        Set<String> fileSet = new HashSet<String>();
        if (fileNames != null) {
            for (String fileName : fileNames) {
                if (fileName.endsWith("zip")) {
                    fileSet.add(fileName);
                }
            }
        }

        Map<String, Set<String>> result = new HashMap<String, Set<String>>();
        result.put("files", fileSet);

        httpMsg.putResponseBodyInString(JSONHelper.toString(result), 200, "UTF-8");
    }

}
