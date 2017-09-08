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

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.monitor.datastore.jmx.ProfileObserverMBean;
import com.creditease.monitor.globalfilter.jee.AbsJEEGlobalFilterHandler;
import com.creditease.monitor.interceptframework.spi.InterceptContext;

public class HttpJEEProfileObserver extends AbsJEEGlobalFilterHandler {

    private HttpDataObserverWorker worker;

    public HttpJEEProfileObserver(String id, HttpDataObserverWorker worker) {
        super(id);
        this.worker = worker;
    }

    @Override
    public String getContext() {

        return "com.creditease.uav/profile";
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void doRequest(HttpServletRequest request, HttpServletResponse response, InterceptContext ic) {

        String action = request.getParameter("action");

        if (action.equalsIgnoreCase("getProfileData")) {

            StringBuilder sb = new StringBuilder();

            Map<String, ProfileObserverMBean> profiles = worker.getProfileMBeans();

            sb.append("[");

            for (String id : profiles.keySet()) {
                ProfileObserverMBean pob = profiles.get(id);
                sb.append("{id:\"" + id + "\",data:" + pob.getData() + ",state:" + pob.getState() + ",update:"
                        + pob.isUpdate() + "},");
            }

            if (sb.length() > 1) {
                sb = sb.deleteCharAt(sb.length() - 1);
            }

            sb.append("]");

            this.writeResponseBody(response, sb.toString(), HttpServletResponse.SC_OK);
        }
        else if (action.equalsIgnoreCase("updateProfile")) {

            Map<String, ProfileObserverMBean> profiles = worker.getProfileMBeans();

            String updateStr = this.getRequestBodyAsString(request, "utf-8");

            Map<String, Boolean> pUpdate = JSONHelper.toObject(updateStr, Map.class);

            for (String profileId : pUpdate.keySet()) {

                Boolean update = pUpdate.get(profileId);

                ProfileObserverMBean p = profiles.get(profileId);

                p.setUpdate(update);
            }

            this.writeResponseBody(response, "", HttpServletResponse.SC_OK);
        }
    }

    @Override
    protected void doResponse(HttpServletRequest request, HttpServletResponse response, InterceptContext ic) {

        // not implement
    }

}
