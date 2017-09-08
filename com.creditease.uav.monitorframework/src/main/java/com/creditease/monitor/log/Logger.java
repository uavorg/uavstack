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

package com.creditease.monitor.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * this Logger helps to involve the JEE application server's log system.
 * 
 * @author zhen zhang
 *
 */
public abstract class Logger {

    public enum LogLevel {
        INFO, ERROR, WARN, DEBUG
    }

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final String logname;
    private boolean enable = true;
    private boolean debugable = false;
    private int callerLevel = 3;

    public Logger(String name) {
        this.logname = name;
    }

    private String buildLog(LogLevel tag, String domain, String log, Throwable e) {

        StringBuilder sb = new StringBuilder();

        String exception = "";
        if (e != null) {
            exception = ",exception=" + exception2String(e);
        }

        return sb.append(buildTimeTag() + "	").append("[CE]	").append(tag).append("	").append("[" + this.logname + "]")
                .append(" ").append(domain + "	").append(log).append(exception).toString();
    }

    private String buildTimeTag() {

        return format.format(new Date());
    }

    private void doLog(LogLevel tag, String domain, String log, Throwable e) {

        if (isLogEnabled()) {

            if (tag == LogLevel.DEBUG && !this.isDebugable()) {
                return;
            }

            String logString = buildLog(tag, domain, log, e);
            doLog(tag, logString);
        }
    }

    protected abstract void doLog(LogLevel tag, String logstr);

    public void info(String log) {

        doLog(LogLevel.INFO, getCallerMethod(callerLevel), log, null);
    }

    public void error(String log, Throwable e) {

        doLog(LogLevel.ERROR, getCallerMethod(callerLevel), log, e);
    }

    public void warn(String log, Throwable e) {

        doLog(LogLevel.INFO, getCallerMethod(callerLevel), log, e);
    }

    public void debug(String log, Throwable e) {

        doLog(LogLevel.DEBUG, getCallerMethod(callerLevel), log, e);
    }

    public boolean isLogEnabled() {

        return this.enable;
    }

    public void setLogEnable(boolean enable) {

        this.enable = enable;
    }

    public boolean isDebugable() {

        return debugable;
    }

    public void setDebugable(boolean debugable) {

        this.debugable = debugable;
    }

    public int getCallerLevel() {

        return callerLevel;
    }

    public void setCallerLevel(int callerLevel) {

        this.callerLevel = callerLevel;
    }

    private String getCallerMethod(int level) {

        int l = 3;

        if (level > 0)
            l = level;

        StackTraceElement[] temp = Thread.currentThread().getStackTrace();
        StackTraceElement a = (StackTraceElement) temp[l];
        return a.getClassName() + "." + a.getMethodName();
    }

    private String exception2String(Throwable e) {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        pw.flush();
        sw.flush();

        return sw.toString();
    }

}
