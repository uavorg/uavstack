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

package com.creditease.uav.mscp.plus;

import com.creditease.agent.SystemStarter;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.monitor.log.Logger;

public class MSCPLog extends Logger {

    private ISystemLogger log;

    public MSCPLog(String name) {
        super(name);
        log = SystemLogger.getLogger(SystemStarter.class);
    }

    @Override
    protected void doLog(LogLevel tag, String logstr) {

        switch (tag) {
            case DEBUG:
                this.log.debug(this, logstr);
                break;
            case ERROR:
                this.log.err(this, logstr);
                break;
            case INFO:
                this.log.info(this, logstr);
                break;
            case WARN:
                this.log.warn(this, logstr);
                break;
        }
    }

}
