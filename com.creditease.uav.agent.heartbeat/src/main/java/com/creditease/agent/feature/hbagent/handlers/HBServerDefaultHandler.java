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

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.creditease.agent.feature.hbagent.node.NodeInfo;
import com.creditease.agent.feature.hbagent.node.NodeInfo.InfoType;
import com.creditease.agent.heartbeat.api.AbstractHBServerHandler;
import com.creditease.agent.heartbeat.api.HeartBeatEvent;
import com.creditease.agent.heartbeat.api.HeartBeatProtocol;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.uav.cache.api.CacheManager;

public class HBServerDefaultHandler extends AbstractHBServerHandler {

    private CacheManager cacheManager;

    private boolean isEnableNTP;

    public HBServerDefaultHandler(String cName, String feature) {
        super(cName, feature);

        cacheManager = (CacheManager) this.getConfigManager().getComponent(this.feature, "HBCacheManager");

        isEnableNTP = DataConvertHelper
                .toBoolean(this.getConfigManager().getFeatureConfiguration(this.feature, "ntp.enable"), false);
    }

    @Override
    public void handleServerOut(HeartBeatEvent data) {

        if (!data.containEvent(HeartBeatProtocol.EVENT_DEFAULT)) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<String> nodeInfoStringArray = (List<String>) data.getParam(HeartBeatProtocol.EVENT_DEFAULT,
                HeartBeatProtocol.EVENT_KEY_NODE_INFO);

        if (null != nodeInfoStringArray && nodeInfoStringArray.size() == 0) {
            return;
        }

        String checkIsMaster = this.getConfigManager().getFeatureConfiguration("hbserveragent", "ismaster");

        // is not master
        if (null == checkIsMaster || !"true".equalsIgnoreCase(checkIsMaster)) {

            AgentFeatureComponent afc = (AgentFeatureComponent) this.getConfigManager().getComponent("hbclientagent",
                    "HeartBeatClientAgent");

            afc.exchange("hbclientagent.nodeinfo.upstream", nodeInfoStringArray);
        }
        // is master
        else {
            // AgentFeatureComponent ida = (AgentFeatureComponent) this.getConfigManager().getComponent("ida",
            // "IssueDiagnoseAssitAgent");

            // sync node info to storage
            cacheManager.beginBatch();

            for (String nodeInfoString : nodeInfoStringArray) {

                NodeInfo ni = NodeInfo.toNodeInfo(nodeInfoString);

                long curTime = System.currentTimeMillis();

                // set server side timestamp
                ni.setServerTimestamp(curTime);

                // /**
                // * push to IDA feature
                // */
                // if (ida != null) {
                // ida.exchange("ida.put.data", ni);
                // }

                cacheManager.putHash(HeartBeatProtocol.STORE_REGION_UAV, HeartBeatProtocol.STORE_KEY_NODEINFO,
                        ni.getId(), ni.toJSONString());

                /**
                 * sync node services to service list
                 */
                String servicesStr = ni.getInfo(InfoType.Node, "services");

                @SuppressWarnings("rawtypes")
                Map services = JSONHelper.toObject(servicesStr, Map.class, true);

                for (Object s : services.keySet()) {

                    String sId = (String) s;

                    /**
                     * MSCP Services: state =1, means all right, expire time out is 60 seconds
                     */
                    cacheManager.putHash(HeartBeatProtocol.STORE_REGION_UAV,
                            HeartBeatProtocol.STORE_KEY_SERVICE_PREFIX + sId, (String) services.get(sId), "1");

                    cacheManager.expire(HeartBeatProtocol.STORE_REGION_UAV,
                            HeartBeatProtocol.STORE_KEY_SERVICE_PREFIX + sId, 60, TimeUnit.SECONDS);
                }
            }

            cacheManager.submitBatch();
        }

        // set return code
        data.putParam(HeartBeatProtocol.EVENT_DEFAULT, HeartBeatProtocol.EVENT_KEY_RETCODE, HeartBeatProtocol.RC_I0000);

        if (isEnableNTP == true) {
            data.putParam(HeartBeatProtocol.EVENT_DEFAULT, HeartBeatProtocol.EVENT_KEY_TIME,
                    System.currentTimeMillis());
        }
    }

    @Override
    public void handleServerIn(HeartBeatEvent data) {

        // ignore
    }
}
