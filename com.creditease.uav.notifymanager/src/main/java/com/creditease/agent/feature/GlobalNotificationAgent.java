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

package com.creditease.agent.feature;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.creditease.agent.feature.globalnotify.GlobalNotificationManager;
import com.creditease.agent.feature.globalnotify.NotificationEventRecordLiferKeeper;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.AgentFeatureComponent;

public class GlobalNotificationAgent extends AgentFeatureComponent {

    private ExecutorService globalNotificationManager_es = Executors.newSingleThreadExecutor();

    public GlobalNotificationAgent(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public void start() {

        // create GlobalNotificationManager
        GlobalNotificationManager amnm = new GlobalNotificationManager("GlobalNotificationManager", this.feature,
                "notifyhandlers");

        // start GlobalNotificationManager
        globalNotificationManager_es.execute(amnm);

        if (log.isTraceEnable()) {
            log.info(this, "GlobalNotificationManager started");
        }

        long expireTime = DataConvertHelper
                .toLong(this.getConfigManager().getFeatureConfiguration(this.feature, "expireTime"), 4 * 3600) * 1000;

        NotificationEventRecordLiferKeeper lifeKeeper = new NotificationEventRecordLiferKeeper(
                "NotificationEventRecordLiferKeeper", this.feature, expireTime);

        this.getTimerWorkManager().scheduleWork("NotificationEventRecordLiferKeeper", lifeKeeper, 0, expireTime / 2);

        if (log.isTraceEnable()) {
            log.info(this, "NotificationEventRecordLiferKeeper started");
        }
    }

    @Override
    public void stop() {

        // stop GlobalNotificationManager
        globalNotificationManager_es.shutdownNow();

        if (log.isTraceEnable()) {
            log.info(this, "GlobalNotificationManager stopped");
        }

        this.getTimerWorkManager().cancel("NotificationEventRecordLiferKeeper");

        if (log.isTraceEnable()) {
            log.info(this, "NotificationEventRecordLiferKeeper stopped");
        }

        super.stop();
    }

    @Override
    public Object exchange(String eventKey, Object... data) {

        if (null == data) {
            return null;
        }

        switch (eventKey) {
            case "global.notify":

                if (!NotificationEvent.class.isAssignableFrom(data[0].getClass())) {

                    log.warn(this, "Wrong Data Type for Event [global.notify]: " + data[0].getClass().getName());

                    return null;
                }

                NotificationEvent event = (NotificationEvent) data[0];

                GlobalNotificationManager ntfmanager = (GlobalNotificationManager) this.getConfigManager()
                        .getComponent(this.feature, "GlobalNotificationManager");

                ntfmanager.putData(event);

                return null;
        }

        throw new RuntimeException("Exchange Event [" + eventKey + "] handle FAIL: data=" + data);
    }

}
