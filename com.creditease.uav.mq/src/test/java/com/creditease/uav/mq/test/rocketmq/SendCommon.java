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

package com.creditease.uav.mq.test.rocketmq;

import org.junit.Test;

import com.creditease.agent.log.SystemLogger;
import com.creditease.uav.mq.api.MQProducerConfig;
import com.creditease.uav.mq.rocketmq.RocketMQProducer;
import com.creditease.uav.mq.test.CommonTest;


public class SendCommon {

    @Test
    public void testSendCommon() {

        SystemLogger.init("INFO", true, 5);
        MQProducerConfig config = new MQProducerConfig();
        config.setNamingServer("localhost:9876");
        config.setProducerGroup("abcd");
        CommonTest.sendCommon(new RocketMQProducer(config));
    }
}
