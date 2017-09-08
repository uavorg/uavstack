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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.helpers.ThreadHelper;
import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.uav.feature.upgrade.beans.UpgradeContext;
import com.creditease.uav.feature.upgrade.beans.UpgradeContext.UpgradePhase;
import com.creditease.uav.feature.upgrade.common.UpgradeConstants;
import com.creditease.uav.feature.upgrade.exception.UpgradeException;

public class PackageDownloadAction extends AbstractUpgradeBaseAction {

    private String host;
    private int port;
    private int retryCount;
    private long retryAfter;

    public PackageDownloadAction(String feature, UpgradeContext upgradeContext, IActionEngine engine, String host,
            int port) {
        super(PackageDownloadAction.class.getSimpleName(), feature, upgradeContext, engine);
        this.host = host;
        this.port = port;
        this.retryAfter = DataConvertHelper
                .toLong(this.getConfigManager().getFeatureConfiguration(this.feature, "download.retry.after"), 60L);
    }

    @Override
    public void doAction(ActionContext context) throws Exception {

        setUpgradePhase(UpgradePhase.DOWNLOAD);
        File file = findTargetUpgradeZipLocally();
        if (file != null) {
            if (log.isTraceEnable()) {
                log.info(this, "Target upgrade package already exist in backup directory, no need to download");
            }

            Path path = Paths.get(this.getBackupDir(), "new");
            if (Files.notExists(path)) {
                Files.createDirectory(path);
            }

            if (log.isTraceEnable()) {
                log.info(this, "Copy backup package to backup new folder");
            }

            Files.copy(file.toPath(), this.getUpgradePackagePath());
            context.setSucessful(true);

            return;
        }

        tryDownloadFile(getSoftwarePackage(), getUpgradePackagePath());
        tryDownloadFile(getMd5FileName(), getUpgardePackageMD5FilePath());
        checkPackageIntegrity();
        writeProcessRecord(this.cName);
        context.setSucessful(true);

    }

    @Override
    public String getSuccessNextActionId() {

        String actionId = null;

        if (this.upgradeContext.isUAVContext()) {
            actionId = UpgradeConstants.getActionName(false, UpgradeConstants.STOP_UAV_PROCESS_ACTION_KEY);
        }
        else {
            actionId = UpgradeConstants.getActionName(true, UpgradeConstants.THIRDPARTY_SOFTWARE_OVERRIDE_ACTION_KEY);
        }

        return actionId;
    }

    @Override
    public String getFailureNextActionId() {

        return UpgradeConstants.getActionName(false, UpgradeConstants.END_ACTION_KEY);
    }

    @Override
    public String getExceptionNextActionId() {

        return getFailureNextActionId();
    }

    /**
     * Check if target upgrade zip already exist in backup directory
     * 
     */
    private File findTargetUpgradeZipLocally() {

        File result = null;
        List<File> fileList = IOHelper.getFiles(getBackupDir());
        for (File file : fileList) {
            if (file.getName().equals(this.getSoftwarePackage())) {
                result = file;
                break;
            }
        }

        return result;
    }

    private enum DownloadResult {
        SUCCESS, FAILURE, SERVER_BUSY
    }

