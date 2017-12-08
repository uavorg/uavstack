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

package com.creditease.uav.xrobot.rest;

import java.util.Map;

import javax.inject.Singleton;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.uav.apphub.core.AppHubBaseRestService;
import com.creditease.uav.httpasync.HttpAsyncException;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;

@Singleton
@Path("xrobot")
public class XRobotRestService extends AppHubBaseRestService {

    private class CmdCallback implements HttpClientCallback {

        AsyncResponse response;

        public CmdCallback(AsyncResponse response) {
            this.response = response;
        }

        @Override
        public void completed(HttpClientCallbackResult result) {

            response.resume(result.getReplyDataAsString());
        }

        @Override
        public void failed(HttpClientCallbackResult result) {

            HttpAsyncException excep = result.getException();

            if (excep != null) {
                response.resume(excep.getCause().getMessage());
            }
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    protected void init() {

        // httpclient
        Map<String, Integer> httpParamsMap = JSONHelper
                .toObject(request.getServletContext().getInitParameter("uav.app.xrobot.http.client.params"), Map.class);
        initHttpClient(httpParamsMap.get("max.con"), httpParamsMap.get("max.tot.con"),
                httpParamsMap.get("sock.time.out"), httpParamsMap.get("con.time.out"),
                httpParamsMap.get("req.time.out"));
    }

    @POST
    @Path("command")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void doCommand(String data, @Suspended AsyncResponse response) {

        UAVHttpMessage msg = new UAVHttpMessage(data);

        this.doHttpPost("uav.app.xrobot.http.addr", "/hit/xrobot", msg, new CmdCallback(response));
    }

}
