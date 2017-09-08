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

import com.creditease.agent.log.api.ISystemLogger;

/**
 * @author peihua
 */

public abstract class AbstractParallelJob extends AbstractPartitionJob {

    private static final long serialVersionUID = 2418083879805108003L;

    private ISystemLogger log = null;

    List<AbstractPartitionJob> tasks = new ArrayList<AbstractPartitionJob>();

    public AbstractParallelJob(ISystemLogger logger) {

        log = logger;
    }

    @Override
    protected void work() {

        addAllTask(tasks);

        startAllT();
    }

    abstract protected void addAllTask(List<AbstractPartitionJob> tasks);

    /**
     * Start All tasks
     */
    protected void startAllT() {

        if (tasks.size() > 0) {
            if (log.isDebugEnable()) {
                log.debug("this", "AbstractParallelJob tasks.size(): " + tasks.size());
            }
            invokeAll(tasks);
        }
        else {
            if (log.isDebugEnable()) {
                log.debug("this", "AbstractParallelJob tasks size is zero ##");
            }
        }
    }

    /**
     * Return while all tasks finished
     */
    public void waitAllTaskDone() {

        try {
            super.get();
        }
        catch (Exception e) {
            log.warn(this, e.getMessage());
        }
    }
}
