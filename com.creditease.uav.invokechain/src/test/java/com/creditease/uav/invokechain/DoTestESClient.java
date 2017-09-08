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

package com.creditease.uav.invokechain;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

public class DoTestESClient {

    public static void main(String[] args) {

        TransportClient client = new PreBuiltTransportClient(Settings.EMPTY);

        client.addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress("127.0.0.1", 9300)));

        IndexRequestBuilder irb = client.prepareIndex("uav_test_db", "uav_test_table");

        Map<String, Object> item = new HashMap<String, Object>();

        item.put("name", "zz");
        item.put("age", 1);

        irb.setSource(item);

        IndexResponse ir = irb.get();

        System.out.println(ir.status());

        client.close();
    }

}
