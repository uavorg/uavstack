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

package com.creditease.uav.messaging.api;

import java.util.HashMap;
import java.util.Map;

import com.creditease.agent.helpers.StringHelper;
import com.creditease.uav.messaging.impl.RMQMessageConsumer;
import com.creditease.uav.messaging.impl.RMQMessageProducer;
import com.creditease.uav.mq.api.MQFactory;

public class MessagingFactory {

    private static MessageProducer producer = null;
    private static Object lock = new Object();

    private MessagingFactory() {

    }

    /**
     * 释放资源
     */
    public static void shutdown() {

        if (producer != null) {
            ((RMQMessageProducer) producer).stop();
        }
    }

    /**
     * 获取MsgProducer，单例应该够用了
     * 
     * @return
     */
    public static MessageProducer getMessageProducer(String producerId) {

        if (producer == null) {

            synchronized (lock) {

                if (producer == null) {
                    producer = new RMQMessageProducer(StringHelper.isEmpty(producerId) ? "default" : producerId);
                }
            }
            lock = null;
        }

        return producer;
    }

    /**
     * 创建MsgConsumer
     * 
     * @param bizIDs
     * @param maxConcurrent
     * @param stopInterval
     * @return
     */
    public static MessageConsumer createMessageConsumer(String name, String[] bizIDs, int maxConcurrent,
            long stopInterval, MQFactory.QueueType queueType) {

        MessageConsumer consumer = new RMQMessageConsumer(name, bizIDs, maxConcurrent, stopInterval, queueType);

        return consumer;
    }

    public static MessageConsumer createMessageConsumer(String name, String[] bizIDs, int maxConcurrent,
            long stopInterval) {

        return createMessageConsumer(name, bizIDs, maxConcurrent, stopInterval, MQFactory.QueueType.QUEUE);
    }

    /**
     * 创建message
     * 
     * @param msgType
     *            message类型
     * @return
     */
    public static Message createMessage(String msgType) {

        return createMessage(null, msgType);
    }

    /**
     * 创建Message
     * 
     * @param params
     *            message需要的参数
     * @param msgType
     *            message类型
     * @return
     */
    public static Message createMessage(Map<String, String> params, String msgType) {

        Message msg = new Message();
        if (params == null) {
            params = new HashMap<String, String>();
        }
        msg.setParams(params);
        msg.setMessageType(msgType);
        msg.setTimeStamp(System.currentTimeMillis());
        return msg;
    }

}
