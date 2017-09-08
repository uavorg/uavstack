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
package com.creditease.uav.collect.client.collectdata;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.creditease.agent.apm.api.CollectDataFrame;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.spi.AbstractComponent;
import com.creditease.agent.workqueue.SystemForkjoinWorker;
import com.creditease.uav.collect.client.CollectDataAgent;
import com.creditease.uav.collect.client.copylogagent.CopyOfProcessOfLogagent;
import com.creditease.uav.collect.client.copylogagent.CopyOfProcessOfLogagent.AppLogPatternInfoCollection;
import com.creditease.uav.collect.client.copylogagent.LogPatternInfo;
import com.creditease.uav.collect.client.copylogagent.LogPatternInfo.StateFlag;
import com.creditease.uav.collect.client.copylogagent.ReliableTaildirEventReader;
import com.creditease.uav.messaging.api.Message;
import com.creditease.uav.messaging.api.MessageProducer;
import com.creditease.uav.messaging.api.MessagingFactory;

public class DataCollector extends AbstractComponent {

    protected static final long MAX_TRANSFER_BYTE_LIMIT = 1L * 1024 * 1024; // 1MB

    private CopyOfProcessOfLogagent copy;
    private ReliableTaildirEventReader reader;

    private Map<String, CollectTask> tasks = new ConcurrentHashMap<>();

    public DataCollector(String cName, String feature) {
        super(cName, feature);
    }

    public void init() {

        try {
            reader = new ReliableTaildirEventReader.Builder().tasks(tasks)
                    .headerTable(new java.util.HashMap<String, String>()).skipToEnd(false).addByteOffset(false).build();
            boolean mutiThreadEnable = "true"
                    .equals(getConfigManager().getFeatureConfiguration(feature, "MutiThread.enable"));
            reader.setMutiThreadEnable(mutiThreadEnable);
            long readMaxByte = DataConvertHelper.toLong(
                    getConfigManager().getFeatureConfiguration(feature, "readMaxByte"), MAX_TRANSFER_BYTE_LIMIT);
            reader.setReadMaxByte(readMaxByte);

            int readLineSize = DataConvertHelper
                    .toInt(getConfigManager().getFeatureConfiguration(feature, "readlinesize"), 100);
            SystemForkjoinWorker executor = null;
            if (mutiThreadEnable) {
                executor = new SystemForkjoinWorker("TailLogProcessPoolWorker", feature, DataConvertHelper
                        .toInt(getConfigManager().getFeatureConfiguration(feature, "MutiThread.thread.max"), 2));
            }
            copy = new CopyOfProcessOfLogagent(this.log, reader, mutiThreadEnable, executor, readLineSize,
                    (int) readMaxByte);
        }
        catch (IOException e) {
            log.err(this, "Error instantiating ReliableTaildirEventReader", e);
            return;
        }
    }

    public void init(List<CollectTask> taskList) {

        init();

        if (taskList != null) {
            for (CollectTask task : taskList) {
                add(task);
            }
        }
    }

    public void close() {

        try {
            copy.close();
            reader.close();
        }
        catch (IOException ignore) {
            // ignore
        }
    }

    public void scan() {

        copy.process();
        // read
        // submit
    }

    public void add(CollectTask task) {

        tasks.put(getTaskId(task), task);

        // TODO to refine
        LogPatternInfo lpi = getLatestLogProfileDataMap().get(task.getTarget(), task.getAction());
        if (lpi == null) {
            lpi = new LogPatternInfo(task.getTarget(), task.getAction(), task.getFile());
            lpi.setFlag(StateFlag.NEWCOME);
            lpi.setTimeStamp(System.currentTimeMillis());
            lpi.setUnsplit(task.isUnsplit());
            getLatestLogProfileDataMap().put(lpi);
        }

        reader.updatelogs(tasks);
    }

    public void delete(CollectTask task) {

        tasks.remove(getTaskId(task));

        getLatestLogProfileDataMap().remove(getTaskId(task), task.getFile());
    }

    public Collection<CollectTask> tasks() {

        return tasks.values();
    }

    public void submit(CollectDataFrame frame) {

        if (log.isDebugEnable()) {
            log.debug(this,
                    String.format("submit frame: time=%s, ip=%s, target=%s, action=%s, file=%s, lines=%s, eof=%s",
                            frame.getTime(), frame.getIp(), frame.getTarget(), frame.getAction(), frame.getFile(),
                            frame.getLines().size(), frame.isEof()));
        }

        MessageProducer producer = (MessageProducer) getComponentResource("messageproducer",
                "MessageProducerResourceComponent");
        CollectTask task = tasks.get(genId(frame.getTarget(), frame.getAction()));

        if (task == null) {
            if (log.isDebugEnable()) {
                log.debug(this, "??? NO task");
            }
            return;
        }
        if (task.isEofOccur() && task.hasEofEvent()) {
            onEof(task);
            frame.setEof(true);
        }

        Message msg = MessagingFactory.createMessage(task.getTopic());
        msg.setParam(task.getTopic(), "[" + frame.toJSONString() + "]");
        boolean result = producer.submit(msg);
        if (!result) {
            String detail = log.isDebugEnable() ? "\n" + msg.toJSONString() : "";
            log.warn(this, "collect data sent FAILED. " + detail);
        }
    }

    private void onEof(CollectTask task) {

        CollectDataAgent cda = (CollectDataAgent) getConfigManager().getComponent("collectclient", "CollectDataAgent");
        cda.taskEof(task);
    }

    // ugly
    public void onTaskEof(String target, String action) {

        if (log.isDebugEnable()) {
            log.debug(this, "task eof: target=" + target + ", action=" + action);
        }

        CollectTask task = tasks.get(genId(target, action));

        if (task != null && task.hasEofEvent()) {
            task.setEofOccur(true);
        }
    }

    public void onTaskFail(String target, String action) {

        if (log.isDebugEnable()) {
            log.debug(this, "task fail: target=" + target + ", action=" + action);
        }

        CollectTask task = tasks.get(genId(target, action));
        if (task == null) {
            return;
        }
        CollectDataAgent cda = (CollectDataAgent) getConfigManager().getComponent("collectclient", "CollectDataAgent");
        cda.taskFailed(task);
    }

    private String getTaskId(CollectTask t) {

        return genId(t.getTarget(), t.getAction());
    }

    private String genId(String target, String action) {

        return target + "-" + action;
    }

    public AppLogPatternInfoCollection getLatestLogProfileDataMap() {

        return copy.getLatestLogProfileDataMap();
    }

    public String getPositions() {

        return copy.getPosInfoJson();
    }

    public void loadPositions(String posJson) {

        reader.loadPositions(posJson);
    }

    public void checkIdleFile() {

        copy.checkIdleFile();
    }
}

