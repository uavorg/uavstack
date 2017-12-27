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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.creditease.agent.helpers.CommonHelper;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.PropertiesHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.agent.spi.AbstractComponent;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.agent.spi.AgentResourceComponent;
import com.creditease.agent.spi.ComponentMonitor;
import com.creditease.agent.spi.IConfigurationManager;
import com.creditease.agent.spi.ResourceLimitationAuditor;
import com.creditease.uav.helpers.uuid.IdWorker;

public final class ConfigurationManager implements IConfigurationManager {

    private enum SystemProKeyScope {
        System, Profile
    }

    private static Map<String, SystemProKeyScope> systemProConfgKeys = new HashMap<String, SystemProKeyScope>();

    static {

        systemProConfgKeys.put("NetCardIndex", SystemProKeyScope.System);
        systemProConfgKeys.put("NetCardName", SystemProKeyScope.System);
        systemProConfgKeys.put("JAppGroup", SystemProKeyScope.System);
        systemProConfgKeys.put("JAppName", SystemProKeyScope.Profile);
    }

    private static ConfigurationManager configMgr = null;

    public static IConfigurationManager build(Map<String, String> cmdArgs) {

        // read profile config from profile properties file
        String profileName = CommonHelper.getValueFromSeqKeys(cmdArgs, new String[] { "-profile", "-p" });

        configMgr = new ConfigurationManager(profileName);

        return configMgr;
    }

    public static IConfigurationManager getInstance() {

        return configMgr;
    }

    private Map<String, Object> componentsMap = new ConcurrentHashMap<String, Object>();
    private Map<String, ClassLoader> featureLoadersMap = new ConcurrentHashMap<String, ClassLoader>();
    private Properties config = new Properties();
    private String rootPath;
    private String profileName;
    private final ISystemLogger log;
    private String nodeUUID;
    private ComponentMonitor globalMonitor = null;
    private ResourceLimitationAuditor resLimitAuditor = null;

