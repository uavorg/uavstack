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

public class JettyAdaptor extends AbstractAdaptor {

    private void detectJettyVersion() {

        // TODO don't know if need detect version
    }

    @Override
    public byte[] onStartup(ClassLoader clsLoader, String uavMofRoot, String className) {

        System.out.println("MOF.ApplicationContainer=Jetty");

        if (pool == null) {
            pool = ClassPool.getDefault();
        }

        // get tomcat version
        this.detectJettyVersion();

        final AbstractAdaptor aa = this;

        final String mofRoot = uavMofRoot;

        return this.inject("org.eclipse.jetty.start.Classpath",
                new String[] { "com.creditease.uav.monitorframework.agent.interceptor" }, new AdaptorProcessor() {

                    @Override
                    public void process(CtMethod m) throws Exception {

                        aa.addLocalVar(m, "mObj", "com.creditease.uav.monitorframework.agent.interceptor.JettyIT");
                        m.insertAfter("{mObj=new JettyIT(\"" + mofRoot + "\");return mObj.installMOF($_);}");
                    }

                    @Override
                    public String getMethodName() {

                        return "getClassLoader";
                    }
                });
    }

    @Override
    public byte[] onLoadClass(ClassLoader clsLoader, final String uavMofRoot, String className) {

        this.addClassPath(clsLoader);

        final AbstractAdaptor aa = this;

        // startServer
        if (className.equals("org.eclipse.jetty.server.Server")) {

            return this.inject(className, new String[] { "com.creditease.uav.jetty.plus.interceptor" },
                    new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.uav.jetty.plus.interceptor.JettyPlusIT");
                            m.insertBefore("{mObj=new JettyPlusIT();mObj.startServer(new Object[]{this});}");
                        }

                        @Override
                        public String getMethodName() {

                            return "doStart";
                        }

                    });

        }
        // onServiceEnd
        if (className.equals("org.eclipse.jetty.server.session.SessionHandler")) {
            return this.inject(className, new String[] { "com.creditease.uav.jetty.plus.interceptor" },
                    new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.uav.jetty.plus.interceptor.JettyPlusIT");
                            m.insertAfter("{mObj=new JettyPlusIT();mObj.onServiceEnd(new Object[]{$3,$4});}");
                        }

                        @Override
                        public String getMethodName() {

                            return "doScope";
                        }

                    });
        }
        // onAppInit
        else if (className.equals("org.eclipse.jetty.deploy.providers.ScanningAppProvider")) {

            return this.inject(className, new String[] { "com.creditease.uav.jetty.plus.interceptor" },
                    new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.uav.jetty.plus.interceptor.JettyPlusIT");
                            m.insertAfter("{mObj=new JettyPlusIT();mObj.onAppInit(new Object[]{$_});}");
                        }

                        @Override
                        public String getMethodName() {

                            return "createApp";
                        }

                    }, new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.uav.jetty.plus.interceptor.JettyPlusIT");
                            m.insertAfter("{mObj=new JettyPlusIT();mObj.onDeployUAVApp(new Object[]{this,\""
                                    + uavMofRoot + "\"});}");
                        }

                        @Override
                        public String getMethodName() {

                            return "doStart";
                        }

                    });
        }
        // onAppStarting
        else if (className.equals("org.eclipse.jetty.webapp.WebAppContext")) {

            return this.inject(className, new String[] { "com.creditease.uav.jetty.plus.interceptor" },
                    new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.uav.jetty.plus.interceptor.JettyPlusIT");
                            m.insertAfter("{mObj=new JettyPlusIT();mObj.onAppStarting(new Object[]{this});}");
                        }

                        @Override
                        public String getMethodName() {

                            return "configure";
                        }

                    });
        }
        // onAppStart
        else if (className.equals("org.eclipse.jetty.deploy.bindings.StandardStarter")) {

            return this.inject(className, new String[] { "com.creditease.uav.jetty.plus.interceptor" },
                    new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.uav.jetty.plus.interceptor.JettyPlusIT");
                            m.insertAfter("{mObj=new JettyPlusIT();mObj.onAppStart(new Object[]{$2});}");
                        }

                        @Override
                        public String getMethodName() {

                            return "processBinding";
                        }

                    });
        }
        // onAppStop
        else if (className.equals("org.eclipse.jetty.deploy.bindings.StandardStopper")) {

            return this.inject(className, new String[] { "com.creditease.uav.jetty.plus.interceptor" },
                    new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.uav.jetty.plus.interceptor.JettyPlusIT");
                            m.insertBefore("{mObj=new JettyPlusIT();mObj.onAppStop(new Object[]{$2});}");
                        }

                        @Override
                        public String getMethodName() {

                            return "processBinding";
                        }

                    });
        }
        // onServletStart/onServletStop
        else if (className.equals("org.eclipse.jetty.servlet.ServletHolder")) {

            return this.inject(className, new String[] { "com.creditease.uav.jetty.plus.interceptor" },
                    new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.uav.jetty.plus.interceptor.JettyPlusIT");
                            m.insertAfter("{mObj=new JettyPlusIT();mObj.onServletStart(new Object[]{this});}");
                        }

                        @Override
                        public String getMethodName() {

                            return "initServlet";
                        }

                    }, new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.uav.jetty.plus.interceptor.JettyPlusIT");
                            m.insertBefore("{mObj=new JettyPlusIT();mObj.onServletStop(new Object[]{this});}");
                        }

                        @Override
                        public String getMethodName() {

                            return "doStop";
                        }

                    });
        }
        // onServletRegist 1
        else if (className.equals("org.eclipse.jetty.server.Request")) {
            return this.inject(className, new String[] { "com.creditease.uav.jetty.plus.interceptor" },
                    new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.uav.jetty.plus.interceptor.JettyPlusIT");
                            m.insertAfter("{mObj=new JettyPlusIT();}");
                            m.insertAfter("{$_=mObj.onWrapServletContext(new Object[]{$_});}");
                        }

                        @Override
                        public String getMethodName() {

                            return "getServletContext";
                        }

                    });
        }
        // onServletRegist 2 / onServiceStart
        else if (className.equals("org.eclipse.jetty.server.handler.ContextHandler")) {
            return this.inject(className, new String[] { "com.creditease.uav.jetty.plus.interceptor" },
                    new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.uav.jetty.plus.interceptor.JettyPlusIT");
                            m.insertBefore(
                                    "{mObj=new JettyPlusIT();$2=new ServletContextEventWrapper(mObj.onServletRegist(new Object[]{$2}));}");
                        }

                        @Override
                        public String getMethodName() {

                            return "callContextInitialized";
                        }

                    }, new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.uav.jetty.plus.interceptor.JettyPlusIT");
                            m.insertBefore(
                                    "{mObj=new JettyPlusIT();$2=new ServletContextEventWrapper(mObj.onServletRegist(new Object[]{$2}));}");
                        }

                        @Override
                        public String getMethodName() {

                            return "callContextDestroyed";
                        }

                    }, new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.uav.jetty.plus.interceptor.JettyPlusIT");
                            m.insertBefore("{mObj=new JettyPlusIT();}");
                            m.insertAfter("{$_=mObj.onWrapServletContext(new Object[]{$_});}");
                        }

                        @Override
                        public String getMethodName() {

                            return "getServletContext";
                        }

                    }, new AdaptorProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            aa.addLocalVar(m, "mObj", "com.creditease.uav.jetty.plus.interceptor.JettyPlusIT");
                            m.insertBefore("{mObj=new JettyPlusIT();mObj.onServiceStart(new Object[]{$3,$4});}");

                        }

                        @Override
                        public String getMethodName() {

                            return "doScope";
                        }

                    }

            );
        }

        return null;
    }

}
