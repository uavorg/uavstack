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

package com.creditease.monitor.interceptframework;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;

public class StandardInterceptContextHelper {

    private static TransmittableThreadLocal<StandardInterceptContextHelper> ThreadCaptureContextHelper = new TransmittableThreadLocal<StandardInterceptContextHelper>();

    protected static InterceptContext getContext(Event event, boolean isCreateForNone) {

        StandardInterceptContextHelper ContextHelper = init(isCreateForNone);

        if (ContextHelper == null) {
            return null;
        }

        return ContextHelper.get(event);
    }

    private static StandardInterceptContextHelper init(boolean isCreateForNone) {

        StandardInterceptContextHelper ContextHelper = ThreadCaptureContextHelper.get();
        if (ContextHelper == null && isCreateForNone == true) {
            ContextHelper = new StandardInterceptContextHelper();
            ThreadCaptureContextHelper.set(ContextHelper);
        }
        return ContextHelper;
    }

    public static void releaseContext(Event event) {

        StandardInterceptContextHelper ContextHelper = init(false);

        if (ContextHelper == null) {
            return;
        }

        ContextHelper.remove(event);
    }

    private Map<Event, InterceptContext> threadInterceptContext = new ConcurrentHashMap<Event, InterceptContext>();

    private InterceptContext get(Event event) {

        if (event == null)
            return null;

        InterceptContext context = threadInterceptContext.get(event);

        if (context == null) {
            context = new StandardInterceptContext(event);
            threadInterceptContext.put(event, context);
        }

        return context;
    }

    private void remove(Event event) {

        if (event == null)
            return;

        threadInterceptContext.remove(event);
    }
}
