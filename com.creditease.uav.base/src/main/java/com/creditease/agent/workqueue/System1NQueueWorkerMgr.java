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

import java.util.Set;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.agent.spi.I1NQueueWorker;
import com.creditease.agent.spi.I1NQueueWorkerMgr;

public class System1NQueueWorkerMgr implements I1NQueueWorkerMgr {

    private static final ISystemLogger logger = SystemLogger.getLogger(System1NQueueWorkerMgr.class);

    /**
     * getQueueWorker
     * 
     * get a created queue worker with queue worker's name
     * 
     * @param queueWorkerName
     * @return
     */
    @Override
    public I1NQueueWorker getQueueWorker(String feature, String queueWorkerName) {

        if (StringHelper.isEmpty(queueWorkerName)) {
            return null;
        }

        return (I1NQueueWorker) ConfigurationManager.getInstance().getComponent(feature, queueWorkerName);
    }

    /**
     * 关闭单个QueueWorker
     * 
     * @param queueWorkerName
     */
    @Override
    public void shutdown(String feature, String queueWorkerName) {

        if (null == queueWorkerName) {
            return;
        }

        I1NQueueWorker qw = (I1NQueueWorker) ConfigurationManager.getInstance().getComponent(feature, queueWorkerName);

        if (qw == null) {
            return;
        }

        try {
            qw.shutdown();
        }
        catch (RuntimeException e) {
            logger.err(this, "Shutting Down QueueWorker[" + qw.getFeature() + "." + qw.getName() + "] FAILs. ", e);
        }
    }

    /**
     * 关闭所有QueueWorker
     */
    @Override
    public void shutdown() {

        Set<I1NQueueWorker> workers = ConfigurationManager.getInstance().getComponents(I1NQueueWorker.class);

        for (I1NQueueWorker work : workers) {

            try {
                work.shutdown();
            }
            catch (RuntimeException e) {
                logger.err(this, "Shutting Down QueueWorker[" + work.getFeature() + "." + work.getName() + "] FAILs. ",
                        e);
            }

        }
    }

    /**
     * 创建新的QueueWorker
     */
    @Override
    public I1NQueueWorker newQueueWorker(String queueWorkerName, String feature, int coreSize, int maxSize,
            int bQueueSize, int keepAliveTimeout) {

        I1NQueueWorker worker = new System1NQueueWorker(queueWorkerName, feature, coreSize, maxSize, bQueueSize,
                keepAliveTimeout);

        return worker;
    }

}
