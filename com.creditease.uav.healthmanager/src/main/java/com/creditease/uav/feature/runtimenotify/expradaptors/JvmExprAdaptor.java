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
 * JvmExprAdaptor description: for JVM expr's adaption
 *
 */
public class JvmExprAdaptor extends AbstractExprAdaptor{


    public JvmExprAdaptor(String cName, String feature, IActionEngine engine) {

        super(cName, feature, engine);        
    }

    @Override
    public String convertInstance(String instance, String type) {

        switch (type) {
            case CLIENT_RESP:
                String[] strs=instance.split("#");
                if(strs.length==3) {
                    instance = "http://"+strs[0]; 
                }else {
                    instance = null ;
                }
                break;
            case URL_RESP:
            case APP_RESP:
                int index=instance.indexOf("//")+2;
                String temp=instance.substring(index);
                if(temp.indexOf("/")>-1) {
                    instance = instance.substring(0,temp.indexOf("/")+index);
                }
                break;
            case SERVER_RESP:
            case JVM:
                break;
            case PROC_STATE:
            case HOST_STATE:            
            default:
                instance = null;
                break;      
                
        }
        
        return instance;
    }

}
