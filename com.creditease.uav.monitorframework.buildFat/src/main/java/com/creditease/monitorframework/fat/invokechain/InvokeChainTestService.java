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

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 *
 * 调用链整体测试程序
 *
 */
@Singleton
@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Path("ivoke_chain")
public class InvokeChainTestService {

    @GET
    @Path("stress_test")
    public String stressTest() {

        CloseableHttpClient client = HttpClients.createDefault();
        HttpUriRequest http = new HttpGet(
                "http://localhost:8080/com.creditease.uav.monitorframework.buildFat/rs/http/httpclientAsynctest");
        HttpUriRequest httpSync = new HttpGet(
                "http://localhost:8080/com.creditease.uav.monitorframework.buildFat/rs/http/httpclienttest");
        try {
            HttpResponse resp1 = client.execute(http);

            System.out.println(resp1.getStatusLine());

            HttpResponse resp2 = client.execute(httpSync);

            System.out.println(resp2.getStatusLine());

            client.close();
        }
        catch (ClientProtocolException e) {

            e.printStackTrace();
        }
        catch (IOException e) {

            e.printStackTrace();
        }
        return "stress test";
    }
}
