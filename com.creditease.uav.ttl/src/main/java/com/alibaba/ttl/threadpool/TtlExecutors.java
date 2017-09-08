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

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Factory Utils for getting TTL Wrapper of jdk executors.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 0.9.0
 */
public final class TtlExecutors {
    /**
     * {@link TransmittableThreadLocal} Wrapper of {@link Executor},
     * transmit the {@link TransmittableThreadLocal} from the task submit time of {@link Runnable}
     * to the execution time of {@link Runnable}.
     */
    public static Executor getTtlExecutor(Executor executor) {
        if (null == executor || executor instanceof ExecutorTtlWrapper) {
            return executor;
        }
        return new ExecutorTtlWrapper(executor);
    }

    /**
     * {@link TransmittableThreadLocal} Wrapper of {@link ExecutorService},
     * transmit the {@link TransmittableThreadLocal} from the task submit time of {@link Runnable} or {@link java.util.concurrent.Callable}
     * to the execution time of {@link Runnable} or {@link java.util.concurrent.Callable}.
     */
    public static ExecutorService getTtlExecutorService(ExecutorService executorService) {
        if (executorService == null || executorService instanceof ExecutorServiceTtlWrapper) {
            return executorService;
        }
        return new ExecutorServiceTtlWrapper(executorService);
    }

    /**
     * {@link TransmittableThreadLocal} Wrapper of {@link ScheduledExecutorService},
     * transmit the {@link TransmittableThreadLocal } from the task submit time of {@link Runnable} or {@link java.util.concurrent.Callable}
     * to the execution time of {@link Runnable} or {@link java.util.concurrent.Callable}.
     */
    public static ScheduledExecutorService getTtlScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
        if (scheduledExecutorService == null || scheduledExecutorService instanceof ScheduledExecutorServiceTtlWrapper) {
            return scheduledExecutorService;
        }
        return new ScheduledExecutorServiceTtlWrapper(scheduledExecutorService);
    }

    private TtlExecutors() {
    }
}
