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

package com.creditease.uav.apm.slowoper.spi;

/**
 * 
 * 慢操作常量
 *
 */
public class SlowOperConstants {

    private SlowOperConstants() {
    }

    // 标记重调用链类型，对应span中的epinfo信息
    public static final String SLOW_OPER_HTTP_SERVER = "http.service";
    public static final String SLOW_OPER_HTTP_CLIENT = "apache.http.Client";
    public static final String SLOW_OPER_HTTP_ASY_CLIENT = "apache.http.AsyncClient";
    public static final String SLOW_OPER_MQ_RABBIT_CONSUMER = "mq.service";
    public static final String SLOW_OPER_MQ_RABBIT_PRODUCER = "rabbitmq.client";
    public static final String SLOW_OPER_JDBC_CLIENT = "jdbc.client";
    public static final String SLOW_OPER_METHOD = "method";

    // 重调用链处理各种协议关键字
    public static final String PROTOCOL_HTTP_HEADER = "protocol.http.header";
    public static final String PROTOCOL_HTTP_BODY = "protocol.http.body";
    public static final String PROTOCOL_HTTP_REQ_HEADER = "protocol.http.req.header";
    public static final String PROTOCOL_HTTP_REQ_BODY = "protocol.http.req.body";
    public static final String PROTOCOL_HTTP_RSP_HEADER = "protocol.http.rsp.header";
    public static final String PROTOCOL_HTTP_RSP_BODY = "protocol.http.rsp.body";
    public static final String PROTOCOL_HTTP_EXCEPTION = "protocol.http.exception";

    public static final String PROTOCOL_MQ_RABBIT_BODY = "protocol.mq.rabbit.body";

    public static final String PROTOCOL_JDBC_SQL = "protocol.jdbc.sql";
    public static final String PROTOCOL_JDBC_RESULT = "protocol.jdbc.result";

    public static final String PROTOCOL_METHOD_PARAMS = "protocol.method.params";
    public static final String PROTOCOL_METHOD_RETURN = "protocol.method.return";
}
