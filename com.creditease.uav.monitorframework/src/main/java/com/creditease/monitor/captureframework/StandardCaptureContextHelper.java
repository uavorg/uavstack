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

package com.creditease.monitor.captureframework;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.creditease.monitor.captureframework.spi.CaptureContext;

public class StandardCaptureContextHelper {

    private static ThreadLocal<StandardCaptureContextHelper> ThreadCaptureContextHelper = new ThreadLocal<StandardCaptureContextHelper>();

    protected static CaptureContext getContext(String monitorId) {

        StandardCaptureContextHelper ContextHelper = init();

        return ContextHelper.get(monitorId);
    }

    private static StandardCaptureContextHelper init() {

        StandardCaptureContextHelper ContextHelper = ThreadCaptureContextHelper.get();
        if (ContextHelper == null) {
            ContextHelper = new StandardCaptureContextHelper();
            ThreadCaptureContextHelper.set(ContextHelper);
        }
        return ContextHelper;
    }

    protected static void releaseContext(String monitorId) {

        StandardCaptureContextHelper ContextHelper = init();

        ContextHelper.remove(monitorId);
    }

    private Map<String, CaptureContext> threadMonitorCaptureContexts = new ConcurrentHashMap<String, CaptureContext>();

    private CaptureContext get(String monitorId) {

        if (monitorId == null)
            return null;

        CaptureContext context = threadMonitorCaptureContexts.get(monitorId);

        if (context == null) {
            context = new StandardCaptureContext();
            threadMonitorCaptureContexts.put(monitorId, context);
        }

        return context;
    }

    private void remove(String monitorId) {

        if (monitorId == null)
            return;

        threadMonitorCaptureContexts.remove(monitorId);
    }
}
