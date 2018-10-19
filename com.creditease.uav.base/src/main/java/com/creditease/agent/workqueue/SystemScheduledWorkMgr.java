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

package com.creditease.agent.workqueue;

import java.util.Collections;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.creditease.agent.spi.AbstractComponent;
import com.creditease.agent.spi.AbstractTimerWork;
import com.creditease.agent.spi.ITimerWorkManager;

public class SystemScheduledWorkMgr extends AbstractComponent implements ITimerWorkManager {

    private final static int DEFAULT_POOL_SIZE = 2;
    private ScheduledThreadPoolExecutor scheduledPool;

    private Map<String, AbstractTimerWork> tasksMap = new ConcurrentHashMap<>();

    /**
     * Create a scheduled work manager with preset thread pool core size. The initial pool size here is 5.
     * 
     * @param cName
     * @param feature
     */
    public SystemScheduledWorkMgr(String cName, String feature) {

        this(cName, feature, DEFAULT_POOL_SIZE);
    }

    public SystemScheduledWorkMgr(String cName, String feature, int coreSize) {

        super(cName, feature);
        this.scheduledPool = new ScheduledThreadPoolExecutor(coreSize, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * @return boolean, when submit a task already. submitted before, return false. else return true.
     * 
     * @throws IllegalArgumentException
     *             when input parameters are null or period less than 0
     */
    @Override
    public boolean scheduleWork(String workName, AbstractTimerWork r, long delay, long period) {

        if (workName == null || "".equals(workName) || r == null || period < 0) {
            throw new IllegalArgumentException("Wrong args for submitting a timer task.");
        }

        // if task already submitted
        if (tasksMap.containsKey(workName)) {
            return false;
        }

        TimerTask timerTask = createTimerTask(workName, r);

        this.scheduledPool.scheduleAtFixedRate(timerTask, delay, period, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public boolean scheduleWorkInPeriod(String workName, AbstractTimerWork r, long delay, long period) {

        if (workName == null || "".equals(workName) || r == null || period < 0) {
            throw new IllegalArgumentException("Wrong args for submitting a timer task.");
        }

        if (tasksMap.containsKey(workName)) {
            return false;
        }

        TimerTask timerTask = createTimerTask(workName, r);

        this.scheduledPool.scheduleWithFixedDelay(timerTask, delay, period, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public boolean cancel(String workName) {

        if (workName == null || "".equals(workName)) {
            return false;
        }

        if (this.tasksMap.containsKey(workName)) {
            AbstractTimerWork timerWork = tasksMap.get(workName);
            timerWork.cancel();
            this.tasksMap.remove(workName);
            return true;
        }
        return false;
    }

    @Override
    public void shutdown() {

        if (this.tasksMap.size() > 0) {
            for (AbstractTimerWork timerWork : tasksMap.values()) {
                timerWork.cancel();
            }
            this.tasksMap.clear();
        }
        scheduledPool.shutdown();
    }

    @Override
    public Map<String, AbstractTimerWork> getAllTimerWork() {

        return Collections.unmodifiableMap(tasksMap);
    }

    private TimerTask createTimerTask(String workName, AbstractTimerWork r) {

        final AbstractTimerWork task = r;
        TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {

                try {
                    task.run();
                }
                catch (Throwable e) {
                    log.err(this, "Timer Worker[" + task.getName() + "] runs FAIL.", e);
                }
            }

        };

        r.setCurrentTimerTask(timerTask);

        this.tasksMap.put(workName, r);

        return timerTask;
    }

}
