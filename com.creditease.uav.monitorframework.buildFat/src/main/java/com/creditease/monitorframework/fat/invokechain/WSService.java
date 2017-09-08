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

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import com.creditease.monitorframework.fat.client.TestService;
import com.creditease.monitorframework.fat.client.TestService_Service;

/**
 * 用来测试web service
 *
 */
@Singleton
@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Path("ws")
public class WSService {

    @GET
    @Path("test")
    public String test() {

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
        return "web service perfect";
    }
}
