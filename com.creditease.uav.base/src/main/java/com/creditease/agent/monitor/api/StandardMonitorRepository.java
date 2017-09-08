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

package com.creditease.agent.monitor.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StandardMonitorRepository {

    private final Map<String, StandardMonitorElement> elemsMap = new ConcurrentHashMap<String, StandardMonitorElement>();

    public StandardMonitorRepository() {
    }

    public StandardMonitorElement[] getElements() {

        StandardMonitorElement[] array = new StandardMonitorElement[elemsMap.values().size()];
        return elemsMap.values().toArray(array);
    }

    public StandardMonitorElement[] getElementByCapId(String CaptureId) {

        List<StandardMonitorElement> list = new ArrayList<StandardMonitorElement>();
        Collection<StandardMonitorElement> elems = elemsMap.values();
        for (StandardMonitorElement elem : elems) {
            if (elem.getCaptureId().equals(CaptureId)) {
                list.add(elem);
            }
        }
        StandardMonitorElement[] array = new StandardMonitorElement[list.size()];
        return list.toArray(array);
    }

    public String toJSONString() {

        StringBuilder sb = new StringBuilder("[");
        StandardMonitorElement[] elems = getElements();
        for (int i = 0; i < elems.length; i++) {
            sb.append(elems[i].toJSONString());

            if (i < elems.length - 1) {
                sb.append(",");
            }
        }
        return sb.append("]").toString();
    }

    public void destroy() {

        for (StandardMonitorElement elem : elemsMap.values()) {
            elem.destroy();
        }

        elemsMap.clear();
    }

    public StandardMonitorElement getElement(String monitorElemId, String captureId, String capClass) {

        if (captureId == null || monitorElemId == null || capClass == null) {
            return null;
        }

        String elemKey = monitorElemId + ":" + captureId + ":" + capClass;

        return elemsMap.get(elemKey);
    }

    public void addElement(String monitorElemId, String captureId, String capClass) {

        if (captureId == null || monitorElemId == null || capClass == null) {
            return;
        }

        String elemKey = monitorElemId + ":" + captureId + ":" + capClass;

        if (elemsMap.containsKey(elemKey)) {
            return;
        }

        StandardMonitorElement elem = new StandardMonitorElement(monitorElemId, captureId, capClass);

        elemsMap.put(elemKey, elem);
    }

    public StandardMonitorElement[] getElementByMoElemIdAndCapId(String moElemId, String captureId) {

        List<StandardMonitorElement> list = new ArrayList<StandardMonitorElement>();
        Collection<StandardMonitorElement> elems = elemsMap.values();
        for (StandardMonitorElement elem : elems) {
            if (elem.getCaptureId().equals(captureId) && elem.getMonitorElemId().equals(moElemId)) {
                list.add(elem);
            }
        }
        StandardMonitorElement[] array = new StandardMonitorElement[list.size()];
        return list.toArray(array);
    }

}
