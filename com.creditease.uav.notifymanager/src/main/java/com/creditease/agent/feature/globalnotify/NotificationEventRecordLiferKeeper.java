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

package com.creditease.agent.feature.globalnotify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.creditease.agent.feature.globalnotify.GlobalNotificationManager.NotificationEventRecord;
import com.creditease.agent.spi.AbstractTimerWork;

public class NotificationEventRecordLiferKeeper extends AbstractTimerWork {

    // default expire time is 4 hour
    private long expireTime = 4 * 3600 * 1000;

    public NotificationEventRecordLiferKeeper(String cName, String feature, long expireTime) {
        super(cName, feature);

        this.expireTime = expireTime;
    }

    @Override
    public void run() {

        GlobalNotificationManager gnm = (GlobalNotificationManager) this.getConfigManager().getComponent(this.feature,
                "GlobalNotificationManager");

        Map<String, NotificationEventRecord> map = gnm.getNotificationEventRecordMap();

        List<String> toBeDelKeys = new ArrayList<String>();

        for (NotificationEventRecord record : map.values()) {

            // get latestReportTime
            long latestReportTime = record.getLatestReportTime();

            long curTime = System.currentTimeMillis();

            /**
             * if reach the expireTime, we should delete this record
             */
            if (curTime - latestReportTime < this.expireTime) {
                continue;
            }

            toBeDelKeys.add(record.getKey());

            /**
             * TODO: should we send the unsended event before deletion??? then we can know when the event is closed and
             * get some stat, but may trigger warning SMS & EMail, a little complex
             */
        }

        /**
         * delete the expired records
         */
        for (String key : toBeDelKeys) {
            map.remove(key);
        }
    }

}
