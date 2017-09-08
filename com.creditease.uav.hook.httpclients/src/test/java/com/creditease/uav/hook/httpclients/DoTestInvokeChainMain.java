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

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class DoTestInvokeChainMain {

    public static void main(String[] args) {

        // testDefaultClient();

        testClient(HttpClients.createDefault());

        // testClient(HttpClients.createMinimal());

        // testClient(HttpClients.createSystem());
    }

    private static void testClient(CloseableHttpClient client) {

        // http://127.0.0.1:8080/com.creditease.uav.console/rs/gui/vc/new?1945

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

}
