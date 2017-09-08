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

package com.creditease.uav.invokechain.collect;

import java.util.Map;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;

import com.creditease.agent.apm.api.AbstractCollectDataHandler;
import com.creditease.agent.apm.api.CollectDataFrame;
import com.creditease.agent.apm.api.CollectDataFrame.Line;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.EncodeHelper;
import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.uav.elasticsearch.client.ESClient;
import com.creditease.uav.invokechain.SlowOperIndexMgr;
import com.creditease.uav.invokechain.collect.actions.SlowOperJdbcAction;
import com.creditease.uav.invokechain.collect.actions.SlowOperMQConsumerAction;
import com.creditease.uav.invokechain.collect.actions.SlowOperMQProducerAction;
import com.creditease.uav.invokechain.collect.actions.SlowOperMethodAction;
import com.creditease.uav.invokechain.collect.actions.SlowOperRpcAction;
import com.creditease.uav.invokechain.data.SlowOperSpan;

/**
 * 
 * 重调用链数据处理handler
 *
 */
public class SlowOperDataCollectHandler extends AbstractCollectDataHandler {

    private ESClient client;

    private SlowOperIndexMgr indexMgr;

    public SlowOperDataCollectHandler(String cName, String feature) {
        super(cName, feature);

        client = (ESClient) this.getConfigManager().getComponent(this.feature, "ESClient");
        indexMgr = (SlowOperIndexMgr) this.getConfigManager().getComponent(this.feature, "SlowOperIndexMgr");
        IActionEngine engine = this.getActionEngineMgr().getActionEngine("SlowOperActionEngine");

        new SlowOperRpcAction("http.service", feature, engine);
        new SlowOperRpcAction("apache.http.Client", feature, engine);
        new SlowOperRpcAction("apache.http.AsyncClient", feature, engine);
        new SlowOperMQConsumerAction("mq.service", feature, engine);
        new SlowOperMQProducerAction("rabbitmq.client", feature, engine);
        new SlowOperMethodAction("method", feature, engine);
        new SlowOperJdbcAction("jdbc.client", feature, engine);
    }

    @Override
    public void handle(CollectDataFrame frame) {

        if (this.log.isDebugEnable()) {
            this.log.debug(this, frame.toJSONString());
        }

        BulkRequestBuilder bulkRequest = client.getClient().prepareBulk();
        String appUUID = frame.getTarget();
        // 从uuid中获取appid
        String appid = appUUID.split("---")[1];
        for (Line line : frame.getLines()) {

            String content = line.getContent();

            try {
                // 提取epinfo
                StringBuilder builder = new StringBuilder();
                int headPoint = 0;
                for (int i = 0; i < content.length(); i++) {
                    char item = content.charAt(i);
                    if (item == ';') {
                        headPoint++;
                        if (headPoint == 3) {
                            break;
                        }
                    }
                    else {
                        if (headPoint > 1) {
                            builder.append(item);
                        }
                    }
                }
                String epinfo = builder.toString();
                // 若epinfo为数字则说明为方法级
                if (DataConvertHelper.toInt(epinfo, -1) != -1) {
                    epinfo = "method";
                }
                IActionEngine engine = this.getActionEngineMgr().getActionEngine("SlowOperActionEngine");
                ActionContext ac = new ActionContext();
                ac.putParam("content", content);
                ac.putParam("appid", appid);
                engine.execute(epinfo, ac);
                SlowOperSpan span = (SlowOperSpan) ac.getParam("span");

                pushSpanToBulkRequest(appUUID, frame.getAppgroup(), span, bulkRequest,
                        (String) ac.getParam("protocolType"));
            }
            catch (Exception e) {
                // 防止有不合法的协议报文出现
                this.log.err(this, "unsupported protocol,content is" + frame.toJSONString(), e);
            }
        }
        BulkResponse bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
            log.err(this, "INSERT InvokeChain Data to ES FAIL: " + bulkResponse.buildFailureMessage());
        }

    }

    @Override
    public boolean isHandleable(String topic) {

        return "JQ_SLW".equalsIgnoreCase(topic);
    }

    /**
     * 存储到ES
     * 
     * @param appUUID
     * @param appGroup
     * @param span
     * @param bulkRequest
     * @param protocolType
     */
    private void pushSpanToBulkRequest(String appUUID, String appGroup, SlowOperSpan span,
            BulkRequestBuilder bulkRequest, String protocolType) {

        /**
         * 保证不会重复(其实是防止重复读取时数据重复)
         */
        String uuid = EncodeHelper.encodeMD5(span.toString());

        /**
         * 获取当前正在使用的index名称
         */
        String currentIndex = indexMgr.prepareIndex(span.getAppid());
        /**
         * 准备对应type
         */
        indexMgr.prepareIndexType(currentIndex, protocolType);

        IndexRequestBuilder irb = client.getClient().prepareIndex(currentIndex, protocolType, uuid);

        Map<String, Object> m = span.toMap();

        // 暂时保留这两个属性
        m.put("appuuid", appUUID);
        m.put("appgroup", appGroup);

        irb.setSource(m);

        bulkRequest.add(irb);
    }

}
