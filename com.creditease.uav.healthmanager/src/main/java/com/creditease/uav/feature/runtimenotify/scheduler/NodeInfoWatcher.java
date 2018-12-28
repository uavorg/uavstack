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

package com.creditease.uav.feature.runtimenotify.scheduler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.AbstractTimerWork;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.agent.spi.AgentResourceComponent;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.cache.api.CacheManager.CacheLock;
import com.creditease.uav.feature.RuntimeNotifyCatcher;
import com.creditease.uav.feature.runtimenotify.NotifyStrategy;
import com.creditease.uav.messaging.api.Message;
import com.creditease.uav.messaging.api.MessageProducer;
import com.creditease.uav.messaging.api.MessagingFactory;

/**
 * 
 * NodeInfoWatcher description:
 * 
 * 1. push proc info to cache
 * 
 * 2. judge if there is any proc down
 *
 */
public class NodeInfoWatcher extends AbstractTimerWork {

    private static class CrashEventObj {

        private String ip;
        private String appgroup;
        private String nodeuuid;
        private List<String> deadProcsInfo = new ArrayList<String>();
        private List<String> deadProcNames = new ArrayList<String>();

        public CrashEventObj(String ip, String appgroup) {

            this.ip = ip;
            this.appgroup = appgroup;
        }

        public String getAppGroup() {

            return appgroup;
        }

        public String getIp() {

            return ip;
        }

        public String getNodeuuid() {

            return nodeuuid;
        }

        public int getDeadProcsCount() {

            return deadProcNames.size();
        }

        public void setNodeuuid(String nodeuuid) {

            this.nodeuuid = nodeuuid;
        }

        public void addDeadProcName(String name) {

            this.deadProcNames.add(name);
        }

        public void addDeadProcInfo(String info) {

            this.deadProcsInfo.add(info);
        }

        public String getDeadProcNamesAsString() {

            StringBuffer sb = new StringBuffer("(");
            for (String name : this.deadProcNames) {
                sb.append(name + ",");
            }

            sb = sb.deleteCharAt(sb.length() - 1);

            return sb.append(")").toString();
        }

        public String getDeadProcsInfoAsString() {

            StringBuffer sb = new StringBuffer();
            for (String dpi : deadProcsInfo) {
                sb.append(dpi).append("\n");
            }

            return sb.toString().replace("\\", "\\\\");
        }

    }

    private static final String LOCK_KEY = "rtnotify.nodeinfotimer.lock";
    private static final String UAV_CACHE_REGION = "store.region.uav";
    private static final String CRASH_PROCS = "rtnotify.dead.procs";
    private static final String CRASH_PROCS_DETAIL = "rtnotify.dead.procs.detail";
    private static final long LOCK_TIMEOUT = 30 * 1000;
    private static final long MIN_RANDOM_PORT = 32768;

    private CacheManager cm;
    private CacheLock lock;
    private int hold;
    private int timeout;
    private boolean isSendMq;
    private boolean isExchange;

    public NodeInfoWatcher(String cName, String feature) {

        super(cName, feature);

        cm = (CacheManager) getConfigManager().getComponent(this.feature, RuntimeNotifyCatcher.CACHE_MANAGER_NAME);

        lock = cm.newCacheLock(UAV_CACHE_REGION, LOCK_KEY, LOCK_TIMEOUT);

        hold = DataConvertHelper.toInt(getConfigManager().getFeatureConfiguration(feature, "nodeinfotimer.period"),
                15000);
        timeout = DataConvertHelper.toInt(getConfigManager().getFeatureConfiguration(feature, "crash.timeout"), 300000);
        isSendMq = DataConvertHelper
                .toBoolean(getConfigManager().getFeatureConfiguration(feature, "nodeinfoprocess.sendmq"), true);

        isExchange = DataConvertHelper
                .toBoolean(getConfigManager().getFeatureConfiguration(feature, "nodeinfoprocess.exchange"), false);
    }

