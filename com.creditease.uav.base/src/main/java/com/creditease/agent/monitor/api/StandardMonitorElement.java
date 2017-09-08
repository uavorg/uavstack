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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StandardMonitorElement {

    private final String monitorElemId;
    private final String captureId;
    private String capClass;
    private final Map<String, StandardMonitorElementInstance> instances = new ConcurrentHashMap<String, StandardMonitorElementInstance>();
    private boolean enable = true;

    public StandardMonitorElement(String monitorElemId, String captureId, String capClass) {
        this.monitorElemId = monitorElemId;
        this.captureId = captureId;
        this.capClass = capClass;
    }

    public String getMonitorElemId() {

        return monitorElemId;
    }

    public String getCaptureId() {

        return captureId;
    }

    public String getCapClass() {

        return capClass;
    }

    public StandardMonitorElementInstance getInstance(String instanceId) {

        if (instanceId == null)
            return null;

        StandardMonitorElementInstance instance = instances.get(instanceId);

        if (instance == null) {

            synchronized (instances) {

                instance = instances.get(instanceId);
                if (instance == null) {
                    instance = new StandardMonitorElementInstance(instanceId, this);
                    this.instances.put(instanceId, instance);
                }

            }
        }

        return instance;
    }

    public StandardMonitorElementInstance[] getInstances() {

        StandardMonitorElementInstance[] array = new StandardMonitorElementInstance[instances.values().size()];
        return instances.values().toArray(array);
    }

    public String toJSONString() {

        StringBuilder sb = new StringBuilder("{");

        sb.append("MEId:\"" + this.monitorElemId + "\",");
        sb.append("Instances:[");
        StandardMonitorElementInstance[] insts = getInstances();
        for (int i = 0; i < insts.length; i++) {
            sb.append(insts[i].toJSONString());

            if (i < insts.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.append("}").toString();
    }

    public void destroy() {

        for (StandardMonitorElementInstance instance : instances.values()) {
            instance.destroy();
        }

        this.instances.clear();
    }

    public boolean isEnabled() {

        return enable;
    }

    public void setEnable(boolean check) {

        this.enable = check;
    }

    public void setCapClass(String capClass) {

        this.capClass = capClass;
    }
}
