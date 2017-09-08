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

package com.creditease.uav.feature.healthmanager.datastore.test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.uav.datastore.api.DataStoreProtocol;

/**
 * Query test
 * 
 * @author hongqiang
 */
@SuppressWarnings("deprecation")
public class DoTestOpenTSDBQuery {

    private static final Logger LOG = Logger.getLogger(HttpTestQuery.class.getName());

    @SuppressWarnings("resource")
    public static void main(String[] args) throws ClientProtocolException, IOException {

        String queryurl = "http://localhost:8765/hm/query";
        HttpPost post = new HttpPost(queryurl);
        HttpClient client = new DefaultHttpClient();

        UAVHttpMessage request = new UAVHttpMessage();
        request.putRequest("start", "146234606");
        request.putRequest("end", "1462346020");
        request.putRequest("metric", "urlResp.tmax");
        request.putRequest("aggregator", "sum");
        request.putRequest("downsample", "1ms-avg");
        LinkedHashMap<String, String> tags = new LinkedHashMap<String, String>();
        tags.put("ip", "127.0.0.1");
        request.putRequest("tags", JSONHelper.toString(tags));

        request.putRequest(DataStoreProtocol.DATASTORE_NAME, HealthManagerConstants.DataStore_Monitor);

        String queryJson = JSONHelper.toString(request);

        // String queryJson = "{\"request\":"
        // +"{\"start\":\"1461908927\",\"end\":\"1461908932\","
        // +"\"datastore.name\":\"monitorDataStore\","
        // +"\"queries\":[{"
        // +"\"aggregator\":\"sum\","
        // +"\"metric\":\"urlResp.tmax\","
        // +"\"downsample\":\"1ms-avg\","
        // +"\"tags\":{\"ip\":\"127.0.0.1\"}"
        // +"}]"
        // +"},"
        // +"\"responseAsJsonString\":\"{}\"}";

        StringEntity entity = new StringEntity(queryJson);
        post.setEntity(entity);
        HttpResponse response = client.execute(post);
        if (response != null) {
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                String result = EntityUtils.toString(resEntity);
                LOG.info(result);
            }
        }
    }

}