    @Override
    public void run() {

        /**
         * Step 1: get node info
         */
        if (!lock.getLock()) {
            return;
        }

        if (log.isTraceEnable()) {
            log.info(this, "NodeInfoWatcher RUN START.");
        }

        Map<String, String> data = cm.getHashAll(UAV_CACHE_REGION, "node.info");

        if (!lock.isLockInHand()) {
            return;
        }

        if (data == null || data.size() == 0) {
            lock.releaseLock();

            if (log.isTraceEnable()) {
                log.info(this, "NodeInfoWatcher RUN END as No Data");
            }

            return;
        }

        if (isFrozen()) {

            if (log.isDebugEnable()) {
                log.debug(this, "NodeInfoWatcher is in frozen time.");
            }

            lock.releaseLock();
            return;
        }

        /**
         * Step 2: sync node info to redis
         */
        List<Map<String, Object>> mdflist = syncProcInfoToCache(data);

        /**
         * Step 3: check if any proc crash
         */
        judgeProcCrash(data);

        /**
         * Step 4: push data to runtimenotify mgr or to mq
         */
        if (isExchange) {
            exchangeToRuntimeNotify(JSONHelper.toString(mdflist));

        }

        if (isSendMq) {
            sendToMQ(mdflist);

        }

        freezeTime();

        lock.releaseLock();
    }

    /**
     * nodeInfo数据转为list返回并将判断进程死亡所用的K、V存入redis
     */
    @SuppressWarnings("rawtypes")
    private List<Map<String, Object>> syncProcInfoToCache(Map<String, String> data) {

        List<Map<String, Object>> mdflist = new ArrayList<>();

        Map<String, String> fieldValues = new HashMap<String, String>();
        Map<String, String> fieldValuesDetail = new HashMap<String, String>();

        for (Map.Entry<String, String> entry : data.entrySet()) {

            Map<String, Object> mdfMap = buildMDF(entry.getValue());
            MonitorDataFrame mdf = new MonitorDataFrame(mdfMap);

            String time = mdf.getTimeFlag() + "";
            List<Map> els = mdf.getElemInstances("server", "procState");
            for (Map el : els) {
                try {

                    @SuppressWarnings("unchecked")
                    Map<String, Object> m = (Map<String, Object>) el.get("values");
                    String hashKey = genProcHashKey(mdf.getIP(), m);

                    Map<String, String> detail = new HashMap<String, String>();
                    detail.put("appgroup", mdf.getExt("appgroup"));
                    detail.put("nodeuuid", entry.getKey());

                    // 分别存时间戳和group
                    fieldValues.put(hashKey, time);
                    fieldValuesDetail.put(hashKey, JSONHelper.toString(detail));
                }
                catch (Exception e) {
                    log.err(this, "Sync ProcInfo To Cache Fail." + " ProcInfo:" + JSONHelper.toString(el), e);
                }

            }

            mdflist.add(mdfMap);
        }

        cm.putHash(UAV_CACHE_REGION, CRASH_PROCS, fieldValues);
        cm.putHash(UAV_CACHE_REGION, CRASH_PROCS_DETAIL, fieldValuesDetail);

        if (log.isDebugEnable()) {
            log.debug(this, "NodeInfoWatcher SYNC Node Data to Cache: data size=" + mdflist.size());
        }

        return mdflist;
    }

