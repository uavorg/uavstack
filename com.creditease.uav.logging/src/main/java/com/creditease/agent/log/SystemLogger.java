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

package com.creditease.agent.log;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.log.PLogger.DefaultLogFormatter;
import com.creditease.agent.log.PLogger.SimpleLogFormatter;
import com.creditease.agent.log.api.IPLogger;
import com.creditease.agent.log.api.IPLogger.LogLevel;
import com.creditease.agent.log.api.ISystemLogger;

/**
 * @author zhen zhang
 */
public class SystemLogger implements ISystemLogger {

    private static ISystemLogger mainLog = null;

    private static Set<String> logSet = new HashSet<String>();

    /**
     * 先执行init, getLogger(Class<?> c),getLogger()可用
     * 
     * @param logLevel
     * @param debugEnable
     */
    public static void init(String logLevel, Boolean debugEnable, int filesize) {

        if (mainLog == null) {
            mainLog = newLogger("mainLog", "agentlog.%g.%u.log", logLevel, debugEnable, 100, filesize);
        }
    }

    public static void init(String logPattern, String logLevel, Boolean debugEnable, int bufferSize, int filesize) {

        if (mainLog == null) {
            mainLog = newLogger("mainLog", logPattern + ".%g.%u.log", logLevel, debugEnable, bufferSize, filesize);
        }
    }

    public static void init(String logPattern, String logPath, String logLevel, Boolean debugEnable, int filesize) {

        if (mainLog == null) {
            mainLog = newLogger("mainLog", logPath, logPattern + ".%g.%u.log", logLevel, debugEnable, 100, filesize);
        }
    }

    public static void init(String logPattern, String logPath, String logLevel, Boolean debugEnable, int buffersize,
            int filesize) {

        if (mainLog == null) {
            mainLog = newLogger("mainLog", logPath, logPattern + ".%g.%u.log", logLevel, debugEnable, buffersize,
                    filesize);
        }
    }

    /**
     * using default main logger to record Class related trace require init() first
     * 
     * @param c
     * @return
     */
    public static ISystemLogger getLogger(Class<?> c) {

        return getLogger();
    }

    /**
     * new an ISystemLogger instance with the logName
     * 
     * @param logName
     * @return
     */
    public static ISystemLogger getLogger(String logName, String logFileFormat, String logLevel, Boolean debugEnable,
            int fileSize) {

        return newLogger(logName, logFileFormat, logLevel, debugEnable, 100, fileSize);
    }

    /**
     * TestLog@usage
     * 
     * @param logName
     * @return
     */

    public static ISystemLogger getTestlogger(String logName, String logFileFormat, String logLevel,
            Boolean debugEnable, int fileSize, int fileCountLimit) {

        String rootPath = IOHelper.getCurrentPath();

        return newLogger(logName, rootPath, logFileFormat, logLevel, debugEnable, 100, fileSize, fileCountLimit);
    }

    /**
     * get the default main log require init() first
     * 
     * @return
     */
    private static ISystemLogger getLogger() {

        return mainLog;
    }

    private static ISystemLogger newLogger(String logName, String logFileFormat, String logLevel, Boolean debugEnable,
            int bufferSize, int fileSize) {

        String curPath = System.getProperty("JAppDefLogPath");

        if (StringHelper.isEmpty(curPath)) {
            curPath = IOHelper.getCurrentPath();
        }

        return newLogger(logName, curPath, logFileFormat, logLevel, debugEnable, bufferSize, fileSize);
    }

    private static ISystemLogger newLogger(String logName, String logPath, String logFileFormat, String logLevel,
            Boolean debugEnable, int bufferSize, int fileSize) {

        ISystemLogger mainLog = new SystemLogger(logName, logPath, logFileFormat, bufferSize, fileSize, 20, false,
                new DefaultLogFormatter());

        /**
         * SetProperty:JAppLogs
         */
        setJAppLogs(logName, logPath, logFileFormat);

        // set log level
        if (null == logLevel) {
            logLevel = "INFO";
        }
        mainLog.setLevel(LogLevel.valueOf(logLevel));

        // set debug enable
        if (null == debugEnable) {
            debugEnable = false;
        }
        mainLog.enableDebug(debugEnable);
        return mainLog;
    }

