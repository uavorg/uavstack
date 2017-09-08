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

package com.creditease.uav.hook.mq;

import java.util.Collections;
import java.util.List;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerOrderly;
import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.UAVServer.ServerVendor;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.log.ConsoleLogger;
import com.creditease.uav.hook.rocketmq.RocketmqHookProxy;

public class DoTestRocketmqProxy {

    static int count1 = 0;
    static int count2 = 0;

    public static void main(String args[]) {

        ConsoleLogger cl = new ConsoleLogger("test");

        cl.setDebugable(true);

        UAVServer.instance().setLog(cl);

        UAVServer.instance().putServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR, ServerVendor.TOMCAT);

        RocketmqHookProxy p = new RocketmqHookProxy("test", Collections.emptyMap());

        p.doInstallDProxy(null, "testApp");

        DefaultMQProducer producer = new DefaultMQProducer("hookTest");
        producer.setNamesrvAddr("127.0.0.1:9876");

        try {
            producer.start();

            for (int i = 0; i < 100; i++) {
                Message msg = new Message("SELF_TEST_TOPIC", (i + "").getBytes());
                producer.send(msg);
            }
        }
        catch (MQClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (RemotingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (MQBrokerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        DefaultMQPushConsumer pushConsumer = new DefaultMQPushConsumer("hookTest1");
        pushConsumer.setNamesrvAddr("127.0.0.1:9876");
        pushConsumer.setMessageModel(MessageModel.BROADCASTING);
        pushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        pushConsumer.registerMessageListener(new MessageListenerOrderly() {

            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {

                System.out.println("Consumer1 " + count1++);
                System.out.println(new String(msgs.get(0).getBody()));
                // TODO Auto-generated method stub
                return ConsumeOrderlyStatus.SUCCESS;
            }

        });
        pushConsumer.registerMessageListener(new MessageListenerConcurrently() {

            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {

                System.out.println("Consumer1 " + count1++);
                System.out.println(new String(msgs.get(0).getBody()));
                // TODO Auto-generated method stub
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }

        });
        try {

            pushConsumer.subscribe("SELF_TEST_TOPIC", "*");
            pushConsumer.start();
        }
        catch (MQClientException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        DefaultMQPushConsumer pushConsumer2 = new DefaultMQPushConsumer("hookTest2");
        pushConsumer2.setNamesrvAddr("127.0.0.1:9876");
        pushConsumer2.setMessageModel(MessageModel.BROADCASTING);
        pushConsumer2.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        pushConsumer2.registerMessageListener(new MessageListenerOrderly() {

            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {

                System.out.println("Consumer2 " + count2++);
                System.out.println(new String(msgs.get(0).getBody()));
                // TODO Auto-generated method stub
                return ConsumeOrderlyStatus.SUCCESS;
            }

        });
        pushConsumer2.registerMessageListener(new MessageListenerConcurrently() {

            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {

                System.out.println("Consumer2 " + count2++);
                System.out.println(new String(msgs.get(0).getBody()));
                // TODO Auto-generated method stub
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }

        });
        try {

            pushConsumer2.subscribe("SELF_TEST_TOPIC", "*");
            pushConsumer2.start();
        }
        catch (MQClientException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        // DefaultMQPullConsumer pullConsumer = new DefaultMQPullConsumer("hookTest");
        // pullConsumer.setNamesrvAddr("127.0.0.1:9876");
        // PullCallback pullCallback = new PullCallback() {
        //
        // @Override
        // public void onSuccess(PullResult pullResult) {
        //
        // System.out.println(pullResult.getMsgFoundList().iterator().next().getBody());
        // // TODO Auto-generated method stub
        //
        // }
        //
        // @Override
        // public void onException(Throwable e) {
        //
        // // TODO Auto-generated method stub
        // System.out.println(e);
        // }
        //
        // };
        //
        // Set<MessageQueue> mqs = null;
        // try {
        // pullConsumer.start();
        // mqs = pullConsumer.fetchSubscribeMessageQueues("SELF_TEST_TOPIC");
        // }
        // catch (MQClientException e2) {
        // // TODO Auto-generated catch block
        // e2.printStackTrace();
        // }
        // for (MessageQueue mq : mqs) {
        // try {
        // for (int i = 0; i <= 10; i++) {
        // pullConsumer.pull(mq, "*", 0, 1, pullCallback);
        // Thread.sleep(1000);
        // }
        // }
        // catch (MQClientException e1) {
        // // TODO Auto-generated catch block
        // e1.printStackTrace();
        // }
        // catch (RemotingException e1) {
        // // TODO Auto-generated catch block
        // e1.printStackTrace();
        // }
        // catch (InterruptedException e1) {
        // // TODO Auto-generated catch block
        // e1.printStackTrace();
        // }
        //
        // }
        // pullConsumer.shutdown();
        try {
            Thread.sleep(5000);
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        pushConsumer.shutdown();
        producer.shutdown();
    }
}
