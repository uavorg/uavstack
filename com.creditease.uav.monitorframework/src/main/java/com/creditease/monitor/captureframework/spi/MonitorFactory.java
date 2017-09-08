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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.creditease.monitor.captureframework.StandardMonitor;
import com.creditease.monitor.captureframework.spi.Monitor.CapturePhase;

public class MonitorFactory {

    private static MonitorFactory instance = new MonitorFactory();

    public static MonitorFactory instance() {

        return instance;
    }

    private Map<String, Monitor> monitors = new ConcurrentHashMap<String, Monitor>();
    private Map<String, Map<String, Monitor>> serverCapPointBindMonitors = new ConcurrentHashMap<String, Map<String, Monitor>>();

    /**
     * build default monitor
     * 
     * @param configFile
     * @return
     */
    public Monitor buildDefaultMonitor(String configFile) {

        return buildMonitor(CaptureConstants.MONITOR_SERVER, configFile);
    }

    /**
     * build application monitor
     * 
     * @param monitorId
     * @param configFile
     *            TODO: configFile is not used yet, only API avaiable
     * @return
     */
    public Monitor buildMonitor(String monitorId, String configFile) {

        if (monitorId == null)
            return null;

        if (monitors.containsKey(monitorId)) {
            monitors.get(monitorId).destroy();
        }

        Monitor mInst = new StandardMonitor(monitorId, configFile);

        monitors.put(monitorId, mInst);

        return mInst;
    }

    /**
     * get default monitor
     * 
     * @return
     */
    public Monitor getDefaultMonitor() {

        return getMonitor(CaptureConstants.MONITOR_SERVER);
    }

    /**
     * get application monitor
     * 
     * @param id
     * @return
     */
    public Monitor getMonitor(String id) {

        if (!monitors.containsKey(id))
            return null;

        return monitors.get(id);
    }

    /**
     * destroy default monitor
     */
    public void destroyDefaultMonitor() {

        destroyMonitor(CaptureConstants.MONITOR_SERVER);
    }

    /**
     * destroy monitor
     * 
     * @param id
     */
    public void destroyMonitor(String id) {

        if (id == null)
            return;

        Monitor m = getMonitor(id);
        if (m != null) {
            m.destroy();
        }

        monitors.remove(id);
    }

    /**
     * bind monitor instance to Server Capture Point
     * 
     * @param captureId
     * @param monitor
     * @param capturePhase
     */
    public void bindMonitorToServerCapPoint(String captureId, Monitor monitor, CapturePhase capturePhase) {

        if (captureId == null || "".equals(captureId) || monitor == null)
            return;
        String capKey = getCapKey(captureId, capturePhase);

        Map<String, Monitor> bindMonitors = serverCapPointBindMonitors.get(capKey);

        if (bindMonitors == null) {
            synchronized (serverCapPointBindMonitors) {

                bindMonitors = serverCapPointBindMonitors.get(capKey);

                if (bindMonitors == null) {
                    bindMonitors = new ConcurrentHashMap<String, Monitor>();
                    serverCapPointBindMonitors.put(capKey, bindMonitors);
                }
            }
        }

        bindMonitors.put(monitor.getId(), monitor);
    }

    private String getCapKey(String captureId, CapturePhase capturePhase) {

        // capkey is captureId + capture phase
        String capKey = captureId + "." + capturePhase;
        return capKey;
    }

    /**
     * get Server Capture Point bind monitors
     * 
     * @param captureId
     * @param phase
     * @return
     */
    public Monitor[] getServerCapPointBindMonitors(String captureId, CapturePhase phase) {

        if (captureId == null || "".equals(captureId))
            return new Monitor[0];

        String capKey = getCapKey(captureId, phase);
        Map<String, Monitor> bindMonitors = serverCapPointBindMonitors.get(capKey);
        if (bindMonitors != null && bindMonitors.size() > 0) {
            Monitor[] monitorArray = new Monitor[bindMonitors.size()];
            return bindMonitors.values().toArray(monitorArray);
        }

        return new Monitor[0];
    }

    /**
     * get all monitors
     * 
     * @return
     */
    public Monitor[] getMonitors() {

        if (monitors.size() > 0) {
            Monitor[] monitorsArray = new Monitor[monitors.size()];
            return monitors.values().toArray(monitorsArray);
        }

        return new Monitor[0];
    }
}
