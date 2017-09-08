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

package com.creditease.monitor.globalfilter.jee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.creditease.monitor.globalfilter.AbsGlobalFilterHandler;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;

/**
 * 
 * AbsJEEGlobalFilterHandler description: for JEE Application, using Filter Mechanism to implement GlobalFilter
 *
 */
public abstract class AbsJEEGlobalFilterHandler
        extends AbsGlobalFilterHandler<HttpServletRequest, HttpServletResponse> {

    public AbsJEEGlobalFilterHandler(String id) {
        super(id);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, InterceptContext ic) {

        if (ic.getEvent() == Event.GLOBAL_FILTER_REQUEST) {
            this.doRequest(request, response, ic);
        }
        else if (ic.getEvent() == Event.GLOBAL_FILTER_RESPONSE) {
            this.doResponse(request, response, ic);
        }
    }

    protected abstract void doRequest(HttpServletRequest request, HttpServletResponse response, InterceptContext ic);

    protected abstract void doResponse(HttpServletRequest request, HttpServletResponse response, InterceptContext ic);

    /**
     * writeResponseBody
     * 
     * @param response
     * @param data
     * @param returnCode
     */
    protected void writeResponseBody(HttpServletResponse response, String data, int returnCode) {

        writeResponseBody(response, data, returnCode, "utf-8");
    }

    protected void writeResponseBody(HttpServletResponse response, String data, int returnCode, String encoding) {

        response.setCharacterEncoding(encoding);
        response.setStatus(returnCode);

        try {
            PrintWriter pw = response.getWriter();
            pw.write(data);
            pw.flush();
            pw.close();
        }
        catch (IOException e) {
            logger.error("BaseGlobalFilterHandler[" + getContext() + "] writes response FAIL.", e);
        }

    }

    /**
     * getRequestBodyAsString
     * 
     * @param request
     * @param encoding
     * @return
     */
    protected String getRequestBodyAsString(HttpServletRequest request, String encoding) {

        StringBuilder resp = new StringBuilder();
        BufferedReader in = null;
        try {
            InputStream currReplyData = request.getInputStream();
            String line;
            in = new BufferedReader(new InputStreamReader(currReplyData, encoding));
            while ((line = in.readLine()) != null) {
                resp.append(line);
            }
        }
        catch (IOException e) {
            // ignore
        }
        finally {
            try {
                in.close();
            }
            catch (IOException e) {
                // ignore
            }
        }
        return resp.toString();
    }
}
