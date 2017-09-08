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

import com.creditease.agent.helpers.JVMToolHelper;
import com.creditease.agent.helpers.NetworkHelper;
import com.creditease.agent.helpers.RuntimeHelper;
import com.creditease.agent.spi.AbstractTimerWork;
import com.creditease.agent.spi.AgentFeatureComponent;

public class NetworkIoDetector extends AbstractTimerWork {

    private String portList;

    public NetworkIoDetector(String cName, String feature) {
        super(cName, feature);
    }

    private int countDown = 0;

    @Override
    public void run() {

        /**
         * NOTE: not support windows yet
         */
        if (JVMToolHelper.isWindows()) {
            return;
        }
        if (countDown != 0) {
            countDown--;
            return;
        }
        if (portList != null) {
            try {
                String networkdetectTime = this.getConfigManager().getFeatureConfiguration(this.feature,
                        "networkDetect.collectTime");
                String ip = NetworkHelper.getLocalIP();
                String netcardName = NetworkHelper.getNetCardName(ip);
                String command = "cd bin; sh networkIoDetect.sh " + netcardName + " " + ip + " " + networkdetectTime
                        + " " + portList;

                String result = RuntimeHelper.exec(10000, "/bin/sh", "-c", command);
                if (!result.contains("in_") || result.toLowerCase().contains("error")
                        || result.toLowerCase().contains("traceback")) {
                    log.err(this, "NetworkIo Monitor runs FAIL with TechError: error=" + result);
                    countDown = 100;
                    return;
                }

                AgentFeatureComponent afc = (AgentFeatureComponent) this.getConfigManager().getComponent(this.feature,
                        "ProcDetectAgent");
                if (null != afc) {
                    afc.exchange("procscan.nodeinfo.portFlux", result, System.currentTimeMillis());
                }

                if (log.isDebugEnable()) {
                    log.debug(this, "NetworkIo Monitor Result: " + result);
                }

            }
            catch (Exception e) {
                log.err(this, "NetworkIo Monitor runs FAIL.", e);
                countDown = 100;
            }
        }
    }

    public void setPortList(String portList) {

        this.portList = portList;
    }

}
