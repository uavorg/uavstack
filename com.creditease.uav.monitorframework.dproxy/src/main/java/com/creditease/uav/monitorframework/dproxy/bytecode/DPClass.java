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
 * DPClass description: ???
 *
 */
public class DPClass {

    private Object ctClass;

    private URLClassLoader mofExtClassLoader;

    public DPClass(Object ctClass) {

        mofExtClassLoader = (URLClassLoader) System.getProperties().get("org.uavstack.mof.ext.clsloader");

        this.ctClass = ctClass;
    }

    public String getSimpleName() {

        String name = (String) ReflectionHelper.invoke("javassist.CtClass", ctClass, "getSimpleName", null, null,
                mofExtClassLoader);

        return name;
    }

    public String getName() {

        String name = (String) ReflectionHelper.invoke("javassist.CtClass", ctClass, "getName", null, null,
                mofExtClassLoader);

        return name;
    }
}