    private ConfigurationManager(String profileName) {

        /**
         * set the MSCP root path and profile name to system properties
         * 
         */
        this.rootPath = IOHelper.getCurrentPath();
        this.profileName = (profileName == null) ? "agent" : profileName;

        System.setProperty("JAppRootPath", this.rootPath);
        System.setProperty("JAppProfileName", this.profileName);

        // load profile config
        Properties p = null;
        String configPath = this.getContext(IConfigurationManager.CONFIGPATH);

        try {

            String pfPath = configPath + this.profileName + ".properties";
            p = PropertiesHelper.loadPropertyFile(pfPath);

            System.setProperty("JAppProfilePath", pfPath);

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        // init metadata
        initMetadata(p);

        // refresh configuration
        this.updateProfileConfiguration(p);

        /**
         * JAppOperPort is the operation port for MSCP application, we take this port as the application port, although
         * there are many ports used for one MSCP applications
         */
        int port = DataConvertHelper.toInt(this.getFeatureConfiguration("nodeoperagent", "http.port"), 10101);
        System.setProperty("JAppOperPort", String.valueOf(port));

        // init logger
        int fileSize = 5 * 1024 * 1024;

        if (!StringHelper.isEmpty(this.getConfiguration("log.fsize"))) {
            fileSize = Integer.parseInt(this.getConfiguration("log.fsize")) * 1024 * 1024;
        }

        /**
         * 设置是否MSCP的log可以被归集
         */
        String isCollectEnable = this.getConfiguration("log.collectEnable");

        if (isCollectEnable != null) {
            System.setProperty("JAppLogsEnable", isCollectEnable);
        }

        /**
         * 设置MSCP的log如果可以被归集，需要符合的文件正则表达式
         */
        String collectMatch = this.getConfiguration("log.collectMatch");

        if (collectMatch != null) {
            System.setProperty("JAppLogsIncludePattern", collectMatch);
        }

        // init buffer size
        int bufferSize = DataConvertHelper.toInt(this.getConfiguration("log.buffersize"), 200);

        String logPath = this.getConfiguration("log.path");

        if (StringHelper.isEmpty(logPath)) {
            logPath = System.getProperty("JAppMetaPath");
        }

        System.setProperty("JAppDefLogPath", logPath);

        SystemLogger.init("P." + this.profileName, logPath, this.getConfiguration("log.level"),
                Boolean.valueOf(this.getConfiguration("log.debug")), bufferSize, fileSize);

        log = SystemLogger.getLogger(this.getClass());

        // make sure the JAppName Exists
        getContext(IConfigurationManager.NODEAPPNAME);

        // create global monitor
        globalMonitor = ComponentMonitor.getMonitor("UAV.Global.Monitor");

        this.registerComponent("Global", "Monitor", globalMonitor);
    }

    /**
     * 
     * @param p
     */
    private void initMetadata(Properties p) {

        // init metadata folder
        String metadatafolder = p.getProperty("meta.path");

        File f = new File(this.rootPath);

        if (StringHelper.isEmpty(metadatafolder)) {
            metadatafolder = this.rootPath + "/../" + f.getName() + ".metadata";
        }

        if (!metadatafolder.endsWith("/")) {
            System.setProperty("JAppMetaPath", metadatafolder + "/");
        }
        else {
            System.setProperty("JAppMetaPath", metadatafolder);
        }

        IOHelper.createFolder(metadatafolder);

        // read the UUID for this node
        initNodeUUID(metadatafolder);

        // read cached system properties
        initSystemProperties(metadatafolder);
    }

    @SuppressWarnings("unchecked")
    private void initSystemProperties(String metadatafolder) {

        String cacheFile = metadatafolder + "/systempro.cache";

        /**
         * if there is no cache file, we read them from system properties and store them
         */
        if (!IOHelper.exists(cacheFile)) {

            this.storeSystemProperties();

            return;
        }

        /**
         * if has cache file, read it and install to system properties
         */
        String s = IOHelper.readTxtFile(cacheFile, "utf-8");

        if (!StringHelper.isEmpty(s)) {

            Map<String, Object> m = JSONHelper.toObject(s, Map.class);

            for (String key : systemProConfgKeys.keySet()) {

                SystemProKeyScope scope = systemProConfgKeys.get(key);

                switch (scope) {
                    case Profile:

                        if (m.containsKey(this.profileName)) {
                            Map<String, String> pm = (Map<String, String>) m.get(this.profileName);
                            if (pm != null) {
                                System.setProperty(key, pm.get(key));
                            }
                        }

                        break;
                    case System:
                    default:
                        String val = (String) m.get(key);
                        if (m.containsKey(key) && !StringHelper.isEmpty(val)) {
                            System.setProperty(key, val);
                        }
                        break;
                }
            }
        }
        this.storeSystemProperties();
    }

    private void initNodeUUID(String metadatafolder) {

        String idFile = metadatafolder + "/" + this.profileName + ".uuid";

        this.nodeUUID = IOHelper.readTxtFile(idFile, "utf-8");

        if (null == this.nodeUUID) {

            // read datacenter id, yes THINK BIG
            String datacenterid = this.getConfiguration("meta.datacenterid");

            if (null == datacenterid) {
                datacenterid = "1";
            }

            IdWorker idWorker = CommonHelper.getUUIDWorker(new Random().nextInt(30) + 1,
                    Integer.parseInt(datacenterid));

            this.nodeUUID = String.valueOf(idWorker.nextId());

            try {
                IOHelper.writeTxtFile(idFile, this.nodeUUID, "utf-8", false);
            }
            catch (IOException e) {
                throw new RuntimeException("Write UUID to file \"" + idFile + "\" Fails.", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Set<T> getComponents(Class<T> componentTypeFilter) {

        if (componentTypeFilter == null) {
            return Collections.emptySet();
        }

        Set<T> components = new HashSet<T>();

        Iterator<Object> itr = this.componentsMap.values().iterator();

        while (itr.hasNext()) {

            Object comp = itr.next();

            if (componentTypeFilter.isAssignableFrom(comp.getClass())) {
                components.add((T) comp);
            }
        }

        return components;
    }

    @Override
    public Set<Object> getComponents() {

        Set<Object> components = new HashSet<Object>();

        Iterator<Object> itr = this.componentsMap.values().iterator();

        while (itr.hasNext()) {

            Object comp = itr.next();

            components.add(comp);
        }

        return components;
    }

    @Override
    public String getConfiguration(String configName) {

        if (configName == null || "".equals(configName))
            return null;

        return this.config.getProperty(configName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getConfiguration(Class<T> clz, String configName) {

        String configValue = getConfiguration(configName);

        if (configValue == null)
            return null;

        if (Integer.class.isAssignableFrom(clz)) {
            return (T) Integer.valueOf(configValue);
        }
        else if (Long.class.isAssignableFrom(clz)) {
            return (T) Long.valueOf(configValue);
        }
        else if (Double.class.isAssignableFrom(clz)) {
            return (T) Double.valueOf(configValue);
        }
        else if (Boolean.class.isAssignableFrom(clz)) {
            return (T) Boolean.valueOf(configValue);
        }

        return null;
    }

    @Override
    public Properties getConfigurations() {

        return this.config;
    }

    @Override
    public String getContext(String key) {

        if (IConfigurationManager.ROOT.equals(key)) {
            return this.rootPath;
        }
        else if (IConfigurationManager.CONFIGPATH.equals(key)) {
            return this.rootPath + "/config/";
        }
        else if (IConfigurationManager.BINPATH.equals(key)) {
            return this.rootPath + "/bin/";
        }
        else if (IConfigurationManager.PROFILENAME.equals(key)) {
            return this.profileName;
        }
        else if (IConfigurationManager.METADATAPATH.equals(key)) {
            return System.getProperty("JAppMetaPath");
        }
        else if (IConfigurationManager.NODEUUID.equals(key)) {
            return this.nodeUUID;
        }
        else if (IConfigurationManager.NODETYPE.equals(key)) {
            return this.getConfiguration("meta.nodetype");
        }
        else if (IConfigurationManager.NODEAPPID.equals(key)) {

            return System.getProperty("JAppID");
        }
        else if (IConfigurationManager.NODEAPPNAME.equals(key)) {

            String jAppName = System.getProperty("JAppName");

            if (jAppName == null) {

                String nType = this.getConfiguration("meta.nodetype");

                if (nType != null) {
                    System.setProperty("JAppName", nType);
                }

                return nType;
            }

            return jAppName;
        }
        else if (IConfigurationManager.NODEAPPVERSION.equals(key)) {
            String nodeAppVersion = System.getProperty("JAppVersion");
            if (nodeAppVersion == null) {
                nodeAppVersion = this.getConfiguration("meta.nodeappversion");

                if (nodeAppVersion == null) {
                    nodeAppVersion = "1.0";
                }
                else {
                    System.setProperty("JAppVersion", nodeAppVersion);
                }
            }

            return nodeAppVersion;
        }
        else if (IConfigurationManager.NODEGROUP.equals(key)) {
            String nodeGroup = this.getConfiguration("meta.nodegroup");

            if (nodeGroup == null) {
                return System.getProperty("JAppGroup", "");
            }
        }
        return null;
    }

    @Override
    public String getFeatureConfiguration(String featureName, String key) {

        String value = this.config.getProperty("feature." + featureName + "." + key);

        if (value == null) {
            value = this.config.getProperty("resource." + featureName + "." + key);
        }

        return value;
    }

    @Override
    public String getResourceConfiguration(String resourceName, String key) {

        return this.config.getProperty("resource." + resourceName + "." + key);
    }

    /**
     * fire the event of profile configuration update
     * 
     * @param newConfig
     */
    @Override
    public void fireProfileConfigurationUpdateEvent(Map<String, String> config) {

        Properties newConfig = new Properties();

        newConfig.putAll(config);

        // update the memory config
        updateProfileConfiguration(newConfig);

        // fire event onConfigUpdate for all AbstractComponents
        Collection<Object> components = componentsMap.values();

        for (Object obj : components) {

            if (AbstractComponent.class.isAssignableFrom(obj.getClass())) {

                AbstractComponent ac = (AbstractComponent) obj;

                try {
                    ac.onConfigUpdate(newConfig);
                }
                catch (Exception e) {
                    log.err(this, "Fire ConfigurationUpdateEvent on AbstractComponent[" + ac.getName()
                            + "] for Feature[" + ac.getFeature() + "] Fail.", e);
                }
            }
        }
    }

    /**
     * update profile configuration
     * 
     * @param newConfig
     */
    public void updateProfileConfiguration(Properties newConfig) {

        if (null == newConfig) {
            return;
        }

        Properties config1 = (Properties) this.config.clone();

        Set<Object> pNames = newConfig.keySet();

        for (Object pName : pNames) {

            String key = (String) pName;

            config1.setProperty(key, newConfig.getProperty(key));
        }

        synchronized (this) {
            // update the memory config
            this.config = config1;
        }
    }

    @Override
    public void fireSystemPropertiesUpdateEvent(Map<String, String> systemPros) {

        for (String key : systemProConfgKeys.keySet()) {

            if (systemPros.containsKey(key)) {
                System.setProperty(key, systemPros.get(key));
            }
        }

        // we need update the system properties cache file
        storeSystemProperties();
    }

    @SuppressWarnings("unchecked")
    private void storeSystemProperties() {

        Map<String, Object> m = new HashMap<String, Object>();

        for (String key : systemProConfgKeys.keySet()) {

            SystemProKeyScope scope = systemProConfgKeys.get(key);

            switch (scope) {
                case Profile:
                    String pval = System.getProperty(key);

                    if (pval != null) {

                        Map<String, String> pm = (Map<String, String>) m.get(this.profileName);

                        if (pm == null) {
                            pm = new HashMap<String, String>();
                            m.put(this.profileName, pm);
                        }

                        pm.put(key, pval);
                    }

                    break;
                case System:
                default:
                    pval = System.getProperty(key);

                    if (pval != null) {
                        m.put(key, pval);
                    }
                    break;
            }

        }

        String cacheFile = this.getContext(IConfigurationManager.METADATAPATH) + "/systempro.cache";

        try {
            IOHelper.writeTxtFile(cacheFile, JSONHelper.toString(m), "utf-8", false);
        }
        catch (IOException e) {
            log.err(this, "Cache System Properties FAIL.", e);
        }
    }

    @Override
    public void unregisterFeatureComponents(String featureName) {

        if (featureName == null) {
            return;
        }

        for (String key : componentsMap.keySet()) {

            Object obj = componentsMap.get(key);

            if (AgentFeatureComponent.class.isAssignableFrom(obj.getClass())) {
                continue;
            }

            /**
             * NOTE: 所有Component的命名格式为<Feature>.<ComponentName>, 包括AbstractComponent和第三方注册的Component
             */
            if (key.indexOf(featureName) == 0) {
                componentsMap.remove(key);
            }
        }
    }

    @Override
    public void registerComponent(String feature, String componentName, Object instance) {

        if (StringHelper.isEmpty(feature) || StringHelper.isEmpty(componentName) || instance == null) {
            return;
        }

        componentsMap.put(feature + "." + componentName, instance);
    }

    @Override
    public void unregisterComponent(String feature, String componentName) {

        if (StringHelper.isEmpty(feature) || StringHelper.isEmpty(componentName)) {
            return;
        }

        componentsMap.remove(feature + "." + componentName);
    }

    @Override
    public Object getComponent(String feature, String componentName) {

        if (StringHelper.isEmpty(componentName) || StringHelper.isEmpty(feature)) {
            return null;
        }

        String fn = feature + "." + componentName;

        if (componentsMap.containsKey(fn)) {
            return componentsMap.get(fn);
        }

        return null;
    }

    @Override
    public Object getComponentResource(String resource, String objName) {

        AgentResourceComponent arc = (AgentResourceComponent) this.getComponent(resource, objName);

        if (arc != null) {
            return arc.getResource();
        }

        return null;
    }

    @Override
    public void init() {

        // create resLimitAuditor
        resLimitAuditor = new ResourceLimitationAuditor("UAV.Global.ResLimit", "ResourceLimitationAuditor");

        this.registerComponent("Global", "ResourceLimitationAuditor", resLimitAuditor);
    }

    @Override
    public ClassLoader getFeatureClassLoader(String feature) {

        if (StringHelper.isEmpty(feature)) {
            return null;
        }

        ClassLoader cl = this.featureLoadersMap.get(feature);

        return (cl == null) ? this.getClass().getClassLoader() : cl;
    }

    @Override
    public void setFeatureClassLoader(String feature, ClassLoader classloader) {

        if (StringHelper.isEmpty(feature) || classloader == null) {
            return;
        }

        this.featureLoadersMap.put(feature, classloader);
    }

    @Override
    public void unsetFeatureClassLoader(String feature) {

        if (StringHelper.isEmpty(feature)) {
            return;
        }

        this.featureLoadersMap.remove(feature);
    }

    /**
     * NOTE: load class from features' classloader
     * 
     * @param feature
     *            if null means to search all features' classloader
     */
    @Override
    public Class<?> loadClassFromFeatureClassLoaders(String className, String... feature) {

        Class<?> cls = null;

        if (feature == null || feature.length == 0) {
            for (ClassLoader cl : this.featureLoadersMap.values()) {
                try {
                    cls = cl.loadClass(className);
                    break;
                }
                catch (ClassNotFoundException e) {
                    continue;
                }
            }
        }
        else {
            for (String f : feature) {
                ClassLoader cl = this.getFeatureClassLoader(f);
                try {
                    cls = cl.loadClass(className);
                    break;
                }
                catch (ClassNotFoundException e) {
                    continue;
                }
            }
        }

        return cls;
    }

    @Override
    public Collection<ClassLoader> getFeatureClassLoader(String... features) {

        Collection<ClassLoader> clList = new ArrayList<ClassLoader>();

        if (features == null || features.length == 0) {

            clList.addAll(this.featureLoadersMap.values());
        }
        else {
            for (String feature : features) {
                ClassLoader cl = this.featureLoadersMap.get(feature);

                if (cl != null) {
                    clList.add(cl);
                }
            }
        }

        return clList;
    }

    /**
     * get the configuration by patterns
     * 
     * @param pattern
     *            using * means any char
     */
    @Override
    public Map<String, String> getConfigurationByPattern(String pattern) {

        String[] kwds = pattern.split("\\*");

        Map<String, String> result = new LinkedHashMap<String, String>();

        for (String pkey : this.config.stringPropertyNames()) {

            boolean check = true;
            for (String kwd : kwds) {
                if (pkey.indexOf(kwd) == -1) {
                    check = false;
                    break;
                }
            }

            if (check == true) {
                result.put(pkey, this.config.getProperty(pkey));
            }
        }

        return result;
    }

}
