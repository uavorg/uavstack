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

import javassist.ClassPool;
import javassist.CtMethod;

public class SpringBootTomcatAdaptor extends AbstractAdaptor {

    @Override
    public byte[] onStartup(ClassLoader clsLoader, String uavMofRoot, String className) {

        System.out.println("MOF.ApplicationContainer=SpringBoot.Tomcat");

        if (pool == null) {
            pool = ClassPool.getDefault();
        }

        final AbstractAdaptor aa = this;

        final String mofRoot = uavMofRoot;

        if ("org.springframework.boot.loader.Launcher".equals(className)) {
            return this.inject("org.springframework.boot.loader.Launcher",
                    new String[] { "com.creditease.uav.monitorframework.agent.interceptor" }, new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj",
                                    "com.creditease.uav.monitorframework.agent.interceptor.SpringBootTomcatIT");
                            m.insertAfter("{mObj=new SpringBootTomcatIT(\"" + mofRoot + "\"); mObj.installMOF($_);}");
                        }

                        @Override
                        public String getMethodName() {

                            return "createClassLoader";
                        }

                    });
        }
        else if ("org.springframework.boot.SpringApplication".equals(className)) {
            return this.inject("org.springframework.boot.SpringApplication",
                    new String[] { "com.creditease.uav.monitorframework.agent.interceptor" }, new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj",
                                    "com.creditease.uav.monitorframework.agent.interceptor.SpringBootTomcatIT");
                            m.insertAfter("{mObj=new SpringBootTomcatIT(\"" + mofRoot
                                    + "\"); mObj.installMOF(getClassLoader());}");
                        }

                        @Override
                        public String getMethodName() {

                            return "initialize";
                        }

                    });
        }
        return null;
    }

    @Override
    public byte[] onLoadClass(ClassLoader clsLoader, String uavMofRoot, String className) {

        this.addClassPath(clsLoader);

        final AbstractAdaptor aa = this;

        if (className.equals("org.springframework.context.support.AbstractApplicationContext"))

        {
            return this.inject(className, new String[] { "com.creditease.tomcat.plus.interceptor" },
                    new AdaptorProcessor() {

                        /**
                         * we need startServer before ApplicationContext's refresh cause some hook operation could
                         * happen when refresh.
                         */
                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.tomcat.plus.interceptor.SpringBootTomcatPlusIT");
                            m.insertBefore(
                                    "{mObj=new SpringBootTomcatPlusIT();mObj.startServer(this.getEnvironment().getProperty(\"server.port\"),this.getEnvironment().getProperty(\"server.context-path\"));}");
                        }

                        @Override
                        public String getMethodName() {

                            return "refresh";
                        }

                    });
        }

        else if (className.equals("org.apache.catalina.core.StandardEngineValve")) {
            return this.inject(className, new String[] { "com.creditease.tomcat.plus.interceptor" },
                    new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.tomcat.plus.interceptor.SpringBootTomcatPlusIT");
                            m.insertBefore(
                                    "{mObj=new SpringBootTomcatPlusIT();mObj.onServiceStart(new Object[]{this});}");
                            m.insertAfter("{mObj.onServiceEnd(new Object[]{$1,$2});}");
                        }

                        @Override
                        public String getMethodName() {

                            return "invoke";
                        }

                    });
        }

        else if (className.equals("org.apache.catalina.core.StandardContext")) {

            return this.inject(className,
                    new String[] { "com.creditease.tomcat.plus.interceptor", "org.apache.catalina.deploy",
                            "org.apache.catalina.core", "org.apache.tomcat.util.descriptor.web" },
                    new AdaptorProcessor[] { new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.tomcat.plus.interceptor.SpringBootTomcatPlusIT");
                            m.insertAfter(
                                    "{mObj=new SpringBootTomcatPlusIT();mObj.onAppStarting(new Object[]{this});                                    }");
                        }

                        @Override
                        public String getMethodName() {

                            return "mergeParameters";
                        }

                    }, new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.tomcat.plus.interceptor.SpringBootTomcatPlusIT");
                            m.insertBefore("{mObj=new SpringBootTomcatPlusIT();mObj.onAppInit(new Object[]{this});}");
                            String sb = "{mObj.onAppStart(new Object[]{this});" + "FilterDef fd=new FilterDef();"
                                    + "fd.setFilterClass(\"com.creditease.monitor.jee.filters.GlobalFilter\");"
                                    + "fd.setDisplayName(\"UAV_Global_Filter\");"
                                    + "fd.setFilterName(\"UAV_Global_Filter\");"
                                    + "ApplicationFilterConfig filterConfig = new ApplicationFilterConfig(this, fd);"
                                    + "this.filterConfigs.put(\"UAV_Global_Filter\", filterConfig);"
                                    + "FilterMap filterMap=new FilterMap();"
                                    + "filterMap.setFilterName(\"UAV_Global_Filter\");"
                                    + "filterMap.addURLPattern(\"/*\");" + "this.filterMaps.addBefore(filterMap);"
                                    + "}";

                            m.insertAfter(sb);
                        }

                        @Override
                        public String getMethodName() {

                            return "startInternal";
                        }

                    }, new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.tomcat.plus.interceptor.SpringBootTomcatPlusIT");
                            m.insertAfter("{mObj=new SpringBootTomcatPlusIT();mObj.onAppStop(new Object[]{this});}");
                        }

                        @Override
                        public String getMethodName() {

                            return "stopInternal";
                        }

                    } });
        }
        else if (className.equals("org.apache.catalina.core.ApplicationContext")) {

            return this.inject(className, new String[] { "com.creditease.tomcat.plus.interceptor" },
                    new AdaptorProcessor[] { new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.tomcat.plus.interceptor.SpringBootTomcatPlusIT");
                            m.insertAfter(
                                    "{mObj=new SpringBootTomcatPlusIT();return mObj.onServletRegist(new Object[]{$_});}");
                        }

                        @Override
                        public String getMethodName() {

                            return "getFacade";
                        }

                    } });
        }
        else if (className.equals("org.apache.catalina.core.StandardWrapper")) {
            return this.inject(className, new String[] { "com.creditease.tomcat.plus.interceptor" },
                    new AdaptorProcessor[] { new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.tomcat.plus.interceptor.SpringBootTomcatPlusIT");
                            m.insertAfter(
                                    "{mObj=new SpringBootTomcatPlusIT();mObj.onServletStart(new Object[]{this,$_});}");
                        }

                        @Override
                        public String getMethodName() {

                            return "loadServlet";
                        }

                    }, new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.tomcat.plus.interceptor.SpringBootTomcatPlusIT");
                            m.insertBefore(
                                    "{mObj=new SpringBootTomcatPlusIT();mObj.onServletStop(new Object[]{this,this.instance});}");
                        }

                        @Override
                        public String getMethodName() {

                            return "unload";
                        }

                    } });
        }
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

                            aa.addLocalVar(m, "mObj", "com.creditease.tomcat.plus.interceptor.SpringBootTomcatPlusIT");
                            m.insertBefore(
                                    "{mObj=new SpringBootTomcatPlusIT();mObj.onResourceInit(new Object[]{$1});}");
                            m.insertAfter("{$_=mObj.onResourceCreate(new Object[]{$_,$1});}");
                        }

                    } });
        }
        else if (className.equals("org.springframework.boot.context.embedded.EmbeddedWebApplicationContext")) {
            return this.inject(className, new String[] { "com.creditease.tomcat.plus.interceptor" },
                    new AdaptorProcessor[] { new AdaptorProcessor() {

                        @Override
                        public String getMethodName() {

                            return "finishRefresh";
                        }

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.tomcat.plus.interceptor.SpringBootTomcatPlusIT");
                            m.insertAfter("{mObj=new SpringBootTomcatPlusIT();mObj.onSpringFinishRefresh();}");
                        }

                    }, new AdaptorProcessor() {

                        @Override
                        public String getMethodName() {

                            return "postProcessBeanFactory";
                        }

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.tomcat.plus.interceptor.SpringBootTomcatPlusIT");
                            m.insertBefore(
                                    "{mObj=new SpringBootTomcatPlusIT();mObj.onSpringBeanRegist(this.getEnvironment().getProperty(\"server.context-path\"));}");
                        }

                    } });
        }

        return null;
    }

}
