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
import org.apache.log4j.FileAppender;
import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.TTCCLayout;
import org.apache.log4j.spi.LoggerRepository;

import com.creditease.monitor.appfra.hook.spi.HookConstants;
import com.creditease.monitor.appfra.hook.spi.HookContext;
import com.creditease.monitor.appfra.hook.spi.HookProxy;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;
import com.creditease.uav.profiling.handlers.log.LogProfileInfo;

public class Log4jHookProxy extends HookProxy {

    @SuppressWarnings("rawtypes")
    public Log4jHookProxy(String id, Map config) {
        super(id, config);
    }

    @Override
    public void start(HookContext context, ClassLoader webapploader) {

        Event evt = context.get(Event.class);

        switch (evt) {
            case WEBCONTAINER_RESOURCE_INIT:
            case WEBCONTAINER_INIT:

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
        figureoutLogConfiguration(logger4j.getRootLogger(), list, appid);
		
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

            figureoutLogConfiguration(sLogger, list, appid);
        }
    }

    @SuppressWarnings("unchecked")
    private void figureoutLogConfiguration(Logger Logger, LinkedList<LogProfileInfo> list, String appid) {

        Enumeration<Appender> appenders = Logger.getAllAppenders();

        while (appenders != null && appenders.hasMoreElements()) {

            LogProfileInfo logProfileInfo = new LogProfileInfo();

            if (appid != null) {
                logProfileInfo.setAppId(appid);
            }

            logProfileInfo.setLogType(LogProfileInfo.LogType.Log4j);

            Map<String, String> attributes = new HashMap<String, String>();

            attributes.put(LogProfileInfo.ENGINE, "log4j");

            Appender appender = appenders.nextElement();

            if (!(appender instanceof FileAppender)) {
                continue;
            }

            FileAppender fileAppender = (FileAppender) appender;

            Layout layout = fileAppender.getLayout();

            if (null != layout) {

                if (layout instanceof PatternLayout) {
                    PatternLayout patternLayout = (PatternLayout) fileAppender.getLayout();
                    attributes.put(LogProfileInfo.PATTERN, patternLayout.getConversionPattern());
                }
                else if (layout instanceof HTMLLayout) {
                    attributes.put(LogProfileInfo.PATTERN, "HTMLLayout");
                }
                else if (layout instanceof SimpleLayout) {
                    attributes.put(LogProfileInfo.PATTERN, "SimpleLayout");
                }
                else if (layout instanceof TTCCLayout) {
                    attributes.put(LogProfileInfo.PATTERN, "TTCCLayout");
                }
            }

            boolean useBuffIO = fileAppender.getBufferedIO();

            if (useBuffIO == true) {
                attributes.put(LogProfileInfo.BUFFER_SIZE, String.valueOf(fileAppender.getBufferSize()));
                attributes.put(LogProfileInfo.BUFFRT_IO, String.valueOf(useBuffIO));
            }

            logProfileInfo.setFilePath(fileAppender.getFile());
            logProfileInfo.setAttributes(attributes);

            list.add(logProfileInfo);

        }
    }
}
