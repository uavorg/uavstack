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

package com.creditease.uav.messaging.api;

import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;

public abstract class MessageProducer {

    protected ISystemLogger log = null;

    protected String name;

    public MessageProducer(String name) {
        this.name = name;
    }

    public String getName() {

        return this.name;
    }

    public void setLogger(ISystemLogger logger) {

        log = logger;

    }

    public ISystemLogger getLog() {

        if (log == null) {
            log = SystemLogger.getLogger(MessageProducer.class);
        }
        return log;
    }

    /**
     * 提交异步作业
     * 
     * @param msg
     * @return
     */
    public abstract boolean submit(Message msg);

    public abstract void start();

    public abstract void stop();

    public void setMsgSizeLimit(long limit) {

    }
}
