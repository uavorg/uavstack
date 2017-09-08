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

package com.creditease.uav.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import com.creditease.monitor.proxy.spi.JDKProxyInvokeHandler;

public class JDKProxyInvokeUtil {

    public static boolean isJDKProxy(Object t) {

        if (!Proxy.isProxyClass(t.getClass())) {
            return false;
        }

        InvocationHandler ih = Proxy.getInvocationHandler(t);

        if (ih == null) {
            return false;
        }

        return JDKProxyInvokeHandler.class.isAssignableFrom(ih.getClass());

    }

    @SuppressWarnings("unchecked")
    public static <T> T newProxyInstance(ClassLoader loader, Class<?>[] interfaces, JDKProxyInvokeHandler<T> ih) {

        if (isJDKProxy(ih.getTarget())) {
            return ih.getTarget();
        }

        return (T) Proxy.newProxyInstance(loader, interfaces, ih);

    }

}
