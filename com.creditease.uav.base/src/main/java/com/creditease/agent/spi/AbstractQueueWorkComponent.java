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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 实现带Queue的component
 * 
 * @author zhen zhang
 *
 * @param <T>
 */
public abstract class AbstractQueueWorkComponent<T> extends AbstractComponent implements Runnable {

    protected final BlockingQueue<T> eventQueue = new LinkedBlockingQueue<T>();

    protected int payLoadCount = 50;

    public AbstractQueueWorkComponent(String cName, String feature) {
        super(cName, feature);
    }

    public void setPayLoadCount(int count) {

        if (count < 0) {
            return;
        }

        this.payLoadCount = count;
    }

    @Override
    public void run() {

        while (!Thread.currentThread().isInterrupted()) {

            List<T> pl = new ArrayList<T>();

            try {
                // get notification event
                T data = eventQueue.take();

                pl.add(data);

                eventQueue.drainTo(pl, this.payLoadCount);

                if (log.isDebugEnable()) {
                    log.debug(this, "EventQueue Size:" + eventQueue.size());
                }

            }
            catch (InterruptedException e) {
                // ignore
                return;
            }

            try {
                handle(pl);
            }
            catch (Exception e) {
                log.err(this, "QueueWorkComponent [" + this.cName + "] handle Data FAIL.", e);
            }
        }
    }

    public void putData(T logData) {

        if (logData == null) {
            return;
        }

        try {
            this.eventQueue.put(logData);
        }
        catch (InterruptedException e) {
            log.err(this, "QueueWorkComponent [" + this.cName + "] put Data FAIL.", e);
        }
    }

    protected abstract void handle(List<T> data);
}
