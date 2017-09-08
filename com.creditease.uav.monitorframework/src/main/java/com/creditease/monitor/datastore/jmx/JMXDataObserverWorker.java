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

package com.creditease.monitor.datastore.jmx;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.monitor.datastore.spi.DataObserverWorker;
import com.creditease.uav.profiling.spi.Profile;
import com.creditease.uav.util.MonitorServerUtil;

public class JMXDataObserverWorker implements DataObserverWorker {

    private final static String domain = "com.creditease";

    private MBeanServer server;

    private Map<ObjectName, Monitor> monitorMBeanMap = new HashMap<ObjectName, Monitor>();
    private Map<ObjectName, Profile> profileMBeanMap = new HashMap<ObjectName, Profile>();

    @Override
    public void start() {

        // get MBeanServer
        if (server == null) {
            server = MonitorServerUtil.getMBeanServer();
        }
    }

    @Override
    public void stop() {

        for (Entry<ObjectName, Monitor> entry : monitorMBeanMap.entrySet()) {
            try {
                server.unregisterMBean(entry.getKey());
            }
            catch (Exception e) {
                // ignore
            }
        }

        monitorMBeanMap.clear();

        for (Entry<ObjectName, Profile> entry : profileMBeanMap.entrySet()) {
            try {
                server.unregisterMBean(entry.getKey());
            }
            catch (Exception e) {
                // ignore
            }
        }

        profileMBeanMap.clear();
    }

    @Override
    public void installMonitor(Monitor monitor) {

        try {
            ObjectName monitorMBeanName = new ObjectName(
                    domain + ":feature=monitors,class=appserver,id=" + monitor.getId());

            if (server.isRegistered(monitorMBeanName)) {
                this.uninstallMonitor(monitor);
            }
            MonitorObserver monitorMBean = new MonitorObserver(monitor);

            server.registerMBean(monitorMBean, monitorMBeanName);
            monitorMBeanMap.put(monitorMBeanName, monitor);
        }
        catch (Exception e) {
            // ignore
        }
    }

    @Override
    public void uninstallMonitor(Monitor monitor) {

        ObjectName monitorMBeanName;
        try {
            monitorMBeanName = new ObjectName(domain + ":feature=monitors,class=appserver,id=" + monitor.getId());
            server.unregisterMBean(monitorMBeanName);
            monitorMBeanMap.remove(monitorMBeanName);
        }
        catch (Exception e) {
            // ignore
        }

    }

    @Override
    public void installProfile(Profile profile) {

        try {
            ObjectName profileMBeanName = new ObjectName(
                    domain + ":feature=profiles,class=appserver,id=" + profile.getId());

            if (server.isRegistered(profileMBeanName)) {
                this.uninstallProfile(profile);
            }
            ProfileObserver monitorMBean = new ProfileObserver(profile);

            server.registerMBean(monitorMBean, profileMBeanName);
            profileMBeanMap.put(profileMBeanName, profile);
        }
        catch (Exception e) {
            // ignore
        }
    }

    @Override
    public void uninstallProfile(Profile profile) {

        ObjectName profileMBeanName;
        try {
            profileMBeanName = new ObjectName(domain + ":feature=profiles,class=appserver,id=" + profile.getId());
            server.unregisterMBean(profileMBeanName);
            profileMBeanMap.remove(profileMBeanName);
        }
        catch (Exception e) {
            // ignore
        }
    }

    @Override
    public Profile getProfile(String profileId) {

        try {
            ObjectName profileMBeanName = new ObjectName(domain + ":feature=profiles,class=appserver,id=" + profileId);

            if (profileMBeanMap.containsKey(profileMBeanName)) {
                return profileMBeanMap.get(profileMBeanName);
            }
        }
        catch (Exception e) {
            // ignore
        }

        return null;
    }

    @Override
    public Monitor getMonitor(String monitorId) {

        ObjectName monitorMBeanName;
        try {
            monitorMBeanName = new ObjectName(domain + ":feature=monitors,class=appserver,id=" + monitorId);

            if (monitorMBeanMap.containsKey(monitorMBeanName)) {
                return monitorMBeanMap.get(monitorMBeanName);
            }
        }
        catch (Exception e) {
            // ignore
        }

        return null;
    }

    @Override
    public Collection<Profile> getProfiles() {

        return this.profileMBeanMap.values();
    }

}
