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

/**
 * HostExprAdaptor description: for HOST_STATE expr's adaption
 *
 */
public class HostExprAdaptor extends AbstractExprAdaptor{


    public HostExprAdaptor(String cName, String feature, IActionEngine engine) {

        super(cName, feature, engine);        
    }

    @Override
    public String convertInstance(String instance, String type) {

        switch (type) {
            case CLIENT_RESP:
                instance = instance.substring(0,instance.indexOf(":")); 
                break;
            case URL_RESP:                   
            case APP_RESP:                     
            case SERVER_RESP:                
            case JVM:
                instance = instance.substring(instance.indexOf("//") + 2, instance.lastIndexOf(":"));
                break;
            case PROC_STATE:
                int endIndex=instance.indexOf("_");
                if(endIndex != -1) {
                    instance = instance.substring(0,endIndex); 
                }       
                break;   
            case HOST_STATE:
                break;
            default:
                instance = null;
                break;                
        }                
        
        return instance;
    }

}
