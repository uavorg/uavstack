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

package com.creditease.agent.feature.procwatch;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.creditease.agent.feature.procwatch.ProcWatcher.ProcStat;

public class ProcInfo {

    public static ProcInfo newborn(String pid, String name, Set<String> ports) {

        ProcInfo p = new ProcInfo();
        p.setPid(pid);
        p.setName(name);
        p.setPorts(ports);
        p.setStat(ProcStat.NEWBORN);
        p.setWatchTime(System.currentTimeMillis());
        return p;
    }

    public static ProcInfo newborn(String pid) {

        ProcInfo p = new ProcInfo();
        p.setPid(pid);
        p.setStat(ProcStat.NEWBORN);
        p.setWatchTime(System.currentTimeMillis());
        return p;
    }

    private String pid;
    private String name;
    private Set<String> ports = new HashSet<String>();

    private String ppid;
    private String user;

    private String cwd;
    private String exe;
    private String comm;
    private String cmd;
    private String environ;
    private String cmdline;

    private long startTime;
    private long stopTime;
    private long watchTime;
    private ProcStat stat;

    private String infoOutput;
    private String errOutput;

    public void running(Map<String, String> info) {

        stat = ProcStat.RUNNING;
        ppid = info.get("ppid");
        user = info.get("user");

        cwd = info.get("cwd");
        exe = info.get("exe");
        comm = info.get("comm");
        cmd = info.get("cmd");
        environ = info.get("environ");
        cmdline = info.get("cmdline");

        infoOutput = info.get("infoOut");
        errOutput = info.get("errOut");
    }

    public void disappeared() {

        stat = ProcStat.DISAPPEARED;
        if (stopTime == 0) {
            stopTime = System.currentTimeMillis();
        }
    }

    public void dying() {

        stat = ProcStat.DYING;
    }

    public void dead() {

        stat = ProcStat.DEAD;
        stopTime = System.currentTimeMillis();
    }

    public void alive() {

        stat = ProcStat.ALIVE;
    }

    public String getPid() {

        return pid;
    }

    public void setPid(String pid) {

        this.pid = pid;
    }

    public String getPpid() {

        return ppid;
    }

    public void setPpid(String ppid) {

        this.ppid = ppid;
    }

    public String getExe() {

        return exe;
    }

    public void setExe(String exe) {

        this.exe = exe;
    }

    public String getCwd() {

        return cwd;
    }

    public void setCwd(String cwd) {

        this.cwd = cwd;
    }

    public String getCmd() {

        return cmd;
    }

    public void setCmd(String cmd) {

        this.cmd = cmd;
    }

    public long getStartTime() {

        return startTime;
    }

    public void setStartTime(long startTime) {

        this.startTime = startTime;
    }

    public long getStopTime() {

        return stopTime;
    }

    public void setStopTime(long stopTime) {

        this.stopTime = stopTime;
    }

    public Set<String> getPorts() {

        return ports;
    }

    public void setPorts(Set<String> ports) {

        this.ports = ports;
    }

    public ProcStat getStat() {

        return stat;
    }

    public void setStat(ProcStat stat) {

        this.stat = stat;
    }

    public String getInfoOutput() {

        return infoOutput;
    }

    public void setInfoOutput(String infoOutput) {

        this.infoOutput = infoOutput;
    }

    public String getErrOutput() {

        return errOutput;
    }

    public void setErrOutput(String errOutput) {

        this.errOutput = errOutput;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getUser() {

        return user;
    }

    public void setUser(String user) {

        this.user = user;
    }

    public String getComm() {

        return comm;
    }

    public void setComm(String comm) {

        this.comm = comm;
    }

    public long getWatchTime() {

        return watchTime;
    }

    public void setWatchTime(long watchTime) {

        this.watchTime = watchTime;
    }

    public String getEnviron() {

        return environ;
    }

    public void setEnviron(String environ) {

        this.environ = environ;
    }

    public String getCmdline() {

        return cmdline;
    }

    public void setCmdline(String cmdline) {

        this.cmdline = cmdline;
    }

}
