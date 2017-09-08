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


public class TestReciverTopic {

    /**
     * 不明白listenr必须用main启动，用test方法启动就不行
     * 
     * @param args
     */
    public static void main(String[] args) throws Exception {

        // MQConsumerConfig config=new MQConsumerConfig();
        // config.setNamingServer("localhost:9876");
        // config.setComsumerGroup("efgh");
        // QueueInfo queueInfo =new QueueInfo(MQFactory.QueueType.QUEUE);
        // queueInfo.addTopic("aaa");
        // CommonTest.reciveTopic(new RocketMQConsumer(config,queueInfo));
    }
}
