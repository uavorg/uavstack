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

package com.creditease.uav.invokechain.collect.actions;

import com.creditease.agent.spi.IActionEngine;

/**
 * 处理MQ协议的producer数据
 * 
 * 当前只支持了rabbitmq producer和consumer处理逻辑相同，日后其他mq可能逻辑不同，故预留位置
 *
 */
public class SlowOperMQProducerAction extends SlowOperMQConsumerAction {

    public SlowOperMQProducerAction(String cName, String feature, IActionEngine engine) {
        super(cName, feature, engine);
    }

}