    /**
     * 判断进程死亡
     * 
     * 1.对于重启后name、main、margs、固定port不变的进程，由于key相同会直接覆盖
     * 
     * 2.固定port改变，找出ip_name相同但固定port不完全相同的进程， 存在任一端口相同则判定两个进程是相同，删除时间戳较老的进程，只保留时间戳较新的进程信息
     * 
     * 3.其余信息不同的进程则认为是新的进程
     * 
     * 4.时间戳超过进程死亡时间（可配置）则保存至死亡进程list并在redis中删除该进程。
     */
    private void judgeProcCrash(Map<String, String> data) {

        if (log.isDebugEnable()) {
            log.debug(this, "NodeInfoWatcher Judge Crash START.");
        }

        Map<String, String> allProcs = cm.getHashAll(UAV_CACHE_REGION, CRASH_PROCS);
        Map<String, String> allProcDetails = cm.getHashAll(UAV_CACHE_REGION, CRASH_PROCS_DETAIL);
        if (allProcs == null) {
            return;
        }

        long deadline = System.currentTimeMillis() - timeout;

        List<String> delKeys = new ArrayList<>();
        List<String> deadKeys = new ArrayList<>();

        for (Entry<String, String> en : allProcs.entrySet()) {
            String procKey = en.getKey();
            long time = Long.parseLong(en.getValue());

            if (delKeys.contains(procKey)) {
                continue;
            }

            try {
                String[] procKeyArray = procKey.split("_", -1);
                String ip = procKeyArray[0];
                String name = procKeyArray[1];

                if (procKeyArray[2].equals("")) {
                    // 不存在固定端口的进程
                    if (time < deadline) {
                        delKeys.add(procKey);
                        deadKeys.add(procKey);
                    }
                    continue;
                }

                List<String> ports = new ArrayList<String>(Arrays.asList(procKeyArray[2].split("#")));

                boolean hasNewProc = false;
                // 存在固定端口的进程，判断是否有ip、name相同但固定端口不完全相同的进程
                for (Entry<String, String> enBak : allProcs.entrySet()) {
                    String procKeyBak = enBak.getKey();
                    long timeBak = Long.parseLong(enBak.getValue());

                    String[] procKeyBakArray = procKeyBak.split("_", -1);
                    String ipBak = procKeyBakArray[0];
                    String nameBak = procKeyBakArray[1];

                    if (procKeyBakArray[2].equals("") || !(ipBak + nameBak).equals(ip + name)
                            || procKeyBak.equals(procKey) || delKeys.contains(procKeyBak)) {
                        continue;
                    }

                    List<String> portsBak = new ArrayList<String>(Arrays.asList(procKeyBakArray[2].split("#")));

                    for (String port : ports) {
                        if (portsBak.contains(port)) {
                            if (time >= timeBak) {
                                delKeys.add(procKeyBak);
                                break;
                            }
                            else {
                                delKeys.add(procKey);
                                hasNewProc = true;
                            }
                        }
                    }
                }

                if (!hasNewProc && time < deadline) {
                    delKeys.add(procKey);
                    deadKeys.add(procKey);
                }
            }
            catch (Exception e) {
                log.err(this, "Fail to judge ProcCrash." + " ProcKey:" + procKey, e);
            }
        }

        /**
         * Step 3: release lock
         */

        String[] dKeys = new String[delKeys.size()];
        delKeys.toArray(dKeys);
        // delete
        cm.delHash(UAV_CACHE_REGION, CRASH_PROCS, dKeys);
        cm.delHash(UAV_CACHE_REGION, CRASH_PROCS_DETAIL, dKeys);

        if (log.isDebugEnable()) {
            log.debug(this, "NodeInfoWatcher Judge Crash RESULT: dead=" + deadKeys.size());
        }

        if (deadKeys.isEmpty()) {
            return;
        }

        /**
         * Step 4: there is dead process, make alert
         */

        Map<String, Map<String, String>> deadProcs = new HashMap<String, Map<String, String>>();
        for (String key : deadKeys) {
            Map<String, String> procDetail = new HashMap<String, String>();
            procDetail.put("deadtime", allProcs.get(key));
            procDetail.put("detail", allProcDetails.get(key));

            deadProcs.put(key, procDetail);
        }

        fireEvent(deadProcs, data);
    }

    /**
     * 触发预警事件
     */
    @SuppressWarnings("unchecked")
    private void fireEvent(Map<String, Map<String, String>> deadProcs, Map<String, String> data) {

        /**
         * Step 1: split crash event by IP
         */
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Map<String, CrashEventObj> ips = new HashMap<String, CrashEventObj>();
        for (Entry<String, Map<String, String>> en : deadProcs.entrySet()) {
            String procKey = en.getKey();
            String[] procInfo = procKey.split("_", -1);
            String ip = procInfo[0];
            String procName = procInfo[1];
            
            Map<String, String> map = en.getValue();

            String deadtime = map.get("deadtime");
            String appgroup;
            String nodeuuid = "";
            
            if(map.get("detail") != null) {
                Map<String, String> detail = JSONHelper.toObject(map.get("detail"), Map.class);
                appgroup = detail.get("appgroup");
                nodeuuid = detail.get("nodeuuid");
            }
            else{
                appgroup = map.get("appgroup");
            }

            CrashEventObj ceo;

            if (!ips.containsKey(ip)) {
                ceo = new CrashEventObj(ip, appgroup);
                ceo.setNodeuuid(nodeuuid);
                ips.put(ip, ceo);
            }
            else {
                ceo = ips.get(ip);
            }

            ceo.addDeadProcName(procName);
            ceo.addDeadProcInfo("触发时间：" + format.format(new Date(Long.parseLong(deadtime))) + ", 进程信息：" + procKey);
        }

        /**
         * Step 2: send notification event by IP
         */
        RuntimeNotifyStrategyMgr strategyMgr = (RuntimeNotifyStrategyMgr) getConfigManager().getComponent(this.feature,
                "RuntimeNotifyStrategyMgr");
        for (CrashEventObj ceo : ips.values()) {
            
            String title;
            String nodeuuid = ceo.getNodeuuid();
            if (!StringHelper.isEmpty(nodeuuid) && StringHelper.isEmpty(data.get(nodeuuid))) {
                title = "应用组[" + ceo.getAppGroup() + "]的" + ceo.getIp() + "监控代理程序(MonitorAgent)超过" + timeout / 1000
                        + "秒没有心跳数据上送";
            }
            else {
                title = "应用组[" + ceo.getAppGroup() + "]的" + ceo.getIp() + "共发现" + ceo.getDeadProcsCount() + "进程"
                        + ceo.getDeadProcNamesAsString() + "可疑死掉";
            }

            String description = ceo.getDeadProcsInfoAsString();

            NotificationEvent event = new NotificationEvent(NotificationEvent.EVENT_RT_ALERT_CRASH, title, description, System.currentTimeMillis(), ceo.getIp(), "");

            /**
             * Notification Manager will not block the event, the frozen time has no effect to this event
             */
            event.addArg(NotificationEvent.EVENT_Tag_NoBlock, "true");
            event.addArg("appgroup", ceo.getAppGroup());
            event.addArg("nodeuuid", nodeuuid);

            NotifyStrategy stra = strategyMgr.seekStrategy("server@procCrash@" + ceo.getIp());

            if (null != stra) {
                putNotifyAction(event, stra);
                event.addArg("strategydesc", stra.getDesc());
            }

            if (log.isTraceEnable()) {
                log.info(this, "NodeInfoWatcher Crash Event Happen: event=" + event.toJSONString());
            }

            this.putNotificationEvent(event);
        }

    }

