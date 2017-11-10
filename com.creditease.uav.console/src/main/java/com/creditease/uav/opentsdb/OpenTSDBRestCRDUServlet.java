package com.creditease.uav.opentsdb;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.cache.api.CacheManagerFactory;
import com.creditease.uav.httpasync.HttpAsyncClient;
import com.creditease.uav.httpasync.HttpAsyncClientFactory;

public class OpenTSDBRestCRDUServlet extends HttpServlet {

    private static Map<String, String> info = null;

    private static HttpAsyncClient httpAsyncClient = null;

    private static String PATH = null;

    private static final String Redis_NAME = "opentsdb_json";

    private static CacheManager cm = null;

    private static final String opentsdbCacheRegion = "apphub.opentsdb.cache";

    private static final String opentsdbCacheRegionKey = "apphub.opentsdb.cache.key";

    @SuppressWarnings("unchecked")
    @Override
    public void init() {

        if (null == info) {
            String esInfo = this.getInitParameter("db.info");
            info = JSONHelper.toObject(esInfo, Map.class);
        }

        if (null == httpAsyncClient) {
            Map<String, Integer> httpParamsMap = JSONHelper
                    .toObject(getServletContext().getInitParameter("uav.app.opentsdb.http.client.params"), Map.class); // 改成db的
            httpAsyncClient = HttpAsyncClientFactory.build(httpParamsMap.get("max.con"),
                    httpParamsMap.get("max.tot.con"), httpParamsMap.get("sock.time.out"),
                    httpParamsMap.get("con.time.out"), httpParamsMap.get("req.time.out"));
        }

        // cache manager
        String redisAddrStr = getServletContext().getInitParameter("uav.app.godeye.redis.store.addr");
        Map<String, Object> redisParamsMap = JSONHelper
                .toObject(getServletContext().getInitParameter("uav.app.godeye.redis.store.params"), Map.class);
        cm = CacheManagerFactory.build(redisAddrStr, Integer.valueOf(String.valueOf(redisParamsMap.get("min"))),
                Integer.valueOf(String.valueOf(redisParamsMap.get("max"))),
                Integer.valueOf(String.valueOf(redisParamsMap.get("queue"))),
                String.valueOf(redisParamsMap.get("pwd")));

    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        if (PATH == null) {
            PATH = req.getServletContext().getRealPath("") + "/uavapp_baseclassmgt/dbhead/json.txt";
        }
        // 获取请求资源
        String proName = req.getServletContext().getContextPath();
        String requestSource = req.getRequestURI();
        requestSource = requestSource.substring(requestSource.indexOf(proName) + proName.length());

        if (requestSource.startsWith("/db")) {
            requestSource = requestSource.substring(3);
        }
        // 加载列表
        if ("/loadDbs".equals(requestSource)) {
            String str = loadDbs();
            try {
                respWriter(resp, str);
            }
            catch (IOException e) {
                ISystemLogger logger = SystemLogger.getLogger(OpenTSDBRestCRDUServlet.class);
                logger.err(this, "OpenTSDBRestCRDUServlet Exception: " + e.getMessage(), e);
            }
            return;
        }

        // 删除列
        if ("/delDb".equals(requestSource)) {
            String data = loadData(req);
            data = data.substring(1, data.length() - 1);
            data = delDb(data);
            try {
                respWriter(resp, data);
            }
            catch (IOException e) {
                ISystemLogger logger = SystemLogger.getLogger(OpenTSDBRestCRDUServlet.class);
                logger.err(this, "OpenTSDBRestCRDUServlet Exception: " + e.getMessage(), e);
            }
            return;
        }

        // 查询ByName
        if ("/queryDb".equals(requestSource)) {
            String data = loadData(req);
            String name = data.substring(data.indexOf(":") + 1, data.length() - 1);
            String res = queryDbById(name);
            res = "[" + res + "]";
            try {
                respWriter(resp, res);
            }
            catch (IOException e) {
                ISystemLogger logger = SystemLogger.getLogger(OpenTSDBRestCRDUServlet.class);
                logger.err(this, "OpenTSDBRestCRDUServlet Exception: " + e.getMessage(), e);
            }
            return;

        }

        // 更新DB
        if ("/modifyDb".equals(requestSource)) {
            String data = loadData(req);
            String res = updateDb(data);
            try {
                respWriter(resp, res);
            }
            catch (IOException e) {
                ISystemLogger logger = SystemLogger.getLogger(OpenTSDBRestCRDUServlet.class);
                logger.err(this, "OpenTSDBRestCRDUServlet Exception: " + e.getMessage(), e);
            }
            return;
        }

        // 添加DB
        if ("/addDb".equals(requestSource)) {
            String data = loadData(req);
            String res = addDb(data);
            try {
                respWriter(resp, res);
            }
            catch (IOException e) {
                ISystemLogger logger = SystemLogger.getLogger(OpenTSDBRestCRDUServlet.class);
                logger.err(this, "OpenTSDBRestCRDUServlet Exception: " + e.getMessage(), e);
            }
            return;
        }
        // 搜索功能
        if ("/searchDbs".equals(requestSource)) {
            String data = loadData(req);
            Map<String, String> map = JSONHelper.toObject(data, HashMap.class);
            String url = map.get("url");
            String res = queryDbByUrl(url);
            try {
                respWriter(resp, res);
            }
            catch (IOException e) {
                ISystemLogger logger = SystemLogger.getLogger(OpenTSDBRestCRDUServlet.class);
                logger.err(this, "OpenTSDBRestCRDUServlet Exception: " + e.getMessage(), e);
            }
            return;
        }

    }

