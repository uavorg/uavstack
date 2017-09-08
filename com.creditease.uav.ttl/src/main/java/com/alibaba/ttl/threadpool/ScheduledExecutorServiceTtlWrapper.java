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

package com.alibaba.ttl.threadpool;

import com.alibaba.ttl.TtlCallable;
import com.alibaba.ttl.TtlRunnable;
import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * {@link TransmittableThreadLocal} Wrapper of {@link ScheduledExecutorService},
 * transmit the {@link TransmittableThreadLocal} from the task submit time of {@link Runnable} or {@link Callable}
 * to the execution time of {@link Runnable} or {@link Callable}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 0.9.0
 */
class ScheduledExecutorServiceTtlWrapper extends ExecutorServiceTtlWrapper implements ScheduledExecutorService {
    final ScheduledExecutorService scheduledExecutorService;

    public ScheduledExecutorServiceTtlWrapper(ScheduledExecutorService scheduledExecutorService) {
        super(scheduledExecutorService);
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return scheduledExecutorService.schedule(TtlRunnable.get(command), delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return scheduledExecutorService.schedule(TtlCallable.get(callable), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return scheduledExecutorService.scheduleAtFixedRate(TtlRunnable.get(command), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return scheduledExecutorService.scheduleWithFixedDelay(TtlRunnable.get(command), initialDelay, delay, unit);
    }
}
