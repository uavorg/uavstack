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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;

import com.creditease.agent.helpers.CommonHelper;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.DateTimeHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.uav.apphub.core.AppHubBaseRestService;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.cache.api.CacheManagerFactory;
import com.creditease.uav.helpers.url.BASE64DecoderUrl;
import com.creditease.uav.helpers.uuid.IdWorker;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;

/**
 * GodEye 应用服务端入口
 * 
 * @author zhen zhang
 *
 */
@Singleton
@Path("godeye")
public class GodEyeRestService extends AppHubBaseRestService {

    /**
     * ===========================================回调异步 begin===================================================
     */

	private class CommonCB implements HttpClientCallback {

        private AsyncResponse response;
        private String method;

        public CommonCB(AsyncResponse response, String method) {

            this.response = response;
            this.method = method;
        }

        @Override
        public void completed(HttpClientCallbackResult result) {

            response.resume(result.getReplyData());
        }

        @Override
        public void failed(HttpClientCallbackResult result) {

            String reStr = StringHelper.isEmpty(result.getReplyDataAsString())
                    ? "GodEyeRestService " + method + " is failed."
                    : result.getReplyDataAsString();
            if (result.getRetCode() != HttpStatus.SC_BAD_REQUEST) {
                /**
                 * Confusing.......
                 */
                logger.err(this, "GodEyeRestService " + method + " get result is failed -returnCode["
                        + result.getRetCode() + "] and retMsg[" + reStr + "]", result.getException());
                response.resume(reStr);
            }
            else {
                response.resume(reStr + ",exception=" + result.getException());
            }
        }
    }

    private class LoadMonitorDataFromCacheCB implements HttpClientCallback {

        private AsyncResponse response;
        private String cacheKey;
        private String godeyeCacheRegion;
        private String godeyeFilterGroupCacheRegion;
        private String data;

        public void setResponse(AsyncResponse response) {

            this.response = response;
        }

        public void setCacheKey(String cacheKey) {

            this.cacheKey = cacheKey;
        }

        public void setGodeyeCacheRegion(String godeyeCacheRegion) {

            this.godeyeCacheRegion = godeyeCacheRegion;
        }

        public void setGodeyeFilterGroupCacheRegion(String godeyeFilterGroupCacheRegion) {

            this.godeyeFilterGroupCacheRegion = godeyeFilterGroupCacheRegion;
        }

        public void setData(String data) {

            this.data = data;
        }

        @Override
        public void completed(HttpClientCallbackResult result) {

            String stream = result.getReplyDataAsString();

            if (cacheKey != null) {
                cm.put(godeyeFilterGroupCacheRegion, cacheKey, stream);
                cm.expire(godeyeCacheRegion, cacheKey, 1, TimeUnit.MINUTES);
            }
            response.resume(stream);
        }

        @Override
        public void failed(HttpClientCallbackResult result) {

            /**
             * when read data from cache fails, we could change to openTSDB
             */
            loadMonitorDataFromOpenTSDB(data, response);
        }
    }

    private class NotifyDescQueryCB implements HttpClientCallback {

        private AsyncResponse response;
        private Long time;
        private String ntfkey;
        private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        public void setResponse(AsyncResponse response) {

            this.response = response;
        }

        public void setTime(Long time) {

            this.time = time;
        }

        public void setNtfkey(String ntfkey) {

            this.ntfkey = ntfkey;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void completed(HttpClientCallbackResult result) {

            String respStr = result.getReplyDataAsString();
            HashMap<String, String> respMap = JSONHelper.toObject(respStr, HashMap.class);
            Map<String, Object> rs = JSONHelper.toObjectArray(respMap.get("rs"), Map.class).get(0);

            /* begin 聚集查询 */

            List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
            HashMap<String, Object> startTime = new HashMap<String, Object>();
            HashMap<String, Object> startTimeRegex = new HashMap<String, Object>();
            HashMap<String, Object> startTimeMatch = new HashMap<String, Object>();
            startTime.put("time", time - 1);
            startTimeRegex.put(">", startTime);
            startTimeMatch.put("match", startTimeRegex);
            list.add(startTimeMatch);

            if (null != rs.get("latestrecord_ts")) {
                HashMap<String, Object> endTime = new HashMap<String, Object>();
                HashMap<String, Object> endTimeRegex = new HashMap<String, Object>();
                HashMap<String, Object> endTimeMatch = new HashMap<String, Object>();
                endTime.put("time", Long.valueOf(String.valueOf(rs.get("latestrecord_ts"))) + 1);
                endTimeRegex.put("<", endTime);
                endTimeMatch.put("match", endTimeRegex);
                list.add(endTimeMatch);
            }

            HashMap<String, Object> ntfkeyMap = new HashMap<String, Object>();
            HashMap<String, Object> ntfkeyMatch = new HashMap<String, Object>();
            ntfkeyMap.put("ntfkey", ntfkey);
            ntfkeyMatch.put("match", ntfkeyMap);
            list.add(ntfkeyMatch);

            HashMap<String, Object> sortMap = new HashMap<String, Object>();
            HashMap<String, Object> sort = new HashMap<String, Object>();
            sort.put("values", "time");
            sort.put("sortorder", "-1");
            sortMap.put("sort", sort);

            list.add(sortMap);
            // 封装http请求数据
            UAVHttpMessage message = new UAVHttpMessage();
            message.putRequest("mgo.sql", JSONHelper.toString(list));
            message.putRequest("datastore.name", "MT_Notify");
            message.putRequest("mgo.coll.name", "uav_notify");

            String lastStr = null == rs.get("lattest_ts") ? ""
                    : simpleDateFormat.format(new Date((Long) rs.get("lattest_ts")));
            logger.info(this, "预警详情  time=[" + time + "]-[" + rs.get("lattest_ts") + "],time=["
                    + simpleDateFormat.format(new Date(time)) + "]-[" + lastStr + "],ntfky=[" + ntfkey + "]");

            doHttpPost("uav.app.godeye.healthmanager.http.addr", "/hm/query", message, new HttpClientCallback() {

                @Override
                public void completed(HttpClientCallbackResult result) {

                    String respStr = result.getReplyDataAsString();
                    Map<String, String> resMap = JSONHelper.toObject(respStr, Map.class);
                    response.resume(resMap.get("rs"));
                }

                @Override
                public void failed(HttpClientCallbackResult result) {

                    logger.err(this, "预警详情步骤二 result is failed :" + result.getException());
                    String resp = "预警详情步骤二 GodEyeRestService notifyDescQuery is failed.";
                    response.resume(resp);
                }

            });
            /* end */

        }

        @Override
        public void failed(HttpClientCallbackResult result) {

            logger.err(this, "预警详情步骤一 result is failed :" + result.getException());
            String resp = "预警详情步骤一  GodEyeRestService notifyDescQuery is failed.";
            response.resume(resp);
        }

    }

    private class NotifyUpdateCB implements HttpClientCallback {

        private AsyncResponse response;
        private String log;

        public void setResponse(AsyncResponse response) {

            this.response = response;
        }

        public void setLog(String log) {

            this.log = log;
        }

        @Override
        public void completed(HttpClientCallbackResult result) {

            String respStr = result.getReplyDataAsString();
            logger.info(this, "预警update : " + log + ",respStr=[" + respStr + "]");
            response.resume("T");
        }

        @Override
        public void failed(HttpClientCallbackResult result) {

            logger.err(this, "预警update异常: " + log + "\r\n", result.getException());
            response.resume("F");
        }
    }

    private class NotifyStrategyQuery implements HttpClientCallback {

        private AsyncResponse response;
        private int pagesize;
        private int pageindex;

        public void setResponse(AsyncResponse response) {

            this.response = response;
        }

