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

package com.creditease.uav.mscp.plus;

import java.util.HashMap;
import java.util.Map;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.spi.AbstractBaseHttpServComponent;
import com.creditease.agent.spi.HttpMessage;
import com.creditease.agent.spi.IConfigurationManager;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.monitor.interceptframework.InterceptSupport;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;

public class MSCPPlusIT {

    /**
     * startUAVServer
     */
    public void startServer() {

        // integrate Tomcat log
        UAVServer.instance().setLog(new MSCPLog("MonitorServer"));
        // start Monitor Server when server starts
        UAVServer.instance().start(new Object[] { UAVServer.ServerVendor.MSCP });

        // take the operation port as the default port
        UAVServer.instance().putServerInfo(CaptureConstants.INFO_APPSERVER_LISTEN_PORT,
                Integer.parseInt(System.getProperty("JAppOperPort")));
    }

    /**
     * onServiceInit
     * 
     * @param args
     */
    public void onServiceInit(Object... args) {

    }

    /**
     * onServiceStart
     * 
     * @param args
     */
    public void onServiceStart(Object... args) {

        // on service start pre-cap
        UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_SERVER_CONNECTOR,
                Monitor.CapturePhase.PRECAP, null);
    }

    /**
     * onServiceEnd
     * 
     * @param args
     */
    @SuppressWarnings("rawtypes")
    public void onServiceEnd(Object... args) {

        AbstractBaseHttpServComponent abhsc = (AbstractBaseHttpServComponent) args[0];

        HttpMessage message = (HttpMessage) args[1];

        String reqURL = abhsc.getHttpRootURL() + message.getContextPath();

        Map<String, Object> params = new HashMap<String, Object>();
        try {
            params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_REQUEST_URL, reqURL);
        }
        catch (Exception e) {
        }
        try {
            params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_SERVLET, reqURL);
        }
        catch (Exception e) {
        }
        try {
            params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_CONTEXT, "");
        }
        catch (Exception e) {
        }
        try {
            params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_CONTEXT_REALPATH, "/" + System.getProperty("JAppID"));
        }
        catch (Exception e) {
        }
        try {
            params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_RESPONSECODE, message.getResponseCode());
        }
        catch (Exception e) {
        }
        try {
            params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_FORWARDADDR, message.getHeader("X-Forwarded-For"));
        }
        catch (Exception e) {
        }
        try {
            params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_LISTENPORT, System.getProperty("JAppOperPort"));
        }
        catch (Exception e) {
        }
        try {
            params.put(CaptureConstants.INFO_APPSERVER_CLIENT_USRAGENT, message.getHeader("User-Agent"));
        }
        catch (Exception e) {
        }
        try {
            params.put(CaptureConstants.INFO_APPSERVER_UAVCLIENT_TAG, message.getHeader("UAV-Client-Src"));
        }
        catch (Exception e) {
        }
        try {
            params.put(CaptureConstants.INFO_APPSERVER_PROXY_HOST, message.getHeader("Host"));
        }
        catch (Exception e) {
        }
        try {
            params.put(CaptureConstants.INFO_APPSERVER_CONNECTOR_CLIENTADDR, message.getClientAddress());
        }
        catch (Exception e) {
        }
        UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_SERVER_CONNECTOR,
                Monitor.CapturePhase.DOCAP, params);
    }

    /**
     * onAppStarting
     * 
     * @param args
     */
    public void onAppStarting() {

        IConfigurationManager cm = ConfigurationManager.getInstance();

        InterceptSupport iSupport = InterceptSupport.instance();
        InterceptContext context = iSupport.createInterceptContext(Event.WEBCONTAINER_INIT);
        context.put(InterceptConstants.WEBAPPLOADER, this.getClass().getClassLoader());
        context.put(InterceptConstants.WEBWORKDIR, cm.getContext(IConfigurationManager.ROOT));
        context.put(InterceptConstants.CONTEXTPATH, "");
        context.put(InterceptConstants.APPNAME, cm.getContext(IConfigurationManager.NODEAPPID));
        context.put(InterceptConstants.BASEPATH, "/" + cm.getContext(IConfigurationManager.NODEAPPID));

        iSupport.doIntercept(context);
    }

    /**
     * onAppStart
     * 
     * @param args
     */
    public void onAppStart() {

        // String featureName = (String) args[0];

        IConfigurationManager cm = ConfigurationManager.getInstance();

        InterceptSupport iSupport = InterceptSupport.instance();
        InterceptContext context = iSupport.createInterceptContext(Event.WEBCONTAINER_STARTED);
        context.put(InterceptConstants.WEBAPPLOADER, this.getClass().getClassLoader());
        context.put(InterceptConstants.WEBWORKDIR, cm.getContext(IConfigurationManager.ROOT));
        context.put(InterceptConstants.CONTEXTPATH, "");
        context.put(InterceptConstants.APPNAME, cm.getContext(IConfigurationManager.NODEAPPID));
        context.put(InterceptConstants.BASEPATH, "/" + cm.getContext(IConfigurationManager.NODEAPPID));

        iSupport.doIntercept(context);
    }

    /**
     * onAppStop
     * 
     * @param args
     */
    public void onAppStop() {

        // TODO:
    }

}
