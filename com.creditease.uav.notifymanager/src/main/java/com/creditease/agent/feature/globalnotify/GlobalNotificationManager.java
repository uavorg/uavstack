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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.AbstractHandleWorkComponent;
import com.creditease.agent.spi.AbstractHandler;

/**
 * GlobalNotificationManager 负责收集Node内部所有的Notification事件并进行发送动作
 * 
 * @author zhen zhang
 *
 */
public class GlobalNotificationManager
        extends AbstractHandleWorkComponent<NotificationEvent, AbstractHandler<NotificationEvent>> {

    public static class NotificationEventRecord {

        private String key;

        private NotificationEvent event;

        private long latestReportTime;

        private long firstReportTime;

        private long frozenStartTime;

        private AtomicInteger reportNum = new AtomicInteger(0);

        public NotificationEventRecord(NotificationEvent event) {

            this.event = event;

            this.key = getUUID(event);

            this.latestReportTime = event.getTime();

            this.frozenStartTime = event.getTime();

            this.firstReportTime = event.getTime();

            reportNum.incrementAndGet();
        }

        public void increReportNum() {

            reportNum.incrementAndGet();
        }

        public int getReportNum() {

            return reportNum.get();
        }

        public long getFirstReportTime() {

            return firstReportTime;
        }

        public String getKey() {

            return key;
        }

        public void setKey(String key) {

            this.key = key;
        }

        public NotificationEvent getEvent() {

            return event;
        }

        public void setEvent(NotificationEvent event) {

            this.event = event;
        }

        public long getLatestReportTime() {

            return latestReportTime;
        }

        public void setLatestReportTime(long latestReportTime) {

            this.latestReportTime = latestReportTime;
        }

        public long getFrozenStartTime() {

            return frozenStartTime;
        }

        public void setFrozenStartTime(long frozenStartTime) {

            this.frozenStartTime = frozenStartTime;
        }

        /**
         * get event UUID
         * 
         * @param event
         * @return
         */
        public static String getUUID(NotificationEvent event) {

            return event.getArg("feature") + ":" + event.getArg("component") + ":" + event.getId() + ":"
                    + event.getTitle().hashCode();
        }

    }

    private Map<String, NotificationEventRecord> NotificationEventRecordMap = new ConcurrentHashMap<String, NotificationEventRecord>();

    // default frozenTime is 5 min
    private long frozenTime = 300000;

    public GlobalNotificationManager(String cName, String feature, String initHandlerKey) {
        super(cName, feature, initHandlerKey);

        this.frozenTime = DataConvertHelper
                .toLong(this.getConfigManager().getFeatureConfiguration(this.feature, "frozenTime"), 300) * 1000;
    }

    @Override
    public void putData(NotificationEvent event) {

        if (event == null) {
            return;
        }

        /**
         * if there is tag EVENT_Tag_NoBlock, then not 压制提交频率
         */
        boolean isNoBlock = ("true".equalsIgnoreCase(event.getArg(NotificationEvent.EVENT_Tag_NoBlock))) ? true : false;

        if (isNoBlock == true) {
            super.putData(event);
            return;
        }

        /**
         * NOTE: 压制提交频率
         */
        String newComeEventKey = NotificationEventRecord.getUUID(event);

        /**
         * OLD Notification EVENT check
         */
        if (NotificationEventRecordMap.containsKey(newComeEventKey)) {

            NotificationEventRecord ner = NotificationEventRecordMap.get(newComeEventKey);

            // increase report numbers
            ner.increReportNum();

            /**
             * check if timeSpan is enough to send
             */
            long curTime = System.currentTimeMillis();

            long timeSpan = curTime - ner.getFrozenStartTime();

            // still froze the event
            if (timeSpan < this.frozenTime) {

                // set latest report time for expire the notification event if there is on further report for a long
                // time (such as 4h) that may
                // mean the issue is fixed, then we should clean this event record
                ner.setLatestReportTime(event.getTime());

                return;
            }
            // exceed frozeTime, then SEND the latest event and some stat info
            else {
                // add firstReportTime
                event.addArg("firstReportTime", String.valueOf(ner.getFirstReportTime()));
                // add reportNum
                event.addArg("reportNum", String.valueOf(ner.getReportNum()));

                // reset forzeStartTime
                ner.setFrozenStartTime(curTime);
            }
        }
        /**
         * NEW COME Notification EVENT SEND
         */
        else {
            NotificationEventRecord record = new NotificationEventRecord(event);

            NotificationEventRecordMap.put(newComeEventKey, record);
        }

        super.putData(event);
    }

    public Map<String, NotificationEventRecord> getNotificationEventRecordMap() {

        return NotificationEventRecordMap;
    }

}
