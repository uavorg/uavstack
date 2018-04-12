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

import java.util.Map;

import com.creditease.agent.monitor.api.NotificationEvent;
import com.creditease.agent.spi.AbstractComponent;
import com.creditease.uav.cache.api.CacheManager;

/**
 * 
 * NCEventStatusManager description: package the CacheManager used in notification handler
 *
 */
public class NCEventStatusManager extends AbstractComponent {
    
    private CacheManager cm;
    
    public NCEventStatusManager(String cName, String feature, CacheManager cm) {

        super(cName, feature);
        this.cm = cm;
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
     * obtain the cache for the event
     * @param eventKey
     * @return
     * @throws RuntimeException
     */
    public Map<String, String> obtainEventCache(String eventKey) throws RuntimeException{
        
        try {
            Map<String, String> ntfvalue = cm.getHash(NCConstant.STORE_REGION, NCConstant.STORE_KEY_NCINFO, eventKey);
            return ntfvalue;
        } catch (RuntimeException e) {
            cm.del(NCConstant.STORE_REGION, eventKey);
            throw e;
        }
        
    }
    
    /**
     * update cache after the handle of event
     * @param event
     */
    public void updateCache(NotificationEvent event) {

        if (log.isDebugEnable()) {
            log.debug(this, "NC Update Cache: " + event.getArg(NCConstant.NTFVALUE));
        }

        try {
            cm.putHash(NCConstant.STORE_REGION, NCConstant.STORE_KEY_NCINFO, event.getArg(NCConstant.NTFKEY),
                    event.getArg(NCConstant.NTFVALUE));
        }
        catch (Exception e) {

            log.err(this, "Error when updateCache", e);
        } finally {
            cm.del(NCConstant.STORE_REGION, event.getArg(NCConstant.NTFKEY));
        }
        
    }

}