        public void setPagesize(int pagesize) {

            this.pagesize = pagesize;
        }

        public void setPageindex(int pageindex) {

            this.pageindex = pageindex;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public void completed(HttpClientCallbackResult result) {

            String rsStr = result.getReplyDataAsString();
            HashMap<String, String> rsMap = JSONHelper.toObject(rsStr, HashMap.class);

            HashMap<String, String> resultMap = JSONHelper.toObject(rsMap.get("rs"), HashMap.class);
            Map<String, String> sortMap = new TreeMap<String, String>(new Comparator<String>() {

                @Override
                public int compare(String str1, String str2) {

                    return str1.compareTo(str2);
                }
            });
            sortMap.putAll(resultMap);

            Map<String, String> pageMap = new HashMap<String, String>();
            int begin = (pageindex - 1) * pagesize;
            int end = begin + pagesize;

            Iterator i = sortMap.keySet().iterator();
            int index = 0;
            while (i.hasNext()) {
                String key = String.valueOf(i.next());
                if (index >= begin && index < end) {
                    pageMap.put(key, sortMap.get(key));
                }
                index++;
            }

            HashMap<String, Object> respMap = new HashMap<String, Object>();
            respMap.put("count", resultMap.size());
            respMap.put("rs", pageMap);

            response.resume(JSONHelper.toString(respMap));
        }

        @Override
        public void failed(HttpClientCallbackResult result) {

            logger.err(this, "GodEyeRestService notifyStrategyQuery result is failed :" + result.getException());
            String resp = "GodEyeRestService notifyStrategyQuery is failed.";
            response.resume(resp);
        }
    }

    private class NotifyStrategyGetCB implements HttpClientCallback {

        private AsyncResponse response;

        public void setResponse(AsyncResponse response) {

            this.response = response;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void completed(HttpClientCallbackResult result) {

            String rsStr = result.getReplyDataAsString();
            HashMap<String, String> rsMap = JSONHelper.toObject(rsStr, HashMap.class);
            response.resume(rsMap.get("rs"));
        }

        @Override
        public void failed(HttpClientCallbackResult result) {

            logger.err(this, "GodEyeRestService notifyStrategyQuery result is failed :" + result.getException());
            String resp = "GodEyeRestService notifyStrategyQuery is failed.";
            response.resume(resp);
        }

    }

    private class NotifyStrategyUpdateCB implements HttpClientCallback {

        private AsyncResponse response;
        private String stgyData;

        public void setResponse(AsyncResponse response) {

            this.response = response;
        }

        public void setStgyData(String stgyData) {

            this.stgyData = stgyData;
        }

        @Override
        public void completed(HttpClientCallbackResult result) {

            String respStr = result.getReplyDataAsString();
            logger.info(this, "修改策略 =[" + stgyData + "],resp=[" + respStr + "]");
            if (respStr.indexOf("OK") > -1) {
                response.resume("T");
            }
            else {
                response.resume("F");
            }
        }

        @Override
        public void failed(HttpClientCallbackResult result) {

            logger.err(this, "修改策略 result is failed :" + result.getException());
            response.resume("F");
        }
    }

    private class NotifyStrategyRemoveCB implements HttpClientCallback {

        private AsyncResponse response;
        private String data;

        public void setResponse(AsyncResponse response) {

            this.response = response;
        }

        public void setData(String data) {

            this.data = data;
        }

        @Override
        public void completed(HttpClientCallbackResult result) {

            String respStr = result.getReplyDataAsString();
            logger.info(this, "删除策略 =[" + data + "],resp=[" + respStr + "]");
            if (respStr.indexOf("OK") > -1) {
                response.resume("T");
            }
            else {
                response.resume("F");
            }
        }

        @Override
        public void failed(HttpClientCallbackResult result) {

            logger.err(this, "删除策略 result is failed :" + result.getException());
            response.resume("F");
        }

    }

    /**
     * ===========================================回调异步 end===================================================
     */

    private String godeyeCacheRegion = "apphub.app.godeye.cache";

    private String godeyeFilterGroupCacheRegion = "apphub.app.godeye.filter.cache";

    private String godeyeFilterGroupCacheRegionKey = "email.list.group";

    protected CacheManager cm = null;

    protected long cacheAccessLimit = 0;

    protected Map<String, Integer> mongodbQueryRangeMap = null;

    protected boolean isCheckNodeOperSecurity = true;

    @SuppressWarnings("unchecked")
    @Override
    protected void init() {

        // httpclient
        Map<String, Integer> httpParamsMap = JSONHelper
                .toObject(request.getServletContext().getInitParameter("uav.app.godeye.http.client.params"), Map.class);
        initHttpClient(httpParamsMap.get("max.con"), httpParamsMap.get("max.tot.con"),
                httpParamsMap.get("sock.time.out"), httpParamsMap.get("con.time.out"),
                httpParamsMap.get("req.time.out"));

        isCheckNodeOperSecurity = DataConvertHelper
                .toBoolean(request.getServletContext().getInitParameter("uav.server.nodeoper.security"), true);

        // cache manager
        String redisAddrStr = request.getServletContext().getInitParameter("uav.app.godeye.redis.store.addr");
        Map<String, Object> redisParamsMap = JSONHelper
                .toObject(request.getServletContext().getInitParameter("uav.app.godeye.redis.store.params"), Map.class);
        cm = CacheManagerFactory.build(redisAddrStr, Integer.valueOf(String.valueOf(redisParamsMap.get("min"))),
                Integer.valueOf(String.valueOf(redisParamsMap.get("max"))),
                Integer.valueOf(String.valueOf(redisParamsMap.get("queue"))),
                String.valueOf(redisParamsMap.get("pwd")));

        cacheAccessLimit = DataConvertHelper.toInt(Integer.valueOf(String.valueOf(redisParamsMap.get("cacheaccess"))),
                0) * 60000;

        mongodbQueryRangeMap = JSONHelper.toObject(
                request.getServletContext().getInitParameter("uav.app.godeye.notify.mongodb.query.range"), Map.class);
    }

    /**
     * 应用监控：获取应用的IPLink
     * 
     * @param data
     *            a json string ["UAV@http://127.0.0.1:8090/apphub","TestGroup@http://127.0.0.1:9090/testapp"]
     * @param response
     */
    @POST
    @Path("profile/q/iplink")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void loadAppIPLinkList(String data, @Suspended AsyncResponse response) {

        UAVHttpMessage message = new UAVHttpMessage();

        message.setIntent("iplnk");

        message.putRequest("appgpids", data);

        this.doHttpPost("uav.app.godeye.healthmanager.http.addr", "/hm/cache/q", message,
                new CommonCB(response, "loadAppIPLinkList"));
    }

    /**
     * 读取profile的某个lazy模式加载的数据
     * 
     * @param data
     * @param response
     */
    @POST
    @Path("profile/q/detail")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void loadAppProfileDetail(String data, @Suspended AsyncResponse response) {

        UAVHttpMessage message = new UAVHttpMessage(data);

        loadAppProfileListFromHttp(message, response);
    }

    /**
     * 应用服务监控：全网应用Profile SnapShot
     * 
     * @return
     */
    @GET
    @Path("profile/q/cache")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void loadAppProfileList(@QueryParam("fkey") String fkey, @QueryParam("fvalue") String fvalue,
            @Suspended AsyncResponse response) {

        UAVHttpMessage message = new UAVHttpMessage();
        message.setIntent("profile");
        if (StringHelper.isEmpty(fkey) || StringHelper.isEmpty(fvalue)) {
            String groups = getUserGroupsByFilter(request);
            if ("NOMAPPING".equals(groups)) {
                response.resume("{\"rs\":\"{}\"}");
            }
            else if ("ALL".equals(groups)) {
                loadAppProfileListFromHttp(message, response);
            }
            else {
                message.putRequest("fkey", "appgroup");
                message.putRequest("fvalue", groups);
                loadAppProfileListFromHttp(message, response);
            }
        }
        else {
            message.putRequest("fkey", fkey);
            message.putRequest("fvalue", fvalue);
            loadAppProfileListFromHttp(message, response);
        }

    }

