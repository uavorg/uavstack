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

import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;


public abstract class MQProducer {

    protected final static ISystemLogger log = SystemLogger.getLogger(MQProducer.class);

    protected MQProducerConfig producerConfig;

    public MQProducer(MQProducerConfig producerConfig) {
        this.producerConfig = producerConfig;
    }

    /**
     * 发送字符串
     * 
     * @param queueInfo
     * @param data
     * @throws Exception
     */
    abstract public void send(QueueInfo queueInfo, String data) throws Exception;

    /**
     * 发送字符流
     * 
     * @param queueInfo
     * @param data
     */
    abstract public void send(QueueInfo queueInfo, byte[] data) throws Exception;

    abstract public void start();

    /**
     * 当不需要mq发送信息的时候，清理连接等
     */
    abstract public void shutdown();

}
