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

public class MSCPAdaptor extends AbstractAdaptor {

    @Override
    public byte[] onStartup(ClassLoader clsLoader, String uavMofRoot, String className) {

        System.out.println("MOF.ApplicationContainer=MSCP");

        if (pool == null) {
            pool = ClassPool.getDefault();
        }

        final AbstractAdaptor aa = this;

        final String mofRoot = uavMofRoot;

        return this.inject("com.creditease.mscp.boot.MSCPBoot",
                new String[] { "com.creditease.uav.monitorframework.agent.interceptor" }, new AdaptorProcessor() {

                    @Override
                    public void process(CtMethod m) throws Exception {

                        aa.addLocalVar(m, "mObj", "com.creditease.uav.monitorframework.agent.interceptor.MSCPIT");
                        m.insertAfter("{mObj=new MSCPIT(\"" + mofRoot + "\");return mObj.installMOF($_);}");
                    }

                    @Override
                    public String getMethodName() {

                        return "createBootClassLoader";
                    }

                });
    }

    @Override
    public byte[] onLoadClass(ClassLoader clsLoader, String uavMofRoot, String className) {

        this.addClassPath(clsLoader);

        final AbstractAdaptor aa = this;

        if (className.equals("com.creditease.agent.SystemStarter")) {

            return this.inject(className, new String[] { "com.creditease.uav.mscp.plus" }, new AdaptorProcessor() {

                @Override
                public void process(CtMethod m) throws Exception {

                    aa.addLocalVar(m, "mObj", "com.creditease.uav.mscp.plus.MSCPPlusIT");
                    m.insertAfter("{mObj=new MSCPPlusIT();mObj.startServer();}");
                }

                @Override
                public String getMethodName() {

                    return "createConfigurationManager";
                }

            }, new AdaptorProcessor() {

                @Override
                public String getMethodName() {

                    return "installResources";
                }

                @Override
                public void process(CtMethod m) throws Exception {

                    aa.addLocalVar(m, "mObj", "com.creditease.uav.mscp.plus.MSCPPlusIT");
                    m.insertBefore("{mObj=new MSCPPlusIT();mObj.onAppStarting();}");
                }

            }, new AdaptorProcessor() {

                @Override
                public String getMethodName() {

                    return "installFeatures";
                }

                @Override
                public void process(CtMethod m) throws Exception {

                    aa.addLocalVar(m, "mObj", "com.creditease.uav.mscp.plus.MSCPPlusIT");
                    m.insertBefore("{mObj=new MSCPPlusIT();}");
                    m.insertAfter("{mObj.onAppStart();}");
                }

            });

        }
        else if (className.equals("com.creditease.agent.spi.AbstractBaseHttpServComponent")) {
            return this.inject(className, new String[] { "com.creditease.uav.mscp.plus" }, new AdaptorProcessor() {

                @Override
                public void process(CtMethod m) throws Exception {

                    aa.addLocalVar(m, "mObj", "com.creditease.uav.mscp.plus.MSCPPlusIT");
                    m.insertBefore("{mObj=new MSCPPlusIT();mObj.onServiceStart(new Object[]{$1});}");
                    m.insertAfter("{mObj=new MSCPPlusIT();mObj.onServiceEnd(new Object[]{this,$1});}");
                }

                @Override
                public String getMethodName() {

                    return "handleMessage";
                }

            });
        }

        return null;
    }

}
