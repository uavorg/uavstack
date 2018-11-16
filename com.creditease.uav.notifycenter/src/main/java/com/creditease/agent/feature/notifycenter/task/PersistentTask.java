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

package com.creditease.agent.feature.notifycenter.task;

import java.util.LinkedHashMap;
import java.util.Map;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.feature.notifycenter.NCConstant;
import com.creditease.agent.feature.notifycenter.NCEventStatusManager;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.Abstract1NTask;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.uav.datastore.api.DataStoreMsg;
import com.creditease.uav.datastore.api.DataStoreProtocol;

/**
 * 
 * @author peihua
 *
 */
public class PersistentTask extends Abstract1NTask {

    private NCEventStatusManager eventStatusManager;

    public PersistentTask(String name, String feature) {
        super(name, feature);
        eventStatusManager = (NCEventStatusManager) ConfigurationManager.getInstance().getComponent(feature, "EventStatusManager");
    }

    @Override
    public void run() {

        NotificationEvent event = (NotificationEvent) this.get(NCConstant.NCEventParam);
        // 1.同步cache
        updateCache(event);

        // 2.同步Mongodb
        updateDatabase(event);
    }

    public void updateCache(NotificationEvent event) {

        eventStatusManager.updateEventCache(event);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void updateDatabase(NotificationEvent event) {

        DataStoreMsg msg = new DataStoreMsg();
        Map<String, Map> condition = new LinkedHashMap<String, Map>();
        Map<String, Object> where = new LinkedHashMap<String, Object>();
        Map<String, Object> update = new LinkedHashMap<String, Object>();
        Map<String, Object> set = new LinkedHashMap<String, Object>();

        Map<String, Object> ntfargs = JSONHelper.toObject(event.getArg(NCConstant.NTFVALUE), Map.class);

        // 如果当前预警是第一条数据,如果不是第一条数据，只更新第一条
        if (event.getArg(NCConstant.NCFirstEvent).equals("true")) {

            if (log.isDebugEnable()) {
                log.debug(this, "the Event is new for key:" + event.getArg(NCConstant.NTFKEY));
                log.debug(this, "the Event is new for start Time :" + event.getTime());
            }
            where.put(NCConstant.COLUMN_NTFKEY, event.getArg(NCConstant.NTFKEY));
            where.put("time", event.getTime());// fix bug
            set.put(NCConstant.COLUMN_FIRSTRECORD, "true");
        }
        else {
            if (log.isDebugEnable()) {
                log.debug(this, "the Event exists for key:" + event.getArgs(true).get(NCConstant.NTFKEY));
                log.debug(this, "the Event exists for start Time :" + event.getArg(NCConstant.COLUMN_STARTTIME));
            }
            where.put(NCConstant.COLUMN_NTFKEY, event.getArg(NCConstant.NTFKEY));
            where.put("time", ntfargs.get(NCConstant.COLUMN_STARTTIME));
            where.put(NCConstant.COLUMN_FIRSTRECORD, "true");
        }

        set.put(NCConstant.COLUMN_STATE, ntfargs.get(NCConstant.COLUMN_STATE));

        if (ntfargs.get(NCConstant.COLUMN_RETRY_COUNT) != null) {
            set.put(NCConstant.COLUMN_RETRY_COUNT, ntfargs.get(NCConstant.COLUMN_RETRY_COUNT));
        }
        if (ntfargs.get(NCConstant.COLUMN_VIEWTIME) != null) {
            set.put(NCConstant.COLUMN_VIEWTIME, ntfargs.get(NCConstant.COLUMN_VIEWTIME));
        }
        if (ntfargs.get(NCConstant.COLUMN_PROCESSTIME) != null) {
            set.put(NCConstant.COLUMN_PROCESSTIME, ntfargs.get(NCConstant.COLUMN_PROCESSTIME));
        }
        if (ntfargs.get(NCConstant.COLUMN_LATESTIME) != null) {
            set.put(NCConstant.COLUMN_LATESTIME, ntfargs.get(NCConstant.COLUMN_LATESTIME));
        }
        if (ntfargs.get(NCConstant.COLUMN_LATESTRECORDTIME) != null) {
            set.put(NCConstant.COLUMN_LATESTRECORDTIME, ntfargs.get(NCConstant.COLUMN_LATESTRECORDTIME));
        }
        if (ntfargs.get(NCConstant.EVENT_COUNT) != null) {
            set.put(NCConstant.EVENT_COUNT, ntfargs.get(NCConstant.EVENT_COUNT));
        }

        update.put("set", set);
        condition.put("where", where);
        condition.put("update", update);
        msg.put(DataStoreProtocol.DATASTORE_NAME, MonitorDataFrame.MessageType.Notification.toString());
        msg.put(DataStoreProtocol.MONGO_QUERY_SQL, JSONHelper.toString(condition));
        msg.put(DataStoreProtocol.MONGO_COLLECTION_NAME, NCConstant.MONGO_COLLECTION_NOTIFY);

        if (log.isDebugEnable()) {
            log.debug(this, "NC Update Mongodb condition: " + JSONHelper.toString(condition));
        }
        // Exchange消息给HM做数据库更新
        AgentFeatureComponent afc = (AgentFeatureComponent) ConfigurationManager.getInstance()
                .getComponent("healthmanager", "HealthManager");

        if (null != afc) {
            boolean flag = (boolean) afc.exchange("opt.update", msg);
            if (log.isDebugEnable()) {
                log.debug(this, "NC Update Mongodb result: " + flag);
            }
        }
    }

}
