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

package com.creditease.uav.mq.rocketmq;

import java.util.Map;
import java.util.Set;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.common.message.Message;
import com.creditease.uav.mq.api.MQProducer;
import com.creditease.uav.mq.api.MQProducerConfig;
import com.creditease.uav.mq.api.QueueInfo;
import com.creditease.uav.mq.api.RMQClientLogger;


public class RocketMQProducer extends MQProducer {

    private DefaultMQProducer producer = null;

    private int maxSize = 262144;// 262144 is default Max size on RMQ broker

    public RocketMQProducer(MQProducerConfig producerConfig) {
        super(producerConfig);
        producer = initProducer(producerConfig.getProducerGroup(), producerConfig.getNamingServer());

        // #221:FIX The RMQ max size issue
        producer.setMaxMessageSize(maxSize);

    }

    @Override
    public void send(QueueInfo queueInfo, String data) throws Exception {

        if (data == null) {
            return;
        }
        send(queueInfo, data.getBytes("UTF-8"));
    }

    @Override
    public void shutdown() {

        producer.shutdown();

    }

    @Override
    public void send(QueueInfo queueInfo, byte[] data) throws Exception {

        Map<String, String[]> topics = queueInfo.getTopics();
        Set<String> topicList = topics.keySet();
        for (String topicName : topicList) {
            String[] tagList = topics.get(topicName);
            // if there is no tag configuration, send only one message
            if (tagList.length == 0) {
                Message msg = new Message(topicName, data);
                producer.send(msg);
            }
            // if there are tags configuration, send the messages to equal number of tags
            else {
                for (String tags : tagList) {
                    Message msg = new Message(topicName, tags, data);
                    producer.send(msg);
                }
            }
        }

    }

    private DefaultMQProducer initProducer(String producerGroup, String nameServer) {

        DefaultMQProducer dmp = new DefaultMQProducer(producerGroup);
        dmp.setNamesrvAddr(nameServer);
        return dmp;
    }

    @Override
    public void start() {

        ClientLogger.setLog(new RMQClientLogger());

        try {
            producer.start();
        }
        catch (MQClientException e) {
            log.err("com.creditease.uav.mq.rocketmq.RocketMQProducer.start", "MQProducer启动失败", e);
        }
    }
}
