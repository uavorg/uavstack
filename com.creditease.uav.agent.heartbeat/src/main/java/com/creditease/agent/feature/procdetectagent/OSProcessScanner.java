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

package com.creditease.agent.feature.procdetectagent;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.JVMToolHelper;
import com.creditease.agent.helpers.OSProcessHelper;
import com.creditease.agent.helpers.RuntimeHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.helpers.osproc.OSProcess;
import com.creditease.agent.helpers.osproc.ProcDiskIOCollector;
import com.creditease.agent.spi.AbstractTimerWork;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.agent.spi.IConfigurationManager;

public class OSProcessScanner extends AbstractTimerWork {

    private String shellPath;

    private String containerTagsStr = "";

    private String portFlux;
    private long portFluxTimestamp;

    private Map<String, Long> containerTags = new HashMap<String, Long>();

    private ProcDiskIOCollector pdioc;

    private Map<String, OSProcess> curProcsState;

    public OSProcessScanner(String cName, String feature) {
        super(cName, feature);

        pdioc = OSProcessHelper.newProcDiskIOCollector();

        shellPath = this.getConfigManager().getContext(IConfigurationManager.METADATAPATH) + "osscan";
    }

    @Override
    public void run() {

        Map<String, OSProcess> procs = new LinkedHashMap<String, OSProcess>();
        /**
         * get all process info
         */
        StringBuffer portList = new StringBuffer();
        if (JVMToolHelper.isWindows()) {
            scanWindowsProcesses(procs, portList, this.shellPath);
        }
        else {
            scanLinuxProcesses(procs, portList, this.shellPath);
        }

        AgentFeatureComponent afc = (AgentFeatureComponent) this.getConfigManager().getComponent(this.feature,
                "ProcDetectAgent");
        if (null != afc) {
            afc.exchange("networkdetect.portList", portList.toString());
        }

        /**
         * get java process ext info
         */
        List<Map<String, String>> jvmProcs = JVMToolHelper.getAllJVMProcesses(null);

        for (Map<String, String> jvmProc : jvmProcs) {

            String pid = jvmProc.get("pid");

            if (procs.containsKey(pid)) {

                /**
                 * add service java info to process info
                 */
                OSProcess p = procs.get(pid);

                addTagsToOSProcess(jvmProc, p);

            }
            else {
                /**
                 * add the non-service java into process info
                 */
                OSProcess p = new OSProcess();
                p.setName("java");
                p.setPid(pid);

                addTagsToOSProcess(jvmProc, p);

                procs.put(pid, p);
            }
        }

        if (log.isDebugEnable()) {
            log.debug(this, "Process Scan Result: " + JSONHelper.toString(procs));
        }

        // collect each process's cpu, mem and so on
        collectProcState(procs);

        /**
         * pass to NodeInfo
         */
        afc = (AgentFeatureComponent) this.getConfigManager().getComponent("hbclientagent", "HeartBeatClientAgent");

        Map<String, Object> extInfo = new LinkedHashMap<String, Object>();

        extInfo.put("procs", procs);
        extInfo.put("tags", this.containerTagsStr);

        if (afc != null) {
            afc.exchange("hbclientagent.nodeinfo.extinfo", extInfo);
        }

        /**
         * pass to ProcWatcher to check if the guiding process is alive
         */
        afc = (AgentFeatureComponent) this.getConfigManager().getComponent("procwatch", "ProcWatchAgent");
        if (afc != null) {
            afc.exchange("agent.procwatch.refresh", procs);
        }

        /**
         * refresh current processes states
         */
        this.curProcsState = procs;
    }

    private void addTagsToOSProcess(Map<String, String> jvmProc, OSProcess p) {

        for (String key : jvmProc.keySet()) {

            if ("pid".equalsIgnoreCase(key)) {
                continue;
            }

            p.addTag(key, jvmProc.get(key));
        }
    }

