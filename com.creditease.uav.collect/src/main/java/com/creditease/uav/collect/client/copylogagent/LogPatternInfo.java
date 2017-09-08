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

package com.creditease.uav.collect.client.copylogagent;

import java.io.File;
import java.util.regex.Pattern;

public class LogPatternInfo {

    // 0:exist-unkown,1:newcome,2:exist,3:update,4:issue
    public enum StateFlag {

        NEWCOME(0), EXIST_UNKOWN(1), EXIST(2), UPDATE(3);

        private final int flag;

        private StateFlag(int flag) {
            this.flag = flag;
        }

        public int getFlagWeight() {

            return flag;
        }
    }

    public LogPatternInfo() {
    }

    public LogPatternInfo(String servId, String appId, String absPath) {
        this.servId = servId;
        this.appId = appId;
        this.setAbsolutePath(absPath);
    }

    private String servId;

    private String appId;

    private String logParttern;

    private String absolutePath;

    private Pattern logRegxPattern;

    private String webAppRoot;

    private File parentDir;

    private StateFlag flag;

    private long timeStamp;
    
    private boolean unsplit;

    public long getTimeStamp() {

        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {

        this.timeStamp = timeStamp;
    }

    public StateFlag getFlag() {

        return flag;
    }

    public void setFlag(StateFlag flag) {

        this.flag = flag;
    }

    public String getServId() {

        return servId;
    }

    public void setServId(String servId) {

        this.servId = servId;
    }

    public String getAppId() {

        return appId;
    }

    public void setAppId(String appId) {

        this.appId = appId;
    }

    public void setLogParttern(String logParttern) {

        this.logParttern = logParttern;
    }

    public String getLogParttern() {

        return logParttern;
    }

    public String getAbsolutePath() {

        return absolutePath;
    }

    public void setAbsolutePath(String absPath) {

        if (null == absPath) {
            throw new NullPointerException("Absolute path should not be NULL");
        }

        File f = new File(absPath);

        this.parentDir = f.getParentFile();
        this.logRegxPattern = Pattern.compile(f.getName());
        this.absolutePath = absPath;
    }

    public String getUUID() {

        return this.servId + "-" + this.appId + "-" + this.logParttern;
    }

    public String getAppUUID() {

        return this.servId + "-" + this.appId;
    }

    public Pattern getLogRegxPattern() {

        return this.logRegxPattern;
    }

    public File getParentDir() {

        return this.parentDir;
    }

    public String getWebAppRoot() {

        return webAppRoot;
    }

    public void setWebAppRoot(String webAppRoot) {

        this.webAppRoot = webAppRoot;
    }

    
    public boolean isUnsplit() {
    
        return unsplit;
    }

    
    public void setUnsplit(boolean unsplit) {
    
        this.unsplit = unsplit;
    }

}
