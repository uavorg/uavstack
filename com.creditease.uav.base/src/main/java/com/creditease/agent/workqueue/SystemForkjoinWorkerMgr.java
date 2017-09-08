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
import com.creditease.agent.spi.IForkjoinWorker;
import com.creditease.agent.spi.IForkjoinWorkerMgr;

public class SystemForkjoinWorkerMgr implements IForkjoinWorkerMgr {

    private static final ISystemLogger logger = SystemLogger.getLogger(SystemForkjoinWorkerMgr.class);

    @Override
    public IForkjoinWorker getForkjoinWorker(String feature, String queueWorkerName) {

        if (StringHelper.isEmpty(queueWorkerName)) {
            return null;
        }

        return (IForkjoinWorker) ConfigurationManager.getInstance().getComponent(feature, queueWorkerName);
    }

    @Override
    public IForkjoinWorker newForkjoinWorker(String queueWorkerName, String feature, int maxConcurrent) {

        IForkjoinWorker worker = new SystemForkjoinWorker(queueWorkerName, feature, maxConcurrent);

        return worker;
    }

    @Override
    public void shutdown(String feature, String queueWorkerName) {

        if (null == queueWorkerName) {
            return;
        }

        IForkjoinWorker qw = (IForkjoinWorker) ConfigurationManager.getInstance().getComponent(feature,
                queueWorkerName);

        if (qw == null) {
            return;
        }

        try {
            qw.shutdown();
        }
        catch (RuntimeException e) {
            logger.err(this, "Shutting Down ForkjoinWorker[" + qw.getFeature() + "." + qw.getName() + "] FAILs. ", e);
        }
    }

    @Override
    public void shutdown() {

        Set<IForkjoinWorker> workers = ConfigurationManager.getInstance().getComponents(IForkjoinWorker.class);

        for (IForkjoinWorker work : workers) {

            try {
                work.shutdown();
            }
            catch (RuntimeException e) {
                logger.err(this,
                        "Shutting Down ForkjoinWorker[" + work.getFeature() + "." + work.getName() + "] FAILs. ", e);
            }

        }
    }

}
