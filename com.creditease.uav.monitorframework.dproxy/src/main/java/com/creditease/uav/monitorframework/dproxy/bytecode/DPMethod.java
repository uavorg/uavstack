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

/*-
 * <<
 * UAVStack
 * ==
 * Copyright (C) 2016 - 2018 UAVStack
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
package com.creditease.uav.monitorframework.dproxy.bytecode;

import java.net.URLClassLoader;

import com.creditease.agent.helpers.ReflectionHelper;

/**
 * CtMethod description: deletegate class for Javassit CtMethod
 *
 */
public class DPMethod {

    private Object ctMethod;

    private URLClassLoader mofExtClassLoader;

    public DPMethod(Object ctMethod) {

        mofExtClassLoader = (URLClassLoader) System.getProperties().get("org.uavstack.mof.ext.clsloader");

        this.ctMethod = ctMethod;
    }

    public void insertBefore(String src) {

        ReflectionHelper.invoke("javassist.CtMethod", ctMethod, "insertBefore", new Class[] { String.class },
                new Object[] { src }, mofExtClassLoader);

    }

    public void insertAfter(String src) {

        ReflectionHelper.invoke("javassist.CtMethod", ctMethod, "insertAfter", new Class[] { String.class },
                new Object[] { src }, mofExtClassLoader);
    }

    public String getName() {

        String name = (String) ReflectionHelper.invoke("javassist.CtMethod", ctMethod, "getName", null, null,
                mofExtClassLoader);

        return name;
    }

    public Object getCtMethod() {

        return this.ctMethod;
    }

    public DPClass[] getParameterTypes() {

        return callMethodGetCtClsArray("getParameterTypes");
    }

    public DPClass getReturnType() {

        Object ctClsObj = ReflectionHelper.invoke("javassist.CtMethod", ctMethod, "getReturnType", null, null,
                mofExtClassLoader);

        DPClass dpcls = new DPClass(ctClsObj);

        return dpcls;
    }

    public DPClass[] getExceptionTypes() {

        return callMethodGetCtClsArray("getExceptionTypes");
    }

    /**
     * @return
     */
    private DPClass[] callMethodGetCtClsArray(String methodName) {

        Object[] ctClsArray = (Object[]) ReflectionHelper.invoke("javassist.CtMethod", ctMethod, methodName, null, null,
                mofExtClassLoader);

        DPClass[] dpClsArray = new DPClass[ctClsArray.length];

        int index = 0;

        for (Object ctClsObj : ctClsArray) {

            DPClass dpcls = new DPClass(ctClsObj);

            dpClsArray[index] = dpcls;

            index++;
        }

        return dpClsArray;
    }
}
