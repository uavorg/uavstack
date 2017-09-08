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

package com.creditease.uav.collect.server;

import java.util.Arrays;
import java.util.List;

import com.creditease.agent.apm.api.AbstractCollectDataHandler;
import com.creditease.agent.apm.api.CollectDataFrame;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.agent.spi.AgentFeatureComponent;

public class GatherDataManagerTest extends AgentFeatureComponent {

    ISystemLogger logger = SystemLogger.getLogger(GatherDataManagerTest.class.getSimpleName(),
            "TestGatherData.%g.%u.log", "INFO", false, 5 * 1024 * 1024);

    public GatherDataManagerTest(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public void start() {

        String topic = getConfigManager().getFeatureConfiguration(feature, "topics");
        String[] topics = topic.split(",");
        List<String> list = Arrays.asList(topics);

        new CollectDataHandlerTest(CollectDataHandlerTest.class.getName(), feature, list);
    }

    @Override
    public Object exchange(String eventKey, Object... data) {

        return null;
    }

    private class CollectDataHandlerTest extends AbstractCollectDataHandler {

        private List<String> list;

        public CollectDataHandlerTest(String cName, String feature, List<String> list) {
            super(cName, feature);
            this.list = list;
        }

        @Override
        public void handle(CollectDataFrame frame) {

            logger.info(this, "======================================================================"
                    + "\n**********************************************************************");
            logger.info(this, String.format("time=%s, ip=%s, target=%s, action=%s, file=%s, eof=%s", frame.getTime(),
                    frame.getIp(), frame.getTarget(), frame.getAction(), frame.getFile(), frame.isEof()));
            for (CollectDataFrame.Line line : frame.getLines()) {
                logger.info(this, line.getLnum() + "  " + line.getContent());
            }
            logger.info(this, "**********************************************************************"
                    + "\n======================================================================");
        }

        @Override
        public boolean isHandleable(String topic) {

            return list.contains(topic);
        }

    }
}
