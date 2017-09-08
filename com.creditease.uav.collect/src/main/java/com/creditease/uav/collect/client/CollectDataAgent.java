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

package com.creditease.uav.collect.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.spi.AbstractTimerWork;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.agent.spi.IConfigurationManager;
import com.creditease.uav.collect.client.actions.CollectNodeOperAction;
import com.creditease.uav.collect.client.collectdata.CollectTask;
import com.creditease.uav.collect.client.collectdata.DataCollector;

/**
 * "collectdata.add" simple input:<br>
 * {tasks: [ {"target": "http://127.0.0.1:8080/app", "action": "SLOWOPS", "file": "/app/slowops.out", "topic":
 * "SLOWTOPIC"} ]}
 * 
 * <p>
 * if need callback:<br>
 * {<br>
 * tasks: [<br>
 * &nbsp;&nbsp;{"target": "http://127.0.0.1:8080/app", "action": "SLOWOPS", "file": "/app/slowops.out", "topic":
 * "SLOWTOPIC"}<br>
 * ],<br>
 * callback: {<br>
 * &nbsp;&nbsp;feature: 'featureName',<br>
 * &nbsp;&nbsp;component: 'componentName', <br>
 * &nbsp;&nbsp;eventKey: 'eventKeyName'<br>
 * }<br>
 * }<br>
 * callback invoke the component's exchange method with the same json data, if task has failed, failed tasks will be
 * passed by exchange data at the second argument
 * 
 * 
 * @author zhuang
 *
 */
public class CollectDataAgent extends AgentFeatureComponent {

    private static final String POSITION_FILE = "position.cache";
    private static final String TASK_FILE = "task.cache";

    private String positionFilePath;
    private String taskFilePath;

    private Map<String, TaskContext> ctxCaching = new ConcurrentHashMap<>();

    private DataCollector dc;

    /**
     * timer to invoke file scan
     */
    private static class FileScanScheduler extends AbstractTimerWork {

        public FileScanScheduler(String cName, String feature) {
            super(cName, feature);
        }

        @Override
        public void run() {

            ((DataCollector) getConfigManager().getComponent(feature, DataCollector.class.getName())).scan();
        }

    }

    /**
     * write a position file which has the last read position of each file.
     */
    private class PositionPersistence extends AbstractTimerWork {

        public PositionPersistence(String cName, String feature) {
            super(cName, feature);
        }

        @Override
        public void run() {

            writePositions();
        }
    }

    /**
     * check idle file , then close it.
     */
    private class IdleFileChecker extends AbstractTimerWork {

        public IdleFileChecker(String cName, String feature) {
            super(cName, feature);
        }

        @Override
        public void run() {

            dc.checkIdleFile();
        }

    }

