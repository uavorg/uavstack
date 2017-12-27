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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.creditease.uav.common.BaseComponent;

/**
 * 
 * JDKProxyInvokeHandler description: the JDK Proxy implementation, although this tech is not so cool, but it does work
 *
 * @param <T>
 */
public class JDKProxyInvokeHandler<T> extends BaseComponent implements InvocationHandler {

    private T target;

    private JDKProxyInvokeProcessor<T> processor;

    public JDKProxyInvokeHandler(T t, JDKProxyInvokeProcessor<T> processor) {
        this.target = t;
        this.processor = processor;
    }

    public Object getTargetObj() {

        return target;
    }

    public T getTarget() {

        return target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Object res = null;
        try {
            this.processor.preProcess(this.target, proxy, method, args);
        }
        catch (Exception e) {
            logger.warn("Do Proxy PreProcess FAIL.", e);
        }

        try {
			method.setAccessible(true);
            res = method.invoke(this.target, args);
        }
        catch (InvocationTargetException e) {
            this.processor.catchInvokeException(this.target, proxy, method, args, e);
            throw e.getTargetException();
        }
        catch (Throwable e) {
            this.processor.catchInvokeException(this.target, proxy, method, args, e);
            throw e;
        }

        try {
            Object res2 = this.processor.postProcess(res, this.target, proxy, method, args);

            if (res2 != null) {
                res = res2;
            }
        }
        catch (Exception e) {
            logger.warn("Do Proxy PreProcess FAIL.", e);
        }

        return res;
    }

}
