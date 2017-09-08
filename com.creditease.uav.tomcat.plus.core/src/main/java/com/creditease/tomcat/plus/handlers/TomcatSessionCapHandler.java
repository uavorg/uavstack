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

package com.creditease.tomcat.plus.handlers;

import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.MonitorElementInstance;
import com.creditease.monitor.handlers.ServerEndRespTimeCapHandler;
import com.creditease.uav.util.MonitorServerUtil;

public class TomcatSessionCapHandler extends ServerEndRespTimeCapHandler {

    private MBeanServer server = MonitorServerUtil.getMBeanServer();

    @Override
    public void preStore(MonitorElementInstance instance) {

        super.preStore(instance);

        if (CaptureConstants.MOELEM_SERVER_RESPTIME_APP.equals(instance.getMonitorElement().getMonitorElemId())) {

            try {
                Set<ObjectInstance> mbeans = server.queryMBeans(
                        new ObjectName("Catalina:type=Manager,path=" + instance.getInstanceId() + ",*"), null);
                if (mbeans == null || mbeans.size() != 1) {
                    return;
                }

                ObjectName on = mbeans.iterator().next().getObjectName();

                instance.setValue("host", on.getKeyProperty("host"));
                instance.setValue("sesActive", server.getAttribute(on, "activeSessions"));
                instance.setValue("sesMax", server.getAttribute(on, "maxActive"));
                instance.setValue("sesTotal", server.getAttribute(on, "sessionCounter"));
                instance.setValue("sesCR", server.getAttribute(on, "sessionCreateRate"));
                instance.setValue("sesER", server.getAttribute(on, "sessionExpireRate"));
                instance.setValue("sesAvgTime", server.getAttribute(on, "sessionAverageAliveTime"));
                instance.setValue("sesMaxTime", server.getAttribute(on, "sessionMaxAliveTime"));
            }
            catch (Exception e) {
                // ignore
            }
        }
    }

}
