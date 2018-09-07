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

package com.creditease.uav.feature.runtimenotify.scheduler;

import java.util.HashSet;

import com.creditease.agent.spi.AbstractTimerWork;
import com.creditease.agent.spi.I1NQueueWorker;
import com.creditease.uav.feature.RuntimeNotifyCatcher;
import com.creditease.uav.feature.runtimenotify.NotifyStrategy;
import com.creditease.uav.feature.runtimenotify.task.JudgeNotifyTask;
import com.creditease.uav.feature.runtimenotify.task.JudgeNotifyTaskForTimer;

/**
 * 
 * RuntimeNotifyStrategyMgr description:
 * 
 * 1. provide Strategy function
 * 
 * 2. get the strategy setting interval
 *
 */
public class TimerNotifyWorker extends AbstractTimerWork {

    public TimerNotifyWorker(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public void run() {

        RuntimeNotifyStrategyMgr strategyMgr = (RuntimeNotifyStrategyMgr) getConfigManager().getComponent(this.feature,
                "RuntimeNotifyStrategyMgr");
        HashSet<NotifyStrategy> strategies = new HashSet<NotifyStrategy>(strategyMgr.getStrategies());
        for (NotifyStrategy stra : strategies) {
            if (stra.getType() != NotifyStrategy.Type.TIMER) {
                continue;
            }
            I1NQueueWorker n1nqw = get1NQueueWorkerMgr().getQueueWorker(this.feature,
                    RuntimeNotifyCatcher.QWORKER_NAME);
            n1nqw.put(new JudgeNotifyTaskForTimer(JudgeNotifyTask.class.getSimpleName(), feature,
                    System.currentTimeMillis(), stra));

        }
    }

}
