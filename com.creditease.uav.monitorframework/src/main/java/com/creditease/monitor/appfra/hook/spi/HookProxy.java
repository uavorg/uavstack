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

package com.creditease.monitor.appfra.hook.spi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.creditease.uav.common.BaseComponent;

@SuppressWarnings("rawtypes")
public abstract class HookProxy extends BaseComponent {

    protected final String id;

    protected Map<String, Boolean> isHookEventDone = new HashMap<String, Boolean>();

    protected final Map config;

    public HookProxy(String id, Map config) {
        this.id = id;
        this.config = config;
    }

    public String getId() {

        return this.id;
    }

    public Map getConfig() {

        return this.config;
    }

    public Map getAdapts() {

        Map a = (Map) this.config.get("adapts");

        if (a != null) {
            return a;
        }

        return Collections.emptyMap();
    }

    public abstract void start(HookContext context, ClassLoader webapploader);

    public abstract void stop(HookContext context, ClassLoader webapploader);

    /**
     * wish to run something for hookproxy
     * 
     * @param context
     */
    public void run(HookContext context) {

        // not implementation
    }

    /**
     * check if wish to run
     * 
     * @param context
     * @return
     */
    public boolean isRun(HookContext context) {

        return false;
    }

    /**
     * check if hook event is done
     * 
     * @param event
     * @return
     */
    public boolean isHookEventDone(String event) {

        Boolean result = isHookEventDone.get(event);
        isHookEventDone.put(event, true);
        return (result == null) ? false : result;
    }
}
