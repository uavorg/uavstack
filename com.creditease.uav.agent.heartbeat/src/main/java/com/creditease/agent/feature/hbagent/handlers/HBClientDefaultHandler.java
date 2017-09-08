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

package com.creditease.agent.feature.hbagent.handlers;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.feature.HeartBeatClientAgent;
import com.creditease.agent.feature.hbagent.HeartBeatClientReqWorker;
import com.creditease.agent.feature.hbagent.node.NodeInfo;
import com.creditease.agent.feature.hbagent.node.NodeInfo.InfoType;
import com.creditease.agent.heartbeat.api.AbstractHBClientHandler;
import com.creditease.agent.heartbeat.api.HeartBeatEvent;
import com.creditease.agent.heartbeat.api.HeartBeatProtocol;
import com.creditease.agent.helpers.DateTimeHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.JVMToolHelper;
import com.creditease.agent.helpers.NetworkHelper;
import com.creditease.agent.helpers.OSProcessHelper;
import com.creditease.agent.helpers.RuntimeHelper;
import com.creditease.agent.helpers.osproc.DiskIOCollector;
import com.creditease.agent.spi.AbstractBaseHttpServComponent;
import com.creditease.agent.spi.AbstractHttpHandler;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.agent.spi.IConfigurationManager;
import com.sun.management.OperatingSystemMXBean;

/**
 * HeartBeatDefaultHandler is used to upload the meta data of the node
 * 
 * @author zhen zhang
 *
 */
@SuppressWarnings("restriction")
public class HBClientDefaultHandler extends AbstractHBClientHandler {

    private long hbStartTime = 0;

    private DiskIOCollector dioc;

    public HBClientDefaultHandler(String cName, String feature) {
        super(cName, feature);
        dioc = OSProcessHelper.newDiskIOCollector();
    }

    @Override
    public void handleClientOut(HeartBeatEvent data) {

        /**
         * sync node info
         */

        NodeInfo ni = new NodeInfo();

        // node id
        ni.setId(this.getConfigManager().getContext(IConfigurationManager.NODEUUID));

        String[] hosts = NetworkHelper.getHosts();

        // host
        ni.setHost(hosts[0]);
        // ip
        ni.setIp(hosts[1]);

        // node name
        String nodeName = this.getConfigManager().getContext(IConfigurationManager.NODEAPPNAME);
        ni.setName(nodeName);

        // node group
        String nodeGroup = this.getConfigManager().getContext(IConfigurationManager.NODEGROUP);
        ni.setGroup(nodeGroup);

        // mac address
        ni.putInfo(InfoType.OS, "mac", NetworkHelper.getMACAddress());
        // OS type
        ni.putInfo(InfoType.OS, "type", System.getProperty("os.name"));
        // OS arch
        ni.putInfo(InfoType.OS, "arch", System.getProperty("os.arch"));
        // CPU number
        ni.putInfo(InfoType.OS, "cpu.number", String.valueOf(Runtime.getRuntime().availableProcessors()));

        OperatingSystemMXBean osmb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        // max physical memory
        ni.putInfo(InfoType.OS, "cpu.maxmem", String.valueOf(osmb.getTotalPhysicalMemorySize()));
        // free physical memory
        ni.putInfo(InfoType.OS, "cpu.freemem", String.valueOf(osmb.getFreePhysicalMemorySize()));
        // CPU load
        ni.putInfo(InfoType.OS, "cpu.load", String.valueOf(Math.round(osmb.getSystemCpuLoad() * 100)));
        // CPU avg load
        ni.putInfo(InfoType.OS, "cpu.avgload", String.valueOf(Math.round(osmb.getSystemLoadAverage() * 100)));
        // Current Connections
        ni.putInfo(InfoType.OS, "conn.cur", getCurrentConnectionCount());

        // IO DISK INFO
        String str = "{}";
        try {
            str = getIODiskInfo();
        }
        catch (Exception e) {
            log.err(this, "error occur during get diskinfo:" + e);
        }
        ni.putInfo(InfoType.OS, "io.disk", str);

        // java version
        ni.putInfo(InfoType.OS, "java.ver", System.getProperty("java.version"));
        // java vm name
        ni.putInfo(InfoType.OS, "java.vm", System.getProperty("java.vm.name"));
        // java home
        ni.putInfo(InfoType.OS, "java.home", System.getProperty("java.home"));

        // node info
        ni.putInfo(InfoType.Node, "root", ConfigurationManager.getInstance().getContext(IConfigurationManager.ROOT));
        ni.putInfo(InfoType.Node, "profile",
                ConfigurationManager.getInstance().getContext(IConfigurationManager.PROFILENAME));
        ni.putInfo(InfoType.Node, "hbserver", ((HeartBeatClientReqWorker) ConfigurationManager.getInstance()
                .getComponent(this.feature, "HeartBeatClientReqWorker")).getCurrentHBServerURL());
        ni.putInfo(InfoType.Node, "pid", getPID());
        /**
         * node.state>0, means node alive node.state==0, means node is dying node.state<0, means node is dead
         */
        ni.putInfo(InfoType.Node, "state", "1");

        // node feature info
        Map<String, List<String>> featureInfo = getFeatureInfo();
        ni.putInfo(InfoType.Node, "feature", JSONHelper.toString(featureInfo));

        // node services info
        Map<String, String> services = getServiceURLs();
        String servicesStr = JSONHelper.toString(services);
        ni.putInfo(InfoType.Node, "services", servicesStr);

        ni.putInfo(InfoType.Node, "version", getConfigManager().getContext(IConfigurationManager.NODEAPPVERSION));

        // set time stamp
        ni.setClientTimestamp(System.currentTimeMillis());
        // set -1 mean no set by server at client side
        ni.setServerTimestamp(-1);

        // get the hbagent queue if this is non-master hbserver node
        HeartBeatClientAgent hbagent = (HeartBeatClientAgent) this.getConfigManager().getComponent(this.feature,
                "HeartBeatClientAgent");

        // get node ext info
        if (null != hbagent) {
            Map<String, Object> nodeExtInfo = hbagent.getNodeExtInfo();
            for (String key : nodeExtInfo.keySet()) {
                ni.putInfo(InfoType.Node, key, JSONHelper.toString(nodeExtInfo.get(key)));
            }
        }

        List<String> nodeInfos = new ArrayList<String>();

        // add node info of this node
        String nodeInfoOfThisNode = ni.toJSONString();

        nodeInfos.add(nodeInfoOfThisNode);

        if (null != hbagent) {

            List<List<String>> nodeInfoLists = hbagent.pollNodeInfoQueue();

            if (nodeInfoLists.size() > 0) {

                for (List<String> nodeInfoList : nodeInfoLists) {

                    nodeInfos.addAll(nodeInfoList);
                }
            }
        }

        data.putParam(HeartBeatProtocol.EVENT_DEFAULT, HeartBeatProtocol.EVENT_KEY_NODE_INFO, nodeInfos);

        // record hb request start time
        hbStartTime = System.currentTimeMillis();
    }

