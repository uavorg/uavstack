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

package com.creditease.uav.feature.upgrade.record;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.spi.AbstractComponent;
import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.agent.spi.IConfigurationManager;
import com.creditease.uav.feature.upgrade.action.EndAction;
import com.creditease.uav.feature.upgrade.action.OverrideFileAction;
import com.creditease.uav.feature.upgrade.action.thirdparty.ThirdpartySoftwareOverrideFileAction;
import com.creditease.uav.feature.upgrade.action.uav.StartUAVProcessAction;
import com.creditease.uav.feature.upgrade.action.uav.UAVOverrideFileAction;
import com.creditease.uav.feature.upgrade.beans.TargetProcess;
import com.creditease.uav.feature.upgrade.beans.UpgradeConfig;
import com.creditease.uav.feature.upgrade.beans.UpgradeContext;
import com.creditease.uav.feature.upgrade.beans.UpgradeContext.UpgradePhase;
import com.creditease.uav.feature.upgrade.common.UpgradeConstants;
import com.creditease.uav.feature.upgrade.common.UpgradeUtil;

/**
 * 
 * UpgradeOperationRecordConsumer description: Consume legacy upgrade operation record(if exists), trying to recover
 * interrupted upgrade process
 *
 */
public class UpgradeOperationRecordConsumer extends AbstractComponent {

    private List<UpgradeOperationRecord> recordList = new ArrayList<UpgradeOperationRecord>();
    private UpgradeContext upgradeContext;

    private IActionEngine engine;

    public UpgradeOperationRecordConsumer(String name, String feature, IActionEngine engine) {
        super(name, feature);
        this.engine = engine;
    }

    public void handleOperationRecord(String upgradeInfo) {

        EndAction endAction = new EndAction(this.feature, null, null);
        try {
            parseOldUpgradeRecord(feature, upgradeInfo);
        }
        catch (Exception e) {
            if (log.isTraceEnable()) {
                log.err(this, "Failed to parse upgrade record", e);
            }

            endAction.stopUpgradeProcess();
        }

        int length = this.recordList.size();
        if (length <= 1) {
            endAction.stopUpgradeProcess();
        }

        endAction = new EndAction(this.feature, this.upgradeContext, this.engine);
        if (!upgradeContext.getFileLock()) {
            if (log.isTraceEnable()) {
                log.warn(this, "Failed to get upgrade file lock");
            }

            endAction.stopUpgradeProcess();
        }

        UpgradeOperationRecord record = this.recordList.get(length - 1);

        if (upgradeContext.needRollback(record.getPhase())) {
            if (upgradeContext.isUAVContext()) {
                handleUAVOperationRecord(record, endAction);
            }
            else {
                handleThirdPartySoftwareOperationRecord(record);
            }

            return;
        }

        endAction.doAction(new ActionContext());
    }

    @SuppressWarnings("unchecked")
    private void handleThirdPartySoftwareOperationRecord(UpgradeOperationRecord oprRecord) {

        Map<String, Object> action = JSONHelper.toObject(String.valueOf(oprRecord.getAction()), Map.class);
        if (action.containsKey("override")) {
            JSONObject object = (JSONObject) action.get("override");
            String backupZip = String.valueOf(object.get("backup"));

            OverrideFileAction overrideFileAction = new ThirdpartySoftwareOverrideFileAction(this.feature,
                    upgradeContext, this.engine);
            overrideFileAction.setBackupZipPath(Paths.get(backupZip));

            engine.execute(overrideFileAction.getName(), new ActionContext());
        }
    }

