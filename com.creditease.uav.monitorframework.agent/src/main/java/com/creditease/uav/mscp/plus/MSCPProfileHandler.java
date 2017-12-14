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

package com.creditease.uav.mscp.plus;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.DateTimeHelper;
import com.creditease.agent.helpers.NetworkHelper;
import com.creditease.agent.spi.AbstractBaseHttpServComponent;
import com.creditease.agent.spi.AbstractHttpHandler;
import com.creditease.agent.spi.AbstractTimerWork;
import com.creditease.agent.spi.IConfigurationManager;
import com.creditease.agent.spi.ITimerWorkManager;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.uav.common.BaseComponent;
import com.creditease.uav.profiling.spi.ProfileConstants;
import com.creditease.uav.profiling.spi.ProfileContext;
import com.creditease.uav.profiling.spi.ProfileElement;
import com.creditease.uav.profiling.spi.ProfileElementInstance;
import com.creditease.uav.profiling.spi.ProfileHandler;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

public class MSCPProfileHandler extends BaseComponent implements ProfileHandler {

    /**
     * 
     * ComponentProcessor description:
     *
     */
    private static abstract class ComponentProcessor {

        public void handle(String compnentClass, String featureName, ProfileElementInstance inst,
                FastClasspathScanner fcs) {

            List<String> MSCPComponents = fcs.getNamesOfSubclassesOf(compnentClass);

            for (String MSCPComponent : MSCPComponents) {
                this.process(MSCPComponent, featureName, inst);
            }
        }

        protected abstract void process(String MSCPComponent, String featureName, ProfileElementInstance inst);
    }

    /**
     * 
     * MSCPHttpComponentProcessor description: com.creditease.agent.spi.AbstractBaseHttpServComponent
     *
     */
    private static class MSCPHttpComponentProcessor extends ComponentProcessor {

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected void process(String MSCPComponent, String featureName, ProfileElementInstance inst) {

            if (MSCPComponent.indexOf("AbstractBaseHttpServComponent") > -1
                    || MSCPComponent.indexOf("AbstractHttpServiceComponent") > -1
                    || MSCPComponent.indexOf("AbstractHttpServiceComponent2") > -1) {
                return;
            }

            HashSet<String> murls = (HashSet<String>) UAVServer.instance().getServerInfo("monitor.urls");

            Map<String, Object> info = new HashMap<String, Object>();

            Class<?> c = ConfigurationManager.getInstance().loadClassFromFeatureClassLoaders(MSCPComponent);

            if (c == null) {
                return;
            }

            Set comps = ConfigurationManager.getInstance().getComponents();

            if (comps.isEmpty()) {
                return;
            }

            Object tcomp = null;

            for (Object comp : comps) {

                if (comp.getClass().getName().equals(MSCPComponent)) {
                    tcomp = comp;
                    break;
                }
            }

            AbstractBaseHttpServComponent abhsc = (AbstractBaseHttpServComponent) tcomp;

            if (abhsc == null) {
                return;
            }

            String serviceRootURL = abhsc.getHttpRootURL();

            info.put("path", serviceRootURL);
            info.put("feature", abhsc.getFeature());

            Map<String, Object> handlerInfos = new HashMap<String, Object>();

            info.put("handlers", handlerInfos);

            List<AbstractHttpHandler> httpHandlers = abhsc.getHandlers();

            for (AbstractHttpHandler ahh : httpHandlers) {

                Map<String, Object> handlerInfo = new HashMap<String, Object>();

                handlerInfo.put("path", ahh.getContextPath());
                handlerInfo.put("name", ahh.getName());

                handlerInfos.put(ahh.getClass().getName(), handlerInfo);

                /**
                 * NOTE: for Monitor Service URL checking
                 */
                murls.add(ahh.getContextPath());
            }

            inst.setValue(MSCPComponent, info);
        }
    }

    /**
     * 
     * MSCPTimeWorkComponentProcessor description: com.creditease.agent.spi.AbstractTimerWork
     *
     */
    private static class MSCPTimeWorkComponentProcessor extends ComponentProcessor {

        @Override
        protected void process(String MSCPComponent, String featureName, ProfileElementInstance inst) {

            if (MSCPComponent.indexOf("AbstractTimerWork") > -1) {
                return;
            }

            ITimerWorkManager itwm = (ITimerWorkManager) ConfigurationManager.getInstance().getComponent("Global",
                    "ITimerWorkManager");

            if (itwm == null) {
                return;
            }

            for (AbstractTimerWork atw : itwm.getAllTimerWork().values()) {

                if (!atw.getClass().getName().equals(MSCPComponent)) {// || !atw.getFeature().equals(featureName)) {
                    continue;
                }

                Map<String, Object> info = new HashMap<String, Object>();

                info.put("name", atw.getName());
                info.put("feature", atw.getFeature());
                info.put("exetime",
                        DateTimeHelper.toStandardDateFormat(atw.getCurrentTimerTask().scheduledExecutionTime()));
                info.put("interval", atw.getPeriod());
                inst.setValue(MSCPComponent + "-" + atw.getName(), info);

                break;
            }
        }

    }

    private static final Map<String, ComponentProcessor> componentClassMap = new HashMap<String, ComponentProcessor>();

