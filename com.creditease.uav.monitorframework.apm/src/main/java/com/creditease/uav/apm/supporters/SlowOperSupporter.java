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

import com.creditease.monitor.log.DataLoggerManager;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants.CapturePhase;
import com.creditease.uav.apm.invokechain.spi.InvokeChainContext;
import com.creditease.uav.apm.slowoper.handlers.HttpClientSlowOperHandler;
import com.creditease.uav.apm.slowoper.handlers.HttpServerSlowOperHandler;
import com.creditease.uav.apm.slowoper.handlers.JdbcSlowOperHandler;
import com.creditease.uav.apm.slowoper.handlers.MQSlowOperHandler;
import com.creditease.uav.apm.slowoper.handlers.MethodSlowOperHandler;
import com.creditease.uav.apm.slowoper.spi.SlowOperCapHandler;
import com.creditease.uav.apm.slowoper.spi.SlowOperConstants;
import com.creditease.uav.common.Supporter;

/**
 * 
 * SlowOperSupporter description: 所有慢操作收集，比如SQL
 *
 */
public class SlowOperSupporter extends Supporter {

    private DataLoggerManager dlm;

    private Map<String, SlowOperCapHandler> handlerMap = new HashMap<String, SlowOperCapHandler>();

    @Override
    public void start() {

        // init slow operate datalogger manager
        dlm = this.newDataLoggerManager("ivcdat", "com.creditease.uav.ivcdat");

        /**
         * NOTE: when start the invokechain, we have to clean all ivc logs & lock files
         */
        dlm.clearLogs();

        // register 处理http协议的server类型的handler
        handlerMap.put(SlowOperConstants.SLOW_OPER_HTTP_SERVER, new HttpServerSlowOperHandler());

        // register 处理http协议的同步client类型的handler
        handlerMap.put(SlowOperConstants.SLOW_OPER_HTTP_CLIENT, new HttpClientSlowOperHandler());

        // register 处理http协议的异步client类型的handler
        handlerMap.put(SlowOperConstants.SLOW_OPER_HTTP_ASY_CLIENT, new HttpClientSlowOperHandler());

        // register 处理rabbit mq协议的consumer类型的handler
        handlerMap.put(SlowOperConstants.SLOW_OPER_MQ_RABBIT_CONSUMER, new MQSlowOperHandler());

        // register 处理rabbit mq协议的producer类型的handler
        handlerMap.put(SlowOperConstants.SLOW_OPER_MQ_RABBIT_PRODUCER, new MQSlowOperHandler());

        // register 处理jdbc协议类型的handler
        handlerMap.put(SlowOperConstants.SLOW_OPER_JDBC_CLIENT, new JdbcSlowOperHandler());

        // register 处理method类型的handler
        handlerMap.put(SlowOperConstants.SLOW_OPER_METHOD, new MethodSlowOperHandler());

        // register 处理dubbo consumer类型的handler
        handlerMap.put(SlowOperConstants.SLOW_OPER_DUBBO_CONSUMER, new MethodSlowOperHandler());

        // register 处理dubbo provider类型的handler
        handlerMap.put(SlowOperConstants.SLOW_OPER_DUBBO_PROVIDER, new MethodSlowOperHandler());

        handlerMap.put(SlowOperConstants.SLOW_OPER_MQ_ROCKET, new MQSlowOperHandler());
    }

    @Override
    public void stop() {

        dlm.destroy();

        handlerMap.clear();
        super.stop();
    }

    @Override
    public Object run(String methodName, Object... params) {

        if (methodName.equals("runCap")) {
            String handlerType = (String) params[0];
            CapturePhase phase = (CapturePhase) params[1];
            InvokeChainContext context = (InvokeChainContext) params[2];
            Object[] args = (Object[]) params[3];
            this.runCap(handlerType, phase, context, args);
        }
        else {
        }
        return null;
    }

    private void runCap(String handlerType, CapturePhase phase, InvokeChainContext context, Object[] args) {

        /**
         * Step 1: get the capture handler
         */
        SlowOperCapHandler handler = handlerMap.get(handlerType);

        if (handler == null) {
            return;
        }

        /**
         * Step 2: process invoke chain capture
         */
        switch (phase) {
            case DOCAP:
                handler.doCap(context, args);
                break;
            case PRECAP:
                handler.preCap(context, args);
                break;

        }

    }
}
