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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.concurrent.TimeoutException;

import com.creditease.monitor.UAVServer;
import com.creditease.monitor.UAVServer.ServerVendor;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.log.ConsoleLogger;
import com.creditease.uav.hook.rabbitmq.RabbitmqHookProxy;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class DoTestRabbitmqProxy {

    public static final String QUEUE_NAME = "haha";
    public static Connection conn;
    public static Channel channel;

    public static void main(String args[]) {

        ConsoleLogger cl = new ConsoleLogger("test");

        cl.setDebugable(true);

        UAVServer.instance().setLog(cl);

        UAVServer.instance().putServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR, ServerVendor.TOMCAT);

        RabbitmqHookProxy p = new RabbitmqHookProxy("test", Collections.emptyMap());

        p.doInstallDProxy(null, "testApp");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setHost("127.0.0.1");
        factory.setPort(5672);
        try {
            conn = factory.newConnection();
            channel = conn.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.queueDeclare("aaa", false, false, false, null);
            new Thread(new Runnable() {

                @Override
                public void run() {

                    String message = "Hello World!";
                    while (true) {
                        try {
                            channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
                            // System.out.println(" [x] Sent '" + message + "'");
                            channel.basicPublish("", "aaa", null, "aaame".getBytes("UTF-8"));
                            // System.out.println(" [x] Sent 'aaame'");
                            Thread.sleep(1000);
                        }
                        catch (UnsupportedEncodingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
                }

            }).start();

            Connection connection = factory.newConnection();
            Channel recvchannel = connection.createChannel();

            recvchannel.queueDeclare(QUEUE_NAME, false, false, false, null);
            recvchannel.queueDeclare("aaa", false, false, false, null);
            Consumer consumer = new DefaultConsumer(recvchannel) {

                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                        byte[] body) throws IOException {

                    String message = new String(body, "UTF-8");
                    // System.out.println(" [x] Received '" + message + "'1");
                }
            };

            recvchannel.basicConsume(QUEUE_NAME, true, consumer);
            String consumerTag = recvchannel.basicConsume("aaa", true, consumer);
            try {
                Thread.sleep(50000);
                recvchannel.basicCancel(consumerTag);

            }
            catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
