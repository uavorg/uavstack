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

package com.creditease.uav.hook.jdbc;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.Map;

import com.creditease.monitor.UAVServer.ServerVendor;
import com.creditease.monitor.appfra.hook.spi.HookConstants;
import com.creditease.monitor.appfra.hook.spi.HookContext;
import com.creditease.monitor.appfra.hook.spi.HookProxy;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;
import com.creditease.uav.hook.jdbc.interceptors.JdbcDriverIT;
import com.creditease.uav.monitorframework.dproxy.DynamicProxyInstaller;
import com.creditease.uav.monitorframework.dproxy.DynamicProxyProcessor;
import com.creditease.uav.util.MonitorServerUtil;

import javassist.CtMethod;

public class JdbcHookProxy extends HookProxy {

    private final DynamicProxyInstaller dpInstall;

    private boolean isInjectDrvMgr = false;
    private boolean isInjectDruid = false;
    private boolean isInjectDBCP2 = false;
    private boolean isInjectTomcatDBCP2 = false;
    private boolean isInjectHikari = false;
    private boolean isInjectDataSource = false;

    @SuppressWarnings("rawtypes")
    public JdbcHookProxy(String id, Map config) {
        super(id, config);

        dpInstall = new DynamicProxyInstaller();
    }

    @Override
    public void start(HookContext context, ClassLoader webapploader) {

        Event event = context.get(Event.class);
        InterceptContext ic = (InterceptContext) context.get(HookConstants.INTERCEPTCONTEXT);
        switch (event) {
            case WEBCONTAINER_RESOURCE_INIT:
                this.injectDriverManager(webapploader, ic);
                this.injectDBCP2(webapploader, ic);
                this.injectTomcatDBCP2(webapploader, ic);
                this.injectHikari(webapploader, ic);
                this.injectDruid(webapploader, ic);
                break;
            case WEBCONTAINER_RESOURCE_CREATE:
                this.injectDataSource(ic);
                break;
            case WEBCONTAINER_INIT:
                this.injectDriverManager(webapploader, ic);
                this.injectDBCP2(webapploader, ic);
                this.injectTomcatDBCP2(webapploader, ic);
                this.injectHikari(webapploader, ic);
                this.injectDruid(webapploader, ic);
                break;
            case AFTER_SERVET_INIT:
                break;
            case BEFORE_SERVLET_DESTROY:
                break;
            case WEBCONTAINER_STARTED:
                break;
            case WEBCONTAINER_STOPPED:
                break;
            default:
                break;
        }

    }

    private String getAppID(InterceptContext ic) {

        String contextPath = (String) ic.get(InterceptConstants.CONTEXTPATH);
        String basePath = (String) ic.get(InterceptConstants.BASEPATH);
        String appid = MonitorServerUtil.getApplicationId(contextPath, basePath);

        return appid;
    }

    /**
     * inject DriverManager
     * 
     * @param webapploader
     * @param appid
     * @return
     */
    private void injectDriverManager(ClassLoader webapploader, InterceptContext ic) {

        if (this.isInjectDrvMgr == true) {
            return;
        }

        this.isInjectDrvMgr = true;

        JdbcDriverIT jdbcDriverIT = new JdbcDriverIT(this.getAppID(ic));

        jdbcDriverIT.initSomeDrivers(webapploader);

        Enumeration<Driver> eu = DriverManager.getDrivers();

        while (eu.hasMoreElements()) {

            Driver dr = eu.nextElement();

            jdbcDriverIT.doRegisterDriver(dr, true);
        }

    }

    /**
     * inject ApplicationServer DataSource
     * 
     * @param webapploader
     * @param appid
     */
    private void injectDataSource(InterceptContext ic) {

        Object resObj = ic.get(InterceptConstants.RESOURCEOBJ);
        Object resCfgObj = ic.get(InterceptConstants.RESOURCECFG);

        if (resObj == null || resCfgObj == null) {
            return;
        }

        if (this.isInjectDataSource == true) {
            return;
        }

        this.isInjectDataSource = true;

        JdbcDriverIT jdbcDriverIT = new JdbcDriverIT(this.getAppID(ic));

        ServerVendor vendor = (ServerVendor) this.getServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR);

