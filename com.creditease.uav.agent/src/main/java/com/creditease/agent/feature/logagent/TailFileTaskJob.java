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
import java.util.Map;
import java.util.Map.Entry;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.agent.spi.AbstractPartitionJob;

/**
 * @author peihua
 */

public class TailFileTaskJob extends AbstractPartitionJob {

    private final ISystemLogger log = (ISystemLogger) ConfigurationManager.getInstance().getComponent("logagent",
            "LogDataLog");
    private static final long serialVersionUID = -5268363221449586510L;

    private static ThreadLocal<TaildirLogComponent> tfref = new ThreadLocal<TaildirLogComponent>();

    public void setCurrenttfref(TaildirLogComponent c) {

        tfref.set(c);
    }

    public TaildirLogComponent getCurrenttfref() {

        return tfref.get();
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void work() {

        Map<TailFile, List<Map>> serverlogs = null;
        try {
            TailFile tf = (TailFile) this.get("tfevent");
            this.setCurrenttfref((TaildirLogComponent) this.get("tfref"));
            // tfref = ((TaildirLogComponent) this.get("tfref"));
            serverlogs = this.getCurrenttfref().tailFileProcessSeprate(tf, true);

            for (Entry<TailFile, List<Map>> applogs : serverlogs.entrySet()) {
                if (log.isDebugEnable()) {
                    log.debug(this, "### Logvalue ###: " + applogs.getValue());
                }
            }

            if (!(serverlogs.isEmpty())) {
                this.getCurrenttfref().sendLogDataBatch(serverlogs);
            }
            else {
                if (log.isDebugEnable()) {
                    log.debug(this, "serverlogs is emptry!!!");
                }
            }

        }
        catch (Throwable t) {
            log.err(this, "Unable to tail files.", t);
        }
        finally {
            if (log.isDebugEnable()) {
                log.debug(this, "finally invoked...");
            }
            if (null != serverlogs) {
                serverlogs.clear();
            }
        }

    }

}
