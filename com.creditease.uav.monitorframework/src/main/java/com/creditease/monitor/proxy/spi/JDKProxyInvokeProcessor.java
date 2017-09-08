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

package com.creditease.monitor.proxy.spi;

import java.lang.reflect.Method;

/**
 * 
 * JDKProxyInvokeProcessor description: to handle JDK proxy action
 *
 * @param <T>
 */
public abstract class JDKProxyInvokeProcessor<T> {

    /**
     * 
     * @param t
     *            the real object for this proxy
     * @param proxy
     *            the proxy object
     * @param method
     *            current calling method
     * @param args
     *            current args for the calling method
     */
    public abstract void preProcess(T t, Object proxy, Method method, Object[] args);

    /**
     * sometimes we may need catch invoking exception
     * 
     * @param t
     * @param proxy
     * @param method
     * @param args
     * @param e
     */
    public void catchInvokeException(T t, Object proxy, Method method, Object[] args, Throwable e) {

        // do nothing
    }

    /**
     * 
     * @param res
     *            the result object returned by the calling method
     * @param t
     *            the real object for this proxy
     * @param proxy
     *            the proxy object
     * @param method
     *            current calling method
     * @param args
     *            current args for the calling method
     * @return the result wish to return to the calling, if is null, the the result object is still the one from the
     *         calling method
     */
    public abstract Object postProcess(Object res, T t, Object proxy, Method method, Object[] args);

}