    /**
     * scan linux processes
     * 
     * @param procs
     * @param portList
     * @param shellPath
     */
    public void scanLinuxProcesses(Map<String, OSProcess> procs, StringBuffer portList, String shellPath) {

        try {
            String output = RuntimeHelper.exeShell("netstat -nlp -t", shellPath);

            if (log.isDebugEnable()) {
                log.debug(this, "Linux Process Scanning Shell Result: " + output);
            }

            if (!StringHelper.isEmpty(output)) {

                String[] lines = output.split("\n");

                int i = 0;

                for (; i < lines.length; i++) {

                    String line = lines[i].trim();

                    int tIndex = line.indexOf("tcp");

                    if (tIndex != 0) {
                        continue;
                    }

                    OSProcess proc;

                    line = line.replaceAll("\\s{2,}", " ");

                    String[] fields = line.split(" ");

                    // only need listening service port
                    boolean findListenTag = false;
                    int indexPidPro = -1;
                    for (int fi = 0; fi < fields.length; fi++) {
                        if (fields[fi].indexOf("LISTEN") > -1) {
                            findListenTag = true;
                            indexPidPro = fi + 1;
                        }
                    }

                    if (findListenTag == false) {
                        continue;
                    }

                    // pid
                    String pidPro = fields[indexPidPro];

                    String[] pids = pidPro.split("/");

                    String pid = pids[0];
                    String proName = pids[pids.length - 1];

                    if (!procs.containsKey(pid)) {

                        proc = new OSProcess();
                        proc.setPid(pid);
                        proc.setName(proName);
                      //process startTime
                        Map <String,String> procTags = proc.getTags();
                        String startTime =  Long.toString(OSProcessHelper.getProcStartTime(pid,shellPath));                         
                        procTags.put("starttime", startTime);
                        proc.setTags(procTags);
                        procs.put(pid, proc);
                    }
                    else {
                        proc = procs.get(pid);
                    }

                    // port
                    String addr = fields[indexPidPro - 3];

                    String[] addrLs = addr.split(":");

                    String port = addrLs[addrLs.length - 1];

                    proc.addPort(port);

                    portList = portList.append(port).append(" ");
                }

            }
        }
        catch (Exception e) {
            log.err(this, "Linux Process Scanning Fail.", e);
        }
    }

    /**
     * scan windows processes
     * 
     * @param portList
     */
    public void scanWindowsProcesses(Map<String, OSProcess> procs, StringBuffer portList, String shellPath) {

        try {
            String output = RuntimeHelper.exeShell("netstat -anob -p TCP", shellPath);

            if (!StringHelper.isEmpty(output)) {

                String[] lines = output.split("\n");

                int i = 0;

                for (; i < lines.length; i++) {

                    String line = lines[i].trim();

                    int tIndex = line.indexOf("TCP");

                    if (tIndex != 0) {
                        continue;
                    }

                    OSProcess proc;

                    line = line.replaceAll("\\s{2,}", " ");

                    String[] fields = line.split(" ");

                    // only need listening service port
                    if (!"listening".equalsIgnoreCase(fields[fields.length - 2])) {
                        continue;
                    }

                    // pid
                    String pid = fields[fields.length - 1];

                    if (!procs.containsKey(pid)) {

                        proc = new OSProcess();
                        proc.setPid(pid);
                      //process startTime
                        Map <String,String> procTags = proc.getTags();
                        String startTime =  Long.toString(OSProcessHelper.getProcStartTime(pid,shellPath));                         
                        procTags.put("starttime", startTime);
                        proc.setTags(procTags);
                        procs.put(pid, proc);
                    }
                    else {
                        proc = procs.get(pid);
                    }

                    // port
                    String addr = fields[fields.length - 4];

                    String port = addr.split(":")[1];

                    proc.addPort(port);

                    portList = portList.append(port).append(" ");
                    if (!"UNKNOWN".equals(proc.getName())) {
                        continue;
                    }

                    // name
                    String procNameLine = "";
                    for (int j = i + 1; j < lines.length; j++) {

                        procNameLine = lines[j].trim();

                        if (procNameLine.indexOf("TCP") == 0) {
                            // i=j;
                            break;
                        }
                        if (procNameLine.indexOf("[") > -1) {
                            // i=j+1;
                            String procName = procNameLine.substring(1, procNameLine.length() - 1);
                            proc.setName(procName.trim());
                            break;
                        }
                    }
                }
            }

        }
        catch (Exception e) {
            log.err(this, "Windows Process Scanning Fail.", e);
        }
    }