    /**
     * Try to download file, if download server is handling too many requests, will retry to download file after a few
     * seconds again, the max retry count and retry-after time are set in the profile.
     * 
     */
    private void tryDownloadFile(String fileName, Path destination) throws Exception {

        if (log.isTraceEnable()) {
            log.info(this, "try to download file " + fileName + " to " + destination);
        }

        DownloadResult result = DownloadResult.FAILURE;
        this.retryCount = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "download.retry.count"), 20);

        while (retryCount-- > 0) {
            try {
                if (downloadFile(fileName, destination)) {
                    result = DownloadResult.SUCCESS;
                    if (log.isTraceEnable()) {
                        log.info(this, "Download file: " + fileName + " successfully");
                    }

                    break;
                }
            }
            catch (Exception ex) {
                if (ex instanceof HttpResponseException) {
                    HttpResponseException hre = (HttpResponseException) ex;
                    if (hre.getStatusCode() != UpgradeConstants.HTTP_CODE_TOO_MANY_REQUESTS) {
                        throw ex;
                    }

                    if (log.isTraceEnable()) {
                        log.info(this,
                                "Server now is handling too many download requests, so need one more retry after "
                                        + retryAfter + " seconds");
                    }

                    ThreadHelper.suspend(retryAfter * 1000);
                    result = DownloadResult.SERVER_BUSY;
                    continue;
                }
                else {
                    result = DownloadResult.FAILURE;
                    break;
                }
            }
        }

        if (result == DownloadResult.SERVER_BUSY) {
            if (log.isTraceEnable()) {
                log.err(this, "Reached the max value of retry count, so download task was failed");
            }

            throw new Exception("Server busy");
        }
        else if (result == DownloadResult.FAILURE) {
            if (log.isTraceEnable()) {
                log.err(this, "Failed to download file: " + fileName);
            }

            throw new Exception("Failed to download");
        }
    }

    /**
     * Download file from HTTP server(host:port)
     * 
     * @param fileName:
     *            the target file name
     * @param target:
     *            The destination where new file will be placed
     * @return
     * @throws Exception
     */
    private boolean downloadFile(String fileName, Path destination) throws Exception {

        final Path path = destination;

        if (log.isTraceEnable()) {
            log.info(this, "Downloading file: " + fileName);
        }

        if (path == null || StringHelper.isEmpty(fileName)) {
            throw new IllegalArgumentException("Illegal argument for downloading");
        }

        URI uri = new URIBuilder().setScheme("http").setHost(this.host).setPort(this.port).setPath("/uav/upgrade")
                .addParameter("softwareId", this.getSoftwareId()).addParameter("target", fileName).build();

        Files.deleteIfExists(path);
        if (Files.notExists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        boolean result = false;
        try (CloseableHttpClient httpClient = HttpClients.createDefault();) {
            HttpGet httpGet = new HttpGet(uri);
            result = httpClient.execute(httpGet, new ResponseHandler<Boolean>() {

                @Override
                public Boolean handleResponse(HttpResponse response) throws IOException {

                    HttpEntity entity = response.getEntity();
                    StatusLine statusLine = response.getStatusLine();

                    if (statusLine.getStatusCode() >= 300) {
                        throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
                    }

                    if (entity == null) {
                        throw new ClientProtocolException("Response contains no content");
                    }

                    if (entity != null) {
                        try (InputStream instream = entity.getContent();
                                FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.CREATE,
                                        StandardOpenOption.APPEND);) {

                            byte[] byteArr = new byte[8192];
                            ByteBuffer byteBuf = ByteBuffer.wrap(byteArr);
                            int len = -1;
                            while ((len = instream.read(byteArr)) != -1) {
                                byteBuf.position(len);
                                byteBuf.flip();
                                fileChannel.write(byteBuf);
                                byteBuf.clear();
                            }
                        }
                    }
                    return true;
                }
            });
        }

        return result;
    }

    /**
     * Check if md5 of current upgrade zip package is same as the value in the md5 file
     * 
     * @throws IOException
     * @throws UpgradeException
     */
    private void checkPackageIntegrity() throws IOException, UpgradeException {

        Path packagePath = this.getUpgradePackagePath();
        if (Files.notExists(packagePath)) {
            if (this.log.isTraceEnable()) {
                this.log.warn(this, "Package " + this.getSoftwarePackage() + " does not exist");
            }

            throw new UpgradeException("Upgrade package does not exist");
        }

        Path md5Path = this.getUpgardePackageMD5FilePath();
        if (Files.notExists(md5Path)) {
            if (this.log.isTraceEnable()) {
                this.log.warn(this, "MD5 file for " + this.getSoftwarePackage() + " does not exist");
            }

            throw new UpgradeException("MD5 file does not exist");
        }

        String originalMD5 = new String(Files.readAllBytes(md5Path));
        String newMD5 = generateMD5ForFile(packagePath.toFile());

        if (this.log.isTraceEnable()) {
            this.log.info(this, "original md5 is " + originalMD5 + " new md5 is " + newMD5);
        }

        if (!originalMD5.trim().equals(newMD5)) {
            throw new UpgradeException("MD5 dismatched");
        }

        if (this.log.isTraceEnable()) {
            this.log.info(this, "Upgrade package integrity check was successful");
        }
    }

    /**
     * Generate the md5 for target file
     * 
     * @param file
     * @return md5 value
     * @throws IOException
     */
    private String generateMD5ForFile(File file) throws IOException {

        try (FileInputStream fis = new FileInputStream(file);) {
            return DigestUtils.md5Hex(fis);
        }
    }

    /**
     * Get the md5 file path associated with upgrade package
     * 
     * @return
     */
    private Path getUpgardePackageMD5FilePath() {

        return Paths.get(this.getBackupDir(), "new", this.getSoftwarePackage() + ".MD5");
    }

    private String getMd5FileName() {

        return this.getSoftwarePackage() + ".MD5";
    }

}
