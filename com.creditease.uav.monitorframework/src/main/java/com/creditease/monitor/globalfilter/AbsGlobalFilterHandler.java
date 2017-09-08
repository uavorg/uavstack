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

package com.creditease.monitor.globalfilter;

import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.uav.common.BaseComponent;

/**
 * 
 * AbsGlobalFilterHandler description: Generic abstract GlobalFilterHandler, parent class of all GlobalFilterHandlers
 *
 * @param <K>
 * @param <V>
 */
public abstract class AbsGlobalFilterHandler<K, V> extends BaseComponent {

    protected int order = -1;

    protected String id;

    public AbsGlobalFilterHandler(String id) {
        this.id = id;
    }

    public abstract String getContext();

    public abstract void handle(K request, V response, InterceptContext ic);

    /**
     * by default, the GlobalFilterHandler will block the handler chain, that means only one handler executed. if the
     * return value = false, then other handlers will also be executed util one of them block the handler chain as its
     * isBlockHandlerChain return true
     * 
     * @return
     */
    public boolean isBlockHandlerChain() {

        return true;
    }

    /**
     * by default, the MOF service handler will block the filter chain, that means all back end services will not be
     * accessed. sometimes MOF service handler may expose as intercepter to handle some cases on the application's
     * request, then the return value should be false
     * 
     * @return
     */
    public boolean isBlockFilterChain() {

        return true;
    }

    public int getOrder() {

        return this.order;
    }

    public void setOrder(int order) {

        this.order = order;
    }

    public String getId() {

        return this.id;
    }
}
