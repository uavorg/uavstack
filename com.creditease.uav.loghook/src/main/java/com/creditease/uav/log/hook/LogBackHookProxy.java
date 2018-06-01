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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

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

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.html.HTMLLayout;
import ch.qos.logback.classic.layout.TTLLLayout;
import ch.qos.logback.classic.log4j.XMLLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;

public class LogBackHookProxy extends HookProxy {

    private final DynamicProxyInstaller dpInstall;

    @SuppressWarnings("rawtypes")
    public LogBackHookProxy(String id, Map config) {
        super(id, config);
        dpInstall = new DynamicProxyInstaller();
    }

    @Override
    public void start(HookContext context, ClassLoader webapploader) {

        Event evt = context.get(Event.class);

        switch (evt) {
            case WEBCONTAINER_INIT:
                InsertIntercept(context, webapploader);
                break;
            // when servlet init
            case AFTER_SERVET_INIT:
                break;
            case BEFORE_SERVLET_DESTROY:
                break;
            case WEBCONTAINER_STARTED:
                figureOutLogBackConfig(context, webapploader);
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
     * figure out the logback's configuration: for example, appenders' file path, buff io, etc...
     * 
     * @param context
     * @param webapploader
     */
    private void figureOutLogBackConfig(HookContext context, ClassLoader webapploader) {

        Logger logback = (Logger) LoggerFactory.getLogger(LogBackHookProxy.class);

        InterceptContext interceptContext = (InterceptContext) context.get(HookConstants.INTERCEPTCONTEXT);

        if (interceptContext == null) {
            logback.warn("No InterceptContext available, can't figure out LogBack configuration.");
            return;
        }

        @SuppressWarnings("unchecked")
        LinkedList<LogProfileInfo> list = (LinkedList<LogProfileInfo>) interceptContext
                .get(HookConstants.LOG_PROFILE_LIST);

        if (null == list) {
            list = new LinkedList<LogProfileInfo>();
            interceptContext.put(HookConstants.LOG_PROFILE_LIST, list);
        }

        String appid = (String) (interceptContext.get(InterceptConstants.CONTEXTPATH));

        // figureour all loggers
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        List<ch.qos.logback.classic.Logger> loggers = loggerContext.getLoggerList();

        for (Logger logger : loggers) {
            figureoutLogConfiguration(logger, list, appid);
        }

    }

    private void figureoutLogConfiguration(Logger Logger, LinkedList<LogProfileInfo> list, String appid) {

        Iterator<Appender<ILoggingEvent>> appenders = Logger.iteratorForAppenders();
        while (appenders != null && appenders.hasNext()) {
            Appender<ILoggingEvent> appender = appenders.next();
            if (appender instanceof FileAppender<?>) {
                getAppenderInfo((FileAppender<ILoggingEvent>)appender, list, appid);
            }
            else if(appender instanceof AsyncAppender) {
                Iterator<Appender<ILoggingEvent>> itAppenders = (Iterator<Appender<ILoggingEvent>>)((AsyncAppender) appender).iteratorForAppenders();
                while (itAppenders != null && itAppenders.hasNext()) {
                    Appender<ILoggingEvent> ap = itAppenders.next();
                    if (ap instanceof FileAppender<?>) {
                        getAppenderInfo((FileAppender<ILoggingEvent>)ap, list, appid);
                    }
                } 
            }
        }
    }

    private void InsertIntercept(HookContext context, ClassLoader webapploader) {

        if (isHookEventDone("insertLogbackIntercepter")) {
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
        dpInstall.defineField("uavLogHook", LogIT.class, "ch.qos.logback.core.encoder.LayoutWrappingEncoder",
                "new LogIT()");

        dpInstall.installProxy("ch.qos.logback.core.encoder.LayoutWrappingEncoder",
                new String[] { "com.creditease.uav.log.hook.interceptors" }, new DynamicProxyProcessor() {

                    @Override
                    public void process(DPMethod m) throws Exception {

                        if ("convertToBytes".equals(m.getName())) {
                            m.insertBefore("{$1=uavLogHook.formatLog($1);}");

                        }
                    }
                }, false);

        // release loader
        dpInstall.releaseTargetClassLoader();
    }
    
    private void getAppenderInfo(FileAppender<ILoggingEvent> fileAppender, LinkedList<LogProfileInfo> list, String appid) {
        
        LogProfileInfo logProfileInfo = new LogProfileInfo();
        logProfileInfo.setLogType(LogProfileInfo.LogType.Log4j);
        logProfileInfo.setFilePath(fileAppender.getFile());
        if (appid != null) {
            logProfileInfo.setAppId(appid);
        }
        
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(LogProfileInfo.ENGINE, "logback");

        @SuppressWarnings("rawtypes")
        LayoutWrappingEncoder encoder = (LayoutWrappingEncoder) fileAppender.getEncoder();
        Layout<?> layout = encoder.getLayout();
        if (null != layout) {
            if (layout instanceof PatternLayout) {
                PatternLayout patternLayout = (PatternLayout) encoder.getLayout();
                attributes.put(LogProfileInfo.PATTERN, patternLayout.getPattern());
            }
            else if (layout instanceof HTMLLayout) {
                attributes.put(LogProfileInfo.PATTERN, "HTMLLayout");
            }
            else if (layout instanceof XMLLayout) {
                attributes.put(LogProfileInfo.PATTERN, "XMLLayout");
            }
            else if (layout instanceof TTLLLayout) {
                attributes.put(LogProfileInfo.PATTERN, "TTCCLayout");
            }
        }

        logProfileInfo.setAttributes(attributes);
        list.add(logProfileInfo);      
    }
}
