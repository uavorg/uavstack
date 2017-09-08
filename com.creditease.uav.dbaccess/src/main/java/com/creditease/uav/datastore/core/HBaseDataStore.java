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

package com.creditease.uav.datastore.core;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.BufferedMutator;
import org.apache.hadoop.hbase.client.BufferedMutatorParams;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.util.Bytes;

import com.creditease.uav.datastore.api.DataStoreAdapter;
import com.creditease.uav.datastore.api.DataStoreConnection;
import com.creditease.uav.datastore.api.DataStoreMsg;
import com.creditease.uav.datastore.api.DataStoreProtocol;
// import com.creditease.uav.datastore.hbase.HBaseDataOps;
import com.creditease.uav.datastore.source.AbstractDataSource;
import com.creditease.uav.datastore.source.HBaseDataSource;
import com.google.common.collect.Lists;

public class HBaseDataStore extends AbstractDataStore<Connection> {

    public HBaseDataStore(DataStoreConnection connectObj, DataStoreAdapter adaptor, String feature) {
        super(connectObj, adaptor, feature);
    }

    @Override
    protected AbstractDataSource<Connection> getDataSource(DataStoreConnection obj) {

        return new HBaseDataSource(obj);
    }

    /**
     * msg 包括：
     * 
     * @param tablename
     * @param entity:
     *            rowkey->cf:column->value 其中增加对_timestamp字段的处理
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected boolean insert(DataStoreMsg msg) {

        // 根据TABLE名进行合法验证
        Map[] maps = (Map[]) adaptor.prepareInsertObj(msg, datasource.getDataStoreConnection());
        Map<byte[], Map> entity = maps[0];
        Map<byte[], Long> entityStamp = maps[1];
        String tableName = (String) msg.get(DataStoreProtocol.HBASE_TABLE_NAME);
        // add write buffer
        BufferedMutatorParams params = new BufferedMutatorParams(TableName.valueOf(tableName));

        params.writeBufferSize(1024 * 1024 * 2);
        try (BufferedMutator table = datasource.getSourceConnect().getBufferedMutator(params);) {

            // 取得所有cf
            List<Put> puts = Lists.newArrayList();
            Put put = null;
            for (byte[] rowkey : entity.keySet()) {
                // 定制时间戳
                put = entityStamp.containsKey(rowkey) ? new Put(rowkey, entityStamp.get(rowkey)) : new Put(rowkey);

                // 取得column和value
                for (Object entry : entity.get(rowkey).keySet()) {

                    String[] column = ((String) entry).split(":");
                    put.addColumn(Bytes.toBytes(column[0]), Bytes.toBytes(column[1]),
                            Bytes.toBytes((String) entity.get(rowkey).get(entry)));
                }
                puts.add(put);
            }
            // 批量提交
            Object[] results = new Object[puts.size()];
            // table.batch(puts, results);
            table.mutate(puts);
            // flush
            table.flush();
            // 根据插入信息操作并返回结果
            return adaptor.handleInsertResult(results, msg, datasource.getDataStoreConnection());
        }
        catch (IOException e) {
            log.err(this, "INSERT HBASE TABLE[" + tableName + "] FAIL:" + msg.toJSONString(), e);
            return false;
        }

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected List query(DataStoreMsg msg) {

        log.info(this, "DataStore Query Log data start");
        msg = (DataStoreMsg) adaptor.prepareQueryObj(msg, datasource.getDataStoreConnection());
        // 根据TABLE名取得table实例
        String tableName = (String) msg.get(DataStoreProtocol.HBASE_TABLE_NAME);
        // 根据family名取得scaner实例
        String cfName = (String) msg.get(DataStoreProtocol.HBASE_FAMILY_NAME);
        Scan scan = null;
        List<byte[]> kv = null;
        try (Table table = datasource.getSourceConnect().getTable(TableName.valueOf(tableName));) {
            scan = new Scan();
            DataStoreConnection con = datasource.getDataStoreConnection();
            scan.setCaching(Integer.parseInt((String) con.getContext(DataStoreProtocol.HBASE_QUERY_CACHING)));
            scan.setMaxResultSize(Long.parseLong((String) con.getContext(DataStoreProtocol.HBASE_QUERY_MAXRESULTSIZE)));
            scan.setReversed((boolean) msg.get(DataStoreProtocol.HBASE_QUERY_REVERSE));
            scan.addFamily(cfName.getBytes("UTF-8"));
            // 根据查询信息构建实例,目前支持rowkey过滤
            FilterList flist = new FilterList(FilterList.Operator.MUST_PASS_ALL);
            if (msg.containsKey(DataStoreProtocol.HBASE_QUERY_STARTROW)) {
                scan.setStartRow((byte[]) msg.get(DataStoreProtocol.HBASE_QUERY_STARTROW));
            }
            if (msg.containsKey(DataStoreProtocol.HBASE_QUERY_ENDROW)) {
                scan.setStopRow((byte[]) msg.get(DataStoreProtocol.HBASE_QUERY_ENDROW));
            }
            if (msg.containsKey(DataStoreProtocol.HBASE_QUERY_ROW_KEYVALUE)) {
                kv = (List<byte[]>) msg.get(DataStoreProtocol.HBASE_QUERY_ROW_KEYVALUE);
                for (byte[] b : kv) {
                    flist.addFilter(new RowFilter(CompareOp.EQUAL, new SubstringComparator(new String(b))));
                }

            }
            flist.addFilter(new PageFilter((long) msg.get(DataStoreProtocol.HBASE_QUERY_PAGESIZE)));
            scan.setFilter(flist);
            log.info(this, "DataStore Query Log data: getFilter String:" + scan.getFilter().toString());
            try (ResultScanner result = table.getScanner(scan);) {
                List<NavigableMap<byte[], byte[]>> resultList = Lists.newArrayList();
                for (Result r : result) {
                    NavigableMap<byte[], byte[]> map = r.getFamilyMap(cfName.getBytes());
                    map.put("_timestamp".getBytes(), String.valueOf(r.rawCells()[0].getTimestamp()).getBytes());
                    resultList.add(map);

                }
                return adaptor.handleQueryResult(resultList, msg, datasource.getDataStoreConnection());
            }

        }
        catch (IOException e) {
            log.err(this, "QUERY HBASE TABLE[" + tableName + "] FAMILY[" + cfName + "] FAIL:" + msg.toJSONString(), e);
            return null;
        }
    }

    @Override
    protected boolean update(DataStoreMsg msg) {

        return false;
    }

}
