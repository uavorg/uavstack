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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.OSProcessHelper;
import com.creditease.agent.helpers.RuntimeHelper;
import com.creditease.agent.helpers.ThreadHelper;
import com.creditease.agent.helpers.osproc.OSProcess;
import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.AbstractTimerWork;
import com.creditease.agent.spi.IConfigurationManager;

public class ProcWatcher extends AbstractTimerWork {

    public enum ProcStat {
        NEWBORN, RUNNING, DISAPPEARED, DYING, ALIVE, DEAD
    }

    private WatchHolder watches;
    private ProcHolder procs;

    private String cacheFilePath;
    private String shellPath;

    private long restartTimeout = 10000L;

    private String[] webContainers = { "org.apache.catalina.startup.Bootstrap",
            "org.springframework.boot.loader.Launcher", "org.springframework.boot.SpringApplication" };

    public ProcWatcher(String cName, String feature) {
        super(cName, feature);

        watches = new WatchHolder();
        procs = new ProcHolder();
    }

    public void setCacheFilePath(String cacheFilePath) {

        this.cacheFilePath = cacheFilePath;
    }

    public void setShellPath(String shellPath) {

        this.shellPath = shellPath;
    }

    /**
     * refresh current running pids
     * 
     * @param npids
     */
    public void refresh(Map<String, OSProcess> currentProcs) {

        procs.refresh(currentProcs);
    }

    /**
     * 
     * @param pid
     */
    public int watch(String pid) {

        if (pid == null || "".equals(pid.trim())) {
            return 0;
        }

        if (watches.isWatching(pid)) {
            return 2;
        }

        if (procs.contains(pid)) {
            String name = procs.getName(pid);
            Set<String> ports = procs.getPorts(pid);
            ProcInfo p = ProcInfo.newborn(pid, name, ports);
            detectProcessInfo(p);
            watches.addWatch(p);
            return 1;
        }

        if (log.isTraceEnable()) {
            log.warn(this, "Not Found pid " + pid + " to watch.");
        }
        return -1;
    }

    /**
     * 
     * @param pid
     */
    public int unwatch(String pid) {

        if (pid == null || "".equals(pid.trim())) {
            return 0;
        }

        if (watches.isWatching(pid)) {
            watches.delWatch(pid);
            return 1;
        }

        if (log.isTraceEnable()) {
            log.warn(this, "Not Found pid " + pid + " to unwatch.");
        }
        return -1;
    }

    /**
     * find out disappeared process of watching, restart.
     */
    @Override
    public void run() {

        if (log.isDebugEnable()) {
            log.debug(this, "ProcWatcher run START watching: " + JSONHelper.toString(watches.getWatchPids())
                    + ", runningPids: " + JSONHelper.toString(procs.getRunningPids()));
        }

        if (!watches.hasWatches()) {
            return;
        }

        if (procs.isEmpty()) { // maybe ma has just launched, procs not been refresh.
            return;
        }

        Set<String> wpids = watches.getWatchPids();

        for (String wpid : wpids) {

            // 1 judge process is running
            if (procs.contains(wpid) && !procs.isIllegalStat(wpid)) {
                continue;
            }

            ProcInfo dying = watches.getProcInfo(wpid);
            // 2 judge process is alive
            if (rescue(dying)) { // process is alive!!!
                continue;
            }

            // last judge ps with cmdline
            if (isRunningWithCmdline(dying.getComm(), dying.getCmd())) {
                log.info(this, "process is dying, but alive in ps with comm: " + JSONHelper.toString(dying));
                continue;
            }

            // 3 restart process
            String msg = restart(dying);

            // 4 send notification
            notify(dying, msg);
        }

        // save to local file
        persistent();

        if (log.isDebugEnable()) {
            log.debug(this, "ProcWatcher run END");
        }
    }

    /**
     * "我觉得我还可以抢救一下"
     * 
     * @param dying
     * @return true if process is alive , else false
     */
    private boolean rescue(ProcInfo dying) {

        if (isRunning(dying.getPid())) {
            log.warn(this, "process is dying, but alive in ps: " + JSONHelper.toString(dying));
            return true;
        }

        List<ProcInfo> probabilities = new ArrayList<>();

        Set<String> runningPids = procs.getRunningPids();
        for (String rpid : runningPids) {
            String rname = procs.getName(rpid);
            if (!dying.getName().equals(rname)) {
                continue;
            }

            Set<String> rports = procs.getPorts(rpid);
            if (dying.getPorts().size() != 0) {
                boolean containsPort = false;
                for (String port : dying.getPorts()) {
                    if (rports.contains(port)) {
                        containsPort = true;
                    }
                }
                if (!containsPort) {
                    continue;
                }
            }

            ProcInfo np = ProcInfo.newborn(rpid, rname, rports);
            detectProcessInfo(np);
            probabilities.add(np);
        }

        if (probabilities.size() == 0) {
            return false;
        }

        for (ProcInfo np : probabilities) {
            if (!dying.getComm().equals(np.getComm()) || !dying.getExe().equals(np.getExe())) {
                continue;
            }

            if (dying.getCwd().equals(np.getCwd()) && dying.getCmd().equals(np.getCmd())) {
                watches.replace(dying, np);

                if (log.isDebugEnable()) {
                    log.debug(this, dying.getPid() + " is same as " + np.getPid());
                }

                return true;
            }
        }

        if (isRunningWithPort(dying.getPorts())) {
            log.warn(this, "process ports is listening");
            return true;
        }

        return false;
    }

