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

import java.util.Properties;
import java.util.jar.Manifest;

import com.creditease.uav.monitorframework.agent.util.JarUtil;

import javassist.ClassPool;
import javassist.CtMethod;

public class TomcatAdaptor extends AbstractAdaptor {

    private String currentVersion = "";

    private void detectTomcatVersion() {

        if (!currentVersion.equals("")) {
            return;
        }

        // get server home
        String appServerHome = System.getProperty("catalina.home", System.getProperty("user.dir"));

        // get version from ServerInfo.properties
        Properties p = JarUtil.getResourceAsProperties(appServerHome + "/lib/catalina.jar",
                "org/apache/catalina/util/ServerInfo.properties");

        if (null == p) {
            throw new RuntimeException("Read Tomcat version FAIL.");
        }
        String version = p.getProperty("server.number");

        String[] versions = version.split("\\.");

        String ver = versions[0];

        try {
            Integer.parseInt(ver);
        }
        catch (NumberFormatException e) {

            // get version from Manifest
            Manifest man = JarUtil.getJarManifest(appServerHome + "/lib/tomcat-coyote.jar");

            version = man.getMainAttributes().getValue("Implementation-Version");

            versions = version.split("\\.");

            ver = versions[0];
        }

        currentVersion = ver;
    }

    @Override
    public byte[] onStartup(ClassLoader clsLoader, String uavMofRoot, String className) {

        System.out.println("MOF.ApplicationContainer=Tomcat");

        if (pool == null) {
            pool = ClassPool.getDefault();
        }

        // get tomcat version
        this.detectTomcatVersion();

        final AbstractAdaptor aa = this;

        final String mofRoot = uavMofRoot;

        return this.inject("org.apache.catalina.startup.Bootstrap",
                new String[] { "com.creditease.uav.monitorframework.agent.interceptor" }, new AdaptorProcessor() {

                    @Override
                    public void process(CtMethod m) throws Exception {

                        aa.addLocalVar(m, "mObj", "com.creditease.uav.monitorframework.agent.interceptor.TomcatIT");
                        m.insertAfter("{mObj=new TomcatIT(\"" + mofRoot + "\");return mObj.installMOF($_,$1);}");
                    }

                    @Override
                    public String getMethodName() {

                        return "createClassLoader";
                    }

                });
    }

