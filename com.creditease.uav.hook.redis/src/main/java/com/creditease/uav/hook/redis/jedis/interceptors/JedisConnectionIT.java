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

package com.creditease.uav.hook.redis.jedis.interceptors;

import java.util.HashMap;
import java.util.Map;

import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.common.BaseComponent;
import com.creditease.uav.hook.redis.jedis.invokeChain.JedisClientAdapter;

import redis.clients.jedis.Protocol.Command;

public class JedisConnectionIT extends BaseComponent {

    private static ThreadLocal<JedisConnectionIT> tl = new ThreadLocal<JedisConnectionIT>();

    private String appid;

    private Map<String, Object> ivcContextParams = new HashMap<String, Object>();

    public JedisConnectionIT(String appid) {
        this.appid = appid;
    }

    public static void start(String appid, Object[] args) {

        JedisConnectionIT jc = new JedisConnectionIT(appid);
        tl.set(jc);
        jc.doSendCommandStart(args);
    }

    public static void end(Object[] args) {

        JedisConnectionIT jc = tl.get();
        tl.remove();

        if (jc == null) {
            return;
        }
        jc.doSendCommandEnd(args);
    }

    @SuppressWarnings({ "unchecked" })
    public Object doSendCommandStart(Object[] args) {

        Command cmd = (Command) args[0];
        String host = (String) args[2];
        Integer port = (Integer) args[3];
        String targetURL = "redis://" + host + ":" + port;
        String redisAction = cmd.name();

        if (logger.isDebugable()) {
            logger.debug("REDIS INVOKE START: " + targetURL + " action: " + redisAction, null);
        }

        Map<String, Object> params = new HashMap<String, Object>();

        params.put(CaptureConstants.INFO_CLIENT_REQUEST_URL, targetURL);
        params.put(CaptureConstants.INFO_CLIENT_REQUEST_ACTION, redisAction);
        params.put(CaptureConstants.INFO_CLIENT_APPID, appid);
        params.put(CaptureConstants.INFO_CLIENT_TYPE, "redis.client.Jedis");

        UAVServer.instance().runMonitorCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                Monitor.CapturePhase.PRECAP, params);

        // register adapter
        UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "registerAdapter",
                JedisClientAdapter.class);

        ivcContextParams = (Map<String, Object>) UAVServer.instance().runSupporter(
                "com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                InvokeChainConstants.CHAIN_APP_CLIENT, InvokeChainConstants.CapturePhase.PRECAP, params,
                JedisClientAdapter.class, args);
        return null;
    }

    public Object doSendCommandEnd(Object[] args) {

        int rc = -1;

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
                JedisClientAdapter.class, args);
        return null;
    }

}
