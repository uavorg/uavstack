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

package com.creditease.agent.feature.nodeopagent;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.PropertiesHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.uav.httpasync.HttpAsyncClient;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;

/**
 * 
 * NodeOperCtrlClient description: 控制Node的客户端小程序
 *
 */
public class NodeOperCtrlClient {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {

        if (args == null || args.length == 0) {
            print("Please input args");
            return;
        }

        /**
         * Step 1: figure out args
         */
        String profile = args[0];

        String profileFilePath = IOHelper.getCurrentPath() + "/config/" + profile + ".properties";

        String port = "10101";

        try {
            Properties p = PropertiesHelper.loadPropertyFile(profileFilePath);

            port = p.getProperty("feature.nodeoperagent.http.port");

            if (port == null) {
                print("Can't Read NodeCtrlPort from profile file[" + profileFilePath + "]");
                return;
            }
        }
        catch (IOException e) {
            print(e);
        }

        String cmd = args[1];

        Map<String, String> jsonArgs = Collections.emptyMap();
        if (args.length >= 3) {

            String jsonArgsStr = args[2];
            jsonArgs = JSONHelper.toObject(jsonArgsStr, Map.class);
        }

        /**
         * Step 2: doCommand
         */
        doCMD(cmd, jsonArgs, port);
    }

    private static void print(Object str) {

        System.out.println(str);
    }

    private static void doCMD(final String cmd, Map<String, String> jsonArgs, String port) {

        UAVHttpMessage msg = new UAVHttpMessage();

        msg.setIntent(cmd);

        for (String key : jsonArgs.keySet()) {
            msg.putRequest(key, jsonArgs.get(key));
        }

        HttpAsyncClient.build(5, 5);

        String url = "http://localhost:" + port + "/node/ctrl";

        print("Node Operation Command[" + cmd + "] START on " + url);

        HttpAsyncClient.instance().doAsyncHttpPost(url, JSONHelper.toString(msg), new HttpClientCallback() {

            @Override
            public void completed(HttpClientCallbackResult result) {

                print("Node Operation Command[" + cmd + "] DONE.");
                System.exit(0);
            }

            @Override
            public void failed(HttpClientCallbackResult result) {

                print("Node Operation Command[" + cmd + "] DONE.");
                System.exit(0);
            }

        });
    }
}
