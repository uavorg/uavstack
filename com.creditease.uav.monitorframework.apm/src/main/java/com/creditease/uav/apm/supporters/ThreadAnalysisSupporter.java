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

package com.creditease.uav.apm.supporters;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.DateTimeHelper;
import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.JVMToolHelper;
import com.creditease.agent.helpers.NetworkHelper;
import com.creditease.agent.helpers.RuntimeHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.monitor.UAVMonitor;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.uav.common.Supporter;

public class ThreadAnalysisSupporter extends Supporter {

    private UAVMonitor monitor = new UAVMonitor(logger, 60000);

    // 执行时间map,key为进程号，value为执行时间；用于判断在限定时间段内不需要发起多次请求
    private volatile Map<String, Long> timeIntervalMap = new ConcurrentHashMap<String, Long>();

    private final String SYMBOL = "_";
    private int timeInterval = 60000;

    @Override
    public void start() {

        // 从配置文件中获取限定时间段，没有使用默认值
        timeInterval = DataConvertHelper.toInt(System.getProperty("com.creditease.uav.threadanalysis.timeinterval"),
                60000);

    }

    @Override
    public void stop() {

        super.stop();
    }

    @Override
    public Object run(String methodName, Object... params) {

        if (!"captureJavaThreadAnalysis".equals(methodName) || null == params || params.length < 4) {

            return "ERR:METHOD NOT SUPPORT";
        }
        return captureJavaThreadAnalysis(params);
    }

    private Object captureJavaThreadAnalysis(Object... params) {

        /**
         * TODO： window系统不支持线程分析功能，以后再做适配
         *
         */
        if (JVMToolHelper.isWindows() == true) {
            return "ERR:NOT SUPPORT WINDOWNS";
        }

        long stTime = System.currentTimeMillis();

        // 获取java_home路径
        String javahome = System.getProperty("java.home");
        // 路径是否由“/”结束，没有则添加
        if (!javahome.endsWith("/")) {
            javahome = javahome + "/";
        }
        // 获取java_home bin的路径
        final String jdk_binpath = javahome + "../bin";
        // 如果没有获取到java_home，返回结果
        if (!IOHelper.exists(jdk_binpath)) {
            return "ERR:NO JDK";
        }

        // 进程号
        String pid = (String) params[0];
        // 如果没有传入线程分析的进程号，取当前MOF所在程序的PID
        if (StringHelper.isEmpty(pid)) {
            pid = JVMToolHelper.getCurrentProcId();
            // return "ERR:NO PID";
        }
        // 执行时间(从请求端获取)，传递long型字符串，然后转化成long型
        Long exectime = DataConvertHelper.toLong(params[1], -1);
        // 如果时间值不对，则取MOF所在系统的当前时间
        if (-1 == exectime) {
            exectime = System.currentTimeMillis();
            // return "ERR:EXECTIME ERROR";
        }

        // IP地址如果没传，则获取MOF所在系统的IP
        String ip = (String) params[2];
        if (StringHelper.isEmpty(ip)) {
            ip = NetworkHelper.getLocalIP();
        }
        // 端口号
        String port = UAVServer.instance().getServerInfo(CaptureConstants.INFO_APPSERVER_LISTEN_PORT) + "";

        // 文件路径
        String fileBase = (String) params[3];
        if (StringHelper.isEmpty(fileBase)) {
            return "ERR:NO STORE FILE BASE";
        }

        // 并发控制
        if (!controlConcurrency(pid, exectime)) {
            return "ERR:IS RUNNING";
        }

        // 生成线程分析文件即将开始运行，在日志中记录开始运行记录
        if (logger.isDebugable()) {
            logger.debug("RUN Java Thread Analysis START: pid=" + pid, null);
        }

        if (!checkDirPermission(fileBase)) {
            return "ERR:FILE PERMISSION DENIED";
        }

        String dateTime = DateTimeHelper.toFormat("yyyy-MM-dd_HH-mm-ss.SSS", exectime);
        // 规定线程分析结果文件名
        String name = ip + SYMBOL + port + SYMBOL + dateTime + ".log";
        String file = fileBase + "/" + name;
        
        // 生成线程分析结果文件需要执行的命令
        String cmd = " top -Hp " + pid + " bn 1 > " + file + " && echo '=====' >> " + file + " && " + jdk_binpath
                + "/jstack " + pid + " >>  " + file;

        try {
            // 执行命令
            RuntimeHelper.exec(10000, "/bin/sh", "-c", cmd);
        }
        catch (Exception e) {
            logger.warn("RUN Java Thread Analysis FAIL: ", e);
        }
        // 执行命令后如果文件不存在，在日志中记录，返回
        if (!IOHelper.exists(file)) {
            logger.warn("RUN Java Thread Analysis FAIL: file[" + file + "] not exist", null);
            return "ERR:FILE NOT EXIST";
        }

        /**
         * FIX: 在某些文件系统中，会复用已删除的inode，归集依赖inode。所以先建文件后删除其它的
         */
        if (!deleteFileByFuzzyName(fileBase, SYMBOL + port + SYMBOL, name)) {
            return "ERR:FILE PERMISSION DENIED";
        }

        monitor.logPerf(stTime, "THREAD_ANALYSIS");
        return file;

    }

    /**
     * controlConcurrency
     * 
     * @param pid
     * @param exectime
     * @return
     */
    private boolean controlConcurrency(String pid, Long exectime) {

        // initial
        if (!timeIntervalMap.containsKey(pid)) {
            synchronized (timeIntervalMap) {
                if (!timeIntervalMap.containsKey(pid)) {
                    // 在exectimeMap记录进程号和执行时间
                    timeIntervalMap.put(pid, exectime);
                    return true;
                }
            }
        }
        // only one can entrance
        if ((exectime - timeIntervalMap.get(pid)) > timeInterval) {
            synchronized (timeIntervalMap) {
                if ((exectime - timeIntervalMap.get(pid)) > timeInterval) {
                    // 在exectimeMap记录进程号和执行时间
                    timeIntervalMap.put(pid, exectime);
                    return true;
                }
            }
        }
        // thread analysis is running, abandon
        return false;
    }

    /**
     * deleteFileByFuzzyName
     * 
     * @param parentPath
     * @param fuzzyName
     * @return
     */
    private boolean deleteFileByFuzzyName(String parentPath, String fuzzyName, String filter) {

        try {

            File dir = new File(parentPath);
            if (dir.exists()) {
                File[] files = dir.listFiles();
                for (File file : files) {
                    if (file.isFile() && file.getName().contains(fuzzyName) && !file.getName().equals(filter)) {
                        file.delete();
                    }
                }
            }
        }
        catch (Exception e) {
            logger.warn("In " + parentPath + " can not delete fuzzy file " + fuzzyName, e);
            return false;
        }
        return true;
    }

    private boolean checkDirPermission(String dir) {

        try {
            File f = new File(dir);
            return f.canWrite();
        }
        catch (Exception e) {
            return false;
        }
    }
}
