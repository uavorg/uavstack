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

package com.creditease.uav.hook.jdbc.pools;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import com.creditease.monitor.appfra.hook.spi.HookConstants;
import com.creditease.monitor.appfra.hook.spi.HookContext;
import com.creditease.monitor.appfra.hook.spi.HookProxy;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.monitor.captureframework.spi.MonitorElement;
import com.creditease.monitor.captureframework.spi.MonitorElementInstance;
import com.creditease.monitor.captureframework.spi.MonitorFactory;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;
import com.creditease.uav.util.MonitorServerUtil;

public abstract class AbsDBPoolHookProxy extends HookProxy {

    protected String appid;

    protected Set<DataSource> datasources = new HashSet<DataSource>();

    @SuppressWarnings("rawtypes")
    public AbsDBPoolHookProxy(String id, Map config) {
        super(id, config);
    }

    @Override
    public void start(HookContext context, ClassLoader webapploader) {

        Event event = context.get(Event.class);
        InterceptContext ic = (InterceptContext) context.get(HookConstants.INTERCEPTCONTEXT);
        switch (event) {
            case SPRING_BEAN_REGIST:
            case WEBCONTAINER_INIT:
                initHook(ic);
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
            case WEBCONTAINER_RESOURCE_INIT:
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
     * initHook
     * 
     * @param ic
     */
    private void initHook(InterceptContext ic) {

        if (isHookEventDone("initHook")) {
            return;
        }
        String contextPath = (String) ic.get(InterceptConstants.CONTEXTPATH);
        String basePath = (String) ic.get(InterceptConstants.BASEPATH);
        appid = MonitorServerUtil.getApplicationId(contextPath, basePath);
    }

    @Override
    public void stop(HookContext context, ClassLoader webapploader) {

        Event event = context.get(Event.class);
        switch (event) {
            case AFTER_SERVET_INIT:
                break;
            case BEFORE_SERVLET_DESTROY:
                break;
            case GLOBAL_FILTER_REQUEST:
                break;
            case GLOBAL_FILTER_RESPONSE:
                break;
            case WEBCONTAINER_INIT:
                break;
            case WEBCONTAINER_RESOURCE_CREATE:
                break;
            case WEBCONTAINER_RESOURCE_INIT:
                break;
            case WEBCONTAINER_STARTED:
                break;
            case WEBCONTAINER_STOPPED:
                datasources.clear();
                break;
            default:
                break;

        }
    }

    /**
     * isRun
     */
    @Override
    public boolean isRun(HookContext context) {

        String check = (String) context.get("monitor.client.prestore");

        if (check == null) {
            return false;
        }

        return true;
    }

    @Override
    public void run(HookContext context) {

        String check = (String) context.get("monitor.client.prestore");

        if (check == null) {
            return;
        }

        /**
         * Step 1: 从ClientMonitor去clientResp的MonitorElem
         */
        Monitor monitor = MonitorFactory.instance().getMonitor(CaptureConstants.MONITOR_CLIENT);

        MonitorElement[] meElem = monitor.getRepository().getElementByMoElemIdAndCapId(
                CaptureConstants.MOELEM_CLIENT_RESPTIME, CaptureConstants.CAPPOINT_APP_CLIENT);

        MonitorElement clientElem = meElem[0];

        collectDBPoolMetrics(clientElem);
    }

    /**
     * matchElemInstance
     * 
     * @param clientElem
     * @param jdbcURL
     * @return
     */
    public MonitorElementInstance matchElemInstance(MonitorElement clientElem, String jdbcURL) {

        jdbcURL = MonitorServerUtil.formatJDBCURL(jdbcURL);

        /**
         * 匹配客户端应用
         */
        MonitorElementInstance inst = MonitorServerUtil.matchClientUrl(clientElem, jdbcURL, appid);

        return inst;
    }

    /**
     * 注册DataSource
     * 
     * @param ds
     */
    public void registerDataSource(DataSource ds) {

        this.datasources.add(ds);
    }

    /**
     * collectDBPoolMetrics
     * 
     * @param clientElem
     */
    protected abstract void collectDBPoolMetrics(MonitorElement clientElem);
}
