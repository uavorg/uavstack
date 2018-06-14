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

package com.creditease.monitor.handlers;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.creditease.agent.helpers.JVMToolHelper;
import com.creditease.agent.helpers.NetworkHelper;
import com.creditease.agent.helpers.ReflectionHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.CaptureContext;
import com.creditease.monitor.captureframework.spi.MonitorElemCapHandler;
import com.creditease.monitor.captureframework.spi.MonitorElement;
import com.creditease.monitor.captureframework.spi.MonitorElementInstance;

public class JVMStateCapHandler implements MonitorElemCapHandler {

    private static final Set<String> minorGC = new HashSet<String>();
    private static final Set<String> fullGC = new HashSet<String>();

    static {

        // Oracle (Sun) HotSpot
        // -XX:+UseSerialGC
        minorGC.add("Copy");
        // -XX:+UseParNewGC
        minorGC.add("ParNew");
        // -XX:+UseParallelGC
        minorGC.add("PS Scavenge");
        // Oracle (BEA) JRockit
        // -XgcPrio:pausetime
        minorGC.add("Garbage collection optimized for short pausetimes Young Collector");
        // -XgcPrio:throughput
        minorGC.add("Garbage collection optimized for throughput Young Collector");
        // -XgcPrio:deterministic
        minorGC.add("Garbage collection optimized for deterministic pausetimes Young Collector");
        // -XX:+UseG1GC
        minorGC.add("G1 Young Generation");
        
        // Oracle (Sun) HotSpot
        // -XX:+UseSerialGC
        fullGC.add("MarkSweepCompact");
        // -XX:+UseParallelGC and (-XX:+UseParallelOldGC or -XX:+UseParallelOldGCCompacting)
        fullGC.add("PS MarkSweep");
        // -XX:+UseConcMarkSweepGC
        fullGC.add("ConcurrentMarkSweep");
        // -XX:+UseG1GC
        fullGC.add("G1 Old Generation");

        // Oracle (BEA) JRockit
        // -XgcPrio:pausetime
        fullGC.add("Garbage collection optimized for short pausetimes Old Collector");
        // -XgcPrio:throughput
        fullGC.add("Garbage collection optimized for throughput Old Collector");
        // -XgcPrio:deterministic
        fullGC.add("Garbage collection optimized for deterministic pausetimes Old Collector");

    }

    private DecimalFormat formatter = new DecimalFormat("00.0");

    @Override
    public void preCap(MonitorElement elem, CaptureContext context) {

        // Do nothing but must pass sonar check
    }

    @Override
    public void doCap(MonitorElement elem, CaptureContext context) {

        if (CaptureConstants.MOELEM_JVMSTATE.equals(elem.getMonitorElemId())) {

            Integer port = (Integer) UAVServer.instance().getServerInfo(CaptureConstants.INFO_APPSERVER_LISTEN_PORT);

            String serverJVMid = "http://" + NetworkHelper.getLocalIP() + ":" + port;

            MonitorElementInstance instance = elem.getInstance(serverJVMid);

            // heap
            readHeapUsage(instance);

            // no heap
            readNonHeapUsage(instance);

            // thread
            readThreadUsage(instance);

            // heap & code mem pool
            readHeapPoolUsage(instance);

            // gc
            readGCUsage(instance);

            // classes usage
            readClassLoadUsage(instance);

            // cpu
            readCPUUsage(instance);

            // read customized metrics
            readCustomizedMetrics(instance);

        }
    }

    /**
     * read Customized Metrics
     * 
     * @param instance
     */
    private void readCustomizedMetrics(MonitorElementInstance instance) {

        Enumeration<?> enumeration = System.getProperties().propertyNames();

        while (enumeration.hasMoreElements()) {

            String name = (String) enumeration.nextElement();

            int moIndex = name.indexOf("mo@");

            if (moIndex != 0) {
                continue;
            }

            String[] metrics = name.split("@");

            // add monitor data
            String metricValStr = System.getProperties().getProperty(name);

            try {
                Double d = Double.parseDouble(metricValStr);

                instance.setValue(metrics[1], d);
            }
            catch (Exception e) {
                // ignore
            }
        }
    }

    private void readGCUsage(MonitorElementInstance instance) {

        List<GarbageCollectorMXBean> gcmbList = ManagementFactory.getGarbageCollectorMXBeans();

        for (GarbageCollectorMXBean gcmb : gcmbList) {

            String name = gcmb.getName();
            String gcName = null;
            if (minorGC.contains(name)) {
                gcName = "mgc";

            }
            else if (fullGC.contains(name)) {
                gcName = "fgc";
            }

            if (gcName == null) {
                continue;
            }

            instance.setValue(gcName + "_count", gcmb.getCollectionCount());
            instance.setValue(gcName + "_time", gcmb.getCollectionTime());
        }
    }

