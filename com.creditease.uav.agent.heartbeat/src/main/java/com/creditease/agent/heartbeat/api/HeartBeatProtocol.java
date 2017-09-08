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

package com.creditease.agent.heartbeat.api;

public class HeartBeatProtocol {

    // ----------------------------------------------------------//
    /**
     * event
     */
    public static final String EVENT_DEFAULT = "event.default";

    public static final String EVENT_LOG_FAR = "event.log.far";

    // ----------------------------------------------------------//
    /**
     * event key
     */
    // node info
    public static final String EVENT_KEY_NODE_INFO = "node.info";
    // default return key
    public static final String EVENT_KEY_RETCODE = "rc";
    // update log catch info
    public static final String EVENT_KEY_FAR_INFO = "log.far.info";

    public static final String EVENT_KEY_TIME = "sync.time";

    // ----------------------------------------------------------//
    /**
     * event return code I means INFO E means ERROR W means WARN
     */
    // OK
    public static final String RC_I0000 = "I0000";

    // ----------------------------------------------------------//
    /**
     * store region key
     */
    public static final String STORE_REGION_UAV = "store.region.uav";

    // ----------------------------------------------------------//
    /**
     * store key
     */
    public static final String STORE_KEY_NODEINFO = "node.info";
    public static final String STORE_KEY_LIFEKEEP_LOCK = "lifekeep.lock";

    public static final String STORE_KEY_SERVICE_PREFIX = "service:";

}
