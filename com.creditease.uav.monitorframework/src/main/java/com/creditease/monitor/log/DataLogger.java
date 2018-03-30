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

import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.log.PLogger;
import com.creditease.agent.log.PLogger.SimpleLogFormatter;
import com.creditease.agent.log.api.IPLogger;
import com.creditease.agent.log.api.IPLogger.LogLevel;

/**
 * 
 * DataLogger description: common log out data
 *
 */
public class DataLogger {

    private IPLogger log = null;
    private String filePattern = "";
    private int fileSizeLimit = 1024 * 1024 * 100;
    private int fileCountLimit = 10;
    private int logBufferSize = 200;
    private String logRoot = ".";

    /**
     * 
     * @param name
     *            查找或生成对应名字的logger
     * @param rootpath
     *            日志产生的根目录
     * @param logFilePattern
     *            日志文件名称规则
     * @param logBufferSize
     *            日志缓冲行数
     * @param fileSizeLimit
     *            每个日志文件大小（单位byte）
     * @param fileCountLimit
     *            日志文件个数
     */
    public DataLogger(String name, String rootpath, String logFilePattern, int logBufferSize, int fileSizeLimit,
            int fileCountLimit) {
        this.filePattern = logFilePattern;
        this.fileSizeLimit = fileSizeLimit;
        this.fileCountLimit = fileCountLimit;
        this.logBufferSize = logBufferSize;

        log = new PLogger(name);
        log.enableConsoleOut(false);
        log.setLogLevel(LogLevel.INFO);

        this.logRoot = rootpath;

        if (!IOHelper.exists(logRoot)) {
            isInitSus = false;
            return;
        }

        isInitSus = log.enableFileOut(this.logRoot + "/" + this.filePattern, true, this.logBufferSize,
                this.fileSizeLimit, this.fileCountLimit, true, new SimpleLogFormatter());
    }

    public void logData(String info) {
		
        if (isInitSus == false) {
           return;
        }
		
        this.log.info(info);
    }

    public void destroy() {

        this.log.destroy();
    }
}
