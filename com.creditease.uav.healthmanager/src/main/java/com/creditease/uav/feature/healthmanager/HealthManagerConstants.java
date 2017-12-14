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

package com.creditease.uav.feature.healthmanager;

public class HealthManagerConstants {

    private HealthManagerConstants() {
    }

    /**
     * Consumers
     */
    public static final String COMSUMER_MONITOR = "monitorDataConsumer";
    public static final String COMSUMER_NOTIFY = "notificationConsumer";
    public static final String COMSUMER_PROFILE = "profileDataConsumer";
    public static final String COMSUMER_LOG = "logDataConsumer";
    public static final String COMSUMER_NODE = "nodeinfoDataConsumer";

    /**
     * Mongo DB CollectionName
     */
    public static final String MONGO_COLLECTION_NOTIFY = "uav_notify";
    public static final String MONGO_COLLECTION_PROFILE = "uav_profile";

    /**
     * InfluxDB MEASUREMENTS
     */
    public static final String INFLUXDB_POINT_MEASUREMENT = "uav_monitor";

    /**
     * HBase DB TableName &
     */
    public static final String HBASE_TABLE_LOGDATA = "Log_data";
    public static final String HBASE_TABLE_LOGTYPE = "Log_type";
    public static final String HBASE_DEFAULT_FAMILY = "cf";
    /**
     * STORE REGION
     */
    public static final String STORE_REGION_UAV = "store.region.uav";

    /**
     * STORE KEY for profile info
     */
    public static final String STORE_KEY_PROFILEINFO = "profile.info";

    /**
     * STORE KEY for profile info of application clients
     */
    public static final String STORE_KEY_PROFILEINFO_APPCLIENT = "profile.info.client";

    /**
     * STORE KEY for profile info jar lib
     */
    public static final String STORE_KEY_PROFILEINFO_JARLIB = "profile.info.jarlib";
    public static final String STORE_KEY_PROFILEINFO_DUBBOPROVIDER = "profile.info.dubboprovider";
    public static final String STORE_KEY_PROFILEINFO_MSCPHTTP = "profile.info.mscphttp";
    public static final String STORE_KEY_PROFILEINFO_MSCPTIMEWORKER = "profile.info.mscptimeworker";
    public static final String STORE_KEY_PROFILEINFO_FILTER = "profile.info.filter";
    public static final String STORE_KEY_PROFILEINFO_LISTENER = "profile.info.listener";
    public static final String STORE_KEY_PROFILEINFO_SERVLET = "profile.info.servlet";
    public static final String STORE_KEY_PROFILEINFO_JAXWS = "profile.info.jaxws";
    public static final String STORE_KEY_PROFILEINFO_JAXWSP = "profile.info.jaxwsp";
    public static final String STORE_KEY_PROFILEINFO_JAXRS = "profile.info.jaxrs";
    public static final String STORE_KEY_PROFILEINFO_SPRINGMVC = "profile.info.springmvc";
    public static final String STORE_KEY_PROFILEINFO_SPRINGMVCREST = "profile.info.springmvcrest";
    public static final String STORE_KEY_PROFILEINFO_STRUTS2 = "profile.info.struts2";

    /**
     * STORE KEY for monitor
     */
    public static final String STORE_KEY_MDCACHE_PREFIX = "MD";

    /**
     * STORE LOCK KEY for profile info
     */
    public static final String STORE_KEY_PROFILEINFO_LIFEKEEP_LOCK = "profile.lifekeep.lock";

    /**
     * DataStore operation Type
     */
    public static final String QUERY = "opt.query";
    public static final String INSERT = "opt.insert";
    public static final String UPDATE = "opt.update";

}
