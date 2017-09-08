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

package com.creditease.agent.workqueue;

import java.util.Set;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.spi.AbstractSystemInvoker;
import com.creditease.agent.spi.ISystemInvokerMgr;

public class SystemInvokerManager implements ISystemInvokerMgr {

    @SuppressWarnings("rawtypes")
    @Override
    public AbstractSystemInvoker getSystemInvoker(InvokerType type) {

        AbstractSystemInvoker invoker = null;

        switch (type) {
            case HTTP:
            default:
                invoker = (AbstractSystemInvoker) ConfigurationManager.getInstance().getComponentResource("httpinvoke",
                        "HttpInvokerResourceComponent");

                break;
        }

        return invoker;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void shutdown() {

        Set<AbstractSystemInvoker> abs = ConfigurationManager.getInstance().getComponents(AbstractSystemInvoker.class);

        for (AbstractSystemInvoker invoker : abs) {

            invoker.stop();

            ConfigurationManager.getInstance().unregisterComponent(invoker.getFeature(), invoker.getName());
        }
    }
}