    private void loadAppProfileListFromHttp(UAVHttpMessage message, final AsyncResponse response) {

    	this.doHttpPost("uav.app.godeye.healthmanager.http.addr", "/hm/cache/q", message,
                new CommonCB(response, "loadAppProfileListFromHttp"));
    }

    /**
     * 应用容器监控+UAV节点信息
     * 
     * @return
     */
    @GET
    @Path("node/q/cache")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void loadUavNetworkInfo(@QueryParam("fkey") String fkey, @QueryParam("fvalue") String fvalue,
            @Suspended AsyncResponse response) {

        UAVHttpMessage message = new UAVHttpMessage();
        message.setIntent("node");

        if (StringHelper.isEmpty(fkey) || StringHelper.isEmpty(fvalue)) {
            String groups = getUserGroupsByFilter(request);
            if ("NOMAPPING".equals(groups)) {
                response.resume("{\"rs\":\"{}\"}");
            }
            else if ("ALL".equals(groups)) {
                loadUavNetworkInfoFromHttp(message, response);
            }
            else {
                message.putRequest("fkey", "group");
                message.putRequest("fvalue", groups);
                loadUavNetworkInfoFromHttp(message, response);
            }
        }
        else {
            message.putRequest("fkey", fkey);
            message.putRequest("fvalue", fvalue);
            loadUavNetworkInfoFromHttp(message, response);
        }

    }

    private void loadUavNetworkInfoFromHttp(UAVHttpMessage message, final AsyncResponse response) {

    	this.doHttpPost("uav.app.godeye.hbquery.http.addr", "/hb/query", message,
                new CommonCB(response, "loadUavNetworkInfoFromHttp"));
    }

    /**
     * 
     * 从缓存查询最近1min的MonitorData
     * 
     * @param data
     * @param response
     */
    private void loadMonitorDataFromCache(String data, @Suspended AsyncResponse response, String cacheKey) {

        UAVHttpMessage message = new UAVHttpMessage();

        message.setIntent("monitor");

        message.putRequest("cache.query.json", data);

        LoadMonitorDataFromCacheCB callback = new LoadMonitorDataFromCacheCB();
        callback.setCacheKey(cacheKey);
        callback.setData(data);
        callback.setGodeyeCacheRegion(godeyeCacheRegion);
        callback.setGodeyeFilterGroupCacheRegion(godeyeFilterGroupCacheRegion);
        callback.setResponse(response);

        this.doHttpPost("uav.app.godeye.healthmanager.http.addr", "/hm/cache/q", message, callback);
    }

    /**
     * MonitorData 查询
     * 
     * @param data
     *            QUERY JSON 字符串
     * 
     *            {
     * 
     *            "start": 1356998400,
     * 
     *            "end": 1356998460,
     * 
     *            "queries": [
     * 
     *            { "aggregator": "sum",
     * 
     *            "metric": "appResp.tmax",
     * 
     *            "tags": {
     * 
     *            "ip": "127.0.0.1",
     * 
     *            "pgid":
     *            "E:/UAVIDE/tomcat/apache-tomcat-7.0.65---E:/UAVIDE/defaultworkspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp0",
     * 
     *            "instid": "http://127.0.0.1:8080/@ROOT"
     * 
     *            },
     * 
     *            ..... <------可以包含多个子查询
     * 
     *            }
     * 
     *            ]
     * 
     *            }
     * @param response
     * 
     *            [
     * 
     *            {
     * 
     *            "metric": "tsd.hbase.puts",
     * 
     *            "tags": {
     * 
     *            "host": "tsdb-1.mysite.com"
     * 
     *            },
     * 
     *            "aggregatedTags": [],
     * 
     *            "dps": {
     * 
     *            "1365966001": 3758788892,
     * 
     *            "1365966061": 3758804070,
     * 
     *            "1365974281": 3778141673
     * 
     *            }
     * 
     *            },
     * 
     *            {
     * 
     *            "metric": "tsd.hbase.puts",
     * 
     *            "tags": {
     * 
     *            "host": "tsdb-2.mysite.com"
     * 
     *            },
     * 
     *            "aggregatedTags": [],
     * 
     *            "dps": { "1365966001": 3902179270,
     * 
     *            "1365966062": 3902197769,
     * 
     *            "1365974281": 3922266478
     * 
     *            }
     * 
     *            }
     * 
     *            ]
     */
    @POST
    @Path("monitor/q/hm")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void getMonitorData(String data, @Suspended AsyncResponse response) {

        @SuppressWarnings("rawtypes")
        Map mData = JSONHelper.toObject(data, Map.class);

        Long edTime = (Long) mData.get("end");
        long curTime = System.currentTimeMillis();
        long span = curTime - edTime;

        /**
         * Step 1： < 1 min then goes to cache
         */
        this.getLogger().info(this, "Check Monitor Data Time Range Gap: timeGap=" + span);
        if (span < 70000) {

            mData.remove("start");
            mData.remove("end");

            String cacheKey = "md-" + JSONHelper.toString(mData).hashCode();

            String cache = cm.get(godeyeCacheRegion, cacheKey);

            /**
             * Step 1.1: read data from cache, this benefits to all users in 1 min access, no need reload from uav node
             * service
             */
            if (!StringHelper.isEmpty(cache)) {
                this.getLogger().info(this, "Get Monitor Data from AppHub Cache: cacheKey=" + cacheKey);
                response.resume(cache);
                return;
            }

            /**
             * Step 1.2: read data from healthmanager cache
             * 
             * : thinking about the cache system
             */
            this.getLogger().info(this, "Get Monitor Data from HealthManager Cache: cacheKey=" + cacheKey);
            loadMonitorDataFromCache(data, response, null);
            return;
        }

        /**
         * Step 2: > 1 min may be from cache, depends on the configuration
         * "feature.healthmanager.MT_Monitor.ds.cachetime" on HealthManager
         */
        if (cacheAccessLimit > 70000 && (span > 70000 && span < cacheAccessLimit)) {

            /**
             * Step 1.2: read data from healthmanager cache
             */
            this.getLogger().info(this, "Get Monitor Data from HealthManager Cache(>1min): end="
                    + DateTimeHelper.toStandardDateFormat(edTime));
            loadMonitorDataFromCache(data, response, null);
            return;
        }

        /**
         * Step 3： > 1 min and no cache, then goes to opentsdb
         */
        this.getLogger().info(this, "Get Monitor Data from OpenTSDB");
        loadMonitorDataFromOpenTSDB(data, response);
    }

    @POST
    @Path("monitor/q/hm/db")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void getMonitorDataFromOpenTSDB(String data, @Suspended AsyncResponse response) {

        loadMonitorDataFromOpenTSDB(data, response);
    }

    private void loadMonitorDataFromOpenTSDB(String data, final AsyncResponse response) {

        UAVHttpMessage message = new UAVHttpMessage();
        message.putRequest("opentsdb.query.json", data);
        message.putRequest("datastore.name", MonitorDataFrame.MessageType.Monitor.toString());

        this.doHttpPost("uav.app.godeye.healthmanager.http.addr", "/hm/query", message,
                new CommonCB(response, "loadMonitorDataFromOpenTSDB"));
    }

