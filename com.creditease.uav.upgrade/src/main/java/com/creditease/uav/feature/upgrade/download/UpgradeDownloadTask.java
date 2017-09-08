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

import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.creditease.agent.spi.Abstract1NTask;
import com.creditease.agent.spi.HttpMessage;
import com.creditease.uav.feature.upgrade.common.UpgradeConstants;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * 
 * UpgradeDownloadTask description: 1n queue task for upgrade downloading
 *
 */
public class UpgradeDownloadTask extends Abstract1NTask {

    private HttpMessage httpMsg;

    public UpgradeDownloadTask(String name, String feature, HttpMessage httpMsg) {
        super(name, feature);
        this.httpMsg = httpMsg;
    }

    @Override
    public void run() {

        String baseDir = this.getConfigManager().getFeatureConfiguration(feature, "download.dir");
        String target = httpMsg.getParameter(UpgradeConstants.DOWNLOAD_REQUEST_PARAM_KEY);
        String software = httpMsg.getParameter(UpgradeConstants.DOWNLOAD_TARGET_SOFTWARE);

        handleDownloadRequest(baseDir, software, target);
    }

    /**
     * Handle the request for downloading upgrade file
     * 
     * @param baseDir:
     *            the base directory
     * @param software:
     *            work as the sub directory for storing target zip file
     * @param target:
     *            the name of target zip file
     */
    private void handleDownloadRequest(String baseDir, String software, String target) {

        Path path = Paths.get(baseDir, software, target);

        if (log.isTraceEnable()) {
            log.info(this, "Downloading " + target);
        }

        File file = path.toFile();

        if (!file.exists() || file.isHidden()) {
            sendRspStatus(NOT_FOUND);
            return;
        }

        if (file.isDirectory()) {
            sendListing(file);
            return;
        }

        if (!file.isFile()) {
            sendRspStatus(FORBIDDEN);
            return;
        }

        httpMsg.putResponseBodyInChunkedFile(file);
    }

    /**
     * This is a backdoor to show file list in browser
     * 
     * @param dir
     */
    private void sendListing(File dir) {

        StringBuilder buf = new StringBuilder().append("<!DOCTYPE html>\r\n")
                .append("<html><head><meta charset='utf-8' /></head><body>\r\n").append("").append("<ul>");

        for (File f : dir.listFiles()) {
            if (f.isHidden() || !f.canRead()) {
                continue;
            }

            String name = f.getName();
            buf.append("<li>").append(name).append("</li>\r\n");
        }

        buf.append("</ul></body></html>\r\n");

        httpMsg.putResponseBodyInString(buf.toString(), 200, "UTF-8");
    }

    private void sendRspStatus(HttpResponseStatus status) {

        httpMsg.putResponseBodyInString(status.reasonPhrase(), status.code(), "UTF-8");
    }

}
