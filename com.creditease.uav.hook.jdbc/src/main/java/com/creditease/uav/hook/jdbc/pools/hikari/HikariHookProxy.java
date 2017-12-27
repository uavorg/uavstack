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

package com.creditease.uav.hook.jdbc.pools.hikari;

import java.util.Map;

import javax.sql.DataSource;

import com.creditease.agent.helpers.ReflectHelper;
import com.creditease.monitor.appfra.hook.spi.HookConstants;
import com.creditease.monitor.appfra.hook.spi.HookContext;
import com.creditease.monitor.captureframework.spi.MonitorElement;
import com.creditease.monitor.captureframework.spi.MonitorElementInstance;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;
import com.creditease.uav.hook.jdbc.pools.AbsDBPoolHookProxy;
import com.creditease.uav.monitorframework.dproxy.DynamicProxyInstaller;
import com.creditease.uav.util.MonitorServerUtil;

public class HikariHookProxy extends AbsDBPoolHookProxy {

    public static final String MTRX_PREFIX = "EXT_hikari_";

    protected DynamicProxyInstaller dpInstall;

    private boolean isInit = false;

    @SuppressWarnings("rawtypes")
    public HikariHookProxy(String id, Map config) {
        super(id, config);

        dpInstall = new DynamicProxyInstaller();
    }

    @Override
    public void start(HookContext context, ClassLoader webapploader) {

        super.start(context, webapploader);

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

    /**
     * InsertInterceptToClients
     * 
     * @param ic
     */
    private void InsertInterceptToClients(HookContext context, ClassLoader webapploader) {

        if (isHookEventDone("InsertInterceptToClients")) {
            return;
        }

        InterceptContext ic = (InterceptContext) context.get(HookConstants.INTERCEPTCONTEXT);
        String contextPath = (String) ic.get(InterceptConstants.CONTEXTPATH);
        String basePath = (String) ic.get(InterceptConstants.BASEPATH);
        appid = MonitorServerUtil.getApplicationId(contextPath, basePath);

        // /**
        // * set the webapploader is the target classloader
        // */
        // dpInstall.setTargetClassLoader(webapploader);
        //
        // /**
        // * Hikari DataSource
        // */
        // dpInstall.installProxy("com.zaxxer.hikari.HikariDataSource",
        // new String[] { "com.creditease.uav.hook.jdbc.pools.hikari.interceptors" }, new DynamicProxyProcessor() {
        //
        // @Override
        // public void process(CtMethod m) throws Exception {
        //
        // if ("getConnection".equals(m.getName())) {
        // dpInstall.defineLocalVal(m, "mObj", DBCPIT.class);
        // m.insertBefore("{mObj=new HikariIT(\"" + id + "\",this);}");
        // }
        // }
        // }, false);
        //
        // // release loader
        // dpInstall.releaseTargetClassLoader();
    }

    @Override
    protected void collectDBPoolMetrics(MonitorElement clientElem) {

        if (this.datasources.size() == 0) {
            return;
        }

        for (DataSource cp : this.datasources) {

            String jdbcURL = (String) ReflectHelper.invoke(cp.getClass().getName(), cp, "getJdbcUrl", null, null,
                    cp.getClass().getClassLoader());

            /**
             * 匹配客户端应用
             */
            MonitorElementInstance inst = this.matchElemInstance(clientElem, jdbcURL);

            if (inst == null) {
                continue;
            }

            collectDataSourceStat(inst, cp);

        }
    }

    /**
     * 收集DataSource指标
     * 
     * @param inst
     * @param pds
     */
    private void collectDataSourceStat(MonitorElementInstance inst, DataSource pds) {

        String[] collectMtrx = new String[] { "ActiveConnections", "IdleConnections", "TotalConnections",
                "ThreadsAwaitingConnection" };

        String prefix = "get";

        Object pool = ReflectHelper.getField(pds.getClass(), pds, "pool");

        if (pool == null) {
            return;
        }

        String className = pool.getClass().getName();

        for (int i = 0; i < collectMtrx.length; i++) {

            Object val = ReflectHelper.invoke(className, pool, prefix + collectMtrx[i], null, null);

            if (val == null) {
                continue;
            }

            inst.setValue(MTRX_PREFIX + collectMtrx[i], val);
        }
    }

}
