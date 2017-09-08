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

package com.creditease.uav.agent.heartbeat.test;

import java.io.UnsupportedEncodingException;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.uav.httpasync.HttpAsyncClient;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;

public class DoTestNodeOperCtrl {

    public static void main(String[] args) {

        HttpAsyncClient.build(5, 5);

        UAVHttpMessage msg = new UAVHttpMessage();

        doTestSysPro(msg);

        installMOF(msg);

        // try {
        // System.in.read();
        // }
        // catch (IOException e) {
        //
        // }
        //
        // uninstallMOF(msg);
    }

    private static void uninstallMOF(UAVHttpMessage msg) {

        msg.setIntent("uninstallmof");

        msg.putRequest("orgboot", "/app/t7/t7/bin/bootstrap.jar");
        msg.putRequest("asroot", "/app/t7/t7/bin");
        msg.putRequest("stcmd", "./startup.sh");
        msg.putRequest("pid", "27226");

        post(msg, "localhost");
    }

    private static void installMOF(UAVHttpMessage msg) {

        msg.setIntent("installmof");

        msg.putRequest("uavboot", "/app/uav/uavmof/com.creditease.uav.tomcat/7/boot/bootstrap.jar");
        msg.putRequest("orgboot", "/app/t7/t7/bin/bootstrap.jar");
        msg.putRequest("asroot", "/app/t7/t7/bin");
        msg.putRequest("stcmd", "./startup.sh");
        msg.putRequest("pid", "31758");

        post(msg, "localhost");
    }

    private static void doTestSysPro(UAVHttpMessage msg) {

        msg.setIntent("chgsyspro");

        msg.putRequest("syspro@JAppGroup", "UAV");

        post(msg, null);
    }

    private static void post(UAVHttpMessage msg, String ip) {

        if (ip == null) {
            ip = "localhost";
        }

        try {
            HttpAsyncClient.instance().doAsyncHttpPost("http://" + ip + ":10101/node/ctrl",
                    JSONHelper.toString(msg).getBytes("utf-8"), "application/json", "utf-8", new HttpClientCallback() {

                        @Override
                        public void completed(HttpClientCallbackResult result) {

                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void failed(HttpClientCallbackResult result) {

                            // TODO Auto-generated method stub

                        }
                    });
        }
        catch (UnsupportedEncodingException e) {

            e.printStackTrace();
        }
    }

}
