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

package com.creditease.uav.datastore.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.creditease.agent.helpers.StringHelper;
import com.creditease.uav.datastore.api.DataStoreFactory.DataStoreType;

/**
 * 传递连接所需的信息
 * 
 * @author peihua
 */
public class DataStoreConnection {

    private String username;

    private String password;

    private String dbName;

    private String dbPower = "admin";

    DataStoreType sourceType;

    private List<String> addressList = new ArrayList<String>();

    private int retryTimes = 3;

    private int blackExpireTime = 30000;

    /**
     * 不同类型的存储连接，可能需要额外的配置项，通过context进行传递
     */
    private Map<String, Object> context = new LinkedHashMap<String, Object>();

    public DataStoreConnection(String username, String password, String dbName, List<String> serverlist,
            DataStoreType sourceType) {
        this.addressList.addAll(serverlist);
        this.username = username;
        this.password = password;
        this.dbName = dbName;
        this.sourceType = sourceType;
    }

    public DataStoreConnection(String username, String password, String dbName, String dbPower, List<String> serverlist,
            DataStoreType sourceType) {
        this.addressList.addAll(serverlist);
        this.username = username;
        this.password = password;
        this.dbName = dbName;
        this.sourceType = sourceType;
        this.dbPower = null == dbPower ? this.dbPower : (dbPower.length() > 0 ? dbPower : this.dbPower);
    }

    public void putContext(String key, Object obj) {

        if (StringHelper.isEmpty(key) || null == obj) {
            return;
        }

        context.put(key, obj);
    }

    public void putContextAll(Map<String, Object> ctx) {

        context.putAll(ctx);
    }

    public List<String> getAddressList() {

        return addressList;
    }

    public Object getContext(String key) {

        return context.get(key);
    }

    public DataStoreType getSourceType() {

        return sourceType;
    }

    public String getUsername() {

        return username;
    }

    public String getPassword() {

        return password;
    }

    public String getDbName() {

        return dbName;
    }

    public String getDbPower() {

        return dbPower;
    }

    public void setDbPower(String dbPower) {

        this.dbPower = dbPower;
    }

    public int getRetryTimes() {

        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {

        this.retryTimes = retryTimes;
    }

    public int getBlackExpireTime() {

        return blackExpireTime;
    }

    public void setBlackExpireTime(int blackExpireTime) {

        this.blackExpireTime = blackExpireTime;
    }

    /**
     * example: 127.0.0.1:9999,xxx.creditease.cn:2181
     * 
     * @param separator
     * @return
     */
    public String toString(String separator) {

        separator = (separator == null) ? "," : separator;
        StringBuffer servers = new StringBuffer();
        for (String address : addressList) {
            servers.append(address).append(separator);
        }
        if (addressList.size() > 0) {
            return servers.substring(0, servers.length() - 1);
        }
        else {
            return servers.toString();
        }
    }
}
