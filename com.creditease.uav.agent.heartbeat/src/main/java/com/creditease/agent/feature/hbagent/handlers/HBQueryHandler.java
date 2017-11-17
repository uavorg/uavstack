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

package com.creditease.agent.feature.hbagent.handlers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.creditease.agent.heartbeat.api.HeartBeatProtocol;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.spi.AbstractHttpHandler;
import com.creditease.uav.cache.api.CacheManager;

/**
 * 
 * HBQueryHandler description: 提供NodeInfo的查询能力
 *
 */
public class HBQueryHandler extends AbstractHttpHandler<UAVHttpMessage> {

    private CacheManager cacheManager;

    public HBQueryHandler(String cName, String feature) {
        super(cName, feature);

        cacheManager = (CacheManager) this.getConfigManager().getComponent(this.feature, "HBCacheManager");
    }

    @Override
    public String getContextPath() {

        return "/hb/query";
    }

    @Override
    public void handle(UAVHttpMessage data) {

        switch (data.getIntent()) {
            case "node":
                queryNodeInfo(data);
                break;
            case "services":
                queryNodeServies(data);
                break;
        }
    }

    /**
     * 提供服务发现的接口
     * 
     * @param data
     */
    private void queryNodeServies(UAVHttpMessage data) {

        String serviceName = data.getRequest("service");

        boolean exist = cacheManager.exists(HeartBeatProtocol.STORE_REGION_UAV,
                HeartBeatProtocol.STORE_KEY_SERVICE_PREFIX + serviceName);

        if (exist == false) {
            data.putResponse(UAVHttpMessage.ERR, "NoServiceExist");
            return;
        }

        Map<String, String> services = cacheManager.getHashAll(HeartBeatProtocol.STORE_REGION_UAV,
                HeartBeatProtocol.STORE_KEY_SERVICE_PREFIX + serviceName);

        Set<String> tarServices = new HashSet<String>();

        for (String key : services.keySet()) {

            if (services.get(key).toString().equalsIgnoreCase("1")) {
                tarServices.add(key);
            }
        }

        data.putResponse(UAVHttpMessage.RESULT, JSONHelper.toString(tarServices));
    }

    /**
     * 提供NodeInfo的查询能力
     * 
     * @param data
     */
    @SuppressWarnings("rawtypes")
    private void queryNodeInfo(UAVHttpMessage data) {

        Map<String, String> resultMap = cacheManager.getHashAll(HeartBeatProtocol.STORE_REGION_UAV,
                HeartBeatProtocol.STORE_KEY_NODEINFO);

        String resultMsg = "{}";
        String filterKey = data.getRequest("fkey");
        String filterValue = data.getRequest("fvalue");
        // 过滤 begin
        if (!StringHelper.isEmpty(filterValue)) {
            Map<String, String> filterMap = new HashMap<String, String>();
            String[] fValues = filterValue.split(",");
            Iterator i = resultMap.keySet().iterator();
            while (i.hasNext()) {
                String key = String.valueOf(i.next());
                String body = resultMap.get(key);

                for (String value : fValues) {
                    String groupFilter = "\"" + filterKey + "\":\"" + value + "\",";
                    if (body.indexOf(groupFilter) >= 0) {
                        filterMap.put(key, body);
                        break;
                    }
                }
            }

            resultMsg = JSONHelper.toString(filterMap);
        }
        else {
            resultMsg = JSONHelper.toString(resultMap);
        }

        data.putResponse(UAVHttpMessage.RESULT, resultMsg);
    }

}
