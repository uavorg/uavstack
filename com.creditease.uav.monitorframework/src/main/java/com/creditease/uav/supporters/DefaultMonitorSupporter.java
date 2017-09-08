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

package com.creditease.uav.supporters;

import com.creditease.monitor.UAVServer.ServerVendor;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.monitor.captureframework.spi.Monitor.CapturePhase;
import com.creditease.monitor.captureframework.spi.MonitorFactory;
import com.creditease.monitor.datastore.DataObserver;
import com.creditease.uav.common.Supporter;

public class DefaultMonitorSupporter extends Supporter {

    private Monitor defaultMonitor = null;

    @Override
    public void start() {

        ServerVendor vendor = (ServerVendor) this.getServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR);

        defaultMonitor = MonitorFactory.instance().buildDefaultMonitor(null);
        /**
         * System,URL,Application's RespTime & LoadCounter
         */
        defaultMonitor.getRepository().addElement(CaptureConstants.MOELEM_SERVER_RESPTIME_SYSTEM,
                CaptureConstants.CAPPOINT_SERVER_CONNECTOR,
                "com.creditease.monitor.handlers.ServerEndRespTimeCapHandler");
        defaultMonitor.getRepository().addElement(CaptureConstants.MOELEM_SERVER_RESPTIME_APP,
                CaptureConstants.CAPPOINT_SERVER_CONNECTOR,
                "com.creditease.monitor.handlers.ServerEndRespTimeCapHandler");
        defaultMonitor.getRepository().addElement(CaptureConstants.MOELEM_SERVER_RESPTIME_URL,
                CaptureConstants.CAPPOINT_SERVER_CONNECTOR,
                "com.creditease.monitor.handlers.ServerEndRespTimeCapHandler");

        /**
         * for Tomcat need TomcatSessionCapHandler
         */
        if (vendor == ServerVendor.TOMCAT || vendor == ServerVendor.SPRINGBOOT) {
            // MonitorElement[] appMoElem = defaultMonitor.getRepository().getElementByMoElemIdAndCapId(
            // CaptureConstants.MOELEM_SERVER_RESPTIME_APP, CaptureConstants.CAPPOINT_SERVER_CONNECTOR);
            // appMoElem[0].setCapClass("com.creditease.tomcat.plus.handlers.TomcatSessionCapHandler");
        }

        /**
         * JVM state
         */
        defaultMonitor.getRepository().addElement(CaptureConstants.MOELEM_JVMSTATE,
                CaptureConstants.CAPPOINT_MONITOR_DATAOBSERVER, "com.creditease.monitor.handlers.JVMStateCapHandler");

        /**
         * install monitor to dataobserver
         */
        DataObserver.instance().installMonitor(defaultMonitor);

        // bind default monitor to server capture endpoint
        MonitorFactory.instance().bindMonitorToServerCapPoint(CaptureConstants.CAPPOINT_SERVER_CONNECTOR,
                defaultMonitor, CapturePhase.PRECAP);
        MonitorFactory.instance().bindMonitorToServerCapPoint(CaptureConstants.CAPPOINT_SERVER_CONNECTOR,
                defaultMonitor, CapturePhase.DOCAP);
    }

    @Override
    public void stop() {

        // stop the data observer when server stops
        DataObserver dataObserver = DataObserver.instance();
        dataObserver.stop();

        // destrory monitor
        if (defaultMonitor != null) {
            defaultMonitor.destroy();
        }

        super.stop();
    }

}