    /**
     * 显示当前app的最近实现的日志信息, 默认查询指定时间内的日志记录
     * 
     * @return
     */
    @GET
    @Path("log/q/hm/{appid}")
    @Deprecated
    public void getAppLog(@PathParam("appid") String appid, @QueryParam("timespan") String timespan,
            @QueryParam("psize") String pagesize, @Suspended AsyncResponse response) {

        try {

            // 默认：5分钟
            long timeLength = DataConvertHelper.toLong(timespan, 1000 * 60 * 5);
            // 默认不传limit
            StringBuilder data = new StringBuilder("{\"appid\":\"").append(appid).append("\", \"endtime\":")
                    .append(System.currentTimeMillis() - timeLength);
            if (pagesize != null && StringHelper.isNumeric(pagesize)) {
                data.append(",\"psize\":").append(Long.parseLong(pagesize));
            }
            data.append("}");
            queryAppLog(data.toString(), response);
        }
        catch (Exception e) {
            logger.err(this, "Error:" + e.getMessage(), e);
        }
    }

    /**
     * 根据查询条件，显示日志信息
     * 
     * @param data<br>
     *            格式如下：<br>
     *            {"appid":"string", "ip":"string", "svrid":"string", "logid"="string", starttime:long毫秒数,
     *            endtime:long毫秒数, "psize": 显示行数, "reversed"=true}<br>
     *            1. 当reversed=true时，starttime < endtime; endtime必输，startime默认为当前时间 <br>
     *            2. 当reversed=false时，starttime > endtime;starttime必输，endtime 默认为当前时间
     * @return
     */
    @POST
    @Path("log/q/hm")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Deprecated
    public void customizeQuery(String data, @Suspended AsyncResponse response) {

        queryAppLog(data, response);
    }

    @Deprecated
    protected void queryAppLog(String data, AsyncResponse response) {

        UAVHttpMessage message = new UAVHttpMessage();
        message.putRequest("hbase.query.json", data);
        message.putRequest("datastore.name", MonitorDataFrame.MessageType.Log.toString());

        this.doHttpPost("uav.app.godeye.healthmanager.http.addr", "/hm/query", message,
                new CommonCB(response, "queryAppLog"));

    }

    /**
     * 先获取第一条预警信息,条件：time，ntfkey 获取当前查看时间:如果有则取其时间范围内数据，没有则取time以后所有数据
     * 
     * @param data
     * @param response
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @POST
    @Path("notify/q/desc/hm")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void notifyDescQuery(String data, @Suspended AsyncResponse response) throws Exception {

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Map<String, String> jsonParam = JSONHelper.toObject(data, Map.class);

        String ntfkey = jsonParam.get("ntfkey");
        Long time;
        if ("link".equals(jsonParam.get("type"))) {
            ntfkey = new String(new BASE64DecoderUrl().decodeBuffer(ntfkey), "utf-8");
            time = Long.valueOf(ntfkey.substring(ntfkey.lastIndexOf("&") + 1));
            ntfkey = ntfkey.substring(0, ntfkey.lastIndexOf("&"));
        }
        else {
            time = simpleDateFormat.parse(jsonParam.get("time")).getTime();
        }

        HashMap<String, Object> requestWhere = new HashMap<String, Object>();
        requestWhere.put("ntfkey", ntfkey);
        requestWhere.put("firstrecord", "true");
        requestWhere.put("time", time);

        HashMap<String, Object> requestParam = new HashMap<String, Object>();
        requestParam.put("where", requestWhere);

        HashMap<String, Object> sort = new HashMap<String, Object>();
        sort.put("values", "time");
        sort.put("sortorder", "-1");
        requestParam.put("sort", sort);

        // 封装http请求数据
        UAVHttpMessage message = new UAVHttpMessage();
        message.putRequest("mgo.sql", JSONHelper.toString(requestParam));
        message.putRequest("datastore.name", "MT_Notify");
        message.putRequest("mgo.coll.name", "uav_notify");

        NotifyDescQueryCB callback = new NotifyDescQueryCB();
        callback.setResponse(response);
        callback.setNtfkey(ntfkey);
        callback.setTime(time);

        doHttpPost("uav.app.godeye.healthmanager.http.addr", "/hm/query", message, callback);

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @POST
    @Path("notify/q/hm")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void notifyQuery(String data, @Suspended AsyncResponse response) {

        // 数据权限begin
        String groups = getUserGroupsByFilter(request);
        List groupList = Collections.emptyList();
        boolean checkAuthor = true;

        if ("NOMAPPING".equals(groups)) {
            checkAuthor = false;
        }
        else if ("ALL".equals(groups)) {
            /**
             * nothing
             */
        }
        else {
            String[] gs = groups.split(",");
            groupList = new ArrayList<HashMap<String, String>>();

            for (String group : gs) {
                Map temp = new HashMap<>(1);
                temp.put("appgroup", group);
                groupList.add(temp);
            }
        }
        // 数据权限end

        if (checkAuthor) {
            // mongodb .为关键字，封装时替换为#交由后台处理
            data = data.replaceAll("\\.", "#");
            Map whereMap = new HashMap<>();
            whereMap.put("firstrecord", "true");

            // 封装组合查询条件
            HashMap<String, String> jsonParam = JSONHelper.toObject(data, HashMap.class);
            if (jsonParam.containsKey("search")) {
                String whereStr = "[{\"regex\":{\"ip\":\"searchValue\"}},{\"regex\":{\"eventid\":\"searchValue\"}},{\"regex\":{\"title\":\"searchValue\"}},{\"regex\":{\"host\":\"searchValue\"}}]";
                // mongodb .为关键字，封装时替换为#交由后台处理
                String inputSearchStr = jsonParam.get("search").replaceAll("\\.", "#");
                whereStr = whereStr.replaceAll("searchValue", inputSearchStr);
                whereMap.put("or1", JSONHelper.toObjectArray(whereStr, Map.class));
            }

            if (!groupList.isEmpty()) {
                whereMap.put("or2", groupList);
            }

            // 以最近触发预警记录的时间排序
            Map sortMap = new HashMap<>();
            sortMap.put("values", "latestrecord_ts");
            sortMap.put("sortorder", "-1");

            Map map = new HashMap();
            map.put("sort", sortMap);
            map.put("limit", mongodbQueryRangeMap.get("limit"));
            map.put("pageindex", String.valueOf(jsonParam.get("pageindex")));
            map.put("pagesize", String.valueOf(jsonParam.get("pagesize")));
            map.put("where", whereMap);

            // 封装http请求数据
            UAVHttpMessage message = new UAVHttpMessage();
            message.putRequest("mgo.sql", JSONHelper.toString(map));
            message.putRequest("datastore.name", "MT_Notify");
            message.putRequest("mgo.coll.name", "uav_notify");

            doHttpPost("uav.app.godeye.healthmanager.http.addr", "/hm/query", message,
                    new CommonCB(response, "notifyQuery"));
        }
        else {
            response.resume("{\"rs\":\"[]\"}");
        }

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @POST
    @Path("notify/q/count/hm")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void notifyCountQuery(String data, @Suspended AsyncResponse response) {

        // 数据权限begin
        String groups = getUserGroupsByFilter(request);
        List groupList = Collections.emptyList();
        boolean checkAuthor = true;

        if ("NOMAPPING".equals(groups)) {
            checkAuthor = false;
        }
        else if ("ALL".equals(groups)) {
            /**
             * nothing
             */
        }
        else {
            String[] gs = groups.split(",");
            groupList = new ArrayList<HashMap<String, String>>();

            for (String group : gs) {
                Map temp = new HashMap<>(1);
                temp.put("appgroup", group);
                groupList.add(temp);
            }
        }
        // 数据权限end

        if (checkAuthor) {
            // mongodb .为关键字，封装时替换为#交由后台处理
            data = data.replaceAll("\\.", "#");
            Map whereMap = new HashMap<>();
            whereMap.put("firstrecord", "true");

            // 封装组合查询条件
            HashMap<String, String> jsonParam = JSONHelper.toObject(data, HashMap.class);
            if (jsonParam.containsKey("search")) {
                String whereStr = "[{\"regex\":{\"ip\":\"searchValue\"}},{\"regex\":{\"eventid\":\"searchValue\"}},{\"regex\":{\"title\":\"searchValue\"}},{\"regex\":{\"host\":\"searchValue\"}}]";
                // mongodb .为关键字，封装时替换为#交由后台处理
                String inputSearchStr = jsonParam.get("search").replaceAll("\\.", "#");
                whereStr = whereStr.replaceAll("searchValue", inputSearchStr);
                whereMap.put("or1", JSONHelper.toObjectArray(whereStr, Map.class));
            }

            if (!groupList.isEmpty()) {
                whereMap.put("or2", groupList);
            }

            Map map = new HashMap();
            map.put("limit", mongodbQueryRangeMap.get("limit"));
            map.put("where", whereMap);
            map.put("count", "true");

            // 封装http请求数据
            UAVHttpMessage message = new UAVHttpMessage();
            message.putRequest("mgo.sql", JSONHelper.toString(map));
            message.putRequest("datastore.name", "MT_Notify");
            message.putRequest("mgo.coll.name", "uav_notify");

            doHttpPost("uav.app.godeye.healthmanager.http.addr", "/hm/query", message,
                    new CommonCB(response, "notifyCountQuery"));
        }
        else {
            response.resume("{'rs':'[{\"count\":15}]'}");
        }
    }

