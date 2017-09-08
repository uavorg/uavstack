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

package com.creditease.uav.monitorframework.adaptors;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.ReflectHelper;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;

public abstract class AbstractAdaptor {

    public abstract class AdaptorProcessor {

        public abstract String getMethodName();

        public int getParamNum() {

            return -1;
        }

        public abstract void process(CtMethod m) throws Exception;

    }

    /*
     * in some case(like springboot),the hook should be done before the webContainer start. This map is provided to
     * record the mapping of hookclass and it's hookjar
     * 
     */
    protected Map<String, String> hookJarMap;

    protected ClassPool pool;

    protected Map<Integer,String> classLoaderHashCodeMap = new ConcurrentHashMap<Integer,String>();

    protected void addClassPath(ClassLoader cl) {

        if (cl == null) {
            return;
        }

        if (classLoaderHashCodeMap.containsKey(cl.hashCode())) {
            return;
        }

        classLoaderHashCodeMap.put(cl.hashCode(), "");

        if (pool != null) {
            pool.appendClassPath(new LoaderClassPath(cl));
        }
    }

    protected void addLocalVar(CtMethod m, String varName, String varClassName) {

        try {
            CtClass cc = pool.get(varClassName);

            m.addLocalVariable(varName, cc);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected byte[] inject(String className, String[] importPackages, AdaptorProcessor... p) {

        try {
            CtClass cc = pool.get(className);

            if (importPackages != null) {
                for (String ip : importPackages) {
                    pool.importPackage(ip);
                }
            }

            for (AdaptorProcessor ap : p) {

                CtMethod mthd = null;

                try {
                    if (ap.getParamNum() == -1) {
                        mthd = cc.getDeclaredMethod(ap.getMethodName());
                    }
                    else {
                        CtMethod[] mlist = cc.getDeclaredMethods();

                        for (CtMethod m : mlist) {

                            if (m.getParameterTypes().length == ap.getParamNum()
                                    && m.getName().equals(ap.getMethodName())) {
                                mthd = m;
                                break;
                            }

                        }
                    }

                }
                catch (Exception e) {
                    continue;
                }

                if (mthd == null) {
                    continue;
                }

                ap.process(mthd);
            }

            byte[] code = cc.toBytecode();

            cc.detach();

            return code;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected Map<String, String> getHookJarMap() {

        if (hookJarMap != null) {
            return hookJarMap;
        }

        String configString = System.getProperty("com.creditease.uav.hookfactory.config");

        @SuppressWarnings("rawtypes")
        List<Map> hookConfig = JSONHelper.toObjectArray(configString, Map.class);

        hookJarMap = new HashMap<String, String>();

        for (@SuppressWarnings("rawtypes")
        Map config : hookConfig) {
            hookJarMap.put((String) config.get("detect"), (String) config.get("jar"));
        }
        return hookJarMap;

    }

    public void installHookJars(ClassLoader webapploader, String loaderName, String uavMofRoot) throws Exception {

        String loaderPath = uavMofRoot + "/com.creditease.uav.appfrk/" + loaderName;

        ReflectHelper.invoke(URLClassLoader.class.getName(), webapploader, "addURL", new Class<?>[] { URL.class },
                new Object[] { new URL("file:///" + loaderPath) }, this.getClass().getClassLoader());

    }

    public abstract byte[] onStartup(ClassLoader clsLoader, String uavMofRoot, String className);

    public abstract byte[] onLoadClass(ClassLoader clsLoader, String uavMofRoot, String className);
}
