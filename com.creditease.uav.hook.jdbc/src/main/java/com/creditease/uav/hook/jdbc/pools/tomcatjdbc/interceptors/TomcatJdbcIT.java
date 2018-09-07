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

package com.creditease.uav.hook.jdbc.pools.tomcatjdbc.interceptors;

import javax.sql.DataSource;

import com.creditease.monitor.appfra.hook.spi.HookFactory;
import com.creditease.monitor.interceptframework.InterceptSupport;
import com.creditease.uav.appserver.listeners.AppFrkHookFactoryListener;
import com.creditease.uav.hook.jdbc.pools.AbsDBPoolHookProxy;

public class TomcatJdbcIT {

    public TomcatJdbcIT(String contextPath, Object ds) {

        if (!DataSource.class.isAssignableFrom(ds.getClass())) {
            return;
        }

        AppFrkHookFactoryListener listener = (AppFrkHookFactoryListener) InterceptSupport.instance()
                .getEventListener(AppFrkHookFactoryListener.class);
        HookFactory hf = listener.getHookFactory();
        // 依赖HookFactory.getHookProxyKey的实现
        String proxyId = "com.creditease.uav.hook.jdbc.pools.tomcatjdbc.TomcatJdbcHookProxy@" + contextPath;
        AbsDBPoolHookProxy hookProxy = (AbsDBPoolHookProxy) hf.getHook(proxyId);

        hookProxy.registerDataSource((DataSource) ds);

    }
}
