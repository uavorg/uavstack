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

import static com.creditease.uav.feature.RuntimeNotifyCatcher.STORAGE_CACHE_MANAGER_NAME;
import static com.creditease.uav.feature.RuntimeNotifyCatcher.UAV_CACHE_REGION;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.creditease.agent.helpers.DateTimeHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.Abstract1NTask;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.feature.runtimenotify.NotifyStrategy;
import com.creditease.uav.feature.runtimenotify.RuntimeNotifySliceMgr;
import com.creditease.uav.feature.runtimenotify.Slice;
import com.creditease.uav.feature.runtimenotify.StrategyJudgement;
import com.creditease.uav.feature.runtimenotify.scheduler.RuntimeNotifyStrategyMgr;

public class JudgeNotifyTask extends Abstract1NTask {

    private static final String RNJUDGE_PREFIX = "RNJUDGE_";

    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{[\\w-]+\\}");

    private Slice curSlice;

    private long taskStart = System.currentTimeMillis();

    public JudgeNotifyTask(String name, String feature, Slice slice) {
        super(name, feature);
        this.curSlice = slice;
    }

    @Override
    public void run() {

        NotifyStrategy stra = null;
        try {

            /**
             * Step 1: seek strategy
             */
            RuntimeNotifyStrategyMgr strategyMgr = (RuntimeNotifyStrategyMgr) getConfigManager()
                    .getComponent(this.feature, "RuntimeNotifyStrategyMgr");

            stra = strategyMgr.seekStrategy(curSlice.getKey());
            if (stra == null) {
                return;
            }

            /**
             * Step 1.5: underclocking
             */
            long range = stra.getMaxRange();
            if (range > 0) {
                CacheManager cm = (CacheManager) getConfigManager().getComponent(this.feature,
                        STORAGE_CACHE_MANAGER_NAME);

                String judgedKey = genJudgedKey(curSlice);
                if (cm.exists(UAV_CACHE_REGION, judgedKey)) {
                    return;
                }
                else {
                    cm.put(UAV_CACHE_REGION, judgedKey, String.valueOf(curSlice.getTime()));
                    cm.expire(UAV_CACHE_REGION, judgedKey, range, TimeUnit.MILLISECONDS);
                }
            }

            /**
             * Step 2: dump range slices
             */
            List<Slice> rangeSlices = null;
            if (range > 0) {
                RuntimeNotifySliceMgr sliceMgr = (RuntimeNotifySliceMgr) getConfigManager().getComponent(this.feature,
                        "RuntimeNotifySliceMgr");
                rangeSlices = sliceMgr.getSlices(curSlice, range);
                if (rangeSlices.isEmpty()) {
                    if (log.isDebugEnable()) {
                        log.debug(this, "RuntimeNotify judge dump invalid.");
                    }
                    return;
                }
            }
            else {
                rangeSlices = new ArrayList<>(1);
                rangeSlices.add(curSlice);
            }

            /**
             * Step 3: judge the strategy
             */
            StrategyJudgement judgement = (StrategyJudgement) getConfigManager().getComponent(feature,
                    "StrategyJudgement");
            Map<String, String> result = judgement.judge(curSlice, stra, rangeSlices);

            /**
             * Step 4: release range slices
             */
            // ?? maybe no effective
            if (rangeSlices != null) {
                rangeSlices.clear();
                rangeSlices = null;
            }

            /**
             * Step 5: if fire the event, build notification event
             */
            if (result != null && !result.isEmpty()) {
                NotificationEvent event = this.newNotificationEvent(result);

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
        catch (Exception e) {
            log.err(this, "JudgeNotifyTask RUN FAIL.", e);
        }

        if (log.isDebugEnable()) {
            long cost = System.currentTimeMillis() - taskStart;
            String detail = cost < 10 ? ""
                    : " detail: key=" + curSlice.getKey() + ", strategy=" + JSONHelper.toString(stra);
            log.debug(this, "whole task lifecycle COST: (" + cost + ")ms" + detail);
        }
    }

    /**
     * get context
     * 
     * TODO: we need support context param in strategy
     */
    private void putContext(NotificationEvent event) {

        Map<String, Object> args = this.curSlice.getArgs();

        for (String key : args.keySet()) {

            Object argVal = args.get(key);

            String jsonstr = JSONHelper.toString(argVal);

            event.addArg(key, jsonstr);
        }
    }

    /**
     * newNotificationEvent
     * 
     * 
     * @return
     */
    private NotificationEvent newNotificationEvent(Map<String, String> result) {

        String ip = this.curSlice.getMdf().getIP();
        String host = this.curSlice.getMdf().getHost();

        String appgroup = this.curSlice.getMdf().getExt("appgroup");
        appgroup = (appgroup == null) ? "" : appgroup;

        StringBuilder description = new StringBuilder();
        StringBuffer conditionIndex = new StringBuffer();

        for (Map.Entry<String, String> cause : result.entrySet()) {
            // description
            description.append("触发条件[" + cause.getKey() + "]：").append(cause.getValue()).append("\r\n");
            // condition index
            conditionIndex.append(" " + cause.getKey());
        }

        String title = ip + "[" + this.curSlice.getKey() + "]触发" + result.size() + "个报警(条件序号："
                + conditionIndex.toString() + ")";

        NotificationEvent ne = new NotificationEvent(NotificationEvent.EVENT_RT_ALERT_THRESHOLD, title,
                description.toString(), curSlice.getTime(), ip, host);

        // add appgroup
        ne.addArg("appgroup", appgroup);

        return ne;
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
        String msg = makeMsgByTemplate(msgTemplate, curSlice);

        if (msg != null) {
            event.addArg("msg", msg);
        }
    }

    @SuppressWarnings("deprecation")
    private String genJudgedKey(Slice slice) {

        Date d = new Date(slice.getTime());
        return RNJUDGE_PREFIX + slice.getKey() + "_" + d.getMinutes();
    }

    /**
     * 
     * @param template
     * @param slice
     * @return
     */
    private String makeMsgByTemplate(String template, Slice slice) {

        if (StringHelper.isEmpty(template)) {
            return "";
        }

        Matcher m = TEMPLATE_PATTERN.matcher(template);
        while (m.find()) {
            String var = m.group();
            String val = pickFiniteElement(var, slice);
            template = template.replace(var, val);
        }

        return template;
    }

    /**
     * 
     * @param key
     *            '{variable}'
     * @param slice
     *            current Slice
     * @return
     */
    private String pickFiniteElement(String key, Slice slice) {

        String v = null;
        switch (key) {
            case "{ip}":
                v = slice.getMdf().getIP();
                break;
            case "{host}":
                v = slice.getMdf().getHost();
                break;
            case "{time}":
                v = DateTimeHelper.toStandardDateFormat(slice.getMdf().getTimeFlag());
                break;
            case "{svrid}":
                v = slice.getMdf().getServerId();
                break;
            case "{appgroup}":
                v = slice.getMdf().getExt("appgroup");
                break;
            case "{key}":
                v = slice.getKey();
                break;
            default:
                String var = key.substring(1, key.length() - 1);
                Object o = slice.getArgs().get(var);
                v = o == null ? "" : o.toString();
                break;
        }

        if (v == null) {
            v = "";
        }

        return v;
    }
}
