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

package com.creditease.uav.monitorframework.agent.trans;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

public class MOFClsTransformer implements ClassFileTransformer {

    private static Map<String, String> adaptors = new HashMap<String, String>();

    static {
        // tomcat
        adaptors.put("org/apache/catalina/startup/Bootstrap",
                "com.creditease.uav.monitorframework.adaptors.TomcatAdaptor");

        // MSCP
        adaptors.put("com/creditease/mscp/boot/MSCPBoot", "com.creditease.uav.monitorframework.adaptors.MSCPAdaptor");

        // SpringBoot
        adaptors.put("org/springframework/boot/loader/Launcher",
                "com.creditease.uav.monitorframework.adaptors.SpringBootTomcatAdaptor");
        adaptors.put("org/springframework/boot/loader/PropertiesLauncher",
                "com.creditease.uav.monitorframework.adaptors.SpringBootTomcatAdaptor");
        adaptors.put("org/springframework/boot/SpringApplication",
                "com.creditease.uav.monitorframework.adaptors.SpringBootTomcatAdaptor");

        // Jetty
        adaptors.put("org/eclipse/jetty/start/Classpath", "com.creditease.uav.monitorframework.adaptors.JettyAdaptor");
    }

    private ClassLoader mofLoader;
    private String uavMofRoot;
    private Object adaptorInst;
    private Method adaptorOnLoadClassMethod;

    // private Object generalAdaptorInst;
    // private Method generalOnLoadClassMethod;

    public MOFClsTransformer(ClassLoader mofLoader, String uavMORoot, String agentArgs) {

        System.out.println("<------------MOF Agent------------->");
        System.out.println("MOF.AgentArgs=" + agentArgs);
        System.out.println("MOF.Root=" + uavMORoot);
        this.mofLoader = mofLoader;
        this.uavMofRoot = uavMORoot;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        if (null == className) {
            return null;
        }

        // /**
        // * init generalAdaptorInst
        // */
        // if (this.generalAdaptorInst == null) {
        //
        // try {
        // Class adptCls = this.mofLoader
        // .loadClass("com.creditease.uav.monitorframework.adaptors.GeneralClassAOPAdaptor");
        //
        // generalAdaptorInst = adptCls.newInstance();
        //
        // Method onStartupMethod = adptCls.getMethod("onStartup",
        // new Class[] { ClassLoader.class, String.class, String.class });
        //
        // generalOnLoadClassMethod = adptCls.getMethod("onLoadClass",
        // new Class[] { ClassLoader.class, String.class, String.class });
        //
        // onStartupMethod.invoke(generalAdaptorInst, new Object[] { loader, this.uavMofRoot, "" });
        // }
        // catch (Exception e) {
        // e.printStackTrace();
        // }
        // }

        /**
         * for JVM MiddleWare
         */
        if (adaptorInst == null && adaptorOnLoadClassMethod == null && adaptors.containsKey(className)) {

            System.out.println("MOF.EngineType=" + className);

            String adaptorClass = adaptors.get(className);

            try {
                Class adptCls = this.mofLoader.loadClass(adaptorClass);

                // adaptorInst = adptCls.newInstance();
                // jdk9中newInstance被deprecate，建议使用如下方式：
                adaptorInst = adptCls.getDeclaredConstructor().newInstance();

                Method onStartupMethod = adptCls.getMethod("onStartup",
                        new Class[] { ClassLoader.class, String.class, String.class });

                adaptorOnLoadClassMethod = adptCls.getMethod("onLoadClass",
                        new Class[] { ClassLoader.class, String.class, String.class });

                String tClassName = className.replace("/", ".");

                byte[] res = (byte[]) onStartupMethod.invoke(adaptorInst,
                        new Object[] { loader, this.uavMofRoot, tClassName });

                return res;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (adaptorInst != null && adaptorOnLoadClassMethod != null) {

            try {

                String tClassName = className.replace("/", ".");

                byte[] res = (byte[]) adaptorOnLoadClassMethod.invoke(adaptorInst,
                        new Object[] { loader, this.uavMofRoot, tClassName });

                return res;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        // /**
        // * General Class Adaptor:
        // */
        // if (this.generalAdaptorInst != null && this.generalOnLoadClassMethod != null) {
        //
        // try {
        //
        // String tClassName = className.replace("/", ".");
        //
        // byte[] res = (byte[]) generalOnLoadClassMethod.invoke(generalAdaptorInst,
        // new Object[] { loader, this.uavMofRoot, tClassName });
        //
        // return res;
        // }
        // catch (Exception e) {
        // e.printStackTrace();
        // }
        // }

        return null;
    }

}
