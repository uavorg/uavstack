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

import java.util.concurrent.ForkJoinPool;

import com.creditease.agent.spi.AbstractComponent;
import com.creditease.agent.spi.AbstractPartitionJob;
import com.creditease.agent.spi.IForkjoinWorker;

/**
 * @author peihua
 */

public class SystemForkjoinWorker extends AbstractComponent implements IForkjoinWorker {

    private ForkJoinPool executor;

    /*
     * 构造方法
     */
    public SystemForkjoinWorker(String name, String feature, int maxthreadCount) {
        super(name, feature);
        executor = new ForkJoinPool(maxthreadCount);
    }

    /*
     * submit方法
     */
    @Override
    public void submitTask(AbstractPartitionJob task) {

        executor.submit(task);
    }

    /*
     * 关闭ForkJoinPool线程池
     */
    @Override
    public void shutdown() {

        this.getConfigManager().unregisterComponent(this.feature, this.cName);

        executor.shutdown();
    }

    /*
     * parallelism level of this pool
     */
    @Override
    public void getParallelism() {

        executor.getParallelism();
    }

    /*
     * Returns an estimate of the number of worker threads
     */
    @Override
    public void getRunningThreadCount() {

        executor.getRunningThreadCount();
    }

    /*
     * Returns the number of worker threads
     */
    @Override
    public void getPoolSize() {

        executor.getPoolSize();
    }

    /*
     * Returns the number of queued tasks
     */
    @Override
    public void getQueuedTaskCount() {

        executor.getQueuedTaskCount();
    }

    /*
     * Returns a string identifying this pool
     */
    @Override
    public String toString() {

        return executor.toString();
    }
}
