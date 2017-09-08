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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import com.creditease.agent.spi.Abstract1NTask;
import com.creditease.agent.spi.AbstractComponent;
import com.creditease.agent.spi.I1NQueueWorker;
import com.creditease.uav.helpers.thread.QueueWorkerRejectedExecutionHandler;
import com.creditease.uav.helpers.thread.QueueWorkerThreadPoolExecutor;
import com.creditease.uav.helpers.thread.QueueWorkerThreadPoolExecutor.QueueWorkerThread;
import com.creditease.uav.helpers.thread.QueueWorkerThreadPoolExecutor.QueueWorkerThreadFactory;

public class System1NQueueWorker extends AbstractComponent implements I1NQueueWorker {

    // 请求队列
    protected final LinkedBlockingQueue<Abstract1NTask> lbQueue;
    // 拒绝队列
    protected final QueueWorkerRejectedExecutionHandler handler;
    // 线程池
    protected final QueueWorkerThreadPoolExecutor executor;
    // threadfactory
    protected final QueueWorkerThreadFactory threadFactory;

    /*
     * 构造方法
     */
    protected System1NQueueWorker(String name, String feature, int coreSize, int maxSize, int bQueueSize,
            int keepAliveTimeout) {
        super(name, feature);
        lbQueue = new LinkedBlockingQueue<Abstract1NTask>();
        handler = new QueueWorkerThreadPoolExecutor.DequeTaskPushPolicy();
        threadFactory = new QueueWorkerThreadPoolExecutor.QueueWorkerThreadFactory(name);
        executor = new QueueWorkerThreadPoolExecutor(coreSize, maxSize, keepAliveTimeout, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(bQueueSize), threadFactory, handler);

    }

    /*
     * 遍历消息队列，put任务到线程池执行
     */
    @Override
    public void run() {

        while (!Thread.interrupted()) {

            Abstract1NTask task = null;
            try {
                task = lbQueue.take();

            }
            catch (InterruptedException e) {
                log.err(this, "Taking QueueTask is Interrupted. ", e);
                return;
            }

            if (null == task) {
                continue;
            }

            try {
                executor.execute(task);
            }
            catch (RejectedExecutionException e) {

                log.err(this, "Execute QueueTask[" + task.getName() + "] is Rejected", e);
            }
        }
    }

    /*
     * put任务到lbq
     */
    @Override
    public void put(Abstract1NTask qt) {

        if (null == qt) {
            return;
        }

        try {
            lbQueue.put(qt);
        }
        catch (InterruptedException e) {
            log.err(this, "Putting QueueTask[" + qt.getName() + "] is Rejected. params=", e);
        }
    }

    /**
     * using this method to elastic adjustment
     * 
     * @param coreSize
     * @param maxSize
     * @param keepAliveTimeout
     */
    @Override
    public void adjust(int coreSize, int maxSize, int keepAliveTimeout) {

        if (keepAliveTimeout > 0) {
            this.executor.setKeepAliveTime(keepAliveTimeout, TimeUnit.SECONDS);
        }

        if (coreSize > 0) {
            this.executor.setCorePoolSize(coreSize);
        }

        if (maxSize > 0) {
            this.executor.setMaximumPoolSize(maxSize);
        }
    }

    @Override
    public int getActiveCount() {

        return this.executor.getActiveCount();
    }

    @Override
    public long getCompletedCount() {

        return this.executor.getCompletedTaskCount();
    }

    @Override
    public int getLargestPoolSize() {

        return this.executor.getLargestPoolSize();
    }

    @Override
    public int getCorePoolSize() {

        return this.executor.getCorePoolSize();
    }

    @Override
    public long getTaskCount() {

        return this.executor.getTaskCount();
    }

    @Override
    public int getTaskQueueSize() {

        return this.lbQueue.size();
    }

    @Override
    public int getExecutorQueueSize() {

        return this.executor.getQueue().size();
    }

    @Override
    public int getPoolSize() {

        return this.executor.getPoolSize();
    }

    /**
     * 
     * get the state data into json string
     * 
     * @param requireThreadDetail
     * @return
     */
    @Override
    public String toJSONString(boolean requireThreadDetail) {

        StringBuffer sbf = new StringBuffer("{");
        sbf.append("Q:").append(this.getTaskQueueSize()).append(",");
        sbf.append("BQ:").append(this.getExecutorQueueSize()).append(",");
        sbf.append("RP:").append(this.getPoolSize()).append(",");
        sbf.append("AP:").append(this.getActiveCount()).append(",");
        sbf.append("LP:").append(this.getLargestPoolSize()).append(",");
        sbf.append("CP:").append(this.getCorePoolSize()).append(",");
        sbf.append("TskD:").append(this.getCompletedCount()).append(",");
        sbf.append("Tsk:").append(this.getTaskCount());

        /**
         * if need detail state of evey thread
         */
        if (requireThreadDetail == true) {

            sbf.append(",");
            ThreadGroup tg = threadFactory.getThreadGroup();
            Thread[] thd = new Thread[tg.activeCount()];
            tg.enumerate(thd);
            for (int i = 0; i < thd.length; i++) {
                QueueWorkerThread qwt = (QueueWorkerThread) thd[i];

                if (!qwt.isAlive() || qwt.isInterrupted()) {
                    continue;
                }
                sbf.append("T" + qwt.getId()).append(":").append(qwt.getWorkQueueSize());

                if (i < thd.length - 1) {
                    sbf.append(",");
                }
            }
        }

        return sbf.append("}").toString();
    }

    @Override
    public void shutdown() {

        lbQueue.clear();

        this.getConfigManager().unregisterComponent(this.feature, this.cName);

        this.executor.shutdown();
    }

}
