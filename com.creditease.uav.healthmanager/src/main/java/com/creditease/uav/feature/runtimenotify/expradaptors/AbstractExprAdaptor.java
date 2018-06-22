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

import com.creditease.agent.spi.AbstractBaseAction;
import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.IActionEngine;

/**
 * AbstractExprAdaptAction description: expr's adaption
 *
 */
public abstract class AbstractExprAdaptor extends AbstractBaseAction {

    public final static String CLIENT_RESP="clientResp";
    
    public final static String URL_RESP="urlResp";
    
    public final static String APP_RESP="appResp";
    
    public final static String SERVER_RESP="serverResp";
  
    public final static String JVM="jvm";

    public final static String PROC_STATE="procState";
    
    public final static String HOST_STATE="hostState";

    public AbstractExprAdaptor(String cName, String feature, IActionEngine engine) {

        super(cName, feature, engine);
    }


    @Override
    public void doAction(ActionContext context) throws Exception {
        
       String instance=(String) context.getParam("instance");
       String type=(String) context.getParam("instType");  
       
       context.putParam("instance", convertInstance(instance, type));
    }


    @Override
    public String getSuccessNextActionId() {
      
        return null;
    }

    @Override
    public String getFailureNextActionId() {
      
        return null;
    }


    @Override
    public String getExceptionNextActionId() {
       
        return null;
    }
    
    /**
     *  convert source instance to target Expr's valid instance </br>
     *  </br>
     *  sample instance:</br>
     *  </br>
     *  clientRespInst: "127.0.0.1:8080#apphub#http://127.0.0.1:4243"</br>
     *  urlRespInst: "http://127.0.0.1:8080/apphub/godeye/hm/q"</br>
     *  appRespInst: "http://127.0.0.1:8080/apphub---apphub"</br>
     *  serverRespInst: "http://127.0.0.1:8080"</br>
     *  procStateInst: "127.0.0.1_java_10342"</br>
     *  hostStateInst: "127.0.0.1"</br>
    */
    public abstract String convertInstance(String instance, String type);

}
