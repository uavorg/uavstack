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

package com.creditease.uav.hook.esclient.transport.interceptors;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.CaptureContext;
import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.monitor.proxy.spi.JDKProxyInvokeHandler;
import com.creditease.monitor.proxy.spi.JDKProxyInvokeProcessor;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.common.BaseComponent;
import com.creditease.uav.hook.esclient.transport.interceptors.TransportIT;
import com.creditease.uav.hook.esclient.transport.invokeChain.TransportAdapter;
import com.creditease.uav.util.JDKProxyInvokeUtil;

import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.transport.TransportResponseHandler;

public class TransportIT extends BaseComponent{
    
    /**
     * 
     * ESHandlerProxyInvokeProcessor description:
     *
     */
    @SuppressWarnings("rawtypes")
    public class ESHandlerProxyInvokeProcessor extends JDKProxyInvokeProcessor<TransportResponseHandler> {

        @Override
        public void preProcess(TransportResponseHandler t, Object proxy, Method method, Object[] args) {

        }

        @Override
        public void catchInvokeException(TransportResponseHandler t, Object proxy, Method method, Object[] args, Throwable e) {

            doEnd(method, args, -1, e);
        }

        @Override
        public Object postProcess(Object res, TransportResponseHandler t, Object proxy, Method method, Object[] args) {

            if (method.getName().equals("handleResponse")) {
                doEnd(method, args, 1, null);
            }
            else if (method.getName().equals("handleException")){
                doEnd(method, args, -1, (Throwable)args[0]);
            }

            return res;
        }
        
        private void doEnd(Method method, Object[] args, int rc, Throwable e) {
                
            Map<String, Object> params = new HashMap<String, Object>();

            params.put(CaptureConstants.INFO_CLIENT_TARGETSERVER, "elasticsearch");
            params.put(CaptureConstants.INFO_CLIENT_RESPONSECODE, rc);
            params.put(CaptureConstants.INFO_CLIENT_REQUEST_URL, targetURL);
            params.put(CaptureConstants.INFO_CLIENT_APPID, appid);
            params.put(CaptureConstants.INFO_CLIENT_TYPE, "elasticsearch.client");
                
            if (e != null) {
                params.put(CaptureConstants.INFO_CLIENT_RESPONSESTATE, e.toString());
                    
                if (logger.isDebugable()) {
                    logger.debug("Elastaicsearch INVOKE Exception: " + targetURL + " action: " + esAction, null);
                }
            }
            else if (logger.isDebugable()) {
                logger.debug("Elastaicsearch INVOKE End: " + targetURL + " action: " + esAction, null);
            }

            UAVServer.instance().runMonitorAsyncCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                    Monitor.CapturePhase.DOCAP, params, ccMap);
                
            if (ivcContextParams != null) {
                ivcContextParams.putAll(params);
            }

            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                    InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.DOCAP, ivcContextParams,
                    TransportAdapter.class, args);
        }

    }
    
    private Map<String, CaptureContext> ccMap;
    
    private String targetURL = "";
    
    private String esAction = "";

    private String appid;
    
    private Map<String, Object> ivcContextParams = new HashMap<String, Object>();

    public TransportIT(String appid) {
        this.appid = appid;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object doAsyncStart(Object[] args) {

        DiscoveryNode node = (DiscoveryNode) args[0];
        esAction = (String)args[1];
        TransportResponseHandler handler = (TransportResponseHandler)args[4];
        
        String address = node.getHostAddress();
        Integer port = node.getAddress().getPort();
        
        targetURL = "elasticsearch://" + address + ":" + port;

        if (logger.isDebugable()) {
            logger.debug("Elastaicsearch INVOKE START: " + targetURL + " action: " + esAction, null);
        }

        Map<String, Object> params = new HashMap<String, Object>();

        params.put(CaptureConstants.INFO_CLIENT_REQUEST_URL, targetURL);
        params.put(CaptureConstants.INFO_CLIENT_REQUEST_ACTION, esAction);
        params.put(CaptureConstants.INFO_CLIENT_APPID, appid);
        params.put(CaptureConstants.INFO_CLIENT_TYPE, "elasticsearch.client");

        
        ccMap = UAVServer.instance().runMonitorAsyncCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                Monitor.CapturePhase.PRECAP, params, null);
        
        // register adapter
        UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "registerAdapter",
                TransportAdapter.class);

        ivcContextParams = (Map<String, Object>) UAVServer.instance().runSupporter(
                "com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.PRECAP, params,
                TransportAdapter.class, args);
        
        if (handler == null) {
            return null;
        }
        
        handler = JDKProxyInvokeUtil.newProxyInstance(TransportResponseHandler.class.getClassLoader(),
                new Class<?>[] { TransportResponseHandler.class }, new JDKProxyInvokeHandler<TransportResponseHandler>(handler,
                        new ESHandlerProxyInvokeProcessor()));

        return handler;
    }

}
