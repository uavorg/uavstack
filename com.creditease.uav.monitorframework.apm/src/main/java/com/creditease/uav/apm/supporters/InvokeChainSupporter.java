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

package com.creditease.uav.apm.supporters;

import java.util.HashMap;
import java.util.Map;

import com.creditease.agent.helpers.ReflectHelper;
import com.creditease.monitor.UAVMonitor;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.UAVServer.ServerVendor;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.interceptframework.InterceptSupport;
import com.creditease.monitor.log.DataLoggerManager;
import com.creditease.uav.apm.invokechain.handlers.ClientSpanInvokeChainHandler;
import com.creditease.uav.apm.invokechain.handlers.MethodSpanInvokeChainHandler;
import com.creditease.uav.apm.invokechain.handlers.ServiceSpanInvokeChainHandler;
import com.creditease.uav.apm.invokechain.handlers.SpanContextHandler;
import com.creditease.uav.apm.invokechain.jee.JEEServiceRunGlobalFilterHandler;
import com.creditease.uav.apm.invokechain.spi.InvokeChainAdapter;
import com.creditease.uav.apm.invokechain.spi.InvokeChainCapHandler;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants.CapturePhase;
import com.creditease.uav.apm.invokechain.spi.InvokeChainContext;
import com.creditease.uav.appserver.listeners.GlobalFilterDispatchListener;
import com.creditease.uav.common.Supporter;

/**
 * 
 * InvokeChainSupporter description: InvokeChain Capture Start or Stop & Run
 *
 */
public class InvokeChainSupporter extends Supporter {

    private enum AdapterPhase {

        Before, After
    }

    private Map<String, InvokeChainCapHandler> handlerMap = new HashMap<String, InvokeChainCapHandler>();

    private Map<String, InvokeChainAdapter> adapterMap = new HashMap<String, InvokeChainAdapter>();

    private UAVMonitor monitor = new UAVMonitor(logger, 60000);

    private DataLoggerManager dlm;

    @Override
    public void start() {

        // init the simple invokechain dataloggermgr
        dlm = this.newDataLoggerManager("ivc", "com.creditease.uav.invokechain");

        /**
         * NOTE: when start the invokechain, we have to clean all ivc logs & lock files
         */
        dlm.clearLogs();

        ServerVendor vendor = (ServerVendor) UAVServer.instance().getServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR);

        // MSCP
        if (vendor == ServerVendor.MSCP) {
            // TODO
        }
        // JEE
        else {
            // register ServiceStartInvokeChainHandler to GlobalFilter
            GlobalFilterDispatchListener listener = (GlobalFilterDispatchListener) InterceptSupport.instance()
                    .getEventListener(GlobalFilterDispatchListener.class);

            listener.registerHandler(new JEEServiceRunGlobalFilterHandler("JEEServiceRunGlobalFilterHandler"));
        }

        // register ServiceEndInvokeChainHandler
        handlerMap.put(InvokeChainConstants.CHAIN_APP_SERVICE, new ServiceSpanInvokeChainHandler());

        // register ClientSpanInvokeChainHandler
        handlerMap.put(InvokeChainConstants.CHAIN_APP_CLIENT, new ClientSpanInvokeChainHandler());

