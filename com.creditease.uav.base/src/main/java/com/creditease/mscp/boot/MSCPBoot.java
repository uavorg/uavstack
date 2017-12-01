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

package com.creditease.mscp.boot;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import com.creditease.agent.helpers.CommonHelper;
import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.PropertiesHelper;

public class MSCPBoot {

    ClassLoader mainClsLoader = null;

    public static void main(String[] args) {

        MSCPBoot boot = new MSCPBoot();
        boot.start(args);
    }

    public void start(String[] args) {

        // step1: command argument parsing
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
            sb.append("-help ?      display help information\n");
            sb.append("-profile or -p <profile name>      use the target profile in name <profile name> to startup\n");

            System.out.print(sb.toString());
            return;
        }

        // step 2: exclude those feature jars
        Set<String> featureJars = new HashSet<String>();
        String rootPath = IOHelper.getCurrentPath();
        String profileName = CommonHelper.getValueFromSeqKeys(cmdArgs, new String[] { "-profile", "-p" });
        profileName = (profileName == null) ? "agent" : profileName;
        String pfPath = rootPath + "/config/" + profileName + ".properties";
        try {
            Properties p = PropertiesHelper.loadPropertyFile(pfPath);

            Set<Entry<Object, Object>> configEntrys = p.entrySet();

            for (Entry<Object, Object> configEntry : configEntrys) {

                String configName = configEntry.getKey().toString();

                // check if feature configuration
                if (!(configName.indexOf("feature.") == 0)) {
                    continue;
                }

                if (configName.endsWith(".loader") == false) {
                    continue;
                }

                String jarList = configEntry.getValue().toString();

                if ("default".equalsIgnoreCase(jarList)) {
                    continue;
                }

                String[] featureJarsList = jarList.split(",");
                featureJars.addAll(Arrays.asList(featureJarsList));
            }
        }
        catch (IOException e1) {
            e1.printStackTrace();
            return;
        }

        // step 3: create common classloader
        mainClsLoader = createBootClassLoader(featureJars);

        Thread.currentThread().setContextClassLoader(mainClsLoader);

        try {
            Class<?> c = mainClsLoader.loadClass("com.creditease.agent.SystemStarter");

            Object starter = c.newInstance();

            Method m = c.getMethod("startup", new Class<?>[] { Map.class });

            m.invoke(starter, new Object[] { cmdArgs });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ClassLoader createBootClassLoader(Set<String> featureJars) {

        File f = new File("");

        String currentPath = f.getAbsolutePath();

        String libPath = currentPath + "/lib";

        final URL[] files = loadJars(featureJars, libPath);

        final ClassLoader parent = this.getClass().getClassLoader();

        return AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {

            @Override
            public URLClassLoader run() {

                return new URLClassLoader(files, parent);
            }
        });
    }

    private URL[] loadJars(final Set<String> featureJars, String... libPaths) {

        List<File> files = new ArrayList<File>();

        for (String libPath : libPaths) {

            File plusRoot = new File(libPath);

            if (!plusRoot.exists() || !plusRoot.isDirectory())
                return null;

            File[] jarFiles = plusRoot.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {

                    if (name.endsWith(".jar") && !featureJars.contains(name)) {
                        return true;
                    }

                    return false;
                }

            });

            if (jarFiles == null || jarFiles.length == 0) {
                continue;
            }

            files.addAll(Arrays.asList(jarFiles));
        }

        List<URL> jarURLs = new ArrayList<URL>();

        for (File jar : files) {
            try {
                jarURLs.add(jar.toURI().toURL());
            }
            catch (MalformedURLException e) {
                // ignore
            }
        }

        URL[] jarURLArray = new URL[jarURLs.size()];
        jarURLs.toArray(jarURLArray);
        return jarURLArray;
    }
}