    @Override
    public byte[] onLoadClass(ClassLoader clsLoader, final String uavMofRoot, String className) {

        this.addClassPath(clsLoader);

        final AbstractAdaptor aa = this;

        // startServer
        if (className.equals("org.apache.catalina.startup.Catalina")) {

            return this.inject(className, new String[] { "com.creditease.tomcat.plus.interceptor" },
                    new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.tomcat.plus.interceptor.TomcatPlusIT");
                            m.insertBefore("{mObj=new TomcatPlusIT();mObj.startServer();}");
                        }

                        @Override
                        public String getMethodName() {

                            return "start";
                        }

                    });

        }
        // onServiceInit
        else if (className.equals("org.apache.catalina.connector.Connector")) {

            final String version = this.currentVersion;

            return this.inject(className, new String[] { "com.creditease.tomcat.plus.interceptor" },
                    new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.tomcat.plus.interceptor.TomcatPlusIT");
                            m.insertAfter("{mObj=new TomcatPlusIT();mObj.onServiceInit(new Object[]{this});}");
                        }

                        @Override
                        public String getMethodName() {

                            return (version.equalsIgnoreCase("6")) ? "initialize" : "initInternal";
                        }

                    });
        }
        // onServiceStart/onServiceEnd
        else if (className.equals("org.apache.catalina.core.StandardEngineValve")) {
            return this.inject(className, new String[] { "com.creditease.tomcat.plus.interceptor" },
                    new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.tomcat.plus.interceptor.TomcatPlusIT");
                            m.insertBefore("{mObj=new TomcatPlusIT();mObj.onServiceStart(new Object[]{this,$1,$2});}");
                            m.insertAfter("{mObj.onServiceEnd(new Object[]{$1,$2});}");
                        }

                        @Override
                        public String getMethodName() {

                            return "invoke";
                        }

                    });
        }
        // onAppStarting/onAppInit/onAppStart/onAppStop/GlobalFilter
        else if (className.equals("org.apache.catalina.core.StandardContext")) {
            final String version = this.currentVersion;

            return this.inject(className,
                    new String[] { "com.creditease.tomcat.plus.interceptor", "org.apache.catalina.deploy",
                            "org.apache.catalina.core", "org.apache.tomcat.util.descriptor.web" },
                    new AdaptorProcessor[] { new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.tomcat.plus.interceptor.TomcatPlusIT");
                            m.insertAfter(
                                    "{mObj=new TomcatPlusIT();mObj.onAppStarting(new Object[]{this});                                    }");
                        }

                        @Override
                        public String getMethodName() {

                            return "mergeParameters";
                        }

                    }, new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.tomcat.plus.interceptor.TomcatPlusIT");
                            m.insertBefore("{mObj=new TomcatPlusIT();mObj.onAppInit(new Object[]{this});}");
                            String sb = "{mObj.onAppStart(new Object[]{this});" + "FilterDef fd=new FilterDef();"
                                    + "fd.setFilterClass(\"com.creditease.monitor.jee.filters.GlobalFilter\");"
                                    + "fd.setDisplayName(\"UAV_Global_Filter\");"
                                    + "fd.setFilterName(\"UAV_Global_Filter\");"
                                    + "ApplicationFilterConfig filterConfig = new ApplicationFilterConfig(this, fd);"
                                    + "this.filterConfigs.put(\"UAV_Global_Filter\", filterConfig);"
                                    + "FilterMap filterMap=new FilterMap();"
                                    + "filterMap.setFilterName(\"UAV_Global_Filter\");"
                                    + "filterMap.addURLPattern(\"/*\");";

                            if (version.equalsIgnoreCase("6")) {
                                sb += "synchronized (filterMaps) {"
                                        + "FilterMap results[] =new FilterMap[filterMaps.length + 1];"
                                        + "System.arraycopy(filterMaps, 0, results, 1, filterMaps.length);"
                                        + "results[0] = filterMap;filterMaps = results;}";
                            }
                            else {
                                sb += "this.filterMaps.addBefore(filterMap);";
                            }
                            sb += "}";

                            m.insertAfter(sb);
                        }

                        @Override
                        public String getMethodName() {

                            return (version.equalsIgnoreCase("6")) ? "start" : "startInternal";
                        }

                    }, new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.tomcat.plus.interceptor.TomcatPlusIT");
                            /**
                             * NOTE: we have to do UAVMOF stop before App Stop as we need use some objects such as
                             * ApplicationContext
                             */
                            m.insertBefore("{mObj=new TomcatPlusIT();mObj.onAppStop(new Object[]{this});}");
                        }

                        @Override
                        public String getMethodName() {

                            return (version.equalsIgnoreCase("6")) ? "stop" : "stopInternal";
                        }

                    } });
        }
        // onServletRegist
        else if (className.equals("org.apache.catalina.core.ApplicationContext")) {

            return this.inject(className, new String[] { "com.creditease.tomcat.plus.interceptor" },
                    new AdaptorProcessor[] { new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.tomcat.plus.interceptor.TomcatPlusIT");
                            m.insertAfter("{mObj=new TomcatPlusIT();return mObj.onServletRegist(new Object[]{$_});}");
                        }

                        @Override
                        public String getMethodName() {

                            return "getFacade";
                        }

                    } });
        }
        // onServletStart/onServletStop
        else if (className.equals("org.apache.catalina.core.StandardWrapper")) {
            return this.inject(className, new String[] { "com.creditease.tomcat.plus.interceptor" },
                    new AdaptorProcessor[] { new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.tomcat.plus.interceptor.TomcatPlusIT");
                            m.insertAfter("{mObj=new TomcatPlusIT();mObj.onServletStart(new Object[]{this,$_});}");
                        }

                        @Override
                        public String getMethodName() {

                            return "loadServlet";
                        }

                    }, new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.tomcat.plus.interceptor.TomcatPlusIT");
                            m.insertBefore(
                                    "{mObj=new TomcatPlusIT();mObj.onServletStop(new Object[]{this,this.instance});}");
                        }

                        @Override
                        public String getMethodName() {

                            return "unload";
                        }

                    } });
        }
        // onDeployUAVApp
        else if (className.equals("org.apache.catalina.startup.HostConfig")) {

            final String curVersion = this.currentVersion;
            return this.inject(className, new String[] { "com.creditease.tomcat.plus.interceptor" },
                    new AdaptorProcessor[] { new AdaptorProcessor() {

                        @Override
                        public String getMethodName() {

                            return "deployDirectories";
                        }

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.tomcat.plus.interceptor.TomcatPlusIT");
                            m.insertAfter(
                                    "{mObj=new TomcatPlusIT();mObj.onDeployUAVApp(new Object[]{this,this.host,$1,\""
                                            + uavMofRoot + "\",\"" + curVersion + "\"});}");
                        }

                    } });
        }
        // onResourceInit/onResourceCreate
        else if (
        // after Tomcat 8
        className.equals("org.apache.naming.factory.FactoryBase") ||
        // before Tomcat 7
                className.equals("org.apache.naming.factory.ResourceFactory")) {
            return this.inject(className, new String[] { "com.creditease.tomcat.plus.interceptor" },
                    new AdaptorProcessor[] { new AdaptorProcessor() {

                        @Override
                        public String getMethodName() {

                            return "getObjectInstance";
                        }

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.tomcat.plus.interceptor.TomcatPlusIT");
                            m.insertBefore("{mObj=new TomcatPlusIT();mObj.onResourceInit(new Object[]{$1});}");
                            m.insertAfter("{$_=mObj.onResourceCreate(new Object[]{$_,$1});}");
                        }

                    } });
        }

        return null;
    }

}