    /**
     * MSCP程序无需profiling，通过这个模式即可知道有哪些日志
     * 
     * 也可用此控制哪些日志需要归集
     * 
     * @param logName
     * @param logPath
     * @param logFileFormat
     */
    private static void setJAppLogs(String logName, String logPath, String logFileFormat) {

        /**
         * 检查是否启用归集
         */
        String isEnableJAppLog = System.getProperty("JAppLogsEnable");

        if (isEnableJAppLog == null || "false".equalsIgnoreCase(isEnableJAppLog)) {
            return;
        }

        /**
         * 检查是否符合文件名的正则表达式
         */
        String pattern = System.getProperty("JAppLogsIncludePattern");

        if (pattern != null && !"".equalsIgnoreCase(pattern)) {

            Pattern regxPattern = Pattern.compile(pattern);

            Matcher matcher = regxPattern.matcher(logFileFormat);

            if (matcher.matches() == false) {
                return;
            }
        }

        String folderPath = logPath + "/logs/";

        String fileName = logFileFormat.replace("%g.%u", "0.0");

        StringBuffer logPatten = new StringBuffer();
        logPatten.append(folderPath).append(fileName);

        if (!logName.equals("LogDataLog")) {
            logSet.add(logPatten.toString());
        }

        StringBuffer logsets = new StringBuffer();
        for (String log : logSet) {
            logsets.append(log).append(";");
        }

        System.setProperty("JAppLogs", logsets.toString());
    }

    private static ISystemLogger newLogger(String logName, String rootpath, String logFileFormat, String logLevel,
            Boolean debugEnable, int bufferSize, int fileSize, int fileCountLimit) {

        ISystemLogger mainLog = new SystemLogger(logName, rootpath, logFileFormat, bufferSize, fileSize, fileCountLimit,
                false, new SimpleLogFormatter());

        // set log level
        if (null == logLevel) {
            logLevel = "INFO";
        }
        mainLog.setLevel(LogLevel.valueOf(logLevel));

        // set debug enable
        if (null == debugEnable) {
            debugEnable = false;
        }
        mainLog.enableDebug(debugEnable);
        return mainLog;
    }

    private IPLogger log = null;
    private String filePattern = "agentlog.%g.%u.log";
    private int fileSizeLimit = 0;
    private int fileCountLimit = 0;
    private int logBufferSize = 100;
    private boolean shouldAppend = false;
    private String logRoot;
    private boolean enableTrace = true;
    private boolean enableDebug = false;

    private SystemLogger(String name, String rootpath, String logFilePattern, int logBufferSize, int fileSizeLimit,
            int fileCountLimit, boolean append, Formatter format) {

        this.filePattern = logFilePattern;
        this.fileSizeLimit = fileSizeLimit;
        this.fileCountLimit = fileCountLimit;
        this.logBufferSize = logBufferSize;
        this.shouldAppend = append;

        log = new PLogger(name);
        log.enableConsoleOut(true);
        log.setLogLevel(LogLevel.INFO);

        this.logRoot = rootpath + "/logs";

        if (!IOHelper.exists(logRoot)) {
            IOHelper.createFolder(logRoot);
        }

        log.enableFileOut(this.logRoot + "/" + this.filePattern, true, this.logBufferSize, this.fileSizeLimit,
                this.fileCountLimit, this.shouldAppend, format);
    }

    private String getDomainInfo(Object domain) {

        if (Class.class.isAssignableFrom(domain.getClass())) {
            return ((Class<?>) domain).getName();
        }
        else if (String.class.isAssignableFrom(domain.getClass())) {
            return (String) domain;
        }

        return domain.getClass().getName();
    }

    private String getCallerMethod(int level) {

        int l = 3;

        if (level > 0)
            l = level;

        StackTraceElement[] temp = Thread.currentThread().getStackTrace();
        StackTraceElement a = temp[l];
        return a.getMethodName() + "(" + a.getLineNumber() + ")";
    }

    @Override
    public void info(Object domain, String info, Object... objects) {

        String msgKey = getMsgKey(objects);
        log.info(msgKey + getDomainInfo(domain) + "." + getCallerMethod(-1) + " " + info, objects);
    }

    @Override
    public void warn(Object domain, String info, Object... objects) {

        String msgKey = getMsgKey(objects);
        log.warn(msgKey + getDomainInfo(domain) + "." + getCallerMethod(-1) + " " + info, objects);
    }

