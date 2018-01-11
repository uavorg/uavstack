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

package com.creditease.uav.hook.jdbc.pools.c3p0;

import java.util.Map;
import java.util.Set;

import com.creditease.agent.helpers.ReflectionHelper;
import com.creditease.monitor.captureframework.spi.MonitorElement;
import com.creditease.monitor.captureframework.spi.MonitorElementInstance;
import com.creditease.uav.hook.jdbc.pools.AbsDBPoolHookProxy;
import com.mchange.v2.c3p0.C3P0Registry;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * 
 * C3P0HookProxy description: 支持c3p0
 *
 */
public class C3P0HookProxy extends AbsDBPoolHookProxy {

    private static final String MTRX_PREFIX = "EXT_c3p0_";

    @SuppressWarnings("rawtypes")
    public C3P0HookProxy(String id, Map config) {
        super(id, config);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void collectDBPoolMetrics(MonitorElement clientElem) {

        Set dsList = C3P0Registry.getPooledDataSources();

        if (dsList == null || dsList.size() == 0) {
            return;
        }

        /**
         * Step 2: 匹配对应的jdbc url
         */
        for (Object ds : dsList) {

            if (!ComboPooledDataSource.class.isAssignableFrom(ds.getClass())) {
                continue;
            }

            ComboPooledDataSource pds = (ComboPooledDataSource) ds;

            String jdbcURL = pds.getJdbcUrl();

            /**
             * 匹配客户端应用
             */
            MonitorElementInstance inst = this.matchElemInstance(clientElem, jdbcURL);

            if (inst == null) {
                continue;
            }

            collectDataSourceStat(inst, pds);
        }
    }

    /**
     * 收集DataSource性能指标
     * 
     * @param inst
     * @param pds
     */
    private void collectDataSourceStat(MonitorElementInstance inst, ComboPooledDataSource pds) {

        String[] collectMtrx = new String[] { "NumConnections", "NumBusyConnections", "NumIdleConnections",
                "NumUnclosedOrphanedConnections", "ThreadPoolSize", "ThreadPoolNumActiveThreads",
                "ThreadPoolNumIdleThreads", "ThreadPoolNumTasksPending", "StatementCacheNumStatementsAllUsers",
                "InitialPoolSize", "MaxPoolSize", "MaxStatements", "MaxStatementsPerConnection" };

        String prefix = "get";

        for (int i = 0; i < collectMtrx.length; i++) {
            inst.setValue(MTRX_PREFIX + collectMtrx[i], ReflectionHelper.invoke(ComboPooledDataSource.class.getName(), pds,
                    prefix + collectMtrx[i], null, null));
        }
    }

}
