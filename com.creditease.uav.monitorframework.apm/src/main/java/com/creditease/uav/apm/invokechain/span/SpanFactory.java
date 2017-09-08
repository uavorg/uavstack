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

package com.creditease.uav.apm.invokechain.span;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.creditease.agent.helpers.NetworkHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;

/**
 * 
 * SpanFactory description: 负责生产Span
 *
 */
public class SpanFactory {

    /**
     * 
     * TraceIDGen description: produce trace id
     *
     */
    private static class TraceIDGen {

        private String ip;
        private String port;
        private volatile AtomicLong count = new AtomicLong(0);

        public TraceIDGen() {
            ip = NetworkHelper.getLocalIP();
        }

        public String getIP() {

            return this.ip;
        }

        public String getPort() {

            return this.port;
        }

        /**
         * 
         * @return the new trace id
         */
        public String id() {

            if (port == null) {
                port = UAVServer.instance().getServerInfo(CaptureConstants.INFO_APPSERVER_LISTEN_PORT) + "";
            }

            StringBuilder t = new StringBuilder();
            t.append(ip).append("_");
            t.append(port).append("_");
            t.append(System.currentTimeMillis()).append("_");
            t.append(Thread.currentThread().getId()).append("_");

            /**
             * 10W TPS, nearly the top
             */
            long ct = count.incrementAndGet();
            t.append(ct);

            if (ct >= 1000) {
                count.compareAndSet(1000, 0);
            }

            return t.toString();
        }
    }

    private static TraceIDGen idgen = new TraceIDGen();

    public static final TransmittableThreadLocal<Map<String, Span>> threadLocalTracker = new TransmittableThreadLocal<Map<String, Span>>() {

        @Override
        protected Map<String, Span> initialValue() {

            return new HashMap<String, Span>();
        }
    };

    /**
     * 通过一个父span来，创建一个span
     *
     * @param parentSpan
     * @param endpointType
     * @param endpointInfo
     * @return
     */
    public Span buildSpan(Span parentSpan, Span.SpanEndpointType endpointType, String endpointInfo) {

        Span span = null;

        if (parentSpan == null) {

            span = new Span(Span.SpanEndpointType.Root, idgen.id(), "1", null);
        }
        /**
         * NOTE: 方法级的SPAN使用其父SPAN的spanid和parentid，原因是方法级的调用栈是极其复杂的，
         * 
         * 考虑只能按时间来排列即可， 这样就避免考虑span的嵌套关系，以及造成span id过大过长等问题
         */
        else if (endpointType == Span.SpanEndpointType.Method) {
            span = new Span(endpointType, parentSpan.getTraceId(), parentSpan.getSpanId(), parentSpan.getParentId());
        }
        else {

            int subSpanSeq = parentSpan.getSubSpanSeqCounter().incrementAndGet();
            String spanId = parentSpan.getSpanId() + "." + subSpanSeq;
            span = new Span(endpointType, parentSpan.getTraceId(), spanId, parentSpan.getSpanId());
        }

        if (!StringHelper.isEmpty(endpointInfo)) {
            span.setEndpointInfo(endpointInfo);
        }

        span.setAppHostPort(idgen.getIP() + ":" + idgen.getPort());
        span.setStartTime(System.currentTimeMillis());

        return span;
    }

    /**
     * 从spanMeta来创建一个span
     * 
     * @param spanMeta
     * @param endpointType
     * @param endpointInfo
     * @return
     */
    public Span buildSpan(String spanMeta, Span.SpanEndpointType endpointType, String endpointInfo) {

        Span span = null;

        if (spanMeta == null) {

            span = new Span(Span.SpanEndpointType.Root, idgen.id(), "1", null);
        }
        else {

            String[] spanInfo = spanMeta.split("&");

            String traceId = spanInfo[0];
            String spanId = spanInfo[1];
            String parentId = spanInfo[2];

            span = new Span(endpointType, traceId, spanId, parentId);
        }

        if (!StringHelper.isEmpty(endpointInfo)) {
            span.setEndpointInfo(endpointInfo);
        }

        span.setAppHostPort(idgen.getIP() + ":" + idgen.getPort());
        span.setStartTime(System.currentTimeMillis());

        return span;
    }

    /**
     * 获取Span的元数据
     * 
     * @param span
     * @return
     */
    public String getSpanMeta(Span span) {

        StringBuffer b = new StringBuffer();

        b.append(span.getTraceId()).append("&");
        b.append(span.getSpanId()).append("&");
        b.append(span.getParentId()).append("&");

        return b.toString();
    }

    /**
     * 从ThreadLocal中获取span
     * 
     * @return
     */
    public Span getSpanFromContext(String key) {

        Map<String, Span> spanMap = threadLocalTracker.get();

        return spanMap.get(key);
    }

    /**
     * 从ThreadLocal中获取span, 并删除span
     * 
     * @param key
     * @return
     */
    public Span getRemoveSpanFromContext(String key) {

        Map<String, Span> spanMap = threadLocalTracker.get();

        return spanMap.remove(key);
    }

    /**
     * 向ThreadLocal设置span
     * 
     * @param span
     */
    public void setSpanToContext(String key, Span span) {

        Map<String, Span> spanMap = threadLocalTracker.get();

        spanMap.put(key, span);
    }

    /**
     * 移除当前线程的threadlocal值（不影响子线程）
     */
    public void removeCurrentThreadValue() {

        threadLocalTracker.removeCurrent();
    }

}
