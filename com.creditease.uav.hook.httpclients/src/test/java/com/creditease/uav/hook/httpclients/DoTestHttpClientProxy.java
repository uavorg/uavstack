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

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.creditease.monitor.UAVServer;
import com.creditease.monitor.UAVServer.ServerVendor;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.log.ConsoleLogger;
import com.creditease.uav.hook.httpclients.sync.HttpClientHookProxy;

@SuppressWarnings("deprecation")
public class DoTestHttpClientProxy {

    public static void main(String[] args) {

        ConsoleLogger cl = new ConsoleLogger("test");

        cl.setDebugable(true);

        UAVServer.instance().setLog(cl);

        UAVServer.instance().putServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR, ServerVendor.TOMCAT);

        HttpClientHookProxy p = new HttpClientHookProxy("test", Collections.emptyMap());

        p.doInstallDProxy(null, "testApp");

        // testDefaultClient();

        testClient(HttpClients.createDefault());

        // testClient(HttpClients.createMinimal());

        // testClient(HttpClients.createSystem());
    }

    private static void testClient(CloseableHttpClient client) {

        HttpUriRequest httpMethod = new HttpGet("http://localhost:8080/com.creditease.uav.console/ping");

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
    }

    private static void testDefaultClient() {

        DefaultHttpClient client = new DefaultHttpClient();

        HttpUriRequest httpMethod = new HttpGet("http://localhost:9090/com.creditease.uav.console");

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
    }

}
