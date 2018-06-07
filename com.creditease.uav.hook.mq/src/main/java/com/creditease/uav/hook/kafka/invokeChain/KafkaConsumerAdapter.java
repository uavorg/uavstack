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
package com.creditease.uav.hook.kafka.invokeChain;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.uav.apm.invokechain.spi.InvokeChainAdapter;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.apm.invokechain.spi.InvokeChainContext;

public class KafkaConsumerAdapter extends InvokeChainAdapter {

    @Override
    public void beforePreCap(InvokeChainContext context, Object[] args) {
        context.put(InvokeChainConstants.CLIENT_IT_KEY, DataConvertHelper
                .toInt(System.getProperty("com.creditease.uav.invokechain.code.kafka.consumer.key"), 0));
        context.put(InvokeChainConstants.CLIENT_IT_CLASS,
                System.getProperty("com.creditease.uav.invokechain.code.kafka.consumer.class"));
    }

    @Override
    public void afterPreCap(InvokeChainContext context, Object[] args) {

    }

    @Override
    public void beforeDoCap(InvokeChainContext context, Object[] args) {

    }

    @Override
    public void afterDoCap(InvokeChainContext context, Object[] args) {
    }

}