        /**
         * NOTE: Tomcat use ResourceFactory to get DataSource Object, then we can get the url from config (Reference)
         * Diff AppServer the behavior may be not the same. SpringBoot default uses Tomcat as its default app server
         */
        if (vendor == ServerVendor.TOMCAT || vendor == ServerVendor.SPRINGBOOT) {

            Object dsProxy = jdbcDriverIT.doProxyDataSource(resObj, resCfgObj, "Tomcat");

            ic.put(InterceptConstants.RESOURCEOBJ, dsProxy);
        }

    }

    /**
     * inject TomcatDBCP2
     * 
     * 
     * @param webapploader
     * @param ic
     */
    private void injectTomcatDBCP2(ClassLoader webapploader, InterceptContext ic) {

        try {
            webapploader.loadClass("org.apache.tomcat.dbcp.dbcp2.Constants");
        }
        catch (ClassNotFoundException e) {
            return;
        }

        if (this.isInjectTomcatDBCP2 == true) {
            return;
        }

        this.isInjectTomcatDBCP2 = true;

        final String appid = this.getAppID(ic);

        /**
         * set the webapploader is the target classloader
         */
        dpInstall.setTargetClassLoader(webapploader);

        /**
         * inject Tomcat DBCP2 BasicDataSource
         */
        dpInstall.installProxy("org.apache.tomcat.dbcp.dbcp2.DriverConnectionFactory",
                new String[] { "com.creditease.uav.hook.jdbc.interceptors" }, new DynamicProxyProcessor() {

                    @Override
                    public void process(CtMethod m) throws Exception {

                        if ("createConnection".equals(m.getName())) {

                            dpInstall.defineLocalVal(m, "mObj", JdbcDriverIT.class);
                            m.insertBefore("{mObj=new JdbcDriverIT(\"" + appid
                                    + "\");mObj.setJDBCUrlByObjField(this,\"_connectUri\");}");
                            m.insertAfter("{$_=mObj.doProxyConnection($_);}");

                        }
                    }
                }, false);

        // release loader
        dpInstall.releaseTargetClassLoader();
    }

    /**
     * inject DBCP2
     * 
     * NOTE: DBCP is default injected by injectDriverManager
     * 
     * @param webapploader
     * @param ic
     */
    private void injectDBCP2(ClassLoader webapploader, InterceptContext ic) {

        try {
            webapploader.loadClass("org.apache.commons.dbcp2.Constants");
        }
        catch (ClassNotFoundException e) {
            return;
        }

        if (this.isInjectDBCP2 == true) {
            return;
        }

        this.isInjectDBCP2 = true;

        final String appid = this.getAppID(ic);

        /**
         * set the webapploader is the target classloader
         */
        dpInstall.setTargetClassLoader(webapploader);

        /**
         * inject DBCP2 BasicDataSource
         */
        dpInstall.installProxy("org.apache.commons.dbcp2.DriverConnectionFactory",
                new String[] { "com.creditease.uav.hook.jdbc.interceptors" }, new DynamicProxyProcessor() {

                    @Override
                    public void process(CtMethod m) throws Exception {

                        if ("createConnection".equals(m.getName())) {

                            dpInstall.defineLocalVal(m, "mObj", JdbcDriverIT.class);
                            m.insertBefore("{mObj=new JdbcDriverIT(\"" + appid
                                    + "\");mObj.setJDBCUrlByObjField(this,\"_connectUri\");}");
                            m.insertAfter("{$_=mObj.doProxyConnection($_);}");

                        }
                    }
                }, false);

        // release loader
        dpInstall.releaseTargetClassLoader();
    }

    /**
     * inject Hikari
     * 
     * @param webapploader
     * @param ic
     */
    private void injectHikari(ClassLoader webapploader, InterceptContext ic) {

        try {
            webapploader.loadClass("com.zaxxer.hikari.HikariConfigMXBean");
        }
        catch (ClassNotFoundException e) {
            return;
        }

        if (isInjectHikari == true) {
            return;
        }

        isInjectHikari = true;

        final String appid = this.getAppID(ic);

        final String contextPath = (String) ic.get(InterceptConstants.CONTEXTPATH);

        /**
         * set the webapploader is the target classloader
         */
        dpInstall.setTargetClassLoader(webapploader);

        /**
         * inject DBCP2 BasicDataSource
         */
        dpInstall
                .installProxy("com.zaxxer.hikari.HikariDataSource",
                        new String[] { "com.creditease.uav.hook.jdbc.interceptors",
                                "com.creditease.uav.hook.jdbc.pools.hikari.interceptors" },
                        new DynamicProxyProcessor() {

                            @Override
                            public void process(CtMethod m) throws Exception {

                                if ("getConnection".equals(m.getName())
                                        && (m.getParameterTypes() == null || m.getParameterTypes().length == 0)) {

                                    dpInstall.defineLocalVal(m, "mObj", JdbcDriverIT.class);
                                    // dpInstall.defineLocalVal(m, "mObj2", HikariIT.class);
                                    m.insertBefore("{mObj=new JdbcDriverIT(\"" + appid
                                            + "\");mObj.setJDBCUrl(this.getJdbcUrl());new HikariIT(\"" + contextPath
                                            + "\",this);}");
                                    m.insertAfter("{$_=mObj.doProxyConnection($_);}");

                                }
                            }
                        }, false);

        // release loader
        dpInstall.releaseTargetClassLoader();
    }

    /**
     * injectDruid
     * 
     * @param webapploader
     * @param appid
     */
    private void injectDruid(ClassLoader webapploader, InterceptContext ic) {

        /**
         * check if there is druid, god, so sad we are in china, we have to support alibaba druid
         */
        try {
            webapploader.loadClass("com.alibaba.druid.Constants");
        }
        catch (ClassNotFoundException e) {
            return;
        }

        if (this.isInjectDruid == true) {
            return;
        }

        this.isInjectDruid = true;

        final String appid = this.getAppID(ic);

        /**
         * set the webapploader is the target classloader
         */
        dpInstall.setTargetClassLoader(webapploader);

        /**
         * inject DruidDriver
         */
        dpInstall.installProxy("com.alibaba.druid.proxy.jdbc.DataSourceProxyImpl",
                new String[] { "com.creditease.uav.hook.jdbc.interceptors" }, new DynamicProxyProcessor() {

                    @Override
                    public void process(CtMethod m) throws Exception {

                        if ("connect".equals(m.getName())) {

                            dpInstall.defineLocalVal(m, "mObj", JdbcDriverIT.class);
                            m.insertBefore("{mObj=new JdbcDriverIT(\"" + appid + "\");}");
                            m.insertAfter("{$_=mObj.doProxyConnection($_);}");

                        }
                    }
                }, false);

        /**
         * inject DruidDataSource
         */
        dpInstall.installProxy("com.alibaba.druid.util.JdbcUtils",
                new String[] { "com.creditease.uav.hook.jdbc.interceptors" }, new DynamicProxyProcessor() {

                    @Override
                    public void process(CtMethod m) throws Exception {

                        if ("createDriver".equals(m.getName()) && m.getParameterTypes().length == 2) {

                            dpInstall.defineLocalVal(m, "mObj", JdbcDriverIT.class);
                            m.insertBefore("{mObj=new JdbcDriverIT(\"" + appid + "\");}");
                            m.insertAfter("{$_=mObj.doRegisterDriver($_,false);}");

                        }
                    }
                }, false);

        // release loader
        dpInstall.releaseTargetClassLoader();

    }

    @Override
    public void stop(HookContext context, ClassLoader webapploader) {

        Event event = context.get(Event.class);
        switch (event) {
            case AFTER_SERVET_INIT:
                break;
            case BEFORE_SERVLET_DESTROY:
                break;
            case WEBCONTAINER_STARTED:
                break;
            case WEBCONTAINER_STOPPED:
                break;
            default:
                break;
        }
    }

}
