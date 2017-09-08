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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

import com.creditease.monitor.UAVServer;
import com.creditease.monitor.UAVServer.ServerVendor;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.log.ConsoleLogger;
import com.creditease.uav.hook.httpclients3.sync.HttpClient3HookProxy;

public class DoTestHttpClient3Proxy {

    public static void main(String[] args) {

        ConsoleLogger cl = new ConsoleLogger("test");

        cl.setDebugable(true);

        UAVServer.instance().setLog(cl);

        UAVServer.instance().putServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR, ServerVendor.TOMCAT);

        HttpClient3HookProxy p = new HttpClient3HookProxy("test", Collections.emptyMap());

        p.doInstallDProxy(null, "testApp");

        testClient();
    }

    private static void testClient() {

        HttpClient httpClient = new HttpClient();
        HttpMethod method = new GetMethod("http://localhost:8080/apphub/main.html");

        try {
            httpClient.executeMethod(method);
            System.out.println(method.getURI());
            System.out.println(method.getStatusLine());
            System.out.println(method.getName());
            System.out.println(method.getResponseHeader("Server").getValue());
        }
        catch (HttpException e) {
            e.printStackTrace();

        }
        catch (IOException e) {
            e.printStackTrace();

        }
        finally {
            method.releaseConnection();

        }
    }

}