    /**
     * 添加报警action
     */
    private void putNotifyAction(NotificationEvent event, NotifyStrategy stra) {

        Map<String, String> actions = stra.getAction();
        if (actions == null || actions.isEmpty()) {
            return;
        }

        for (Entry<String, String> act : actions.entrySet()) {
            event.addArg("action_" + act.getKey(), act.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildMDF(String node) {

        Map<String, Object> mdf = new HashMap<>();

        Map<String, Object> nodeMap = JSONHelper.toObject(node, Map.class);
        long time = (long) nodeMap.get("clientTimestamp");
        String ip = (String) nodeMap.get("ip");
        String host = (String) nodeMap.get("host");
        String svrid = (String) nodeMap.get("id");
        String name = (String) nodeMap.get("name");

        mdf.put("time", time);
        mdf.put("host", host);
        mdf.put("ip", ip);
        mdf.put("svrid", svrid + "---" + name);
        mdf.put("tag", "N");

        Map<String, String> ext = new HashMap<String, String>();

        // add appgroup
        ext.put("appgroup", (String) nodeMap.get("group"));

        mdf.put("ext", ext);

        // frames
        Map<String, Object> frames = new HashMap<>();
        // server
        List<Map<String, Object>> servers = new ArrayList<>();
        Map<String, Object> server = null;
        List<Map<String, Object>> instances = null;

        // MEId : "IP"
        server = new HashMap<>();
        server.put("MEId", "hostState");
        // instances
        instances = new ArrayList<>();
        Map<String, Object> infoMap = (Map<String, Object>) nodeMap.get("info");
        Map<String, Object> ins = new HashMap<>();
        ins.put("id", ip + "_");
        putDiskInfo(infoMap);
        ins.put("values", infoMap);
        instances.add(ins);
        server.put("Instances", instances);
        servers.add(server);

        String nodeProcs = (String) infoMap.get("node.procs");
        if (nodeProcs != null) {
            // MEId : "PROC"
            server = new HashMap<>();
            server.put("MEId", "procState");
            // instances
            instances = new ArrayList<>();
            Map<String, Object> procs = JSONHelper.toObject(nodeProcs, Map.class);
            Map<String, Object> values = null;
            for (Map.Entry<String, Object> proc : procs.entrySet()) {
                ins = new HashMap<>();
                values = new HashMap<>();
                Map<String, Object> tmp = (Map<String, Object>) proc.getValue();
                for (Map.Entry<String, Object> e : tmp.entrySet()) {
                    if ("tags".equals(e.getKey())) {
                        values.putAll((Map<String, Object>) e.getValue());
                        continue;
                    }
                    values.put(e.getKey(), e.getValue());
                }
                ins.put("id", ip + "_" + tmp.get("name") + "_" + proc.getKey());
                ins.put("values", values);
                instances.add(ins);
            }
            server.put("Instances", instances);
            servers.add(server);
        }

        frames.put("server", servers);
        mdf.put("frames", frames);

        return mdf;
    }

    /**
     * 拼接判断进程死亡所用的key
     * 
     * 1.存在固定端口进程 ip_name_port#port#__
     * 
     * 2.不存在固定端口的非java进程 ip_name___
     * 
     * 3.不存在固定端口、java非服务进程 ip_name__main_margshash
     * 
     * @return ip_name_ports_main_margshash
     */
    private String genProcHashKey(String ip, Map<String, Object> m) {

        StringBuilder psb = new StringBuilder();
        @SuppressWarnings("unchecked")
        List<String> ports = (List<String>) m.get("ports");
        // 对端口排序，保证端口不变时key相同
        Collections.sort(ports);
        for (String port : ports) {
            String portKey = port;
            // 考虑Container拼接的端口为ip:port的形式
            if (port.contains(":")) {
                port = port.split(":", -1)[1];
            }

            if (Integer.parseInt(port) < MIN_RANDOM_PORT) {// 过滤随机端口
                psb.append(portKey).append("#");
            }
        }

        String name = (String) m.get("name");
        String javaInfos = "_";
        String main = (String) m.get("main");
        if (null != main) {
            String margs = m.get("margs") == null ? "" : (String) m.get("margs");

            if (0 == psb.length()) {
                javaInfos = main + "_" + margs.hashCode();
            }
        }

        return ip + "_" + name + "_" + psb.toString() + "_" + javaInfos;
    }

    //
    @SuppressWarnings("unchecked")
    private void putDiskInfo(Map<String, Object> infoMap) {

        if (infoMap.get("os.io.disk") == null) {
            return;
        }

        String diskStr = (String) infoMap.get("os.io.disk"); // deal windows
        diskStr = diskStr.replace(":\\", "/");
        Map<String, Object> disk = JSONHelper.toObject(diskStr, Map.class);
        for (String dk : disk.keySet()) {
            String pk = dk.replace("/", ".");
            if (!pk.startsWith(".")) {
                pk = "." + pk;
            }
            if (!pk.endsWith(".")) {
                pk = pk + ".";
            }

            Map<String, Object> dv = (Map<String, Object>) disk.get(dk);
            for (String dvk : dv.keySet()) {
                String dvv = dv.get(dvk).toString();
                if ("useRate".equals(dvk) || "useRateInode".equals(dvk)) {
                    dvv = dvv.replace("%", ""); // cut '%'
                }
                infoMap.put("os.io.disk" + pk + dvk, dvv);
            }
        }
    }

    private boolean isFrozen() {

        String timestampStr = cm.get(UAV_CACHE_REGION, "rtnotify.nodeinfotimer.hold");
        if (timestampStr == null) {
            return false;
        }

        long timestamp = 0;
        try {
            timestamp = Long.parseLong(timestampStr);
        }
        catch (NumberFormatException e) {
            log.err(this, "NodeInfoWatcher timestampStr format Long fail: " + timestampStr, e);
            return false;
        }
        long now = System.currentTimeMillis();
        if (now - timestamp >= hold) {
            return false;
        }
        return true;

    }

    /**
     * freeze time
     * 
     * @return true if freeze success, else return false.
     */
    private void freezeTime() {

        long now = System.currentTimeMillis();
        cm.put(UAV_CACHE_REGION, "rtnotify.nodeinfotimer.hold", now + "");
    }

    private void exchangeToRuntimeNotify(String mdfs) {

        AgentFeatureComponent rn = (AgentFeatureComponent) this.getConfigManager().getComponent("runtimenotify",
                "RuntimeNotifyCatcher");
        if (rn != null) {
            rn.exchange("runtime.notify", mdfs, true);
        }

        if (log.isTraceEnable()) {
            log.info(this, "NodeInfoWatcher RUN END.");
        }

    }

    private void sendToMQ(List<Map<String, Object>> mdfList) {

        AgentResourceComponent arc = (AgentResourceComponent) ConfigurationManager.getInstance()
                .getComponent("messageproducer", "MessageProducerResourceComponent");

        MessageProducer producer = (MessageProducer) arc.getResource();
        if (producer == null) {
            log.debug(this, "MessageProducer is null!");
            return;
        }

        List<Map<String, Object>> mdfMsg = null;
        for (Map<String, Object> mdf : mdfList) {
            mdfMsg = new ArrayList<>(1);
            mdfMsg.add(mdf);
            String mesKey = MonitorDataFrame.MessageType.NodeInfo.toString();
            Message msg = MessagingFactory.createMessage(mesKey);
            msg.setParam(mesKey, JSONHelper.toString(mdfMsg));

            boolean check = producer.submit(msg);
            String sendState = mesKey + " Data Sent " + (check ? "SUCCESS" : "FAIL");

            if (log.isDebugEnable()) {
                String mdfDetail = check ? (mdf.get("ip") + "---" + mdf.get("time")) : JSONHelper.toString(mdfMsg);
                log.debug(this, sendState + ". MDF:" + mdfDetail);
            }
        }
    }
}
