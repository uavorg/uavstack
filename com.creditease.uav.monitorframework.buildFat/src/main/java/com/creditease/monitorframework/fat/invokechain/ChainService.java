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

package com.creditease.monitorframework.fat.invokechain;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bson.Document;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.creditease.monitorframework.fat.client.TestService;
import com.creditease.monitorframework.fat.client.TestService_Service;
import com.creditease.monitorframework.fat.dubbo.IMyDubboService;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import redis.clients.jedis.Jedis;

/**
 * 测试链式交互的程序
 *
 */
@Singleton
@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Path("chain")
public class ChainService {

    /**
     * 测试用例（同步）
     * 
     * @return
     */
    @GET
    @Path("test")
    public String test() {

        // 首先进行redis读写操作
        System.out.println("Jedis OPS======================================================");
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

        // 进行服务之间交互
        CloseableHttpClient client = HttpClients.createDefault();
        HttpUriRequest http = new HttpGet(
                "http://localhost:8080/com.creditease.uav.monitorframework.buildFat/rs/http/httpclienttest");
        try {
            HttpResponse resp1 = client.execute(http);

            System.out.println(resp1.getStatusLine());

            HttpResponse resp2 = client.execute(http);

            System.out.println(resp2.getStatusLine());

            client.close();
        }
        catch (ClientProtocolException e) {

            e.printStackTrace();
        }
        catch (IOException e) {

            e.printStackTrace();
        }
        return "chain perfect";
    }

    /**
     * 测试用例(异步)
     * 
     * @return
     */
    @GET
    @Path("async_test")
    public String asyncTest() {

        // 首先进行redis读写操作
        System.out.println("Jedis OPS======================================================");
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

        // 进行服务之间交互
        CloseableHttpClient client = HttpClients.createDefault();
        HttpUriRequest http = new HttpGet(
                "http://localhost:8080/com.creditease.uav.monitorframework.buildFat/rs/http/httpclientAsynctest");
        try {
            HttpResponse resp1 = client.execute(http);

            System.out.println(resp1.getStatusLine());

            // HttpResponse resp2 = client.execute(http);

            // System.out.println(resp2.getStatusLine());

            client.close();
        }
        catch (ClientProtocolException e) {

            e.printStackTrace();
        }
        catch (IOException e) {

            e.printStackTrace();
        }
        return "async chain perfect";
    }

    @Context
    private ServletContext sc;

    @GET
    @Path("circle_test")
    public String circleTest(@QueryParam("time") int time) throws SQLException {

        if (time == 10) {
            return "";
        }
        // 首先进行redis读写操作
        System.out.println("Jedis OPS======================================================");
        Jedis jedis = new Jedis("localhost", 6379);
        jedis.set("foo", "bar");

        jedis.get("foo");

        jedis.close();

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

        // ws调用begin
        TestService_Service s = new TestService_Service();
        TestService ts = s.getTestServicePort();

        // 设置客户端的配置信息，超时等.
        Client proxy = ClientProxy.getClient(ts);
        HTTPConduit conduit = (HTTPConduit) proxy.getConduit();
        HTTPClientPolicy policy = new HTTPClientPolicy();

        // 连接服务器超时时间
        policy.setConnectionTimeout(30000);
        // 等待服务器响应超时时间
        policy.setReceiveTimeout(30000);

        conduit.setClient(policy);

        ts.echo();
        // ws调用end

        // mysql调用
        Connection c = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/testdb", "root", "root");

        System.out.println("Statement -------------------->");

        Statement st = c.createStatement();

        st.execute("insert into mytest values (1,'zz',23)");

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
        time++;
        HttpUriRequest http2 = new HttpGet(
                "http://localhost:8080/com.creditease.uav.monitorframework.buildFat/rs/chain/circle_test" + "?time="
                        + time);
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

        // dubbo调用begin
        WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(sc);

        IMyDubboService mds = (IMyDubboService) wac.getBean("myDubboServiceC");
        mds.sayHello("zz");
        // dubbo调用end

        return "circle test perfect";
    }
}
