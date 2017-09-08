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

package com.creditease.monitor.captureframework;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.creditease.agent.helpers.ReflectHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.repository.StandardMonitorRepository;
import com.creditease.monitor.captureframework.spi.CaptureContext;
import com.creditease.monitor.captureframework.spi.Monitor;
import com.creditease.monitor.captureframework.spi.MonitorElemCapHandler;
import com.creditease.monitor.captureframework.spi.MonitorElement;
import com.creditease.monitor.captureframework.spi.MonitorElementInstance;
import com.creditease.monitor.captureframework.spi.MonitorRepository;
import com.creditease.monitor.log.Logger;

/**
 * @author zhen zhang
 */
public class StandardMonitor implements Monitor {

    private final String id;
    private final MonitorRepository mr;
    private final Map<String, MonitorElemCapHandler> handlers = new ConcurrentHashMap<String, MonitorElemCapHandler>();
    private final Logger log;

    public StandardMonitor(String id, String configFile) {
        this.id = id;
        this.mr = new StandardMonitorRepository(this);
        this.log = UAVServer.instance().getLog();
    }

    @Override
    public void doCapture(String captureId, CaptureContext context, CapturePhase capPhase) {

        if (captureId == null) {
            return;
        }

        // step 1: see if it is a valid captureId
        MonitorElement[] elems = mr.getElementByCapId(captureId);

        for (MonitorElement elem : elems) {

            // if the monitor element is not enabled, just skip it
            if (elem.isEnabled() == false)
                continue;

            // step 2: get capture class
            String capClassName = elem.getCapClass();

            // step 3: check if there is one handler exists, if not new one
            MonitorElemCapHandler caphandler = selectHandler(capClassName);

            // step 4: invoke handler
            if (caphandler != null) {

                try {
                    invokeCaphandler(context, capPhase, elem, caphandler);
                }
                catch (Exception e) {
                    log.error("captureHandler[" + capClassName + "] execution [" + capPhase + "] fails ", e);
                    // ignore
                }

            }
        }
    }

    /**
     * @param context
     * @param capPhase
     * @param elem
     * @param caphandler
     */
    private void invokeCaphandler(CaptureContext context, CapturePhase capPhase, MonitorElement elem,
            MonitorElemCapHandler caphandler) {

        if (capPhase == CapturePhase.PRECAP) {
            caphandler.preCap(elem, context);
        }
        else if (capPhase == CapturePhase.DOCAP) {
            caphandler.doCap(elem, context);
        }
    }

    @SuppressWarnings("unused")
    private MonitorElemCapHandler selectHandler(String capClassName) {

        MonitorElemCapHandler caphandler = handlers.get(capClassName);

        if (caphandler == null) {

            synchronized (handlers) {

                if (caphandler == null) {

                    caphandler = newInstance(capClassName);
                    if (caphandler != null) {
                        handlers.put(capClassName, caphandler);
                    }
                }
                else {
                    caphandler = handlers.get(capClassName);
                }
            }
        }

        return caphandler;
    }

    @Override
    public MonitorRepository getRepository() {

        return this.mr;
    }

    @Override
    public String getId() {

        return this.id;
    }

    @Override
    public void destroy() {

        handlers.clear();

        this.mr.destroy();
    }

    @Override
    public CaptureContext getCaptureContext() {

        CaptureContext context = StandardCaptureContextHelper.getContext(this.id);

        return context;
    }

    @Override
    public void doPreStore() {

        MonitorRepository cmr = getRepository();
        MonitorElement[] elems = cmr.getElements();
        for (MonitorElement elem : elems) {
            MonitorElemCapHandler caphandler = selectHandler(elem.getCapClass());

            if (caphandler == null)
                continue;

            MonitorElementInstance[] instances = elem.getInstances();
            for (MonitorElementInstance instance : instances) {
                try {
                    caphandler.preStore(instance);
                }
                catch (Exception e) {
                    log.error("captureHandler[" + caphandler.getClass().getName() + "] execution [preStore] fails ", e);
                    // ignore
                }
            }
        }
    }

    private MonitorElemCapHandler newInstance(String capClassName) {

        MonitorElemCapHandler caphandler = (MonitorElemCapHandler) ReflectHelper.newInstance(capClassName,
                this.getClass().getClassLoader());
        return caphandler;
    }

    @Override
    public void releaseCaptureContext() {

        StandardCaptureContextHelper.releaseContext(this.id);
    }

    @Override
    public CaptureContext getCaptureContext(String contextTag) {

        CaptureContext context = StandardCaptureContextHelper.getContext(this.id + ":" + contextTag);

        return context;
    }

    @Override
    public void releaseCaptureContext(String contextTag) {

        StandardCaptureContextHelper.releaseContext(this.id + ":" + contextTag);
    }
}
