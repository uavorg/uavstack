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

package com.creditease.monitorframework.fat;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bson.Document;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.alibaba.druid.pool.DruidDataSource;
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
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import com.creditease.agent.log.SystemLogger;
import com.creditease.monitorframework.fat.dbconnpool.DAOFactory;
import com.creditease.monitorframework.fat.dbconnpool.DAOFactory.QueryHelper;
import com.creditease.monitorframework.fat.dubbo.IMyDubboService;
import com.creditease.monitorframework.fat.ivc.MyTestInjectObj;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.cache.api.CacheManagerFactory;
import com.creditease.uav.httpasync.HttpAsyncClient;
import com.creditease.uav.httpasync.HttpAsyncClientFactory;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;
import com.lambdaworks.redis.RedisAsyncConnection;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.github.lukehutch.fastclasspathscanner.utils.Log;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Singleton
@Path("TestRestService")
public class TestRestService {

    HttpAsyncClient client;

    @Context
    private ServletContext sc;

    public TestRestService() {
        client = HttpAsyncClientFactory.build(20, 50);
    }

    @GET
    @Path("echo1")
    public String testEcho(String param) {

        return param;
    }

    @GET
    @Path("methodPath1")
    public String testMethod1(@QueryParam("param") String param, @QueryParam("pint") int pint) {

        return param;
    }

    @POST
    @Path("methodPath2")
    @Consumes("application/json")
    @Produces("application/json")
    public String testPostMethod1(String jsonString) {

        return jsonString;
    }

    @POST
    @Path("methodPath3")
    @Consumes("applicatino/json")
    @Produces("plain/text")
    public String[] testPosthMethod2(String jsonString) {

        return new String[0];
    }

    @POST
    @Path("testAccessOtherService")
    public void testAccessOtherService(String jsonString) {

        client.doAsyncHttpGet("http://localhost:8080/com.creditease.uav.console/rs/godeye/profile/q/cache",
                new HttpClientCallback() {

                    @Override
                    public void completed(HttpClientCallbackResult result) {

                        System.out.println(result.getReplyDataAsString());
                    }

                    @Override
                    public void failed(HttpClientCallbackResult result) {

                        System.out.println(result.getException());
                    }

                });
    }

    @POST
    @Path("testAccessProxyService")
    public void testAccessProxyService(String jsonString) {

        client.doAsyncHttpGet("http://localhost:9090/com.creditease.uav.console/", new HttpClientCallback() {

            @Override
            public void completed(HttpClientCallbackResult result) {

                System.out.println(result.getReplyDataAsString());
            }

            @Override
            public void failed(HttpClientCallbackResult result) {

                System.out.println(result.getException());
            }

        });
    }

    @POST
    @Path("testAccessProxy2Service")
    public void testAccessProxy2Service(String jsonString) {

        client.doAsyncHttpGet("http://localhost:9010/com.creditease.uav.console/", new HttpClientCallback() {

            @Override
            public void completed(HttpClientCallbackResult result) {

                System.out.println(result.getReplyDataAsString());
            }

            @Override
            public void failed(HttpClientCallbackResult result) {

                System.out.println(result.getException());
            }

        });
    }

    @POST
    @Path("testAccessSyncService")
    public void testAccessSyncService(String jsonString) {

        CloseableHttpClient cl = HttpClients.createDefault();

        HttpUriRequest httpMethod = new HttpGet(
                "http://localhost:8080/com.creditease.uav.console/rs/godeye/profile/q/cache");

        try {
            System.out.println(cl.execute(httpMethod));
        }
        catch (ClientProtocolException e) {

            e.printStackTrace();
        }
        catch (IOException e) {

            e.printStackTrace();
        }
    }

