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

package com.creditease.uav.feature.upgrade.download;

import java.util.HashMap;
import java.util.Map;

import com.creditease.agent.http.api.UAVHttpMessage;

public class UpgradeHttpMessage extends UAVHttpMessage {

    public UpgradeHttpMessage() {

    }

    private Map<String, Object> objectParamMap = new HashMap<String, Object>();

    public Object getObjectParam(String key) {

        return objectParamMap.get(key);
    }

    public void putObjectParam(String key, Object obj) {

        objectParamMap.put(key, obj);
    }
}
