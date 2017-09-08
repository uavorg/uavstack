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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.agent.spi.AbstractHttpHandler;

public class MDFListenServerHandler extends AbstractHttpHandler<UAVHttpMessage> {

    public MDFListenServerHandler(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public String getContextPath() {

        return "/ma/put/mdf";
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void handle(UAVHttpMessage data) {

        try {
            String body = data.getRequest(UAVHttpMessage.BODY);

            List<MonitorDataFrame> mdfs = new ArrayList<MonitorDataFrame>();

            // MDF array
            if (body.indexOf("[") == 0) {
                List<Map> mdfList = JSONHelper.toObjectArray(body, Map.class);

                for (Map mdfMap : mdfList) {
                    MonitorDataFrame mdf = new MonitorDataFrame(mdfMap);
                    mdfs.add(mdf);
                }

            }
            // MDF
            else {
                MonitorDataFrame mdf = new MonitorDataFrame(body);
                mdfs.add(mdf);
            }

            // send MDFs
            MonitorDataPublishHandler mdph = (MonitorDataPublishHandler) this.getConfigManager()
                    .getComponent(this.feature, "MonitorDataPublishHandler");

            mdph.handle(mdfs);

            data.putResponse(UAVHttpMessage.RESULT, "OK");
        }
        catch (Exception e) {
            data.putResponse(UAVHttpMessage.ERR, e.getMessage());
        }
    }

}
