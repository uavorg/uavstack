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

package com.creditease.uav.mq.test;

import com.creditease.uav.mq.api.MQConsumer;
import com.creditease.uav.mq.api.MQProducer;


public class CommonTest {

    public static void sendTopic(MQProducer mq) {

        try {
            mq.send(Const.topicQueueInfo, Const.message);
            mq.send(Const.topicQueueInfo, Const.message);
            mq.send(Const.topicQueueInfo, Const.message);
            mq.send(Const.topicQueueInfo, "SHUTDOWN");

        }
        catch (Exception e) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static void sendCommon(MQProducer mq) {

        try {
            mq.start();
            mq.send(Const.commonQueueInfo, Const.message);
            mq.send(Const.commonQueueInfo, Const.message);
            mq.send(Const.commonQueueInfo, Const.message);
            mq.send(Const.commonQueueInfo, "SHUTDOWN");

        }
        catch (Exception e) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static void reciveTopic(MQConsumer mq) {

        try {
            mq.registerListener(new MyHandler());
        }
        catch (Exception e) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static void reciveCommon(MQConsumer mq) {

        try {
            mq.registerListener(new MyHandler());
            mq.start();
        }
        catch (Exception e) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
        }
    }
}
