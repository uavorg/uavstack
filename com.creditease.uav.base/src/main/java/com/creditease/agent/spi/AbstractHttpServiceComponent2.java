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

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import javax.activation.MimetypesFileTypeMap;

import com.creditease.agent.helpers.NetworkHelper;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * 
 * AbstractHttpServiceComponent2 description: Netty Implementation
 *
 * @param <T>
 */
public abstract class AbstractHttpServiceComponent2<T> extends AbstractBaseHttpServComponent<T> {

    public class HttpMessageImpl extends HttpMessage {

        private ChannelHandlerContext ctx;
        private FullHttpRequest request;
        private FullHttpResponse response;

        public HttpMessageImpl(ChannelHandlerContext ctx, FullHttpRequest request) {
            this.ctx = ctx;
            this.request = request.copy();
            this.param = parseQueryString(getRequestURI().toString());
            this.response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(200));
        }

        public ChannelHandlerContext getCtx() {

            return this.ctx;
        }

        @Override
        public String getMethod() {

            return request.getMethod().name();
        }

        @Override
        public String getHeader(String name) {

            return request.headers().get(name);
        }

        @Override
        public void putResponseBodyInString(String payload, int retCode, String encoding) {

            // this.response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(retCode),
            // Unpooled.copiedBuffer(payload, CharsetUtil.UTF_8));
            this.response.setStatus(HttpResponseStatus.valueOf(retCode));

            this.response.content().writeBytes(Unpooled.copiedBuffer(payload, Charset.forName(encoding)));

            ctx.write(response);

            // write end marker
            ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
        }

        @Override
        public String getRequestBodyAsString(String encoding) {

            if (request.content().refCnt() > 0) {
                return request.content().toString(Charset.forName(encoding));
            }

            return "";
        }

        @Override
        public URI getRequestURI() {

            try {
                return new URI(request.getUri());
            }
            catch (URISyntaxException e) {
                return null;
            }
        }

        @Override
        protected InputStream getRequestBody() {

            throw new RuntimeException("Not Implementation");
        }

        @Override
        protected OutputStream getResponseBody() {

            throw new RuntimeException("Not Implementation");
        }

        @Override
        protected void putResponseCodeInfo(int retCode, int payloadLength) {

            throw new RuntimeException("Not Implementation");
        }

        // parse decoded query String
        @Override
        protected Map<String, List<String>> parseQueryString(String s) {

            QueryStringDecoder decoder = new QueryStringDecoder(s);
            return decoder.parameters();
        }

        @Override
        public void putResponseBodyInChunkedFile(File file) {

            if (!file.exists()) {
                return;
            }

            RandomAccessFile raf;
            try {
                raf = new RandomAccessFile(file, "r");
            }
            catch (FileNotFoundException e1) {
                log.err(this, "File [" + file.getAbsolutePath() + "] NOT Exist.", e1);
                return;
            }
            final RandomAccessFile rf = raf;

            long fileLength = file.length();

            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.OK);
            response.headers().set(Names.CONTENT_LENGTH, fileLength);

            MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
            response.headers().set(Names.CONTENT_TYPE, mimeTypesMap.getContentType(file.getPath()));
            response.headers().set("Content-Disposition", "attachment;filename=" + file.getName());

            if (isKeepAlive()) {
                response.headers().set(Names.CONNECTION, Values.KEEP_ALIVE);
            }

            // Write the initial line and the header.
            ctx.write(response);

            // Write the content.
            ChannelFuture sendFileFuture = ctx.write(new DefaultFileRegion(rf.getChannel(), 0, fileLength),
                    ctx.newProgressivePromise());

