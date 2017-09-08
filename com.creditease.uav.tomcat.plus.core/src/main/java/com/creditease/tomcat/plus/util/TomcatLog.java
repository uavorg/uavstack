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

package com.creditease.tomcat.plus.util;

import org.apache.catalina.startup.CatalinaProperties;
import org.apache.juli.logging.LogFactory;

import com.creditease.monitor.log.Logger;

public class TomcatLog extends Logger {

    public static Logger getLog(Class<?> c) {

        return getLog(c.getName());
    }

    public static Logger getLog(String name) {

        return new TomcatLog(name);
    }

    private org.apache.juli.logging.Log log;

    public TomcatLog(String name) {
        super(name);
        this.log = LogFactory.getLog(name);
        Boolean debugcheck = Boolean.valueOf(CatalinaProperties.getProperty("com.creditease.monitor.debug"));
        this.setDebugable(debugcheck);
    }

    @Override
    protected void doLog(LogLevel tag, String logstr) {

        switch (tag) {
            case DEBUG:
                this.log.debug(logstr);
                break;
            case ERROR:
                this.log.error(logstr);
                break;
            case INFO:
                this.log.info(logstr);
                break;
            case WARN:
                this.log.warn(logstr);
                break;
        }
    }

}
