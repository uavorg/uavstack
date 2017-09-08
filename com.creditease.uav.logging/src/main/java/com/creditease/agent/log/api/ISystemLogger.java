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

public interface ISystemLogger {

    public void enableTrace(boolean check);

    public boolean isTraceEnable();

    public void enableDebug(boolean check);

    public boolean isDebugEnable();

    public void setLevel(IPLogger.LogLevel level);

    public void info(Object domain, String info, Object... objects);

    public void info(Object domain, String info, int level, Object... objects);

    public void warn(Object domain, String info, Object... objects);

    public void warn(Object domain, String info, int level, Object... objects);

    public void err(Object domain, String info, Object... objects);

    public void err(Object domain, String info, int level, Object... objects);

    public void debug(Object domain, String info, Object... objects);

    public void debug(Object domain, String info, int level, Object... objects);

    public void fine(Object domain, String info, Object... objects);

    public void fine(Object domain, String info, int level, Object... objects);

    public void finer(Object domain, String info, Object... objects);

    public void finer(Object domain, String info, int level, Object... objects);

    public void trace(Object domain, String info, Object... objects);
}
