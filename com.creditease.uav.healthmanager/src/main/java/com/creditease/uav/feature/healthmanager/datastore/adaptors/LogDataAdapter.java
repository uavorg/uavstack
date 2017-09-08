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

package com.creditease.uav.feature.healthmanager.datastore.adaptors;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;

import org.apache.commons.codec.digest.DigestUtils;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.uav.datastore.api.DataStoreAdapter;
import com.creditease.uav.datastore.api.DataStoreConnection;
import com.creditease.uav.datastore.api.DataStoreMsg;
import com.creditease.uav.datastore.api.DataStoreProtocol;
import com.creditease.uav.feature.healthmanager.HealthManagerConstants;

public class LogDataAdapter extends DataStoreAdapter {

    /**
     * [ { time:1464248773620, host:"09-201211070016", ip:"127.0.0.1",
     * svrid:"F:/testenv/apache-tomcat-7.0.65---F:/testenv/apache-tomcat-7.0.65", tag:"L", frames:{ "ccsp":[ {
     * "MEId":"log", "Instances":[ { "id":"F:/testenv/apache-tomcat-7.0.65/logs/ccsp.log", "values":{ "content":[ {
     * "content":"2016-localhost-startStop-1INFORootWebApplicationContext:initializationstarted",
     * "_timestamp":"1212345678129", "_lnum" : "123" } ] } }, { "id":"/ccsp/log_error.log", "values":{ "content":[
     * {"content":"xxxxxxxxxx", "_timestamp":"1212345678129", "_lnum" : "123"} ] } } ] } ] } } ]
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Object prepareInsertObj(DataStoreMsg msg, DataStoreConnection connection) {

        String data = (String) msg.get(MonitorDataFrame.MessageType.Log.toString());
        List<String> array = JSONHelper.toObjectArray(data, String.class);
        Map<byte[], Map> ops = new HashMap<byte[], Map>();
        Map<byte[], Long> ops_timestamp = new HashMap<byte[], Long>();

        for (String mdfStr : array) {
            long ip = 0;
            byte[] svrid = null;
            @SuppressWarnings("unused")
            byte[] timestamp = null;
            // 反序列化为MonitorDataFrame
            MonitorDataFrame mdf = new MonitorDataFrame(mdfStr);

            ip = createipRowkey(mdf.getIP());
            svrid = createRowkey(mdf.getServerId());
            timestamp = toBytes(mdf.getTimeFlag());
            Map<String, List<Map>> frames = mdf.getDatas();
            // frames = JSONHelper.toObject(object.get("frames").toString(), Map.class);

            for (String appid : frames.keySet()) {
                // appid = ((String) entry).split(",", 2);
                List<Map> applogs = frames.get(appid);
                for (Map applog : applogs) {
                    List<Map> instances = (List<Map>) applog.get("Instances");
                    for (Map instance : instances) {
                        // get instance id
                        String logid = (String) instance.get("id");
                        // get instance fields
                        Map<String, Object> fields = (Map<String, Object>) instance.get("values");
                        List<Map> logs = (List<Map>) fields.get("content");

                        Map cklogs = null;
                        // --------------------------------------------------
                        for (int j = 0; j < logs.size(); j++) {
                            Map log = logs.get(j);
                            cklogs = new HashMap();
                            // byte[] rowkey = merge(createRowkey(appid), timestamp, toBytes(ip),
                            // svrid,createRowkey(logid));
                            long stamp = 0;
                            long num = 0;
                            for (Object lentry : log.keySet()) {
                                // 当出现_timestamp时，需要记录为定制时间戳
                                if ("_timestamp".equals(lentry) && StringHelper.isNumeric((String) log.get(lentry))) {
                                    stamp = Long.parseLong((String) log.get(lentry));
                                    continue;
                                }
                                // 当出现_lnum时,记录行号并加入rowkey
                                if ("_lnum".equals(lentry) && StringHelper.isNumeric((String) log.get(lentry))) {
                                    num = Long.parseLong((String) log.get(lentry));
                                    // rowkey = merge(rowkey, toBytes(Long.parseLong(num)));
                                }
                                // cf:k字段写入实际值
                                cklogs.put("cf:" + lentry, log.get(lentry));
                            }

                            byte[] rowkey = merge(createRowkey(appid), toBytes(stamp), toBytes(ip), svrid,
                                    createRowkey(logid), toBytes(num));
                            ops.put(rowkey, cklogs);
                            if (stamp != 0) {
                                ops_timestamp.put(rowkey, stamp);
                            }
                        }
                        // ------------------------------------------------------------
                    }
                }

            }
        }
        return new Map[] { ops, ops_timestamp };
    }

    private byte[] merge(byte[]... b) {

        int length = 0;
        for (int i = 0; i < b.length; i++) {
            length += b[i].length;
        }
        ByteBuffer merge = ByteBuffer.allocate(length);
        for (int i = 0; i < b.length; i++) {
            merge.put(b[i]);
        }
        return merge.array();

    }

    private long createipRowkey(String ipaddr) {

        if (ipaddr == null)
            return 0;
        String[] ipAddressInArray = ipaddr.split("\\.");
        long iprow = 0;
        for (int i = 0; i < ipAddressInArray.length; i++) {
            int power = 3 - i;
            int ip = Integer.parseInt(ipAddressInArray[i]);
            iprow += ip * Math.pow(256, power);
        }
        return iprow;
    }

    private byte[] createRowkey(String id) {

        return DigestUtils.md2Hex(id).getBytes();

    }

    private byte[] toBytes(long val) {

        byte[] b = new byte[8];
        for (int i = 7; i > 0; i--) {
            b[i] = (byte) val;
            val >>>= 8;
        }
        b[0] = (byte) val;
        return b;
    }

    @Override
    public boolean handleInsertResult(Object result, DataStoreMsg msg, DataStoreConnection connection) {

        Object[] relist = (Object[]) result;
        StringBuffer recode = new StringBuffer("insert log return code -[");
        for (int i = 0; i < relist.length; i++) {
            recode.append(relist[i]).append(", ");
        }
        if (log.isDebugEnable()) {
            log.debug(this, recode.append("]").toString());
        }
        // TODO 需要判断是部分成功，还是全部成功不同的处理
        return true;
    }

    /**
     * { "starttime": 145629382438, "endtime": 145629382438, //optional "ip": "127.0.0.1", "svrid":
     * "D:/UAV/apache-tomcat-6.0.41::D:/eclipseProject/.metadata/.plugins/org.eclipse.wst.server.core/tmp0", "appid":
     * "sms" "logid":"logs/out.log" }
     * 
     * 
     * TODO 需要测试一下到底是怎么玩的。 The startrow is inclusive and the stoprow is exclusive. Given a table with rows a, b, c, d,
     * e, f, and startrow of c and stoprow of f, rows c-e are returned. If you omit startrow, the first row of the table
     * is the startrow. If you omit the stoprow, all results after startrow (including startrow) are returned. If
     * startrow is lexicographically after stoprow, and you set Scan setReversed(boolean reversed) to true, the results
     * are returned in reverse order. Given the same table above, with rows a-f, if you specify c as the stoprow and f
     * as the startrow, rows f, e, and d are returned.
     */
    @Override
    public Object prepareQueryObj(DataStoreMsg msg, DataStoreConnection connection) {

        msg.put(DataStoreProtocol.HBASE_TABLE_NAME, HealthManagerConstants.HBASE_TABLE_LOGDATA);
        msg.put(DataStoreProtocol.HBASE_FAMILY_NAME, HealthManagerConstants.HBASE_DEFAULT_FAMILY);
        String data = (String) msg.get(DataStoreProtocol.HBASE_QUERY_JSON_KEY);
        Map<?, ?> dataObject = JSONHelper.toObject(data, Map.class);
        // 组织rowkey:以 IP-Timestamp-Svrid-Appid, 即当IP未知时为全表scan
        String ip = (String) dataObject.get("ip");
        String svrid = (String) dataObject.get("svrid");
        String appid = (String) dataObject.get("appid");
        String logid = (String) dataObject.get("logid");
        long startime = DataConvertHelper.toLong(dataObject.get("starttime"), 0);
        long endtime = DataConvertHelper.toLong(dataObject.get("endtime"), 0);
        long pagesize = DataConvertHelper.toLong(String.valueOf(dataObject.get("psize")),
                (long) connection.getContext(DataStoreProtocol.HBASE_QUERY_PAGESIZE));
        boolean reversed = DataConvertHelper.toBoolean(dataObject.get("reversed"),
                (boolean) connection.getContext(DataStoreProtocol.HBASE_QUERY_REVERSE));
        byte[] startRowkey = null;
        byte[] endRowkey = null;
        // 使用appid_timestamp为默认rowkey前缀
        if (null != appid && !"".equals(appid)) {
            byte[] appidRow = createRowkey(appid);
            if (reversed) {
                startRowkey = (startime != 0) ? merge(appidRow, toBytes(startime))
                        : merge(appidRow, toBytes(System.currentTimeMillis()));
                endRowkey = (endtime != 0) ? merge(appidRow, toBytes(endtime + 1)) : appidRow;
            }
            else {
                startRowkey = (startime != 0) ? merge(appidRow, toBytes(startime)) : appidRow;
                endRowkey = (endtime != 0) ? merge(appidRow, toBytes(endtime + 1))
                        : merge(appidRow, toBytes(System.currentTimeMillis()));
            }
            msg.put(DataStoreProtocol.HBASE_QUERY_STARTROW, startRowkey);
            msg.put(DataStoreProtocol.HBASE_QUERY_ENDROW, endRowkey);
            msg.put(DataStoreProtocol.HBASE_QUERY_PAGESIZE, pagesize);
            msg.put(DataStoreProtocol.HBASE_QUERY_REVERSE, reversed);
        }

        List<byte[]> rowkv = new ArrayList<byte[]>();
        if (null != svrid && !svrid.isEmpty()) {
            rowkv.add(createRowkey(svrid));
        }
        if (null != ip && !ip.isEmpty()) {
            rowkv.add(toBytes(createipRowkey(ip)));
        }
        if (null != logid && !logid.isEmpty()) {
            rowkv.add(createRowkey(logid));
        }
        if (rowkv.size() > 0) {
            msg.put(DataStoreProtocol.HBASE_QUERY_ROW_KEYVALUE, rowkv);
        }
        return msg;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Map<String, String>> handleQueryResult(Object result, DataStoreMsg msg,
            DataStoreConnection connection) {

        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        Map<String, String> map = null;
        for (NavigableMap<byte[], byte[]> entrys : (List<NavigableMap<byte[], byte[]>>) result) {
            map = new HashMap<String, String>();
            for (Entry<byte[], byte[]> entry : entrys.entrySet()) {
                map.put(new String(entry.getKey()), new String(entry.getValue()));
            }
            list.add(map);
        }
        return list;
    }

    @Override
    public Object prepareUpdateObj(DataStoreMsg msg, DataStoreConnection connection) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean handleUpdateResult(Object result, DataStoreMsg msg, DataStoreConnection connection) {

        // TODO Auto-generated method stub
        return false;
    }

}
