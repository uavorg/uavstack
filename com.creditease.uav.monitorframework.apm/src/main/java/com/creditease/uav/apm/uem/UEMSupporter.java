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

package com.creditease.uav.apm.uem;

import com.creditease.monitor.interceptframework.InterceptSupport;
import com.creditease.uav.appserver.listeners.GlobalFilterDispatchListener;
import com.creditease.uav.common.Supporter;

/**
 * 
 * UEMSupporter description: User Experience Management
 *
 */
public class UEMSupporter extends Supporter {

    @Override
    public void start() {

        // new UEM DataLoggerManager
        this.newDataLoggerManager("uem", "com.creditease.uav.uem");

        // register UEMGlobalFilterHandler
        GlobalFilterDispatchListener listener = (GlobalFilterDispatchListener) InterceptSupport.instance()
                .getEventListener(GlobalFilterDispatchListener.class);

        listener.registerHandler(new UEMHookJSGHHandler("UEMHookJSGHHandler"));
        listener.registerHandler(new UEMRewritePageGHHandler("UEMRewritePageGHHandler"));
        listener.registerHandler(new UEMServiceGFHandler("UEMServiceGFHandler"));
    }

    @Override
    public void stop() {

        GlobalFilterDispatchListener listener = (GlobalFilterDispatchListener) InterceptSupport.instance()
                .getEventListener(GlobalFilterDispatchListener.class);

        listener.unregisterHandler("UEMRewritePageGHHandler");
        listener.unregisterHandler("UEMServiceGFHandler");
        listener.unregisterHandler("UEMHookJSGHHandler");

        super.stop();
    }

}
