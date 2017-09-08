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

package com.creditease.uav.feature.upgrade.download;

import com.creditease.agent.spi.AbstractHttpServiceComponent2;
import com.creditease.agent.spi.HttpMessage;

public class UpgradeDownloadServerWorker extends AbstractHttpServiceComponent2<UpgradeHttpMessage> {

    public UpgradeDownloadServerWorker(String cName, String feature, String initHandlerKey) {
        super(cName, feature, initHandlerKey);
    }

    @Override
    protected UpgradeHttpMessage adaptRequest(HttpMessage message) {

        if (log.isDebugEnable()) {
            log.debug(this, "Upgrade download http server request: " + message.getRequestURI());
        }
        UpgradeHttpMessage msg = new UpgradeHttpMessage();
        msg.putObjectParam("message", message);

        return msg;
    }

    @Override
    protected void adaptResponse(HttpMessage message, UpgradeHttpMessage t) {

        // will not implement, because downloading package will be handled in asynchronous way.
    }
}
