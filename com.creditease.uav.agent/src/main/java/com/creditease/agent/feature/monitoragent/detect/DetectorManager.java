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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.creditease.agent.feature.monitoragent.datacatch.BaseMonitorDataCatchWorker;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.jvmtool.JVMAgentInfo;
import com.creditease.agent.spi.AbstractTimerWork;
import com.creditease.agent.spi.ResourceLimitationAuditor;
import com.creditease.agent.spi.ResourceLimitationAuditor.ResourceType;

public class DetectorManager extends AbstractTimerWork {

    protected final Map<String, BaseDetector> detectorMap = new LinkedHashMap<String, BaseDetector>();
	
    protected final Map<String, BaseMonitorDataCatchWorker> workers = new ConcurrentHashMap<String, BaseMonitorDataCatchWorker>();

    public DetectorManager(String cName, String feature) {
        super(cName, feature);
    }

    public void installDetector(BaseDetector detector) {

        if (detector == null) {
            return;
        }

        detector.setWorkers(workers);
		
        detectorMap.put(detector.getName(), detector);
    }

    public void uninstallDetector(String detectorId) {

        if (detectorId == null) {
            return;
        }

        detectorMap.remove(detectorId);
    }

    /**
     * get the AppServer JVM Info
     * 
     * @param serverId
     * @return
     */
    public JVMAgentInfo getJVMAgentInfo(String serverId) {

        if (null == serverId || "".equals(serverId)) {
            return null;
        }

        for (BaseDetector detector : detectorMap.values()) {

            JVMAgentInfo info = detector.getJVMAgentInfo(serverId);

            if (info != null) {
                return info;
            }
        }

        return null;
    }

    @Override
    public void run() {

        /**
         * step 1: check if I am is a cpu killer or mem killer if yes kill myself
         * 
         * limitation to MA: 300M memory use ; cpu over 30% for one core (note: just one core) during 2 mins
         */
        ResourceLimitationAuditor rla = this.getGlobalResourceLimitationAuditor();

        double memLimit = DataConvertHelper
                .toDouble(this.getConfigManager().getFeatureConfiguration(this.feature, "limit.mem"), 300000D);

        double cpuLimit = DataConvertHelper
                .toDouble(this.getConfigManager().getFeatureConfiguration(this.feature, "limit.cpu.value"), 30D);

        long timerange = DataConvertHelper
                .toLong(this.getConfigManager().getFeatureConfiguration(this.feature, "limit.cpu.timerange"), 120000);

        rla.check(rla.new ResourceCheckRule(ResourceType.Memory, memLimit),
                rla.new ResourceCheckRule(ResourceType.CPU, cpuLimit, timerange, this.period));

        /**
         * step 2: run detector
         */
        for (BaseDetector detector : detectorMap.values()) {
            try {
                detector.run();

                if (log.isDebugEnable()) {
                    log.debug(this, "RUN Detector[" + detector.getName() + "] SUCCESS.");
                }

            }
            catch (Exception e) {
                log.err(this, "RUN Detector[" + detector.getName() + "] FAIL.", e);
            }
        }
    }

    @Override
    public void cancel() {

        super.cancel();
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            // ignore
        }
        for (BaseDetector detector : detectorMap.values()) {
            try {
                detector.stop();
            }
            catch (Exception e) {
                log.err(this, "STOP Detector[" + detector.getName() + "]  FAIL.", e);
            }
        }
    }
}
