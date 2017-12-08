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

package com.creditease.uav.opentsdb;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.uav.apphub.core.AppHubBaseRestService;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.cache.api.CacheManagerFactory;

@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Path("db")
public class OpenTSDBRestService extends AppHubBaseRestService {

    private static CacheManager cm = null;

    private static final String OPENTSDB_AGENT_CACHEREGION = "opentsdb.agent.cache";

    private static final String OPENTSDB_AGENT_CACHEREGION_KEY = "opentsdb.agent.cache.key";

    @SuppressWarnings("unchecked")
    @Override
    public void init() {

        // cache manager
        String redisAddrStr = request.getServletContext().getInitParameter("uav.app.opentsdb.redis.store.addr");
        Map<String, Object> redisParamsMap = JSONHelper.toObject(
                request.getServletContext().getInitParameter("uav.app.opentsdb.redis.store.params"), Map.class);
        cm = CacheManagerFactory.build(redisAddrStr, Integer.valueOf(String.valueOf(redisParamsMap.get("min"))),
                Integer.valueOf(String.valueOf(redisParamsMap.get("max"))),
                Integer.valueOf(String.valueOf(redisParamsMap.get("queue"))),
                String.valueOf(redisParamsMap.get("pwd")));

    }

    @GET
    @Path("loadDbs")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public String loadDbs() {

        Map<String, String> total = queryMapRedis();
        return obtainResultString(total);

    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("delDb")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public String delDb(String name) {

        Map<String, String> map = JSONHelper.toObject(name, Map.class);
        name = map.get("name");

        return delDbFromRedis(name);
    }

    @GET
    @Path("queryDb")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public String queryDb(@QueryParam("name") String name) {

        String res = cm.getHash(OPENTSDB_AGENT_CACHEREGION, OPENTSDB_AGENT_CACHEREGION_KEY, name).get(name);
        res = "[" + res + "]";

        return res;

    }

    @POST
    @Path("modifyDb")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public String modifyDb(String data) {

        return modifyDbFromRedis(data);
    }

    @POST
    @Path("addDb")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public String addDb(String data) {

        return addDbFromRedis(data);
    }

    @GET
    @Path("searchDbs")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public String searchDbs(@QueryParam("url") String url) {

        return queryDbByUrl(url);
    }

    private String obtainResultString(Map<String, String> total) {

        String res = null;
        StringBuilder sb = new StringBuilder("[");
        for (String key : total.keySet()) {
            sb.append(total.get(key));
            sb.append(",");
        }
        if (sb.length() > 1) {
            res = sb.substring(0, sb.length() - 1);
        }
        else {
            res = "[";
        }
        res += "]";

        return res;
    }

    @SuppressWarnings("unchecked")
    private String queryDbByUrl(String url) {

        Map<String, String> total = queryMapRedis();

        Iterator<String> iter = total.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            Map<String, String> map = JSONHelper.toObject(total.get(key), Map.class);
            String mapUrl = map.get("url");
            if (!mapUrl.contains(url)) {
                iter.remove();
            }
        }

        return obtainResultString(total);

    }

    private String delDbFromRedis(String name) {

        Map<String, String> total = queryMapRedis();
        total.remove(name);
        cm.delHash(OPENTSDB_AGENT_CACHEREGION, OPENTSDB_AGENT_CACHEREGION_KEY, name);

        return obtainResultString(total);
    }

    @SuppressWarnings("unchecked")
    private String addDbFromRedis(String data) {

        Map<String, String> map = JSONHelper.toObject(data, Map.class);
        String name = map.get("name");
        String res = cm.getHash(OPENTSDB_AGENT_CACHEREGION, OPENTSDB_AGENT_CACHEREGION_KEY, name).get(name);
        if (!StringHelper.isEmpty(res)) {
            Map<String, String> error = new HashMap<String, String>();
            error.put("error", "conflict_name");
            return JSONHelper.toString(error);
        }

        return updateMapRedisByName(name, data);

    }

    @SuppressWarnings("unchecked")
    private String modifyDbFromRedis(String data) {

        Map<String, String> map = JSONHelper.toObject(data, Map.class);
        String name = map.get("name");

        return updateMapRedisByName(name, data);
    }

    private String updateMapRedisByName(String name, String data) {

        Map<String, String> total = queryMapRedis();
        total.put(name, data);
        cm.putHash(OPENTSDB_AGENT_CACHEREGION, OPENTSDB_AGENT_CACHEREGION_KEY, name, data);

        return obtainResultString(total);
    }

    private Map<String, String> queryMapRedis() {

        Map<String, String> total = cm.getHashAll(OPENTSDB_AGENT_CACHEREGION, OPENTSDB_AGENT_CACHEREGION_KEY);
        if (null == total) {
            return new HashMap<String, String>();
        }

        return total;
    }

}
