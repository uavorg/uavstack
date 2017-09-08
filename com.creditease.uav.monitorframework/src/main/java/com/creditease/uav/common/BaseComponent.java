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

package com.creditease.uav.common;

import com.creditease.monitor.UAVServer;
import com.creditease.monitor.log.Logger;

public abstract class BaseComponent {

    protected Logger logger = UAVServer.instance().getLog();

    public Object getServerInfo(String key) {

        return UAVServer.instance().getServerInfo(key);
    }

    /**
     * get supporter by class name
     * 
     * @param name
     * @return
     */
    protected Supporter getSupporter(String name) {

        return UAVServer.instance().getSupportor(name);
    }

    /**
     * get supporter by special class type
     * 
     * @param cls
     * @return
     */
    @SuppressWarnings("unchecked")
    protected <V extends Supporter> V getSupporter(Class<V> cls) {

        return (V) getSupporter(cls.getName());
    }
}
