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

package com.creditease.uav.hook.httpclients3.sync.interceptors;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.URIException;

import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.common.BaseComponent;
import com.creditease.uav.hook.httpclients3.sync.invokeChain.ApacheHttpClient3Adapter;
import com.creditease.uav.util.MonitorServerUtil;

/**
 * 
 * HttpClient3Proxy description: this is the real work class who does all interceptions
 *
 */
public class ApacheHttpClient3IT extends BaseComponent {

    private static ThreadLocal<ApacheHttpClient3IT> tl = new ThreadLocal<ApacheHttpClient3IT>();

    /**
     * use threadlocal to pass the ApacheHttpClient3IT Object is more safe with bytecode weave
     * 
     * @param appid
     * @param args
     */
    public static void start(String appid, Object[] args) {

        ApacheHttpClient3IT m = new ApacheHttpClient3IT(appid);
        tl.set(m);

        m.doStart(args);
    }

    /**
     * use threadlocal to pass the ApacheHttpClient3IT Object is more safe with bytecode weave
     * 
     * @param args
     */
    public static void end(Object[] args) {

        ApacheHttpClient3IT m = tl.get();

        m.doEnd(args);

        tl.remove();
    }

    private String applicationId;
    private String targetURL = "";
    private Map<String, Object> ivcContextParams = new HashMap<String, Object>();

    public ApacheHttpClient3IT(String appid) {
        this.applicationId = appid;
    }

    /**
     * for http client
     * 
     * @param args
     * @return
     */
    @SuppressWarnings({ "unused", "unchecked" })
    public void doStart(Object[] args) {

        HostConfiguration hostconfig = (HostConfiguration) args[0];
        HttpMethod method = (HttpMethod) args[1];
        HttpState state = (HttpState) args[2];

        String httpAction = "";
        method.setRequestHeader("UAV-Client-Src", MonitorServerUtil.getUAVClientSrc(this.applicationId));

        try {
            httpAction = method.getName();
            targetURL = method.getURI().toString();

            // HttpMethod中可能不包含ip:port，需要从httpHost中拿到再拼接
            if (!targetURL.startsWith("http")) {
                targetURL = hostconfig.getHostURL() + targetURL;
            }
        }
        catch (URIException e) {
            // ignore
        }

        Map<String, Object> params = new HashMap<String, Object>();

        params.put(CaptureConstants.INFO_CLIENT_REQUEST_URL, targetURL);
        params.put(CaptureConstants.INFO_CLIENT_REQUEST_ACTION, httpAction);
        params.put(CaptureConstants.INFO_CLIENT_APPID, this.applicationId);
        params.put(CaptureConstants.INFO_CLIENT_TYPE, "apache.http.Client");

        if (logger.isDebugable()) {
            logger.debug("Invoke START:" + targetURL + "," + httpAction + "," + this.applicationId, null);
        }

        UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                Monitor.CapturePhase.PRECAP, params);

        // register adapter
        UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "registerAdapter",
                ApacheHttpClient3Adapter.class);

        ivcContextParams = (Map<String, Object>) UAVServer.instance().runSupporter(
                "com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.PRECAP, params,
                ApacheHttpClient3Adapter.class, args);

    }

    /**
     * for http client
     * 
     * @param args
     * @return
     */
    public void doEnd(Object[] args) {

        Map<String, Object> params = new HashMap<String, Object>();

        String server = "";
        int rc = -1;
        String responseState = "";

        if (Throwable.class.isAssignableFrom(args[0].getClass())) {

            Throwable e = (Throwable) args[0];

            rc = 0;

            responseState = e.toString();
        }
        else {
            HttpMethod method = (HttpMethod) args[0];

            Header sheader = method.getResponseHeader("Server");

            if (sheader != null) {
                server = sheader.getValue();
            }

            rc = 1;
            responseState = method.getStatusLine().getStatusCode() + "";
        }

        if (logger.isDebugable()) {
            logger.debug("Invoke END:" + rc + "," + server, null);
        }

        params.put(CaptureConstants.INFO_CLIENT_TARGETSERVER, server);
        params.put(CaptureConstants.INFO_CLIENT_RESPONSECODE, rc);
        params.put(CaptureConstants.INFO_CLIENT_APPID, this.applicationId);
        params.put(CaptureConstants.INFO_CLIENT_TYPE, "apache.http.Client");
        params.put(CaptureConstants.INFO_CLIENT_REQUEST_URL, targetURL);
        params.put(CaptureConstants.INFO_CLIENT_RESPONSESTATE, responseState);

        UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                Monitor.CapturePhase.DOCAP, params);

        if (ivcContextParams != null) {
            ivcContextParams.putAll(params);
        }

        UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.DOCAP, ivcContextParams,
                ApacheHttpClient3Adapter.class, args);

    }
}
