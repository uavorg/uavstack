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

package com.creditease.agent;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import com.creditease.agent.helpers.NetworkHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.helpers.ThreadHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.agent.spi.AgentResourceComponent;
import com.creditease.agent.spi.I1NQueueWorkerMgr;
import com.creditease.agent.spi.IConfigurationManager;
import com.creditease.agent.spi.IForkjoinWorkerMgr;
import com.creditease.agent.spi.ISystemActionEngineMgr;
import com.creditease.agent.spi.ISystemInvokerMgr;
import com.creditease.agent.spi.ITimerWorkManager;
import com.creditease.agent.workqueue.System1NQueueWorkerMgr;
import com.creditease.agent.workqueue.SystemActionEngineMgr;
import com.creditease.agent.workqueue.SystemForkjoinWorkerMgr;
import com.creditease.agent.workqueue.SystemInvokerManager;
import com.creditease.agent.workqueue.SystemTimerWorkMgr;

public class SystemStarter {

    private static ISystemLogger log = null;

    public static void main(String[] args) {

        SystemStarter starter = new SystemStarter();
        starter.startup(args);
    }

    private ITimerWorkManager timerWorkManager = null;
    private IConfigurationManager configMgr = null;

    private I1NQueueWorkerMgr sys1nQueueWorkerMgr = null;
    private IForkjoinWorkerMgr sysForkjoinWorkerMgr = null;
    private ISystemActionEngineMgr sysActionEngineMgr = null;
    private ISystemInvokerMgr sysInvokerMgr = null;

    public void stop() {

        Set<Object> components = this.configMgr.getComponents();

        for (Object component : components) {
            if (AgentFeatureComponent.class.isAssignableFrom(component.getClass())) {
                AgentFeatureComponent afc = (AgentFeatureComponent) component;
                try {
                    afc.stop();

                    // this.configMgr.unregisterComponent(afc.getFeature(), afc.getName());

                    log.info(this, "stop feature [" + afc.getFeature() + "] component [" + afc.getName() + "] SUCCESS");
                }
                catch (Exception e) {
                    log.err(this, "stop feature [" + afc.getFeature() + "] component [" + afc.getName() + "] FAILs ",
                            e);
                }
            }
            else if (AgentResourceComponent.class.isAssignableFrom(component.getClass())) {
                AgentResourceComponent arc = (AgentResourceComponent) component;
                try {
                    arc.releaseResource();

                    // this.configMgr.unregisterComponent(arc.getFeature(), arc.getClass().getSimpleName());

                    log.info(this,
                            "stop resource [" + arc.getFeature() + "] component [" + arc.getName() + "] SUCCESS");
                }
                catch (Exception e) {
                    log.err(this, "stop resource [" + arc.getFeature() + "] component [" + arc.getName() + "] FAILs ",
                            e);
                }
            }
        }

        if (sysInvokerMgr != null) {
            sysInvokerMgr.shutdown();
            this.configMgr.unregisterComponent("Global", "ISystemInvokerMgr");
            log.info(this, "System Invoker Manager shutdown");
        }

        if (sysForkjoinWorkerMgr != null) {
            sysForkjoinWorkerMgr.shutdown();
            this.configMgr.unregisterComponent("Global", "I1NQueueWorkerMgr");
            log.info(this, "System ForkjoinWorker Manager shutdown");
        }

        if (sys1nQueueWorkerMgr != null) {
            sys1nQueueWorkerMgr.shutdown();
            this.configMgr.unregisterComponent("Global", "I1NQueueWorkerMgr");
            log.info(this, "System 1+N QueueWorker Manager shutdown");
        }

        if (sysActionEngineMgr != null) {
            sysActionEngineMgr.shutdown();
            this.configMgr.unregisterComponent("Global", "ISystemActionEngineMgr");
            log.info(this, "System ActionEngine Manager shutdown");
        }

        if (timerWorkManager != null) {
            timerWorkManager.shutdown();
            this.configMgr.unregisterComponent("Global", "ITimerWorkManager");
            log.info(this, "System Timer Manager shutdown");
        }

        log.info(this, "CreditEase Agent Server stopped");
    }

