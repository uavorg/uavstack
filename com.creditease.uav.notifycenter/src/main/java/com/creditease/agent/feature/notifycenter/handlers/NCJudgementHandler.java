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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.feature.notifycenter.NCConstant;
import com.creditease.agent.feature.notifycenter.NCConstant.StateFlag;
import com.creditease.agent.feature.notifycenter.NCEventStatusManager;
import com.creditease.agent.feature.notifycenter.task.NotificationTask;
import com.creditease.agent.feature.notifycenter.task.PersistentTask;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.DataStoreHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.AbstractBaseAction;
import com.creditease.agent.spi.AbstractHandler;
import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.I1NQueueWorker;
import com.creditease.agent.spi.IActionEngine;

/**
 * 
 * @author peihua
 *
 * @actionChange add priorityList
 * 
 *               args:
 * 
 *               {
 * 
 *               "action_mail": "[\"priority1@mail.com,priority1@mail.com\", \"priority2@mail.com\"]",
 * 
 *               "action_sms": "[\"1353334111,135333411,135333433\", \"135333422,135333432\"]",
 * 
 *               "action_phone":"[\"135333433,135333433\", \"135333433,135333433\"]",
 * 
 *               }
 * 
 */

public class NCJudgementHandler extends AbstractHandler<NotificationEvent> {
    
    
    private class NotificationConvergenceAction extends AbstractBaseAction {
        
