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

package com.creditease.monitorframework.fat;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.creditease.monitor.captureframework.spi.CaptureContext;
import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.monitor.captureframework.spi.MonitorFactory;
import com.creditease.monitor.captureframework.spi.Monitor.CapturePhase;
import com.creditease.monitor.datastore.DataObserver;

/**
 * Servlet implementation class AppCustomMonitor
 */
public class AppCustomMonitor extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * @see javax.servlet.http.HttpServlet#HttpServlet()
     */
    public AppCustomMonitor() {
        super();

        /**
         * define an application monitor
         */
        Monitor monitor = MonitorFactory.instance().buildMonitor("AppCustomMonitor", null);
        /**
         * add application monitor element
         */
        monitor.getRepository().addElement("AppCustomMonitor_Counter", "AppCustomMonitor.doGet",
                "com.creditease.monitorframework.fat.appmonitor.MyAppMonitorCounterHandler");
        /**
         * install monitor to dataobserver
         */
        DataObserver.instance().installMonitor(monitor);
        /**
         * (optional)if wish to bind monitor to tomcat capture point at PRECAP, DOCAP
         */
        // MonitorFactory.instance().bindMonitorToServerCapPoint(CaptureConstants.CAPPOINT_TOMCAT_CONNECTOR, monitor,
        // CapturePhase.REPCAP);
        // MonitorFactory.instance().bindMonitorToServerCapPoint(CaptureConstants.CAPPOINT_TOMCAT_CONNECTOR, monitor,
        // CapturePhase.DOCAP);

    }

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest request,
     *      javax.servlet.http.HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        /**
         * get your defined application monitor
         */
        Monitor monitor = MonitorFactory.instance().getMonitor("AppCustomMonitor");
        /**
         * create CaptureContext to wrap anything wish to pass MyAppMonitorCounterHandler
         */
        CaptureContext context = monitor.getCaptureContext();
        context.put(HttpServletRequest.class, request);
        context.put(HttpServletResponse.class, response);
        /**
         * do DOCAP operation, then MyAppMonitorCounterHandler is notified to do the same operation
         */
        monitor.doCapture("AppCustomMonitor.doGet", context, CapturePhase.DOCAP);

        request.getSession().setAttribute("test", 1);
    }

    /**
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest request,
     *      javax.servlet.http.HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    }

}
