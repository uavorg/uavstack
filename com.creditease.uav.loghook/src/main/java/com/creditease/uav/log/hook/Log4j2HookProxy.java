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

package com.creditease.uav.log.hook;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AsyncAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.MemoryMappedFileAppender;
import org.apache.logging.log4j.core.appender.RandomAccessFileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.creditease.monitor.UAVServer;
import com.creditease.monitor.UAVServer.ServerVendor;
import com.creditease.monitor.appfra.hook.spi.HookConstants;
import com.creditease.monitor.appfra.hook.spi.HookContext;
import com.creditease.monitor.appfra.hook.spi.HookProxy;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;
import com.creditease.uav.log.hook.interceptors.LogIT;
import com.creditease.uav.monitorframework.dproxy.DynamicProxyInstaller;
import com.creditease.uav.monitorframework.dproxy.DynamicProxyProcessor;
import com.creditease.uav.monitorframework.dproxy.bytecode.DPMethod;
import com.creditease.uav.profiling.handlers.log.LogProfileInfo;

/*
 * 日志画像支持Log4j2 2.0-2.11.0，调用链与日志关联支持PatternLayout，不支持其他layout和AsyncAppender
 */
public class Log4j2HookProxy extends HookProxy {

    private final DynamicProxyInstaller dpInstall;

    @SuppressWarnings("rawtypes")
    public Log4j2HookProxy(String id, Map config) {

        super(id, config);
        dpInstall = new DynamicProxyInstaller();
    }

    @Override
    public void start(HookContext context, ClassLoader webapploader) {

        Event evt = context.get(Event.class);

        switch (evt) {
            case WEBCONTAINER_RESOURCE_INIT:
            case WEBCONTAINER_INIT:
                InsertIntercept(context, webapploader);
                break;
            // when servlet init
            case AFTER_SERVET_INIT:
                break;
            case BEFORE_SERVLET_DESTROY:
                break;
            case WEBCONTAINER_STARTED:
                figureOutLog4j2Config(context, webapploader);
                break;
            case WEBCONTAINER_STOPPED:
                break;
            default:
                break;
        }
    }

    @Override
    public void stop(HookContext context, ClassLoader webapploader) {

    }

    /**
     * figure out the log4j2's configuration
     * 
     * @param context
     * @param webapploader
     */
    private void figureOutLog4j2Config(HookContext context, ClassLoader webapploader) {

        InterceptContext interceptContext = (InterceptContext) context.get(HookConstants.INTERCEPTCONTEXT);
        if (interceptContext == null) {
            logger.warn("No InterceptContext available, can't figure out Log4j2 configuration.", null);
            return;
        }

        String appid = (String) (interceptContext.get(InterceptConstants.CONTEXTPATH));
        @SuppressWarnings("unchecked")
        LinkedList<LogProfileInfo> list = (LinkedList<LogProfileInfo>) interceptContext.get(HookConstants.LOG_PROFILE_LIST);

        if (null == list) {
            list = new LinkedList<LogProfileInfo>();
            interceptContext.put(HookConstants.LOG_PROFILE_LIST, list);
        }

        org.apache.logging.log4j.spi.LoggerContext lc = LogManager.getContext(webapploader, false);
        if (!(lc instanceof LoggerContext)) {
            return;
        }

        Configuration config = ((LoggerContext) lc).getConfiguration();
        Map<String, LoggerConfig> loggers = config.getLoggers();

        for (LoggerConfig log4j2 : loggers.values()) {
            Map<String, Appender> appenders = log4j2.getAppenders();
            for (Appender appender : appenders.values()) {
                if (appender instanceof AsyncAppender) {
                    String[] appendersRef = ((AsyncAppender) appender).getAppenderRefStrings();
                    Map<String, Appender> allAps = config.getAppenders();
                    for (String apName : appendersRef) {
                        Appender supAp = allAps.get(apName);
                        LogProfileInfo logProfileInfo = getAppenderInfo(supAp, appid);
                        if(logProfileInfo != null) {
                            list.add(logProfileInfo);
                        }
                    }
                }
                else {
                    LogProfileInfo logProfileInfo = getAppenderInfo(appender, appid);
                    if(logProfileInfo != null) {
                        list.add(logProfileInfo);
                    }
                }
            }
        }
    }

    private LogProfileInfo getAppenderInfo(Appender appender, String appid) {
        
        if(appender == null) {
            return null;
        }
        
        String fileName;
        if (appender instanceof FileAppender) {
            fileName = ((FileAppender) appender).getFileName();
        }
        else if (appender instanceof RollingFileAppender) {
            fileName = ((RollingFileAppender) appender).getFileName();
        }
        else if (appender instanceof RollingRandomAccessFileAppender) {
            fileName = ((RollingRandomAccessFileAppender) appender).getFileName();
        }
        else if (appender instanceof RandomAccessFileAppender) {
            fileName = ((RandomAccessFileAppender) appender).getFileName();
        }
        else if (appender instanceof MemoryMappedFileAppender) {
            fileName = ((MemoryMappedFileAppender) appender).getFileName();
        }
        else {
            return null;
        }

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(LogProfileInfo.ENGINE, "log4j2");
        Layout<?> layout = appender.getLayout();
        if (null != layout) {
            if (layout instanceof PatternLayout) {
                attributes.put(LogProfileInfo.PATTERN, ((PatternLayout) layout).getConversionPattern());
            }
            else {
                attributes.put(LogProfileInfo.PATTERN, layout.getClass().getSimpleName());
            }
        }

        LogProfileInfo logProfileInfo = new LogProfileInfo();
        logProfileInfo.setAppId(appid);
        logProfileInfo.setFilePath(fileName);
        logProfileInfo.setAttributes(attributes);
        logProfileInfo.setLogType(LogProfileInfo.LogType.Log4j);

        return logProfileInfo;
    }

    private void InsertIntercept(HookContext context, ClassLoader webapploader) {

        if (isHookEventDone("insertLog4j2Intercepter")) {
            return;
        }

        ServerVendor vendor = (ServerVendor) UAVServer.instance().getServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR);
        if (vendor == ServerVendor.SPRINGBOOT) {
            return;
        }

        /**
         * set the webapploader is the target classloader
         */
        dpInstall.setTargetClassLoader(webapploader);

        // 定义一个类里面的hook变量并初始化，防止每次拦截write时都初始化对象
        dpInstall.defineField("uavLogHook", LogIT.class, "org.apache.logging.log4j.core.layout.PatternLayout", "new LogIT()");

        dpInstall.installProxy("org.apache.logging.log4j.core.layout.PatternLayout",
                new String[] { "com.creditease.uav.log.hook.interceptors" }, new DynamicProxyProcessor() {

                    @Override
                    public void process(DPMethod m) throws Exception {

                        if ("toText".equals(m.getName())) {
                            m.insertAfter("{$_=uavLogHook.formatLog($_);}");
                        }
                        else if ("toSerializable".equals(m.getName()) && "String".equals(m.getReturnType().getSimpleName())) {
                            m.insertAfter("{$_=uavLogHook.formatLog($_);}");
                        }
                    }
                }, false);

        // release loader
        dpInstall.releaseTargetClassLoader();
    }
}