    public void startup(String[] args) {

        // command argument parsing
        if (null != args && (args.length % 2) != 0) {
            throw new RuntimeException(
                    "The count of command arguments should be even number! Please check the command arguments line.");
        }

        Map<String, String> cmdArgs = new HashMap<String, String>();

        for (int i = 0; i < args.length; i += 2) {

            String cmdKey = args[i];

            if (cmdKey.indexOf("-") != 0) {
                throw new RuntimeException("The command argument key at " + (i + 1) + " should start with \"-\" ");
            }

            cmdArgs.put(cmdKey, args[i + 1]);
        }

        if (cmdArgs.containsKey("-help")) {

            StringBuilder sb = new StringBuilder();

            sb.append("UAV base framework help:\n");
            sb.append("-help ?		display help information\n");
            sb.append(
                    "-profile or -p <profile name>		use the target profile in name <profile name> to startup\n");

            System.out.print(sb.toString());
            return;
        }

        // init configuration manager
        this.configMgr = createConfigurationManager(cmdArgs);

        log = SystemLogger.getLogger(SystemStarter.class);

        // System.out to log system
        initSystemToLog();

        log.info(this, "<--------------Micro-Service Computing Platform-------------->");

        log.info(this, "PROFILE=" + this.configMgr.getContext(IConfigurationManager.PROFILENAME));
        log.info(this, "NODEUUID=" + this.configMgr.getContext(IConfigurationManager.NODEUUID));

        // check if network is ready
        checkNetworkReady();

        this.timerWorkManager = this.createTimerWorkManager();

        this.sys1nQueueWorkerMgr = this.createSys1NQueueWorkerManager();

        this.sysActionEngineMgr = this.createSystemActionEngineMgr();

        this.sysForkjoinWorkerMgr = this.createSysForkjoinWorkerManager();

        this.sysInvokerMgr = this.createSystemInvokerMgr();

        // install resources
        installResources();

        // install features
        installFeatures();

        log.info(this, "Micro-Service Computing Platform started");
    }

    /**
     * Sometimes the network may be 127.0.0.1 or not ready when the process starts then wait for max 30 seconds to see
     * if the network is ready if still not ready, then exit
     */
    private void checkNetworkReady() {

        int i = 0;

        boolean isReady = false;

        while (i < 30) {

            String ipAddr = NetworkHelper.getLocalIP();

            if (StringHelper.isEmpty(ipAddr) || "127.0.0.1".equals(ipAddr)) {

                ThreadHelper.suspend(1000);

                i++;

                log.warn(this, "Waiting for network ready: iteration=" + i + ", current IP=" + ipAddr);
            }
            else {
                log.info(this, "Node IP address=" + ipAddr);

                isReady = true;

                break;
            }
        }

        if (isReady == false) {
            log.warn(this, "Micro-Service Computing Platform starts fail as network is not ready.");
        }
    }

    /**
     * initSystemToLog
     */
    protected void initSystemToLog() {

        PrintStream ps = new PrintStream(System.out) {

            @Override
            public void println(boolean x) {

                log.info(this, (Boolean.valueOf(x)).toString());
            }

            @Override
            public void println(char x) {

                log.info(this, Character.valueOf(x).toString());
            }

            @Override
            public void println(char[] x) {

                log.info(this, (x == null ? null : new String(x)));
            }

            @Override
            public void println(double x) {

                log.info(this, Double.valueOf(x).toString());
            }

            @Override
            public void println(float x) {

                log.info(this, Float.valueOf(x).toString());
            }

            @Override
            public void println(int x) {

                log.info(this, Integer.valueOf(x).toString());
            }

            @Override
            public void println(long x) {

                log.info(this, String.valueOf(x));
            }

            @Override
            public void println(Object x) {

                log.info(this, x.toString());
            }

            @Override
            public void println(String x) {

                log.info(this, x);
            }

        };

        PrintStream errps = new PrintStream(System.err) {

            @Override
            public void println(boolean x) {

                log.err(this, (Boolean.valueOf(x)).toString());
            }

            @Override
            public void println(char x) {

                log.err(this, Character.valueOf(x).toString());
            }

            @Override
            public void println(char[] x) {

                log.err(this, (x == null ? null : new String(x)));
            }

            @Override
            public void println(double x) {

                log.err(this, Double.valueOf(x).toString());
            }

            @Override
            public void println(float x) {

                log.err(this, Float.valueOf(x).toString());
            }

            @Override
            public void println(int x) {

                log.err(this, Integer.valueOf(x).toString());
            }

            @Override
            public void println(long x) {

                log.err(this, String.valueOf(x));
            }

            @Override
            public void println(Object x) {

                log.err(this, x.toString());
            }

            @Override
            public void println(String x) {

                log.err(this, x);
            }
        };

        System.setOut(ps);
        System.setErr(errps);
    }