    static {

        componentClassMap.put("com.creditease.agent.spi.AbstractBaseHttpServComponent",
                new MSCPHttpComponentProcessor());

        componentClassMap.put("com.creditease.agent.spi.AbstractTimerWork", new MSCPTimeWorkComponentProcessor());
    };

    @Override
    public void doProfiling(ProfileElement elem, ProfileContext context) {

        UAVServer.ServerVendor sv = (UAVServer.ServerVendor) UAVServer.instance()
                .getServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR);

        // only support MSCP Application
        if (sv != UAVServer.ServerVendor.MSCP) {
            return;
        }

        if (!ProfileConstants.PROELEM_COMPONENT.equals(elem.getElemId())) {
            return;
        }

        InterceptContext ic = context.get(InterceptContext.class);

        if (ic == null) {
            this.logger.warn("Profile:Annotation FAILs as No InterceptContext available", null);
            return;
        }

        /**
         * 1. get webappclassloader
         */
        ClassLoader webappclsLoader = (ClassLoader) ic.get(InterceptConstants.WEBAPPLOADER);

        if (null == webappclsLoader) {
            this.logger.warn("Profile:JARS FAILs as No webappclsLoader available", null);
            return;
        }

        Collection<ClassLoader> clList = ConfigurationManager.getInstance().getFeatureClassLoader();
        clList.add(webappclsLoader);

        ClassLoader[] allcl = new ClassLoader[clList.size()];
        allcl = clList.toArray(allcl);

        /**
         * 2. see what kind of components we could get via annotations or interface or parentClass
         */
        FastClasspathScanner fcs = new FastClasspathScanner(allcl, "com.creditease.uav", "com.creditease.agent",
                "org.uavstack");
        fcs.scan();

        /**
         * 3. get MSCPServlets profile info
         */

        InterceptContext itContext = context.get(InterceptContext.class);
        String appRootPath = (String) itContext.get(InterceptConstants.BASEPATH);
        String appName = (String) itContext.get(InterceptConstants.APPNAME);

        for (String componentClassName : componentClassMap.keySet()) {

            // set the instance id = simple name of the annotation class
            ProfileElementInstance inst = elem.getInstance(componentClassName);

            ComponentProcessor cp = componentClassMap.get(componentClassName);

            cp.handle(componentClassName, appName, inst, fcs);

        }

        /**
         * 4. load application info
         */
        loadAppInfo(elem, appRootPath, appName);
    }

    /**
     * load application info
     * 
     * @param elem
     * @param featureRootPath
     * @param featureName
     */
    private void loadAppInfo(ProfileElement elem, String featureRootPath, String appName) {

        ProfileElementInstance inst = elem.getInstance("webapp");

        String nodeAppName = ConfigurationManager.getInstance().getContext(IConfigurationManager.NODEAPPNAME);
        String profileName = ConfigurationManager.getInstance().getContext(IConfigurationManager.PROFILENAME);

        // get app name
        inst.setValue("appname", nodeAppName);
        // get app des
        inst.setValue("appdes",
                "MSCP Application[" + appName + "] of NodeType[" + nodeAppName + "] with Profile[" + profileName + "]");
        // get the real path of application context root
        inst.setValue("webapproot", featureRootPath);
        // get the app Http URL
        inst.setValue("appurl", getServiceURI());
        // get app group
        getAppGroup(inst);
        // get customized metrics
        // getCustomizedMetrics(inst);
    }

    /**
     * getServiceURI
     * 
     * @return
     */
    private String getServiceURI() {

        String serviceURL = null;
        // schema://IP:port/context/
        StringBuffer serviceurl = new StringBuffer("http://");
        String ip = NetworkHelper.getLocalIP();
        int port = DataConvertHelper
                .toInt(ConfigurationManager.getInstance().getFeatureConfiguration("nodeoperagent", "http.port"), 10101);
        serviceURL = serviceurl.append(ip).append(":").append(port).append("/").toString();
        return serviceURL;
    }

    /**
     * getAppGroup
     * 
     * @param inst
     */
    private void getAppGroup(ProfileElementInstance inst) {

        String JAppGroup = System.getProperty("JAppGroup", "");
        inst.setValue("appgroup", JAppGroup);
    }

    /**
     * getCustomizedMetrics
     * 
     * @param inst
     */
    // private void getCustomizedMetrics(ProfileElementInstance inst) {
    //
    // @SuppressWarnings("rawtypes")
    // Map<String, Map> metrics = new HashMap<String, Map>();
    //
    // Enumeration<?> enumeration = System.getProperties().propertyNames();
    //
    // while (enumeration.hasMoreElements()) {
    //
    // String name = (String) enumeration.nextElement();
    //
    // int moIndex = name.indexOf("mo@");
    //
    // if (moIndex != 0) {
    // continue;
    // }
    //
    // try {
    // String[] metricsArray = name.split("@");
    //
    // // add metricName to customizedMetrics
    // if (metricsArray.length == 3) {
    // metrics.put(metricsArray[1], JSONHelper.toObject(metricsArray[2], Map.class));
    // }
    // else {
    // metrics.put(metricsArray[1], Collections.emptyMap());
    // }
    // }
    // catch (Exception e) {
    // logger.error("Parsing Custom Metrics[" + name + "] FAIL.", e);
    // continue;
    // }
    // }
    //
    // inst.setValue("appmetrics", JSONHelper.toString(metrics));
    // }

}