    public CollectDataAgent(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public void start() {

        // init position file. If file is not exists, create it
        String rootMetaPath = this.getConfigManager().getContext(IConfigurationManager.METADATAPATH) + "collectdata";
        IOHelper.createFolder(rootMetaPath);
        positionFilePath = rootMetaPath + File.separator + POSITION_FILE;
        taskFilePath = rootMetaPath + File.separator + TASK_FILE;
        initFiles(new String[] { positionFilePath, taskFilePath });

        // register CollectNodeOperAction
        IActionEngine engine = this.getActionEngineMgr().getActionEngine("NodeOperActionEngine");
        new CollectNodeOperAction("collectdata", feature, engine);

        // init DataCollector
        dc = new DataCollector(DataCollector.class.getName(), feature);
        List<CollectTask> tasks = loadTasks();
        dc.init(tasks);
        dc.loadPositions(IOHelper.readTxtFile(positionFilePath, "UTF-8"));

        // start data scaner
        long scanInterval = DataConvertHelper.toLong(getConfigManager().getFeatureConfiguration(feature, "interval"),
                3000L);
        FileScanScheduler scanner = new FileScanScheduler(FileScanScheduler.class.getName(), feature);
        getTimerWorkManager().scheduleWork(FileScanScheduler.class.getName(), scanner, 0, scanInterval);

        // write position file scheduler
        long writePosDelay = DataConvertHelper
                .toLong(getConfigManager().getFeatureConfiguration(feature, "writeposdelay"), 5000L);
        long writePosInterval = DataConvertHelper
                .toLong(getConfigManager().getFeatureConfiguration(feature, "writeposinterval"), 3000L);
        PositionPersistence position = new PositionPersistence(PositionPersistence.class.getName(), feature);
        getTimerWorkManager().scheduleWork(PositionPersistence.class.getName(), position, writePosDelay,
                writePosInterval);

        // check idle file scheduler
        long idleCheckerDelay = DataConvertHelper
                .toLong(getConfigManager().getFeatureConfiguration(feature, "idlecheckerdelay"), 120000L);
        long idleCheckerInterval = DataConvertHelper
                .toLong(getConfigManager().getFeatureConfiguration(feature, "idlecheckerinterval"), 5000L);
        IdleFileChecker idleChecker = new IdleFileChecker(IdleFileChecker.class.getName(), feature);
        getTimerWorkManager().scheduleWork(IdleFileChecker.class.getName(), idleChecker, idleCheckerDelay,
                idleCheckerInterval);

        log.info(this, "CollectDataAgent Started. Config: interval=" + scanInterval);
    }

    @Override
    public void stop() {

        // 1 cancel file scan worker
        getTimerWorkManager().cancel(FileScanScheduler.class.getName());

        getTimerWorkManager().cancel(PositionPersistence.class.getName());

        getTimerWorkManager().cancel(IdleFileChecker.class.getName());

        //
        writePositions();

        // 2 close dc & unregist
        dc.close();

        // 3 unregist this feature
        super.stop();
    }

    @Override
    public Object exchange(String eventKey, Object... data) {

        if (log.isDebugEnable()) {
            log.debug(this, "exchange: eventKey=" + eventKey + ", data=" + Arrays.toString(data));
        }

        if ("collectdata.add".equals(eventKey)) {
            TaskContext ctx = new TaskContext((String) data[0]);
            if (ctx.hasCallback()) {
                ctxCaching.put(ctx.id, ctx);
            }
            for (CollectTask task : ctx.tasks) {
                dc.add(task);
            }

            persistTasks();
        }

        else if ("collectdata.del".equals(eventKey)) {
            TaskContext ctx = new TaskContext((String) data[0]);
            for (CollectTask task : ctx.tasks) {
                dc.delete(task);
            }

            persistTasks();
        }

        else if ("collectdata.status".equals(eventKey)) {
            return JSONHelper.toString(dc.tasks());
        }

        else {
            throw new UnsupportedOperationException("Unsupported Operation: " + eventKey);
        }
        return null;
    }

    public void taskEof(CollectTask task) {

        TaskContext ctx = ctxCaching.get(task.getCtxid());
        if (ctx == null) {
            return;
        }

        ctx.taskEnd();
    }

    public void taskFailed(CollectTask task) {

        TaskContext ctx = ctxCaching.get(task.getCtxid());
        if (ctx == null) {
            return;
        }
        ctx.taskFail(task);
    }

    private void doCallback(final String feature, final String component, final String eventKey, final String data,
            final List<CollectTask> fails) {

        if (log.isDebugEnable()) {
            log.debug(this, "task context callback feature=" + feature + ", component=" + component + ", eventKey="
                    + eventKey + ", data=" + data + ", fails=" + fails.size());
        }

        final AgentFeatureComponent afc = (AgentFeatureComponent) getConfigManager().getComponent(feature, component);
        if (afc != null) {
            final String failTasks = fails.size() == 0 ? null : JSONHelper.toString(fails);
            new Thread(new Runnable() {

                @Override
                public void run() {

                    try {
                        afc.exchange(eventKey, data, failTasks);
                    }
                    catch (Exception e) {
                        log.err(this, "task callback FAILED. feature=" + feature + ", component=" + component
                                + ", eventKey=" + eventKey, e);
                    }
                }
            }).start();
        }
    }

    // TODO to refine
    private class TaskContext {

        private String id;

        private List<CollectTask> tasks = new ArrayList<>();
        private volatile AtomicInteger taskLatch;

        private List<CollectTask> fails = Collections.synchronizedList(new ArrayList<CollectTask>());

        private Map<String, String> callback;

        private String json;

        @SuppressWarnings("unchecked")
        public TaskContext(String json) {
            Map<String, Object> m = JSONHelper.toObject(json, Map.class);

            this.json = json;
            this.id = UUID.randomUUID().toString();
            this.tasks = JSONHelper.toObjectArray(m.get("tasks").toString(), CollectTask.class);
            this.taskLatch = new AtomicInteger(this.tasks.size());
            this.callback = (Map<String, String>) m.get("callback");
            for (CollectTask task : this.tasks) {
                task.setCtxid(this.id);
                task.setEofEvent(hasCallback());
            }

            if (log.isDebugEnable()) {
                log.debug(this, "New TaskContext: taskLatch=" + taskLatch.get() + ", hasCallback=" + hasCallback()
                        + ", json=" + json);
            }
        }

        public boolean hasCallback() {

            return callback != null;
        }

        public void taskEnd() {

            if (taskLatch.decrementAndGet() == 0) {
                for (CollectTask task : tasks) {
                    dc.delete(task);
                }
                ctxCaching.remove(id);

                persistTasks();

                if (hasCallback()) {
                    doCallback(callback.get("feature"), callback.get("component"), callback.get("eventKey"), json,
                            fails);
                }
            }
        }

        public void taskFail(CollectTask task) {

            fails.add(task);

            taskEnd();
        }
    }

    private void initFiles(String[] filePaths) {

        for (String filePath : filePaths) {
            File f = new File(filePath);
            if (!f.exists()) {
                try {
                    f.createNewFile();
                }
                catch (IOException e) {
                    log.err(this, "init file[" + filePath + "] FAILED.", e);
                }
            }
        }
    }

    private void writePositions() {

        String posJson = dc.getPositions();
        try {
            IOHelper.writeTxtFile(positionFilePath, posJson, "UTF-8", false);
        }
        catch (IOException e) {
            log.err(this, "Write position to file[" + positionFilePath + "] FAILED.", e);
        }
    }

    private void persistTasks() {

        String taskJson = JSONHelper.toString(dc.tasks());
        try {
            IOHelper.writeTxtFile(taskFilePath, taskJson, "UTF-8", false);
        }
        catch (IOException e) {
            log.err(this, "Write tasks to file[" + taskFilePath + "] FAILED.", e);
        }
    }

    private List<CollectTask> loadTasks() {

        String taskJson = IOHelper.readTxtFile(taskFilePath, "UTF-8");
        if (StringHelper.isEmpty(taskJson)) {
            return new ArrayList<>();
        }
        return JSONHelper.toObjectArray(taskJson, CollectTask.class);
    }
}
