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

package com.creditease.uav.apm.slowoper.span;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.creditease.uav.apm.invokechain.span.Span;

/**
 * 
 * 重调用链span工厂。 由于重调用链span不存在传递情况，但会出现并发写入情况。故此处选择使用ConcurrentHashMap实现
 *
 */
public class SlowOperSpanFactory {

    public static final Map<String, SlowOperSpan> spanTracker = new ConcurrentHashMap<String, SlowOperSpan>();

    /**
     * 获取SlowOperSpan
     * 
     * @return
     */
    public SlowOperSpan getSlowOperSpan(String key) {

        return spanTracker.get(key);
    }

    /**
     * 获取SlowOperSpan, 并删除SlowOperSpan
     * 
     * @param key
     * @return
     */
    public SlowOperSpan getRemoveSlowOperSpanFromContext(String key) {

        return spanTracker.remove(key);
    }

    /**
     * 设置SlowOperSpan
     * 
     * @param span
     */
    public void setSlowOperSpanToContext(String key, SlowOperSpan span) {

        spanTracker.put(key, span);
    }

    /**
     * 根据轻调用链span信息创建重调用链SlowOperSpan
     * 
     * @return
     */
    public SlowOperSpan buildSlowOperSpan(Span span) {

        SlowOperSpan slowOperSpan = new SlowOperSpan();
        slowOperSpan.setTraceId(span.getTraceId());
        slowOperSpan.setSpanId(span.getSpanId());
        slowOperSpan.setEndpointInfo(span.getEndpointInfo().split(",")[0]);
        return slowOperSpan;
    }

}
