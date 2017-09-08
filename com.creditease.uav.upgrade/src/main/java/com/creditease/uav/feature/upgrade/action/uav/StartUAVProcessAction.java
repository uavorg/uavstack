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

package com.creditease.uav.feature.upgrade.action.uav;

import java.util.ArrayList;
import java.util.List;

import com.creditease.agent.helpers.RuntimeHelper;
import com.creditease.agent.helpers.ThreadHelper;
import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.uav.feature.upgrade.beans.TargetProcess;
import com.creditease.uav.feature.upgrade.beans.UpgradeContext;
import com.creditease.uav.feature.upgrade.beans.UpgradeContext.UpgradePhase;
import com.creditease.uav.feature.upgrade.common.UpgradeConstants;
import com.creditease.uav.feature.upgrade.common.UpgradeUtil;
import com.creditease.uav.feature.upgrade.exception.UpgradeException;

public class StartUAVProcessAction extends AbstractUAVProcessAction {

    public StartUAVProcessAction(String feature, UpgradeContext upgradeContext, IActionEngine engine) {
        super(StartUAVProcessAction.class.getSimpleName(), feature, upgradeContext, engine);
    }

    @Override
    public void doAction(ActionContext context) throws Exception {

        setUpgradePhase(UpgradePhase.PROCESS_START);

        List<TargetProcess> processList = getProcessList();
        int size = processList.size();
        float processIndex = 1;
        for (TargetProcess process : processList) {

            if (UpgradeUtil.isUAVProcessAlive(process)) {
                if (log.isTraceEnable()) {
                    log.info(this, process + " is already alive, no need to start");
                }

                continue;
            }

            startProcess(process);

            // Sleep 3 seconds to finish starting
            ThreadHelper.suspend(3000);

            if (!UpgradeUtil.isUAVProcessAlive(process)) {
                throw new UpgradeException("Failed to start profile: " + process.getProfileName());
            }

            if (this.upgradeContext.canWriteOperationRecord()) {
                this.writeProcessRecord(this.cName, process, processIndex / size,
                        UpgradeUtil.getUAVProcessJsonStrList(processList));
            }

            if (log.isTraceEnable()) {
                log.info(this, "Profile: " + process.getProfileName() + " was restarted successfully");
            }

            processIndex++;
        }

        context.setSucessful(true);

    }

    @Override
    public String getSuccessNextActionId() {

        return UpgradeConstants.getActionName(false, UpgradeConstants.END_ACTION_KEY);
    }

    @Override
    public String getFailureNextActionId() {

        if (this.upgradeContext.isRollback()) {
            return UpgradeConstants.getActionName(false, UpgradeConstants.END_ACTION_KEY);
        }

        this.upgradeContext.setRollback(true);
        return UpgradeConstants.getActionName(false, UpgradeConstants.STOP_UAV_PROCESS_ACTION_KEY);
    }

    @Override
    public String getExceptionNextActionId() {

        return getFailureNextActionId();
    }

    /**
     * This method is to start the process to be upgraded
     * 
     * @param process
     * @throws Exception
     */
    private void startProcess(TargetProcess process) throws Exception {

        if (log.isTraceEnable()) {
            log.info(this, "Start uav process with profile: " + process.getProfileName() + " using "
                    + process.getStartScriptName());
        }

        String[] osExecuteCmd = getOSExecuteCmd();
        RuntimeHelper.exec(UpgradeConstants.DEFAULT_CMD_EXEC_TIME, new String[] { osExecuteCmd[0], osExecuteCmd[1],
                UpgradeUtil.constructScriptCmd(this.getBinPath().toString(), process.getStartScriptName()) });
    }

    @Override
    public List<TargetProcess> getProcessList() {

        if (this.processList != null) {
            return this.processList;
        }

        List<TargetProcess> resultList = new ArrayList<TargetProcess>();

        for (TargetProcess process : this.upgradeContext.getAffectedProcessList()) {
            if (process.getProfileName().contains("upgrade")) {
                if (log.isTraceEnable()) {
                    log.info(this, "Ignore start action for profile: " + process.getProfileName());
                }

                continue;
            }

            resultList.add(process);
        }

        return resultList;
    }

}
