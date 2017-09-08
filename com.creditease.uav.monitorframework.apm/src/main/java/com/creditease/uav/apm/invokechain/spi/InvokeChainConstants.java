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

package com.creditease.uav.apm.invokechain.spi;

public class InvokeChainConstants {

    public static enum CapturePhase {
        PRECAP, DOCAP
    }

    private InvokeChainConstants() {
    }

    // capture point
    public final static String CHAIN_APP_SERVICE = "invokechain.service";
    public final static String CHAIN_APP_CLIENT = "invokechain.client";
    public final static String CHAIN_APP_METHOD = "invokechain.method";

    // key for SpanTransfter on HTTP-Header
    public static final String PARAM_HTTPHEAD_SPANINFO = "param.http.head.span";
    public static final String PARAM_INTECEPTCONTEXT = "param.interceptcontext";
    public static final String PARAM_REMOTE_SRC_INFO = "param.remote.src.info";

    // key for SpanTransfter on MQ-Header
    public static final String PARAM_MQHEAD_SPANINFO = "param.mq.head.span";
    // key for SpanTransfter on rpc-header
    public static final String PARAM_RPCHEAD_SPANINFO = "param.rpc.head.span";
    public static final String PARAM_RPCHEAD_INFO = "param.rpc.head.info";
    // MQ头信息
    public static final String PARAM_MQHEAD_INFO = "param.mq.head.info";

    // key for SpanTransfter
    public static final String PARAM_SPAN_KEY = "span.key";

    // 应用类信息距离client类层数差
    public static final String CLIENT_IT_KEY = "thread.client.key";

    // 关键类名(执行类名)
    public static final String CLIENT_IT_CLASS = "thread.client.class";
    // 执行方法名
    public static final String CLIENT_IT_METHOD = "thread.client.method";

    // Client Span 在ThreadLocal存储时使用的key， preCap来设置这个key，通过context params传递，doCap取这个key的value就是Client
    // Span在ThreadLocal里面的值
    public static final String CLIENT_SPAN_THREADLOCAL_STOREKEY = "client.span.store.key";

    // Method Span
    public static final String METHOD_SPAN_CLS = "method.span.class";
    public static final String METHOD_SPAN_MTD = "method.span.method";
    public static final String METHOD_SPAN_MTDSIGN = "method.span.sign";
    public static final String METHOD_SPAN_APPID = "method.span.appid";
    public static final String METHOD_SPAN_STOREKEY = "method.span.storekey";

}
