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

package com.alibaba.ttl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * TtlRunnable动态代理类，适配当除实现runnable接口外其他接口情况
 *
 */
public class TtlRunnableProxy implements InvocationHandler {

    private TtlRunnable target;

    /**
     * 
     */
    public TtlRunnableProxy(TtlRunnable target) {

        this.target = target;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (method.getName().equals("run")) {
            return method.invoke(this.target, args);
        }
        else {
            return method.invoke(this.target.getRunnable(), args);
        }
    }

}
