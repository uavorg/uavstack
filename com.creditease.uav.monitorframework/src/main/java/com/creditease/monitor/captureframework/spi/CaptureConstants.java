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

package com.creditease.monitor.captureframework.spi;

/**
 * @author zhen zhang
 */
public class CaptureConstants {

    // 1 day ttl for tmax, tmin
    public final static long MEI_INST_TTL = 24 * 3600 * 1000;

    // default monitor id
    public final static String MONITOR_SERVER = "server";
    public final static String MONITOR_CXF = "cxf";
    public final static String MONITOR_CLIENT = "client";

    // capture point
    public final static String CAPPOINT_SERVER_CONNECTOR = "CoyoteAdapter.service";
    public final static String CAPPOINT_MONITOR_DATAOBSERVER = "DataObserver.getData";
    public final static String CAPPOINT_CXF_CLIENT_CALLFLOW = "CXF.Client.CallFlow";
    public final static String CAPPOINT_APP_CLIENT = "AP.APPClient.CallFlow";

    // default monitor element id
    public final static String MOELEM_CXF_CLIENT_E2ETIME = "cxfE2E";
    public final static String MOELEM_CXF_SERVICE_TIME = "cxfSR";
    public final static String MOELEM_SERVER_RESPTIME_SYSTEM = "serverResp";
    public final static String MOELEM_SERVER_RESPTIME_APP = "appResp";
    public final static String MOELEM_SERVER_RESPTIME_URL = "urlResp";
    public final static String MOELEM_JVMSTATE = "jvm";
    public final static String MOELEM_CLIENT_RESPTIME = "clientResp";

    // default monitor element instance id
    public final static String MEI_RESP_SUMTIME = "tsum";
    public final static String MEI_RESP_AVGTIME = "tavg";
    public final static String MEI_RESP_MAXTIME = "tmax";
    // tmax timestamp
    public final static String MEI_RESP_MAXTIME_ST = "tmax_st";
    public final static String MEI_RESP_MINTIME = "tmin";
    // tmin timestamp
    public static final String MEI_RESP_MINTIME_ST = "tmin_st";
    public final static String MEI_COUNTER = "count";
    /**
     * for server side: error is [400,~) warn [300,400)
     * 
     * for client side: error is exception return
     */
    public final static String MEI_ERROR = "err";
    public final static String MEI_WARN = "warn";
    public final static String MEI_RC = "RC";
    public final static String MEI_AC = "AC";
    public final static String MEI_AC_ERROR = "ACERR";

    // default capture info data id
    // Common Info
    public final static String INFO_APPSERVER_VENDOR = "uav.engine.vendor";
    public final static String INFO_CAPCONTEXT_TAG = "uav.capcontext.tag";

    // Application Server
    public final static String INFO_APPSERVER_CONNECTOR_REQUEST_URL = "info.appserver.request.url";
    public final static String INFO_APPSERVER_CONNECTOR_SERVLET = "info.appserver.servlet";
    public final static String INFO_APPSERVER_CONNECTOR_RESPONSECODE = "info.appserver.response.code";
    public final static String INFO_APPSERVER_CONNECTOR_RESPONSESTATE = "info.appserver.response.state";
    public static final String INFO_APPSERVER_CONNECTOR_FORWARDADDR = "info.appserver.forwardaddr";
    public static final String INFO_APPSERVER_CONNECTOR_CLIENTADDR = "info.appserver.clientaddr";
    public static final String INFO_APPSERVER_CONNECTOR_LISTENPORT = "info.appserver.listenport";
    public static final String INFO_APPSERVER_CONNECTOR_CONTEXT = "info.appserver.context";
    public final static String INFO_APPSERVER_CONNECTOR_CONTEXT_REALPATH = "info.appserver.context.realpath";
    public final static String INFO_APPSERVER_CLIENT_USRAGENT = "info.appserver.client.useragent";
    public final static String INFO_APPSERVER_LISTEN_PORT = "info.appserver.port";
    public static final String INFO_APPSERVER_UAVCLIENT_TAG = "info.appserver.uavclient.tag";
    public static final String INFO_APPSERVER_PROXY_HOST = "info.appserver.proxy.host";
    public static final String INFO_APPSERVER_APPID = "info.appserver.appid";

    // Client for all: http, ws, jdbc, mongo, redis, note everthing can be URI
    public static final String INFO_CLIENT_REQUEST_URL = "info.client.request.url";
    public static final String INFO_CLIENT_REQUEST_ACTION = "info.client.request.action"; // any operation can be the
                                                                                          // action
    public static final String INFO_CLIENT_RESPONSECODE = "info.client.response.code";
    public static final String INFO_CLIENT_RESPONSESTATE = "info.client.repsonse.state";
    public static final String INFO_CLIENT_APPID = "info.client.appid";
    public static final String INFO_CLIENT_TYPE = "info.client.type";

    public static final String INFO_CLIENT_TARGETSERVER = "info.client.target.server";

    public static final String INFO_MOF_METAPATH = "info.mof.metapath";

    private CaptureConstants() {

    }
}