        /**
         * @param cName
         * @param feature
         * @param engine
         */
        public NotificationConvergenceAction(String cName, String feature, IActionEngine engine) {
            
            super(cName, feature, engine);
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public void doAction(ActionContext context) throws Exception {
            
            String eventKey = (String) context.getParam("eventKey");
            NotificationEvent event = (NotificationEvent) context.getParam("event");
            String stateDataStr = (String) context.getParam("stateDataStr");
                        
            Map<String, Object> stateData = null;
            
            /**
             * if none, take this event as the first event record
             */
            if (null == stateDataStr) {
                
                if (log.isDebugEnable()) {
                    log.debug(this, "NC Convergence Event happens FIRST TIME: NTFKEY=" + eventKey + ",time=" + event.getTime());
                }
                
                Map<String, Object> statusSet = playAsFirstEventRecordConvergence(event, eventKey);
                doNotificationConvergence(event, eventKey, statusSet);
                return;
            }
                
            stateData = JSONHelper.toObject(stateDataStr, Map.class);
            
            if (log.isDebugEnable()) {
                log.debug(this, "NC Event happens with stateData: "+stateDataStr);
            }
                
            long curTime = System.currentTimeMillis();
            
            long viewTime = DataConvertHelper.toLong(stateData.get(NCConstant.COLUMN_VIEWTIME), -1);
            long firstrecordTime = DataConvertHelper.toLong(stateData.get(NCConstant.COLUMN_FIRSTRECORDTIME), curTime);
                
            if (viewTime > -1) {
                
                if (log.isDebugEnable()) {
                    log.debug(this, "NC Convergence Event Check Result, Over View TTL: viewTTL=" + viewTTL + ",curTime - viewTime="
                            + (curTime - viewTime));
                }
                
                /**
                 * if there is ViewTime and ViewTime exceeds ViewTTL, take this event as a newone
                 */
                if (curTime - viewTime > viewTTL) {
                    stateData = playAsFirstEventRecordConvergence(event, eventKey);
                }
                else {
                    int count = increEventCount(stateData);
                    stateData.put(NCConstant.EVENT_COUNT, count);
                    playAsReEntryRecordConvergence(event, eventKey, stateData, false);
                    if (log.isTraceEnable()) {
                        log.info(this, "NCJudgementHandler END: key=" + eventKey);
                    }
                    return;
                }
                
            } else if (curTime - firstrecordTime > viewTTL){
                stateData = playAsFirstEventRecordConvergence(event, eventKey);
            } else {
                int count = increEventCount(stateData);
                stateData.put(NCConstant.EVENT_COUNT, count);
            }
            
            doNotificationConvergence(event, eventKey, stateData);
            
        }
        
        /**
         * the logic of gradient convergence
         * @param event
         * @param eventKey
         * @param stateData
         */
        private void doNotificationConvergence(NotificationEvent event, String eventKey, Map<String, Object> stateData) {
            
            String convergence = event.getArg("convergences");
            List<String> convs = Arrays.asList(convergence.split(","));
            String count = stateData.get(NCConstant.EVENT_COUNT) +"";
            if (convs.contains(count)) {
                
                if (log.isDebugEnable()) {
                    log.debug(this, "NC Convergence Event Check Result, RE-Action Notify: current event=" + count);
                }
                playAsReEntryRecordConvergence(event, eventKey, stateData, true);
                addNotficationTask(event, stateData);
                
            } else {
                
                playAsReEntryRecordConvergence(event, eventKey, stateData, false);
            }
            
            if (log.isTraceEnable()) {
                log.info(this, "NCJudgementHandler END: key=" + eventKey);
            }
        }
        
        @Override
        public String getSuccessNextActionId() {
            
            return null;
        }
        
        @Override
        public String getFailureNextActionId() {
            
            return null;
        }
        
        @Override
        public String getExceptionNextActionId() {
            
            return null;
        }
        
        private Map<String, Object> playAsFirstEventRecordConvergence(NotificationEvent event, String eventKey) {
            
            Map<String, Object> statusSet = new LinkedHashMap<String, Object>();
            
            statusSet.put(NCConstant.COLUMN_STARTTIME, event.getTime());
            statusSet.put(NCConstant.COLUMN_STATE, NCConstant.StateFlag.NEWCOME.getStatFlag());
            statusSet.put(NCConstant.COLUMN_RETRY_COUNT, 0);
            statusSet.put(NCConstant.COLUMN_LATESTIME, System.currentTimeMillis());
            statusSet.put(NCConstant.COLUMN_FIRSTRECORDTIME, System.currentTimeMillis());
            
            // First Time NTFE take priority as the "0" Priority.
            statusSet.put(NCConstant.COLUMN_PRIORITY, 0);
            statusSet.put(NCConstant.EVENT_COUNT, 1);
            
            String statusStr = JSONHelper.toString(statusSet);
            
            event.addArg(NCConstant.NCFirstEvent, "true");
            event.addArg(NCConstant.NTFKEY, eventKey);
            event.addArg(NCConstant.NTFVALUE, statusStr);
            
            return statusSet;
        }
        
        private void playAsReEntryRecordConvergence(NotificationEvent event, String eventKey, Map<String, Object> stateData,
                boolean isRetry) {
            
            // NOTE: if the state is VIEW, the UPDATE should not be set as the latest state
            int curState = (int) stateData.get(NCConstant.COLUMN_STATE);
            
            if (curState < StateFlag.UPDATE.getStatFlag() && !"true".equals(event.getArg(NCConstant.NCFirstEvent))) {
                curState = StateFlag.UPDATE.getStatFlag();
                stateData.put(NCConstant.COLUMN_STATE, curState);
            }
            
            // NOTE: if the state is VIEW, for this part that means VIEW & UPDATE
            if (curState == StateFlag.VIEW.getStatFlag()) {
                curState = StateFlag.VIEWUPDATE.getStatFlag();
                stateData.put(NCConstant.COLUMN_STATE, curState);
            }
            
            if (isRetry == true) {
                int curRetry = (Integer) stateData.get(NCConstant.COLUMN_RETRY_COUNT) + 1;
                stateData.put(NCConstant.COLUMN_RETRY_COUNT, curRetry);
                stateData.put(NCConstant.COLUMN_LATESTIME, System.currentTimeMillis());
            }
            
            stateData.put(NCConstant.COLUMN_LATESTRECORDTIME, event.getTime());
            
            String statusStr = JSONHelper.toString(stateData);
            
            if (null == event.getArg(NCConstant.NCFirstEvent)) {
                event.addArg(NCConstant.NCFirstEvent, "false");
            }
            event.addArg(NCConstant.NTFKEY, eventKey);
            event.addArg(NCConstant.NTFVALUE, statusStr);
            
            // 持久化操作
            addPersistentTask(event);
        }
        
    }
    
    
    private int retryTime = 3;
    /**
     * NOTE: viewTTL is the time to give human to fix the issue, see we are nice as default is 4 hours
     */
    private int viewTTL = 4 * 3600 * 1000;
    private long frozenTime = 300000;
    private I1NQueueWorker qworker;
    private IActionEngine engine;
    private NCEventStatusManager eventStatusManager;

