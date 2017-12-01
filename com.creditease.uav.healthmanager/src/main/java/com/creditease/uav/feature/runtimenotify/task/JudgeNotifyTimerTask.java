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

package com.creditease.uav.feature.runtimenotify.task;

import java.util.Map;
import java.util.Map.Entry;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.NetworkHelper;
import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.Abstract1NTask;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.cache.api.CacheManager.CacheLock;
import com.creditease.uav.feature.RuntimeNotifyCatcher;
import com.creditease.uav.feature.runtimenotify.NotifyStrategy;
import com.creditease.uav.feature.runtimenotify.Slice;
import com.creditease.uav.feature.runtimenotify.StrategyJudgement;
import com.creditease.uav.feature.runtimenotify.scheduler.RuntimeNotifyStrategyMgr;

public class JudgeNotifyTimerTask extends Abstract1NTask {

    private NotifyStrategy stra;
    private long taskStart = System.currentTimeMillis();
    private long judge_time;
    private static final String LOCK_REGION = "lock.region.uav";
    private static final long LOCK_TIMEOUT = 60 * 1000;
    private CacheManager cm;

    public JudgeNotifyTimerTask(String name, String feature, long judge_time, NotifyStrategy stra) {
        super(name, feature);
        this.stra = stra;
        this.judge_time = judge_time - judge_time % 60000;
        cm = (CacheManager) this.getConfigManager().getComponent(feature, RuntimeNotifyCatcher.CACHE_MANAGER_NAME);
    }

    @Override
    public void run() {

        CacheLock lock = null;
        try {
            lock = cm.newCacheLock(LOCK_REGION, stra.getName(), LOCK_TIMEOUT);

            if (!lock.getLock()) {
                return;
            }
            /**
             * Step 1:find out instance
             */
            for (String instance : stra.getInstances()) {
                /**
                 * Step 2: judge the strategy
                 */

                StrategyJudgement judgement = (StrategyJudgement) getConfigManager().getComponent(feature,
                        "StrategyJudgement");
                Map<String, String> result = judgement.judge(new Slice(instance, judge_time), stra, null);

                /**
                 * Step 3: if fire the event, build notification event
                 */
                if (result != null && !result.isEmpty()) {
                    NotificationEvent event = this.newNotificationEvent(instance, result);

                    // get context
                    putContext(event);

                    // get action
                    putNotifyAction(event, stra);

                    // get msg tempalte
                    putNotifyMsg(event, stra);

                    if (this.log.isTraceEnable()) {
                        this.log.info(this, "RuntimeNotify Notification Event Happen: event=" + event.toJSONString());
                    }

                    this.putNotificationEvent(event);
                }
            }

        }
        catch (Exception e) {
            log.err(this, "JudgeNotifyTimerTask" + stra.getName() + " RUN FAIL.", e);
        }
        finally {
            if (lock != null && lock.isLockInHand()) {
                lock.releaseLock();
            }
        }

        if (log.isDebugEnable()) {
            long cost = System.currentTimeMillis() - taskStart;
            String detail = cost < 10 ? "" : " detail:strategy=" + JSONHelper.toString(stra);
            log.debug(this, "whole task lifecycle COST: (" + cost + ")ms" + detail);
        }
    }

    /**
     * get context
     * 
     * TODO: we need support context param in strategy
     */
    private void putContext(NotificationEvent event) {

    }

    /**
     * newNotificationEvent
     * 
     * 
     * @return
     */
    private NotificationEvent newNotificationEvent(String instance, Map<String, String> result) {

        String ip = instance;
        String host = instance;
        String appgroup = "UNKNOWN";

        instance = formatInstance(instance);

        Map<String, Object> infos = getInfoFromSliceCache(instance);
        if (infos != null) {
            ip = String.valueOf(infos.get("ip"));
            host = String.valueOf(infos.get("host"));
            appgroup = String.valueOf(infos.get("appgroup"));
        }

        StringBuilder desc = new StringBuilder();
        StringBuilder conditionIndex = new StringBuilder();

        for (Map.Entry<String, String> cause : result.entrySet()) {
            // description
            desc.append(instance + "触发条件[" + cause.getKey() + "]：").append(cause.getValue()).append("\r\n");
            // condition index
            conditionIndex.append(" " + cause.getKey());
        }

        String title = ip + "[" + instance + "]触发" + result.size() + "个报警(条件序号：" + conditionIndex.toString() + ")";

        // fix &nbsp(\u00A0) can be shown in email
        String description = desc.toString().replace('\u00A0', ' ');

        NotificationEvent ne = new NotificationEvent(NotificationEvent.EVENT_RT_ALERT_THRESHOLD, title, description,
                judge_time, ip, host);

        // add appgroup
        ne.addArg("appgroup", appgroup);

        return ne;
    }

    private String formatInstance(String instance) {

        if (NetworkHelper.isIPV4(instance)) {
            instance += "_";
        }
        instance = stra.getName().substring(0, stra.getName().lastIndexOf('@') + 1) + instance;

        return instance;
    }

    private Map<String, Object> getInfoFromSliceCache(String instance) {

        String cacheKey = "SLICE_" + instance + "_";
        for (int index = 0; index < 60; index++) {
            String result = cm.lpop(RuntimeNotifyStrategyMgr.UAV_CACHE_REGION, cacheKey + index);
            if (result != null) {
                Slice s = new Slice(result);
                return s.getArgs();
            }
        }

        return null;
    }

    private void putNotifyAction(NotificationEvent event, NotifyStrategy stra) {

        Map<String, String> actions = stra.getAction();
        if (actions == null || actions.isEmpty()) {
            return;
        }

        for (Entry<String, String> act : actions.entrySet()) {
            event.addArg("action_" + act.getKey(), act.getValue());
        }
    }

    private void putNotifyMsg(NotificationEvent event, NotifyStrategy stra) {

        String msgTemplate = stra.getMsgTemplate();
        String msg = makeMsgByTemplate(msgTemplate, stra);

        if (msg != null) {
            event.addArg("msg", msg);
        }
    }

    /**
     * 
     * @param template
     * @param slice
     * @return
     */
    private String makeMsgByTemplate(String template, NotifyStrategy stra) {

        return "";
    }

}
