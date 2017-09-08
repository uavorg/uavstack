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

package com.creditease.uav.apm.invokechain.spi;

import com.creditease.monitor.log.DataLogger;
import com.creditease.uav.apm.invokechain.span.Span;
import com.creditease.uav.apm.invokechain.span.SpanFactory;
import com.creditease.uav.apm.supporters.InvokeChainSupporter;
import com.creditease.uav.common.BaseComponent;

/**
 * 
 * InvokeChainCapHandler description: 调用链处理的实现基础类
 *
 */
public abstract class InvokeChainCapHandler extends BaseComponent {

    protected SpanFactory spanFactory = new SpanFactory();

    /**
     * 获取应用InvokeChain日志实例
     * 
     * @param appid
     * @return
     */
    protected DataLogger getAppInvokeChainLogger(String appid) {

        return this.getSupporter(InvokeChainSupporter.class).getDataLogger("ivc", appid);
    }

    /**
     * Before Span Capture action
     * 
     * @param context
     */
    public abstract void preCap(InvokeChainContext context);

    /**
     * After Span Capture action
     * 
     * @param context
     */
    public abstract void doCap(InvokeChainContext context);

    /**
     * 根据关键类和层数差获取当前业务调用类信息
     * 
     * @param span
     * @param level
     * @param clazz
     */
    protected void setCallerThreadInfo(Span span, int level, String clazz) {

        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        int num = -1;
        // 功能增强，当clazz字符串结尾为*时匹配所有栈信息，找出最后符合条件的信息
        if (clazz.endsWith("*")) {
            String str = clazz.substring(0, clazz.length() - 2);
            for (int i = 0; i < stacktrace.length; i++) {
                if (stacktrace[i].getClassName().indexOf(str) > -1) {
                    num = i;
                }
            }
        }
        else {
            for (int i = 0; i < stacktrace.length; i++) {
                if (stacktrace[i].getClassName().equals(clazz) && !stacktrace[i + 1].getClassName().equals(clazz)) {
                    num = i;
                    break;
                }
            }
        }

        if (num == -1) {
            if (logger.isLogEnabled()) {
                logger.warn("InvokeChain Caller Not Found: target class=" + clazz + ",span=" + span.toString(), null);
            }
            return;
        }

        StackTraceElement a = stacktrace[num + level];

        span.setClassName(a.getClassName());
        span.setMethodName(a.getMethodName());
    }
}
