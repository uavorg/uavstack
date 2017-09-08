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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

/**
 * 测试与其他系统通过http通信的
 *
 */
@Singleton
@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Path("http")
public class HttpService {

    /**
     * 同步post测试用例
     * 
     * @return
     */
    @GET
    @Path("httpclientPostTest")
    public String httpclientPostTest() {

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpMethod = new HttpPost(
                "http://localhost:8080/com.creditease.uav.monitorframework.buildFat/rs/TestRestService/methodPath2?user=httpclientPost");
        StringEntity entity = null;
        try {
            entity = new StringEntity("{\"size\": \"httpclientPostTest\"}");
        }
        catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        entity.setContentEncoding("utf-8");
        entity.setContentType("application/json");
        httpMethod.setEntity(entity);

        try {
            HttpResponse resp = client.execute(httpMethod);

            HttpEntity httpEntity = resp.getEntity();
            System.out.println(httpEntity.getContentEncoding());
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpEntity.getContent()));
            StringBuilder body = new StringBuilder();
            String str;
            while ((str = reader.readLine()) != null) {
                body.append(str);
            }
            System.out.println(body);
            System.out.println(resp.getStatusLine());

            client.close();

        }
        catch (ClientProtocolException e) {

            e.printStackTrace();
        }
        catch (IOException e) {

            e.printStackTrace();
        }
        return "httpClientSuccess";
    }

    /**
     * 异步post测试用例
     * 
     * @return
     */
    @GET
    @Path("httpclientAsyncPostTest")
    public String httpclientAsyncPostTest() {

        final CloseableHttpAsyncClient client = HttpAsyncClients.custom().build();
        HttpPost httpMethod = new HttpPost(
                "http://localhost:8080/com.creditease.uav.monitorframework.buildFat/rs/TestRestService/methodPath2?user=chonggege");

        client.start();

        StringEntity entity = null;
        try {
            entity = new StringEntity("{\"size\": \"httpclientAsyncPostTest\"}");
        }
        catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        entity.setContentEncoding("utf-8");
        entity.setContentType("application/json");
        httpMethod.setEntity(entity);

        client.execute(httpMethod, new FutureCallback<HttpResponse>() {

            @Override
            public void completed(HttpResponse result) {

                try {
                    System.out.println(client.getClass().getName() + "---OK");

                    HttpEntity httpEntity = result.getEntity();
                    System.out.println(httpEntity.getContentEncoding());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(httpEntity.getContent()));
                    StringBuilder body = new StringBuilder();
                    String str;
                    while ((str = reader.readLine()) != null) {
                        body.append(str);
                    }
                    System.out.println("response---" + body);
                    System.out.println(result.getStatusLine());

                    client.close();

                }
                catch (ClientProtocolException e) {

                    e.printStackTrace();
                }
                catch (IOException e) {

                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Exception ex) {

                System.out.println(client.getClass().getName() + "---FAIL");
                try {
                    client.close();
                }
                catch (IOException e) {

                    e.printStackTrace();
                }
            }

            @Override
            public void cancelled() {

                System.out.println(client.getClass().getName() + "---CANCEL");
                try {
                    client.close();
                }
                catch (IOException e) {

                    e.printStackTrace();
                }
            }

        });
        return "httpclientAsynctest success";
    }

    /**
     * 同步测试用例
     * 
     * @return
     */
    @GET
    @Path("httpclienttest")
    public String httpclienttest() {

        CloseableHttpClient client = HttpClients.createDefault();
        HttpUriRequest httpMethod = new HttpGet("https://www.baidu.com/");

        try {
            HttpResponse resp = client.execute(httpMethod);

            System.out.println(resp.getStatusLine());

            client.close();

        }
        catch (ClientProtocolException e) {

            e.printStackTrace();
        }
        catch (IOException e) {

            e.printStackTrace();
        }
        // // 模拟做一些事情
        // try {
        // Thread.sleep(100);
        // }
        // catch (InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        return "httpClientSuccess";
    }

    /**
     * 测试同步httpclient异常情况
     * 
     * @return
     */
    @GET
    @Path("httpclienttest_exception")
    public String httpclienttestException() {

        CloseableHttpClient client = HttpClients.createDefault();
        HttpUriRequest httpMethod = new HttpGet("https://www.baidu89.com/");

        try {
            HttpResponse resp = client.execute(httpMethod);

            System.out.println(resp.getStatusLine());

            client.close();

        }
        catch (ClientProtocolException e) {

            e.printStackTrace();
        }
        catch (IOException e) {

            e.printStackTrace();
        }
        return "httpClientSuccess";
    }

    /**
     * 同步客户端3.X版本
     * 
     * @return
     */
    @GET
    @Path("httpclient3test")
    public String httpClient3Test() {

        HttpClient httpClient = new HttpClient();
        HttpMethod method = new GetMethod("https://www.baidu.com/");

        try {
            httpClient.executeMethod(method);
            System.out.println(method.getURI());
            System.out.println(method.getStatusLine());
            System.out.println(method.getName());
            System.out.println(method.getResponseHeader("Server").getValue());
            System.out.println(method.getResponseBodyAsString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return "httpClient3 test success";
    }

    /**
     * 同步客户端3.X版本
     * 
     * @return
     */
    @GET
    @Path("httpclient3Posttest")
    public String httpClient3PostTest() {

        HttpClient httpClient = new HttpClient();
        PostMethod method = new PostMethod(
                "http://localhost:8080/com.creditease.uav.monitorframework.buildFat/rs/TestRestService/methodPath2?user=httpclient3Posttest");

        NameValuePair nameValuePair = new NameValuePair("name", "tom");
        NameValuePair[] pairs = { nameValuePair };
        method.setRequestBody(pairs);
        try {
            httpClient.executeMethod(method);
            System.out.println(method.getURI());
            System.out.println(method.getStatusLine());
            System.out.println(method.getName());
            System.out.println(method.getResponseHeader("Server").getValue());
            System.out.println(method.getResponseBodyAsString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            return method.getResponseBodyAsString();
        }
        catch (IOException e) {
            return e.toString();
        }
    }

    /**
     * 异步测试用例
     * 
     * @return
     */
    @GET
    @Path("httpclientAsynctest")
    public String httpclientAsynctest() {

        final CloseableHttpAsyncClient client = HttpAsyncClients.custom().build();
        HttpUriRequest httpMethod = new HttpGet("https://www.baidu.com/");
        client.start();
        client.execute(httpMethod, new FutureCallback<HttpResponse>() {

            @Override
            public void completed(HttpResponse result) {

                System.out.println(client.getClass().getName() + "---OK");
                try {
                    client.close();
                }
                catch (IOException e) {

                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Exception ex) {

                System.out.println(client.getClass().getName() + "---FAIL");
                try {
                    client.close();
                }
                catch (IOException e) {

                    e.printStackTrace();
                }
            }

            @Override
            public void cancelled() {

                System.out.println(client.getClass().getName() + "---CANCEL");
                try {
                    client.close();
                }
                catch (IOException e) {

                    e.printStackTrace();
                }
            }

        });
        // 模拟做一些事情
        // try {
        // Thread.sleep(100);
        // }
        // catch (InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        return "httpclientAsynctest success";
    }

    /**
     * 异步测试异常用例
     * 
     * @return
     */
    @GET
    @Path("httpclientAsynctest_exception")
    public String httpclientAsynctestException() {

        final CloseableHttpAsyncClient client = HttpAsyncClients.custom().build();
        HttpUriRequest httpMethod = new HttpGet(
                "ht://10.10.37.41:8080/com.creditease.uav.monitorframework.buildFat/rs/redis/aredistest");
        client.start();
        client.execute(httpMethod, new FutureCallback<HttpResponse>() {

            @Override
            public void completed(HttpResponse result) {

                System.out.println(client.getClass().getName() + "---OK");
                try {
                    client.close();
                }
                catch (IOException e) {

                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Exception ex) {

                System.out.println(client.getClass().getName() + "---FAIL");
                ex.printStackTrace();
                try {
                    client.close();
                }
                catch (IOException e) {

                    e.printStackTrace();
                }
            }

            @Override
            public void cancelled() {

                System.out.println(client.getClass().getName() + "---CANCEL");
                try {
                    client.close();
                }
                catch (IOException e) {

                    e.printStackTrace();
                }
            }

        });
        return "httpclientAsynctest success";
    }

    @GET
    @Path("httpclient_double_test")
    public String httpclientDoubleTest() {

        httpclienttest();
        httpclienttest();
        return "httpclient_double_test";
    }

    @GET
    @Path("httpclientAsync_double_test")
    public String httpclientAsyncDoubleTest() {

        httpclientAsynctest();
        final CloseableHttpAsyncClient client = HttpAsyncClients.custom().build();
        HttpUriRequest httpMethod = new HttpGet("http://weibo.com/");
        client.start();
        client.execute(httpMethod, new FutureCallback<HttpResponse>() {

            @Override
            public void completed(HttpResponse result) {

                System.out.println(client.getClass().getName() + "---OK");
                try {
                    client.close();
                }
                catch (IOException e) {

                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Exception ex) {

                System.out.println(client.getClass().getName() + "---FAIL");
                try {
                    client.close();
                }
                catch (IOException e) {

                    e.printStackTrace();
                }
            }

            @Override
            public void cancelled() {

                System.out.println(client.getClass().getName() + "---CANCEL");
                try {
                    client.close();
                }
                catch (IOException e) {

                    e.printStackTrace();
                }
            }

        });
        return "httpclientAsync_double_test";
    }
}