    private static void respWriter(HttpServletResponse resp, String res) throws IOException {

        resp.setCharacterEncoding("utf-8");
        resp.getWriter().print(res);
        resp.getWriter().flush();
        resp.getWriter().close();
    }

    private static String loadData(HttpServletRequest req) throws IOException {

        StringBuffer json = new StringBuffer();
        String line = null;
        BufferedReader reader = req.getReader();
        while ((line = reader.readLine()) != null) {
            json.append(line);
        }
        return json.toString();
    }

    private String loadDbs() throws IOException {

        String str = loadJson();
        return str;
    }

    private String queryDbById(String name) throws IOException {

        String res = loadDbs();

        List<Map> list = JSONHelper.toObjectArray(res, Map.class);
        if (list == null || list.size() == 0) {
            return null;
        }
        if (name.length() > 1) {
            name = name.substring(1, name.length() - 1);
        }
        for (Map m : list) {
            String map_name = (String) m.get("name");
            if (name.equals(map_name)) {
                return JSONHelper.toString(m);
            }
        }

        return null;
    }

    private String queryDbByUrl(String url) throws IOException {

        String res = loadDbs();

        List<Map> list = JSONHelper.toObjectArray(res, Map.class);
        if (list == null || list.size() == 0) {
            return null;
        }
        List<Map> ret = new ArrayList<Map>();
        for (Map m : list) {
            String map_url = (String) m.get("url");
            if (map_url.contains(url)) {
                ret.add(m);
            }
        }
        return JSONHelper.toString(ret);
    }

    private String delDb(String name) throws IOException {

        String res = loadDbs();
        if (name.length() > 1) {
            name = name.substring(name.indexOf(":") + 2, name.length() - 1);
        }
        List<Map> list = JSONHelper.toObjectArray(res, Map.class);

        for (Map m : list) {
            String map_name = (String) m.get("name");
            if (name.equals(map_name)) {
                list.remove(m);
                // saveJSON(JSONHelper.toString(list));
                saveJSONRedis(JSONHelper.toString(list));
                return loadDbs();
            }
        }

        return null;
    }

    private String addDb(String str) throws IOException {

        String name = str.substring(str.indexOf(":") + 1, str.indexOf(","));

        if (name == null || name.equals("\"\"")) {
            return null;
        }
        String res = queryDbById(name);
        if (res != null) {
            // return loadDbs();
            return "multi_name";
        }

        res = loadJson();

        List<Map> list = JSONHelper.toObjectArray(res, Map.class);
        Map m = JSONHelper.toObject(str, Map.class);
        list.add(m);
        saveJSONRedis(JSONHelper.toString(list));
        return loadDbs();
    }

    private String updateDb(String str) throws IOException {

        String name = str.substring(str.indexOf(":") + 1, str.indexOf(","));

        if (name == null || name.equals("\"\"")) {
            return null;
        }
        String res = loadDbs();
        if (res == null) {
            return null;
        }
        if (name.length() > 1) {
            name = name.substring(1, name.length() - 1);
        }

        List<Map> list = JSONHelper.toObjectArray(res, Map.class);
        List<Map> ret = new ArrayList<Map>();
        for (Map m : list) {
            String map_name = (String) m.get("name");
            if (name.equals(map_name)) {
                ret.add(JSONHelper.toObject(str, Map.class));
            }
            else {
                ret.add(m);
            }
        }
        saveJSONRedis(JSONHelper.toString(ret));
        return loadDbs();
    }

    private void saveJSONRedis(String str) {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(this.Redis_NAME, str);
        cm.putHash(opentsdbCacheRegion, opentsdbCacheRegionKey, this.Redis_NAME, JSONHelper.toString(map));
    }

    private String queryJSONRedis() {

        Map<String, String> dbMap = cm.getHash(opentsdbCacheRegion, opentsdbCacheRegionKey, this.Redis_NAME);
        Map<String, String> old = JSONHelper.toObject(dbMap.get(this.Redis_NAME), Map.class);
        if (null == old) {
            return null;
        }
        return old.get(this.Redis_NAME);
    }

    private String loadJson() throws IOException {

        String str;
        str = queryJSONRedis();
        if (null == str || str.length() < 2) {
            str = "[]";
        }
        return str;
    }

}
