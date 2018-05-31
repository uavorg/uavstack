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

package com.creditease.uav.godeye.rest;

import java.io.InputStream;
import java.util.Map;

import javax.inject.Singleton;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;

import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;

/**
 * 
 * APMRestService description: APM is a new domain
 *
 */
@Singleton
@Path("apm")
public class APMRestService extends GodEyeRestService {

    private class IVCCallback implements HttpClientCallback {

        private AsyncResponse response;

        public IVCCallback(AsyncResponse response) {

            this.response = response;
        }

        @Override
        public void completed(HttpClientCallbackResult result) {

            InputStream input = result.getReplyData();

            response.resume(input);
        }

        @Override
        public void failed(HttpClientCallbackResult result) {

            String reStr = result.getReplyDataAsString();
            if (result.getRetCode() != HttpStatus.SC_BAD_REQUEST) {
                /**
                 * Confusing.......
                 */
                logger.err(this,
                        "get query result failed -returnCode[" + result.getRetCode() + "] and retMsg[" + reStr + "]",
                        result.getException());
                response.resume(reStr);
            }
        }

    }

    @POST
    @Path("ivc/q")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void queryIVC(String data, @Suspended AsyncResponse response) {

        UAVHttpMessage msg = new UAVHttpMessage(data);

        Map<String, String> request = msg.getRequest();
        String content = request.get("content");
        
        if (!StringHelper.isEmpty(content)) {
            String[] contents = content.split(",");
            String[] condition;
            for (String ctn : contents) {
                if(StringHelper.isEmpty(ctn)) {
                    continue;
                }
                condition = ctn.split("=");
                if(condition.length < 2) {
                    response.resume("{\"rs\":\"ERR\",\"msg\":\"query syntax error\"}");
                    return;
                }
                request.put(condition[0], condition[1].replaceAll("\"", ""));
            }
        }
        
        this.doHttpPost("uav.app.apm.ivc.http.addr", "/ivc/q", msg, new IVCCallback(response));
    }

    @POST
    @Path("ivcdata/q")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void queryIVCData(String data, @Suspended AsyncResponse response) {

        UAVHttpMessage msg = new UAVHttpMessage(data);

        this.doHttpPost("uav.app.apm.ivc.http.addr", "/slw/q", msg, new IVCCallback(response));
    }

    @POST
    @Path("jta/q")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void queryJavaThreadAnalysis(String data, @Suspended final AsyncResponse response) {

        UAVHttpMessage msg = new UAVHttpMessage(data);

        this.doHttpPost("uav.app.apm.jta.http.addr", "/jta/q", msg, new HttpClientCallback() {

            @Override
            public void completed(HttpClientCallbackResult result) {

                response.resume(result.getReplyData());
            }

            @Override
            public void failed(HttpClientCallbackResult result) {

                String reStr = result.getReplyDataAsString();

                response.resume(reStr);

                logger.err(this, "query jta http server FAILED. retCode=" + result.getRetCode() + ", msg=" + reStr,
                        result.getException());
            }
        });
    }
}
