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

package com.creditease.agent.spi;

import java.util.concurrent.Executor;

public abstract class AbstractBaseHttpServComponent<T> extends AbstractHandleWorkComponent<T, AbstractHttpHandler<T>> {

    protected int port;

    protected String host;

    public AbstractBaseHttpServComponent(String cName, String feature, String initHandlerKey) {
        super(cName, feature, initHandlerKey);
    }

    /**
     * get http port
     * 
     * @return
     */
    public int getPort() {

        return port;
    }

    /**
     * get http host
     * 
     * @return
     */
    public String getHost() {

        return host;
    }

    /**
     * get the http root url
     * 
     * @return
     */
    public String getHttpRootURL() {

        return "http://" + host + ":" + port;
    }

    /**
     * real handle the request & response
     * 
     * @param message
     */
    protected void handleMessage(HttpMessage message) {

        String path = message.getContextPath();

        /**
         * IN CASE: the ping scanning
         */
        if (message.getMethod().equalsIgnoreCase("get") && path.indexOf("com.creditease.uav/jvm") > -1) {
            message.putResponseBodyInString("Adapt HttpMessage REQUEST FAIL", 204, "utf-8");
            return;
        }

        /**
         * step 1: adapt message to T
         */
        T t = null;
        try {
            t = adaptRequest(message);
        }
        catch (Exception e) {
            log.err(this, "HttpServiceComponent[" + this.getName() + "] for feature[" + this.getFeature()
                    + "] adapt HttpMessage REQUEST FAIL.", e);
            message.putResponseBodyInString("Adapt HttpMessage REQUEST FAIL", 500, "utf-8");
            return;
        }

        /**
         * step 2: run http handlers
         */
        boolean dispatchFlag = false;
        for (AbstractHttpHandler<T> handler : handlers) {

            if (!path.equalsIgnoreCase(handler.getContextPath())) {
                continue;
            }

            try {
                handler.handle(t);
            }
            catch (Exception e) {
                log.err(this,
                        "HttpHandler[" + handler.getName() + "] for feature[" + handler.getFeature() + "] handle FAIL.",
                        e);
            }

            dispatchFlag = true;
            break;
        }

        if (false == dispatchFlag) {
        	log.warn(this, "NO Matching Handler To Handle This REQUEST, for path: " + path);
        	message.putResponseBodyInString("NO Matching Handler To Handle This REQUEST, for IP: " + com.creditease.agent.helpers.NetworkHelper.getLocalIP() + ", and path: " + path, 404, "utf-8");
        	return;
        }
        
        /**
         * step 3:
         */
        try {
            adaptResponse(message, t);
        }
        catch (Exception e) {
            log.err(this, "HttpServiceComponent[" + this.getName() + "] for feature[" + this.getFeature()
                    + "] adapt HttpMessage RESPONSE　FAIL.", e);
            message.putResponseBodyInString("Adapt HttpMessage RESPONSE FAIL", 500, "utf-8");
            return;
        }
    }

    /**
     * adapt http message to T object
     * 
     * @param message
     * @return
     */
    protected abstract T adaptRequest(HttpMessage message);

    protected abstract void adaptResponse(HttpMessage message, T t);

    public abstract void stop();

    public abstract void start(Executor executor, int port, int backlog);

    public abstract void start(Executor executor, int port, int backlog, boolean forceExit);

    public abstract void start(int port, int backlog);

    public abstract void start(int port, int backlog, int listenThreadCount, int handleThreadCount);

    public abstract void start(int port, int backlog, int listenThreadCount, int handleThreadCount, boolean forceExit);
}
