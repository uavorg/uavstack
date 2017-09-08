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

public interface Monitor {

    /**
     * PRECAP before the real capture process DOCAP do the real capture process
     * 
     * @author 201506110096
     *
     */
    public enum CapturePhase {
        PRECAP, DOCAP
    }

    /**
     * do PRECAP/CAP action at a specific capture point
     * 
     * @param captureId
     * @param context
     * @param capPhase
     */
    public void doCapture(String captureId, CaptureContext context, CapturePhase capPhase);

    /**
     * do capture action before store data
     */
    public void doPreStore();

    /**
     * get monitor repository
     * 
     * @return
     */
    public MonitorRepository getRepository();

    /**
     * get monitor id
     * 
     * @return
     */
    public String getId();

    /**
     * destroy monitor instance
     */
    public void destroy();

    /**
     * get the capture context NOTE: do not NEW a stand-alone capture context
     * 
     * @return
     */
    public CaptureContext getCaptureContext();

    /**
     * release capture context when everything is done NOTE: this is important
     */
    public void releaseCaptureContext();

    /**
     * if in one thread, there need more than one context, we need context tag for different usage of one monitor
     * 
     * @param contextTag
     * @return
     */
    public CaptureContext getCaptureContext(String contextTag);

    public void releaseCaptureContext(String contextTag);
}