    public NCJudgementHandler(String cName, String feature) {

        super(cName, feature);

        eventStatusManager = (NCEventStatusManager) ConfigurationManager.getInstance().getComponent(feature, "EventStatusManager");
        
        retryTime = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "nc.notify.retry"), 3);
        viewTTL = DataConvertHelper
                .toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "nc.notify.ttl"), 4) * 3600 * 1000;
        frozenTime = DataConvertHelper.toLong(
                this.getConfigManager().getFeatureConfiguration(this.feature, "nc.notify.frozenTime"), 300) * 1000;

        qworker = (I1NQueueWorker) ConfigurationManager.getInstance().getComponent(this.feature,
                NCConstant.NC1NQueueWorkerName);
        engine = this.getActionEngineMgr().getActionEngine("NCJudgementEngine");
        new NotificationConvergenceAction(NotificationConvergenceAction.class.getSimpleName(), feature, engine);
    }

    /**
     * 预警通知决策流程
     */
    @SuppressWarnings({ "unchecked" })
    @Override
    public void handle(NotificationEvent event) {

        /**
         * Step 1 generate eventKey
         **/
        String eventKey = getKeyfromNTFE(event);
        String stateDataStr = null;

        if (log.isTraceEnable()) {
            log.info(this, "NCJudgementHandler START: key=" + eventKey + ",event=" + event.toJSONString());
        }

        /***
         * Step 1.1 check if there is SYNC atomic key on enevtKey
         * 
         */

        boolean isSYNCAtomicKey = eventStatusManager.checkSyncAtomicKey(eventKey);
        if (isSYNCAtomicKey) {
            if (log.isTraceEnable()) {
                log.info(this, "NCJudgementHandler event is locked:" + event.toJSONString());
            }
            return;
        }

        /**
         * Step 2 Check if there is a cache for current event
         **/
        try {
            Map<String, String> ntfvalue = eventStatusManager.obtainEventCache(eventKey);
            stateDataStr = ntfvalue.get(eventKey);
        }
        catch (RuntimeException e) {

            if (log.isTraceEnable()) {
                log.info(this, "NCJudgementHandler exception got RuntimeException" + e.getMessage() + " The event is: "
                        + event.toJSONString());
            }

            return;
        }
        
        /**
         * notification convergence
         */
        if(!StringHelper.isEmpty(event.getArg("convergences"))) {
            
            ActionContext ac = new ActionContext();
            ac.putParam("eventKey", eventKey);
            ac.putParam("event", event);
            ac.putParam("stateDataStr", stateDataStr);
            engine.execute(NotificationConvergenceAction.class.getSimpleName(), ac);            
            if (log.isTraceEnable()) {
                log.info(this, "NCJudgementHandler END: key=" + eventKey);
            }
            return;
        }

        /**
         * Step 2.1 if none, take this event as the first event record
         */
        if (null == stateDataStr) {

            if (log.isDebugEnable()) {
                log.debug(this, "NC Event happens FIRST TIME: NTFKEY=" + eventKey + ",time=" + event.getTime());
            }

            playAsFirstEventRecord(event, eventKey);

            if (log.isTraceEnable()) {
                log.info(this, "NCJudgementHandler END: key=" + eventKey);
            }

            return;
        }

        /**
         * Step 2.2 if there is a cache for current event, we need get the cache state data
         */
        Map<String, Object> stateData = JSONHelper.toObject(stateDataStr, Map.class);

        if (log.isDebugEnable()) {
            log.debug(this, "NC Event happens OVERTIMEs: NTFKEY=" + eventKey + ",time=" + event.getTime());
        }

        /**
         * Step 3 if there is ViewTime and ViewTime exceeds ViewTTL, that means human has seen this event, and ViewTTL
         * is the timespan we give to them to fix the issue, if reach ViewTTL, that may mean the issue is not fixed or
         * may be a new issue, anyway, we will take current event as NEWCOME. then a new first event record is
         * generated, it is the split line to the previous first event record
         */
        long curTime = System.currentTimeMillis();

        long viewTime = DataConvertHelper.toLong(stateData.get(NCConstant.COLUMN_VIEWTIME), -1);

        if (viewTime > -1) {

            if (log.isDebugEnable()) {
                log.debug(this, "NC Event Check Result, Over View TTL: viewTTL=" + viewTTL + ",curTime - viewTime="
                        + (curTime - viewTime));
            }

            /**
             * if over viewTTL, take this as a new event record
             */
            if (curTime - viewTime > viewTTL) {
                playAsFirstEventRecord(event, eventKey);
            }
            /**
             * if less than viewTTL, take this as a re-entry event record then we will not send the notification
             */
            else {
                playAsReEntryRecord(event, eventKey, stateData, false);
            }

            if (log.isTraceEnable()) {
                log.info(this, "NCJudgementHandler END: key=" + eventKey);
            }
            return;
        }

        /**
         * Step 4 check if reach notification frozenTime, if not, just update the COLUMN_LATESTRECORDTIME
         */
        long timeSpan = curTime - DataConvertHelper.toLong(stateData.get(NCConstant.COLUMN_LATESTIME), 0);

        if (timeSpan < frozenTime) {
            if (log.isDebugEnable()) {
                log.debug(this, "NC Event Check Result, NO Rearch Frozen Time: timespan=" + timeSpan + ",frozentime="
                        + frozenTime);
            }

            playAsReEntryRecord(event, eventKey, stateData, false);

            if (log.isTraceEnable()) {
                log.info(this, "NCJudgementHandler END: key=" + eventKey);
            }
            return;
        }

        /**
         * Step 5 if reach frozeTime, then check if reach MAX retry times
         */
        int curRetry = (int) stateData.get(NCConstant.COLUMN_RETRY_COUNT);
        /**
         * Step 5.1 if less than retryTime, we need send notification and update the retry & latest_time
         */
        if (curRetry < retryTime) {

            if (log.isDebugEnable()) {
                log.debug(this, "NC Event Check Result, RE-Action Notify: retry done=" + (++curRetry));
            }

            playAsReEntryRecord(event, eventKey, stateData, true);

            // doing notification actions
            addNotficationTask(event, stateData);
        }
        /**
         * Step 5.2 if more than retryTime, just update status, not send notification
         */
        else {

            /**
             * Step 5.3 check priority from cache
             * 
             */
            log.debug(this, "Reach MAX Retry with NO RE-Action Notify: retry:" + curRetry);

            int priority = (int) stateData.get(NCConstant.COLUMN_PRIORITY);

            if (priority < getMaxPrioritySizeByEvent(event) - 1) {

                log.debug(this, "current priority is: " + priority);

                /**
                 * 
                 * Add priorityFlag to make playAsReEntryRecord could distinct this step,
                 * 
                 * if priorityFlag is true, it need ++ priority
                 * 
                 */
                stateData.put(NCConstant.PRIORITYFLAG, true);

                playAsReEntryRecord(event, eventKey, stateData, false);

                // doing notification actions
                addNotficationTask(event, stateData);

            }
            else {

                log.debug(this, "Reach MAX Priority size , priority: " + priority);
                playAsReEntryRecord(event, eventKey, stateData, false);
            }
        }

        if (log.isTraceEnable()) {
            log.info(this, "NCJudgementHandler END: key=" + eventKey);
        }
    }

    /**
     * playAsReEntryRecord
     * 
     * @param event
     * @param eventKey
     * @param stateData
     * @param isRetry
     */
    private void playAsReEntryRecord(NotificationEvent event, String eventKey, Map<String, Object> stateData,
            boolean isRetry) {

        // NOTE: if the state is VIEW, the UPDATE should not be set as the latest state
        int curstate = (int) stateData.get(NCConstant.COLUMN_STATE);

        if (curstate < StateFlag.UPDATE.getStatFlag()) {
            curstate = StateFlag.UPDATE.getStatFlag();
            stateData.put(NCConstant.COLUMN_STATE, curstate);
        }

        // NOTE: if the state is VIEW, for this part that means VIEW & UPDATE
        if (curstate == StateFlag.VIEW.getStatFlag()) {
            curstate = StateFlag.VIEWUPDATE.getStatFlag();
            stateData.put(NCConstant.COLUMN_STATE, curstate);
        }

        if (isRetry == true) {
            int curRetry = (Integer) stateData.get(NCConstant.COLUMN_RETRY_COUNT) + 1;
            stateData.put(NCConstant.COLUMN_RETRY_COUNT, curRetry);
            stateData.put(NCConstant.COLUMN_LATESTIME, System.currentTimeMillis());
        }

        stateData.put(NCConstant.COLUMN_LATESTRECORDTIME, event.getTime());
        
        int count = increEventCount(stateData);
        stateData.put(NCConstant.EVENT_COUNT, count);

        /**
         * check and increase priority if needed
         */
        ackIncPriority(stateData);

        String statusStr = JSONHelper.toString(stateData);

        event.addArg(NCConstant.NCFirstEvent, "false");
        event.addArg(NCConstant.NTFKEY, eventKey);
        event.addArg(NCConstant.NTFVALUE, statusStr);

        addPersistentTask(event);
    }

    /**
     * playAsFirstEventRecord
     * 
     * @param event
     * @param eventKey
     */
    private void playAsFirstEventRecord(NotificationEvent event, String eventKey) {

        Map<String, Object> statusSet = new LinkedHashMap<String, Object>();

        statusSet.put(NCConstant.COLUMN_STARTTIME, event.getTime());
        statusSet.put(NCConstant.COLUMN_STATE, NCConstant.StateFlag.NEWCOME.getStatFlag());
        statusSet.put(NCConstant.COLUMN_RETRY_COUNT, 1);
        statusSet.put(NCConstant.COLUMN_LATESTIME, System.currentTimeMillis());
        statusSet.put(NCConstant.COLUMN_FIRSTRECORDTIME, System.currentTimeMillis());
        // NOTE: the latest record event time, then we can find out all records from first record startTime to this time
        statusSet.put(NCConstant.COLUMN_LATESTRECORDTIME, event.getTime());

        // First Time NTFE take priority as the "0" Priority.
        statusSet.put(NCConstant.COLUMN_PRIORITY, 0);
        statusSet.put(NCConstant.EVENT_COUNT, 1);

        String statusStr = JSONHelper.toString(statusSet);

        event.addArg(NCConstant.NCFirstEvent, "true");
        event.addArg(NCConstant.NTFKEY, eventKey);
        event.addArg(NCConstant.NTFVALUE, statusStr);

        addPersistentTask(event);

        addNotficationTask(event, statusSet);
    }

    /**
     * 预警特征提取
     **/
    public String getKeyfromNTFE(NotificationEvent data) {

        StringBuffer keybuffer = new StringBuffer();
        keybuffer.append(data.getIP() + NCConstant.seperator);
        keybuffer.append(data.getId() + NCConstant.seperator);
        keybuffer.append(data.getArg("component") + NCConstant.seperator);
        keybuffer.append(data.getArg("feature") + NCConstant.seperator);
        keybuffer.append(DataStoreHelper.decodeForMongoDB(data.getTitle()).hashCode());

        String key = keybuffer.toString();
        return key;
    }

    /**
     * 添加预警事件预警到短信邮件通知任务
     */
    public void addNotficationTask(NotificationEvent event, Map<String, Object> statusSet) {

        NotificationTask taskN = new NotificationTask(cName, feature);

        taskN.put(NCConstant.NCEventParam, event);

        // add priority for taskN
        taskN.put(NCConstant.PRIORITYLEVEL, this.getCurrentEventPriority(statusSet));

        qworker.put(taskN);
    }

    /**
     * 添加预警事件到持久化任务
     */
    public void addPersistentTask(NotificationEvent event) {

        PersistentTask taskP = new PersistentTask(cName, feature);

        taskP.put(NCConstant.NCEventParam, event);

        qworker.put(taskP);
    }

    @SuppressWarnings("rawtypes")
    public int getMaxPrioritySizeByEvent(NotificationEvent event) {

        int maxPriority = 0;

        Map<String, String> args = event.getArgs(true);

        for (String key : args.keySet()) {

            if (key.indexOf("action_") != 0) {
                continue;
            }

            String actionstr = args.get(key);

            List actionlist = JSONHelper.toObject(actionstr, LinkedList.class);

            int size = actionlist.size();

            if (size > maxPriority) {

                maxPriority = size;
            }
        }

        return maxPriority;
    }

    /***
     * 
     * Increase Priority if need
     * 
     */
    public void ackIncPriority(Map<String, Object> stateData) {

        Object pflag = stateData.get(NCConstant.PRIORITYFLAG);

        if (pflag != null && ((boolean) pflag == true)) {

            int prioriy = (int) stateData.get(NCConstant.COLUMN_PRIORITY) + 1;

            stateData.put(NCConstant.COLUMN_PRIORITY, prioriy);

            // retry need recover to zero
            stateData.put(NCConstant.COLUMN_RETRY_COUNT, 1);
            stateData.put(NCConstant.COLUMN_LATESTIME, System.currentTimeMillis());
            stateData.put(NCConstant.PRIORITYFLAG, false);
        }
    }

    public int getCurrentEventPriority(Map<String, Object> stateData) {

        if (stateData != null) {
            Integer p = (Integer) stateData.get(NCConstant.COLUMN_PRIORITY);
            return (p == null) ? 0 : p;
        }
        else {
            return 0;
        }

    }
    
    private int increEventCount(Map<String, Object> stateData) {
        Integer count = (Integer) stateData.get(NCConstant.EVENT_COUNT);
        if (null == count) {
            count = 0;
        }
        count = count + 1;
        return count;
    }

}