/*-
 * <<
 * UAVStack
 * ==
 * Copyright (C) 2016 - 2018 UAVStack
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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.creditease.monitor.UAVServer;
import com.creditease.monitor.UAVServer.ServerVendor;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.log.ConsoleLogger;
import com.creditease.uav.hook.kafka.KafkaHookProxy;
import com.creditease.uav.monitorframework.agent.MOFAgent;

public class DoTestkafkaHookProxy {

    static String url = "127.0.0.1:9092,127.0.0.2:9092,127.0.0.3:9092";

    @SuppressWarnings("rawtypes")
    public static void main(String[] args) {

        ConsoleLogger cl = new ConsoleLogger("test");

        cl.setDebugable(true);
        UAVServer.instance().setLog(cl);

        UAVServer.instance().putServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR, ServerVendor.TOMCAT);
        MOFAgent.mofContext.put("org.uavstack.mof.ext.clsloader", Thread.currentThread().getContextClassLoader());

        KafkaHookProxy p = new KafkaHookProxy("test", new HashMap());

        p.doProxyInstall(null, "testApp");
        kafkaAsycnSendTest();
        kafkaSendTest();
        kafkaRecvTest();
    }

    private static void kafkaAsycnSendTest() {
        Producer<String, String> producer = createProducer();
        for (int i = 0; i <= 3; i++) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String testMsg = "this is producer send test msg,date:" + simpleDateFormat.format(new Date());
            String key = "key_" + i;
            String topic = "test";
            if (i % 3 == 0) {
                topic = "test1";
            }
            producer.send(new ProducerRecord<String, String>(topic, key, testMsg), new Callback() {

                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    if (exception != null) {
                        exception.printStackTrace();
                        System.out.println("find send exception:" + exception);
                    }

                    System.out.println(
                            "send to partition(" + metadata.partition() + ")," + "offset(" + metadata.offset() + ")");

                }
            });

            try {
                TimeUnit.SECONDS.sleep(1);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            i++;
        }
        System.out.println("send message over.");
        producer.close(100, TimeUnit.MILLISECONDS);
    }

    /**
     * 
     */
    private static void kafkaSendTest() {
        Producer<String, String> producer = createProducer();
        for (int i = 0; i <= 0; i++) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String testMsg = "this is producer send test msg,date:" + simpleDateFormat.format(new Date());
            String key = "key_" + i;
            String topic = "test";
            if (i % 3 == 0) {
                topic = "test1";
            }
            try {
                RecordMetadata fm = producer.send(new ProducerRecord<String, String>(topic, key, testMsg)).get();

                System.out.println(fm.topic() + "," + fm.offset());

            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            catch (ExecutionException e) {
                e.printStackTrace();
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        System.out.println("send message over.");
        producer.close(100, TimeUnit.MILLISECONDS);

    }

    private static Producer<String, String> createProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", url);
        // 请求完整性
        props.put("acks", "all");
        // 重试
        props.put("retries", 0);
        // 缓冲批量
        props.put("batch.size", 16384);
        // 缓冲处理等待时间
        props.put("linger.ms", 1);
        // 缓冲内存
        props.put("buffer.memory", 33554432);
        // 字符串类型
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return new KafkaProducer<String, String>(props);
    }

    private static void kafkaRecvTest() {

        KafkaConsumer<String, String> consumer = createConsumer();
        consumer.subscribe(Arrays.asList("test", "test1"));
        for (int i = 0; i < 10; i++) {
            ConsumerRecords<String, String> records = consumer.poll(1000);
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("offset=%d,key=%s,value=%s%n", record.offset(), record.key(), record.value());
            }
        }

        consumer.close();
    }

    private static KafkaConsumer<String, String> createConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", url);
        props.put("group.id", "test");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        return new KafkaConsumer<String, String>(props);
    }

}
