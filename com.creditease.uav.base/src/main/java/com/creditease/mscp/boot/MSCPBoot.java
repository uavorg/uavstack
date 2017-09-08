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
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MSCPBoot {

    ClassLoader mainClsLoader = null;

    public static void main(String[] args) {

        MSCPBoot boot = new MSCPBoot();
        boot.start(args);
    }

    public void start(String[] args) {

        mainClsLoader = createBootClassLoader();

        Thread.currentThread().setContextClassLoader(mainClsLoader);

        try {
            Class<?> c = mainClsLoader.loadClass("com.creditease.agent.SystemStarter");

            Object starter = c.newInstance();

            Method m = c.getMethod("startup", new Class<?>[] { String[].class });

            m.invoke(starter, new Object[] { args });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ClassLoader createBootClassLoader() {

        File f = new File("");

        String currentPath = f.getAbsolutePath();

        String libPath = currentPath + "/lib";

        final URL[] files = loadJars(libPath);

        final ClassLoader parent = this.getClass().getClassLoader();

        return AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {

            @Override
            public URLClassLoader run() {

                return new URLClassLoader(files, parent);
            }
        });
    }

    private URL[] loadJars(String... libPaths) {

        List<File> files = new ArrayList<File>();

        for (String libPath : libPaths) {

            File plusRoot = new File(libPath);

            if (!plusRoot.exists() || !plusRoot.isDirectory())
                return null;

            File[] jarFiles = plusRoot.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {

                    if (name.endsWith(".jar")) {
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
