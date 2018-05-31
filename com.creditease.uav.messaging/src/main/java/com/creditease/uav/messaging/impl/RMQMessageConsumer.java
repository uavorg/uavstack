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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.creditease.agent.helpers.CompressHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.uav.messaging.api.Message;
import com.creditease.uav.messaging.api.MessageConsumer;
import com.creditease.uav.messaging.api.MessageHandler;
import com.creditease.uav.messaging.api.MessagingContext;
import com.creditease.uav.mq.api.MQConsumer;
import com.creditease.uav.mq.api.MQConsumerConfig;
import com.creditease.uav.mq.api.MQFactory;
import com.creditease.uav.mq.api.MQMessage;
import com.creditease.uav.mq.api.MQMessageListener;
import com.creditease.uav.mq.api.QueueInfo;

public class RMQMessageConsumer extends MessageConsumer {

    protected final static ISystemLogger logger = SystemLogger.getLogger(RMQMessageConsumer.class);
    private Map<String, MessageHandler> handlerMap = new HashMap<String, MessageHandler>();
    private MessagingContext context;
    private MQConsumer consumer;
    private Map<String, String> msg2topicMap;
    private long stopInterval = 0;

    @SuppressWarnings("unchecked")
    public RMQMessageConsumer(String name, String[] bizIds, int maxConcurrent, int initConcurrent, long stopInterval,
            MQFactory.QueueType queueType) {

        super(name, bizIds);

        this.context = MessagingContext.instance();

        this.stopInterval = stopInterval;

        MQConsumerConfig config = new MQConsumerConfig(MQFactory.EngineType.ROCKETMQ);
        config.setNamingServer((String) this.context.get(MessagingContext.MQServerAddress));
        // group不同的consumer不能重复
        config.setComsumerGroup(MessagingContext.DefaultConsumerGroup + "-" + this.getName());
        config.setConsumeThreadMax(maxConcurrent);
        config.setConsumeThreadMin(initConcurrent);
        // 暂时使用默认值，调优的时候可能需要调整这个参数
        config.setPullBatchSize(0);
        // get bizId2TopicMap
        msg2topicMap = (Map<String, String>) context.get(MessagingContext.Msg2TopicMAP);
        QueueInfo queueInfo = new QueueInfo(queueType);

        // need merge tag list of the same topic as key
        for (String msgTypeId : this.msgTypeIds) {
            String topic = msg2topicMap.get(msgTypeId);
            queueInfo.addTopic(topic, msgTypeId);
        }
        // init consumer handlers
        initHandler();

        consumer = MQFactory.createMQConsumer(config, queueInfo);
    }

    public RMQMessageConsumer(String name, String[] bizIds, int maxConcurrent, int initConcurrent, long stopInterval) {

        this(name, bizIds, maxConcurrent, initConcurrent, stopInterval, MQFactory.QueueType.QUEUE);
    }

    @Override
    public void start() {

        try {
            consumer.registerListener(new MQMessageListener() {// register msg listener

                @Override
                public void handle(MQMessage message) {

                    handleMessage(message);
                }

                private void handleMessage(MQMessage message) {

                    try {
                        byte[] msgByteArr = message.getMessage();
                        if (CompressHelper.isCompressedByGZIP(msgByteArr)) {
                            msgByteArr = CompressHelper.uncompressByteArrWithGZIP(msgByteArr);
                        }

                        Message msg = (Message) CompressHelper.readObjectFromByteArr(msgByteArr,
                                this.getClass().getClassLoader());
                        String msgType = msg.getMessageType();
                        MessageHandler mhandler = handlerMap.get(msgType);
                        if (mhandler == null) {
                            if (logger.isTraceEnable()) {
                                logger.warn(this, "MessageHandler NOT FOUND for MessageType[" + msgType + "]");
                            }
                            return;
                        }
                        mhandler.handle(msg);
                        if (stopInterval > 0) {// 处理停顿
                            Thread.sleep(stopInterval);
                        }
                    }
                    catch (Exception e) {
                        logger.err(this, "处理消息失败", e);
                    }
                }
            });
            consumer.start();
            logger.info(this, "MessageConsumer[" + this.name + "] starts SUCCESS");
        }
        catch (Exception e) {
            logger.err(this, "MessageConsumer[" + this.name + "] starts FAIL.", e);
        }
    }

    private void initHandler() {

        String[] handlerClassesStr = (String[]) this.context.get(MessagingContext.ConsumerHandlerClasses);
        ClassLoader[] clsLoaders = (ClassLoader[]) this.context.get(MessagingContext.ConsumerHandlerClassLoaders);
        ClassLoader cl = this.getClass().getClassLoader();
        for (String handlerClassStr : handlerClassesStr) {
            Class<?> c = null;

            try {
                c = cl.loadClass(handlerClassStr);
            }
            catch (ClassNotFoundException e) {
                // ignore
            }

            if (c == null && clsLoaders != null) {

                for (ClassLoader tcl : clsLoaders) {
                    try {
                        c = tcl.loadClass(handlerClassStr);
                    }
                    catch (ClassNotFoundException e) {
                        // ignore
                    }

                    if (c != null) {
                        break;
                    }
                }
            }

            try {
                MessageHandler instance = (MessageHandler) c.newInstance();
                handlerMap.put(instance.getMsgTypeName(), instance);
            }
            catch (Exception e) {
                logger.err(this, "ConsumerHandler Class[" + handlerClassStr + "] load FAILs", e);
            }
        }

        // ext MessageHandler instances
        @SuppressWarnings("unchecked")
        List<MessageHandler> handlers = (List<MessageHandler>) context.get(MessagingContext.CONSUMER_HANDLER_INSTANCES);
        if (handlers != null) {
            for (MessageHandler handler : handlers) {
                handlerMap.put(handler.getMsgTypeName(), handler);
            }
        }
    }

    @Override
    public void shutdown() {

        consumer.shutdown();
        logger.info(this, "MessageConsumer[" + this.name + "] stops SUCCESS");
    }
}
