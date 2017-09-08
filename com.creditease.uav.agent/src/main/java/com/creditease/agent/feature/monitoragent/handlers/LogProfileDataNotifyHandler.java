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

package com.creditease.agent.feature.monitoragent.handlers;

import com.creditease.agent.feature.monitoragent.detect.DetectorManager;
import com.creditease.agent.helpers.jvmtool.JVMAgentInfo;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.agent.spi.AbstractHandler;
import com.creditease.agent.spi.AgentFeatureComponent;

/**
 * ProfileDataNotifyHandler helps to notify logAgent profileData change
 * 
 * @author hongqiang wei modified by alexzan
 *
 */
public class LogProfileDataNotifyHandler extends AbstractHandler<MonitorDataFrame> {

    public LogProfileDataNotifyHandler(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public void handle(MonitorDataFrame profileData) {

        /**
         * find out the logagent feature component and exchange the MDF
         */
        AgentFeatureComponent afc = (AgentFeatureComponent) this.getConfigManager().getComponent("logagent",
                "LogAgent");

        /**
         * NOTE：both JMX and Http Scan supported
         */
        DetectorManager asmd = (DetectorManager) this.getConfigManager().getComponent(this.feature,
                "AppServerMonitorDetector_TimerWorker");

        if (null != afc && null != asmd) {

            JVMAgentInfo jvmAgentInfo = asmd.getJVMAgentInfo(profileData.getServerId());

            if (null == jvmAgentInfo) {
                log.warn(this, "can't find jvm agent info for serverid [" + profileData.getServerId()
                        + "], the jvm may be dead.");
                return;
            }

            afc.exchange("logagent.profiledata.notify", profileData, jvmAgentInfo);
        }
        else {
            log.warn(this, "can't find agent feature component [logagent], this feature may not start. LogAgent-null("
                    + (afc == null) + "), DetectorManager-null(" + (asmd == null) + ")");
        }
    }
}
