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

package com.creditease.agent.spi;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 *
 * Description: 任务抽象类，实现类需要实现run方法
 *
 * @author chenlikai
 * 
 *         <pre>
 * Modification History: 
 * Date         Author      Version     Description 
 * ------------------------------------------------------------------ 
 * 2015年9月8日    chenlikai       1.0        1.0 Version
 *         </pre>
 */
public abstract class Abstract1NTask extends AbstractComponent implements Runnable {

    /*
     * 任务执行参数，需要传参时，put参数到此map
     */
    protected Map<String, Object> params = new HashMap<String, Object>();

    public Abstract1NTask(String name, String feature) {
        super(name, feature);
    }

    /*
     * put参数到参数map中
     */
    public void put(String pKey, Object pObj) {

        params.put(pKey, pObj);
    }

    /*
     * get 参数
     */
    public Object get(String pKey) {

        return params.get(pKey);
    }

    public <T> void put(Class<T> c, T t) {

        if (null == c || null == t) {
            return;
        }

        params.put(c.getName(), t);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> c) {

        if (null == c) {
            return null;
        }

        return (T) params.get(c.getName());
    }

}
