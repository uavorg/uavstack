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

import java.util.Map;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.monitor.captureframework.spi.MonitorElement;
import com.creditease.monitor.captureframework.spi.MonitorElementInstance;
import com.creditease.uav.util.LimitConcurrentHashMap;

public class StandardMonitorElement implements MonitorElement {

    private final String monitorElemId;
    private final String captureId;
    private String capClass;
    private final Map<String, MonitorElementInstance> instances;
    private boolean enable = true;
    private final Monitor monitor;

    public StandardMonitorElement(String monitorElemId, String captureId, String capClass, Monitor monitor) {
        this.monitorElemId = monitorElemId;
        this.captureId = captureId;
        this.capClass = capClass;
        this.monitor = monitor;
        int instLimitCount = DataConvertHelper.toInt(System.getProperty("com.creditease.uav.monitor.eleminst.limit"),
                80);
        instances = new LimitConcurrentHashMap<String, MonitorElementInstance>(instLimitCount);
    }

    @Override
    public String getMonitorElemId() {

        return monitorElemId;
    }

    @Override
    public String getCaptureId() {

        return captureId;
    }

    @Override
    public String getCapClass() {

        return capClass;
    }

    @Override
    public MonitorElementInstance getInstance(String instanceId) {

        if (instanceId == null)
            return null;

        MonitorElementInstance instance = instances.get(instanceId);

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

    @Override
    public MonitorElementInstance[] getInstances() {

        MonitorElementInstance[] array = new MonitorElementInstance[instances.values().size()];
        return instances.values().toArray(array);
    }

    @Override
    public String toJSONString() {

        StringBuilder sb = new StringBuilder("{");

        sb.append("MEId:\"" + this.monitorElemId + "\",");
        sb.append("Instances:[");
        MonitorElementInstance[] insts = getInstances();
        for (int i = 0; i < insts.length; i++) {
            sb.append(insts[i].toJSONString());

            if (i < insts.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.append("}").toString();
    }

    @Override
    public void destroy() {

        for (MonitorElementInstance instance : instances.values()) {
            instance.destroy();
        }

        this.instances.clear();
    }

    @Override
    public boolean isEnabled() {

        return enable;
    }

    @Override
    public void setEnable(boolean check) {

        this.enable = check;
    }

    @Override
    public void setCapClass(String capClass) {

        this.capClass = capClass;
    }

    @Override
    public Monitor getMonitor() {

        return this.monitor;
    }

    @Override
    public boolean existInstance(String instanceId) {

        return this.instances.containsKey(instanceId);
    }
}
