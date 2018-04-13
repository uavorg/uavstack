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

package com.creditease.agent.log.api;

import java.util.logging.Formatter;

public interface IPLogger {

    public enum LogLevel {
        INFO, ERR, WARNING, DEBUG, FINE, FINER, ALL
    }

    public void setLogLevel(LogLevel level);

    public boolean enableConsoleOut(boolean check);

    public boolean enableFileOut(String filepattern, boolean check, int bufferSize, int fileSize, int fileCount,
            boolean isAppend, Formatter format);

    public void log(LogLevel level, String info, Object... objects);

    public void info(String info, Object... objects);

    public void warn(String info, Object... objects);

    public void err(String info, Object... objects);

    public void debug(String info, Object... objects);

    public void fine(String info, Object... objects);

    public void finer(String info, Object... objects);

    public void destroy();
}
