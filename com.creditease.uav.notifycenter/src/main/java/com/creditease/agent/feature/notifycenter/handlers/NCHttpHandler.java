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

package com.creditease.agent.feature.notifycenter.handlers;

import java.util.Map;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.feature.notifycenter.NCConstant;
import com.creditease.agent.feature.notifycenter.NCConstant.StateFlag;
import com.creditease.agent.feature.notifycenter.task.PersistentTask;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.AbstractHttpHandler;
import com.creditease.agent.spi.I1NQueueWorker;
import com.creditease.uav.cache.api.CacheManager;

/**
 * @author peihua
 *
 */

public class NCHttpHandler extends AbstractHttpHandler<UAVHttpMessage> {

    private CacheManager cm;
    private I1NQueueWorker qworker;

    public NCHttpHandler(String cName, String feature) {
        super(cName, feature);

        cm = (CacheManager) ConfigurationManager.getInstance().getComponent(this.feature, "NCCacheManager");

        qworker = (I1NQueueWorker) ConfigurationManager.getInstance().getComponent(this.feature,
                NCConstant.NC1NQueueWorkerName);
    }

    @Override
    public String getContextPath() {

        return "/nc/update";
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handle(UAVHttpMessage data) {

        String ntfkey = data.getRequest(NCConstant.NCEventParam);

        long startTime_gui = DataConvertHelper.toLong(data.getRequest(NCConstant.NCEventTime), -1);

        Map<String, String> ntfvalue = cm.getHash(NCConstant.STORE_REGION, NCConstant.STORE_KEY_NCINFO, ntfkey);
        String argsValue = ntfvalue.get(ntfkey);

        // no exist the cache value
        if (argsValue == null) {
            data.putResponse(UAVHttpMessage.ERR, "No Exist Cache for key[" + ntfkey + "]");
            return;
        }

        Map<String, Object> ntfValue = JSONHelper.toObject(argsValue, Map.class);

        long viewTime = DataConvertHelper.toLong(ntfValue.get(NCConstant.COLUMN_VIEWTIME), -1);

        long startTime = DataConvertHelper.toLong(ntfValue.get(NCConstant.COLUMN_STARTTIME), -1);

        // if there is viewTime, that means the event with the startTime has been viewed.
        // if the startTime_gui not equal startTime, means the event with the startTime_gui has been viewed
        if (viewTime != -1 || startTime_gui != startTime) {
            data.putResponse(UAVHttpMessage.RESULT, "Event[" + ntfkey + "](" + startTime + ") has been viewed");
            return;
        }

        NotificationEvent event = new NotificationEvent("Viewed", "ViewTitle", "tempDescription");

        ntfValue.put(NCConstant.COLUMN_STATE, StateFlag.VIEW.getStatFlag());
        ntfValue.put(NCConstant.COLUMN_VIEWTIME, System.currentTimeMillis());

        event.addArg(NCConstant.NTFKEY, ntfkey);
        event.addArg(NCConstant.NTFVALUE, JSONHelper.toString(ntfValue));
        event.addArg(NCConstant.NCFirstEvent, "false");

        PersistentTask taskP = new PersistentTask(cName, feature);

        taskP.put(NCConstant.NCEventParam, event);

        qworker.put(taskP);

        data.putResponse(UAVHttpMessage.RESULT, "OK");
    }

}
