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

package com.creditease.agent.feature;

import java.util.Map;

import com.creditease.agent.feature.procdetectagent.NetworkIoDetector;
import com.creditease.agent.feature.procdetectagent.OSProcessScanner;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.spi.AgentFeatureComponent;

/**
 * 进程探测
 * 
 * @author zhen zhang
 * 
 */
public class ProcDetectAgent extends AgentFeatureComponent {

    private OSProcessScanner scanner;

    private NetworkIoDetector networkIoDetector;

    public ProcDetectAgent(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public void start() {

        String scanIntervalStr = this.getConfigManager().getFeatureConfiguration(this.feature, "scan.interval");

        int scanInterval = (StringHelper.isEmpty(scanIntervalStr)) ? 30000 : Integer.parseInt(scanIntervalStr);

        String networkDetectIntervalStr = this.getConfigManager().getFeatureConfiguration(this.feature,
                "networkDetect.interval");

        int networkDetectInterval = (StringHelper.isEmpty(networkDetectIntervalStr)) ? 60000
                : Integer.parseInt(networkDetectIntervalStr);

        scanner = new OSProcessScanner("OSProcessScanner", this.feature);

        this.getTimerWorkManager().scheduleWork(scanner.getName(), scanner, 0, scanInterval);

        if (log.isTraceEnable()) {
            log.info(this, "OSProcessScanner Started");
        }

        networkIoDetector = new NetworkIoDetector("NetworkIoDetector", this.feature);

        this.getTimerWorkManager().scheduleWork(networkIoDetector.getName(), networkIoDetector, 5000,
                networkDetectInterval);

        if (log.isTraceEnable()) {
            log.info(this, "NetworkIoDetector Started");
        }

    }

    @Override
    public void stop() {

        this.getTimerWorkManager().cancel("OSProcessScanner");

        if (log.isTraceEnable()) {
            log.info(this, "OSProcessScanner Stopped.");
        }

        this.getTimerWorkManager().cancel("NetworkIoDetector");

        if (log.isTraceEnable()) {
            log.info(this, "NetworkIoDetector Stopped.");
        }
        super.stop();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object exchange(String eventKey, Object... data) {

        switch (eventKey) {
            case "procscan.nodeinfo.tags":
                scanner.putContainerTags((Map<String, Long>) data[0]);
                break;
            case "procscan.nodeinfo.portFlux":
                scanner.setPortFlux((String) data[0], (Long) data[1]);
                break;
            case "networkdetect.portList":
                networkIoDetector.setPortList((String) data[0]);
                break;
            case "procscan.query.procstate":
                return scanner.getProcState((String) data[0]);
            case "procscan.query.allprocs":
                return scanner.getAllProcsState();
        }

        return null;
    }

}