            // Write the end marker.
            ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);

            sendFileFuture.addListener(new ChannelProgressiveFutureListener() {

                @Override
                public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {

                    if (total < 0) { // total unknown
                        if (log.isDebugEnable()) {
                            log.debug(this, future.channel() + " Transfer progress: " + progress);
                        }
                    }
                    else {
                        if (log.isDebugEnable()) {
                            log.debug(this, future.channel() + " Transfer progress: " + progress + " / " + total);
                        }
                    }
                }

                @Override
                public void operationComplete(ChannelProgressiveFuture future) {

                    if (log.isTraceEnable()) {
                        log.info(this, future.channel() + " Transfer complete.");
                    }

                    if (rf != null) {
                        try {
                            rf.close();
                        }
                        catch (IOException e) {
                            // ignore
                        }
                    }
                }
            });
        }

        @Override
        public String getClientAddress() {

            InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
            String clientIP = insocket.getAddress().getHostAddress();
            return clientIP;
        }

        @Override
        public int getResponseCode() {

            // TODO Auto-generated method stub
            return this.response.getStatus().code();
        }

    }

    /**
     * 
     * HttpServerInitializer description:
     *
     */
    public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

        private final AbstractHttpServiceComponent2<T> ahsc;

        public HttpServerInitializer(AbstractHttpServiceComponent2<T> abstractHttpServiceComponent2) {
            this.ahsc = abstractHttpServiceComponent2;
        }

        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {

            ChannelPipeline pipeline = socketChannel.pipeline();
            pipeline.addLast(new HttpServerCodec());
            pipeline.addLast(new HttpObjectAggregator(65536));
            pipeline.addLast(new ChunkedWriteHandler());
            pipeline.addLast(new DefaultHttpServerHandler(ahsc));
        }

    }

    /**
     * 
     * DefaultHttpServerHandler description:
     *
     */
    public class DefaultHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

        private final AbstractHttpServiceComponent2<T> ahsc;

        public DefaultHttpServerHandler(AbstractHttpServiceComponent2<T> abstractHttpServiceComponent2) {
            this.ahsc = abstractHttpServiceComponent2;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {

            HttpMessage message = new HttpMessageImpl(ctx, req);

            this.ahsc.handleMessage(message);
        }

    }

    public AbstractHttpServiceComponent2(String cName, String feature, String initHandlerKey) {

        super(cName, feature, initHandlerKey);
    }

    private ServerBootstrap server;

    @Override
    public void start(int port, int backlog) {

        start(port, backlog, 2, Runtime.getRuntime().availableProcessors() * 2);
    }

    @Override
    public void start(int port, int backlog, int listenThreadCount, int handleThreadCount) {

        start(port, backlog, listenThreadCount, handleThreadCount, true);
    }

    @Override
    public void start(Executor executor, int port, int backlog) {

        start(executor, port, backlog, true);
    }

    @Override
    public void start(Executor executor, int port, int backlog, boolean forceExit) {

        if (!ThreadPoolExecutor.class.isAssignableFrom(executor.getClass())) {
            throw new RuntimeException("No Supportive Executor Exception: only support ThreadPoolExecutor");
        }

        ThreadPoolExecutor tpe = (ThreadPoolExecutor) executor;

        start(port, backlog, Runtime.getRuntime().availableProcessors() * 2, tpe.getCorePoolSize(), forceExit);

        tpe.shutdownNow();
    }

    @Override
    public void start(int port, int backlog, int listenThreadCount, int handleThreadCount, boolean forceExit) {

        EventLoopGroup bossGroup = new NioEventLoopGroup(listenThreadCount);
        EventLoopGroup workerGroup = new NioEventLoopGroup(handleThreadCount);

        server = new ServerBootstrap();
        server.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).localAddress(port)
                .option(ChannelOption.SO_BACKLOG, backlog).childHandler(new HttpServerInitializer(this));

        try {
            this.host = NetworkHelper.getLocalIP();
            this.port = port;

            // Start the server.
            server.bind().sync();

            if (log.isTraceEnable()) {
                log.info(this, "HttpServiceComponent[" + this.cName + "] for feature[" + this.feature
                        + "] started SUCCESS: port=" + this.port);
            }
        }
        catch (Exception e) {
            log.err(this, "HttpServiceComponent[" + this.cName + "] for feature[" + this.feature + "] starts FAIL.", e);

            if (forceExit == true) {
                System.exit(-1);
            }

        }
    }

    @Override
    public void stop() {

        if (server != null) {

            if (server.group() != null) {
                server.group().shutdownGracefully();
            }

            if (server.childGroup() != null) {
                server.childGroup().shutdownGracefully();
            }

            server = null;

            if (log.isTraceEnable()) {
                log.info(this,
                        "HttpServiceComponent[" + this.cName + "] for feature[" + this.feature + "] stopped SUCCESS");
            }
        }
    }

}
