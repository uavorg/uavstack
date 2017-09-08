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

package com.creditease.agent.feature.logagent;

import java.util.List;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.agent.spi.AbstractParallelJob;
import com.creditease.agent.spi.AbstractPartitionJob;

/**
 * @author peihua
 */

public class TailFilesMutiJobs extends AbstractParallelJob {

    public TailFilesMutiJobs(ISystemLogger logger) {
        super(logger);
    }

    private final ISystemLogger log = (ISystemLogger) ConfigurationManager.getInstance().getComponent("logagent",
            "LogDataLog");

    private static final long serialVersionUID = -5268363221449586510L;

    @Override
    protected void addAllTask(List<AbstractPartitionJob> tasks) {

        @SuppressWarnings("unchecked")
        List<Long> existingInodes = (List<Long>) this.get("existingInodes");
        ReliableTaildirEventReader reader = (ReliableTaildirEventReader) this.get("reader");
        TaildirLogComponent tfref = (TaildirLogComponent) this.get("TailLogcomp");

        for (long inode : existingInodes) {

            AbstractPartitionJob job = new TailFileTaskJob();

            TailFile tf = reader.getTailFiles().get(inode);

            if (log.isDebugEnable()) {
                log.debug("this", "### TailFilesMutiJobs tf path:###" + tf.getPath());
                log.debug("this", "### TailFilesMutiJobs tf needTail:  ###" + tf.needTail());
            }
            if (tf.needTail()) {
                job.put("tfevent", tf);
                job.put("tfref", tfref);
                // job.put("reader", reader);
                tasks.add(job);
            }
        }
    }

}
