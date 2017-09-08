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
import java.util.List;
import java.util.Map;

import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;



public class MessagingContext extends HashMap<String, Object> {

    public final static String HandlerConfigStr = "HandlerConfigStr";
    public final static String DefaultProducerGroup = "MessageProducer";
    public final static String DefaultConsumerGroup = "MessageConsumer";
    public final static String Msg2TopicMAP = "Msg2TOPICMAP";
    public final static String MQServerAddress = "MQServerAddress";
    public final static String SpringContext = "SpringContext";
    public final static String MsgHandlerManager = "MsgHandlerManager";
    public static final String ConsumerHandlerClasses = "ConsumerHandlerClasses";
    public static final String MESSAGE_THRESHOLD = "MsgThreshold";
    public static final long DEFAULT_MESSAGE_THRESHOLD = 30L; // unit is kb
    public static final String CONSUMER_HANDLER_INSTANCES = "ConsumerHandlerInstances"; // ext MessageHandler instances
    public static final String MESSAGE_SIZE_LIMIT = "msgSizeLimit";
    public static final long DEFAULT_MSG_SIZE_LIMIT = 262000L;

    private static final long serialVersionUID = -8774944359586955221L;
    protected final static ISystemLogger log = SystemLogger.getLogger(MessagingContext.class);
    private static MessagingContext context = new MessagingContext();
    public static final String profileName = "default";

    private MessagingContext() {

    }

    public static MessagingContext instance() {

        return context;
    }

    /**
     * 先调init, 才能调instance
     * 
     * @param nameServerAddress
     * @param msgType2topicMap
     * @param consumerHandlerClasses
     * @param msgThreshold
     *            The message threshold for message producer
     */
    public static void init(String nameServerAddress, Map<String, String> msgType2topicMap,
            String[] consumerHandlerClasses, long msgThreshold, long msgSizeLimit) {

        context.put(MessagingContext.Msg2TopicMAP, msgType2topicMap);
        context.put(MessagingContext.MQServerAddress, nameServerAddress);
        context.put(MessagingContext.ConsumerHandlerClasses, consumerHandlerClasses);
        context.put(MessagingContext.MESSAGE_THRESHOLD, msgThreshold);

        context.put(MessagingContext.MESSAGE_SIZE_LIMIT, msgSizeLimit);
    }

    public static void putConsumerHandlers(List<MessageHandler> handlers) {

        context.put(CONSUMER_HANDLER_INSTANCES, handlers);
    }
}
