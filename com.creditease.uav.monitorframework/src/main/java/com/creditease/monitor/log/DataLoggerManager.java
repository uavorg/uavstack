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

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.IOHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.uav.common.BaseComponent;

/**
 * 
 * DataLoggerManager description: 管理用于输出各种数据日志的logger
 *
 */
public class DataLoggerManager extends BaseComponent {

    private Map<String, DataLogger> logMap = new ConcurrentHashMap<String, DataLogger>();

    private String configPrefix;

    private String logTypeName;

    /**
     * 
     * @param logTypeName
     *            日志的业务类型标识
     * @param configPrefix
     *            uav.properties里面该DataLoggerManager所有配置项的前缀
     */
    public DataLoggerManager(String logTypeName, String configPrefix) {
        this.configPrefix = configPrefix;
        this.logTypeName = logTypeName;
    }

    /**
     * destroy
     */
    public void destroy() {

        for (DataLogger dl : this.logMap.values()) {
            dl.destroy();
        }

        this.logMap.clear();
    }

    public void clearLogs() {

        String port = UAVServer.instance().getServerInfo(CaptureConstants.INFO_APPSERVER_LISTEN_PORT) + "";

        String logUUIDPattern = "_" + port + "_" + this.logTypeName;

        String rootPath = getRootPath();

        List<File> files = IOHelper.getFiles(rootPath);

        for (File f : files) {
            if (f.getName().indexOf(logUUIDPattern) > -1) {
                f.delete();
            }
        }
    }

    /**
     * get a data logger
     * 
     * @param appid
     *            应用唯一标识
     * @return
     */
    public DataLogger getDataLogger(String appid) {

        if (logMap.containsKey(appid)) {
            return logMap.get(appid);
        }

        DataLogger dl = null;

        synchronized (logMap) {

            if (logMap.containsKey(appid)) {
                return logMap.get(appid);
            }

            String rootPath = getRootPath();

            int buffer = DataConvertHelper.toInt(System.getProperty(this.configPrefix + ".logbuffer"), 100);
            int fileSize = DataConvertHelper.toInt(System.getProperty(this.configPrefix + ".logsize"),
                    1024 * 1024 * 100);
            int fileCount = DataConvertHelper.toInt(System.getProperty(this.configPrefix + ".logcount"), 3);

            String port = UAVServer.instance().getServerInfo(CaptureConstants.INFO_APPSERVER_LISTEN_PORT) + "";

            /**
             * 保证log在同一个机器上的唯一性
             */
            String logUUID = appid + "_" + port + "_" + this.logTypeName;

            dl = new DataLogger(logUUID, rootPath, logUUID + ".%g.log", buffer, fileSize, fileCount);

            this.logMap.put(appid, dl);
        }

        return dl;
    }

    private String getRootPath() {

        String rootPath = System.getProperty(this.configPrefix + ".logroot");

        if (rootPath == null) {
            rootPath = "/data/uav/" + this.logTypeName;
        }
        return rootPath;
    }
}
