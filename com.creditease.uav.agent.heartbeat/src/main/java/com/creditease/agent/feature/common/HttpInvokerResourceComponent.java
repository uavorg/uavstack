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

package com.creditease.agent.feature.common;

import com.creditease.agent.spi.AbstractSystemInvoker;
import com.creditease.agent.spi.AgentResourceComponent;

public class HttpInvokerResourceComponent extends AgentResourceComponent {

    @SuppressWarnings("rawtypes")
    private AbstractSystemInvoker invoker = null;

    public HttpInvokerResourceComponent(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public Object initResource() {

        // http invoker
        invoker = new HttpSystemInvoker(this.cName + "-res", feature);

        invoker.start();

        // new NodeOperActionEngine
        this.getActionEngineMgr().newActionEngine("NodeOperActionEngine", feature);

        return invoker;
    }

    @Override
    public Object getResource() {

        return invoker;
    }

    @Override
    public void releaseResource() {

        // shutdown NodeOperActionEngine
        this.getActionEngineMgr().shutdown("NodeOperActionEngine");

        super.releaseResource();
    }

}
