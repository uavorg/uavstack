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

package com.creditease.agent.feature.monitoragent.datacatch;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.creditease.agent.feature.monitoragent.detect.BaseDetector;
import com.creditease.agent.helpers.JVMToolHelper;
import com.creditease.agent.helpers.ThreadHelper;
import com.creditease.agent.helpers.jvmtool.JVMAgentInfo;

public abstract class BaseJMXMonitorDataCatchWorker extends BaseMonitorDataCatchWorker {

    protected JMXServiceURL JMXConnectionURL;
    protected MBeanServerConnection mbsc;
    protected JMXConnector mbscConnector;

    public BaseJMXMonitorDataCatchWorker(String cName, String feature, JVMAgentInfo appServerInfo,
            BaseDetector detector) {
        super(cName, feature, appServerInfo, detector);
    }

    /**
     * scan out all monitor MBeans
     *
     * @param mbsc
     * @return
     * @throws IOException
     */
    protected Set<ObjectInstance> scanMBeans(MBeanServerConnection mbsc, String pattern) throws IOException {

        Set<ObjectInstance> monitorMBeans = null;
        int count = 2;
        while (count > 0) {
            try {
                monitorMBeans = mbsc.queryMBeans(new ObjectName(pattern), null);

                if (monitorMBeans == null || monitorMBeans.isEmpty()) {
                    ThreadHelper.suspend(2000);
                }
                else {
                    break;
                }

            }
            catch (MalformedObjectNameException e) {
                // ignore
            }
            count--;
        }

        return monitorMBeans;
    }

    @Override
    public void updateAttrs(JVMAgentInfo appServerInfo) {

        try {
            String jmxConnectAddress = appServerInfo.getAgentProperties()
                    .getProperty(JVMToolHelper.JMX_CONNECTOR_ADDRESS);

            appServerInfo.setJVMAccessURL(jmxConnectAddress);

            this.JMXConnectionURL = new JMXServiceURL(appServerInfo.getJVMAccessURL());
        }
        catch (MalformedURLException e) {
            log.err(this, "JMX Connection URL [" + appServerInfo.getJVMAccessURL() + "] is WRONG");
        }
    }

    @Override
    public boolean isVMAlive(JVMAgentInfo appServerInfo) {

        return JVMToolHelper.isVMAlive(this.appServerInfo.getId());
    }

    @Override
    public int start() {

        try {
            this.mbscConnector = JMXConnectorFactory.connect(this.JMXConnectionURL);

            this.mbsc = mbscConnector.getMBeanServerConnection();

            return 1;
        }
        catch (Exception e) {

            log.err(this,
                    "Worker[" + this.cName + "] Connect to JMXURL[" + this.JMXConnectionURL.toString() + "] FAIL:", e);

            return -1;
        }
    }

    @Override
    public void cancel() {

        super.cancel();

        if (this.mbscConnector != null) {
            try {
                this.mbscConnector.close();
            }
            catch (IOException e) {
                log.err(this, "Worker[" + this.cName + "] Close FAIL:", e);
            }
        }
    }

}
