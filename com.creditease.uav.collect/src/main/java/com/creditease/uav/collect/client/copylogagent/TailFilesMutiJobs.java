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

package com.creditease.uav.collect.client.copylogagent;

import java.util.List;

import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.agent.spi.AbstractParallelJob;
import com.creditease.agent.spi.AbstractPartitionJob;

public class TailFilesMutiJobs extends AbstractParallelJob {

    private static final long serialVersionUID = -5268363221449586510L;

    private ISystemLogger logger;
    
    public TailFilesMutiJobs(ISystemLogger logger) {
        super(logger);
    }

    @Override
    protected void addAllTask(List<AbstractPartitionJob> tasks) {

        @SuppressWarnings("unchecked")
        List<Long> existingInodes = (List<Long>) this.get("existingInodes");
        ReliableTaildirEventReader reader = (ReliableTaildirEventReader) this.get("reader");
        CopyOfProcessOfLogagent cpy = (CopyOfProcessOfLogagent) this.get("TailLogcomp");

        for (long inode : existingInodes) {

            AbstractPartitionJob job = new TailFileTaskJob();

            TailFile tf = reader.getTailFiles().get(inode);

            if (logger.isDebugEnable()) {
                logger.debug("this", "### TailFilesMutiJobs tf path:###" + tf.getPath());
                logger.debug("this", "### TailFilesMutiJobs tf needTail:  ###" + tf.needTail());
            }
            if (tf.needTail()) {
                job.put("tfevent", tf);
                job.put("tfref", cpy);
                // job.put("reader", reader);
                tasks.add(job);
            }
        }
    }

}
