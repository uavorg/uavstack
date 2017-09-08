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

package com.creditease.agent.feature.hbagent;

import com.creditease.agent.heartbeat.api.HeartBeatEvent;
import com.creditease.agent.spi.AbstractHttpServiceComponent;
import com.creditease.agent.spi.HttpMessage;

public class HeartBeatServerListenWorker extends AbstractHttpServiceComponent<HeartBeatEvent> {

    public HeartBeatServerListenWorker(String cName, String feature, String initHandlerKey) {
        super(cName, feature, initHandlerKey);
    }

    @Override
    protected HeartBeatEvent adaptRequest(HttpMessage message) {

        String payload = message.getRequestBodyAsString("utf-8");

        HeartBeatEvent event = new HeartBeatEvent(HeartBeatEvent.Stage.SERVER_IN, payload);

        this.runHandlers(event);

        event.setStage(HeartBeatEvent.Stage.SERVER_OUT);

        return event;
    }

    @Override
    protected void adaptResponse(HttpMessage message, HeartBeatEvent t) {

        message.putResponseBodyInString(t.toJSONString(), 200, "utf-8");
    }

}
