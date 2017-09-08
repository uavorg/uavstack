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

import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.creditease.agent.helpers.JVMToolHelper;
import com.creditease.agent.helpers.jvmtool.JVMAgentInfo;
import com.creditease.agent.helpers.jvmtool.JVMPropertyFilter;

/**
 * 
 * JVMLocalOSDetector description: scan all jvm on this OS
 *
 */
public class JVMLocalOSDetector extends BaseDetector {

    private String[] excludeJavaProc = new String[] { "com.sun.tools", "sun.tools", "jenkins-cli.jar",
            "com.creditease.agent.feature.nodeopagent.NodeOperCtrlClient" };

    public JVMLocalOSDetector(String cName, String feature, String initHandlerKey, long detectInterval) {
        super(cName, feature, initHandlerKey, detectInterval);

        String jvmFiltersStr = this.getConfigManager().getFeatureConfiguration(this.feature, "jvm.filters");

        if (jvmFiltersStr != null) {
            excludeJavaProc = jvmFiltersStr.split(",");
        }
    }

    @Override
    protected String matchJVMType(JVMAgentInfo appServerInfo) throws RuntimeException {

        // start data catch worker
        String jmxConnectAddress = appServerInfo.getAgentProperties().getProperty(JVMToolHelper.JMX_CONNECTOR_ADDRESS);

        if (jmxConnectAddress == null) {
            throw new RuntimeException("No JMX URL available access!");
        }

        return super.matchJVMType(appServerInfo);
    }

    @Override
    public void run() {

        /**
         * step 1: build the skip local attach support set which contains those jvm with management.jar attached
         */
        Set<String> skipLocalAttachSupportSet = new HashSet<String>();

        for (JVMAgentInfo agentInfo : jvmAgentInfos.values()) {

            skipLocalAttachSupportSet.add(agentInfo.getId());
        }

        /**
         * step 2: scan out all java processes
         */
        List<JVMAgentInfo> appServerJVMInfos = JVMToolHelper.getAllLocalJvmInfo(new JVMPropertyFilter() {

            @Override
            public boolean isMatchAgentProperties(Properties jvmProperties) {

                /**
                 * exclude all java tools
                 */
                Enumeration<?> e = jvmProperties.propertyNames();

                while (e.hasMoreElements()) {
                    String key = (String) e.nextElement();
                    String val = jvmProperties.getProperty(key);

                    for (String t : excludeJavaProc) {
                        if (val.indexOf(t) > -1) {
                            return false;
                        }
                    }

                }

                return true;
            }

            @Override
            public boolean isMatchSystemProperties(Properties systemProperties) {

                return true;
            }

        }, true, skipLocalAttachSupportSet);

        if (appServerJVMInfos.isEmpty()) {
            return;
        }

        /**
         * step 3: see if need start a new worker to catch data for each JVM
         */
        for (JVMAgentInfo appServerInfo : appServerJVMInfos) {

            try {
                addWorker(appServerInfo);
            }
            catch (Exception e) {
                log.err(this, "JVMLocalOSDetector[" + this.cName + "] start worker FAIL: java process id="
                        + appServerInfo.getId(), e);
            }
        }

    }
}
