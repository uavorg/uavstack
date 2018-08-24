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

package com.creditease.uav.hook.jdbc.pools.tomcatjdbc;

import java.util.Map;

import javax.sql.DataSource;

import com.creditease.agent.helpers.ReflectionHelper;
import com.creditease.monitor.captureframework.spi.MonitorElement;
import com.creditease.monitor.captureframework.spi.MonitorElementInstance;
import com.creditease.uav.hook.jdbc.pools.AbsDBPoolHookProxy;

/**
 * 
 * TomcatJdbcHookProxy description: 支持TomcatJdbc 7.0.19-9.0.10
 *
 */
public class TomcatJdbcHookProxy extends AbsDBPoolHookProxy {

    private static final String MTRX_PREFIX = "EXT_tomcatjdbc_";

    @SuppressWarnings("rawtypes")
    public TomcatJdbcHookProxy(String id, Map config) {

        super(id, config);
    }

    @Override
    protected void collectDBPoolMetrics(MonitorElement clientElem) {

        if (this.datasources.size() == 0) {
            return;
        }

        for (DataSource ds : this.datasources) {

            String jdbcURL = (String) ReflectionHelper.invoke(ds.getClass().getName(), ds, "getUrl", null, null,
                    ds.getClass().getClassLoader());

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
    private void collectDataSourceStat(MonitorElementInstance inst, DataSource ds) {

        String[] collectMtrx = new String[] { "NumActive", "NumIdle", "MaxActive", "MaxIdle", "MinIdle", "MaxWait",
                "InitialSize", "NumTestsPerEvictionRun", "NumTestsPerEvictionRun", "TimeBetweenEvictionRunsMillis",
                "RemoveAbandonedTimeout", "MinEvictableIdleTimeMillis" };

        String prefix = "get";
        String className = ds.getClass().getName();

        for (int i = 0; i < collectMtrx.length; i++) {

            Object val = ReflectionHelper.invoke(className, ds, prefix + collectMtrx[i], null, null,
                    this.getClass().getClassLoader());

            if (val == null) {
                continue;
            }

            inst.setValue(MTRX_PREFIX + collectMtrx[i], val);
        }
    }

}
