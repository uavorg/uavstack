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

package com.creditease.uav.feature.runtimenotify.expradaptors;

import com.creditease.agent.spi.IActionEngine;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.feature.RuntimeNotifyCatcher;
import com.creditease.uav.feature.runtimenotify.Slice;
import com.creditease.uav.feature.runtimenotify.scheduler.RuntimeNotifyStrategyMgr;

/**
 * ProcExprAdaptor  description: for PROC_STATE expr's adaption
 *
 */
public class ProcExprAdaptor extends AbstractExprAdaptor{
    
    private CacheManager cm;
    
    public ProcExprAdaptor(String cName, String feature, IActionEngine engine) {

        super(cName, feature, engine);
        
        cm = (CacheManager) this.getConfigManager().getComponent(feature, RuntimeNotifyCatcher.CACHE_MANAGER_NAME);

    }

    @Override
    public String convertInstance(String instance, String type) {

        switch (type) {
            case CLIENT_RESP:
                String pid = getPidFromCache(instance,type);
                if (pid!=null) {
                    instance = instance.substring(0,instance.indexOf(":"));
                    instance = instance+"_*_"+pid;
                }else {
                    instance =null;
                }
                break;
            case URL_RESP:                   
            case APP_RESP:                     
            case SERVER_RESP:                
            case JVM:
                pid = getPidFromCache(instance,type);
                if (pid!=null) {
                    instance = instance.substring(instance.indexOf("//") + 2, instance.lastIndexOf(":"));
                    instance = instance+"_*_"+pid;
                }else {
                    instance =null;
                }               
                break;            
            case PROC_STATE:
                instance = instance+"*";
                break;
            case HOST_STATE:                
            default:
                instance = null;
                break;                    
        }                
        
        return instance;
    }
    
    private String getPidFromCache(String instance, String type) {        

        if(CLIENT_RESP.equals(type)) {
            instance = "client@"+type+"@"+instance;
        }
        else{
            instance = "server@"+type+"@"+instance;
        }            

        String cacheKey = "SLICE_" + instance + "_";
        
        for (int index = 0; index < 60; index++) {
            String result = cm.lpop(RuntimeNotifyStrategyMgr.UAV_CACHE_REGION, cacheKey + index);
            if (result != null) {
                Slice s = new Slice(result);
                return (String) s.getArgs().get("pid");
            }
        }
        
        return null;
    }   
}
