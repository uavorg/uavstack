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

package com.creditease.uav.apm.uem;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.creditease.agent.helpers.IOHelper;
import com.creditease.monitor.globalfilter.jee.AbsJEEGlobalFilterHandler;
import com.creditease.monitor.interceptframework.spi.InterceptContext;

public class UEMHookJSGHHandler extends AbsJEEGlobalFilterHandler {

    public UEMHookJSGHHandler(String id) {
        super(id);
    }

    @Override
    protected void doRequest(HttpServletRequest request, HttpServletResponse response, InterceptContext ic) {

        String uavMofRoot = System.getProperty("com.creditease.uav.uavmof.root");

        String uemHookJS = uavMofRoot + "/com.creditease.uav/com.creditease.uav.uemhook-1.0.jsx";

        String js = IOHelper.readTxtFile(uemHookJS, "utf-8");

        js = js.replace("{$Server}", System.getProperty("com.creditease.uav.uem.server", ""));

        this.writeResponseBody(response, js, 200);
    }

    @Override
    protected void doResponse(HttpServletRequest request, HttpServletResponse response, InterceptContext ic) {

    }

    @Override
    public String getContext() {

        return "com.creditease.uav/com.creditease.uav.uemhook.jsx";
    }

}
