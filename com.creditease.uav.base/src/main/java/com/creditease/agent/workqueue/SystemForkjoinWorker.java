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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;

import com.creditease.agent.spi.AbstractComponent;
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
     * invokeAll方法
     */
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {

        return executor.invokeAll(tasks);
    }

    /*
     * submit方法
     */
    @Override
    public <T> ForkJoinTask<T> submit(ForkJoinTask<T> task) {

        return executor.submit(task);
    }

    /*
     * submit方法
     */
    @Override
    public <T> ForkJoinTask<T> submit(Callable<T> task) {

        return executor.submit(task);
    }

    /*
     * submit方法
     */

    @Override
    public <T> ForkJoinTask<T> submit(Runnable task, T result) {

        return executor.submit(task, result);
    }

    /*
     * submit方法
     */
    @Override
    public ForkJoinTask<?> submit(Runnable task) {

        return executor.submit(task);
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
    public int getParallelism() {

        return executor.getParallelism();
    }

    /*
     * Returns an estimate of the number of worker threads
     */
    @Override
    public int getRunningThreadCount() {

        return executor.getRunningThreadCount();
    }

    /*
     * Returns the number of worker threads
     */
    @Override
    public int getPoolSize() {

        return executor.getPoolSize();
    }

    /*
     * Returns the number of queued tasks
     */
    @Override
    public long getQueuedTaskCount() {

        return executor.getQueuedTaskCount();
    }

    /*
     * Returns a string identifying this pool
     */
    @Override
    public String toString() {

        return executor.toString();
    }
}
