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

package com.creditease.monitor.captureframework.spi;

public interface MonitorElement {

    /**
     * get monitor element id
     * 
     * @return
     */
    public String getMonitorElemId();

    /**
     * get capture id
     * 
     * @return
     */
    public String getCaptureId();

    /**
     * get capture implementation class implements MonitorElemCapHandler
     * 
     * @return
     */
    public String getCapClass();

    /**
     * get monitor element instance by instance id
     * 
     * @param instanceId
     * @return
     */
    public MonitorElementInstance getInstance(String instanceId);

    /**
     * check if exist the instance
     * 
     * @param instanceId
     * @return
     */
    public boolean existInstance(String instanceId);

    /**
     * get monitor element instances
     * 
     * @return
     */
    public MonitorElementInstance[] getInstances();

    /**
     * marshall data to JSON string
     * 
     * @return
     */
    public String toJSONString();

    /**
     * destroy monitor element & all its instances
     */
    public void destroy();

    /**
     * check if this monitor element is enabled
     * 
     * @return
     */
    public boolean isEnabled();

    /**
     * enable or disable the monitor element
     * 
     * @param check
     */
    public void setEnable(boolean check);

    /**
     * set capture class
     * 
     * @param capClass
     */
    public void setCapClass(String capClass);

    public Monitor getMonitor();
}
