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

import com.creditease.agent.spi.AbstractHandler;

public abstract class AbstractHBClientHandler extends AbstractHandler<HeartBeatEvent> {

    public AbstractHBClientHandler(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public void handle(HeartBeatEvent data) {

        switch (data.getStage()) {
            /**
             * CLIENT_IN
             */
            case CLIENT_IN:
                handleClientIn(data);
                break;
            /**
             * CLIENT_OUT
             */
            case CLIENT_OUT:
                handleClientOut(data);
                break;
            default:
                throw new RuntimeException("Unknown Stage[" + data.getStage()
                        + "] for HBClientDefaultHandler's HeartBeatEvent " + data.toJSONString());
        }

        if (log.isDebugEnable()) {
            log.debug(this, "EVENT " + data.getStage() + ":" + data.toJSONString());
        }
    }

    /**
     * handleClientOut
     * 
     * @param data
     */
    public abstract void handleClientOut(HeartBeatEvent data);

    /**
     * handleClientIn
     * 
     * @param data
     */
    public abstract void handleClientIn(HeartBeatEvent data);

}
