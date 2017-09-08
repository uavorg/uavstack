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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

public class TtlAdaptor {

    private static final String RUNNABLE_CLASS_NAME = "java.lang.Runnable";
    private static final String CALLABLE_CLASS_NAME = "java.util.concurrent.Callable";

    private static final String TTL_RUNNABLE_CLASS_NAME = "com.alibaba.ttl.TtlRunnable";
    private static final String TTL_CALLABLE_CLASS_NAME = "com.alibaba.ttl.TtlCallable";

    private static final String THREAD_POOL_CLASS_FILE = "java.util.concurrent.ThreadPoolExecutor".replace('.', '/');
    private static final String SCHEDULER_CLASS_FILE = "java.util.concurrent.ScheduledThreadPoolExecutor".replace('.',
            '/');

    private static final String TIMER_TASK_CLASS_FILE = "java.util.TimerTask".replace('.', '/');
    private static final byte[] EMPTY_BYTE_ARRAY = {};

    public byte[] adaptorTransform(byte[] classFileBuffer, ClassLoader classLoader, String classFile) {

        try {
            // Lambda has no class file, no need to transform, just return.
            if (classFile == null) {
                return EMPTY_BYTE_ARRAY;
            }

            if (THREAD_POOL_CLASS_FILE.equals(classFile) || SCHEDULER_CLASS_FILE.equals(classFile)) {
                CtClass clazz = getCtClass(classFileBuffer, classLoader);

                for (CtMethod method : clazz.getDeclaredMethods()) {
                    updateMethod(clazz, method);
                }
                return clazz.toBytecode();
            }
            else if (TIMER_TASK_CLASS_FILE.equals(classFile)) {
                CtClass clazz = getCtClass(classFileBuffer, classLoader);
                while (true) {
                    String name = clazz.getSuperclass().getName();
                    if (Object.class.getName().equals(name)) {
                        break;
                    }
                    if (TIMER_TASK_CLASS_FILE.equals(name)) {
                        // FIXME add code here
                        return EMPTY_BYTE_ARRAY;
                    }
                }
            }
        }
        catch (Throwable t) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            t.printStackTrace(printWriter);
            String msg = "Fail to transform class " + classFile + ", cause: " + stringWriter.toString();
            throw new IllegalStateException(msg, t);
        }
        return EMPTY_BYTE_ARRAY;
    }

    private CtClass getCtClass(byte[] classFileBuffer, ClassLoader classLoader) throws IOException {

        ClassPool classPool = new ClassPool(true);
        if (null != classLoader) {
            classPool.appendClassPath(new LoaderClassPath(classLoader));
        }

        CtClass clazz = classPool.makeClass(new ByteArrayInputStream(classFileBuffer), false);
        clazz.defrost();
        return clazz;
    }

    static final Set<String> updateMethodNames = new HashSet<String>();

    static {
        updateMethodNames.add("execute");
        updateMethodNames.add("submit");
        updateMethodNames.add("schedule");
        updateMethodNames.add("scheduleAtFixedRate");
        updateMethodNames.add("scheduleWithFixedDelay");
    }

    static void updateMethod(CtClass clazz, CtMethod method) throws NotFoundException, CannotCompileException {

        if (!updateMethodNames.contains(method.getName())) {
            return;
        }
        if (method.getDeclaringClass() != clazz) {
            return;
        }
        final int modifiers = method.getModifiers();
        if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers)) {
            return;
        }

        CtClass[] parameterTypes = method.getParameterTypes();
        StringBuilder insertCode = new StringBuilder();
        for (int i = 0; i < parameterTypes.length; i++) {
            CtClass paraType = parameterTypes[i];
            if (RUNNABLE_CLASS_NAME.equals(paraType.getName())) {
                String code = String.format("$%d = %s.get($%d, false, true);", i + 1, TTL_RUNNABLE_CLASS_NAME, i + 1);
                insertCode.append(code);
            }
            else if (CALLABLE_CLASS_NAME.equals(paraType.getName())) {
                String code = String.format("$%d = %s.get($%d, false, true);", i + 1, TTL_CALLABLE_CLASS_NAME, i + 1);
                insertCode.append(code);
            }
        }
        if (insertCode.length() > 0) {
            method.insertBefore(insertCode.toString());
        }
    }
}
