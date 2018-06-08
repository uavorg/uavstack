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

package com.creditease.uav.feature.healthmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.spi.AbstractTimerWork;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.cache.api.CacheManager.CacheLock;

/**
 * TTL 负责清理过期的Profile数据
 * 
 * @author zhen zhang
 *
 */
public class HealthManagerProfileDataLifeKeeper extends AbstractTimerWork {

    private long lockTimeout = 60000;

    private long nodeDyingTimeout = 15000;

    private CacheManager cacheManager;

    private CacheLock lock;

    private static List<String> profileKeyList = new ArrayList<String>();

    static {
        profileKeyList.add(HealthManagerConstants.STORE_KEY_PROFILEINFO_JARLIB);
        profileKeyList.add(HealthManagerConstants.STORE_KEY_PROFILEINFO_DUBBOPROVIDER);
        profileKeyList.add(HealthManagerConstants.STORE_KEY_PROFILEINFO_MSCPHTTP);
        profileKeyList.add(HealthManagerConstants.STORE_KEY_PROFILEINFO_MSCPTIMEWORKER);
        profileKeyList.add(HealthManagerConstants.STORE_KEY_PROFILEINFO_FILTER);
        profileKeyList.add(HealthManagerConstants.STORE_KEY_PROFILEINFO_LISTENER);
        profileKeyList.add(HealthManagerConstants.STORE_KEY_PROFILEINFO_SERVLET);
        profileKeyList.add(HealthManagerConstants.STORE_KEY_PROFILEINFO_JAXWS);
        profileKeyList.add(HealthManagerConstants.STORE_KEY_PROFILEINFO_JAXWSP);
        profileKeyList.add(HealthManagerConstants.STORE_KEY_PROFILEINFO_JAXRS);
        profileKeyList.add(HealthManagerConstants.STORE_KEY_PROFILEINFO_SPRINGMVC);
        profileKeyList.add(HealthManagerConstants.STORE_KEY_PROFILEINFO_SPRINGMVCREST);
        profileKeyList.add(HealthManagerConstants.STORE_KEY_PROFILEINFO_STRUTS2);
    }

    public HealthManagerProfileDataLifeKeeper(String cName, String feature) {
        super(cName, feature);

        // lock timeout
        String lockTimeoutStr = this.getConfigManager().getFeatureConfiguration(feature, "lifekeeper.locktimeout");

        lockTimeout = (StringHelper.isEmpty(lockTimeoutStr)) ? lockTimeout : Long.parseLong(lockTimeoutStr);

        // node dying timeout
        String nodeDyingTimeoutStr = this.getConfigManager().getFeatureConfiguration(feature, "lifekeeper.apptimeout");

        nodeDyingTimeout = (StringHelper.isEmpty(nodeDyingTimeoutStr)) ? nodeDyingTimeout
                : Long.parseLong(nodeDyingTimeoutStr);

        cacheManager = (CacheManager) this.getConfigManager().getComponent(this.feature, "HMCacheManager");

        lock = cacheManager.newCacheLock(HealthManagerConstants.STORE_REGION_UAV,
                HealthManagerConstants.STORE_KEY_PROFILEINFO_LIFEKEEP_LOCK, lockTimeout);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void run() {

        CacheManager cm = cacheManager;

        /**
         * step 1: get the life keeper lock & check if can operate the nodeinfo
         */
        if (!lock.getLock()) {
            return;
        }

        List<String> nodeIds = cm.getHashKeys(HealthManagerConstants.STORE_REGION_UAV,
                HealthManagerConstants.STORE_KEY_PROFILEINFO);

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

            Map<String, String> nodeInfoMap = cm.getHash(HealthManagerConstants.STORE_REGION_UAV,
                    HealthManagerConstants.STORE_KEY_PROFILEINFO, nodeId);

            String nodeInfoString = nodeInfoMap.get(nodeId);

            Map appInstProfile = JSONHelper.toObject(nodeInfoString, Map.class);

            long nodeClientTimestamp = (Long) appInstProfile.get("time");

            long checkTime = System.currentTimeMillis();

            long timeout = checkTime - nodeClientTimestamp;

            if (timeout < 0) {
                log.warn(this, "[PROFILE] There is sth wrong with timeout check of NODE[" + nodeId + "]: cur="
                        + checkTime + ",node=" + nodeClientTimestamp);
                continue;
            }

            // recheck if the lock is still in hand
            if (null == isLockInHand) {
                isLockInHand = lock.isLockInHand();// isLockStillInHand(cm, myLock);
            }

            if (!isLockInHand) {
                return;
            }

            /**
             * step 1: maintain application state TTL
             */

            // node is in dying state
            if (timeout >= this.nodeDyingTimeout && timeout < this.nodeDyingTimeout * 2) {
                appInstProfile.put("state", "0");
                nodeInfoMap.put(nodeId, JSONHelper.toString(appInstProfile));
                cm.putHash(HealthManagerConstants.STORE_REGION_UAV, HealthManagerConstants.STORE_KEY_PROFILEINFO,
                        nodeInfoMap);
            }
            // node is in dead state
            else if (timeout >= this.nodeDyingTimeout * 2 && timeout < this.nodeDyingTimeout * 3) {
                appInstProfile.put("state", "-1");
                nodeInfoMap.put(nodeId, JSONHelper.toString(appInstProfile));
                cm.putHash(HealthManagerConstants.STORE_REGION_UAV, HealthManagerConstants.STORE_KEY_PROFILEINFO,
                        nodeInfoMap);
            }
            // clean this node info
            else if (timeout >= this.nodeDyingTimeout * 3) {
                // delete profile data
                cm.delHash(HealthManagerConstants.STORE_REGION_UAV, HealthManagerConstants.STORE_KEY_PROFILEINFO,
                        nodeId);

                for (String key : profileKeyList) {
                    cm.delHash(HealthManagerConstants.STORE_REGION_UAV, key, nodeId);
                }

                // delete client profile data
                cm.delHash(HealthManagerConstants.STORE_REGION_UAV,
                        HealthManagerConstants.STORE_KEY_PROFILEINFO_APPCLIENT, "C@" + nodeId);
                // delete ip link profile data
                cm.del(HealthManagerConstants.STORE_REGION_UAV, "LNK@" + nodeId);
            }

            /**
             * step 2: maintain app ip link profile data
             */
            checkTime = System.currentTimeMillis();

            boolean isAnyChange = false;

            long iplinkTimeOut = 3600 * 1000;

            Map<String, String> iplinkMap = cm.getHashAll(HealthManagerConstants.STORE_REGION_UAV, "LNK@" + nodeId);

            List<String> delKeys = new ArrayList<String>();

            for (String key : iplinkMap.keySet()) {

                if (key.endsWith("-ts") == false) {
                    continue;
                }

                String tkey = key.substring(0, key.length() - 3);

                long iplink_ts = DataConvertHelper.toLong(iplinkMap.get(key), -1);

                if (iplink_ts == -1) {
                    continue;
                }

                long iplink_timeout = checkTime - iplink_ts;

                /**
                 * iplink over 1 hour, then remove it, at least we can see the ip route in 1 hour from first happen
                 */
                if (iplink_timeout >= iplinkTimeOut) {
                    isAnyChange = true;
                    delKeys.add(tkey);
                    delKeys.add(key);
                }
            }

            if (isAnyChange == true) {
                String[] fields = new String[delKeys.size()];
                cm.delHash(HealthManagerConstants.STORE_REGION_UAV, "LNK@" + nodeId, delKeys.toArray(fields));
            }
        }

        /**
         * step 4: release the lock
         */
        lock.releaseLock();
    }

}
