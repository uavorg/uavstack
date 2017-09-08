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

package test.messaging;

import java.util.HashMap;
import java.util.Map;

import com.creditease.agent.log.SystemLogger;
import com.creditease.uav.messaging.api.MessageConsumer;
import com.creditease.uav.messaging.api.MessagingContext;
import com.creditease.uav.messaging.api.MessagingFactory;


public class TestRMQMessageConsumer {

    public static void main(String[] args) {

        Map<String, String> msgType2topicMap = new HashMap<String, String>();
        msgType2topicMap.put("test", "JQ_Sub2");

        String[] consumerHandlerClasses = "test.messaging.TestMessageHandler".split(",");
        MessagingContext.init("127.0.0.1:9876", msgType2topicMap, consumerHandlerClasses, 30L, 262000L);
        SystemLogger.init("INFO", true, 5);

        String[] bizIDs = new String[1];
        bizIDs[0] = "test";
        String[] handlerNames = new String[1];
        handlerNames[0] = "test";
        MessageConsumer consumer = MessagingFactory.createMessageConsumer("name", bizIDs, 10, 0);
        consumer.start();
    }
}
