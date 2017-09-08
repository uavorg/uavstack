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

package com.creditease.uav.common;

import java.util.HashMap;
import java.util.Map;

import com.creditease.agent.helpers.StringHelper;
import com.creditease.monitor.log.DataLogger;
import com.creditease.monitor.log.DataLoggerManager;

/**
 * Implements Supporter interface to start certain capability of MOF.
 * 
 * Method 1: config the Supporter Impl class into tomcatplus.properties's "com.creditease.monitor.supporters", then the
 * supporter is started as default supporter
 * 
 * Method 2: also can use http interface via UAVServerController (such as UAVServerJEEController with context path
 * /com.creditease.uav/server) to start or stop a supporter
 * 
 * 
 */
public abstract class Supporter extends BaseComponent {

    private Map<String, DataLoggerManager> dlmMap = new HashMap<String, DataLoggerManager>(1);

    /**
     * 创建DataLoggerManager
     * 
     * @param logTypeName
     * @param configPreix
     * @return
     */
    protected DataLoggerManager newDataLoggerManager(String logTypeName, String configPreix) {

        if (StringHelper.isEmpty(logTypeName) || StringHelper.isEmpty(configPreix)) {
            return null;
        }

        DataLoggerManager dlm = new DataLoggerManager(logTypeName, configPreix);

        this.dlmMap.put(logTypeName, dlm);

        return dlm;
    }

    /**
     * 获取DataLoggerManager
     * 
     * @param logTypeName
     * @param appid
     * @return
     */
    public DataLogger getDataLogger(String logTypeName, String appid) {

        if (StringHelper.isEmpty(logTypeName) || StringHelper.isEmpty(appid)) {
            return null;
        }

        DataLoggerManager dlm = this.dlmMap.get(logTypeName);

        if (dlm == null) {
            return null;
        }

        return dlm.getDataLogger(appid);
    }

    public abstract void start();

    public void stop() {

        for (DataLoggerManager dlm : this.dlmMap.values()) {
            dlm.destroy();
        }

        this.dlmMap.clear();
    }

    /**
     * NOTE: the generic RUN supporter method, better this is the only public method for this supporter to external such
     * as Hook
     * 
     * @param params
     * @return
     */
    public Object run(String methodName, Object... params) {

        // not implement

        return null;
    }
}