    /**
     * install resources
     */
    private void installResources() {

        Properties config = this.configMgr.getConfigurations();

        Set<Entry<Object, Object>> configEntrys = config.entrySet();

        for (Entry<Object, Object> configEntry : configEntrys) {

            String configName = configEntry.getKey().toString();
            // check if resource configuration
            if (!(configName.indexOf("resource.") == 0)) {
                continue;
            }

            // check if resource configuration
            String[] floader = configName.split("\\.");
            if (floader.length != 3 || !"class".equals(floader[2])) {
                continue;
            }

            // get resource name
            String resourceName = floader[1];

            // using default classloader
            ClassLoader cl = this.getClass().getClassLoader();

            String resourceClsName = config.getProperty("resource." + resourceName + ".class");

            try {
                // load resource class
                Class<?> resourceCls = cl.loadClass(resourceClsName);
                // check if resource class extends AgentResourceComponent
                if (!AgentResourceComponent.class.isAssignableFrom(resourceCls)) {
                    continue;
                }

                // get constructor
                Constructor<?> con = resourceCls.getConstructor(new Class<?>[] { String.class, String.class });

                AgentResourceComponent arc = (AgentResourceComponent) con
                        .newInstance(new Object[] { resourceCls.getSimpleName(), resourceName });

                // invoke AgentResourceComponent to init resource object
                Object resource = arc.initResource();

                if (resource != null) {
                    // // register resource object & AgentResourceComponent
                    // this.configMgr.registerComponent(resourceName, arc.getClass().getSimpleName(), arc);

                    log.info(this, "load resource [" + resourceName + "] from resourceClass [" + resourceClsName + "]");
                    continue;
                }

                // in case resource object is null
                throw new NullPointerException("init failure of resource object");

            }
            catch (Exception e) {

                log.err(this, "fail to load resource [" + resourceName + "]:" + e.getMessage(), e);
            }

        }

    }