    @SuppressWarnings("unchecked")
    private void handleUAVOperationRecord(UpgradeOperationRecord oprRecord, EndAction endAction) {

        Map<String, Object> action = JSONHelper.toObject(String.valueOf(oprRecord.getAction()), Map.class);
        if (UpgradePhase.PROCESS_STOP.toString().equalsIgnoreCase(oprRecord.getPhase())) {
            // will start all the process which have been stopped
            this.upgradeContext.setRollback(true);
            List<TargetProcess> processList = UpgradeUtil
                    .generateUAVProcessListFromJsonStrList((List<String>) action.get("processes"));

            StartUAVProcessAction startAction = new StartUAVProcessAction(this.feature, this.upgradeContext,
                    this.engine);
            startAction.setProcessList(processList);
            engine.execute(startAction.getName(), new ActionContext());
        }
        else if (UpgradePhase.OVERRIDE_FILE.toString().equalsIgnoreCase(oprRecord.getPhase())) {
            // will roll back
            this.upgradeContext.setRollback(true);
            JSONObject object = (JSONObject) action.get("override");
            String backupZip = String.valueOf(object.get("backup"));

            OverrideFileAction overrideFileAction = new UAVOverrideFileAction(this.feature, this.upgradeContext,
                    this.engine);
            overrideFileAction.setBackupZipPath(Paths.get(backupZip));

            List<UpgradeOperationRecord> recordList = getOperationRecordByPhase(UpgradePhase.PROCESS_STOP);
            Map<String, Object> actionMap = JSONHelper.toObject(String.valueOf(recordList.get(0).getAction()),
                    Map.class);
            List<TargetProcess> processList = UpgradeUtil
                    .generateUAVProcessListFromJsonStrList((List<String>) actionMap.get("processes"));

            StartUAVProcessAction startAction = new StartUAVProcessAction(this.feature, this.upgradeContext,
                    this.engine);
            startAction.setProcessList(processList);

            engine.execute(overrideFileAction.getName(), new ActionContext());
        }
        else if (UpgradePhase.PROCESS_START.toString().equalsIgnoreCase(oprRecord.getPhase())) {
            if (Float.valueOf(String.valueOf(action.get("ratio"))) == 1.0f) {
                // all the processes have been started successfully, so just finished the end action.
                engine.execute(endAction.getName(), new ActionContext());
            }
            else {
                // no all the processes were started successfully, so continue to start processes
                List<TargetProcess> totalProcessList = UpgradeUtil
                        .generateUAVProcessListFromJsonStrList((List<String>) action.get("processes"));

                List<TargetProcess> alreadyStartedProcessList = new ArrayList<TargetProcess>();
                List<UpgradeOperationRecord> recordList = getOperationRecordByPhase(UpgradePhase.PROCESS_START);
                for (UpgradeOperationRecord record : recordList) {
                    Map<String, Object> actionMap = JSONHelper.toObject(String.valueOf(record.getAction()), Map.class);
                    alreadyStartedProcessList.add(
                            JSONHelper.toObject(String.valueOf(actionMap.get("start_process")), TargetProcess.class));
                }

                // find the processes to be started
                List<TargetProcess> toStartProcessList = new ArrayList<TargetProcess>();
                boolean find = false;
                for (TargetProcess process : totalProcessList) {
                    String profile = process.getProfileName();
                    for (TargetProcess startedProcess : alreadyStartedProcessList) {
                        if (profile.equals(startedProcess.getProfileName())) {
                            find = true;
                            break;
                        }
                    }
                    if (!find) {
                        toStartProcessList.add(process);
                    }

                    find = false;
                }

                StartUAVProcessAction startAction = new StartUAVProcessAction(this.feature, this.upgradeContext,
                        this.engine);
                startAction.setProcessList(toStartProcessList);

                engine.execute(startAction.getName(), new ActionContext());
            }
        }
    }

    private void parseOldUpgradeRecord(String feature, String upgradeInfo) throws Exception {

        String rootDir = null;
        if (!StringHelper.isEmpty(upgradeInfo)) {
            UpgradeConfig config = new UpgradeConfig(upgradeInfo);
            if (config.isUAV()) {
                if (log.isTraceEnable()) {
                    log.info(this, "Parsing record: uav upgrade process");
                }

                rootDir = ConfigurationManager.getInstance().getContext(IConfigurationManager.ROOT);
            }
        }

        if (StringHelper.isEmpty(rootDir)) {
            throw new Exception("no root directory");
        }

        Path recordFilePath = Paths.get(rootDir.replace("\"", ""), UpgradeConstants.UPGRADE_RECORD_FILE_NAME);
        if (!Files.exists(recordFilePath)) {
            throw new Exception("no operation record file");
        }

        List<String> recordStrList = Files.readAllLines(recordFilePath, Charset.forName("utf-8"));

        // construct upgradeContext with the first line of record list, that is, the first record stores upgrade
        // parameters.
        this.upgradeContext = new UpgradeContext(new UpgradeConfig(recordStrList.remove(0)), true);
        for (String recordStr : recordStrList) {
            this.recordList.add(new UpgradeOperationRecord(recordStr));
        }
    }

    private List<UpgradeOperationRecord> getOperationRecordByPhase(UpgradePhase phase) {

        List<UpgradeOperationRecord> oprRecordList = new ArrayList<UpgradeOperationRecord>();
        for (UpgradeOperationRecord record : this.recordList) {
            if (phase.toString().equalsIgnoreCase(record.getPhase())) {
                oprRecordList.add(record);
            }
        }

        return oprRecordList;
    }

}
