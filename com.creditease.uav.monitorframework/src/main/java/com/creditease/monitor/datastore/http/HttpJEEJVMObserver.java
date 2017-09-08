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

package com.creditease.monitor.datastore.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.JVMToolHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.UAVServer.ServerVendor;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.globalfilter.jee.AbsJEEGlobalFilterHandler;
import com.creditease.monitor.interceptframework.spi.InterceptContext;

public class HttpJEEJVMObserver extends AbsJEEGlobalFilterHandler {

    public HttpJEEJVMObserver(String id) {
        super(id);
    }

    @Override
    public String getContext() {

        return "com.creditease.uav/jvm";
    }

    @Override
    protected void doRequest(HttpServletRequest request, HttpServletResponse response, InterceptContext ic) {

        String action = request.getParameter("action");

        if (action.equalsIgnoreCase("ping")) {

            ServerVendor sv = (UAVServer.ServerVendor) UAVServer.instance()
                    .getServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR);

            this.writeResponseBody(response, sv.toString(), HttpServletResponse.SC_OK);
        }
        else if (action.equalsIgnoreCase("getSystemPro")) {
            System.setProperty("proc.ext.pid", JVMToolHelper.getCurrentProcId());
            this.writeResponseBody(response, JSONHelper.toString(System.getProperties()), HttpServletResponse.SC_OK);
        }
    }

    @Override
    protected void doResponse(HttpServletRequest request, HttpServletResponse response, InterceptContext ic) {

        // not implement
    }

}
