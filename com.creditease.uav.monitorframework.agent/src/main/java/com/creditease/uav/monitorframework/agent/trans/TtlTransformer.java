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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

public class TtlTransformer implements ClassFileTransformer {

    private ClassLoader mofLoader;
    private Object adaptorInst;
    private static final String ADAPTOR_CLASS = "com.creditease.uav.monitorframework.adaptors.TtlAdaptor";

    public TtlTransformer(ClassLoader mofLoader) {
        this.mofLoader = mofLoader;
    }

    private static final String THREAD_POOL_CLASS_FILE = "java.util.concurrent.ThreadPoolExecutor".replace('.', '/');
    private static final String SCHEDULER_CLASS_FILE = "java.util.concurrent.ScheduledThreadPoolExecutor".replace('.',
            '/');
    private static final String TIMER_TASK_CLASS_FILE = "java.util.TimerTask".replace('.', '/');

    @Override
    public byte[] transform(ClassLoader loader, String classFile, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classFileBuffer) throws IllegalClassFormatException {

        if (THREAD_POOL_CLASS_FILE.equals(classFile) || SCHEDULER_CLASS_FILE.equals(classFile)
                || TIMER_TASK_CLASS_FILE.equals(classFile)) {
            try {
                Class<?> adptCls = this.mofLoader.loadClass(ADAPTOR_CLASS);
                adaptorInst = adptCls.newInstance();
                Method threadPoolMethod = adptCls.getMethod("adaptorTransform",
                        new Class[] { byte[].class, ClassLoader.class, String.class });
                byte[] res = (byte[]) threadPoolMethod.invoke(adaptorInst,
                        new Object[] { classFileBuffer, this.mofLoader, classFile });
                return res;
            }
            catch (Throwable t) {
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                t.printStackTrace(printWriter);
                String msg = "Fail to transform class " + classFile + ", cause: " + stringWriter.toString();
                throw new IllegalStateException(msg, t);
            }
        }
        else {
            return new byte[] {};
        }

    }

}
