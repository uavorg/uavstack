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

package com.creditease.uav.invokechain.collect.actions;

import java.util.List;

import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.uav.invokechain.data.RpcSlowOperSpan;

/**
 * 处理rpc(包括http)协议的数据
 *
 */
public class SlowOperRpcAction extends AbstractSlowOperProtocolAction {

    public SlowOperRpcAction(String cName, String feature, IActionEngine engine) {
        super(cName, feature, engine);
    }

    @Override
    public void doAction(ActionContext context) throws Exception {

        List<String> msg = analyzeProtocol((String) context.getParam("content"));

        if (msg.size() != 6 && msg.size() != 7) {
            this.log.warn(this, "unsupported protocol,content is" + context.getParam("content"));
            return;
        }

        RpcSlowOperSpan span = null;
        if (msg.size() == 6) {
            span = new RpcSlowOperSpan(msg.get(0), msg.get(1), msg.get(2), (String) context.getParam("appid"),
                    msg.get(3), msg.get(4), "", "", msg.get(5));
        }
        else {
            span = new RpcSlowOperSpan(msg.get(0), msg.get(1), msg.get(2), (String) context.getParam("appid"),
                    msg.get(3), msg.get(4), msg.get(5), msg.get(6), "");
        }
        context.putParam("span", span);
        context.putParam("protocolType", getProtocolType());
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

    @Override
    public String getProtocolType() {

        return "rpc";
    }

}
