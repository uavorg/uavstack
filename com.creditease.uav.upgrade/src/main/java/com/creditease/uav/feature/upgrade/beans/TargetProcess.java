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

public class TargetProcess {

    protected String processId;
    private String profileName;
    protected String startScriptName;
    protected String stopScriptName;

    public TargetProcess() {

    }

    public TargetProcess(String processId, String profileName, String startScriptName, String stopScriptName) {
        this.processId = processId;
        this.profileName = profileName;
        this.startScriptName = startScriptName;
        this.stopScriptName = stopScriptName;
    }

    public static TargetProcess buildProcess(String processId, String profileName, String scriptExt) {

        return new TargetProcess(processId, profileName, "start_" + profileName + scriptExt,
                "stop_" + profileName + scriptExt);
    }

    public String getProcessId() {

        return processId;
    }

    public void setProcessId(String processId) {

        this.processId = processId;
    }

    public String getProfileName() {

        return profileName;
    }

    public void setProfileName(String profileName) {

        this.profileName = profileName;
    }

    public String getStartScriptName() {

        return startScriptName;
    }

    public void setStartScriptName(String startScriptName) {

        this.startScriptName = startScriptName;
    }

    public String getStopScriptName() {

        return stopScriptName;
    }

    public void setStopScriptName(String stopScriptName) {

        this.stopScriptName = stopScriptName;
    }

    @Override
    public String toString() {

        StringBuilder sbd = new StringBuilder("[Process]:");
        sbd.append("pid=").append(this.processId).append(",");
        sbd.append("start script=").append(this.startScriptName).append(",");
        sbd.append("stop script=").append(stopScriptName);

        return sbd.toString();
    }
}
