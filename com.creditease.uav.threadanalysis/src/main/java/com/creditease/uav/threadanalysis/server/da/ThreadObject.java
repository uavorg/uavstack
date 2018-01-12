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

package com.creditease.uav.threadanalysis.server.da;

import java.util.List;

public class ThreadObject {

    private String id;
    private String name;
    private boolean daemon;
    private java.lang.Thread.State threadState;
    private List<String> stackTrace;
    private List<MonitorObject> lockedMonitors;
    private MonitorObject pendingMonitor;

    private String info;
    private double cpu;
    private long time;

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public boolean isDaemon() {

        return daemon;
    }

    public void setDaemon(boolean daemon) {

        this.daemon = daemon;
    }

    public List<String> getStackTrace() {

        return stackTrace;
    }

    public void setStackTrace(List<String> stackTrace) {

        this.stackTrace = stackTrace;
    }

    public List<MonitorObject> getLockedMonitors() {

        return lockedMonitors;
    }

    public void setLockedMonitors(List<MonitorObject> lockedMonitors) {

        this.lockedMonitors = lockedMonitors;
    }

    public MonitorObject getPendingMonitor() {

        return pendingMonitor;
    }

    public void setPendingMonitor(MonitorObject pendingMonitor) {

        this.pendingMonitor = pendingMonitor;
    }

    public java.lang.Thread.State getThreadState() {

        return threadState;
    }

    public void setThreadState(java.lang.Thread.State threadState) {

        this.threadState = threadState;
    }

    public double getCpu() {

        return cpu;
    }

    public void setCpu(double cpu) {

        this.cpu = cpu;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getInfo() {

        return info;
    }

    public void setInfo(String info) {

        this.info = info;
    }

    public long getTime() {

        return time;
    }

    public void setTime(long time) {

        this.time = time;
    }

}
