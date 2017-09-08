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

package com.creditease.agent.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.creditease.agent.feature.hbagent.HeartBeatClientReqWorker;
import com.creditease.agent.feature.hbagent.HeartBeatEventClientWorker;
import com.creditease.agent.spi.AgentFeatureComponent;

/**
 * HeartBeatAgent is helping to processing heart beat
 * 
 * @author zhen zhang
 *
 */
public class HeartBeatClientAgent extends AgentFeatureComponent {

    private ExecutorService hbEventClientWorker_es = Executors.newSingleThreadExecutor();

    private HeartBeatClientReqWorker hbclientReqWorker;

    /**
     * 子node的NodeInfo信息
     */
    private BlockingQueue<List<String>> nodeInfoQueue = new LinkedBlockingQueue<List<String>>();

    /**
     * 本node额外补充的信息
     */
    private Map<String, Object> nodeExtInfos = new ConcurrentHashMap<String, Object>();

    public HeartBeatClientAgent(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public void start() {

        // start HeartBeatEventClientWorker
        HeartBeatEventClientWorker hbEventClientWorker = new HeartBeatEventClientWorker("HeartBeatEventClientWorker",
                this.feature, "hbhandlers");

        hbEventClientWorker_es.execute(hbEventClientWorker);

        if (log.isTraceEnable()) {
            log.info(this, "HeartBeatEventClientWorker started");
        }

        hbclientReqWorker = new HeartBeatClientReqWorker("HeartBeatClientReqWorker", this.feature);

        long interval = Long.parseLong(this.getConfigManager().getFeatureConfiguration(this.feature, "interval"));

        long randomDely = new Random().nextInt(5) + 5;

        // start HeartBeatClientReqWorker
        this.getTimerWorkManager().scheduleWorkInPeriod("HeartBeatClientReqWorker", hbclientReqWorker,
                randomDely * 1000, interval);

        if (log.isTraceEnable()) {
            log.info(this, "HeartBeatClientReqWorker started");
        }
    }

    @Override
    public void stop() {

        // stop HeartBeatClientReqWorker
        this.getTimerWorkManager().cancel("HeartBeatClientReqWorker");

        if (log.isTraceEnable()) {
            log.info(this, "HeartBeatClientReqWorker stopped");
        }

        // stop HeartBeatEventClientWorker
        hbEventClientWorker_es.shutdownNow();

        if (log.isTraceEnable()) {
            log.info(this, "HeartBeatEventClientWorker stopped");
        }
        super.stop();
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public Object exchange(String eventKey, Object... data) {

        // ignore
        switch (eventKey) {
            case "hbclientagent.nodeinfo.upstream":
                putUpstreamNodeInfoIntoClientRequest((List<String>) data[0]);
                break;
            case "hbclientagent.nodeinfo.extinfo":
                Map<String, Object> extInfo = (Map<String, Object>) data[0];
                nodeExtInfos.putAll(extInfo);
                break;
            case "hbclientagent.service.discovery":
                return this.hbclientReqWorker.doServiceDiscovery((String) data[0]);
        }

        return null;
    }

    /**
     * upload the node info from non master hbserver
     * 
     * @param data
     */

    private void putUpstreamNodeInfoIntoClientRequest(List<String> data) {

        if (null == data || data.size() == 0) {
            return;
        }

        nodeInfoQueue.offer(data);
    }

    /**
     * pollNodeInfoQueue
     * 
     * @return
     */
    public List<List<String>> pollNodeInfoQueue() {

        List<List<String>> nodeInfoLists = new ArrayList<List<String>>();

        nodeInfoQueue.drainTo(nodeInfoLists);

        return nodeInfoLists;
    }

    /**
     * getNodeExtInfo
     * 
     * @return
     */
    public Map<String, Object> getNodeExtInfo() {

        return Collections.unmodifiableMap(this.nodeExtInfos);
    }

}
