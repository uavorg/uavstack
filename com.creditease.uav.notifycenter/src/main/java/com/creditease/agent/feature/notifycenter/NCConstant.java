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

package com.creditease.agent.feature.notifycenter;

public class NCConstant {

    private NCConstant() {
    }

    public enum StateFlag {

        NEWCOME(0), UPDATE(10), VIEW(15), VIEWUPDATE(20), PROCESS(25);

        private final int flag;

        private StateFlag(int flag) {
            this.flag = flag;
        }

        public int getStatFlag() {

            return flag;
        }
    }

    public static final String NC1NQueueWorkerName = "NC1NQueueWorker";
    public static final String NCEventParam = "ncevent";

    /**
     * notify control flag
     */
    public static final String seperator = "@";

    /**
     * NTF key-value
     */
    public static final String NTFKEY = "ntfkey";
    public static final String NTFVALUE = "ntfvalue";
    public static final String NTFTime = "time";
    public static final String NCFirstEvent = "firstEvent";
    public static final String NCAction = "ntfkeyaction";

    public static final String NCCacheActionP = "put";
    public static final String NCCacheActionD = "delete";
    public static final String NCCacheActionN = "None";

    /**
     * NTF Storage COLUMN key-value
     */
    public static final String COLUMN_NTFKEY = "ntfkey";
    public static final String COLUMN_FIRSTRECORD = "firstrecord";
    public static final String COLUMN_STATE = "state";
    public static final String COLUMN_STARTTIME = "start_ts";
    public static final String COLUMN_LATESTIME = "latest_ts";
    public static final String COLUMN_VIEWTIME = "view_ts";
    public static final String COLUMN_PROCESSTIME = "process_ts";
    public static final String COLUMN_LATESTRECORDTIME = "latestrecord_ts";
    public static final String COLUMN_RETRY_COUNT = "retry";
    public static final String COLUMN_PRIORITY = "priority";// default is 0

    /**
     * Priority constant flag
     */
    public static final String PRIORITYFLAG = "pri.flag";
    public static final String PRIORITYLEVEL = "pri.level";

    /**
     * Action Type
     */
    public static final String ACTION4MAIL = "mail";
    public static final String ACTION4SMS = "sms";
    public static final String ACTION4HTTP = "httpcall";
    public static final String ACTION4PUSHNTF = "pushntf";
    public static final String ACTION4THREADANALYSIS = "threadanalysis";

    /**
     * Action param value
     */
    public static final String ACTIONVALUE = "actionValue";

    /**
     * CACHE STORE
     */
    public static final String STORE_REGION = "store.region.uav";

    /**
     * Notification dataStorename & collection name
     **/
    public static final String MONGO_COLLECTION_NOTIFY = "uav_notify";
    
    public static final String EVENT_COUNT = "event_count";

}
