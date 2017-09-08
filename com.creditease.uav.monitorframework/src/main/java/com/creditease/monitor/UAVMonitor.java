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

package com.creditease.monitor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.creditease.agent.helpers.DateTimeHelper;
import com.creditease.monitor.log.Logger;

public class UAVMonitor {

    private volatile long maxCost = -1;
    private volatile long minCost = -1;
    private volatile AtomicInteger reqCount = new AtomicInteger(0);
    private volatile AtomicLong reqTimeSum = new AtomicLong(0);
    private volatile long stTime = -1;
    private volatile Logger log;
    private long howLongLog = 60000;

    public UAVMonitor(Logger log, long howLongLog) {
        this.log = log;
        if (howLongLog > 0) {
            this.howLongLog = howLongLog;
        }
    }

    public void logPerf(long reqStTime, String message) {

        if (stTime == -1) {

            synchronized (this) {

                if (stTime == -1) {
                    stTime = System.currentTimeMillis();
                }

            }
        }

        record(reqStTime, message);
    }

    private void record(long reqStTime, String message) {

        if (this.stTime == -1) {
            return;
        }

        long endTime = System.currentTimeMillis();

        long cost = endTime - reqStTime;

        if (maxCost == -1) {
            maxCost = cost;
        }

        if (minCost == -1) {
            minCost = cost;
        }

        if (cost > maxCost) {
            maxCost = cost;
        }

        if (cost < minCost) {
            minCost = cost;
        }

        reqTimeSum.addAndGet(cost);

        reqCount.incrementAndGet();

        if (endTime - this.stTime < this.howLongLog) {
            return;
        }

        synchronized (this) {

            if (this.stTime == -1) {
                return;
            }

            if (endTime - this.stTime < this.howLongLog) {
                return;
            }

            long avgCost = -1;

            int count = reqCount.get();
            long total = reqTimeSum.get();
            if (count > 0) {
                avgCost = total / count;
            }

            if (log.isLogEnabled()) {
                log.info("[" + Thread.currentThread().getId() + "(" + Thread.currentThread().getName() + ")]    "
                        + "UAV.MOF  " + DateTimeHelper.toStandardDateFormat(this.stTime) + "~"
                        + DateTimeHelper.toStandardDateFormat(endTime) + "(" + this.howLongLog / 1000 + "): max="
                        + this.maxCost + ",min=" + this.minCost + ",avg=" + avgCost + ",total=" + total + ",req="
                        + count + ",msg=" + message);
            }

            this.stTime = -1;
            this.maxCost = -1;
            this.minCost = -1;
            reqCount.set(0);
            reqTimeSum.set(0);
        }
    }
}