    @Override
    public void err(Object domain, String info, Object... objects) {

        String msgKey = getMsgKey(objects);
        String exception = getException(objects);
        log.err(msgKey + getDomainInfo(domain) + "." + getCallerMethod(-1) + "  " + info + exception, objects);
    }

    @Override
    public void debug(Object domain, String info, Object... objects) {

        if (!this.isDebugEnable())
            return;

        String msgKey = getMsgKey(objects);
        String exception = getException(objects);
        log.debug(msgKey + getDomainInfo(domain) + "." + getCallerMethod(-1) + "    " + info + exception, objects);
    }

    @Override
    public void fine(Object domain, String info, Object... objects) {

        String msgKey = getMsgKey(objects);
        log.fine(msgKey + getDomainInfo(domain) + "." + getCallerMethod(-1) + " " + info, objects);
    }

    @Override
    public void finer(Object domain, String info, Object... objects) {

        String msgKey = getMsgKey(objects);
        log.finer(msgKey + getDomainInfo(domain) + "." + getCallerMethod(-1) + "    " + info, objects);
    }

    @Override
    public void setLevel(LogLevel level) {

        log.setLogLevel(level);
    }

    @Override
    public void enableTrace(boolean check) {

        enableTrace = check;
    }

    @Override
    public boolean isTraceEnable() {

        return enableTrace;
    }

    @Override
    public void enableDebug(boolean check) {

        enableDebug = check;
    }

    @Override
    public boolean isDebugEnable() {

        return enableDebug;
    }

    @Override
    public void info(Object domain, String info, int level, Object... objects) {

        String msgKey = getMsgKey(objects);
        log.info(msgKey + getDomainInfo(domain) + "." + getCallerMethod(level) + "  " + info, objects);
    }

    @Override
    public void trace(Object domain, String info, Object... objects) {

        log.info(info, objects);

    }

    private String getMsgKey(Object... objects) {

        if (objects != null && objects.length > 0 && objects[0] instanceof String) {
            return (String) objects[0];
        }

        return "";
    }

    private String getException(Object... objects) {

        if (objects != null && objects.length > 0) {
            for (Object o : objects) {
                if (o instanceof Throwable) {
                    Throwable e = (Throwable) o;
                    return DataConvertHelper.exception2String(e);
                }
            }
        }

        return "";
    }

    @Override
    public void warn(Object domain, String info, int level, Object... objects) {

        String msgKey = getMsgKey(objects);
        log.warn(msgKey + getDomainInfo(domain) + "." + getCallerMethod(level) + "  " + info, objects);
    }

    @Override
    public void err(Object domain, String info, int level, Object... objects) {

        String msgKey = getMsgKey(objects);
        String exception = getException(objects);
        log.err(msgKey + getDomainInfo(domain) + "." + getCallerMethod(level) + "   " + info + exception, objects);
    }

    @Override
    public void debug(Object domain, String info, int level, Object... objects) {

        if (!this.isDebugEnable())
            return;

        String msgKey = getMsgKey(objects);
        String exception = getException(objects);
        log.debug(msgKey + getDomainInfo(domain) + "." + getCallerMethod(level) + " " + info + exception, objects);
    }

    @Override
    public void fine(Object domain, String info, int level, Object... objects) {

        String msgKey = getMsgKey(objects);
        log.fine(msgKey + getDomainInfo(domain) + "." + getCallerMethod(level) + "  " + info, objects);
    }

    @Override
    public void finer(Object domain, String info, int level, Object... objects) {

        String msgKey = getMsgKey(objects);
        log.finer(msgKey + getDomainInfo(domain) + "." + getCallerMethod(level) + " " + info, objects);
    }

    public static final <K, V> String toLogString(Map<K, V> m) {

        if (m == null || m.isEmpty())
            return "";
        StringBuilder sb = new StringBuilder();

        Iterator<Entry<K, V>> itr = m.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<K, V> entry = itr.next();
            sb.append(entry.getKey().toString() + "=" + entry.getValue().toString() + ",");
        }

        return sb.toString();
    }

    public static final <T> String toLogString(List<T> ls) {

        if (ls == null || ls.isEmpty())
            return "";
        StringBuilder sb = new StringBuilder();
        for (T t : ls) {
            sb.append(t.toString() + ", ");
        }
        return sb.toString();
    }
}
