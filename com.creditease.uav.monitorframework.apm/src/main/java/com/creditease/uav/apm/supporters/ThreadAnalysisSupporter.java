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

    private static final String SUPPORTED_METHOD = "captureJavaThreadAnalysis";

    private static Object lock = new Object();
    private static final long FROZON_TIME_LIMIT = 1000L;
    private static volatile long lastInvokeTime = System.currentTimeMillis();

    private final String SYMBOL = "_";

    @Override
    public void start() {

    }

    @Override
    public void stop() {

        super.stop();
    }

    @Override
    public Object run(String methodName, Object... params) {

        long now = System.currentTimeMillis();

        /**
         * concurrency control
         */
        long lastTime = lastInvokeTime;
        lastInvokeTime = now;
        if (now + FROZON_TIME_LIMIT < lastTime) {
            return "ERR:BE RUNNING";
        }

        // valid arguments
        if (!SUPPORTED_METHOD.equals(methodName) || params == null || params.length < 4) {
            return "ERR:ILLEGAL ARGUMENT";
        }

        Object ret = captureJavaThreadAnalysis(params);

        lastInvokeTime = now;
        monitor.logPerf(now, "THREAD_ANALYSIS");
        return ret;
    }

    private Object captureJavaThreadAnalysis(Object... params) {

        // 进程号
        String pid = (String) params[0];
        if (StringHelper.isEmpty(pid)) {
            // 如果没有传入线程分析的进程号，取当前MOF所在程序的PID
            pid = JVMToolHelper.getCurrentProcId();
        }

        // 执行时间戳(从请求端获取)
        long execTime = DataConvertHelper.toLong(params[1], -1);
        if (execTime == -1) {
            execTime = System.currentTimeMillis();
        }

        // IP地址如果没传，则获取MOF所在系统的IP
        String ip = (String) params[2];
        if (StringHelper.isEmpty(ip)) {
            ip = NetworkHelper.getLocalIP();
        }

        // 文件路径
        String fileBase = (String) params[3];
        if (StringHelper.isEmpty(fileBase)) {
            return "ERR:NO STORE FILE BASE";
        }

        // 端口号
        String port = UAVServer.instance().getServerInfo(CaptureConstants.INFO_APPSERVER_LISTEN_PORT) + "";

        String jdkBinPath = getJdkBinPath();
        if (!IOHelper.exists(jdkBinPath)) {
            // 如果没有获取到java_home，返回结果
            return "ERR:NO JDK";
        }

        if (!checkDirPermission(fileBase)) {
            return "ERR:FILE PERMISSION DENIED";
        }

        if (JVMToolHelper.isWindows()) {
            /**
             * TODO： window系统不支持线程分析功能，以后再做适配
             */
            return "ERR:NOT SUPPORT WINDOWNS";
        }

        // 生成线程分析文件即将开始运行，在日志中记录开始运行记录
        if (logger.isDebugable()) {
            logger.debug("RUN Java Thread Analysis START: pid=" + pid, null);
        }

        String dateTime = DateTimeHelper.toFormat("yyyy-MM-dd_HH-mm-ss.SSS", execTime);
        // 规定线程分析结果文件名
        String name = ip + SYMBOL + port + SYMBOL + dateTime + ".log";
        String file = fileBase + "/" + name;

        // 生成线程分析结果文件需要执行的命令
        String cmd = " top -Hp " + pid + " bn 1 > " + file + " && echo '=====' >> " + file + " && " + jdkBinPath
                + "/jstack " + pid + " >>  " + file;

        try {
            synchronized (lock) {
                // 执行命令
                RuntimeHelper.exec(10000, "/bin/sh", "-c", cmd);
            }
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

        return file;

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

    private String getJdkBinPath() {

        // 获取java_home路径
        String javahome = System.getProperty("java.home");
        // 路径是否由“/”结束，没有则添加
        if (!javahome.endsWith("/")) {
            javahome = javahome + "/";
        }
        // 获取java_home bin的路径
        return javahome + "../bin";
    }
}