    private void notify(ProcInfo proc, String startedMsg) {

        String desc = "发现" + proc.getName() + "进程" + proc.getPid() + "停止，已重启。\n启动参数:" + proc.getCmd() + "\n启动信息："
                + startedMsg;
        NotificationEvent event = new NotificationEvent(NotificationEvent.EVENT_RT_ALERT_CRASH,
                "发现" + proc.getName() + "进程停止，已重启进程", desc);
        putNotificationEvent(event);
    }

    public void persistent() {

        if (log.isDebugEnable()) {
            log.debug(this, "watch process persistent to file " + cacheFilePath);
        }

        try {
            IOHelper.writeTxtFile(cacheFilePath, JSONHelper.toString(watches.getProcInfos()), "UTF-8", false);
        }
        catch (IOException e) {
            log.err(this, "persistent watch procinfo failed", e);
        }
    }

    public void loadWatch() {

        String json = IOHelper.readTxtFile(cacheFilePath, "UTF-8");
        if (json == null || "".equals(json)) {
            return;
        }

        List<ProcInfo> watchProcesses = JSONHelper.toObjectArray(json, ProcInfo.class);
        for (ProcInfo p : watchProcesses) {
            watches.addWatch(p);
        }
    }

    private boolean isRunning(String pid) {

        String shell = "ps h -p " + pid + " | wc -l";
        try {
            String count = RuntimeHelper.exec(1000, "/bin/sh", "-c", shell);

            if (log.isDebugEnable()) {
                log.debug(this, "shell exec: " + shell + "  \t\t Shell result:" + JSONHelper.toString(count));
            }

            return count != null && !"0".equals(trim(count));
        }
        catch (Exception e) {
            log.err(this, "Runtime exec `" + shell + "` failed.", e);
            return false;
        }
    }

    private boolean isRunningWithCmdline(String comm, String cmdline) {

        String shell = "ps -fC " + comm + " | grep \"" + cmdline + "\" | wc -l";
        try {
            String count = RuntimeHelper.exec(1000, "/bin/sh", "-c", shell);

            if (log.isDebugEnable()) {
                log.debug(this, "shell exec: " + shell + "  \t\t Shell result:" + JSONHelper.toString(count));
            }

            return count != null && !"0".equals(trim(count));
        }
        catch (Exception e) {
            log.err(this, "Runtime exec `" + shell + "` failed.", e);
            return false;
        }
    }

