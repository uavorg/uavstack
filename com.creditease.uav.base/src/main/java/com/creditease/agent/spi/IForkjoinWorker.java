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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;

public interface IForkjoinWorker {

    public String getName();

    public String getFeature();

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks);

    public <T> ForkJoinTask<T> submit(ForkJoinTask<T> task);

    public <T> ForkJoinTask<T> submit(Callable<T> task);

    public <T> ForkJoinTask<T> submit(Runnable task, T result);

    public ForkJoinTask<?> submit(Runnable task);

    public void shutdown();

    public int getParallelism();

    public int getRunningThreadCount();

    public int getPoolSize();

    public long getQueuedTaskCount();

    @Override
    public String toString();

}
