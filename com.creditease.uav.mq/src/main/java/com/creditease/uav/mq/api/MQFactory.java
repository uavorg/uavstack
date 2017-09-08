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

package com.creditease.uav.mq.api;

import com.creditease.uav.mq.rocketmq.RocketMQConsumer;
import com.creditease.uav.mq.rocketmq.RocketMQProducer;

public class MQFactory {

    public enum QueueType {
        TOPIC, QUEUE
    }

    public enum EngineType {
        ROCKETMQ, ACTIVEMQ
    }

    public enum ConsumerType {
        PULL, PUSH
    }

    public static MQProducer createMQProducer(MQProducerConfig config) {

        if (null == config)
            return null;

        MQProducer producer = null;

        switch (config.getEngineType()) {
            case ACTIVEMQ:
                break;
            case ROCKETMQ:
            default:
                producer = new RocketMQProducer(config);
                break;
        }

        return producer;
    }

    public static MQConsumer createMQConsumer(MQConsumerConfig config, QueueInfo queueInfo) {

        if (null == config)
            return null;

        MQConsumer consumer = null;

        switch (config.getEngineType()) {
            case ACTIVEMQ:
                break;
            case ROCKETMQ:
            default:
                consumer = new RocketMQConsumer(config, queueInfo);
                break;

        }
        return consumer;
    }
}
