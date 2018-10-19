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

package com.creditease.uav.hook.jaxws.interceptors;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.monitor.proxy.spi.JDKProxyInvokeHandler;
import com.creditease.monitor.proxy.spi.JDKProxyInvokeProcessor;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.common.BaseComponent;
import com.creditease.uav.hook.jaxws.invokeChain.JaxWSAdapter;
import com.creditease.uav.util.JDKProxyInvokeUtil;
import com.creditease.uav.util.MonitorServerUtil;

public class JaxWSHookIT extends BaseComponent {

    /**
     * 
     * JaxWSHookHandler description: jaxws soap handler to set request header and get response header
     *
     */
    public class JaxWSHookHandler implements SOAPHandler<SOAPMessageContext> {

        private String server;

        private Map<String, List<String>> headerMeta = new HashMap<String, List<String>>();

        public void setHeaderMeta(Map<String, List<String>> headerMeta) {

            if (this.headerMeta == null) {
                this.headerMeta = headerMeta;
            }
            else {
                this.headerMeta.putAll(headerMeta);
            }
        }

        @SuppressWarnings({ "unchecked", })
        @Override
        public boolean handleMessage(SOAPMessageContext context) {

            boolean isOut = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

            // outbound
            if (isOut == true) {
                Map<String, List<String>> headers = (Map<String, List<String>>) context
                        .get(MessageContext.HTTP_REQUEST_HEADERS);

                List<String> ls = new ArrayList<String>();

                ls.add(MonitorServerUtil.getUAVClientSrc(appid));

                if (headers == null) {
                    headers = new HashMap<String, List<String>>();
                    context.put(MessageContext.HTTP_REQUEST_HEADERS, headers);
                }

                // when service use axis 1.4, SOAPAction header is necessary
                if (!headers.containsKey("SOAPAction")) {
                    List<String> soapActionHeader = new ArrayList<String>();
                    soapActionHeader.add("\"\"");
                    headers.put("SOAPAction", soapActionHeader);
                }

                headers.put("UAV-Client-Src", ls);
                for (String key : this.headerMeta.keySet()) {
                    headers.remove(key);
                }
                headers.putAll(this.headerMeta);
            }
            // inbound
            else {

                getTargetServer(context);
            }
            return true;
        }

        @Override
        public boolean handleFault(SOAPMessageContext context) {

            boolean isOut = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

            // inbound
            if (isOut == false) {

                getTargetServer(context);
            }
            return true;
        }

        @SuppressWarnings("unchecked")
        private void getTargetServer(SOAPMessageContext context) {

            Map<String, List<String>> headers = (Map<String, List<String>>) context
                    .get(MessageContext.HTTP_RESPONSE_HEADERS);

            if (headers != null) {
                List<String> ls = headers.get("Server");

                if (ls != null) {
                    this.server = ls.get(ls.size() - 1);
                }
            }
        }

        @Override
        public void close(MessageContext context) {

        }

        @Override
        public Set<QName> getHeaders() {

            return null;
        }

        public String getServer() {

            return this.server;
        }

    }

    public abstract class JAXWSStubProcessor<T> extends JDKProxyInvokeProcessor<T> {

        protected String wsdl;

        protected JaxWSHookHandler handler;

        private Map<String, Object> ivcContextParams = new HashMap<String, Object>();

        public JAXWSStubProcessor(String wsdl, JaxWSHookHandler handler) {
            this.wsdl = wsdl;
            this.handler = handler;
        }

        @SuppressWarnings("unchecked")
        protected void doStart(String httpAction) {

            Map<String, Object> params = new HashMap<String, Object>();

            params.put(CaptureConstants.INFO_CLIENT_REQUEST_URL, wsdl);
            params.put(CaptureConstants.INFO_CLIENT_REQUEST_ACTION, httpAction);
            params.put(CaptureConstants.INFO_CLIENT_APPID, appid);
            params.put(CaptureConstants.INFO_CLIENT_TYPE, "JaxWS.Client");

            if (logger.isDebugable()) {
                logger.debug("Invoke START:" + wsdl + "," + httpAction + "," + appid, null);
            }

            UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                    Monitor.CapturePhase.PRECAP, params);

            // register adapter
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter",
                    "registerAdapter", JaxWSAdapter.class);

