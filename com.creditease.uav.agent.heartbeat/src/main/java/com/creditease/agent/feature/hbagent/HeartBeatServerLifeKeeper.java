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

package com.creditease.agent.feature.hbagent;

import java.util.List;
import java.util.Map;

import com.creditease.agent.feature.hbagent.node.NodeInfo;
import com.creditease.agent.feature.hbagent.node.NodeInfo.InfoType;
import com.creditease.agent.heartbeat.api.HeartBeatProtocol;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.spi.AbstractTimerWork;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.cache.api.CacheManager.CacheLock;

/**
 * 用于UAV网络节点状态的更新操作
 * 
 * @author zhen zhang
 *
 */
public class HeartBeatServerLifeKeeper extends AbstractTimerWork {

    private long lockTimeout = 60000;

    private long nodeDyingTimeout = 40000;

    private CacheManager cacheManager;

    private CacheLock lock;

    public HeartBeatServerLifeKeeper(String cName, String feature) {
        super(cName, feature);

        // lock timeout
        String lockTimeoutStr = this.getConfigManager().getFeatureConfiguration(feature, "lifekeeper.locktimeout");

        lockTimeout = (StringHelper.isEmpty(lockTimeoutStr)) ? lockTimeout : Long.parseLong(lockTimeoutStr);

        // node dying timeout
        String nodeDyingTimeoutStr = this.getConfigManager().getFeatureConfiguration(feature, "lifekeeper.nodetimeout");

        nodeDyingTimeout = (StringHelper.isEmpty(nodeDyingTimeoutStr)) ? nodeDyingTimeout
                : Long.parseLong(nodeDyingTimeoutStr);

        cacheManager = (CacheManager) this.getConfigManager().getComponent(this.feature, "HBCacheManager");

        lock = cacheManager.newCacheLock(HeartBeatProtocol.STORE_REGION_UAV, HeartBeatProtocol.STORE_KEY_LIFEKEEP_LOCK,
                lockTimeout);
    }

    @Override
    public void run() {

        CacheManager cm = cacheManager;

        /**
         * step 1: get the life keeper lock & check if can operate the nodeinfo
         */
        if (!lock.getLock()) {
            return;
        }

        List<String> nodeIds = cm.getHashKeys(HeartBeatProtocol.STORE_REGION_UAV, HeartBeatProtocol.STORE_KEY_NODEINFO);

        if (nodeIds.size() == 0) {

            // need re-get the lock in case multi lifekeeper to get the lock at the same time, the last one should open
            // this lock
            if (lock.isLockInHand()) {
                lock.releaseLock();
            }

            return;
        }

        /**
         * step 3: update the data
         */
        Boolean isLockInHand = null;

        for (String nodeId : nodeIds) {

            Map<String, String> nodeInfoMap = cm.getHash(HeartBeatProtocol.STORE_REGION_UAV,
                    HeartBeatProtocol.STORE_KEY_NODEINFO, nodeId);

            String nodeInfoString = nodeInfoMap.get(nodeId);

            NodeInfo nodeInfo = NodeInfo.toNodeInfo(nodeInfoString);

            long nodeClientTimestamp = nodeInfo.getClientTimestamp();

            long checkTime = System.currentTimeMillis();

            long timeout = checkTime - nodeClientTimestamp;

            if (timeout < 0) {
                log.warn(this, "[HEARTBEAT] There is sth wrong with timeout check of NODE[" + nodeInfo.getIp() + "."
                        + nodeInfo.getName() + "]: cur=" + checkTime + ",node=" + nodeClientTimestamp);
                continue;
            }

            // recheck if the lock is still in hand
            if (null == isLockInHand) {
                isLockInHand = lock.isLockInHand();
            }

            if (!isLockInHand) {

                return;
            }

            // node is in dying state
            if (timeout >= this.nodeDyingTimeout && timeout < this.nodeDyingTimeout * 5) {
                nodeInfo.putInfo(InfoType.Node, "state", "0");
                nodeInfoMap.put(nodeId, nodeInfo.toJSONString());
                cm.putHash(HeartBeatProtocol.STORE_REGION_UAV, HeartBeatProtocol.STORE_KEY_NODEINFO, nodeInfoMap);
            }
            // node is in dead state
            else if (timeout >= this.nodeDyingTimeout * 5 && timeout < this.nodeDyingTimeout * 10) {
                nodeInfo.putInfo(InfoType.Node, "state", "-1");
                nodeInfoMap.put(nodeId, nodeInfo.toJSONString());
                cm.putHash(HeartBeatProtocol.STORE_REGION_UAV, HeartBeatProtocol.STORE_KEY_NODEINFO, nodeInfoMap);
            }
            // clean this node info
            else if (timeout >= this.nodeDyingTimeout * 10) {
                cm.delHash(HeartBeatProtocol.STORE_REGION_UAV, HeartBeatProtocol.STORE_KEY_NODEINFO, nodeId);
            }
        }

        /**
         * step 4: release the lock
         */
        lock.releaseLock();
    }
}
