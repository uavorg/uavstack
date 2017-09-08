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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.creditease.monitor.datastore.jmx.MonitorObserverMBean;
import com.creditease.monitor.globalfilter.jee.AbsJEEGlobalFilterHandler;
import com.creditease.monitor.interceptframework.spi.InterceptContext;

public class HttpJEEMonitorObserver extends AbsJEEGlobalFilterHandler {

    private HttpDataObserverWorker worker;

    public HttpJEEMonitorObserver(String id, HttpDataObserverWorker worker) {
        super(id);
        this.worker = worker;
    }

    @Override
    public String getContext() {

        return "com.creditease.uav/monitor";
    }

    @Override
    protected void doRequest(HttpServletRequest request, HttpServletResponse response, InterceptContext ic) {

        String action = request.getParameter("action");

        if (action.equalsIgnoreCase("getMonitorData")) {

            StringBuilder sb = new StringBuilder();

            Map<String, MonitorObserverMBean> monitors = worker.getMonitorMBeans();

            sb.append("[");

            for (String id : monitors.keySet()) {
                sb.append("{id:\"" + id + "\",data:" + monitors.get(id).getData() + "},");
            }

            if (sb.length() > 1) {
                sb = sb.deleteCharAt(sb.length() - 1);
            }

            sb.append("]");

            this.writeResponseBody(response, sb.toString(), HttpServletResponse.SC_OK);
        }
    }

    @Override
    protected void doResponse(HttpServletRequest request, HttpServletResponse response, InterceptContext ic) {

        // not implementation
    }

}
