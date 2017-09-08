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

package com.creditease.monitor.handlers;

import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.CaptureContext;
import com.creditease.monitor.captureframework.spi.MonitorElemCapHandler;
import com.creditease.monitor.captureframework.spi.MonitorElement;
import com.creditease.monitor.captureframework.spi.MonitorElementInstance;

/**
 * NoHttpServiceRespTimeCapHandler helps to record none-http protocol such as dubbo, thirft, etc.
 * 
 * @author zhenzhang18
 *
 */
public class NoHttpServiceRespTimeCapHandler extends AbsServerRespTimeCapHandler implements MonitorElemCapHandler {

    @Override
    public void preCap(MonitorElement elem, CaptureContext context) {

        // no need implementation, as ServerEndRespTimeCapHandler done
    }

    @Override
    public void doCap(MonitorElement elem, CaptureContext context) {

        String urlInfo = (String) context.get(CaptureConstants.INFO_APPSERVER_CONNECTOR_REQUEST_URL);

        if (urlInfo == null) {
            return;
        }

        MonitorElementInstance inst = null;

        if (CaptureConstants.MOELEM_SERVER_RESPTIME_URL.equals(elem.getMonitorElemId())) {
            inst = elem.getInstance(urlInfo);
        }

        recordCounters(context, inst);
    }

    @Override
    public void preStore(MonitorElementInstance instance) {

        // no need implementation, as ServerEndRespTimeCapHandler done
    }

}
