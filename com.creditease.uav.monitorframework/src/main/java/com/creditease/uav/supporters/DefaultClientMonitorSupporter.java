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

import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.monitor.captureframework.spi.Monitor.CapturePhase;
import com.creditease.monitor.captureframework.spi.MonitorFactory;
import com.creditease.monitor.datastore.DataObserver;
import com.creditease.uav.common.Supporter;

public class DefaultClientMonitorSupporter extends Supporter {

    private Monitor defaultClientMonitor = null;

    @Override
    public void start() {

        defaultClientMonitor = MonitorFactory.instance().buildMonitor(CaptureConstants.MONITOR_CLIENT, null);

        defaultClientMonitor.getRepository().addElement(CaptureConstants.MOELEM_CLIENT_RESPTIME,
                CaptureConstants.CAPPOINT_APP_CLIENT, "com.creditease.monitor.handlers.ClientRespTimeCapHandler");

        /**
         * install monitor to dataobserver
         */
        DataObserver.instance().installMonitor(defaultClientMonitor);

        MonitorFactory.instance().bindMonitorToServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                defaultClientMonitor, CapturePhase.PRECAP);
        MonitorFactory.instance().bindMonitorToServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                defaultClientMonitor, CapturePhase.DOCAP);

    }

    @Override
    public void stop() {

        // destrory monitor
        if (defaultClientMonitor != null) {
            defaultClientMonitor.destroy();
        }

        super.stop();
    }

}
