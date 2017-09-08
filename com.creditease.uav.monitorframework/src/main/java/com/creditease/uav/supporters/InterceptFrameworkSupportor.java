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

import com.creditease.monitor.UAVServer;
import com.creditease.monitor.UAVServer.ServerVendor;
import com.creditease.monitor.captureframework.MonitorUrlFilterMgr;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.globalfilter.jee.UAVServerJEEController;
import com.creditease.monitor.interceptframework.InterceptSupport;
import com.creditease.monitor.interceptframework.spi.InterceptEventListener;
import com.creditease.uav.appserver.listeners.GlobalFilterDispatchListener;
import com.creditease.uav.common.Supporter;

public class InterceptFrameworkSupportor extends Supporter {

    @Override
    public void start() {

        String listenersString = System.getProperty("com.creditease.uav.interceptlisteners");

        if (null == listenersString) {
            return;
        }

        String[] defaultListeners = listenersString.split(",");

        // Step 1: install intercept listeners
        InterceptSupport is = InterceptSupport.instance();
        for (String listenerClass : defaultListeners) {
            InterceptEventListener listener = is.createInterceptEventListener(listenerClass);
            if (listener != null) {
                if (this.logger.isLogEnabled()) {
                    this.logger.info("InterceptEventListener[" + listenerClass + "] load SUCCESS");
                }
                is.addEventListener(listener);
            }
        }

        // Step 2: register UAVServerController
        ServerVendor vendor = (ServerVendor) UAVServer.instance().getServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR);

        GlobalFilterDispatchListener listener = (GlobalFilterDispatchListener) InterceptSupport.instance()
                .getEventListener(GlobalFilterDispatchListener.class);
        // MSCP
        if (vendor == ServerVendor.MSCP) {
            // TODO
        }
        // JEE
        else {
            listener.registerHandler(new UAVServerJEEController("UAVServerJEEController"));
        }
        // init MonitorUrlFilterMgr
        MonitorUrlFilterMgr.getInstance().init();
    }

    @Override
    public void stop() {

        InterceptSupport is = InterceptSupport.instance();
        is.clearListeners();

        super.stop();
    }

}