    @POST
    @Path("notify/update/hm")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void notifyUpdate(String data, @Suspended AsyncResponse response) throws Exception {

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        @SuppressWarnings("unchecked")
        Map<String, String> jsonParam = JSONHelper.toObject(data, Map.class);
        String action = jsonParam.get("action");
        String ntfkey = jsonParam.get("ntfkey");
        String time = jsonParam.get("time");

        if ("link".equals(jsonParam.get("type"))) {
            ntfkey = new String(new BASE64DecoderUrl().decodeBuffer(ntfkey), "utf-8");
            time = ntfkey.substring(ntfkey.lastIndexOf("&") + 1);
            ntfkey = ntfkey.substring(0, ntfkey.lastIndexOf("&"));
        }
        else {
            time = String.valueOf(simpleDateFormat.parse(time).getTime());
        }

        String loginUser = "";
        HttpSession session = request.getSession(false);
        if (null != session) {
            loginUser = String.valueOf(session.getAttribute("apphub.gui.session.login.user.id"));
        }

        String log = "loginUser=[" + loginUser + "],type=[" + jsonParam.get("type") + "],action=[" + action
                + "],ntfkey=[" + ntfkey + "],time=[" + time + "]";

        if (!"".equals(ntfkey) && !"undefined".equals(ntfkey)) {
            UAVHttpMessage message = new UAVHttpMessage();
            message.putRequest("action", action);
            message.putRequest("ntfkey", ntfkey);
            message.putRequest("time", time);

            NotifyUpdateCB callback = new NotifyUpdateCB();
            callback.setResponse(response);
            callback.setLog(log);
            doHttpPost("uav.app.godeye.notify.update.http.addr", "/nc/update", message, callback);
        }
    }

