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

package com.creditease.monitor.datastore.http;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.creditease.agent.helpers.StringHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.UAVServer.ServerVendor;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.monitor.datastore.jmx.MonitorObserver;
import com.creditease.monitor.datastore.jmx.MonitorObserverMBean;
import com.creditease.monitor.datastore.jmx.ProfileObserver;
import com.creditease.monitor.datastore.jmx.ProfileObserverMBean;
import com.creditease.monitor.datastore.spi.DataObserverWorker;
import com.creditease.monitor.interceptframework.InterceptSupport;
import com.creditease.uav.appserver.listeners.GlobalFilterDispatchListener;
import com.creditease.uav.profiling.spi.Profile;

public class HttpDataObserverWorker implements DataObserverWorker {

    protected Map<String, Monitor> monitorMap = new HashMap<String, Monitor>();
    protected Map<String, MonitorObserverMBean> monitorMBeanMap = new HashMap<String, MonitorObserverMBean>();
    protected Map<String, Profile> profileMap = new HashMap<String, Profile>();
    protected Map<String, ProfileObserverMBean> profileMBeanMap = new HashMap<String, ProfileObserverMBean>();

    @Override
    public void start() {

        GlobalFilterDispatchListener listener = (GlobalFilterDispatchListener) InterceptSupport.instance()
                .getEventListener(GlobalFilterDispatchListener.class);

        // register Observer to GlobalFilterDispatchListener
        ServerVendor sv = (ServerVendor) UAVServer.instance().getServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR);

        /**
         * MSCP
         */
        if (sv == ServerVendor.MSCP) {
            // TODO
        }
        /**
         * JEE
         */
        else {
            listener.registerHandler(new HttpJEEJVMObserver("HttpJEEJVMObserver"));
            listener.registerHandler(new HttpJEEMonitorObserver("HttpJEEMonitorObserver", this));
            listener.registerHandler(new HttpJEEProfileObserver("HttpJEEProfileObserver", this));
        }
    }

    @Override
    public void stop() {

        GlobalFilterDispatchListener listener = (GlobalFilterDispatchListener) InterceptSupport.instance()
                .getEventListener(GlobalFilterDispatchListener.class);

        // unregister Observer to GlobalFilterDispatchListener
        ServerVendor sv = (ServerVendor) UAVServer.instance().getServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR);

        /**
         * MSCP
         */
        if (sv == ServerVendor.MSCP) {

        }
        /**
         * JEE
         */
        else {
            listener.unregisterHandler("HttpJEEJVMObserver()");
            listener.unregisterHandler("HttpJEEMonitorObserver");
            listener.unregisterHandler("HttpJEEProfileObserver");
        }
        monitorMap.clear();
        monitorMBeanMap.clear();
        profileMap.clear();
        profileMBeanMap.clear();
    }

    @Override
    public void installMonitor(Monitor monitor) {

        if (monitor == null) {
            return;
        }

        monitorMap.put(monitor.getId(), monitor);

        MonitorObserverMBean mbeab = new MonitorObserver(monitor);

        monitorMBeanMap.put(monitor.getId(), mbeab);
    }

    @Override
    public void uninstallMonitor(Monitor monitor) {

        if (monitor == null) {
            return;
        }

        monitorMap.remove(monitor.getId());

        monitorMBeanMap.remove(monitor.getId());
    }

    @Override
    public void installProfile(Profile profile) {

        if (profile == null) {
            return;
        }

        profileMap.put(profile.getId(), profile);

        ProfileObserverMBean mbean = new ProfileObserver(profile);

        profileMBeanMap.put(profile.getId(), mbean);
    }

    @Override
    public void uninstallProfile(Profile profile) {

        if (profile == null) {
            return;
        }
        profileMap.remove(profile.getId());
        profileMBeanMap.remove(profile.getId());
    }

    @Override
    public Profile getProfile(String profileId) {

        if (StringHelper.isEmpty(profileId)) {
            return null;
        }

        return profileMap.get(profileId);
    }

    @Override
    public Monitor getMonitor(String monitorId) {

        if (StringHelper.isEmpty(monitorId)) {
            return null;
        }

        return monitorMap.get(monitorId);
    }

    protected Map<String, MonitorObserverMBean> getMonitorMBeans() {

        return Collections.unmodifiableMap(this.monitorMBeanMap);
    }

    protected Map<String, ProfileObserverMBean> getProfileMBeans() {

        return Collections.unmodifiableMap(this.profileMBeanMap);
    }

    @Override
    public Collection<Profile> getProfiles() {

        return this.profileMap.values();
    }

}
