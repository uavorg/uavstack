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

package com.creditease.uav.messaging.impl;

import java.util.Map;

import com.creditease.agent.helpers.CompressHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.uav.messaging.api.Message;
import com.creditease.uav.messaging.api.MessageProducer;
import com.creditease.uav.messaging.api.MessagingContext;
import com.creditease.uav.mq.api.MQFactory;
import com.creditease.uav.mq.api.MQProducer;
import com.creditease.uav.mq.api.MQProducerConfig;
import com.creditease.uav.mq.api.QueueInfo;



public class RMQMessageProducer extends MessageProducer {

    private MQProducer producer;

    private MessagingContext context;

    private Map<String, String> msg2topicMap;

    // rocketmq default limit 626000
    private volatile long msgSizeLimit;

    @SuppressWarnings("unchecked")
    public RMQMessageProducer(String name) {
        super(name);
        this.context = MessagingContext.instance();

        // get bizId2TopicMap
        msg2topicMap = (Map<String, String>) context.get(MessagingContext.Msg2TopicMAP);

        // start producer
        MQProducerConfig config = new MQProducerConfig(MQFactory.EngineType.ROCKETMQ);

        config.setNamingServer((String) context.get(MessagingContext.MQServerAddress));
        config.setProducerGroup(MessagingContext.DefaultProducerGroup + "-" + this.getName());

        msgSizeLimit = (Long) context.get(MessagingContext.MESSAGE_SIZE_LIMIT);

        producer = MQFactory.createMQProducer(config);
    }

    @Override
    public void start() {

        producer.start();
    }

    @Override
    public void stop() {

        producer.shutdown();
    }

    @Override
    public boolean submit(Message msg) {

        /**
         * 1.根据job的bizId获得topic名字 2.用Topic生成QueueInfo 3.提交作业
         */
        // 设置发出消息的源是谁
        msg.setSystemId(this.name);

        // we only send the topic with the target msg type
        String msgType = msg.getMessageType();
        String topic = msg2topicMap.get(msgType);
        QueueInfo queueInfo = new QueueInfo(MQFactory.QueueType.QUEUE);
        queueInfo.addTopic(topic, msgType);

        // String msgStream = msg.toJSONString();

        try {
            byte[] msgByteArr = CompressHelper.transObjectToByteArr(msg);

            // message threshold's unit is kb
            long msgThreshold = (Long) MessagingContext.instance().get(MessagingContext.MESSAGE_THRESHOLD);

            if (msgByteArr.length >= msgThreshold * 1000) {
                msgByteArr = CompressHelper.compressByteArrWithGZIP(msgByteArr);
            }

            // Exceeds the max permit size, directly return.
            if (msgByteArr != null && (msgByteArr.length > msgSizeLimit)) {
                if (this.getLog().isTraceEnable()) {
                    this.getLog().warn(this, "The Msg Size Is Over Max Limit Allow. limit=" + msgSizeLimit + ", size="
                            + msgByteArr.length);
                }

                return false;
            }

            producer.send(queueInfo, msgByteArr);
            if (this.getLog().isDebugEnable()) {
                String msgInfo = getLog().isDebugEnable() ? msg.toJSONString() : msg.getIDString();
                this.getLog().debug(this, "Submit Message SUCCESS: msg " + msgInfo + ",topic="
                        + JSONHelper.toString(queueInfo.getTopics()));
            }

            return true;
        }
        catch (Exception e) {
            this.getLog().err(this, "Submit Message FAIL: msg=" + msg.toJSONString() + ",topic="
                    + JSONHelper.toString(queueInfo.getTopics()), e);
            return false;
        }
    }

    @Override
    public void setMsgSizeLimit(long newLimit) {

        this.msgSizeLimit = newLimit;
    }
}