    @POST
    @Path("testJdbcService")
    public void testJdbcService(String jsonString) {

        try {
            Connection c = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/testdb", "root", "root");

            System.out.println("Statement -------------------->");

            Statement st = c.createStatement();

            st.execute("insert into mytest values (1,'zz',23)");

            st.executeQuery("select name from mytest where id=1");

            st.executeUpdate("update mytest set age=24 where id=1");

            st.executeUpdate("delete from mytest where id=1");

            st.close();

            System.out.println("PreparedStatement -------------------->");

            PreparedStatement ps = c.prepareStatement("insert into mytest values (?,?,?)");

            ps.setInt(1, 1);

            ps.setString(2, "zz");

            ps.setInt(3, 23);

            ps.execute();

            ps.close();

            ps = c.prepareStatement("select name from mytest where id=?");

            ps.setInt(1, 1);

            ps.executeQuery();

            ps.close();

            ps = c.prepareStatement("update mytest set age=24 where id=?");

            ps.setInt(1, 1);

            ps.executeUpdate();

            ps.close();

            ps = c.prepareStatement("delete from mytest where id=?");

            ps.setInt(1, 1);

            ps.executeUpdate();

            ps.close();

            c.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @POST
    @Path("testJedis")
    public void testJedis(String jsonString) {

        @SuppressWarnings("resource")
        Jedis jedis = new Jedis("localhost", 6379);
        jedis.set("foo", "bar");

        jedis.get("foo");

        jedis.lpush("lll", "a");
        jedis.lpush("lll", "b");
        jedis.lpush("lll", "c");
        jedis.lpop("lll");
        jedis.lpop("lll");
        jedis.lpop("lll");

        jedis.hset("mmm", "abc", "123");
        jedis.hset("mmm", "def", "456");
        jedis.hgetAll("mmm");

        jedis.close();

        JedisPoolConfig cfg = new JedisPoolConfig();
        cfg.setMaxTotal(5);
        cfg.setMaxIdle(1);
        cfg.setMaxWaitMillis(10000L);

        JedisPool jp = new JedisPool(cfg, "localhost", 6379);
        jedis = jp.getResource();

        jedis.set("foo", "bar");
        // jedis.close();
        jedis = jp.getResource();

        jedis.get("foo");
        // jedis.close();
        jedis = jp.getResource();

        jedis.lpush("lll", "a");
        jedis.lpush("lll", "b");
        jedis.lpush("lll", "c");
        jedis.lpop("lll");
        jedis.lpop("lll");
        jedis.lpop("lll");
        // jedis.close();
        jedis = jp.getResource();

        jedis.hset("mmm", "abc", "123");
        jedis.hset("mmm", "def", "456");
        jedis.hgetAll("mmm");

        jp.close();
    }

    @POST
    @Path("testLettuce")
    public void testLettuce(String jsonString) {

        RedisClient redisClient = RedisClient.create("redis://localhost:6379/0");
        RedisConnection<String, String> conn1 = redisClient.connect();

        conn1.set("foo", "bar");
        String value = conn1.get("foo");
        System.out.println(value);

        conn1.close();
        redisClient.shutdown();

        RedisClient client = RedisClient.create("redis://localhost:6379/0");
        RedisAsyncConnection<String, String> conn = client.connectAsync();
        conn.set("foo", "bar");

        conn.get("foo");

        conn.lpush("lll", "a");
        conn.lpush("lll", "b");
        conn.lpush("lll", "c");
        conn.lpop("lll");
        conn.lpop("lll");
        conn.lpop("lll");

        conn.hset("mmm", "abc", "123");
        conn.hset("mmm", "def", "456");
        conn.hgetall("mmm");

        conn.del("foo", "lll", "mmm");

        conn.close();
        client.shutdown();
    }

    @POST
    @Path("testAredis")
    public void testAredis(String jsonString) {

        SystemLogger.init("DEBUG", true, 0);
        CacheManager cm = CacheManagerFactory.build("localhost:6379", 1, 5, 5);
        cm.put("TEST", "foo", "bar");

        cm.get("TEST", "foo");

        cm.lpush("TEST", "lll", "a");
        cm.lpush("TEST", "lll", "b");
        cm.lpush("TEST", "lll", "c");
        cm.lpop("TEST", "lll");
        cm.lpop("TEST", "lll");
        cm.lpop("TEST", "lll");

        cm.putHash("TEST", "mmm", "abc", "123");
        cm.putHash("TEST", "mmm", "def", "456");
        cm.getHashAll("TEST", "mmm");

        cm.del("TEST", "foo");
        cm.del("TEST", "lll");
        cm.del("TEST", "mmm");

        cm.shutdown();
    }

    @POST
    @Path("testMongo1")
    public void testMongo1(String jsonString) {

        MongoClient client = new MongoClient();
        client.listDatabaseNames().first();
        MongoDatabase db = client.getDatabase("apphubDataStore");
        db.listCollectionNames().first();
        MongoCollection<Document> collection = db.getCollection("test");
        collection.listIndexes().first();
        Document doc = new Document("name", "Amarcord Pizzeria")
                .append("contact",
                        new Document("phone", "264-555-0193").append("email", "amarcord.pizzeria@example.net")
                                .append("location", Arrays.asList(-73.88502, 40.749556)))
                .append("stars", 2).append("categories", Arrays.asList("Pizzeria", "Italian", "Pasta"));
        collection.insertOne(doc);
        collection.find().first();

        MongoClient client2 = new MongoClient("localhost:27017");
        db = client2.getDatabase("apphubDataStore");
        db.listCollectionNames().first();
        collection = db.getCollection("test");
        collection.listIndexes().first();

        client.close();
        client2.close();
    }

    @POST
    @Path("testRabbitmq")
    public void testRabbitmq(String jsonString) {

        String QUEUE_NAME = "haha";
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setHost("10.100.66.81");
        factory.setPort(5672);
        try {
            com.rabbitmq.client.Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String message = "Hello World!";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
            // System.out.println(" [x] Sent '" + message + "'");

            channel.close();
            conn.close();

            com.rabbitmq.client.Connection connection = factory.newConnection();
            Channel recvchannel = connection.createChannel();

            recvchannel.queueDeclare(QUEUE_NAME, false, false, false, null);

            com.rabbitmq.client.Consumer consumer = new DefaultConsumer(recvchannel) {

                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                        byte[] body) throws IOException {

                    String message = new String(body, "UTF-8");
                    Log.log(message);
                    // System.out.println(" [x] Received '" + message + "'1");
                }
            };

            recvchannel.basicConsume(QUEUE_NAME, true, consumer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (TimeoutException e) {
            e.printStackTrace();
        }

    }

    @POST
    @Path("testRocketmq")
    public void testRocketmq(String jsonString) {

        DefaultMQProducer producer = new DefaultMQProducer("hookTest");
        producer.setNamesrvAddr("10.100.33.135:9876");
        Message msg = new Message("SELF_TEST_TOPIC", "test".getBytes());
        try {
            producer.start();

            for (int i = 0; i < 10; i++) {
                producer.send(msg);
            }
        }
        catch (MQClientException e) {
            e.printStackTrace();
        }
        catch (RemotingException e) {
            e.printStackTrace();
        }
        catch (MQBrokerException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        DefaultMQPushConsumer pushConsumer = new DefaultMQPushConsumer("hookTest");
        pushConsumer.setNamesrvAddr("10.100.33.135:9876");
        pushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        pushConsumer.registerMessageListener(new MessageListenerOrderly() {

            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {

                System.out.println("haha");
                return ConsumeOrderlyStatus.SUCCESS;
            }

        });
        pushConsumer.registerMessageListener(new MessageListenerConcurrently() {

            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {

                System.out.println("haha");
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }

        });
        try {
            pushConsumer.subscribe("SELF_TEST_TOPIC", null);
            pushConsumer.start();

        }
        catch (MQClientException e1) {
            e1.printStackTrace();
        }

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

    @POST
    @Path("testDubbo")
    public String testDubbo() {

        WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(sc);

        IMyDubboService mds = (IMyDubboService) wac.getBean("myDubboServiceC");

        return mds.sayHello("zz");
    }

    @POST
    @Path("testDubboExceptin")
    public String testDubboExceptin() throws IOException {

        WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(sc);

        IMyDubboService mds = (IMyDubboService) wac.getBean("myDubboServiceC");

        return mds.sayException("exception");
    }

    @POST
    @Path("testDubboUncatchException")
    public String testDubboUncatchException() {

        WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(sc);

        IMyDubboService mds = (IMyDubboService) wac.getBean("myDubboServiceC");

        return mds.sayUncatchException("UncatchException");
    }

    DAOFactory factory;

    @GET
    @Path("testC3P0")
    public void testC3P0Pool() {

        if (factory == null) {
            factory = DAOFactory.buildDAOFactory("com.mysql.jdbc.Driver", "jdbc:mysql://127.0.0.1:3306/testdb", "root",
                    "root", 2, 2, 10, 30000, 120000, "select 1 from dual");
        }

        QueryHelper helper = factory.getQueryHelper();

        try {
            helper.execute("insert into mytest values (1,'zz',23)");
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            helper.query("select name from mytest where id=1");
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            helper.execute("update mytest set age=24 where id=1");
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            helper.execute("delete from mytest where id=1");
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        helper.free();

    }

    DruidDataSource dds;

    @GET
    @Path("testDruid")
    public void testDruid() {

        if (dds == null) {
            dds = new DruidDataSource();
            dds.setDriverClassName("com.mysql.jdbc.Driver");
            dds.setUrl("jdbc:mysql://127.0.0.1:3306/testdb");
            dds.setUsername("root");
            dds.setPassword("root");
            dds.setInitialSize(2);
            dds.setMaxActive(30);
            dds.setMaxWait(30000);
            dds.setMinIdle(1);
        }

        try {
            Connection c = dds.getConnection();

            Statement st = c.createStatement();

            st.execute("insert into mytest values (1,'zz',23)");

            st.executeQuery("select name from mytest where id=1");

            st.executeUpdate("update mytest set age=24 where id=1");

            st.executeUpdate("delete from mytest where id=1");

            st.close();

            c.close();
        }
        catch (Exception e) {

        }

    }

    @GET
    @Path("testProxool")
    public void testProxool() {

        try {
            Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");
            Connection c = DriverManager.getConnection(
                    "proxool.aaa:com.mysql.jdbc.Driver:jdbc:mysql://127.0.0.1:3306/testdb", "root", "root");

            Statement st = c.createStatement();

            st.execute("insert into mytest values (1,'zz',23)");

            st.executeQuery("select name from mytest where id=1");

            st.executeUpdate("update mytest set age=24 where id=1");

            st.executeUpdate("delete from mytest where id=1");

            st.close();

            c.close();
        }
        catch (Exception e) {

        }
    }

    org.apache.commons.dbcp2.BasicDataSource bds2;

    @GET
    @Path("testDBCP2")
    public void testDBCP2() {

        if (bds2 == null) {
            bds2 = new org.apache.commons.dbcp2.BasicDataSource();
            bds2.setUrl("jdbc:mysql://127.0.0.1:3306/testdb");
            bds2.setUsername("root");
            bds2.setPassword("root");
            bds2.setDriverClassName("com.mysql.jdbc.Driver");
            bds2.setInitialSize(2);
            bds2.setMaxTotal(10);
            bds2.setMinIdle(0);
            bds2.setMaxIdle(1);
            bds2.setMaxWaitMillis(30000);
            bds2.setMaxOpenPreparedStatements(20);
        }

        try {
            Connection c = bds2.getConnection();

            Statement st = c.createStatement();

            st.execute("insert into mytest values (1,'zz',23)");

            st.executeQuery("select name from mytest where id=1");

            st.executeUpdate("update mytest set age=24 where id=1");

            st.executeUpdate("delete from mytest where id=1");

            st.close();

            c.close();
        }
        catch (Exception e) {

        }
    }

    BasicDataSource bds;

    @GET
    @Path("testDBCP")
    public void testDBCP() {

        if (bds == null) {
            bds = new BasicDataSource();
            bds.setUrl("jdbc:mysql://127.0.0.1:3306/testdb");
            bds.setUsername("root");
            bds.setPassword("root");
            bds.setDriverClassName("com.mysql.jdbc.Driver");
            bds.setInitialSize(2);
            bds.setMaxActive(10);
            bds.setMinIdle(0);
            bds.setMaxIdle(1);
            bds.setMaxWait(30000);
            bds.setMaxOpenPreparedStatements(20);
        }

        try {
            Connection c = bds.getConnection();

            Statement st = c.createStatement();

            st.execute("insert into mytest values (1,'zz',23)");

            st.executeQuery("select name from mytest where id=1");

            st.executeUpdate("update mytest set age=24 where id=1");

            st.executeUpdate("delete from mytest where id=1");

            st.close();

            c.close();
        }
        catch (Exception e) {

        }
    }

    HikariDataSource hds;

    @GET
    @Path("testHikari")
    public void testHikari() {

        if (hds == null) {

            HikariConfig hc = new HikariConfig();

            hc.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/testdb");
            hc.setUsername("root");
            hc.setPassword("root");
            hc.setDriverClassName("com.mysql.jdbc.Driver");
            hc.setMaximumPoolSize(30);
            hc.setMinimumIdle(1);
            hc.setIdleTimeout(30000);

            hds = new HikariDataSource(hc);
        }

        try {
            Connection c = hds.getConnection();

            Statement st = c.createStatement();

            st.execute("insert into mytest values (1,'zz',23)");

            st.executeQuery("select name from mytest where id=1");

            st.executeUpdate("update mytest set age=24 where id=1");

            st.executeUpdate("delete from mytest where id=1");

            st.close();

            c.close();
        }
        catch (Exception e) {

        }

    }

    @POST
    @Path("testInjectClass")
    public void testInjectClass() {

        MyTestIVCInjectClass my = new MyTestIVCInjectClass();

        String res1 = my.testMethod1("1", new MyTestInjectObj());
        int res2 = my.testMethod1("2", new MyTestInjectObj(), true);
        String res3 = my.testMethod2("3", 1024);
        my.testMethod3(true);
        my.testMethod3(22D);
        my.testMethod3(32F);
        my.testMethod3(19);

        try {
            my.testException();
        }
        catch (Exception e) {

        }
        try {
            my.testNPE();
        }
        catch (Exception e) {

        }

        try {
            my.testException();
        }
        catch (Exception e) {

        }

        // 进行服务之间交互
        CloseableHttpClient client = HttpClients.createDefault();
        HttpUriRequest http = new HttpGet(
                "http://localhost:8080/com.creditease.uav.monitorframework.buildFat/rs/http/httpclienttest");
        try {
            HttpResponse resp1 = client.execute(http);

            System.out.println(resp1.getStatusLine());

            client.close();
        }
        catch (ClientProtocolException e) {

            e.printStackTrace();
        }
        catch (IOException e) {

            e.printStackTrace();
        }

        // mongo
        MongoClient mongoClient = new MongoClient();
        mongoClient.listDatabaseNames().first();
        MongoDatabase db = mongoClient.getDatabase("apphubDataStore");
        db.listCollectionNames().first();
        MongoCollection<Document> collection = db.getCollection("test");
        collection.listIndexes().first();
        Document doc = new Document("name", "Amarcord Pizzeria")
                .append("contact",
                        new Document("phone", "264-555-0193").append("email", "amarcord.pizzeria@example.net")
                                .append("location", Arrays.asList(-73.88502, 40.749556)))
                .append("stars", 2).append("categories", Arrays.asList("Pizzeria", "Italian", "Pasta"));
        collection.insertOne(doc);
        collection.find().first();

        mongoClient.close();

        // 进行服务之间交互
        CloseableHttpClient client2 = HttpClients.createDefault();

        HttpUriRequest http2 = new HttpGet(
                "http://localhost:8080/com.creditease.uav.monitorframework.buildFat/rs/chain/circle_test" + "?time=1");
        try {
            HttpResponse resp1 = client2.execute(http2);

            System.out.println(resp1.getStatusLine());

            client2.close();
        }
        catch (ClientProtocolException e) {

            e.printStackTrace();
        }
        catch (IOException e) {

            e.printStackTrace();
        }
    }
}
