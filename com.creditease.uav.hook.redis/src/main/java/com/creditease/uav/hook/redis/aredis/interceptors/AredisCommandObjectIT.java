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

package com.creditease.uav.hook.redis.aredis.interceptors;

import java.util.HashMap;
import java.util.Map;

import org.aredis.cache.RedisCommand;
import org.aredis.cache.RedisCommandInfo;
import org.aredis.net.AbstractAsyncSocketTransport;
import org.aredis.net.AsyncSocketTransport;

import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.CaptureContext;
import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.uav.common.BaseComponent;

public class AredisCommandObjectIT extends BaseComponent {

    private String appid;

    private Map<String, CaptureContext> ccMap;

    public AredisCommandObjectIT(String appid) {
        this.appid = appid;
    }

    public void doAsyncStart(Object[] args) {

        AsyncSocketTransport con = (AsyncSocketTransport) args[0];
        AbstractAsyncSocketTransport abscon = (AbstractAsyncSocketTransport) con;
        RedisCommandInfo info = (RedisCommandInfo) args[3];
        RedisCommand cmd = info.getCommand();

        String targetURL = "redis://" + abscon.getHost() + ":" + abscon.getPort();
        String redisAction = cmd.toString();

        if (logger.isDebugable()) {
            logger.debug(
                    Thread.currentThread().getId() + "\tREDIS INVOKE START: " + targetURL + " action: " + redisAction,
                    null);
        }

        Map<String, Object> params = new HashMap<String, Object>();

        params.put(CaptureConstants.INFO_CLIENT_REQUEST_URL, targetURL);
        params.put(CaptureConstants.INFO_CLIENT_REQUEST_ACTION, redisAction);
        params.put(CaptureConstants.INFO_CLIENT_APPID, appid);
        params.put(CaptureConstants.INFO_CLIENT_TYPE, "redis.client.Aredis");

        ccMap = UAVServer.instance().runMonitorAsyncCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                Monitor.CapturePhase.PRECAP, params, null);

    }

    public void doAsyncEnd(Object[] args) {

        RedisCommandInfo info = (RedisCommandInfo) args[0];
        Throwable e = info.getError();

        int rc = e == null ? 1 : 0;
        doEnd(rc);
    }

    private void doEnd(int rc) {

        if (logger.isDebugable()) {
            logger.debug(Thread.currentThread().getId() + "\tREDIS INVOKE END: " + rc, null);
        }

        Map<String, Object> params = new HashMap<String, Object>();

        params.put(CaptureConstants.INFO_CLIENT_TARGETSERVER, "redis");
        params.put(CaptureConstants.INFO_CLIENT_RESPONSECODE, rc);

        UAVServer.instance().runMonitorAsyncCaptureOnServerCapPoint(CaptureConstants.CAPPOINT_APP_CLIENT,
                Monitor.CapturePhase.DOCAP, params, ccMap);
    }

}
