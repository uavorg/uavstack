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

package com.creditease.uav.apm.invokechain.spi;

import com.creditease.uav.apm.invokechain.span.SpanFactory;
import com.creditease.uav.common.BaseComponent;

/**
 * 
 * InvokeChainAdapter description: 将输入参数转换为InvokeChain可识别协议的参数列表
 *
 */
public abstract class InvokeChainAdapter extends BaseComponent {

    protected SpanFactory spanFactory = new SpanFactory();

    /**
     * beforePreCap
     * 
     * @param params
     * @param args
     */
    public abstract void beforePreCap(InvokeChainContext context, Object[] args);

    /**
     * afterPreCap
     * 
     * @param params
     * @param args
     */
    public abstract void afterPreCap(InvokeChainContext context, Object[] args);

    /**
     * beforeDoCap
     * 
     * @param params
     * @param args
     */
    public abstract void beforeDoCap(InvokeChainContext context, Object[] args);

    /**
     * afterDoCap
     * 
     * @param params
     * @param args
     */
    public abstract void afterDoCap(InvokeChainContext context, Object[] args);
}