    /**
     * 
     * uninstallFeature
     * 
     * @param featureName
     * @return
     */
    public boolean uninstallFeature(String featureName) {

        for (Object afcObj : this.configMgr.getComponents()) {

            if (!AgentFeatureComponent.class.isAssignableFrom(afcObj.getClass())) {
                continue;
            }

            AgentFeatureComponent afcAC = (AgentFeatureComponent) afcObj;

            String f = afcAC.getFeature();

            if (f.equalsIgnoreCase(featureName)) {

                try {
                    // stop
                    afcAC.stop();

                    // unregister
                    // this.configMgr.unregisterComponent(afcAC.getFeature(), afcAC.getName());
                }
                catch (Exception e) {

                    log.err(this, "stop feature[" + f + "] component[" + afcAC.getName() + "] FAIL.", e);

                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 
     * installFeature
     * 
     * @param featureName
     * @return
     */
    public boolean installFeature(String featureName) {

        Properties config = this.configMgr.getConfigurations();

        // get feature loader path
        String floaderValue = config.getProperty("feature." + featureName + ".loader");

        log.info(this, "load feature [" + featureName + "] with loader=" + floaderValue);

        // load feature jars, if floaderValue is default, using default
        // classloader
        ClassLoader cl = this.getClass().getClassLoader();
        // if floaderValue is path, then using a URLClassLoader to load the
        // feature jar
        if (!"default".equals(floaderValue)) {
            try {
                if (floaderValue.indexOf("{$agent.home}") == 0) {
                    floaderValue = floaderValue.replace("{$agent.home}",
                            this.configMgr.getContext(IConfigurationManager.ROOT));
                }

                URL loaderPath = new URL("file:///" + floaderValue);
                cl = new URLClassLoader(new URL[] { loaderPath });
            }
            catch (Exception e) {
                log.err(this, "create feature [" + featureName + "] loader FAILs ", e);
                return false;
            }
        }
        // load feature components
        String componentConfig = config.getProperty("feature." + featureName + ".components");
        if (componentConfig == null || "".equals(componentConfig)) {
            return false;
        }

        if (log.isTraceEnable()) {
            log.info(this, "load feature [" + featureName + "] components=" + componentConfig);
        }

        // feature may have more than one component
        String[] components = componentConfig.split(",");

        startComponent(featureName, cl, components);

        return true;
    }

    /**
     * install features
     */
    private void installFeatures() {

        Properties config = this.configMgr.getConfigurations();

        Set<Entry<Object, Object>> configEntrys = config.entrySet();

        for (Entry<Object, Object> configEntry : configEntrys) {

            String configName = configEntry.getKey().toString();

            // check if feature configuration
            if (!(configName.indexOf("feature.") == 0)) {
                continue;
            }

            // check if feature loader configuration
            String[] floader = configName.split("\\.");
            if (floader.length != 3 || !"loader".equals(floader[2])) {
                continue;
            }

            // get feature name
            String featureName = floader[1];

            // check if feature is enabled
            boolean featureEnable = Boolean
                    .parseBoolean(config.getProperty("feature." + featureName + ".enable", "false"));
            if (featureEnable == false)
                continue;

            // install feature
            installFeature(featureName);
        }
    }

    /**
     * @param featureName
     * @param cl
     * @param components
     */
    private void startComponent(String featureName, ClassLoader cl, String[] components) {

        // int index = 0;

        for (String component : components) {
            try {
                // load component class
                Class<?> componentClass = cl.loadClass(component);
                // check if component class extends AgentFeatureComponent
                if (!AgentFeatureComponent.class.isAssignableFrom(componentClass)) {
                    continue;
                }
                // get constructor
                Constructor<?> con = componentClass.getConstructor(new Class<?>[] { String.class, String.class });
                // new component instance
                AgentFeatureComponent afc = (AgentFeatureComponent) con
                        .newInstance(new Object[] { componentClass.getSimpleName(), featureName });
                // start the component
                afc.start();

                log.info(this, "start feature [" + featureName + "] component [" + component + "] SUCCESS");
            }
            catch (RuntimeException e) {
                log.err(this, "start feature [" + featureName + "] component [" + component + "] FAILs ", e);
            }
            catch (Exception e) {
                log.err(this, "start feature [" + featureName + "] component [" + component + "] FAILs ", e);
            }
        }
    }

    private ITimerWorkManager createTimerWorkManager() {

        ITimerWorkManager itwm = new SystemTimerWorkMgr();
        this.configMgr.registerComponent("Global", "ITimerWorkManager", itwm);
        log.info(this, "System Timer Manager created");
        return itwm;
    }

    private ISystemActionEngineMgr createSystemActionEngineMgr() {

        ISystemActionEngineMgr saem = new SystemActionEngineMgr();

        this.configMgr.registerComponent("Global", "ISystemActionEngineMgr", saem);

        log.info(this, "System ActionEngine Manager created");
        return saem;
    }

    private I1NQueueWorkerMgr createSys1NQueueWorkerManager() {

        I1NQueueWorkerMgr qwm = new System1NQueueWorkerMgr();

        this.configMgr.registerComponent("Global", "I1NQueueWorkerMgr", qwm);

        log.info(this, "System 1+N QueueWorker Manager created");
        return qwm;
    }

    private IForkjoinWorkerMgr createSysForkjoinWorkerManager() {

        IForkjoinWorkerMgr qwm = new SystemForkjoinWorkerMgr();

        this.configMgr.registerComponent("Global", "IForkjoinWorkerMgr", qwm);

        log.info(this, "System Forkjoin Worker Manager created");
        return qwm;
    }

    private ISystemInvokerMgr createSystemInvokerMgr() {

        ISystemInvokerMgr invokeMgr = new SystemInvokerManager();

        this.configMgr.registerComponent("Global", "ISystemInvokerMgr", invokeMgr);

        log.info(this, "System Invoker Manager created");
        return invokeMgr;
    }

    private IConfigurationManager createConfigurationManager(Map<String, String> cmdArgs) {

        IConfigurationManager icm = ConfigurationManager.build(cmdArgs);

        icm.init();

        // register systemstarter
        icm.registerComponent("Global", "SystemStarter", this);

        return icm;
    }
}
