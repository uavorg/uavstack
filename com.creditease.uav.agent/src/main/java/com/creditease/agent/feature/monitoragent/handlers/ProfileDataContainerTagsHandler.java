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

package com.creditease.agent.feature.monitoragent.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.agent.spi.AbstractHandler;
import com.creditease.agent.spi.AgentFeatureComponent;

/**
 * 将Profile数据转给HeartBeatClient实现打标签
 * 
 * @author zhen zhang
 *
 */
public class ProfileDataContainerTagsHandler extends AbstractHandler<MonitorDataFrame> {

    public ProfileDataContainerTagsHandler(String cName, String feature) {
        super(cName, feature);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void handle(MonitorDataFrame data) {

        AgentFeatureComponent ps = (AgentFeatureComponent) this.getConfigManager().getComponent("procscan",
                "ProcDetectAgent");

        if (ps == null) {
            log.warn(this, "AgentFeatureComponent[procscan] NO Exist.");
            return;
        }

        Map<String, Long> appNames = new HashMap<String, Long>();

        long curTime = System.currentTimeMillis();

        // get all frames
        Map<String, List<Map>> frames = data.getDatas();

        for (String appid : frames.keySet()) {

            List<Map> appDatas = frames.get(appid);

            for (Map appData : appDatas) {
                // get PEId
                String peId = (String) appData.get("PEId");

                if (!"cpt".equals(peId)) {
                    continue;
                }

                List<Map> instances = (List<Map>) appData.get("Instances");

                for (Map instance : instances) {
                    // get instance id
                    String instanceId = (String) instance.get("id");

                    if (!"webapp".equalsIgnoreCase(instanceId)) {
                        continue;
                    }

                    // get instance fields
                    Map<String, Object> fields = (Map<String, Object>) instance.get("values");

                    // get appName as tag
                    String tag = (String) fields.get("appname");

                    if (!StringHelper.isEmpty(tag)) {
                        appNames.put(tag, curTime);
                    }
                }
            }
        }

        ps.exchange("procscan.nodeinfo.tags", appNames);
    }

}
