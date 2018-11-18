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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executor;

import com.creditease.agent.helpers.NetworkHelper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * 
 * AbstractHttpServiceComponent description: Sun Http Server Implementation
 *
 * @param <T>
 */
@SuppressWarnings("restriction")
public abstract class AbstractHttpServiceComponent<T> extends AbstractBaseHttpServComponent<T> {

    /**
     * the http message
     * 
     * @author zhen zhang
     *
     */
    public class HttpMessageImpl extends HttpMessage {

        private HttpExchange exchange;

        public HttpMessageImpl(HttpExchange exchange) {
            this.exchange = exchange;
            param = parseQueryString(exchange.getRequestURI().getQuery());
        }

        @Override
        public String getMethod() {

            return exchange.getRequestMethod();
        }

        @Override
        public String getHeader(String name) {

            return exchange.getRequestHeaders().getFirst(name);
        }

        @Override
        public InputStream getRequestBody() {

            return exchange.getRequestBody();
        }

        @Override
        public OutputStream getResponseBody() {

            return exchange.getResponseBody();
        }

        @Override
        protected void putResponseCodeInfo(int retCode, int payloadLength) {

            try {
                exchange.sendResponseHeaders(retCode, payloadLength);
            }
            catch (IOException e) {
                // ignore
            }
        }

        @Override
        public URI getRequestURI() {

            return exchange.getRequestURI();
        }

        @Override
        public void putResponseBodyInChunkedFile(File file) {

            // not implementation
        }

        @Override
        public String getClientAddress() {

            return this.exchange.getRemoteAddress().getAddress().getHostAddress();
        }

        @Override
        public int getResponseCode() {

            return this.exchange.getResponseCode();
        }
    }

    /**
     * this is the http default handler all http request will be wrapped as HttpMessage for handler processing
     * 
     * @author zhen zhang
     *
     */
    private class DefaultHttpHandler implements HttpHandler {

        private final AbstractHttpServiceComponent<T> ahsc;

        public DefaultHttpHandler(AbstractHttpServiceComponent<T> ahsc) {
            this.ahsc = ahsc;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            HttpMessage message = new HttpMessageImpl(exchange);

            this.ahsc.handleMessage(message);

        }
    }

    protected HttpServer server;

    public AbstractHttpServiceComponent(String cName, String feature, String initHandlerKey) {

        super(cName, feature, initHandlerKey);
    }

    /**
     * start the http service
     * 
     * @param executor
     * @param port
     */
    @Override
    public void start(Executor executor, int port, int backlog) {

        start(executor, port, backlog, true);

    }

    @Override
    public void start(Executor executor, int port, int backlog, boolean forceExit) {

        if (null == executor || port < 0 || backlog < 0) {
            log.err(this, "HttpServiceComponent[" + this.cName + "] for feature[" + this.feature
                    + "] starts FAIL: Thread Executor is null or port<0");
            return;
        }

        // create http server
        try {
            this.port = port;

            InetSocketAddress isa = new InetSocketAddress(this.port);

            this.host = NetworkHelper.getLocalIP();

            server = HttpServer.create(isa, backlog);

        }
        catch (IOException e) {

            log.err(this, "HttpServiceComponent[" + this.cName + "] for feature[" + this.feature + "] starts FAIL.", e);

            if (forceExit == true) {
                System.exit(-1);
            }

            return;
        }

        // set context path for default handler
        server.createContext("/", new DefaultHttpHandler(this));

        // set thread pool executor
        server.setExecutor(executor);

        // start http server
        server.start();

        if (log.isTraceEnable()) {
            log.info(this, "HttpServiceComponent[" + this.cName + "] for feature[" + this.feature
                    + "] started SUCCESS: port=" + this.port);
        }
    }

    @Override
    public void start(int port, int backlog) {

        // not implementation
    }

    @Override
    public void start(int port, int backlog, int listenThreadCount, int handleThreadCount) {

        // not implementation
    }

    @Override
    public void start(int port, int backlog, int listenThreadCount, int handleThreadCount, boolean forceExit) {

        // not implementation
    }

    /**
     * stop http service
     */
    @Override
    public void stop() {

        // stop http server
        if (null != server) {

            server.stop(0);

            if (log.isTraceEnable()) {
                log.info(this,
                        "HttpServiceComponent[" + this.cName + "] for feature[" + this.feature + "] stopped SUCCESS");
            }
        }

    }
}
