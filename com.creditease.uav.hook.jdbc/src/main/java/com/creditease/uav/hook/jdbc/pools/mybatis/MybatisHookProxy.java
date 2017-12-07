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

package com.creditease.uav.hook.jdbc.pools.mybatis;

import java.util.Map;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.pooled.PoolState;

import com.creditease.agent.helpers.ReflectHelper;
import com.creditease.monitor.appfra.hook.spi.HookConstants;
import com.creditease.monitor.appfra.hook.spi.HookContext;
import com.creditease.monitor.captureframework.spi.MonitorElement;
import com.creditease.monitor.captureframework.spi.MonitorElementInstance;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;
import com.creditease.uav.hook.jdbc.pools.AbsDBPoolHookProxy;
import com.creditease.uav.hook.jdbc.pools.mybatis.interceptors.MybatisIT;
import com.creditease.uav.monitorframework.dproxy.DynamicProxyInstaller;
import com.creditease.uav.monitorframework.dproxy.DynamicProxyProcessor;
import com.creditease.uav.util.MonitorServerUtil;

import javassist.CtMethod;

public class MybatisHookProxy extends AbsDBPoolHookProxy {

    private static final String MTRX_PREFIX = "EXT_mybatis_";

    protected DynamicProxyInstaller dpInstall;

    private ClassLoader webapploaderForMybatis = null;

    @SuppressWarnings("rawtypes")
    public MybatisHookProxy(String id, Map config) {
        super(id, config);

        dpInstall = new DynamicProxyInstaller();
    }

    @Override
    public void start(HookContext context, ClassLoader webapploader) {

        super.start(context, webapploader);
        webapploaderForMybatis = webapploader;
        Event event = context.get(Event.class);
        switch (event) {
            case SPRING_BEAN_REGIST:
            case WEBCONTAINER_RESOURCE_INIT:
            case WEBCONTAINER_INIT:
                InsertInterceptToClients(context, webapploader);
                break;
            case AFTER_SERVET_INIT:
                break;
            case BEFORE_SERVLET_DESTROY:
                break;
            case GLOBAL_FILTER_REQUEST:
                break;
            case GLOBAL_FILTER_RESPONSE:
                break;
            case WEBCONTAINER_RESOURCE_CREATE:
                break;
            case WEBCONTAINER_STARTED:
                break;
            case WEBCONTAINER_STOPPED:
                break;
            default:
                break;
        }
    }

    @Override
    protected void collectDBPoolMetrics(MonitorElement clientElem) {

        if (this.datasources.size() == 0) {
            return;
        }

        for (DataSource cp : this.datasources) {
            String jdbcURL = (String) ReflectHelper.invoke(cp.getClass().getName(), cp, "getUrl", null, null,
                    cp.getClass().getClassLoader());

            MonitorElementInstance inst = this.matchElemInstance(clientElem, jdbcURL);

            if (inst == null) {
                continue;
            }

            collectDataSourceStat(inst, cp, webapploaderForMybatis);
        }
    }

    private void InsertInterceptToClients(HookContext context, ClassLoader webapploader) {

        if (isHookEventDone("InsertInterceptToClients")) {
            return;
        }

        InterceptContext ic = (InterceptContext) context.get(HookConstants.INTERCEPTCONTEXT);
        String contextPath = (String) ic.get(InterceptConstants.CONTEXTPATH);
        String basePath = (String) ic.get(InterceptConstants.BASEPATH);
        appid = MonitorServerUtil.getApplicationId(contextPath, basePath);

        dpInstall.setTargetClassLoader(webapploader);

        dpInstall.installProxy("org.apache.ibatis.datasource.pooled.PooledDataSource",
                new String[] { "com.creditease.uav.hook.jdbc.pools.mybatis.interceptors" },
                new DynamicProxyProcessor() {

                    @Override
                    public void process(CtMethod m) throws Exception {

                        if ("setUrl".equals(m.getName())) {
                            dpInstall.defineLocalVal(m, "mObj", MybatisIT.class);
                            m.insertBefore("{mObj=new MybatisIT(\"" + id + "\",this);}");
                        }

                    }

                }, false);
        // release loader
        dpInstall.releaseTargetClassLoader();
    }

    private void collectDataSourceStat(MonitorElementInstance inst, DataSource pds, ClassLoader webapploader) {

        String[] collectMtrx = new String[] { "PoolMaximumActiveConnections", "PoolMaximumIdleConnections",
                "PoolMaximumCheckoutTime", "PoolTimeToWait", "ActiveConnections", "IdleConnections", "RequestCount",
                "AverageRequestTime", "AverageCheckoutTime", "ClaimedOverdue", "AverageOverdueCheckoutTime",
                "HadToWait", "AverageWaitTime", "BadConnectionCount" };
        String prefix = "get";
        String className = pds.getClass().getName();
        int i;
        for (i = 0; i < 4; i++) {
            inst.setValue(MTRX_PREFIX + collectMtrx[i],
                    ReflectHelper.invoke(className, pds, prefix + collectMtrx[i], null, null, webapploader));
        }

        Object poolState = ReflectHelper.invoke(className, pds, "getPoolState", null, null, webapploader);

        for (; i < collectMtrx.length; i++) {

            inst.setValue(MTRX_PREFIX + collectMtrx[i], ReflectHelper.invoke(PoolState.class.getName(), poolState,
                    prefix + collectMtrx[i], null, null, webapploader));
        }

    }
}