            ivcContextParams = (Map<String, Object>) UAVServer.instance().runSupporter(
                    "com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                    InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.PRECAP, params,
                    JaxWSAdapter.class, null);
            if (ivcContextParams != null
                    && ivcContextParams.get(InvokeChainConstants.PARAM_HTTPHEAD_SPANINFO) != null) {
                this.handler.setHeaderMeta(
                        (Map<String, List<String>>) ivcContextParams.get(InvokeChainConstants.PARAM_HTTPHEAD_SPANINFO));
            }
        }

        protected void doEnd(int rc, Throwable e) {

            String server = handler.getServer();

            Map<String, Object> params = new HashMap<String, Object>();

            if (logger.isDebugable()) {
                logger.debug("Invoke END:" + rc + "," + server, null);
            }

            params.put(CaptureConstants.INFO_CLIENT_TARGETSERVER, server);
            params.put(CaptureConstants.INFO_CLIENT_RESPONSECODE, rc);

            UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                    Monitor.CapturePhase.DOCAP, params);

            if (rc == -1) {
                params.put(CaptureConstants.INFO_CLIENT_RESPONSESTATE, e.toString());
            }
            if (ivcContextParams != null) {
                ivcContextParams.putAll(params);
            }

            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                    InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.DOCAP, ivcContextParams,
                    JaxWSAdapter.class, null);
        }
    }

    /**
     * 
     * ClientStubProcessor description: ClientStub Processor
     *
     * @param <T>
     */
    public class ClientStubProcessor<T> extends JAXWSStubProcessor<T> {

        public ClientStubProcessor(String wsdl, JaxWSHookHandler handler) {
            super(wsdl, handler);
        }

        @Override
        public void preProcess(T t, Object proxy, Method method, Object[] args) {

            String httpAction = method.getName();
            /**
             * 呵呵，就是把UAV的客户端标记加到http header里面，接收方就知道是哪个东东调用的了，便于实现来源快速匹配，这个模式只适合直连模式
             * 
             * 对于代理模式，只匹配代理源即可
             */
            doStart(httpAction);

        }

        @Override
        public void catchInvokeException(T t, Object proxy, Method method, Object[] args, Throwable e) {

            doEnd(-1, e);
        }

        @Override
        public Object postProcess(Object res, T t, Object proxy, Method method, Object[] args) {

            doEnd(1, null);

            return null;
        }

    }

    /**
     * 
     * DispatchProcessor description: for dispatch client
     * 
     * TODO: only support invoke, don't support invokeAsync, invokeOneWay
     */
    @SuppressWarnings("rawtypes")
    public class DispatchProcessor extends JAXWSStubProcessor<Dispatch> {

        public DispatchProcessor(String wsdl, JaxWSHookHandler handler) {
            super(wsdl, handler);
        }

        @Override
        public void preProcess(Dispatch t, Object proxy, Method method, Object[] args) {

            String httpAction = method.getName();

            if (!"invoke".equals(method.getName())) {
                return;
            }

            doStart(httpAction);
        }

        @Override
        public void catchInvokeException(Dispatch t, Object proxy, Method method, Object[] args, Throwable e) {

            if (!"invoke".equals(method.getName())) {
                return;
            }

            doEnd(-1, e);
        }

        @Override
        public Object postProcess(Object res, Dispatch t, Object proxy, Method method, Object[] args) {

            if (!"invoke".equals(method.getName())) {
                return null;
            }

            doEnd(1, null);

            return null;
        }

    }

    private String appid;

    protected JaxWSHookHandler handler;

    public JaxWSHookIT(String appid) {
        this.appid = appid;
        this.handler = new JaxWSHookHandler();
    }

    /**
     * this is for Client Stub Programming
     * 
     * @param t
     * @param s
     * @param args
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T getPort(T t, Service s, Object[] args) {
        
        if (JDKProxyInvokeUtil.isJDKProxy(t)) {
            return t;
        }

        Class<T> clz = null;
        if (Class.class.isAssignableFrom(args[0].getClass())) {
            clz = (Class<T>) args[0];
        }
        else if (Class.class.isAssignableFrom(args[1].getClass())) {
            clz = (Class<T>) args[1];
        }

        if (clz == null) {
            return t;
        }

        Binding binding = ((BindingProvider) t).getBinding();
        List<Handler> handlerChain = binding.getHandlerChain();
        handlerChain.add(this.handler);
        binding.setHandlerChain(handlerChain);

        final String wsdlLocation = getServiceURL(s);

        T tProxy = JDKProxyInvokeUtil.newProxyInstance(clz.getClassLoader(), new Class[] { clz },
                new JDKProxyInvokeHandler<T>(t, new ClientStubProcessor(wsdlLocation.toString(), this.handler)));
        return tProxy;
    }

    private String getServiceURL(Service s) {

        String wsdlLocation = s.getWSDLDocumentLocation().toString();

        int index = wsdlLocation.indexOf("?");

        if (index > -1) {
            wsdlLocation = wsdlLocation.substring(0, index);
        }

        return wsdlLocation;
    }

    @SuppressWarnings("rawtypes")
    public Dispatch createDispatch(Dispatch d, Service s, Object[] args) {

        Binding binding = ((BindingProvider) d).getBinding();
        List<Handler> handlerChain = binding.getHandlerChain();
        handlerChain.add(this.handler);
        binding.setHandlerChain(handlerChain);

        final String wsdlLocation = getServiceURL(s);

        Dispatch tProxy = JDKProxyInvokeUtil.newProxyInstance(this.getClass().getClassLoader(),
                new Class[] { Dispatch.class },
                new JDKProxyInvokeHandler<Dispatch>(d, new DispatchProcessor(wsdlLocation.toString(), this.handler)));
        return tProxy;
    }
}
