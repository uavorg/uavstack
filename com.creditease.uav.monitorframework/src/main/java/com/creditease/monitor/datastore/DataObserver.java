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

package com.creditease.monitor.datastore;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.monitor.datastore.http.HttpDataObserverWorker;
import com.creditease.monitor.datastore.jmx.JMXDataObserverWorker;
import com.creditease.monitor.datastore.spi.DataObserverListener;
import com.creditease.monitor.datastore.spi.DataObserverWorker;
import com.creditease.uav.profiling.spi.Profile;

public class DataObserver {

    private static DataObserver instance = null;

    public static DataObserver instance() {

        if (instance == null) {
            instance = new DataObserver();
        }
        return instance;
    }

    public enum WorkModel {
        JMX, HTTP
    }

    private WorkModel curWorkModel;
    private DataObserverWorker worker = null;
    private Map<String, DataObserverListener> doListeners = new ConcurrentHashMap<String, DataObserverListener>();

    public void start(WorkModel workModel) {

        this.curWorkModel = workModel;
        switch (this.curWorkModel) {
            case JMX:
                worker = new JMXDataObserverWorker();
                break;
            case HTTP:
                worker = new HttpDataObserverWorker();
                break;
            default:
                break;
        }
        if (worker != null) {
            worker.start();
        }
    }

    public void installMonitor(Monitor monitor) {

        if (worker != null)
            worker.installMonitor(monitor);
    }

    public void uninstallMonitor(Monitor monitor) {

        if (worker != null)
            worker.uninstallMonitor(monitor);
    }

    public void installProfile(Profile profile) {

        if (worker != null) {
            worker.installProfile(profile);
        }
    }

    public void uninstallProfile(Profile profile) {

        if (worker != null) {
            worker.uninstallProfile(profile);
        }
    }

    public void stop() {

        if (worker != null) {
            worker.stop();
        }
    }

    /**
     * 添加DataObserverListener
     * 
     * @param listener
     */
    public void addListener(DataObserverListener listener) {

        if (null == listener) {
            return;
        }

        doListeners.put(listener.getClass().getName(), listener);
    }

    /**
     * 删除DataObserverListener
     * 
     * @param listener
     */
    public void removeListener(DataObserverListener listener) {

        if (null == listener) {
            return;
        }

        doListeners.remove(listener.getClass().getName());
    }

    /**
     * 获取DataObserverListeners
     * 
     * @return
     */
    public Collection<DataObserverListener> getListeners() {

        return Collections.unmodifiableCollection(doListeners.values());
    }

    /**
     * getProfile
     * 
     * @param profileId
     * @return
     */
    public Profile getProfile(String profileId) {

        if (this.worker != null) {
            return this.worker.getProfile(profileId);
        }

        return null;
    }

    public Collection<Profile> getProfiles() {

        if (this.worker != null) {
            return this.worker.getProfiles();
        }

        return Collections.emptyList();
    }

    /**
     * getMonitor
     * 
     * @param mointorId
     * @return
     */
    public Monitor getMonitor(String mointorId) {

        if (this.worker != null) {
            return this.worker.getMonitor(mointorId);
        }

        return null;
    }
}
