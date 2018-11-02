/*-
 * <<
 * UAVStack
 * ==
 * Copyright (C) 2016 - 2018 UAVStack
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
package com.creditease.uav.hook.redis.lettuce5x.interceptors;

import java.util.HashMap;
import java.util.Map;

import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.common.BaseComponent;
import com.creditease.uav.hook.redis.lettuce5x.invokeChain.Lettuce5ClientAdapter;

import io.lettuce.core.protocol.ProtocolKeyword;
import io.lettuce.core.protocol.RedisCommand;

/**
 * Lettuce5CommandHandlerIT description: ???
 *
 */
public class Lettuce5CommandHandlerIT extends BaseComponent{
    private static ThreadLocal<Lettuce5CommandHandlerIT> tl = new ThreadLocal<Lettuce5CommandHandlerIT>();

    private String appid;

    private Map<String, Object> ivcContextParams = new HashMap<String, Object>();

    public Lettuce5CommandHandlerIT(String appid) {
        this.appid = appid;
    }

    public static void start(String appid, Object[] args) {

        Lettuce5CommandHandlerIT it = new Lettuce5CommandHandlerIT(appid);
        it.doWriteStart(args);
        tl.set(it);
    }

    public static void end(Object[] args) {

        Lettuce5CommandHandlerIT it = tl.get();
        tl.remove();

        if (it == null) {
            return;
        }
        it.doWriteEnd(args);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Object doWriteStart(Object[] args) {

        RedisCommand cmdRc = (RedisCommand) args[0];
        ProtocolKeyword cmd = (ProtocolKeyword)cmdRc.getType();
        String host = (String) args[1];
        Integer port = (Integer) args[2];
        String targetURL = "redis://" + host + ":" + port;
        String redisAction = cmd.toString();

        if (logger.isDebugable()) {
            logger.debug("REDIS INVOKE START: " + targetURL + " action: " + redisAction, null);
        }

        Map<String, Object> params = new HashMap<String, Object>();

        params.put(CaptureConstants.INFO_CLIENT_REQUEST_URL, targetURL);
        params.put(CaptureConstants.INFO_CLIENT_REQUEST_ACTION, redisAction);
        params.put(CaptureConstants.INFO_CLIENT_APPID, appid);
        params.put(CaptureConstants.INFO_CLIENT_TYPE, "redis.client.Lettuce");

        // register adapter
        UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "registerAdapter",
                Lettuce5ClientAdapter.class);

        ivcContextParams = (Map<String, Object>) UAVServer.instance().runSupporter(
                "com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.PRECAP, params,
                Lettuce5ClientAdapter.class, args);

        UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                Monitor.CapturePhase.PRECAP, params);

        return null;
    }

    public Object doWriteEnd(Object[] args) {

        int rc = -1;
        
        if (args == null) {
            rc = 1;
        }

        if (args != null && args.length > 0) {
            if (!Throwable.class.isAssignableFrom(args[0].getClass())) {
                rc = 1;
            }
        }

        if (logger.isDebugable()) {
            logger.debug("REDIS INVOKE END: " + rc, null);
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put(CaptureConstants.INFO_CLIENT_TARGETSERVER, "redis");
        params.put(CaptureConstants.INFO_CLIENT_RESPONSECODE, rc);

        UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                Monitor.CapturePhase.DOCAP, params);

        if (rc == -1) {
            params.put(CaptureConstants.INFO_CLIENT_RESPONSESTATE, ((Throwable) args[0]).toString());
        }
        if (ivcContextParams != null) {
            ivcContextParams.putAll(params);
        }

        UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.DOCAP, ivcContextParams,
                Lettuce5ClientAdapter.class, args);

        return null;
    }
}
