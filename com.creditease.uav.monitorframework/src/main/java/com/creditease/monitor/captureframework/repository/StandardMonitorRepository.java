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

package com.creditease.monitor.captureframework.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.monitor.captureframework.spi.MonitorElement;
import com.creditease.monitor.captureframework.spi.MonitorRepository;

public class StandardMonitorRepository implements MonitorRepository {

    private final Map<String, MonitorElement> elemsMap = new ConcurrentHashMap<String, MonitorElement>();
    private final Monitor monitor;

    public StandardMonitorRepository(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public MonitorElement[] getElements() {

        MonitorElement[] array = new MonitorElement[elemsMap.values().size()];
        return elemsMap.values().toArray(array);
    }

    @Override
    public MonitorElement[] getElementByCapId(String CaptureId) {

        List<MonitorElement> list = new ArrayList<MonitorElement>();
        Collection<MonitorElement> elems = elemsMap.values();
        for (MonitorElement elem : elems) {
            if (elem.getCaptureId().equals(CaptureId)) {
                list.add(elem);
            }
        }
        MonitorElement[] array = new MonitorElement[list.size()];
        return list.toArray(array);
    }

    @Override
    public String toJSONString() {

        StringBuilder sb = new StringBuilder("[");
        MonitorElement[] elems = getElements();
        for (int i = 0; i < elems.length; i++) {
            sb.append(elems[i].toJSONString());

            if (i < elems.length - 1) {
                sb.append(",");
            }
        }
        return sb.append("]").toString();
    }

    @Override
    public void destroy() {

        for (MonitorElement elem : elemsMap.values()) {
            elem.destroy();
        }

        elemsMap.clear();
    }

    @Override
    public void addElement(String monitorElemId, String captureId, String capClass) {

        if (captureId == null || monitorElemId == null || capClass == null) {
            return;
        }

        String elemKey = monitorElemId + ":" + captureId + ":" + capClass;

        MonitorElement elem = new StandardMonitorElement(monitorElemId, captureId, capClass, monitor);

        elemsMap.put(elemKey, elem);
    }

    @Override
    public MonitorElement[] getElementByMoElemIdAndCapId(String moElemId, String captureId) {

        List<MonitorElement> list = new ArrayList<MonitorElement>();
        Collection<MonitorElement> elems = elemsMap.values();
        for (MonitorElement elem : elems) {
            if (elem.getCaptureId().equals(captureId) && elem.getMonitorElemId().equals(moElemId)) {
                list.add(elem);
            }
        }
        MonitorElement[] array = new MonitorElement[list.size()];
        return list.toArray(array);
    }

}
