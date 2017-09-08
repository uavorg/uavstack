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

public interface MonitorRepository {

    /**
     * add a new monitor element to a specific capture id
     * 
     * @param monitorElemId
     * @param captureId
     * @param capClass
     */
    public void addElement(String monitorElemId, String captureId, String capClass);

    /**
     * get all monitor elements
     * 
     * @return
     */
    public MonitorElement[] getElements();

    /**
     * get monitor element by capture id
     * 
     * @param captureId
     * @return
     */
    public MonitorElement[] getElementByCapId(String captureId);

    /**
     * get monitor element by capture id and monitor element id
     * 
     * @param moElemId
     * @param captureId
     * @return
     */
    public MonitorElement[] getElementByMoElemIdAndCapId(String moElemId, String captureId);

    /**
     * marshal to JSON string
     * 
     * @return
     */
    public String toJSONString();

    /**
     * destroy monitor repository
     */
    public void destroy();
}