    /**
     * get the node's http service url
     * 
     * @return
     */
    @SuppressWarnings("rawtypes")
    private Map<String, String> getServiceURLs() {

        Set<Object> components = this.getConfigManager().getComponents();

        Map<String, String> services = new LinkedHashMap<String, String>();

        for (Object comp : components) {

            if (!AbstractBaseHttpServComponent.class.isAssignableFrom(comp.getClass())) {
                continue;
            }

            AbstractBaseHttpServComponent asc = (AbstractBaseHttpServComponent) comp;

            String compid = asc.getFeature() + "-" + asc.getName();

            String httpRootURL = asc.getHttpRootURL();

            List handlers = asc.getHandlers();

            for (Object handler : handlers) {

                if (!AbstractHttpHandler.class.isAssignableFrom(handler.getClass())) {
                    continue;
                }

                AbstractHttpHandler abshandler = (AbstractHttpHandler) handler;

                String serviceId = compid + "-" + abshandler.getContextPath();

                String serviceHttpURL = httpRootURL + abshandler.getContextPath();

                services.put(serviceId, serviceHttpURL);

            }

        }

        return services;
    }

    /**
     * get the node's feature info
     * 
     * @return
     */
    private Map<String, List<String>> getFeatureInfo() {

        Set<Object> components = this.getConfigManager().getComponents();

        Map<String, List<String>> featureInfo = new LinkedHashMap<String, List<String>>();

        for (Object comp : components) {

            if (!AgentFeatureComponent.class.isAssignableFrom(comp.getClass())) {
                continue;
            }

            AgentFeatureComponent afc = (AgentFeatureComponent) comp;
            String feature = afc.getFeature();
            List<String> fcomps = null;
            if (featureInfo.containsKey(feature)) {
                fcomps = featureInfo.get(feature);
            }
            else {
                fcomps = new ArrayList<String>();
                featureInfo.put(feature, fcomps);
            }
            fcomps.add(afc.getName());
        }
        return featureInfo;
    }

    private String getPID() {

        String name = ManagementFactory.getRuntimeMXBean().getName();
        String pid = name.split("@")[0];
        return pid;
    }

