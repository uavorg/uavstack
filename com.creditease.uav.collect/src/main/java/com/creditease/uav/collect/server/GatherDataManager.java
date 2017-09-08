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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.creditease.agent.feature.common.messaging.StandardMessagingBuilder;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.uav.collect.server.messaging.DefaultGatherDataHandler;
import com.creditease.uav.messaging.api.MessageConsumer;
import com.creditease.uav.messaging.api.MessageHandler;
import com.creditease.uav.messaging.api.MessagingContext;

public class GatherDataManager extends AgentFeatureComponent {

    private List<MessageConsumer> consumers;

    public GatherDataManager(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public void start() {

        StandardMessagingBuilder builder = new StandardMessagingBuilder("GatherDataManagerMessageBuilder", feature);
        try {
            builder.init();
        }
        catch (IOException e) {
            log.err(this, "Read msgtype2topic.properties FAILs, GatherDataManager can not START", e);
            return;
        }

        String[] topics = getConfigManager().getFeatureConfiguration(feature, "topics").split(",");
        List<MessageHandler> handlers = new ArrayList<>();
        for (String topic : topics) {
            handlers.add(new DefaultGatherDataHandler(topic));
        }
        MessagingContext.putConsumerHandlers(handlers);

        // init consumers
        consumers = new ArrayList<>();

        for (String topic : topics) {
            MessageConsumer consumer = builder.buildConsumer(topic);

            if (consumer == null) {
                continue;
            }

            consumers.add(consumer);
        }

        // start all consumers
        for (MessageConsumer consumer : consumers) {

            consumer.start();

            log.info(this, "GatherData Consumer [" + consumer.getName() + "] start");
        }

        log.info(this, "GatherDataManager start");
    }

    @Override
    public void stop() {

        for (MessageConsumer consumer : consumers) {

            consumer.shutdown();

            log.info(this, "GatherData Consumer [" + consumer.getName() + "] shutdown");
        }

        log.info(this, "GatherDataManager stop");
    }

    @Override
    public Object exchange(String eventKey, Object... data) {

        return null;
    }
}
