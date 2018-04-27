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

package com.creditease.monitorframework.fat.queue;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.creditease.agent.log.SystemLogger;

/**
 * KafkaTest description: ???
 *
 */
@Singleton
@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Path("kafka")
public class KafkaTest {

    String url = "127.0.0.1:9092,127.0.0.2:9092,127.0.0.3:9092";

    @GET
    @Path("kafkaSyncSend")
    @Produces(MediaType.TEXT_HTML + ";charset=utf-8")
    public String kafkaSyncSend() {

        SystemLogger.init("DEBUG", true, 0);
        System.out.println("TEST kafkaSyncSend ======================================================");
        Producer<String, String> producer = createProducer();
        for (int i = 0; i <= 5; i++) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String testMsg = "this is producer send test msg,date:" + simpleDateFormat.format(new Date());
            String key = "linlinwang3TestKey" + i;
            String topic = "test";
            if (i % 3 == 0) {
                topic = "test1";

            }
            try {
                // 同步发送
                producer.send(new ProducerRecord<String, String>(topic, key, testMsg)).get();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            catch (ExecutionException e) {
                e.printStackTrace();
            }

        }

        System.out.println("send message over.");
        producer.close(100, TimeUnit.MILLISECONDS);
        return "kafkaSyncSend";
    }

    @GET
    @Path("kafkaAsyncSend")
    @Produces(MediaType.TEXT_HTML + ";charset=utf-8")
    public String kafkaAsyncSend() {

        SystemLogger.init("DEBUG", true, 0);
        System.out.println("TEST kafkaAsyncSend ======================================================");
        Producer<String, String> producer = createProducer();
        for (int i = 0; i <= 4; i++) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String testMsg = "this is producer send test msg,date:" + simpleDateFormat.format(new Date());
            String key = "linlinwang3TestKey" + i;
            String topic = "test";
            if (i % 3 == 0) {
                topic = "test1";

            }
            // 异步发送
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
        }

        System.out.println("send message over.");
        return "kafkaAsyncSend";
    }

    private Producer<String, String> createProducer() {
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
        Producer<String, String> producer = new KafkaProducer<String, String>(props);
        return producer;
    }

    @GET
    @Path("kafkaRecv")
    @Produces(MediaType.TEXT_HTML + ";charset=utf-8")
    public String kafkaRecv() {

        SystemLogger.init("DEBUG", true, 0);
        System.out.println("TEST kafkaRecv ======================================================");
        KafkaConsumer<String, String> consumer = createConsumer();
        consumer.subscribe(Arrays.asList("test", "test1"));
        String result = "";
        for (int i = 0; i < 10; i++) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                result += "offset=%d,key=%s,value=%s%n" + record.offset() + record.key() + record.value();
            }
        }
        consumer.close();
        return "".equals(result) ? "NODATA" : result;
    }

    private KafkaConsumer<String, String> createConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", url);
        props.put("group.id", "test");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
        return consumer;
    }
}