    private void readHeapPoolUsage(MonitorElementInstance instance) {

        List<MemoryPoolMXBean> pmbList = ManagementFactory.getMemoryPoolMXBeans();

        /*for (MemoryPoolMXBean mpmb : pmbList) {

            String jvmMemPoolName = getHeapPoolName(mpmb.getName().trim());
    
            MemoryUsage mu = mpmb.getUsage();
    
            instance.setValue(jvmMemPoolName + "_use", mu.getUsed());
            instance.setValue(jvmMemPoolName + "_commit", mu.getCommitted());
            instance.setValue(jvmMemPoolName + "_max", mu.getMax());
            instance.setValue(jvmMemPoolName + "_init", mu.getInit());
        }*/
        
        Set<String> addedSet = new HashSet<String>();
        
        for (MemoryPoolMXBean mpmb : pmbList) {
    
            String jvmMemPoolName = getHeapPoolName(mpmb.getName().trim());
            MemoryUsage mu = mpmb.getUsage();
            
            if(addedSet.contains(jvmMemPoolName)) {
            
                instance.setValue(jvmMemPoolName + "_use", (Long)instance.getValue(jvmMemPoolName + "_use") + mu.getUsed());
                instance.setValue(jvmMemPoolName + "_commit", (Long)instance.getValue(jvmMemPoolName + "_commit") + mu.getCommitted());
                instance.setValue(jvmMemPoolName + "_max", (Long)instance.getValue(jvmMemPoolName + "_max") + mu.getMax());
                instance.setValue(jvmMemPoolName + "_init", (Long)instance.getValue(jvmMemPoolName + "_init") + mu.getInit());
            }else {
                
                addedSet.add(jvmMemPoolName);
                instance.setValue(jvmMemPoolName + "_use", mu.getUsed());
                instance.setValue(jvmMemPoolName + "_commit", mu.getCommitted());
                instance.setValue(jvmMemPoolName + "_max", mu.getMax());
                instance.setValue(jvmMemPoolName + "_init", mu.getInit());
            }
        }
    }

    private String getHeapPoolName(String poolName) {

        String jvmMemPoolName = poolName.toLowerCase();

        if (jvmMemPoolName.indexOf("code") > -1) {
            return "code";
        }
        else if (jvmMemPoolName.indexOf("old") > -1 || jvmMemPoolName.indexOf("tenured") > -1) {
            return "old";
        }
        else if (jvmMemPoolName.indexOf("eden") > -1) {
            return "eden";
        }
        else if (jvmMemPoolName.indexOf("survivor") > -1) {
            return "surv";
        }
        else if (jvmMemPoolName.indexOf("perm") > -1 || jvmMemPoolName.indexOf("metaspace") > -1) {
            return "perm";
        }
        else if (jvmMemPoolName.indexOf("compressed") > -1 && jvmMemPoolName.indexOf("class") > -1) {
            return "compressed";
        }

        return jvmMemPoolName;
    }

    private void readClassLoadUsage(MonitorElementInstance instance) {

        ClassLoadingMXBean clmb = ManagementFactory.getClassLoadingMXBean();

        instance.setValue("class_total", clmb.getTotalLoadedClassCount());
        instance.setValue("class_load", clmb.getLoadedClassCount());
        instance.setValue("class_unload", clmb.getUnloadedClassCount());
    }

    protected void readHeapUsage(MonitorElementInstance instance) {

        MemoryUsage mu = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();

        instance.setValue("heap_use", mu.getUsed());
        instance.setValue("heap_commit", mu.getCommitted());
        instance.setValue("heap_max", mu.getMax());
        instance.setValue("heap_init", mu.getInit());
    }

    protected void readNonHeapUsage(MonitorElementInstance instance) {

        MemoryUsage mu = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();

        instance.setValue("noheap_use", mu.getUsed());
        instance.setValue("noheap_commit", mu.getCommitted());
        instance.setValue("noheap_max", mu.getMax());
        instance.setValue("noheap_init", mu.getInit());
    }

    protected void readThreadUsage(MonitorElementInstance inst) {

        ThreadMXBean tmb = ManagementFactory.getThreadMXBean();

        inst.setValue("thread_live", tmb.getThreadCount());
        inst.setValue("thread_daemon", tmb.getDaemonThreadCount());
        inst.setValue("thread_peak", tmb.getPeakThreadCount());
        inst.setValue("thread_started", tmb.getTotalStartedThreadCount());
    }

    protected void readCPUUsage(MonitorElementInstance inst) {

        OperatingSystemMXBean osMBean = ManagementFactory.getOperatingSystemMXBean();
        Double procCPU = (Double) ReflectionHelper.invoke("com.sun.management.OperatingSystemMXBean", osMBean,
                "getProcessCpuLoad", null, null);
        Double systemCPU = (Double) ReflectionHelper.invoke("com.sun.management.OperatingSystemMXBean", osMBean,
                "getSystemCpuLoad", null, null);

        if (procCPU == null) {
            procCPU = JVMToolHelper.getProcessCpuUtilization();
            systemCPU = -1D;
        }

        inst.setValue("cpu_p", Double.valueOf(formatter.format(procCPU * 100)));
        inst.setValue("cpu_s", Double.valueOf(formatter.format(systemCPU * 100)));
    }

    @Override
    public void preStore(MonitorElementInstance instance) {

        // Do nothing but must pass sonar check
    }

}
