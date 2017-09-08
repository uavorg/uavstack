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

package com.creditease.uav.monitorframework.agent;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

import com.creditease.uav.monitorframework.agent.trans.MOFClsTransformer;
import com.creditease.uav.monitorframework.agent.trans.TtlTransformer;
import com.creditease.uav.monitorframework.agent.util.IOUtil;
import com.creditease.uav.monitorframework.agent.util.JarUtil;

/**
 * 
 * MOFAgent description: the javaagent to hack java application
 *
 */
public class MOFAgent {

    public static void premain(String agentArgs, Instrumentation inst) {

        String uavMORoot = getMOFRoot(agentArgs);

        String extlib = getExtLib(uavMORoot);

        String extJdkLib = uavMORoot + "/com.creditease.uav.jdk";

        installUAVjdk(inst, extJdkLib);

        String adaptorLib = uavMORoot + "/com.creditease.uav.adaptor";

        // load json and helper jar for agent
        String extJar1 = uavMORoot + "/com.creditease.uav/com.creditease.uav.fastjson-1.2.6.jar";

        String extJar2 = uavMORoot + "/com.creditease.uav/com.creditease.uav.helper-1.0.jar";

        String extJar3 = uavMORoot + "/com.creditease.uav.jdk/com.creditease.uav.ttl-2.1.0.jar";

        URL[] mofJars = JarUtil.loadJars(extlib, adaptorLib, extJar1, extJar2, extJar3);

        ClassLoader mofLoader = new URLClassLoader(mofJars);

        inst.addTransformer(new TtlTransformer(mofLoader));

        inst.addTransformer(new MOFClsTransformer(mofLoader, uavMORoot, agentArgs));
    }

    public static String getExtLib(String uavMORoot) {

        String jversion = System.getProperty("java.version");
        String extlib = uavMORoot + "/com.creditease.uav.extlib/";

        if (jversion.indexOf("1.6") > -1) {
            extlib += "6";
        }
        else if (jversion.indexOf("1.7") > -1) {
            extlib += "7";
        }
        else if (jversion.indexOf("1.8") > -1) {
            extlib += "8";
        }
        return extlib;
    }

    /**
     * getMOFRoot
     * 
     * @param agentArgs
     * @return
     */
    private static String getMOFRoot(String agentArgs) {

        /**
         * 从agent args查找
         */
        String uavMORoot = format(agentArgs);

        if (existMOF(uavMORoot)) {
            return uavMORoot;
        }

        /**
         * 从环境变量查找
         */
        uavMORoot = format(System.getenv("UAV_MOF_ROOT"));

        if (existMOF(uavMORoot)) {
            return uavMORoot;
        }

        /**
         * 从-D参数中查找
         */
        uavMORoot = format(System.getProperty("com.creditease.uav.uavmof.root"));

        if (existMOF(uavMORoot)) {
            return uavMORoot;
        }

        /**
         * 从用户目录查找
         */
        String[] userFolders = new String[] { "/uav/uavmof", "/uavmof" };

        for (String userFolder : userFolders) {

            uavMORoot = format(System.getProperty("user.home") + userFolder);

            if (existMOF(uavMORoot)) {
                return uavMORoot;
            }
        }

        /**
         * 从uavmof.location查找
         */
        String userHome = System.getProperty("user.home");

        String tFilePath = userHome + "/uavmof.location";

        File tFile = new File(tFilePath);

        boolean isFind = true;
        if (!tFile.exists()) {

            isFind = false;

            File userHomeFlie = new File(userHome);

            File[] uFList = userHomeFlie.getParentFile().listFiles();

            for (File uF : uFList) {

                tFilePath = uF.getAbsolutePath() + "/uavmof.location";

                File tFileTmp = new File(tFilePath);

                if (tFileTmp.exists()) {
                    isFind = true;
                    break;
                }
            }
        }

        if (isFind == true) {
            String locationStr = IOUtil.readTxtFile(tFilePath, "utf-8");

            uavMORoot = format(locationStr);

            if (existMOF(uavMORoot)) {
                return uavMORoot;
            }
        }

        /**
         * 从JavaAgent的参数路径作为uavmof.root的location
         */
        String classNamePath = MOFAgent.class.getName().replace(".", "/") + ".class";
        String pathabs = MOFAgent.class.getClassLoader().getResource(classNamePath).getPath();

        String pathAbs = format(pathabs);

        if (null != pathAbs) {
            int startIndex = pathAbs.indexOf(":");
            int endIndex = pathAbs.indexOf("com.creditease.uav.agent");

            if (startIndex > 0 && endIndex > 0) {
                uavMORoot = pathAbs.substring(startIndex + 1, endIndex - 1);
            }
        }
        if (existMOF(uavMORoot)) {
            return uavMORoot;
        }

        /**
         * AppServer的home路径同级目录
         */
        String[] appservers = new String[] { "catalina.home", "jetty.home" };

        for (String appserver : appservers) {

            appserver = System.getProperty(appserver);

            if (null == appserver || appserver.length() == 0 || "null".equals(appserver)) {
                continue;
            }

            File f = new File(appserver);

            uavMORoot = f.getParentFile().getAbsolutePath() + "/uavmof";

            uavMORoot = format(uavMORoot);

            if (existMOF(uavMORoot)) {
                return uavMORoot;
            }
        }

        /**
         * 从固定目录中查找
         */
        String[] paths = new String[] { "/app/uav/uavmof", "/app/uavmof" };

        for (String path : paths) {

            uavMORoot = format(path);

            if (existMOF(uavMORoot)) {
                return uavMORoot;
            }
        }

        return uavMORoot;
    }

    /**
     * 格式化uavmofroot
     * 
     * @param mofRoot
     * @return
     */
    private static String format(String mofRoot) {

        if (mofRoot == null) {
            return null;
        }

        String uavMORoot = mofRoot.trim().replace("\n", "").replace("\r", "").replace("\\", "/");

        return uavMORoot;
    }

    /**
     * 检查uav.properties是否存在
     * 
     * @param mofRoot
     * @return
     */
    private static boolean existMOF(String mofRoot) {

        if (mofRoot == null) {
            return false;
        }

        if (new File(mofRoot + "/com.creditease.uav/uav.properties").exists()) {
            // store UAVMOF root to system properties
            System.setProperty("com.creditease.uav.uavmof.root", mofRoot);
            return true;
        }

        return false;
    }

    private static void installUAVjdk(Instrumentation inst, String extJdkLib) {

        URL[] jdkJars = JarUtil.loadJars(extJdkLib);
        try {
            for (URL temp : jdkJars) {
                inst.appendToBootstrapClassLoaderSearch(new JarFile(temp.getFile()));
            }
        }
        catch (IOException e) {
            // ignore
            e.printStackTrace();
        }
    }
}
