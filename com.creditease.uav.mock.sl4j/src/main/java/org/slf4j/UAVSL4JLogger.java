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

package org.slf4j;

public abstract class UAVSL4JLogger implements Logger {

    public enum LogLevel {
        INFO, ERROR, DEBUG, WARN, TRACE
    }

    private static Logger slogger = null;

    public static Logger getLogger() {

        return slogger;
    }

    public static void setLogger(Logger logger) {

        slogger = logger;
    }

    protected abstract void doLog(LogLevel level, String msg, Throwable e, Object... args);

    @Override
    public String getName() {

        return "UAVSL4JLogger";
    }

    @Override
    public void trace(String msg) {

        doLog(LogLevel.TRACE, msg, null);
    }

    @Override
    public void trace(String format, Object arg) {

        doLog(LogLevel.TRACE, format, null, arg);

    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {

        doLog(LogLevel.TRACE, format, null, arg1, arg2);

    }

    @Override
    public void trace(String format, Object... arguments) {

        doLog(LogLevel.TRACE, format, null, arguments);

    }

    @Override
    public void trace(String msg, Throwable t) {

        doLog(LogLevel.TRACE, msg, t);

    }

    @Override
    public boolean isTraceEnabled(Marker marker) {

        return isTraceEnabled();
    }

    @Override
    public void trace(Marker marker, String msg) {

        doLog(LogLevel.TRACE, msg, null);

    }

    @Override
    public void trace(Marker marker, String format, Object arg) {

        doLog(LogLevel.TRACE, format, null, arg);

    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {

        doLog(LogLevel.TRACE, format, null, arg1, arg2);

    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {

        doLog(LogLevel.TRACE, format, null, argArray);

    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {

        doLog(LogLevel.TRACE, msg, null);

    }

    @Override
    public void debug(String msg) {

        doLog(LogLevel.DEBUG, msg, null);
    }

    @Override
    public void debug(String format, Object arg) {

        doLog(LogLevel.DEBUG, format, null, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {

        doLog(LogLevel.DEBUG, format, null, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {

        doLog(LogLevel.DEBUG, format, null, arguments);

    }

    @Override
    public void debug(String msg, Throwable t) {

        doLog(LogLevel.DEBUG, msg, t);

    }

    @Override
    public boolean isDebugEnabled(Marker marker) {

        return this.isDebugEnabled();
    }

    @Override
    public void debug(Marker marker, String msg) {

        doLog(LogLevel.DEBUG, msg, null);

    }

    @Override
    public void debug(Marker marker, String format, Object arg) {

        doLog(LogLevel.DEBUG, format, null, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {

        doLog(LogLevel.DEBUG, format, null, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {

        doLog(LogLevel.DEBUG, format, null, arguments);

    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {

        doLog(LogLevel.DEBUG, msg, t);

    }

    @Override
    public void info(String msg) {

        doLog(LogLevel.INFO, msg, null);

    }

    @Override
    public void info(String format, Object arg) {

        doLog(LogLevel.INFO, format, null, arg);

    }

    @Override
    public void info(String format, Object arg1, Object arg2) {

        doLog(LogLevel.INFO, format, null, arg1, arg2);

    }

    @Override
    public void info(String format, Object... arguments) {

        doLog(LogLevel.INFO, format, null, arguments);

    }

    @Override
    public void info(String msg, Throwable t) {

        doLog(LogLevel.INFO, msg, t);

    }

    @Override
    public boolean isInfoEnabled(Marker marker) {

        return this.isInfoEnabled();
    }

    @Override
    public void info(Marker marker, String msg) {

        doLog(LogLevel.INFO, msg, null);

    }

    @Override
    public void info(Marker marker, String format, Object arg) {

        doLog(LogLevel.INFO, format, null, arg);

    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {

        doLog(LogLevel.INFO, format, null, arg1, arg2);

    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {

        doLog(LogLevel.INFO, format, null, arguments);

    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {

        doLog(LogLevel.INFO, msg, t);

    }

    @Override
    public void warn(String msg) {

        doLog(LogLevel.WARN, msg, null);

    }

    @Override
    public void warn(String format, Object arg) {

        doLog(LogLevel.WARN, format, null, arg);
    }

    @Override
    public void warn(String format, Object... arguments) {

        doLog(LogLevel.WARN, format, null, arguments);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {

        doLog(LogLevel.WARN, format, null, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {

        doLog(LogLevel.WARN, msg, t);

    }

    @Override
    public boolean isWarnEnabled(Marker marker) {

        return this.isWarnEnabled();
    }

    @Override
    public void warn(Marker marker, String msg) {

        doLog(LogLevel.WARN, msg, null);

    }

    @Override
    public void warn(Marker marker, String format, Object arg) {

        doLog(LogLevel.WARN, format, null, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {

        doLog(LogLevel.WARN, format, null, arg1, arg2);

    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {

        doLog(LogLevel.WARN, format, null, arguments);

    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {

        doLog(LogLevel.WARN, msg, t);

    }

    @Override
    public void error(String msg) {

        doLog(LogLevel.ERROR, msg, null);

    }

    @Override
    public void error(String format, Object arg) {

        doLog(LogLevel.ERROR, format, null, arg);

    }

    @Override
    public void error(String format, Object arg1, Object arg2) {

        doLog(LogLevel.ERROR, format, null, arg1, arg2);

    }

    @Override
    public void error(String format, Object... arguments) {

        doLog(LogLevel.ERROR, format, null, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {

        doLog(LogLevel.ERROR, msg, t);

    }

    @Override
    public boolean isErrorEnabled(Marker marker) {

        return this.isErrorEnabled();
    }

    @Override
    public void error(Marker marker, String msg) {

        doLog(LogLevel.ERROR, msg, null);

    }

    @Override
    public void error(Marker marker, String format, Object arg) {

        doLog(LogLevel.ERROR, format, null, arg);

    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {

        doLog(LogLevel.ERROR, format, null, arg1, arg2);

    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {

        doLog(LogLevel.ERROR, format, null, arguments);

    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {

        doLog(LogLevel.ERROR, msg, t);
    }

}
