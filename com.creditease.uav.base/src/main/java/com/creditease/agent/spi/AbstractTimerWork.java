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

package com.creditease.agent.spi;

import java.util.Timer;
import java.util.TimerTask;

public abstract class AbstractTimerWork extends AbstractComponent implements Runnable {

    protected Timer currentTimer;
    protected TimerTask currentTimerTask;
    protected long period;

    public AbstractTimerWork(String cName, String feature) {
        super(cName, feature);
    }

    public TimerTask getCurrentTimerTask() {

        return currentTimerTask;
    }

    public void setCurrentTimerTask(TimerTask currentTimerTask) {

        this.currentTimerTask = currentTimerTask;
    }

    public void setTimer(Timer tt) {

        this.currentTimer = tt;
    }

    public Timer getTimer() {

        return this.currentTimer;
    }

    public void cancel() {

        if (this.currentTimer != null) {
            this.currentTimer.cancel();
        }
    }

    public boolean isDaemon() {

        return true;
    }

    public long getPeriod() {

        return this.period;
    }

    public void setPeriod(long period) {

        this.period = period;
    }
}
