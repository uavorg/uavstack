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

package com.creditease.agent.heartbeat.api;

import com.creditease.agent.spi.AbstractHttpHandler;

public abstract class AbstractHBServerHandler extends AbstractHttpHandler<HeartBeatEvent> {

    public AbstractHBServerHandler(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public String getContextPath() {

        return "/heartbeat";
    }

    @Override
    public void handle(HeartBeatEvent data) {

        switch (data.getStage()) {
            case SERVER_IN:
                handleServerIn(data);
                break;
            case SERVER_OUT:
                handleServerOut(data);
                break;
            default:
                throw new RuntimeException("Unknown Stage[" + data.getStage()
                        + "] for HBServerDefaultHandler's HeartBeatEvent " + data.toJSONString());
        }

        if (log.isDebugEnable()) {
            log.debug(this, "EVENT " + data.getStage() + ":" + data.toJSONString());
        }
    }

    /**
     * handleServerOut
     * 
     * @param data
     */
    public abstract void handleServerOut(HeartBeatEvent data);

    /**
     * handleServerIn
     * 
     * @param data
     */
    public abstract void handleServerIn(HeartBeatEvent data);
}
