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

package com.creditease.uav.profiling.handlers.log;

import java.util.HashMap;
import java.util.Map;

public class LogProfileInfo {

    public enum LogType {
        Log4j("log4j");

        private String typeName;

        private LogType(String typeName) {
            this.typeName = typeName;
        }

        @Override
        public String toString() {

            return typeName;
        }
    }

    public final static String BUFFRT_IO = "buffer.io";

    public final static String BUFFER_SIZE = "bufferr.size";

    public final static String PATTERN = "pattern";

    public final static String ENGINE = "engine";

    private LogType logType;

    private String appId;

    private String filePath;

    private Map<String, String> attributes = new HashMap<String, String>();

    public String getAppId() {

        return appId;
    }

    public void setAppId(String appId) {

        this.appId = appId;
    }

    public LogType getLogType() {

        return logType;
    }

    public void setLogType(LogType logType) {

        this.logType = logType;
    }

    public String getFilePath() {

        return filePath;
    }

    public void setFilePath(String file) {

        this.filePath = file;
    }

    public Map<String, String> getAttributes() {

        return attributes;
    }

    public void setAttributes(Map<String, String> attribute) {

        this.attributes = attribute;
    }

}