        // register MethodSpanInvokeChainHandler
        handlerMap.put(InvokeChainConstants.CHAIN_APP_METHOD, new MethodSpanInvokeChainHandler());

    }

    @Override
    public void stop() {

        ServerVendor vendor = (ServerVendor) UAVServer.instance().getServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR);

        // MSCP
        if (vendor == ServerVendor.MSCP) {
            // TODO
        }
        // JEE
        else {
            GlobalFilterDispatchListener listener = (GlobalFilterDispatchListener) InterceptSupport.instance()
                    .getEventListener(GlobalFilterDispatchListener.class);

            listener.unregisterHandler("JEEServiceRunGlobalFilterHandler");
        }

        dlm.destroy();

        adapterMap.clear();

        handlerMap.clear();

        super.stop();
    }

    /**
     * 捕获调用链: 由于框架代码处理带来的异步情况，由框架代码测的实现（比如Hook）来传递contextParams，因为这里通过return返回了当前处理完的contextParams
     * 
     * @param captureId
     * @param phase
     * @param contextParams
     * @param adapterClass
     * @param adapterInArgs
     * @return
     */
    private InvokeChainContext runCap(String captureId, CapturePhase phase, InvokeChainContext context,
            Class<? extends InvokeChainAdapter> adapterClass, Object[] adapterInArgs) {

        long st = System.currentTimeMillis();

        /**
         * Step 1: get the capture handler
         */
        InvokeChainCapHandler handler = handlerMap.get(captureId);

        if (handler == null) {
            return null;
        }

        /**
         * Step 2: process invoke chain capture
         */
        switch (phase) {
            case DOCAP:
                runAdapter(phase, context, adapterClass, adapterInArgs, AdapterPhase.Before);
                handler.doCap(context);
                runAdapter(phase, context, adapterClass, adapterInArgs, AdapterPhase.After);
                break;
            case PRECAP:
                runAdapter(phase, context, adapterClass, adapterInArgs, AdapterPhase.Before);
                handler.preCap(context);
                runAdapter(phase, context, adapterClass, adapterInArgs, AdapterPhase.After);
                break;

        }

        this.monitor.logPerf(st, "InvokeChain");

        return context;
    }

    /**
     * Check if there an adapter, if yes run the adapter
     * 
     * @param phase
     * @param contextParams
     * @param adapterClass
     * @param inArgs
     */
    private void runAdapter(CapturePhase phase, InvokeChainContext params,
            Class<? extends InvokeChainAdapter> adapterClass, Object[] args, AdapterPhase ap) {

        if (adapterClass == null) {
            return;
        }

        String adpaterName = adapterClass.getClassLoader().hashCode() + "-" + adapterClass.getName();

        InvokeChainAdapter adapter = this.adapterMap.get(adpaterName);

        if (adapter == null) {
            return;
        }

        switch (phase) {
            case PRECAP:

                switch (ap) {
                    case After:
                        adapter.afterPreCap(params, args);
                        break;
                    case Before:
                        adapter.beforePreCap(params, args);
                        break;
                }

                break;
            case DOCAP:
                switch (ap) {
                    case After:
                        adapter.afterDoCap(params, args);
                        break;
                    case Before:
                        adapter.beforeDoCap(params, args);
                        break;
                }
                break;
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public Object run(String methodName, Object... params) {

        if (methodName.equals("runCap")) {

            String captureId = (String) params[0];
            CapturePhase phase = (CapturePhase) params[1];
            Map<String, Object> inputParams = (Map<String, Object>) params[2];

            InvokeChainContext context = new InvokeChainContext();

            context.put(inputParams);

            if (params.length == 5) {
                Class<? extends InvokeChainAdapter> adapterClass = (Class<? extends InvokeChainAdapter>) params[3];
                Object[] adapterInArgs = (Object[]) params[4];
                return this.runCap(captureId, phase, context, adapterClass, adapterInArgs);
            }
            else {
                return this.runCap(captureId, phase, context, null, null);
            }
        }
        else if (methodName.equals("registerAdapter")) {

            try {
                Class<?> adapterClass = ((Class<?>) params[0]);
                String adapterClassName = adapterClass.getName();
                String adapterUUID = adapterClass.getClassLoader().hashCode() + "-" + adapterClassName;

                if (this.adapterMap.containsKey(adapterUUID)) {
                    return true;
                }

                InvokeChainAdapter adapter = (InvokeChainAdapter) ReflectHelper.newInstance(adapterClassName);

                adapterMap.put(adapterUUID, adapter);

                return true;
            }
            catch (Exception e) {
                this.logger.error("Register Adapter" + params[0].toString() + "FAIL. ", e);
                return false;
            }
        }
        else if (methodName.equals("getSpanContext")) {
            try {
                return new SpanContextHandler().new SpanContext();
            }
            catch (Exception e) {
                this.logger.error("getSpanContext FAIL. ", e);
                return null;
            }
        }

        return null;
    }

}
