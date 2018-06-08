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

package com.creditease.uav.elasticsearch.client;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.StringHelper;

/**
 * 
 * ESClient description: ESClient的封装类，简化一些操作吧
 *
 */
public class ESClient {

    private TransportClient client;

    public ESClient(String[] esAddrs, String clusterName) {

        init(esAddrs, clusterName);
    }

    public ESClient(String esAddrStr, String clusterName) {

        String[] esAddrs = esAddrStr.split(",");

        init(esAddrs, clusterName);
    }

    /**
     * init
     * 
     * @param esAddrs
     * @param clusterName
     */
    private void init(String[] esAddrs, String clusterName) {

        Settings settings = Settings.EMPTY;

        if (!StringHelper.isEmpty(clusterName)) {
            settings = Settings.builder().put("cluster.name", clusterName).build();
        }

        client = new PreBuiltTransportClient(settings);

        for (String esAddr : esAddrs) {
            String[] ipport = esAddr.split(":");
            client.addTransportAddress(new InetSocketTransportAddress(
                    new InetSocketAddress(ipport[0], DataConvertHelper.toInt(ipport[1], 9300))));
        }
    }

    /**
     * existIndex
     * 
     * @param index
     * @return
     */
    public boolean existIndex(String index) {

        IndicesExistsRequest request = new IndicesExistsRequest(index);
        IndicesExistsResponse response = client.admin().indices().exists(request).actionGet();
        if (response.isExists()) {
            return true;
        }
        return false;
    }

    /**
     * creatIndex
     * 
     * @param index
     * @return
     * @throws IOException
     */
    public boolean creatIndex(String index) throws IOException {

        return creatIndex(index, null, null, null);
    }

    public boolean creatIndex(String index, String type, Map<String, String> set,
            Map<String, Map<String, Object>> mapping) throws IOException {

        CreateIndexRequestBuilder createIndexRequestBuilder = client.admin().indices().prepareCreate(index);
        if (type != null && mapping != null) {
            createIndexRequestBuilder.addMapping(type, createMapping(type, mapping));
        }
        if (set != null) {
            createIndexRequestBuilder.setSettings(createSetting(set));
        }
        CreateIndexResponse resp = createIndexRequestBuilder.execute().actionGet();

        if (resp.isAcknowledged()) {
            return true;
        }

        return false;
    }

    private Settings createSetting(Map<String, String> set) {

        Settings settings = Settings.builder().put(set).build();

        return settings;
    }

    private XContentBuilder createMapping(String type, Map<String, Map<String, Object>> properties) throws IOException {

        XContentBuilder mapping;

        mapping = jsonBuilder().startObject().startObject(type);

        mapping = mapping.startObject("properties");

        for (String key : properties.keySet()) {
            mapping = mapping.startObject(key);

            Map<String, Object> fv = properties.get(key);

            for (String field : fv.keySet()) {
                mapping = mapping.field(field, fv.get(field));
            }

            mapping = mapping.endObject();
        }

        mapping = mapping.endObject();

        mapping = mapping.endObject();

        mapping = mapping.endObject();

        return mapping;
    }

    /**
     * existType
     * 
     * @param index
     * @param type
     * @return
     */
    public boolean existType(String index, String type) {

        TypesExistsRequest request = new TypesExistsRequest(new String[] { index }, type);

        TypesExistsResponse resp = client.admin().indices().typesExists(request).actionGet();

        if (resp.isExists()) {
            return true;
        }
        return false;
    }

    /**
     * updateIndexSetting
     * 
     * @param index
     * @param set
     * @return
     */
    public boolean updateIndexSetting(String index, Map<String, Object> set) {

        try {
            UpdateSettingsResponse usr = client.admin().indices().prepareUpdateSettings(index).setSettings(set).get();
            return usr.isAcknowledged();
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * setIndexTypeMapping require Index Creation first
     * 
     * @param index
     * @param type
     * @param properties
     * @return
     * @throws IOException
     */
    public boolean setIndexTypeMapping(String index, String type, Map<String, Map<String, Object>> properties)
            throws IOException {

        PutMappingRequest pmp = Requests.putMappingRequest(index).type(type).source(createMapping(type, properties));
        PutMappingResponse resp = client.admin().indices().putMapping(pmp).actionGet();

        return resp.isAcknowledged();

    }

    /**
     * 给索引添加别名
     * 
     * @param index
     * @param alias
     * @return
     */
    public boolean addIndexAlias(String index, String alias) {

        try {
            IndicesAliasesResponse resp = client.admin().indices().prepareAliases().addAlias(index, alias).get();

            return resp.isAcknowledged();
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * 给索引删除别名
     * 
     * @param index
     * @param alias
     * @return
     */
    public boolean removeIndexAlias(String index, String alias) {

        try {
            IndicesAliasesResponse resp = client.admin().indices().prepareAliases().removeAlias(index, alias).get();
            return resp.isAcknowledged();
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * getClient
     * 
     * @return
     */
    public TransportClient getClient() {

        return this.client;
    }

    public void close() {

        if (this.client != null) {
            client.close();
        }
    }
}
