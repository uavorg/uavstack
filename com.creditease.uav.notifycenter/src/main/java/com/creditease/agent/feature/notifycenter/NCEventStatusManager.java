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
package com.creditease.agent.feature.notifycenter;

import java.util.concurrent.TimeUnit;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.AbstractComponent;
import com.creditease.uav.cache.api.CacheManager;

/**
 * 
 * NCEventStatusManager description: package the CacheManager used in notification handler
 *
 */
public class NCEventStatusManager extends AbstractComponent {
    
    private static final String NCJUDGE_PREFIX = "NCJUDGE_";

    private CacheManager cm;
    private int expire;
    
    public NCEventStatusManager(String cName, String feature, CacheManager cm) {

        super(cName, feature);
        this.cm = cm;
        expire = DataConvertHelper.toInt(this.getConfigManager().getFeatureConfiguration(this.feature, "nc.notify.ttl"),
                4) * 3600 * 1000;
    }

    /**
     * check if there is SYNC atomic key on enevtKey
     * @param eventKey
     * @return
     */
    public boolean checkSyncAtomicKey(String eventKey) {
        int AtomicSych = cm.incre(NCConstant.STORE_REGION, eventKey);

        if (AtomicSych > 1) {
            
            String lock = cm.get(NCConstant.STORE_REGION, eventKey);
            if (null != lock) {
                int atomicLock = Integer.parseInt(lock);

                /**
                 * To prevent deadLock of event
                 */
                if (atomicLock > 3) {
                    cm.del(NCConstant.STORE_REGION, eventKey);
                }
            }
            return true;
        }
        else {
            cm.incre(NCConstant.STORE_REGION, eventKey);
            return false;
        }
    }
    
    /**
     * Get the cache for the event
     * 
     * @param eventKey
     * @return
     */
    public String getEventCache(String eventKey) throws RuntimeException {
        
        return cm.get(NCConstant.STORE_REGION, NCJUDGE_PREFIX + eventKey);
    }
    
    /**
     * update cache after the handle of event
     * @param event
     */
    public void updateEventCache(NotificationEvent event) {

        if (log.isDebugEnable()) {
            log.debug(this, "NC Update Cache: " + event.getArg(NCConstant.NTFVALUE));
        }

        try {
            cm.put(NCConstant.STORE_REGION, NCJUDGE_PREFIX + event.getArg(NCConstant.NTFKEY),
                    event.getArg(NCConstant.NTFVALUE));
            // 增加过期时间后如该预警未发生报警的时间大于过期时间则该redis key消失，下次预警将被认为是新预警
            cm.expire(NCConstant.STORE_REGION, NCJUDGE_PREFIX + event.getArg(NCConstant.NTFKEY), expire,
                    TimeUnit.MILLISECONDS);
        }
        catch (Exception e) {
            log.err(this, "Error when updateCache", e);
        } finally {
            cm.del(NCConstant.STORE_REGION, event.getArg(NCConstant.NTFKEY));
        }
        
    }

}
