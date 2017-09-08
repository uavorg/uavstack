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

package com.creditease.uav.hook.jdbc.pools.proxool;

import java.util.Map;

import org.logicalcobwebs.proxool.ConnectionPoolManagerDelegate;

import com.creditease.monitor.captureframework.spi.MonitorElement;
import com.creditease.uav.hook.jdbc.pools.AbsDBPoolHookProxy;

public class ProxoolHookProxy extends AbsDBPoolHookProxy {

    public static final String MTRX_PREFIX = "EXT_proxool_";

    private ConnectionPoolManagerDelegate cpmd = new ConnectionPoolManagerDelegate();

    @SuppressWarnings("rawtypes")
    public ProxoolHookProxy(String id, Map config) {
        super(id, config);
    }

    @Override
    protected void collectDBPoolMetrics(MonitorElement clientElem) {

        cpmd.collectDataPools(clientElem, this);
    }

}