    @POST
    @Path("notify/q/event/hm")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void notifyEvent(@Suspended AsyncResponse response) throws Exception {

        HashMap<String, Object> requestParam = new HashMap<String, Object>();
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("distinct", "eventid");
        requestParam.put("group", data);

        List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

        // 过滤数据,只查询最近的指定数据 begin
        HashMap<String, Object> sortMap = new HashMap<String, Object>();
        HashMap<String, Object> sort = new HashMap<String, Object>();
        sort.put("values", "time");
        sort.put("sortorder", "-1");
        sortMap.put("sort", sort);
        list.add(sortMap);

        // modify start : 只返回firstrecord为true的事件id
        HashMap<String, Object> firstRecord = new HashMap<String, Object>();
        HashMap<String, Object> firstRecordMatch = new HashMap<String, Object>();
        firstRecord.put("firstrecord", "true");
        firstRecordMatch.put("match", firstRecord);
        list.add(firstRecordMatch);
        // modify end

        HashMap<String, Object> skipMap = new HashMap<String, Object>();
        skipMap.put("skip", mongodbQueryRangeMap.get("skip"));
        list.add(skipMap);

        HashMap<String, Object> limitMap = new HashMap<String, Object>();
        limitMap.put("limit", mongodbQueryRangeMap.get("limit"));
        list.add(limitMap);
        // 过滤数据,只查询最近的指定数据 end

        list.add(requestParam);

        // 封装http请求数据
        UAVHttpMessage message = new UAVHttpMessage();
        message.putRequest("mgo.sql", JSONHelper.toString(list));
        message.putRequest("datastore.name", "MT_Notify");
        message.putRequest("mgo.coll.name", "uav_notify");

        doHttpPost("uav.app.godeye.healthmanager.http.addr", "/hm/query", message,
                new CommonCB(response, "notifyEvent"));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @POST
    @Path("notify/q/best/hm")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void notifyQueryBest(String data, @Suspended AsyncResponse response) {

        // 数据权限begin
        String groups = getUserGroupsByFilter(request);
        List groupList = Collections.emptyList();
        boolean checkAuthor = true;

        if ("NOMAPPING".equals(groups)) {
            checkAuthor = false;
        }
        else if ("ALL".equals(groups)) {
            /**
             * nothing
             */
        }
        else {
            String[] gs = groups.split(",");
            groupList = new ArrayList<HashMap<String, String>>();

            for (String group : gs) {
                Map temp = new HashMap<>(1);
                temp.put("appgroup", group);
                groupList.add(temp);
            }
        }
        // 数据权限end

        if (checkAuthor) {
            // mongodb .为关键字，封装时替换为#交由后台处理
            data = data.replaceAll("\\.", "#");
            Map whereMap = new HashMap<>();
            Map regexMap = new HashMap<>();

            // 封装组合查询条件
            HashMap<String, String> jsonParam = JSONHelper.toObject(data, HashMap.class);
            if (jsonParam.containsKey("ip")) {
                regexMap.put("ip", jsonParam.get("ip"));
            }
            if (jsonParam.containsKey("host")) {
                regexMap.put("host", jsonParam.get("host"));
            }
            if (jsonParam.containsKey("description")) {
                regexMap.put("description", jsonParam.get("description"));
            }
            if (jsonParam.containsKey("abstract")) {
                regexMap.put("title", jsonParam.get("abstract"));
            }
            if (jsonParam.containsKey("event")) {
                whereMap.put("eventid", jsonParam.get("event"));
            }
            if (jsonParam.containsKey("startTime")) {
                Map startTime = new HashMap<>(1);
                startTime.put("latestrecord_ts", jsonParam.get("startTime"));
                whereMap.put(">", startTime);
            }
            if (jsonParam.containsKey("endTime")) {
                Map endTime = new HashMap<>(1);
                endTime.put("latestrecord_ts", jsonParam.get("endTime"));
                whereMap.put("<", endTime);
            }

            if (!groupList.isEmpty()) {
                whereMap.put("or", groupList);
            }

            whereMap.put("firstrecord", "true");
            whereMap.put("regex", regexMap);

            // 以最近触发预警记录的时间排序
            Map sortMap = new HashMap<>();
            sortMap.put("values", "latestrecord_ts");
            sortMap.put("sortorder", "-1");

            Map map = new HashMap();
            map.put("sort", sortMap);
            map.put("limit", mongodbQueryRangeMap.get("limit"));
            map.put("pageindex", String.valueOf(jsonParam.get("pageindex")));
            map.put("pagesize", String.valueOf(jsonParam.get("pagesize")));
            map.put("where", whereMap);

            // 封装http请求数据
            UAVHttpMessage message = new UAVHttpMessage();
            message.putRequest("mgo.sql", JSONHelper.toString(map));
            message.putRequest("datastore.name", "MT_Notify");
            message.putRequest("mgo.coll.name", "uav_notify");

            doHttpPost("uav.app.godeye.healthmanager.http.addr", "/hm/query", message,
                    new CommonCB(response, "notifyQueryBest"));
        }

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @POST
    @Path("notify/q/best/count/hm")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void notifyCountQueryBest(String data, @Suspended AsyncResponse response) {

        // 数据权限begin
        String groups = getUserGroupsByFilter(request);
        List groupList = Collections.emptyList();
        boolean checkAuthor = true;

        if ("NOMAPPING".equals(groups)) {
            checkAuthor = false;
        }
        else if ("ALL".equals(groups)) {
            /**
             * nothing
             */
        }
        else {
            String[] gs = groups.split(",");
            groupList = new ArrayList<HashMap<String, String>>();

            for (String group : gs) {
                Map temp = new HashMap<>(1);
                temp.put("appgroup", group);
                groupList.add(temp);
            }
        }
        // 数据权限end

        if (checkAuthor) {
            // mongodb .为关键字，封装时替换为#交由后台处理
            data = data.replaceAll("\\.", "#");
            Map whereMap = new HashMap<>();
            Map regexMap = new HashMap<>();

            // 封装组合查询条件
            HashMap<String, String> jsonParam = JSONHelper.toObject(data, HashMap.class);
            if (jsonParam.containsKey("ip")) {
                regexMap.put("ip", jsonParam.get("ip"));
            }
            if (jsonParam.containsKey("host")) {
                regexMap.put("host", jsonParam.get("host"));
            }
            if (jsonParam.containsKey("description")) {
                regexMap.put("description", jsonParam.get("description"));
            }
            if (jsonParam.containsKey("abstract")) {
                regexMap.put("title", jsonParam.get("abstract"));
            }
            if (jsonParam.containsKey("event")) {
                whereMap.put("eventid", jsonParam.get("event"));
            }
            if (jsonParam.containsKey("startTime")) {
                Map startTime = new HashMap<>(1);
                startTime.put("latestrecord_ts", jsonParam.get("startTime"));
                whereMap.put(">", startTime);
            }
            if (jsonParam.containsKey("endTime")) {
                Map endTime = new HashMap<>(1);
                endTime.put("latestrecord_ts", jsonParam.get("endTime"));
                whereMap.put("<", endTime);
            }

            if (!groupList.isEmpty()) {
                whereMap.put("or", groupList);
            }

            whereMap.put("firstrecord", "true");
            whereMap.put("regex", regexMap);

            Map map = new HashMap();
            map.put("limit", mongodbQueryRangeMap.get("limit"));
            map.put("where", whereMap);
            map.put("count", "true");

            // 封装http请求数据
            UAVHttpMessage message = new UAVHttpMessage();
            message.putRequest("mgo.sql", JSONHelper.toString(map));
            message.putRequest("datastore.name", "MT_Notify");
            message.putRequest("mgo.coll.name", "uav_notify");

            doHttpPost("uav.app.godeye.healthmanager.http.addr", "/hm/query", message,
                    new CommonCB(response, "notifyCountQueryBest"));
        }
        else {
            response.resume("{'rs':'[{\"count\":15}]'}");
        }

    }

    @SuppressWarnings({ "unchecked" })
    @POST
    @Path("notify/q/stgy/hm")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void notifyStrategyQuery(String data, @Suspended AsyncResponse response) throws Exception {

        Map<String, Object> params = JSONHelper.toObject(data, Map.class);
        int pagesize = (int) params.get("pagesize");
        int pageindex = (int) params.get("pageindex");
        Map<String, String> strategyMap = new HashMap<String, String>();
        strategyMap.put("keys", String.valueOf(params.get("inputValue")));
        // 封装http请求数据
        UAVHttpMessage message = new UAVHttpMessage();
        message.putRequest("body", JSONHelper.toString(strategyMap));
        message.setIntent("strategy.query");

        NotifyStrategyQuery callback = new NotifyStrategyQuery();
        callback.setResponse(response);
        callback.setPageindex(pageindex);
        callback.setPagesize(pagesize);

        doHttpPost("uav.app.godeye.notify.strategy.http.addr", "/rtntf/oper", message, callback);
    }

    @POST
    @Path("notify/get/stgy/hm")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void notifyStrategyGet(String data, @Suspended AsyncResponse response) throws Exception {

        Map<String, String> strategyMap = new HashMap<String, String>();
        strategyMap.put("keys", data);
        // 封装http请求数据
        UAVHttpMessage message = new UAVHttpMessage();
        message.putRequest("body", JSONHelper.toString(strategyMap));
        message.setIntent("strategy.query");

        NotifyStrategyGetCB callback = new NotifyStrategyGetCB();
        callback.setResponse(response);
        doHttpPost("uav.app.godeye.notify.strategy.http.addr", "/rtntf/oper", message, callback);
    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("notify/up/stgy/hm")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void notifyStrategyUpdate(String dataParam, @Suspended AsyncResponse response) throws Exception {

        // 添加操作字段
        Map<String, Object> stgyMap = JSONHelper.toObject(dataParam, Map.class);
        for (Entry<String, Object> objecMap : stgyMap.entrySet()) {
            String key = objecMap.getKey();
            Object value = stgyMap.get(key);
            if (null != value) {
                Map<String, Object> idMap = JSONHelper.toObject(String.valueOf(value), Map.class);
                idMap.put("uptime", new Date().getTime());

                stgyMap.put(key, idMap);
                break;
            }
        }

        final String stgyData = JSONHelper.toString(stgyMap);

        // 封装http请求数据
        UAVHttpMessage message = new UAVHttpMessage();
        message.putRequest("body", stgyData);
        message.setIntent("strategy.update");

        NotifyStrategyUpdateCB callback = new NotifyStrategyUpdateCB();
        callback.setResponse(response);
        callback.setStgyData(stgyData);

        doHttpPost("uav.app.godeye.notify.strategy.http.addr", "/rtntf/oper", message, callback);

    }

    @POST
    @Path("notify/del/stgy/hm")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void notifyStrategyRemove(String data, @Suspended AsyncResponse response) throws Exception {

        // 封装http请求数据
        UAVHttpMessage message = new UAVHttpMessage();
        message.putRequest("body", data);
        message.setIntent("strategy.remove");

        NotifyStrategyRemoveCB callback = new NotifyStrategyRemoveCB();
        callback.setResponse(response);
        callback.setData(data);

        doHttpPost("uav.app.godeye.notify.strategy.http.addr", "/rtntf/oper", message, callback);
    }

    // -----------------------------------------Node Ctrl-------------------------------------
    @POST
    @Path("node/ctrl")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void doNodeCtrlOperation(String data, @Suspended AsyncResponse response) {

        // user info
        HttpSession session = request.getSession();
        String uid = (String) session.getAttribute("apphub.gui.session.login.user.id");

        if (uid == null) {
            return;
        }

        UAVHttpMessage msg = new UAVHttpMessage(data);

        String nodeUrl = msg.getRequest("url");
        msg.getRequest().remove("url");

        long timeStamp = System.currentTimeMillis();

        /**
         * check node oper security
         */
        if (isCheckNodeOperSecurity == true) {
            Long ts = (Long) session.getAttribute("apphub.godeye.node.oper." + nodeUrl + ".timeout");

            if (ts != null && timeStamp - ts < 3000 && (!"loadnodepro".equalsIgnoreCase(msg.getIntent())
                    && !"chgsyspro".equalsIgnoreCase(msg.getIntent()))) {
                response.resume("{\"rs\":\"ERR\",\"msg\":\"该节点操作3秒内只能操作一次\"}");
                return;
            }

            if (!"loadnodepro".equalsIgnoreCase(msg.getIntent()) && !"chgsyspro".equalsIgnoreCase(msg.getIntent())) {
                session.setAttribute("apphub.godeye.node.oper." + nodeUrl + ".timeout", timeStamp);
            }
        }

        String ip = request.getRemoteAddr();
        String xip = request.getHeader("X-Forwarded-For");
        ip = getClientIP(ip, xip);

        msg.getRequest().put("uid", uid);
        msg.getRequest().put("uip", ip);
        msg.getRequest().put("ts", String.valueOf(timeStamp));

        String msgStr = JSONHelper.toString(msg);

        if (this.logger.isTraceEnable()) {
            this.logger.info(this, "Do RemoteNodeCtrlOpertaion: url=" + nodeUrl + ", msg=" + msgStr + ", timestamp="
                    + DateTimeHelper.toStandardDateFormat(timeStamp));
        }

        this.doHttpPost(nodeUrl, null, msg, new CommonCB(response, "doNodeCtrlOperation"));
    }

    // ----------------------------------------- database info -------------------------------------
    @POST
    @Path("monitor/q/dba")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void getMonitorViaDba(String data, @Suspended AsyncResponse response) {

        logger.info(this, data);
        doHttpPost("uav.app.godeye.database.http.addr", "/graph/history", data,
                new CommonCB(response, "getMonitorViaDba"));
    }

    // -----------------------------------------filter begin -------------------------------------

    /**
     * 应用服务监控：全网应用Profile SnapShot
     * 
     * @return
     */
    @GET
    @Path("filter/profile/q/cache")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void groupFilterLoadAppProfileList(@Suspended AsyncResponse response) {

        UAVHttpMessage message = new UAVHttpMessage();
        message.setIntent("profile");
        loadAppProfileListFromHttp(message, response);
    }

    @GET
    @Path("filter/group/query")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void groupFilterQuery(@Suspended AsyncResponse response) {

        Map<String, String> resultMap = cm.getHashAll(godeyeFilterGroupCacheRegion, godeyeFilterGroupCacheRegionKey);
        response.resume(JSONHelper.toString(resultMap));
    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("filter/group/save")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void groupFilterSave(String data, @Suspended AsyncResponse response) {

        Map<String, Object> param = JSONHelper.toObject(data, Map.class);
        String emailListStr = String.valueOf(param.get("emailListName"));
        String resultMsg = "{\"code\":\"00\",\"msg\":\"添加成功\"}";

        Map<String, String> esistsMap = cm.getHash(godeyeFilterGroupCacheRegion, godeyeFilterGroupCacheRegionKey,
                emailListStr);
        boolean noExists = true;
        if (esistsMap.get(emailListStr) != null) {
            Map<String, Object> old = JSONHelper.toObject(esistsMap.get(emailListStr), Map.class);
            if ("1".equals(String.valueOf(old.get("state")))) {
                resultMsg = "{\"code\":\"01\",\"msg\":\"邮箱组已经存在\"}";
                noExists = false;
            }
        }

        if (noExists) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timeStr = simpleDateFormat.format(new Date());
            HttpSession session = request.getSession(false);
            String user = "";
            if (session != null) {
                user = String.valueOf(session.getAttribute("apphub.gui.session.login.user.id"));
            }

            Map<String, Object> saveInfo = new HashMap<String, Object>();
            saveInfo.put("emailListName", emailListStr);
            saveInfo.put("groupList", param.get("groupList"));
            saveInfo.put("createTime", timeStr);
            saveInfo.put("operationTime", timeStr);
            saveInfo.put("operationUser", user);
            saveInfo.put("state", 1);

            cm.putHash(godeyeFilterGroupCacheRegion, godeyeFilterGroupCacheRegionKey, emailListStr,
                    JSONHelper.toString(saveInfo));
        }

        response.resume(resultMsg);
    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("filter/group/edit")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void groupFilterEdit(String data, @Suspended AsyncResponse response) {

        Map<String, Object> param = JSONHelper.toObject(data, Map.class);
        String emailListStr = String.valueOf(param.get("emailListName"));
        String resultMsg = "{\"code\":\"00\",\"msg\":\"修改成功\"}";

        Map<String, String> esistsMap = cm.getHash(godeyeFilterGroupCacheRegion, godeyeFilterGroupCacheRegionKey,
                emailListStr);
        if (esistsMap.get(emailListStr) != null) {
            Map<String, Object> old = JSONHelper.toObject(esistsMap.get(emailListStr), Map.class);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timeStr = simpleDateFormat.format(new Date());
            HttpSession session = request.getSession(false);
            String user = "";
            if (session != null) {
                user = String.valueOf(session.getAttribute("apphub.gui.session.login.user.id"));
            }

            Map<String, Object> saveInfo = new HashMap<String, Object>();
            saveInfo.put("emailListName", emailListStr);
            saveInfo.put("groupList", param.get("groupList"));
            saveInfo.put("createTime", old.get("createTime"));
            saveInfo.put("operationTime", timeStr);
            saveInfo.put("operationUser", user);
            saveInfo.put("state", old.get("state"));

            cm.delHash(godeyeFilterGroupCacheRegion, godeyeFilterGroupCacheRegionKey, emailListStr);
            cm.putHash(godeyeFilterGroupCacheRegion, godeyeFilterGroupCacheRegionKey, emailListStr,
                    JSONHelper.toString(saveInfo));
        }

        response.resume(resultMsg);
    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("filter/group/remove")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void groupFilterRemove(String data, @Suspended AsyncResponse response) {

        Map<String, Object> param = JSONHelper.toObject(data, Map.class);
        String emailListStr = String.valueOf(param.get("emailListName"));
        String resultMsg = "{\"code\":\"00\",\"msg\":\"删除成功\"}";

        Map<String, String> esistsMap = cm.getHash(godeyeFilterGroupCacheRegion, godeyeFilterGroupCacheRegionKey,
                emailListStr);
        if (esistsMap.get(emailListStr) != null) {
            cm.delHash(godeyeFilterGroupCacheRegion, godeyeFilterGroupCacheRegionKey, emailListStr);
        }
        else {
            resultMsg = "{\"code\":\"01\",\"msg\":\"邮箱组不存在\"}";
        }

        response.resume(resultMsg);
    }

    @GET
    @Path("filter/group/user/q")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void groupFilterUserQuery(String data, @Suspended AsyncResponse response) {

        response.resume(getUserGroupDetailsByFilter(request));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private String getUserGroupsByFilter(HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        // session中缓存数据
        String groupListSession = String
                .valueOf(session.getAttribute("apphub.gui.session.login.user.profile.groupList"));
        if (!"null".equals(groupListSession)) {
            return groupListSession;
        }

        String result = getUserGroupDetailsByFilter(request);

        if (!"ALL".equals(result) && !"NOMAPPING".equals(result)) {
            Set<String> set = new HashSet<String>();
            List<Map> list = JSONHelper.toObjectArray(result, Map.class);
            for (Map map : list) {
                String groupListStr = String.valueOf(map.get("groupList"));
                Map<String, Object> groupMap = JSONHelper.toObject(groupListStr, Map.class);
                set.addAll(groupMap.keySet());
            }
            result = set.toString().replaceAll(" ", "").replace("[", "").replace("]", "");
        }

        session.setAttribute("apphub.gui.session.login.user.profile.groupList", result);
        return result;
    }

    /**
     * 使用必须要有会话存在（用户必须已经登录）。否则会报错
     * 
     * @param request
     * @return ：[{},{}] ； 所有则返回: ALL,没有返回:NOMAPPING
     */
    @SuppressWarnings("unchecked")
    private String getUserGroupDetailsByFilter(HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        // 白名单用户
        String isWListUser = String.valueOf(session.getAttribute("apphub.gui.session.login.user.group"));
        if ("vipgroup".equals(isWListUser)) {
            return "ALL";
        }

        String[] emailList = String.valueOf(session.getAttribute("apphub.gui.session.login.user.emailList")).split(",");
        if ("UAV.ADMIN.EMAIL.LIST".equals(emailList[0])) {
            return "ALL";
        }

        // 用户邮箱组权限
        List<Object> list = new ArrayList<Object>();
        for (String e : emailList) {
            Map<String, String> esistsMap = cm.getHash(godeyeFilterGroupCacheRegion, godeyeFilterGroupCacheRegionKey,
                    e);
            if (esistsMap.get(e) != null) {
                Map<String, Object> emailMap = JSONHelper.toObject(esistsMap.get(e), Map.class);// 因为嵌套了一层，还需要再取一次
                if (!"1".equals(String.valueOf(emailMap.get("state")))) { // 状态为可用
                    continue;
                }
                list.add(emailMap);
            }
        }

        String result = "";
        if (list.isEmpty()) {
            result = "NOMAPPING";
        }
        else {
            result = JSONHelper.toString(list);
        }
        return result;
    }

    // -----------------------------------------filter end -------------------------------------

    /**
     * 升级中心显示可升级包
     * 
     * @param softwareId
     * @param resp
     * @return
     */
    @GET
    @Path("node/upgrade/list/{softwareId}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public String listUpgradePackage(@PathParam("softwareId") String softwareId, @Suspended AsyncResponse resp) {

        logger.info(this, "http get node/upgrade/list/" + softwareId);

        doHttpPost("uav.app.upgrade.server.http.addr", "/uav/upgrade?target=list&softwareId=" + softwareId, "",
                new CommonCB(resp, "listUpgradePackage"));
        return null;
    }

    // ---------------------------------------新日志服务接口 START-----------------------------------

    /**
     * searchNewLog
     * 
     * @param data
     * @param resp
     */
    @POST
    @Path("/newlog/q")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void searchNewLog(String data, @Suspended AsyncResponse resp) {

        UAVHttpMessage msg = new UAVHttpMessage(data);

        doHttpPost("uav.app.hm.newlog.http.addr", "/newlog/q", msg, new CommonCB(resp, "searchNewLog"));
    }
    // ---------------------------------------新日志服务接口 END-------------------------------------

    // ---------------------------------------反馈建议 START-----------------------------------
    /**
     * searchNewLog
     * 
     * @param data
     * @param resp
     */
    @SuppressWarnings("unchecked")
    @POST
    @Path("/feedback/save")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void saveFeedback(String reqData, @Suspended AsyncResponse resp) {

        String userId = "";
        if (null != request.getSession(false)) {
            Object obj = request.getSession(false).getAttribute("apphub.gui.session.login.user.id");
            userId = null == obj ? "" : String.valueOf(obj);
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String time = simpleDateFormat.format(new Date());

        Map<String, String> httpData = JSONHelper.toObject(reqData, Map.class);
        httpData.put("time", time);
        httpData.put("uid", userId);

        IdWorker idWorker = CommonHelper.getUUIDWorker(new Random().nextInt(30) + 1, 1);
        String uuid = String.valueOf(idWorker.nextId());
        httpData.put("key", uuid);

        /**
         * 打包请求数据
         */
        Map<String, Object> mapParam = new HashMap<String, Object>();
        mapParam.put("type", "create");
        mapParam.put("data", httpData);

        UAVHttpMessage request = new UAVHttpMessage();
        request.putRequest("mrd.data", JSONHelper.toString(mapParam));
        request.putRequest("datastore.name", "AppHub.feedback");
        request.putRequest("mgo.coll.name", "uav_feedback");

        doHttpPost("uav.app.manage.apphubmanager.http.addr", "/ah/feedback", request,
                new CommonCB(resp, "saveFeedback"));
    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("/feedback/query")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void queryFeedback(String reqData, @Suspended AsyncResponse resp) {

        Map<String, String> reqInfo = JSONHelper.toObject(reqData, Map.class);

        Map<String, Object> where = new HashMap<String, Object>();
        String userGroup = String
                .valueOf(request.getSession(false).getAttribute("apphub.gui.session.login.user.group"));
        if (reqInfo.containsKey("checkuser") || (!"vipgroup".equals(userGroup) && !"uav_admin".equals(userGroup))) {
            /**
             * 不是admin用户则添加过滤
             */
            Object obj = request.getSession(false).getAttribute("apphub.gui.session.login.user.id");
            String userId = null == obj ? "" : String.valueOf(obj);
            where.put("uid", userId);
        }

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("where", where);
        data.put("pageindex", reqInfo.get("pageindex"));
        data.put("pagesize", reqInfo.get("pagesize"));

        HashMap<String, Object> sort = new HashMap<String, Object>();
        sort.put("values", "time");
        sort.put("sortorder", "-1");
        data.put("sort", sort);

        Map<String, Object> mapParam = new HashMap<String, Object>();
        mapParam.put("type", "query");
        mapParam.put("data", data);

        /**
         * 打包请求数据
         */
        UAVHttpMessage request = new UAVHttpMessage();
        request.putRequest("mrd.data", JSONHelper.toString(mapParam));
        request.putRequest("datastore.name", "AppHub.feedback");
        request.putRequest("mgo.coll.name", "uav_feedback");

        doHttpPost("uav.app.manage.apphubmanager.http.addr", "/ah/feedback", request,
                new CommonCB(resp, "queryFeedback"));
    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("/feedback/query/count")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void queryFeedbackCount(String reqData, @Suspended AsyncResponse resp) {

        Map<String, String> reqInfo = JSONHelper.toObject(reqData, Map.class);

        Map<String, Object> where = new HashMap<String, Object>();

        String userGroup = String
                .valueOf(request.getSession(false).getAttribute("apphub.gui.session.login.user.group"));
        if (reqInfo.containsKey("checkuser") || (!"vipgroup".equals(userGroup) && !"uav_admin".equals(userGroup))) {
            /**
             * 不是admin用户则添加过滤
             */
            Object obj = request.getSession(false).getAttribute("apphub.gui.session.login.user.id");
            String userId = null == obj ? "" : String.valueOf(obj);
            where.put("uid", userId);
        }

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("where", where);
        data.put("count", "true");

        Map<String, Object> mapParam = new HashMap<String, Object>();
        mapParam.put("type", "query");
        mapParam.put("data", data);

        /**
         * 打包请求数据
         */
        UAVHttpMessage request = new UAVHttpMessage();
        request.putRequest("mrd.data", JSONHelper.toString(mapParam));
        request.putRequest("datastore.name", "AppHub.feedback");
        request.putRequest("mgo.coll.name", "uav_feedback");
        
        doHttpPost("uav.app.manage.apphubmanager.http.addr", "/ah/feedback", request,
                new CommonCB(resp, "queryFeedbackCount"));

    }
    // ---------------------------------------反馈建议 END-----------------------------------

}
