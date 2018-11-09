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
import com.creditease.agent.feature.notifycenter.NCEventStatusManager;
import com.creditease.agent.feature.notifycenter.task.PersistentTask;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.AbstractHttpHandler;
import com.creditease.agent.spi.I1NQueueWorker;

/**
 * @author peihua
 *
 */

public class NCHttpHandler extends AbstractHttpHandler<UAVHttpMessage> {

    private NCEventStatusManager eventStatusManager;
    private I1NQueueWorker qworker;

    public NCHttpHandler(String cName, String feature) {
        super(cName, feature);

        eventStatusManager = (NCEventStatusManager) ConfigurationManager.getInstance().getComponent(feature,
                "EventStatusManager");
        qworker = (I1NQueueWorker) ConfigurationManager.getInstance().getComponent(this.feature,
                NCConstant.NC1NQueueWorkerName);
    }

    @Override
    public String getContextPath() {

        return "/nc/update";
    }

    @Override
    public void handle(UAVHttpMessage data) {

        String action = data.getRequest("action");

        switch (action) {
            case "view":
                updateViewStatus(data);
                break;
            case "process":
                updateProcessStatus(data);
                break;
            default:
                data.putResponse(UAVHttpMessage.ERR, "No Support action: " + action);
        }
    }

    private void updateViewStatus(UAVHttpMessage data) {

        String ntfkey = data.getRequest(NCConstant.NTFKEY);
        long startTime_gui = DataConvertHelper.toLong(data.getRequest(NCConstant.NTFTime), -1);

        String stateDataStr = eventStatusManager.getEventCache(ntfkey);
        // no exist the cache value
        if (stateDataStr == null) {
            data.putResponse(UAVHttpMessage.ERR, "No Exist Cache for key[" + ntfkey + "]");
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> ntfValue = JSONHelper.toObject(stateDataStr, Map.class);
        int curState = DataConvertHelper.toInt(ntfValue.get(NCConstant.COLUMN_STATE), 0);
        long startTime = DataConvertHelper.toLong(ntfValue.get(NCConstant.COLUMN_STARTTIME), -1);

        // if the startTime_gui not equal startTime, means the event with the startTime_gui has been expired
        if (startTime_gui != startTime) {
            data.putResponse(UAVHttpMessage.RESULT, "Event[" + ntfkey + "](" + startTime + ") has been expired");
            return;
        }

        if (curState >= StateFlag.VIEW.getStatFlag()) {
            data.putResponse(UAVHttpMessage.RESULT, "Event[" + ntfkey + "](" + startTime + ") has been viewed");
            return;
        }

        ntfValue.put(NCConstant.COLUMN_STATE, StateFlag.VIEW.getStatFlag());
        ntfValue.put(NCConstant.COLUMN_VIEWTIME, System.currentTimeMillis());

        NotificationEvent event = new NotificationEvent("ViewID", "ViewTitle", "ViewDescription");
        event.addArg(NCConstant.NTFKEY, ntfkey);
        event.addArg(NCConstant.NTFVALUE, JSONHelper.toString(ntfValue));
        event.addArg(NCConstant.NCFirstEvent, "false");

        PersistentTask taskP = new PersistentTask(cName, feature);
        taskP.put(NCConstant.NCEventParam, event);
        qworker.put(taskP);

        data.putResponse(UAVHttpMessage.RESULT, "OK");
    }

    private void updateProcessStatus(UAVHttpMessage data) {

        String ntfkey = data.getRequest(NCConstant.NTFKEY);
        long startTime_gui = DataConvertHelper.toLong(data.getRequest(NCConstant.NTFTime), -1);

        String stateDataStr = eventStatusManager.getEventCache(ntfkey);
        // no exist the cache value
        if (stateDataStr == null) {
            data.putResponse(UAVHttpMessage.ERR, "No Exist Cache for key[" + ntfkey + "]");
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> ntfValue = JSONHelper.toObject(stateDataStr, Map.class);
        int curState = DataConvertHelper.toInt(ntfValue.get(NCConstant.COLUMN_STATE), 0);
        long startTime = DataConvertHelper.toLong(ntfValue.get(NCConstant.COLUMN_STARTTIME), -1);

        // if the startTime_gui not equal startTime, means the event with the startTime_gui has been expired
        if (startTime_gui != startTime) {
            data.putResponse(UAVHttpMessage.RESULT, "Event[" + ntfkey + "](" + startTime + ") has been expired");
            return;
        }

        if (curState == StateFlag.PROCESS.getStatFlag()) {
            data.putResponse(UAVHttpMessage.RESULT, "Event[" + ntfkey + "](" + startTime + ") has been processed");
            return;
        }

        ntfValue.put(NCConstant.COLUMN_STATE, StateFlag.PROCESS.getStatFlag());
        ntfValue.put(NCConstant.COLUMN_PROCESSTIME, System.currentTimeMillis());

        NotificationEvent event = new NotificationEvent("ProcessID", "ProcessTitle", "ProcessDescription");
        event.addArg(NCConstant.NTFKEY, ntfkey);
        event.addArg(NCConstant.NTFVALUE, JSONHelper.toString(ntfValue));
        event.addArg(NCConstant.NCFirstEvent, "false");

        PersistentTask taskP = new PersistentTask(cName, feature);
        taskP.put(NCConstant.NCEventParam, event);
        qworker.put(taskP);

        data.putResponse(UAVHttpMessage.RESULT, "OK");
    }

}
