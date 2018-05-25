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

package com.creditease.uav.hook.esclient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.UAVServer.ServerVendor;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.log.ConsoleLogger;
import com.creditease.uav.hook.esclient.transport.TransportHookProxy;
import com.creditease.uav.monitorframework.agent.MOFAgent;

/**
 * DoTestTransportHookProxy description: ???
 *
 */
public class DoTestTransportHookProxy {  
    
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException {
       
        ConsoleLogger cl = new ConsoleLogger("test");
        
        cl.setDebugable(true);
        
        UAVServer.instance().setLog(cl);
        
        UAVServer.instance().putServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR, ServerVendor.TOMCAT);
        MOFAgent.mofContext.put("org.uavstack.mof.ext.clsloader", Thread.currentThread().getContextClassLoader());
        
        TransportHookProxy p = new TransportHookProxy("test", Collections.emptyMap());
        
        p.doProxyInstall(null, "testApp");
               
        String[] esAddrs = {"127.0.0.1:9300"}; 
        String clusterName = "";
        String index = "esindex";
        String type = "String";
        String alias = "alias";
        Boolean result;
        
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
        
        result = client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists();
        if(result) {
            result = client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet().isAcknowledged();
        }
        
        client.admin().indices().create(new CreateIndexRequest(index)).actionGet().isAcknowledged();
        
        client.admin().indices().typesExists(new TypesExistsRequest(new String[] { index }, type)).actionGet().isExists();
        
        client.admin().indices().prepareAliases().addAlias(index, alias).get().isAcknowledged();
        
        client.admin().indices().prepareAliases().removeAlias(index, alias).get().isAcknowledged();
        
        client.prepareSearch(index).setSearchType(SearchType.DFS_QUERY_THEN_FETCH).get(TimeValue.timeValueMillis(15000));
    
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("user", "kimchy");
        m.put("postDate", new Date());
        m.put("message", "trying out Elasticsearch");
        
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        bulkRequest.add(client.prepareIndex("twitter", "tweet", "1").setSource(m));
        BulkResponse bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
            System.out.println("Failed");
        }
        
        client.close();   
    }
}
