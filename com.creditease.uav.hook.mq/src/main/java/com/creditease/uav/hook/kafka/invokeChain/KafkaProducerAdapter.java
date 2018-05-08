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

package com.creditease.uav.hook.kafka.invokeChain;

import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.uav.apm.invokechain.span.Span;
import com.creditease.uav.apm.invokechain.spi.InvokeChainAdapter;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.apm.invokechain.spi.InvokeChainContext;

public class KafkaProducerAdapter extends InvokeChainAdapter {

    @Override
    public void beforePreCap(InvokeChainContext context, Object[] args) {
        context.put(InvokeChainConstants.CLIENT_IT_KEY, DataConvertHelper
                .toInt(System.getProperty("com.creditease.uav.invokechain.code.kafka.producer.key"), 0));
        context.put(InvokeChainConstants.CLIENT_IT_CLASS,
                System.getProperty("com.creditease.uav.invokechain.code.kafka.producer.class"));

    }

    @Override
    public void afterPreCap(InvokeChainContext context, Object[] args) {
        ProducerConfig producerConfig = (ProducerConfig) args[0];
        Map<String, Object> config = (Map<String, Object>) producerConfig.originals();

        String storeKey = (String) context.get(InvokeChainConstants.CLIENT_SPAN_THREADLOCAL_STOREKEY);
        Span span = this.spanFactory.getSpanFromContext(storeKey);
        String spanMeta = this.spanFactory.getSpanMeta(span);

        config.put(InvokeChainConstants.PARAM_MQHEAD_SPANINFO, spanMeta);
    }

    @Override
    public void beforeDoCap(InvokeChainContext context, Object[] args) {

    }

    @Override
    public void afterDoCap(InvokeChainContext context, Object[] args) {

    }

}
