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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Appender;
import org.apache.log4j.AsyncAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggerRepository;

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

public class Log4jHookProxy extends HookProxy {

    private final DynamicProxyInstaller dpInstall;

    @SuppressWarnings("rawtypes")
    public Log4jHookProxy(String id, Map config) {

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
                figureOutLog4jConfig(context, webapploader);
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
     * figure out the log4j's configuration: for example, appenders' file path, buff io, etc...
     * 
     * @param context
     * @param webapploader
     */
    @SuppressWarnings({ "rawtypes", "static-access" })
    private void figureOutLog4jConfig(HookContext context, ClassLoader webapploader) {

        Logger logger4j = Logger.getLogger(Log4jHookProxy.class);

        InterceptContext interceptContext = (InterceptContext) context.get(HookConstants.INTERCEPTCONTEXT);

        if (interceptContext == null) {
            logger.warn("No InterceptContext available, can't figure out Log4j configuration.", null);
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

        // figureout root logger
        list.addAll(figureoutLogConfiguration(logger4j.getRootLogger(), appid));

        // figureour norootlogger
        LoggerRepository lr = null;
        try {
            lr = logger4j.getLoggerRepository();
        }
        catch (NoSuchMethodError err) {
            // for log4j-over-slf4j, doesn't have this method
            return;
        }

        Enumeration logEnum = lr.getCurrentLoggers();

        while (logEnum != null && logEnum.hasMoreElements()) {

            Logger sLogger = (Logger) logEnum.nextElement();

            list.addAll(figureoutLogConfiguration(sLogger, appid));
        }
    }

    @SuppressWarnings("unchecked")
    private LinkedList<LogProfileInfo> figureoutLogConfiguration(Logger Logger, String appid) {
        
        LinkedList<LogProfileInfo> logProfiles = new LinkedList<LogProfileInfo>();

        Enumeration<Appender> appenders = Logger.getAllAppenders();
        while (appenders != null && appenders.hasMoreElements()) {

            Appender appender = appenders.nextElement();
            if (appender instanceof FileAppender) {
                logProfiles.add(getAppenderInfo((FileAppender)appender, appid));
            }
            else if(appender instanceof AsyncAppender) {
                Enumeration<Appender> ads = ((AsyncAppender) appender).getAllAppenders();
                while(ads != null && ads.hasMoreElements()) {
                    Appender ap = ads.nextElement();
                    if(ap instanceof FileAppender) {
                        logProfiles.add(getAppenderInfo((FileAppender)ap, appid));
                    }
                }
            }
        }
        
        return logProfiles;
    }
    
    private LogProfileInfo getAppenderInfo(FileAppender fileAppender, String appid) {

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(LogProfileInfo.ENGINE, "log4j");
        
        Layout layout = fileAppender.getLayout();
        if (null != layout) {
            if (layout instanceof PatternLayout) {
                PatternLayout patternLayout = (PatternLayout) fileAppender.getLayout();
                attributes.put(LogProfileInfo.PATTERN, patternLayout.getConversionPattern());
            }
            else {
                attributes.put(LogProfileInfo.PATTERN, layout.getClass().getSimpleName());
            }
        }

        boolean useBuffIO = fileAppender.getBufferedIO();
        if (useBuffIO == true) {
            attributes.put(LogProfileInfo.BUFFER_SIZE, String.valueOf(fileAppender.getBufferSize()));
            attributes.put(LogProfileInfo.BUFFRT_IO, String.valueOf(useBuffIO));
        }
        
        LogProfileInfo logProfileInfo = new LogProfileInfo();
        logProfileInfo.setAppId(appid);
        logProfileInfo.setFilePath(fileAppender.getFile());
        logProfileInfo.setAttributes(attributes);
        logProfileInfo.setLogType(LogProfileInfo.LogType.Log4j);

        return logProfileInfo;

    }

    private void InsertIntercept(HookContext context, ClassLoader webapploader) {

        if (isHookEventDone("insertLog4jIntercepter")) {
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
        dpInstall.defineField("uavLogHook", LogIT.class, "org.apache.log4j.helpers.QuietWriter", "new LogIT()");
        // 定义换行符变量(与log4j中保持一致)
        dpInstall.defineField("uavLogHookLineSep", String.class, "org.apache.log4j.helpers.QuietWriter",
                "System.getProperty(\"line.separator\")");

        dpInstall.installProxy("org.apache.log4j.helpers.QuietWriter",
                new String[] { "com.creditease.uav.log.hook.interceptors" }, new DynamicProxyProcessor() {

                    @Override
                    public void process(DPMethod m) throws Exception {

                        if ("write".equals(m.getName())) {
                            m.insertBefore("{if(!$1.equals(uavLogHookLineSep)){$1=uavLogHook.formatLog($1);}}");

                        }
                    }
                }, false);

        // release loader
        dpInstall.releaseTargetClassLoader();
    }
}
