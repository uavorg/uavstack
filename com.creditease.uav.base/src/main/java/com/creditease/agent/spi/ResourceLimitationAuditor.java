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

package com.creditease.agent.spi;

import java.util.HashMap;
import java.util.Map;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.JVMToolHelper;
import com.creditease.agent.helpers.OSProcessHelper;
import com.creditease.agent.helpers.osproc.OSProcess;

public class ResourceLimitationAuditor extends AbstractComponent {

    /**
     * 
     * AuditChecker description:
     *
     */
    private abstract class AuditChecker {

        protected ResourceCheckRule rule;

        protected OSProcess procState;

        protected int countToLimitation = 1;

        protected int recordToLimitation = 0;

        public abstract ResourceType getResourceType();

        public abstract boolean isOverLimit();

        public abstract void doLimitation();

        public void setRule(ResourceCheckRule rule) {

            this.rule = rule;

            if (rule.getTimeRange() > -1 && rule.getCheckInterval() > -1) {
                this.countToLimitation = Math.round(rule.getTimeRange() / rule.getCheckInterval());
            }
        }

        public void setProcState(OSProcess procState) {

            this.procState = procState;
        }

    }

    /**
     * 
     * CPUAuditChecker description: CPU Limitation
     *
     */
    private class CPUAuditChecker extends AuditChecker {

        @Override
        public ResourceType getResourceType() {

            return ResourceType.CPU;
        }

        @Override
        public boolean isOverLimit() {

            /**
             * MonitorAgent should not use cpu rate over 30% during 2 mins
             */
            double cpu = DataConvertHelper.toDouble(procState.getTags().get("cpu"), -1);

            if (cpu >= this.rule.getResLimit()) {
                this.recordToLimitation += 1;
            }
            else {
                this.recordToLimitation = 0;
            }

            if (this.recordToLimitation >= this.countToLimitation) {
                log.warn(this,
                        "Process[" + procState.getPid() + "] CPU Usage[" + cpu + "%] is over " + this.rule.getResLimit()
                                + "% during more than " + this.rule.getTimeRange() / 1000 + " seconds");

                return true;
            }

            if (log.isTraceEnable()) {
                log.info(this, "Process[" + procState.getPid() + "] CPU Usage is " + cpu + "%, watermark="
                        + this.recordToLimitation);
            }

            return false;
        }

        @Override
        public void doLimitation() {

            log.warn(this, "Kill Process[" + procState.getPid() + "] as CPU Usage is over limitation");

            OSProcessHelper.killProcess(procState.getPid());
        }
    }

    /**
     * 
     * MemAuditChecker description: Memory Limitation
     *
     */
    private class MemAuditChecker extends AuditChecker {

        @Override
        public ResourceType getResourceType() {

            return ResourceType.Memory;
        }

        @Override
        public boolean isOverLimit() {

            double mem = DataConvertHelper.toDouble(procState.getTags().get("mem"), -1);

            if (mem > this.rule.getResLimit()) {
                log.warn(this, "Process[" + procState.getPid() + "] Memory Usage[" + Math.round(mem / 1000)
                        + "] is over " + Math.round(this.rule.getResLimit() / 1000) + "M");

                return true;
            }

            if (log.isTraceEnable()) {
                log.info(this, "Process[" + procState.getPid() + "] Memory Usage is " + Math.round(mem / 1000) + "M");
            }

            return false;
        }

        @Override
        public void doLimitation() {

            log.warn(this, "Kill Process[" + procState.getPid() + "] as Memory Usage is over limitation");

            OSProcessHelper.killProcess(procState.getPid());
        }
    }

    /**
     * 
     * MemRateAuditChecker description: MemRate Limitation
     *
     */
    private class MemRateAuditChecker extends MemAuditChecker {

        @Override
        public ResourceType getResourceType() {

            return ResourceType.MemRate;
        }

        @Override
        public boolean isOverLimit() {

            double memRate = DataConvertHelper.toDouble(procState.getTags().get("memRate"), -1);

            if (memRate > this.rule.getResLimit()) {

                log.warn(this, "Process[" + procState.getPid() + "] Memory Usage Rate[" + memRate + "%] is over "
                        + this.rule.getResLimit() + "%");

                return true;
            }

            if (log.isTraceEnable()) {
                log.info(this, "Process[" + procState.getPid() + "] Memory Usage Rate is " + memRate + "%");
            }

            return false;
        }
    }

    public class ResourceCheckRule {

        protected double resLimit = 0;
        protected long timeRange = -1;
        protected long checkInterval = -1;
        protected ResourceType resType;

        public ResourceCheckRule(ResourceType rt, double resLimit) {
            this(rt, resLimit, -1, -1);
        }

        public ResourceCheckRule(ResourceType rt, double resLimit, long timeRange, long checkInterval) {
            this.resType = rt;
            this.resLimit = resLimit;
            this.timeRange = timeRange;
            this.checkInterval = checkInterval;
        }

        public double getResLimit() {

            return resLimit;
        }

        public void setResLimit(double resLimit) {

            this.resLimit = resLimit;
        }

        public long getTimeRange() {

            return timeRange;
        }

        public void setTimeRange(long timeRange) {

            this.timeRange = timeRange;
        }

        public long getCheckInterval() {

            return checkInterval;
        }

        public void setCheckInterval(long checkInterval) {

            this.checkInterval = checkInterval;
        }

        public ResourceType getResType() {

            return resType;
        }

        public void setResType(ResourceType resType) {

            this.resType = resType;
        }

        @Override
        public String toString() {

            return "resType=" + this.resType + ",resLimit=" + resLimit;
        }

    }

    public enum ResourceType {
        CPU("cpu"), Memory("mem"), MemRate("memRate");

        private String tag;

        private ResourceType(String s) {
            this.tag = s;
        }

        @Override
        public String toString() {

            return this.tag;
        }
    }

    private Map<ResourceType, AuditChecker> auditCheckerMap = new HashMap<ResourceType, AuditChecker>();

    public ResourceLimitationAuditor(String cName, String feature) {
        super(cName, feature);
        auditCheckerMap.put(ResourceType.CPU, new CPUAuditChecker());
        auditCheckerMap.put(ResourceType.Memory, new MemAuditChecker());
        auditCheckerMap.put(ResourceType.MemRate, new MemRateAuditChecker());
    }

    public void check(ResourceCheckRule... rules) {

        if (null == rules) {
            return;
        }

        AgentFeatureComponent afc = (AgentFeatureComponent) getConfigManager().getComponent("procscan",
                "ProcDetectAgent");

        if (afc != null) {

            String pid = JVMToolHelper.getCurrentProcId();

            OSProcess procState = (OSProcess) afc.exchange("procscan.query.procstate", pid);

            if (procState == null) {
                log.warn(this, "This component can't work as its dependent Feature[procscan] out of running");
                return;
            }

            for (ResourceCheckRule rt : rules) {

                AuditChecker ac = auditCheckerMap.get(rt.getResType());

                ac.setRule(rt);
                ac.setProcState(procState);

                try {
                    boolean checkResult = ac.isOverLimit();
                    if (checkResult) {
                        ac.doLimitation();
                    }
                }
                catch (Exception e) {
                    log.err(this, "RUN AuditChecker[" + ac.getResourceType() + "] FAIL:", e);
                }
            }

        }
    }
}
