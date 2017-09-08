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

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.creditease.monitor.globalfilter.jee.AbsJEEGlobalFilterHandler;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.uav.util.MonitorServerUtil;

/**
 * 
 * UEMRewritePageGHHandler description: rewrite the response page in order to inject the hook.js
 *
 */
public class UEMRewritePageGHHandler extends AbsJEEGlobalFilterHandler {

    private static String[] acceptMIME = new String[] { "text/html", "application/xhtml+xml" };

    public UEMRewritePageGHHandler(String id) {
        super(id);
    }

    @Override
    protected void doRequest(HttpServletRequest request, HttpServletResponse response, InterceptContext ic) {

        if (!this.isPage(request)) {
            return;
        }

        RewriteResponseWrapper drw = new RewriteResponseWrapper(response, "UEM");

        ic.put(InterceptConstants.HTTPRESPONSE, drw);
    }

    /**
     * isPage
     * 
     * @param request
     * @return
     */
    private boolean isPage(HttpServletRequest request) {

        /**
         * Step 1: check if is html,htm,jsp
         */
        String url = request.getRequestURL().toString();

        if (MonitorServerUtil.isIncludeMonitorURLForPage(url)) {
            return true;
        }

        /**
         * Step 2: if has no extension, check if accept is a page style
         */
        Enumeration<String> accepts = request.getHeaders("Accept");

        while (accepts.hasMoreElements()) {

            String accept = accepts.nextElement();

            for (String mime : acceptMIME) {
                if (accept.indexOf(mime) > -1) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected void doResponse(HttpServletRequest request, HttpServletResponse response, InterceptContext ic) {

        if (!RewriteResponseWrapper.class.isAssignableFrom(response.getClass())) {
            return;
        }

        RewriteResponseWrapper drw = (RewriteResponseWrapper) response;

        if (!"UEM".equals(drw.getTag())) {
            return;
        }

        StringBuilder sb = drw.getContent();

        int index = sb.indexOf("<head>");

        if (index == -1) {
            index = sb.indexOf("<HEAD>");
        }

        if (index > -1) {
            String jsHook = "<script type=\"text/javascript\" src=\"/com.creditease.uav/com.creditease.uav.uemhook.jsx\"></script>";

            sb.insert(index + 6, jsHook);
        }

        drw.doRealFlush();
    }

    @Override
    public String getContext() {

        return "";
    }

    @Override
    public boolean isBlockHandlerChain() {

        return false;
    }

    @Override
    public boolean isBlockFilterChain() {

        return false;
    }

}
