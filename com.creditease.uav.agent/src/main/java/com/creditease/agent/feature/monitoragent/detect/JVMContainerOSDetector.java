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

package com.creditease.agent.feature.monitoragent.detect;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.helpers.jvmtool.JVMAgentInfo;
import com.creditease.agent.helpers.osproc.OSProcess;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.uav.httpasync.HttpAsyncClient;
import com.creditease.uav.httpasync.HttpAsyncClientFactory;
import com.creditease.uav.httpasync.HttpAsyncException;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;

/**
 * 
 * JVMContainerOSDetector description: co-work with UAVMOF to scan out all jvm in Container(Docker,Rocket,...)
 *
 */
public class JVMContainerOSDetector extends BaseDetector {

    private static final String UAV_MOF_ROOT = "/com.creditease.uav/";

    /**
     * 
     * ScanPingCallback description: try to ping the container to detect if there is a JVM with MOF inside
     *
     */
    private class ScanPingCallback implements HttpClientCallback {

        private String url;
        private OSProcess proc;

        public ScanPingCallback(OSProcess proc, String url) {
            this.url = url;
            this.proc = proc;
        }

        @Override
        public void completed(HttpClientCallbackResult result) {

            int retCode = result.getRetCode();

            String vendor = result.getReplyDataAsString();

            if (retCode != 200 || StringHelper.isEmpty(vendor)) {
                return;
            }

            /**
             * vendor=="Tomcat" or "Jetty" or "JBoss" or "JSE" or "MSCP" or "SpringBoot"; so 3<=vendor.length()<=10
             */
            if (!(vendor.length() >= 3 && vendor.length() <= 10)) {
                return;
            }

            if (log.isTraceEnable()) {
                log.info(this, "A Container[" + url + "]'s JVM[" + vendor + "] Detected.");
            }

            // try to get systemproperties
            String getSysUrl = url + UAV_MOF_ROOT + "jvm?action=getSystemPro";

            client.doAsyncHttpGet(getSysUrl, new GetSystemInfoCallback(this.proc, this.url, vendor));
        }

        @Override
        public void failed(HttpClientCallbackResult result) {

            if (log.isDebugEnable()) {
                HttpAsyncException hae = result.getException();

                if (hae == null) {
                    log.debug(this, "No Container[" + url + "] JVM Found.");
                }
                else {
                    log.debug(this, "No Container[" + url + "] JVM Found.", hae.getCause());
                }
            }
        }
    }

    /**
     * 
     * GetSystemInfoCallback description: when scan PING is OK, we need get the SystemProperties of the Container JVM
     *
     */
    private class GetSystemInfoCallback implements HttpClientCallback {

        private String url;
        private OSProcess proc;
        private String vendor;

        public GetSystemInfoCallback(OSProcess proc, String url, String vendor) {
            this.url = url;
            this.proc = proc;
            this.vendor = vendor;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void completed(HttpClientCallbackResult result) {

            String dstr = result.getReplyDataAsString();

            if (StringHelper.isEmpty(dstr)) {
                return;
            }

            try {
                Map<String, String> p = JSONHelper.toObject(dstr, Map.class);

                Properties sysPro = new Properties();

                for (String pKey : p.keySet()) {

                    String value;
                    try {
                        value = p.get(pKey);

                    }
                    catch (Exception e) {
                        continue;
                    }

                    sysPro.setProperty(pKey, value);
                }

                String pid = sysPro.getProperty("proc.ext.pid");

                if (pid == null && proc != null) {
                    pid = proc.getPid();
                }

                JVMAgentInfo appServerInfo = new JVMAgentInfo(pid, null, sysPro);

                // set base url
                appServerInfo.setJVMAccessURL(this.url + UAV_MOF_ROOT);

                addWorker(appServerInfo);
            }
            catch (Exception e) {
                log.err(this,
                        "JVMContainerOSDetector[" + cName + "] start worker FAIL: java process id=" + proc.getPid(), e);
            }
        }

        @Override
        public void failed(HttpClientCallbackResult result) {

            HttpAsyncException hae = result.getException();

            if (hae == null) {
                log.err(this, "Get Container[" + url + "] JVM[" + vendor + "] SystemProperties Fail.");
            }
            else {
                log.err(this, "Get Container[" + url + "] JVM[" + vendor + "] SystemProperties Fail.", hae.getCause());
            }
        }
    }

    protected HttpAsyncClient client;

    protected String[] scanPorts = new String[0];

    public JVMContainerOSDetector(String cName, String feature, String initHandlerKey, long detectInterval) {
        super(cName, feature, initHandlerKey, detectInterval);

        String ports = this.getConfigManager().getFeatureConfiguration(this.feature, "detector.container.ports");

        configScanPorts(ports);

        client = HttpAsyncClientFactory.build(5, 100, 1, 1, 1);
    }

    @Override
    public void run() {

        scanWithProcDetection();

        scanWithConfigURLs();
    }

    /**
     * scanWithConfigURLs
     */
    private void scanWithConfigURLs() {

        for (String port : scanPorts) {

            String baseurl = "http://127.0.0.1:" + port;

            client.doAsyncHttpGet(baseurl + UAV_MOF_ROOT + "jvm?action=ping", new ScanPingCallback(null, baseurl));
        }
    }

    /**
     * configScanPorts
     * 
     * @param ports
     */
    private void configScanPorts(String ports) {

        if (StringHelper.isEmpty(ports)) {
            return;
        }

        scanPorts = ports.split(",");
    }

    /**
     * scanWithProcDetection
     */
    @SuppressWarnings("unchecked")
    private void scanWithProcDetection() {

        AgentFeatureComponent afc = (AgentFeatureComponent) this.getConfigManager().getComponent("procscan",
                "ProcDetectAgent");

        if (afc == null) {
            return;
        }

        Collection<OSProcess> procs = (Collection<OSProcess>) afc.exchange("procscan.query.allprocs");

        if (procs == null) {
            return;
        }

        if (log.isDebugEnable()) {
            log.debug(this, "ContainerOSDetector scan START...");
        }

        for (OSProcess proc : procs) {

            String name = proc.getName();

            /**
             * NOTE: support docker-proxy,docker,docker-current; support java http scanning for dev work more
             */
            if (name.indexOf("docker") == -1 && name.indexOf("java") == -1) {
                continue;
            }

            Map<String, String> tags = proc.getTags();

            String jargs = StringHelper.isEmpty(tags.get("jargs")) ? "" : tags.get("jargs");

            /**
             * NOTE: must install uavmof
             */
            if (!(jargs.contains("-javaagent:") && jargs.contains("monitorframework"))) {
                continue;
            }

            for (String port : proc.getPorts()) {
                String baseurl;

                /**
                 * option 1：ip:port
                 */
                if (port.indexOf(":") > -1) {
                    baseurl = "http://" + port;
                }
                /**
                 * option 2: port only
                 */
                else {
                    baseurl = "http://127.0.0.1:" + port;
                }

                client.doAsyncHttpGet(baseurl + UAV_MOF_ROOT + "jvm?action=ping", new ScanPingCallback(proc, baseurl));
            }
        }
    }

    /**
     * get http async client
     * 
     * @return
     */
    public HttpAsyncClient getClient() {

        return this.client;
    }

    @Override
    public void onConfigUpdate(Properties updatedConfig) {

        if (updatedConfig.containsKey("feature.monitoragent.detector.container.ports")) {
            String ports = (String) updatedConfig.get("feature.monitoragent.detector.container.ports");

            configScanPorts(ports);
        }
    }

}