    public void putContainerTags(Map<String, Long> tags) {

        synchronized (this.containerTags) {

            /**
             * step 1: merging tags
             */
            this.containerTags.putAll(tags);

            long curTime = System.currentTimeMillis();

            Set<String> delTags = new HashSet<String>();

            StringBuilder sb = new StringBuilder();

            /**
             * step 2: check the expire tags and figure out current tags
             */
            for (String key : this.containerTags.keySet()) {

                long ts = this.containerTags.get(key);

                if (curTime - ts > 60000) {
                    delTags.add(key);
                }
                else {
                    sb.append(key).append(",");
                }
            }

            this.containerTagsStr = sb.toString();

            /**
             * step 3: remove expire tags
             */
            for (String key : delTags) {
                this.containerTags.remove(key);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void collectProcState(Map<String, OSProcess> procs) {

        if (procs == null || procs.isEmpty()) {
            return;
        }

        /**
         * detect if the process is watching
         */
        AgentFeatureComponent procwatch = (AgentFeatureComponent) this.getConfigManager().getComponent("procwatch",
                "ProcWatchAgent");

        Set<String> pids = new HashSet<>();
        for (String pid : procs.keySet()) {

            pids.add(pid);

            if (procwatch != null) {
                Object res = procwatch.exchange("agent.procwatch.iswatch", pid);

                if (res != null) {
                    procs.get(pid).addTag("watch", String.valueOf(res));
                }
            }
        }

        /**
         * collect proc state metrics
         */
        Map<String, Map<String, String>> procStates = null;
        if (JVMToolHelper.isWindows()) { // do not include windows
            try {
                procStates = OSProcessHelper.getWinProcessInfo(shellPath, pids);
            }
            catch (Exception e) {
                log.err(this, "Collect Proc State Fail.", e);
                return;
            }
        }
        else {
            try {
                procStates = OSProcessHelper.getProcessInfo(shellPath, pids);
            }
            catch (Exception e) {
                log.err(this, "Collect Proc State Fail.", e);
                return;
            }
        }
        if (procStates == null) {
            return;
        }

        for (Map.Entry<String, Map<String, String>> en : procStates.entrySet()) {
            String pid = en.getKey();
            if (procs.containsKey(pid)) {
                OSProcess process = procs.get(pid);
                Map<String, String> states = en.getValue();
                for (Map.Entry<String, String> state : states.entrySet()) {
                    process.addTag(state.getKey(), state.getValue());
                }
            }
        }

        /**
         * collect disk read/write speed for each process
         */
        pdioc.collect(procs);

        /**
         * collect in/out network stream for each process
         */
        if (null == portFlux) {
            return;
        }

        String networkDetectIntervalStr = this.getConfigManager().getFeatureConfiguration(this.feature,
                "networkDetect.interval");
        int networkDetectInterval = (StringHelper.isEmpty(networkDetectIntervalStr)) ? 60000
                : Integer.parseInt(networkDetectIntervalStr);

        if (System.currentTimeMillis() - portFluxTimestamp > (2 * networkDetectInterval)) {
            if (log.isDebugEnable()) {
                log.debug(this, "portFlux is out of date ");
            }
            return;
        }

        Map<String, String> portFluxMap = null;

        try {
            portFluxMap = JSONHelper.toObject(portFlux, Map.class);
        }
        catch (Exception e) {
            log.err(this, "portFlux" + portFlux + " Json2Map error:\n " + e.toString());
            return;
        }

        if (null == portFluxMap) {
            return;
        }

        for (String pid : procs.keySet()) {
            OSProcess proc = procs.get(pid);
            float in_proc = 0;
            float out_proc = 0;
            for (String port : proc.getPorts()) {
                if (portFluxMap.containsKey("in_" + port)) {
                    proc.addTag("in_" + port, portFluxMap.get("in_" + port));
                    proc.addTag("out_" + port, portFluxMap.get("out_" + port));
                    in_proc += Float.parseFloat(portFluxMap.get("in_" + port));
                    out_proc += Float.parseFloat(portFluxMap.get("out_" + port));
                }
            }
            proc.addTag("in", String.valueOf(in_proc));
            proc.addTag("out", String.valueOf(out_proc));
        }
    }

    public void setPortFlux(String portFlux, long timestamp) {

        this.portFlux = portFlux;
        this.portFluxTimestamp = timestamp;
    }

    public OSProcess getProcState(String pid) {

        if (this.curProcsState != null) {
            return this.curProcsState.get(pid);
        }

        return null;
    }

    public Collection<OSProcess> getAllProcsState() {

        if (this.curProcsState != null) {
            return Collections.unmodifiableCollection(this.curProcsState.values());
        }

        return null;
    }
}
