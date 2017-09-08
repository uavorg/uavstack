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

package com.creditease.uav.feature.healthmanager.datastore.test;

public class HealthManagerConstants {

    /**
     * Default DataStoreNames, Configuration data will replaced it for consistant Usage
     */
    public static final String DataStore_Monitor = "monitorDataStore";
    public static final String DataStore_Profile = "profileDataStore";
    public static final String DataStore_Notification = "notifyDataStore";
    public static final String DataStore_Log = "logDataStore";

    /**
     * Consumers
     */
    public static final String COMSUMER_MONITOR = "monitorDataConsumer";
    public static final String COMSUMER_NOTIFY = "notificationConsumer";
    public static final String COMSUMER_PROFILE = "profileDataConsumer";
    public static final String COMSUMER_LOG = "logDataConsumer";

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
     * STORE LOCK KEY for profile info
     */
    public static final String STORE_KEY_PROFILEINFO_LIFEKEEP_LOCK = "profile.lifekeep.lock";
}
