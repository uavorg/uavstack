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

package com.creditease.agent.workqueue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.spi.ActionEngine;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.agent.spi.ISystemActionEngineMgr;

public class SystemActionEngineMgr implements ISystemActionEngineMgr {

    private Map<String, IActionEngine> aeMap = new HashMap<String, IActionEngine>();

    @Override
    public IActionEngine newActionEngine(String actionEngineID, String feature) {

        if (aeMap.containsKey(actionEngineID)) {
            shutdown(actionEngineID);
        }

        IActionEngine ae = new ActionEngine(actionEngineID, feature);

        aeMap.put(actionEngineID, ae);

        return ae;
    }

    @Override
    public IActionEngine getActionEngine(String actionEngineID) {

        return aeMap.get(actionEngineID);
    }

    @Override
    public void shutdown(String actionEngineID) {

        if (aeMap.containsKey(actionEngineID)) {
            IActionEngine ae = aeMap.remove(actionEngineID);
            ae.stop();
            ae.clean();
            // unregister actionEngine
            ConfigurationManager.getInstance().unregisterComponent(((ActionEngine) ae).getFeature(), actionEngineID);
        }
    }

    @Override
    public void shutdown() {

        Set<String> keys = aeMap.keySet();
        for (String actionEngineID : keys) {
            shutdown(actionEngineID);
        }
    }

}