    private String getIODiskInfo() throws Exception {

        if (JVMToolHelper.isWindows()) {
            File[] roots = File.listRoots();

            StringBuilder sb = new StringBuilder("{");

            for (int i = 0; i < roots.length; i++) {
                File file = roots[i];
                sb.append("\"" + file.getPath() + "\":{")
                        .append("\"free\":\"" + Math.round((double) (file.getFreeSpace() / 1024)) + "\",")// 空闲空间
                        .append("\"total\":\"" + +Math.round((double) (file.getTotalSpace() / 1024)) + "\",")// 总空间
                        .append("\"use\":\""
                                + +Math.round((double) ((file.getTotalSpace() - file.getFreeSpace()) / 1024)) + "\",")// 已使用空间
                        .append("\"useRate\":\""
                                + ((file.getTotalSpace() == 0) ? "0"
                                        : (file.getTotalSpace() - file.getFreeSpace()) * 100 / file.getTotalSpace())
                                + "%\"")// 空间占用率
                        .append("}");

                if (i < roots.length - 1) {
                    sb.append(",");
                }
            }

            sb.append("}");
            return sb.toString();
        }
        else {
            Map<String, Map<String, String>> resultMap = new HashMap<String, Map<String, String>>();
            String diskResult;
            try {
                String shellParentPath = this.getConfigManager().getContext(IConfigurationManager.METADATAPATH) + "sh";
                diskResult = RuntimeHelper.exeShell("df -P", shellParentPath);
            }
            catch (Exception e) {
                return "{}";
            }
            String lines[] = diskResult.split("\n");
            for (int i = 1; i < lines.length; i++) {
                String[] args = lines[i].split("\\s+");
                Map<String, String> temp = new HashMap<String, String>();
                temp.put("total", args[1]);
                temp.put("use", args[2]);
                temp.put("free", args[3]);
                temp.put("useRate", args[4]);
                resultMap.put(args[5], temp);
            }
            /**
             * collect disk read/write speed
             */
            dioc.collect(resultMap);

            return JSON.toJSONString(resultMap);
        }

    }

    private String getCurrentConnectionCount() {

        try {
            if (JVMToolHelper.isWindows()) {
                String str = RuntimeHelper.exec("netstat -s");
                int start = str.indexOf("Current Connections");
                str = str.substring(start);
                int end = str.indexOf("\n");
                str = str.substring(0, end);
                String[] conns = str.split("=");
                return conns[1];
            }
            else {
                String shellParentPath = this.getConfigManager().getContext(IConfigurationManager.METADATAPATH) + "sh";
                String conns = RuntimeHelper.exeShell("netstat -na|grep ESTABLISHED|wc -l", shellParentPath);
                return conns;
            }
        }
        catch (Exception e) {
            return "-1";
        }
    }

    @Override
    public void handleClientIn(HeartBeatEvent data) {

        if (!data.containEvent(HeartBeatProtocol.EVENT_DEFAULT)) {
            return;
        }

        String rc = (String) data.getParam(HeartBeatProtocol.EVENT_DEFAULT, HeartBeatProtocol.EVENT_KEY_RETCODE);

        if (!HeartBeatProtocol.RC_I0000.equals(rc)) {
            log.err(this, "");
        }

        /**
         * Check time span diff to heartbeat server
         */
        Long time = (Long) data.getParam(HeartBeatProtocol.EVENT_DEFAULT, HeartBeatProtocol.EVENT_KEY_TIME);

        if (time == null) {
            return;
        }

        long curTime = System.currentTimeMillis();

        // compute the cost time for this hb request
        long hbCostTime = curTime - this.hbStartTime;

        long timeSpan = Math.abs(curTime - hbCostTime - time);

        // FIX: 1000ms is too hard for network, loose to 3 seconds
        if (timeSpan > 3000) {

            if (log.isTraceEnable()) {
                log.warn(this, "Current Node Time is out of 3 seconds to HeartBeat Server Node.");
            }

            try {
                if (JVMToolHelper.isWindows()) {
                    String syncDate = DateTimeHelper.toFormat("yyyy-MM-dd", time + hbCostTime);
                    String syncTime = DateTimeHelper.toFormat("HH:mm:ss", time + hbCostTime);
                    RuntimeHelper.exeShell("date " + syncDate,
                            this.getConfigManager().getContext(IConfigurationManager.METADATAPATH));
                    RuntimeHelper.exeShell("time " + syncTime,
                            this.getConfigManager().getContext(IConfigurationManager.METADATAPATH));
                }
                else {
                    String syncTime = DateTimeHelper.toFormat("yyyy-MM-dd HH:mm:ss", time + hbCostTime);
                    RuntimeHelper.exeShell("date -s \"" + syncTime + "\"",
                            this.getConfigManager().getContext(IConfigurationManager.METADATAPATH));
                }
            }
            catch (Exception e) {
                log.err(this, "Execute Date Command FAIL.", e);
            }
        }
        else {
            if (log.isDebugEnable()) {
                log.debug(this, "Current Node Time is SYNC to heartbeat server");
            }
        }

    }
}
