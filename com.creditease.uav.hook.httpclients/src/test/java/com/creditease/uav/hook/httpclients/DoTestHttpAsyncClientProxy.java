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

package com.creditease.uav.hook.httpclients;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import com.creditease.monitor.UAVServer;
import com.creditease.monitor.UAVServer.ServerVendor;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.log.ConsoleLogger;
import com.creditease.uav.hook.httpclients.async.HttpAsyncClientHookProxy;

public class DoTestHttpAsyncClientProxy {

    public static void main(String[] args) {

        ConsoleLogger cl = new ConsoleLogger("test");

        cl.setDebugable(true);

        UAVServer.instance().setLog(cl);

        UAVServer.instance().putServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR, ServerVendor.TOMCAT);

        HttpAsyncClientHookProxy p = new HttpAsyncClientHookProxy("test", Collections.emptyMap());

        p.doInstallDProxy(null, "testApp");

        testClientWithoutCallback(HttpAsyncClients.custom().build());

        testClientWithoutCallback(HttpAsyncClients.createMinimal());

        testClientWithoutCallback(HttpAsyncClients.createDefault());

        testClientWithoutCallback(HttpAsyncClients.createPipelining());

        testClientWithoutCallback(HttpAsyncClients.createSystem());

        testClient(HttpAsyncClients.custom().build());

        testClient(HttpAsyncClients.createMinimal());

        testClient(HttpAsyncClients.createDefault());

        testClient(HttpAsyncClients.createPipelining());

        testClient(HttpAsyncClients.createSystem());
    }

    private static void testClientWithoutCallback(final CloseableHttpAsyncClient client) {

        HttpUriRequest httpMethod = new HttpGet("http://127.0.0.1:8080/apphub/main.html");
        client.start();
        Future<HttpResponse> future = client.execute(httpMethod, null);

        try {
            HttpResponse resp = future.get();

            System.out.println(resp.getStatusLine());
            client.close();
        }
        catch (InterruptedException e) {

            e.printStackTrace();
        }
        catch (ExecutionException e) {

            e.printStackTrace();
        }
        catch (IOException e) {

            e.printStackTrace();
        }

    }

    private static void testClient(final CloseableHttpAsyncClient client) {

        HttpUriRequest httpMethod = new HttpGet("http://127.0.0.1:8080/apphub/main.html");
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
    }

}
