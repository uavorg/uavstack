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

package com.alibaba.ttl;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link TtlRunnable} decorate {@link Runnable}, so as to get {@link TransmittableThreadLocal} and transmit it to the
 * time of {@link Runnable} execution, needed when use {@link Runnable} to thread pool.
 * <p>
 * Use factory methods {@link #get} / {@link #gets} to create instance.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see java.util.concurrent.Executor
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 * @see java.util.concurrent.Executors
 * @since 0.9.0
 */
public final class TtlRunnable implements Runnable {

    private final AtomicReference<Map<TransmittableThreadLocal<?>, Object>> copiedRef;
    private final Runnable runnable;
    private final boolean releaseTtlValueReferenceAfterRun;

    private TtlRunnable(Runnable runnable, boolean releaseTtlValueReferenceAfterRun) {

        this.copiedRef = new AtomicReference<Map<TransmittableThreadLocal<?>, Object>>(TransmittableThreadLocal.copy());
        this.runnable = runnable;
        this.releaseTtlValueReferenceAfterRun = releaseTtlValueReferenceAfterRun;
    }

    /**
     * wrap method {@link Runnable#run()}.
     */
    @Override
    public void run() {

        Map<TransmittableThreadLocal<?>, Object> copied = copiedRef.get();
        if (copied == null || releaseTtlValueReferenceAfterRun && !copiedRef.compareAndSet(copied, null)) {
            throw new IllegalStateException("TTL value reference is released after run!");
        }

        Map<TransmittableThreadLocal<?>, Object> backup = TransmittableThreadLocal.backupAndSetToCopied(copied);
        try {
            runnable.run();
        }
        finally {
            TransmittableThreadLocal.restoreBackup(backup);
        }
    }

    /**
     * return original/unwrapped {@link Runnable}.
     */
    public Runnable getRunnable() {

        return runnable;
    }

    /**
     * Factory method, wrapper input {@link Runnable} to {@link TtlRunnable}.
     *
     * @param runnable
     *            input {@link Runnable}. if input is {@code null}, return {@code null}.
     * @return Wrapped {@link Runnable}
     * @throws IllegalStateException
     *             when input is {@link TtlRunnable} already.
     */
    public static Runnable get(Runnable runnable) {

        return get(runnable, false, false);
    }

    /**
     * Factory method, wrapper input {@link Runnable} to {@link TtlRunnable}.
     *
     * @param runnable
     *            input {@link Runnable}. if input is {@code null}, return {@code null}.
     * @param releaseTtlValueReferenceAfterRun
     *            release TTL value reference after run, avoid memory leak even if {@link TtlRunnable} is referred.
     * @return Wrapped {@link Runnable}
     * @throws IllegalStateException
     *             when input is {@link TtlRunnable} already.
     */
    public static Runnable get(Runnable runnable, boolean releaseTtlValueReferenceAfterRun) {

        return get(runnable, releaseTtlValueReferenceAfterRun, false);
    }

    /**
     * Factory method, wrapper input {@link Runnable} to {@link TtlRunnable}.
     *
     * @param runnable
     *            input {@link Runnable}. if input is {@code null}, return {@code null}.
     * @param releaseTtlValueReferenceAfterRun
     *            release TTL value reference after run, avoid memory leak even if {@link TtlRunnable} is referred.
     * @param idempotent
     *            is idempotent mode or not. if {@code true}, just return input {@link Runnable} when it's
     *            {@link TtlRunnable}, otherwise throw {@link IllegalStateException}. <B><I>Caution</I></B>:
     *            {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
     * @return Wrapped {@link Runnable}
     * @throws IllegalStateException
     *             when input is {@link TtlRunnable} already and not idempotent.
     */
    public static Runnable get(Runnable runnable, boolean releaseTtlValueReferenceAfterRun, boolean idempotent) {

        if (null == runnable) {
            return null;
        }

        if (runnable instanceof TtlRunnable) {
            if (idempotent) {
                // avoid redundant decoration, and ensure idempotency
                return (TtlRunnable) runnable;
            }
            else {
                throw new IllegalStateException("Already TtlRunnable!");
            }
        }
        // 如果runnable的具体实现为java.util.concurrent.FutureTask，则认为是通过submit调用的execute
        if (runnable.getClass().getName().equals("java.util.concurrent.FutureTask")) {
            return new TtlRunnable(runnable, releaseTtlValueReferenceAfterRun);
        }
        // 如果runnable的具体实现类只实现了runnable接口，则无需使用代理
        Set<Class<?>> interfaces = getAllInterfaces(runnable.getClass(), true);
        if (interfaces.size() == 1) {
            return new TtlRunnable(runnable, releaseTtlValueReferenceAfterRun);
        }
        Class<?>[] interfaceClass = new Class<?>[interfaces.size()];
        int i = 0;
        for (Class<?> temp : interfaces) {
            interfaceClass[i] = temp;
            i++;
        }
        // 对于实现了除runnable接口以外的多接口现象的支持
        TtlRunnable ttlRunnable = new TtlRunnable(runnable, releaseTtlValueReferenceAfterRun);
        return (Runnable) Proxy.newProxyInstance(runnable.getClass().getClassLoader(), interfaceClass,
                new TtlRunnableProxy(ttlRunnable));
    }

    /**
     * 获取当前类所有实现的接口
     * 
     * @param cls
     * @param needSuper
     * @return
     */
    public static Set<Class<?>> getAllInterfaces(Class<?> cls, boolean needSuper) {

        Set<Class<?>> ai = new HashSet<Class<?>>();

        Class<?>[] interfaces = cls.getInterfaces();

        if (interfaces != null) {
            ai.addAll(Arrays.asList(interfaces));
        }

        if (needSuper == false) {
            return ai;
        }

        Class<?> superCls = cls.getSuperclass();
        while (superCls != null) {

            Class<?>[] ifs = superCls.getInterfaces();

            if (ifs != null) {
                ai.addAll(Arrays.asList(ifs));
            }

            superCls = superCls.getSuperclass();
        }

        return ai;
    }

    /**
     * wrapper input {@link Runnable} Collection to {@link TtlRunnable} Collection.
     *
     * @param tasks
     *            task to be wrapped. if input is {@code null}, return {@code null}.
     * @return wrapped tasks
     * @throws IllegalStateException
     *             when input is {@link TtlRunnable} already.
     */
    public static List<Runnable> gets(Collection<? extends Runnable> tasks) {

        return gets(tasks, false, false);
    }

    /**
     * wrapper input {@link Runnable} Collection to {@link TtlRunnable} Collection.
     *
     * @param tasks
     *            task to be wrapped. if input is {@code null}, return {@code null}.
     * @param releaseTtlValueReferenceAfterRun
     *            release TTL value reference after run, avoid memory leak even if {@link TtlRunnable} is referred.
     * @return wrapped tasks
     * @throws IllegalStateException
     *             when input is {@link TtlRunnable} already.
     */
    public static List<Runnable> gets(Collection<? extends Runnable> tasks, boolean releaseTtlValueReferenceAfterRun) {

        return gets(tasks, releaseTtlValueReferenceAfterRun, false);
    }

    /**
     * wrapper input {@link Runnable} Collection to {@link TtlRunnable} Collection.
     *
     * @param tasks
     *            task to be wrapped. if input is {@code null}, return {@code null}.
     * @param releaseTtlValueReferenceAfterRun
     *            release TTL value reference after run, avoid memory leak even if {@link TtlRunnable} is referred.
     * @param idempotent
     *            is idempotent mode or not. if {@code true}, just return input {@link Runnable} when it's
     *            {@link TtlRunnable}, otherwise throw {@link IllegalStateException}. <B><I>Caution</I></B>:
     *            {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
     * @return wrapped tasks
     * @throws IllegalStateException
     *             when input is {@link TtlRunnable} already and not idempotent.
     */
    public static List<Runnable> gets(Collection<? extends Runnable> tasks, boolean releaseTtlValueReferenceAfterRun,
            boolean idempotent) {

        if (null == tasks) {
            return Collections.emptyList();
        }
        List<Runnable> copy = new ArrayList<Runnable>();
        for (Runnable task : tasks) {
            copy.add(TtlRunnable.get(task, releaseTtlValueReferenceAfterRun, idempotent));
        }
        return copy;
    }

}
