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

package com.creditease.agent.feature.logagent.objects;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
* @author peihua 
* 
*/

public class LogDataElement {

    // Input
    private String logId;

    // Output
    @SuppressWarnings("rawtypes")
    private List<Map> instanceList = new ArrayList<Map>();

    public LogDataElement(String logId) {
        this.logId = logId;
    }

    @SuppressWarnings("rawtypes")
    public void addLogElementList(List<Map> ilist) {

        List<Map> instance = new ArrayList<Map>();
        Map<String, Object> instanceMap = new LinkedHashMap<String, Object>();
        Map<String, Object> valueMap = new LinkedHashMap<String, Object>();
        Map<String, List<Map>> InstanceContentMap = new LinkedHashMap<String, List<Map>>();
        InstanceContentMap.put("content", ilist);

        valueMap.put("id", this.logId);
        valueMap.put("values", InstanceContentMap);

        instance.add(valueMap);
        instanceMap.put("MEId", "log");
        instanceMap.put("Instances", instance);

        instanceList.add(instanceMap);
    }

    @SuppressWarnings("rawtypes")
    public List<Map> getReturnInstLists() {

        return instanceList;
    }

}
