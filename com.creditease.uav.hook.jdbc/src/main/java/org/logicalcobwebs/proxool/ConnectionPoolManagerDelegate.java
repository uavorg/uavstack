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

package org.logicalcobwebs.proxool;

import com.creditease.agent.helpers.ReflectionHelper;
import com.creditease.monitor.captureframework.spi.MonitorElement;
import com.creditease.monitor.captureframework.spi.MonitorElementInstance;
import com.creditease.uav.hook.jdbc.pools.AbsDBPoolHookProxy;
import com.creditease.uav.hook.jdbc.pools.proxool.ProxoolHookProxy;

/**
 * 
 * ConnectionPoolManagerDelegate description: 访问default 类需要同包
 *
 */
public class ConnectionPoolManagerDelegate {

    /**
     * collectDataPools
     * 
     * @param clientElem
     * @param proxy
     */
    public void collectDataPools(MonitorElement clientElem, AbsDBPoolHookProxy proxy) {

        ConnectionPool[] cpList = ConnectionPoolManager.getInstance().getConnectionPools();

        if (cpList == null || cpList.length == 0) {
            return;
        }

        for (ConnectionPool cp : cpList) {

            String jdbcURL = cp.getDefinition().getUrl();

            /**
             * 匹配客户端应用
             */
            MonitorElementInstance inst = proxy.matchElemInstance(clientElem, jdbcURL);

            if (inst == null) {
                continue;
            }

            collectDataSourceStat(inst, cp);
        }
    }

    /**
     * collectDataSourceStat
     * 
     * @param inst
     * @param pds
     */
    private void collectDataSourceStat(MonitorElementInstance inst, ConnectionPool pds) {

        String[] collectMtrx = new String[] { "ConnectionsServedCount", "ConnectionsRefusedCount",
                "ActiveConnectionCount", "AvailableConnectionCount", "OfflineConnectionCount", "ConnectionCount" };

        String prefix = "get";

        for (int i = 0; i < collectMtrx.length; i++) {
            inst.setValue(ProxoolHookProxy.MTRX_PREFIX + collectMtrx[i],
                    ReflectionHelper.invoke(ConnectionPool.class.getName(), pds, prefix + collectMtrx[i], null, null));
        }
    }
}
