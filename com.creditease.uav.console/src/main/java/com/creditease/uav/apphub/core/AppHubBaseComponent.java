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

package com.creditease.uav.apphub.core;

import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;

/**
 * app hub framework的通用父类组件，提供通用功能
 * 
 * @author zhen zhang
 *
 */
public abstract class AppHubBaseComponent {

    /**
     * main log
     */
    protected ISystemLogger logger = null;

    public AppHubBaseComponent() {
        logger = getLogger();
    }

    public ISystemLogger getLogger() {

        return SystemLogger.getLogger(AppHubBaseComponent.class);
    }

}
