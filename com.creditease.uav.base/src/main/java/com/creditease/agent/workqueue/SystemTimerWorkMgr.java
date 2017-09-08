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
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.agent.spi.AbstractTimerWork;
import com.creditease.agent.spi.ITimerWorkManager;

public class SystemTimerWorkMgr implements ITimerWorkManager {

    private Map<String, AbstractTimerWork> timerMap = new ConcurrentHashMap<String, AbstractTimerWork>();
    private static ISystemLogger log = SystemLogger.getLogger(SystemTimerWorkMgr.class);

    @Override
    public boolean scheduleWork(String workName, AbstractTimerWork r, Date firstDate, long period) {

        if (checkNull(workName, r, firstDate, period)) {
            return false;
        }

        Timer t = new Timer(workName, r.isDaemon());
        r.setTimer(t);
        r.setPeriod(period);
        TimerTask tt = createTimerTask(workName, r);

        try {
            t.scheduleAtFixedRate(tt, firstDate, period);
            return true;
        }
        catch (Exception e) {
            log.err(this, "Timer Worker[" + r.getName() + "] starts FAIL.", e);
        }

        return false;
    }

    /**
     * @param workName
     * @param r
     * @param firstDate
     * @param period
     * @return
     */
    private boolean checkNull(String workName, AbstractTimerWork r, Date firstDate, long period) {

        return workName == null || "".equals(workName) || r == null || firstDate == null || period < 0;
    }

    @Override
    public boolean scheduleWork(String workName, AbstractTimerWork r, long delay, long period) {

        if (workName == null || "".equals(workName) || r == null || period < 0) {
            return false;
        }

        Timer t = new Timer(workName, r.isDaemon());
        r.setTimer(t);
        r.setPeriod(period);
        TimerTask tt = createTimerTask(workName, r);

        try {
            t.scheduleAtFixedRate(tt, delay, period);
            return true;
        }
        catch (Exception e) {
            log.err(this, "Timer Worker[" + r.getName() + "] starts FAIL.", e);
        }

        return false;
    }

    private TimerTask createTimerTask(String workName, AbstractTimerWork r) {

        final AbstractTimerWork task = r;
        TimerTask tt = new TimerTask() {

            @Override
            public void run() {

                try {
                    task.run();
                }
                catch (Exception e) {
                    log.err(this, "Timer Worker[" + task.getName() + "] runs FAIL.", e);
                }
            }

        };

        r.setCurrentTimerTask(tt);

        this.timerMap.put(workName, r);

        return tt;
    }

    @Override
    public boolean cancel(String workName) {

        if (workName == null || "".equals(workName)) {
            return false;
        }

        if (this.timerMap.containsKey(workName)) {
            AbstractTimerWork t = this.timerMap.get(workName);
            t.cancel();
            this.timerMap.remove(workName);
            return true;
        }

        return false;
    }

    @Override
    public void shutdown() {

        if (this.timerMap.size() > 0) {
            for (AbstractTimerWork t : this.timerMap.values()) {
                t.cancel();
            }
            this.timerMap.clear();
        }
    }

    @Override
    public boolean scheduleWorkInPeriod(String workName, AbstractTimerWork r, Date firstDate, long period) {

        if (checkNull(workName, r, firstDate, period)) {
            return false;
        }

        Timer t = new Timer(workName, r.isDaemon());
        r.setTimer(t);
        r.setPeriod(period);
        TimerTask tt = createTimerTask(workName, r);

        try {
            t.schedule(tt, firstDate, period);
            return true;
        }
        catch (Exception e) {
            log.err(this, "Timer Worker[" + r.getName() + "] in Period starts FAIL.", e);
        }

        return false;
    }

    @Override
    public boolean scheduleWorkInPeriod(String workName, AbstractTimerWork r, long delay, long period) {

        if (workName == null || "".equals(workName) || r == null || period < 0) {
            return false;
        }

        Timer t = new Timer(workName, r.isDaemon());
        r.setTimer(t);
        r.setPeriod(period);
        TimerTask tt = createTimerTask(workName, r);

        try {
            t.schedule(tt, delay, period);
            return true;
        }
        catch (Exception e) {
            log.err(this, "Timer Worker[" + r.getName() + "] in Period  starts FAIL.", e);
        }

        return false;
    }

    @Override
    public Map<String, AbstractTimerWork> getAllTimerWork() {

        return Collections.unmodifiableMap(this.timerMap);
    }

}
