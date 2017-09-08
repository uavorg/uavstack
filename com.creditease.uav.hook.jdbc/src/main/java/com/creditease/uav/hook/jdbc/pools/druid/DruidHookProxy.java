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

package com.creditease.uav.hook.jdbc.pools.druid;

import java.util.Map;
import java.util.Set;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.stat.DruidDataSourceStatManager;
import com.creditease.monitor.captureframework.spi.MonitorElement;
import com.creditease.monitor.captureframework.spi.MonitorElementInstance;
import com.creditease.uav.hook.jdbc.pools.AbsDBPoolHookProxy;

/**
 * 
 * DruidHookProxy description: 支持Druid
 *
 */
public class DruidHookProxy extends AbsDBPoolHookProxy {

    private static final String MTRX_PREFIX = "EXT_druid_";

    @SuppressWarnings("rawtypes")
    public DruidHookProxy(String id, Map config) {
        super(id, config);
    }

    @Override
    protected void collectDBPoolMetrics(MonitorElement clientElem) {

        Set<DruidDataSource> dsList = DruidDataSourceStatManager.getDruidDataSourceInstances();

        if (dsList == null || dsList.size() == 0) {
            return;
        }

        for (DruidDataSource ds : dsList) {

            String jdbcURL = ds.getRawJdbcUrl();

            /**
             * 匹配客户端应用
             */
            MonitorElementInstance inst = this.matchElemInstance(clientElem, jdbcURL);

            if (inst == null) {
                continue;
            }

            collectDataSourceStat(inst, ds);
        }

    }

    /**
     * collectDataSourceStat
     * 
     * @param inst
     * @param ds
     */
    private void collectDataSourceStat(MonitorElementInstance inst, DruidDataSource ds) {

        Map<String, Object> statData = ds.getStatData();

        for (String key : statData.keySet()) {

            Object value = statData.get(key);

            if (!(value instanceof Number)) {
                continue;
            }

            inst.setValue(MTRX_PREFIX + key, value);
        }
    }

}
