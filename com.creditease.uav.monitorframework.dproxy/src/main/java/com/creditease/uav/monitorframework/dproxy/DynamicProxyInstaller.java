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

package com.creditease.uav.monitorframework.dproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;

import com.creditease.agent.helpers.StringHelper;
import com.creditease.monitor.proxy.spi.JDKProxyInvokeHandler;
import com.creditease.uav.common.BaseComponent;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

/**
 * 
 * DynamicProxyInstaller description: use byteCode tech to hack class, haha haha haha....
 *
 */
public class DynamicProxyInstaller extends BaseComponent {

    @SuppressWarnings("rawtypes")
    public static Object adapt(Object t) {

        if (!Proxy.isProxyClass(t.getClass())) {
            return t;
        }

        InvocationHandler ih = Proxy.getInvocationHandler(t);

        if (ih == null) {
            return t;
        }

        if (!JDKProxyInvokeHandler.class.isAssignableFrom(ih.getClass())) {
            return t;
        }

        JDKProxyInvokeHandler jpih = (JDKProxyInvokeHandler) ih;

        return jpih.getTarget();
    }

    private ClassPool pool;

    private URLClassLoader cl;

    public DynamicProxyInstaller() {

        pool = ClassPool.getDefault();
    }

    /**
     * which classloader should be done with the hack class, if not set, the default is current classloader
     * 
     * @param cl
     */
    public void setTargetClassLoader(ClassLoader cl) {

        this.cl = (URLClassLoader) cl;

        pool.insertClassPath(new LoaderClassPath(cl));
    }

    /**
     * remove class path for loader
     */
    public void releaseTargetClassLoader() {

        if (cl == null) {
            return;
        }

        pool.removeClassPath(new LoaderClassPath(cl));
    }

    /**
     * define a field to a class
     * 
     * @param fieldName
     * @param fieldClass
     * @param varClass
     * @param initCode
     *            Constructor Code
     */
    public void defineField(String fieldName, Class<?> fieldClass, String varClass, String initCode) {

        CtClass cc;

        try {
            cc = pool.get(varClass);

            String fd = "private " + fieldClass.getName() + " " + fieldName;

            if (StringHelper.isEmpty(initCode)) {
                fd += ";";
            }
            else {
                fd += "=" + initCode + ";";
            }

            CtField f = CtField.make(fd, cc);

            cc.addField(f);
        }
        catch (NotFoundException e) {
            this.logger.warn(
                    "Do DynamicProxyInstall FAIL for define Field [" + fieldName + "] in class[" + varClass + "].", e);
        }
        catch (CannotCompileException e) {
            this.logger.warn(
                    "Do DynamicProxyInstall FAIL for define Field [" + fieldName + "] in class[" + varClass + "].", e);
        }

    }

    /**
     * define a local var
     * 
     * @param m
     * @param varName
     * @param varClass
     */
    public void defineLocalVal(CtMethod m, String varName, Class<?> varClass) {

        CtClass cc;
        try {
            cc = pool.get(varClass.getName());
            m.addLocalVariable(varName, cc);
        }
        catch (NotFoundException e) {
            this.logger.warn(
                    "Do DynamicProxyInstall FAIL for define LocalVar [" + varName + "] in method[" + m.getName() + "].",
                    e);
        }
        catch (CannotCompileException e) {
            this.logger.warn(
                    "Do DynamicProxyInstall FAIL for define LocalVar [" + varName + "] in method[" + m.getName() + "].",
                    e);
        }

    }

    /**
     * add catch block code
     * 
     * @param m
     * @param catchCodeBlock
     */
    public void addCatch(CtMethod m, String catchCodeBlock) {

        try {
            CtClass throwExp = pool.get(Throwable.class.getName());

            m.addCatch("{" + catchCodeBlock + ";throw $e;}", throwExp);
        }
        catch (NotFoundException e) {
            this.logger.warn("Do DynamicProxyInstall FAIL for add Catch Block in method[" + m.getName() + "].", e);
        }
        catch (CannotCompileException e) {
            this.logger.warn("Do DynamicProxyInstall FAIL for add Catch Block in method[" + m.getName() + "].", e);
        }
    }

    /**
     * install the proxy code
     * 
     * @param className
     *            the target class to be hacked
     * @param importPackages
     *            sometimes need import some new class package for the code
     * @param p
     *            the processor to finsih hacking
     */
    public void installProxy(String className, String[] importPackages, DynamicProxyProcessor p,
            boolean toSystemClassloader) {

        try {

            CtClass cc = pool.get(className);

            if (importPackages != null) {
                for (String ip : importPackages) {
                    pool.importPackage(ip);
                }
            }

            CtMethod[] ml = cc.getDeclaredMethods();

            for (CtMethod m : ml) {
                try {
                    p.process(m);
                }
                catch (Exception e) {
                    this.logger.error(
                            "Do DynamicProxyInstall FAIL for class[" + className + "] method[" + m.getName() + "].", e);
                }
            }

            if (this.cl == null || toSystemClassloader == true) {
                cc.toClass();
            }
            else {
                cc.toClass(this.cl, null);
            }
            cc.detach();
        }
        catch (NotFoundException e) {
            // ignore
            // this.logger.warn("Do DynamicProxyInstall FAIL for class[" + className + "].", e);
        }
        catch (CannotCompileException e) {
            this.logger.warn("Do DynamicProxyInstall FAIL for class[" + className + "] as Can't Compile.", e);
        }

    }

    /**
     * 
     * @param adapts
     *            a Map 结构类似 类名 方法名 入参类型列表 第几个参数进行替换，如果是-1代表对返回值替换
     *            {"org.apache.cxf.frontend.ClientProxy":{"getClient":{args:["java.lang.Object"],target:0}}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void doAdapts(Map adapts) {

        if (adapts == null) {
            return;
        }

        for (Object clz : adapts.keySet()) {

            String cls = (String) clz;

            final Map<String, Map> methods = (Map<String, Map>) adapts.get(clz);

            this.installProxy(cls, new String[] { "com.creditease.uav.monitorframework.dproxy" },
                    new DynamicProxyProcessor() {

                        @Override
                        public void process(CtMethod m) throws Exception {

                            Map cfg = methods.get(m.getName());

                            // match method name
                            if (cfg == null) {
                                return;
                            }

                            // match params length
                            CtClass[] pClsLs = m.getParameterTypes();

                            List<String> pClsNamesLs = (List<String>) cfg.get("args");

                            if (pClsLs.length != pClsNamesLs.size()) {
                                return;
                            }

                            // match params types
                            for (int i = 0; i < pClsNamesLs.size(); i++) {

                                CtClass pCls = pClsLs[0];

                                if (!pCls.getName().equals(pClsNamesLs.get(i))) {
                                    return;
                                }

                            }

                            int pIndex = (Integer) cfg.get("target") + 1;

                            if (pIndex > 0) {
                                m.insertBefore("{$" + pIndex + "=DynamicProxyInstaller.adapt($" + pIndex + ");}");
                            }
                            else {
                                m.insertAfter("{$_=DynamicProxyInstaller.adapt($_);}");
                            }
                        }

                    }, false);

        }
    }

}
