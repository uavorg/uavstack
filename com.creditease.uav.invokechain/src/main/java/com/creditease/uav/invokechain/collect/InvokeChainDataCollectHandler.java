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
import com.creditease.agent.helpers.EncodeHelper;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.elasticsearch.client.ESClient;
import com.creditease.uav.invokechain.InvokeChainIndexMgr;
import com.creditease.uav.invokechain.data.Span;

public class InvokeChainDataCollectHandler extends AbstractCollectDataHandler {

    @SuppressWarnings("unused")
    private CacheManager cm;

    private ESClient client;

    private InvokeChainIndexMgr indexMgr;

    public InvokeChainDataCollectHandler(String cName, String feature) {
        super(cName, feature);

        cm = (CacheManager) this.getConfigManager().getComponent(this.feature, "IVCCacheManager");
        client = (ESClient) this.getConfigManager().getComponent(this.feature, "ESClient");
        indexMgr = (InvokeChainIndexMgr) this.getConfigManager().getComponent(this.feature, "InvokeChainIndexMgr");
    }

    @Override
    public void handle(CollectDataFrame frame) {

        if (this.log.isDebugEnable()) {
            this.log.debug(this, frame.toJSONString());
        }

        String appUUID = frame.getTarget();
        // cm.beginBatch();
        BulkRequestBuilder bulkRequest = client.getClient().prepareBulk();

        for (Line line : frame.getLines()) {

            try {
                String content = line.getContent();
                                 
                  Span span = new Span(content);

                  pushLatestIVCDataToCache(appUUID, span);

                  pushSpanToBulkRequest(appUUID, frame.getAppgroup(), span, bulkRequest);
              }
              catch (Exception e) {
                  this.log.err(this, "unsupported ivc content :" + line.getContent(), e);
              }
        }

        // cm.submitBatch();
        BulkResponse bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
            log.err(this, "INSERT InvokeChain Data to ES FAIL: " + bulkResponse.buildFailureMessage());
        }
    }

    /**
     * 存储到ES
     * 
     * @param appUUID
     * @param span
     * @param bulkRequest
     */
    private void pushSpanToBulkRequest(String appUUID, String appGroup, Span span, BulkRequestBuilder bulkRequest) {

        /**
         * 保证不会重复
         */
        String uuid = EncodeHelper.encodeMD5(span.toString());

        /**
         * 获取当前正在使用的index名称
         */
        String currentIndex = indexMgr.prepareIndex();

        IndexRequestBuilder irb = client.getClient().prepareIndex(currentIndex, InvokeChainIndexMgr.IVC_Table, uuid);

        Map<String, Object> m = span.toMap();

        m.put("appuuid", appUUID);
        m.put("appgroup", appGroup);

        irb.setSource(m);

        bulkRequest.add(irb);
    }

    /**
     * 将最近的N条服务入口链缓存，便于实时查询
     * 
     * @param appUUID
     * @param span
     */
    private void pushLatestIVCDataToCache(String appUUID, Span span) {

        // NOT Implement NOW
    }

    @Override
    public boolean isHandleable(String topic) {

        return "JQ_IVC".equalsIgnoreCase(topic);
    }

}