    private String restart(ProcInfo proc) {

        // build command
        // String startShell = proc.getCmd() + " 1>>" + proc.getInfoOutput() + " 2>>" + proc.getErrOutput();
        // build startShell from cmdline
        String startShell = buildStartShellFromCmdline(proc.getCmdline()) + " 1>>" + proc.getInfoOutput() + " 2>>"
                + proc.getErrOutput();

        // judge whether to implant the MOF agent
        startShell = doMOFFilter(startShell);

        String[] environs = buildEnvirons(proc.getEnviron());
        // int i = proc.getCmdline().indexOf(" ");
        // String startSh = (i < 0) ? proc.getExe() : proc.getExe() + proc.getCmdline().substring(i);

        if (log.isDebugEnable()) {
            log.debug(this, "============================================================start process: " + startShell);
        }

        String msg = start(proc.getCwd(), startShell, environs);

        if (log.isDebugEnable()) {
            log.debug(this, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!start result:" + msg);
        }

        return msg;
    }

    private String buildStartShellFromCmdline(String cmdline) {

        // /proc/$pid/cmdline separated by '\0'
        if (cmdline == null) {
            return null;
        }

        String[] ss = cmdline.split("\u0000");

        StringBuilder sb = new StringBuilder();
        sb.append(ss[0]);
        for (int i = 1; i < ss.length; i++) {

            sb.append(" ").append("\"").append(ss[i]).append("\"");
        }
        return sb.toString();
    }

    private String doMOFFilter(String startShell) {

        String isMOFInstallTagFile = ConfigurationManager.getInstance().getContext(IConfigurationManager.METADATAPATH)
                + "isMOFInstall";

        String root = ConfigurationManager.getInstance().getContext(IConfigurationManager.ROOT);

        String agentArgs = "-javaagent:" + root.substring(0, root.lastIndexOf("/"))
                + "/uavmof/com.creditease.uav.agent/com.creditease.uav.monitorframework.agent-1.0-agent.jar";

        boolean isMOFInstalled = IOHelper.exists(isMOFInstallTagFile);

        String[] strs = startShell.split(" ");

        // only influence the webContainer
        if (isWebContainer(startShell)) {

            if (isMOFInstalled && startShell.indexOf(agentArgs) == -1) {
                // strs[0] is the "java" command ,add the MOF agent after it;
                startShell = startShell.replace(strs[0], strs[0] + " \"" + agentArgs + "\"");
            }
            else if (!isMOFInstalled) {

                startShell = startShell.replace("\"" + agentArgs + "\" ", "");
            }
        }
        return startShell;

    }

    private boolean isWebContainer(String startShell) {

        for (int i = 0; i < webContainers.length; i++) {
            if (startShell.indexOf(webContainers[i]) > -1) {
                return true;
            }
        }
        return false;
    }

    private String[] buildEnvirons(String environ) {

        String[] envs = environ.split("\u0000");
        String[] environs = new String[envs.length];
        for (int i = 0; i < envs.length; i++) {
            String s = envs[i];
            int idx = s.indexOf("=");
            if (idx < 0 || idx == s.length() - 1) {
                environs[i] = s;
            }
            else {
                environs[i] = s.substring(0, idx + 1) + "'" + s.substring(idx + 1) + "'";
            }
        }
        return environs;
    }

    private String start(String cwd, String startShell, String[] environs) {

        StringBuilder command = new StringBuilder();
        command.append("cd " + cwd);
        command.append(System.lineSeparator());

        if (environs != null && environs.length > 0) {
            for (String env : environs) {
                command.append(env);
                command.append(System.lineSeparator());
            }
        }

        command.append("nohup " + startShell + " &");

        command.append(System.lineSeparator());

        String msg = null;
        try {
            msg = RuntimeHelper.exeShell(command.toString(), shellPath);
        }
        catch (Exception e) {
            log.err(this, "Runtime exec `" + command.toString() + "` failed.", e);
        }
        return msg;
    }

    private void detectProcessInfo(ProcInfo proc) {

        Map<String, String> map = null;
        try {
            // get ppid uid cmdline
            map = OSProcessHelper.getProcessInfo(proc.getPid(), "ppid", "user", "comm", "cmd");

            if (map == null) {
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("readlink /proc/" + proc.getPid() + "/exe");
            sb.append("  && ");
            sb.append("readlink /proc/" + proc.getPid() + "/cwd");
            sb.append("  && ");
            sb.append("readlink /proc/" + proc.getPid() + "/fd/1");
            sb.append("  && ");
            sb.append("readlink /proc/" + proc.getPid() + "/fd/2");
            String re = RuntimeHelper.exec(1000, "/bin/sh", "-c", sb.toString());
            String[] infos = re.split(System.lineSeparator());
            map.put("exe", trim(infos[0]));
            map.put("cwd", trim(infos[1]));
            map.put("infoOut", trim(infos[2]));
            map.put("errOut", trim(infos[3]));
            String environ = IOHelper.readTxtFile("/proc/" + proc.getPid() + "/environ", "UTF-8");
            map.put("environ", environ);
            String cmdline = IOHelper.readTxtFile("/proc/" + proc.getPid() + "/cmdline", "UTF-8");
            map.put("cmdline", cmdline);
            /*
             * // get exe String exe = RuntimeHelper.exec("readlink /proc/" + proc.getPid() + "/exe"); map.put("exe",
             * trim(exe)); // get cwd String cwd = RuntimeHelper.exec("readlink /proc/" + proc.getPid() + "/cwd");
             * map.put("cwd", trim(cwd));
             * 
             * // get output String infoOut = RuntimeHelper.exec("readlink /proc/" + proc.getPid() + "/fd/1");
             * map.put("infoOut", trim(infoOut)); String errOut = RuntimeHelper.exec("readlink /proc/" + proc.getPid() +
             * "/fd/2"); map.put("errOut", trim(errOut));
             */
            if (log.isDebugEnable()) {
                log.debug(this, "detect process info: " + JSONHelper.toString(map));
            }
        }
        catch (IOException e) {
            log.warn(this, "detect newborn process throws IOException, maybe process exited.", e);
            proc.dead();
            return;
        }
        catch (Exception e) {
            log.err(this, "detect newborn process failed.", e);
            return;
        }

        proc.running(map);
    }

    public boolean isWatching(String pid) {

        return watches.isWatching(pid);
    }

    /**
     * 
     */
    private static class ProcHolder {

        private Map<String, OSProcess> currentProcs;

        public Set<String> getRunningPids() {

            return currentProcs == null ? null : currentProcs.keySet();
        }

        public void refresh(Map<String, OSProcess> currentProcs) {

            this.currentProcs = currentProcs;
        }

        public boolean isEmpty() {

            return currentProcs == null || currentProcs.isEmpty();
        }

        public boolean contains(String pid) {

            if (isEmpty()) {
                return false;
            }

            return currentProcs.containsKey(pid);
        }

        public String getName(String pid) {

            if (isEmpty()) {
                return null;
            }

            OSProcess p = currentProcs.get(pid);
            if (p == null) {
                return null;
            }

            return p.getName();
        }

        public Set<String> getPorts(String pid) {

            if (isEmpty()) {
                return null;
            }

            OSProcess p = currentProcs.get(pid);
            if (p == null) {
                return null;
            }

            return p.getPorts();
        }

        /**
         * 判断java进程死掉时，仍有可能采集到进程号的问题
         * 
         * @param pid
         * @return
         */
        public boolean isIllegalStat(String pid) {

            if (isEmpty()) {
                return true;
            }

            OSProcess p = currentProcs.get(pid);
            if (p == null) {
                return true;
            }

            if (!"java".equals(p.getName())) {
                return false;
            }

            String main = p.getTags().get("main");
            if (main == null || "".equals(main.trim())) {
                return true;
            }

            return false;
        }
    }

    /**
     * 
     */
    private static class WatchHolder {

        private Map<String, ProcInfo> watching = new ConcurrentHashMap<>();

        public void addWatch(ProcInfo proc) {

            watching.put(proc.getPid(), proc);
        }

        public void delWatch(String pid) {

            watching.remove(pid);
        }

        public void replace(ProcInfo dead, ProcInfo newborn) {

            watching.remove(dead.getPid());
            watching.put(newborn.getPid(), newborn);
        }

        public Set<String> getWatchPids() {

            return watching.keySet();
        }

        public boolean isWatching(String pid) {

            return watching.containsKey(pid);
        }

        public boolean hasWatches() {

            return !watching.isEmpty();
        }

        public ProcInfo getProcInfo(String pid) {

            return watching.get(pid);
        }

        public Collection<ProcInfo> getProcInfos() {

            return watching.values();
        }

    }

    private String trim(String s) {

        return s == null ? null : s.trim();
    }

    // lsof -nt -i:8080,6379, | wc -l
    private boolean isRunningWithPort(Set<String> ports) {

        if (ports.size() == 0) {
            return false;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("lsof -nt -i:");
        for (String port : ports) {
            sb.append(port).append(",");
        }
        sb.append(" | wc -l");

        try {
            String count = RuntimeHelper.exec(1000, "/bin/sh", "-c", sb.toString());

            if (log.isDebugEnable()) {
                log.debug(this, "shell exec: " + sb.toString() + "  \t\t Shell result:" + JSONHelper.toString(count));
            }

            return count != null && !"0".equals(trim(count));
        }
        catch (Exception e) {
            log.err(this, "Runtime exec `" + sb.toString() + "` failed.", e);
            return false;
        }
    }

    public int restart(String pid) {

        if (!procs.contains(pid)) {
            return 0;
        }

        try {
            ProcInfo proc = null;
            if (watches.isWatching(pid)) {
                proc = watches.getProcInfo(pid);
            }
            else {
                String name = procs.getName(pid);
                Set<String> ports = procs.getPorts(pid);
                proc = ProcInfo.newborn(pid, name, ports);
                detectProcessInfo(proc);
            }

            // execute kill -15 first to release resource
            RuntimeHelper.exec("kill -15 " + pid);

            long startTime = System.currentTimeMillis();

            boolean isProcessAlive = false;

            do {

                isProcessAlive = isRunning(pid);

                if (!isProcessAlive) {

                    if (log.isDebugEnable()) {

                        log.debug(this, "The pid:" + pid + "is killed ");
                    }
                    break;
                }

                long endTime = System.currentTimeMillis();

                if ((endTime - startTime) > restartTimeout) {

                    log.err(this, "kill process " + pid + " timeout, will execute [kill -9] for the pid");

                    RuntimeHelper.exec("kill -9 " + pid);

                    ThreadHelper.suspend(1000L);

                    break;
                }

                else {

                    ThreadHelper.suspend(1000L);

                }

            }

            while (isProcessAlive);

            restart(proc);
        }
        catch (Exception e) {
            log.err(this, "restart " + pid + " faild.", e);
            return -1;
        }
        return 1;
    }

}
