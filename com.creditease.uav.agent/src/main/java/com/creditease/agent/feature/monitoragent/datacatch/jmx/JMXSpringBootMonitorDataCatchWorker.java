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

package com.creditease.agent.feature.monitoragent.datacatch.jmx;

import com.creditease.agent.feature.monitoragent.detect.BaseDetector;
import com.creditease.agent.helpers.jvmtool.JVMAgentInfo;

public class JMXSpringBootMonitorDataCatchWorker extends JMXAppServerMonitorDataCatchWorker {

    public JMXSpringBootMonitorDataCatchWorker(String cName, String feature, JVMAgentInfo appServerInfo,
            BaseDetector detector) {
        super(cName, feature, appServerInfo, detector);
    }

    @Override
    public String getWorkerId() {

        String tomcatHome = this.appServerInfo.getSystemProperties().getProperty("catalina.home");
        String tomcatBase = this.appServerInfo.getSystemProperties().getProperty("catalina.base");
        String tomcatInstanceId = tomcatHome + "---" + tomcatBase;

        tomcatInstanceId = tomcatInstanceId.replace("\\", "/");

        return tomcatInstanceId;
    }

    @Override
    public String getProcessInfoName() {

        return "SpringBoot[" + this.getWorkerId() + "]";
    }
}
