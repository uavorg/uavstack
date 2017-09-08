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

package com.creditease.uav.monitorframework.agent.interceptor;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import com.creditease.uav.monitorframework.agent.MOFAgent;
import com.creditease.uav.monitorframework.agent.util.IOUtil;
import com.creditease.uav.monitorframework.agent.util.JarUtil;

public class AbstractIT {

    protected String uavMofRoot;

    public AbstractIT(String uavMofRoot) {
        this.uavMofRoot = uavMofRoot;
    }

    /**
     * loadMOFConfig
     */
    protected void loadMOFConfig() {

        // load uav.properties
        String uavProFilePath = this.uavMofRoot + "/com.creditease.uav/uav.properties";
        File configFile = new File(uavProFilePath);

        if (configFile.exists()) {
            try {
                Properties config = new Properties();

                config.load(new FileInputStream(configFile));

                Set<Entry<Object, Object>> cfgSet = config.entrySet();

                if (!cfgSet.isEmpty()) {
                    for (Entry<Object, Object> entry : cfgSet) {
                        String key = (String) entry.getKey();
                        String value = (String) entry.getValue();

                        /**
                         * NOTE: if uavmof is inside Container, make sure we use the 1st netcard
                         */
                        if (null != System.getenv("IS_ULTRON") && true == Boolean.valueOf(System.getenv("IS_ULTRON"))) {
                            if (key.equals("NetCardIndex")) {
                                value = "0";
                            }
                        }

                        System.setProperty(key, value);
                    }
                }

            }
            catch (Exception e) {
                // ignore
            }
        }

        // when update mof, NetCardIndex will be replaced with default value, so we read it from agent's metadata.
        String systemProFilePath = this.uavMofRoot + "/../uavagent/metadata/systempro.cache";
        loadNetCardIndex(systemProFilePath);

    }

    /**
     * installMOFJars
     * 
     * @param clsLoader
     * @param appSeverVendorName
     */
    protected void installMOFJars(ClassLoader clsLoader, String appSeverVendorName) {

        System.out.println("MOF.Interceptor[" + appSeverVendorName + "] Install MonitorFramework Jars Start...");

        URLClassLoader commonLoader = (URLClassLoader) clsLoader;

        try {
            Method m = URLClassLoader.class.getDeclaredMethod("addURL", new Class<?>[] { URL.class });

            m.setAccessible(true);

            String moflib = this.uavMofRoot + "/com.creditease.uav";

            String extlib = MOFAgent.getExtLib(this.uavMofRoot);

            String appserverCommonLib = this.uavMofRoot + "/com.creditease.uav." + appSeverVendorName + "/common";

            URL[] mofJars = JarUtil.loadJars(moflib, extlib, appserverCommonLib);

            for (URL url : mofJars) {

                m.invoke(commonLoader, new Object[] { url });

                System.out.println("MOF.Interceptor[" + appSeverVendorName + "] Install to class loader with jar ["
                        + url.toString() + "]");
            }
            System.out.println("MOF.Interceptor[" + appSeverVendorName + "] Install MonitorFramework Jars End.");

        }
        catch (Exception e) {
            System.out.println("MOF.Interceptor[" + appSeverVendorName + "] Install MonitorFramework Jars FAIL.");
            e.printStackTrace();
        }
    }

    private void loadNetCardIndex(String systemProFilePath) {

        File systemProFile = new File(systemProFilePath);
        if (!systemProFile.exists()) {
            return;
        }

        String s = IOUtil.readTxtFile(systemProFilePath, "utf-8").trim().replace("\n", "").replace("\"", "");
        if (s == null) {
            return;
        }

        s = s.substring(1, s.length() - 1);
        String[] strs = s.split(",");

        for (String str : strs) {
            String[] pair = str.split(":");
            if (pair[0].indexOf("NetCardIndex") > -1) {
                System.setProperty("NetCardIndex", pair[1]);
                break;
            }
        }

    }
}
