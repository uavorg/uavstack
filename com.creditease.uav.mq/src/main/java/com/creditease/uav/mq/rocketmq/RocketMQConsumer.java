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

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.*;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.creditease.uav.mq.api.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class RocketMQConsumer extends MQConsumer {

    DefaultMQPushConsumer consumer;

    QueueInfo queueInfo;

    public RocketMQConsumer(MQConsumerConfig consumerConfig, QueueInfo queueInfo) {
        super(consumerConfig);
        this.queueInfo = queueInfo;
        consumer = initConsumer(queueInfo);
    }

    @Override
    public void registerListener(final MQMessageListener messageListner) {

        consumer.registerMessageListener(new MessageListenerConcurrently() {

            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {

                handleRocketMqMessage(msgs, messageListner);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
    }

    @Override
    public void registerOrderlyListener(final MQMessageListener messageListner) {

        consumer.registerMessageListener(new MessageListenerOrderly() {

            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {

                handleRocketMqMessage(msgs, messageListner);
                return ConsumeOrderlyStatus.SUCCESS;
            }
        });
    }

    private DefaultMQPushConsumer initConsumer(QueueInfo queueInfo) {

        DefaultMQPushConsumer dmpc = new DefaultMQPushConsumer(consumerConfig.getComsumerGroup());
        /**
         * 设置Consumer第一次启动是从队列头部开始消费还是队列尾部开始消费<br>
         * 如果非第一次启动，那么按照上次消费的位置继续消费
         */

        dmpc.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        dmpc.setNamesrvAddr(consumerConfig.getNamingServer());
        if (consumerConfig.getPullBatchSize() >= 0) {
            dmpc.setPullBatchSize(50);
        }

        // subscribeTopics
        subscribeTopics(dmpc, queueInfo);

        // 配置是否是单线程的consumer监听，因为在处理事务的时候，使用actor模式，需要单线程处理那些数据库写的请求
        if (consumerConfig.getConsumeThreadMax() != null && consumerConfig.getConsumeThreadMax() > 0) {
            dmpc.setConsumeThreadMax(consumerConfig.getConsumeThreadMax());
        }
        if (consumerConfig.getConsumeThreadMin() != null && consumerConfig.getConsumeThreadMin() > 0) {
            dmpc.setConsumeThreadMin(consumerConfig.getConsumeThreadMin());
        }

        // 进行空值测试，如果没有填写queue类型一律按照queue信息算
        if (MQFactory.QueueType.TOPIC.equals(queueInfo.getQueueType())) {
            dmpc.setMessageModel(MessageModel.BROADCASTING);
        }
        return dmpc;
    }

    /**
     * @param consumer
     * @param topics
     * @param topicNames
     */
    private void subscribeTopics(DefaultMQPushConsumer consumer, QueueInfo queueInfo) {

        Map<String, String[]> topics = queueInfo.getTopics();
        Set<String> topicNames = topics.keySet();
        for (String topicName : topicNames) {
            try {
                String[] tags = topics.get(topicName);
                // if there is no tag, consume all messages on the topic
                if (tags.length == 0) {
                    consumer.subscribe(topicName, "*");
                }
                // if there are tags, consume only tags messages on the topic
                else {
                    StringBuilder tagsBuffer = new StringBuilder();
                    tagsBuffer.append(tags[0]);
                    for (int i = 1; i < tags.length; i++) {
                        tagsBuffer.append("||" + tags[i]);
                    }
                    consumer.subscribe(topicName, tagsBuffer.toString());
                }

            }
            catch (MQClientException e) {
                log.err(this, "Topic[" + topicName + "]订阅异常:" + e.getMessage(), e);
            }
        }
    }

    private void handleRocketMqMessage(List<MessageExt> msgs, MQMessageListener messageListner) {

        for (MessageExt msg : msgs) {
            MQMessage ceMessage = new MQMessage();

            byte[] msgBody = msg.getBody();
            ceMessage.setMessage(msgBody);
            try {
                messageListner.handle(ceMessage);
            }
            catch (Exception e) {
                log.err(this, "MsgId=" + msg.getMsgId() + ",Topic=" + msg.getTopic() + ",MsgBornTimeStamp="
                        + msg.getBornTimestamp() + "处理异常：" + e.getMessage(), e);
            }
        }

    }

    @Override
    public void shutdown() {

        this.consumer.shutdown();
    }

    @Override
    public void start() throws MQClientException {

        this.consumer.start();
    }

}
