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

package com.creditease.uav.profiling.spi;

public class ProfileConstants {

    public final static String PROELEM_JARS = "jars";
    public final static String PROELEM_COMPONENT = "cpt";
    public final static String PROELEM_LOGS = "logs";
    public final static String PROELEM_IPLINK = "iplnk";
    public static final String PROELEM_CLIENT = "clients";

    /**
     * profile instance id
     */
    public final static String PEI_LIB = "lib";
    public final static String PEI_URLS = "urls";
    public final static String PEI_CLIENTS = "clients";

    /**
     * profile context argument
     */
    public final static String PC_ARG_IPLNK = "pc.iplnk";
    public final static String PC_ARG_CLIENTADDR = "pc.client.addr";
    public final static String PC_ARG_IPLINK_TARGETURL = "pc.iplnk.url";
    public static final String PC_ARG_IPLINK_USRAGENT = "pc.iplnk.useragent";
    public static final String PC_ARG_SVRHOST = "pc.iplnk.svrhost";
    public static final String PC_ARG_APPID = "pc.iplnk.appid";

    public final static String PC_ARG_CLIENT_URL = "pc.client.url";
    public final static String PC_ARG_CLIENT_ACTION = "pc.client.action";
    public static final String PC_ARG_CLIENT_RC = "pc.client.rc";
    public static final String PC_ARG_CLIENT_TYPE = "pc.client.type";
    public static final String PC_ARG_CLIENT_TARGETSERVER = "pc.client.target.server";

    public static final String PC_ARG_UAVCLIENT_TAG = "pc.uavclient.tag";
    public static final String PC_ARG_PROXYHOST = "pc.proxy.host";

    private ProfileConstants() {

    }
}
