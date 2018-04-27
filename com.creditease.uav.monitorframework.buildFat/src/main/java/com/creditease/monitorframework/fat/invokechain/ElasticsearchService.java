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

import java.net.InetSocketAddress;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.StringHelper;

/**
 * 测试未与任何系统有交互的程序
 *
 */
@Singleton
@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Path("elasticsearch")
public class ElasticsearchService {
    /**
     * 测试用例
     * 
     * @return
     */
    @SuppressWarnings("resource")
    @GET
    @Path("transportTest")
    public String test() {
        
        String[] esAddrs = {"127.0.0.1:9300"}; 
        String clusterName = "";
        String index = "esindex";
        String type = "String";
        String alias = "alias";

        Settings settings = Settings.EMPTY;

        if (!StringHelper.isEmpty(clusterName)) {
                settings = Settings.builder().put("cluster.name", clusterName).build();
        }

        TransportClient client = new PreBuiltTransportClient(settings);

        for (String esAddr : esAddrs) {
            String[] ipport = esAddr.split(":");
            client.addTransportAddress(new InetSocketTransportAddress(
                    new InetSocketAddress(ipport[0], DataConvertHelper.toInt(ipport[1], 9300))));
        }
        
        client.admin().indices().create(new CreateIndexRequest(index)).actionGet().isAcknowledged();
        
        client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists();
        
        client.admin().indices().typesExists(new TypesExistsRequest(new String[] { index }, type)).actionGet().isExists();
        
        client.admin().indices().prepareAliases().addAlias(index, alias).get().isAcknowledged();
        
        client.admin().indices().prepareAliases().removeAlias(index, alias).get().isAcknowledged();
        
        client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet().isAcknowledged();
        
        return "transportTest";
    }
}
