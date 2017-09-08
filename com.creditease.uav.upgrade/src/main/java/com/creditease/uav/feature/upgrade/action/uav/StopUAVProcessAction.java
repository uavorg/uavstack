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

import com.creditease.agent.helpers.JVMToolHelper;
import com.creditease.agent.helpers.RuntimeHelper;
import com.creditease.agent.helpers.ThreadHelper;
import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.uav.feature.upgrade.beans.TargetProcess;
import com.creditease.uav.feature.upgrade.beans.UpgradeContext;
import com.creditease.uav.feature.upgrade.beans.UpgradeContext.UpgradePhase;
import com.creditease.uav.feature.upgrade.common.UpgradeConstants;
import com.creditease.uav.feature.upgrade.common.UpgradeUtil;

public class StopUAVProcessAction extends AbstractUAVProcessAction {

    public StopUAVProcessAction(String feature, UpgradeContext upgradeContext, IActionEngine engine) {
        super(StopUAVProcessAction.class.getSimpleName(), feature, upgradeContext, engine);
    }

    @Override
    public void doAction(ActionContext context) throws Exception {

        setUpgradePhase(UpgradePhase.PROCESS_STOP);

        List<TargetProcess> processList = getProcessList();
        int size = processList.size();
        float processIndex = 1;
        for (TargetProcess process : processList) {
            if (!UpgradeUtil.isUAVProcessAlive(process)) {
                if (log.isTraceEnable()) {
                    log.info(this, process + " is not alive, no need to stop it");
                }

                continue;
            }

            stopProcess(process);
            if (this.upgradeContext.canWriteOperationRecord()) {
                this.writeProcessRecord(this.cName, process, processIndex / size,
                        UpgradeUtil.getUAVProcessJsonStrList(processList));
            }

            processIndex++;
        }

        context.setSucessful(true);
    }

    @Override
    public String getSuccessNextActionId() {

        return UpgradeConstants.getActionName(false, UpgradeConstants.UAV_OVERRIDE_FILE_ACTION_KEY);
    }

    @Override
    public String getFailureNextActionId() {

        if (this.upgradeContext.isRollback()) {
            return UpgradeConstants.getActionName(false, UpgradeConstants.END_ACTION_KEY);
        }

        this.upgradeContext.setRollback(true);
        return UpgradeConstants.getActionName(false, UpgradeConstants.START_UAV_PROCESS_ACTION_KEY);
    }

    @Override
    public String getExceptionNextActionId() {

        return getFailureNextActionId();
    }

    /**
     * This method is to stop the process to be upgraded
     * 
     * @param process
     * @throws Exception
     */
    private void stopProcess(TargetProcess process) throws Exception {

        if (log.isTraceEnable()) {
            log.info(this, "Stop process:" + process.getProcessId() + " profile: " + process.getProfileName()
                    + " using " + process.getStopScriptName());
        }

        // step 1, check if the process is alive, if not, just return.
        if (!UpgradeUtil.isUAVProcessAlive(process)) {
            log.info(this, "No need to stop this porcess:" + process + ", because this process is not alive");
            return;
        }

        // step 2, shutdown process gracefully
        String[] osExecuteCmd = getOSExecuteCmd();
        RuntimeHelper.exec(UpgradeConstants.DEFAULT_CMD_EXEC_TIME, new String[] { osExecuteCmd[0], osExecuteCmd[1],
                UpgradeUtil.constructScriptCmd(this.getBinPath().toString(), process.getStopScriptName()) });

        // Sleep 5 seconds to wait the completion of stop script.
        ThreadHelper.suspend(5000);

        // step 3, if the process is still alive, shutdown it forcibly
        if (UpgradeUtil.isUAVProcessAlive(process)) {

            if (log.isTraceEnable()) {
                log.info(this, process.getProcessId() + " is still alive, start killing it forcibly");
            }

            if (JVMToolHelper.isWindows()) {
                RuntimeHelper.exec("Taskkill /F /PID " + process.getProcessId());
            }
            else {
                RuntimeHelper.exec("kill -9 " + process.getProcessId());
            }

            // Wait 3s to for killing process
            ThreadHelper.suspend(3000);
        }
    }

    @Override
    public List<TargetProcess> getProcessList() {

        if (this.processList != null) {
            return this.processList;
        }

        List<TargetProcess> resultList = new ArrayList<TargetProcess>();

        if (this.upgradeContext.isRollback()) {
            List<TargetProcess> currentProcessList = UpgradeUtil.findCurrentUAVTargetProcess();
            for (TargetProcess affectedProcess : this.upgradeContext.getAffectedProcessList()) {
                for (TargetProcess currentUAVProcess : currentProcessList) {
                    if (currentUAVProcess.getProfileName().equalsIgnoreCase(affectedProcess.getProfileName())) {
                        // refresh the process id of old process with current alive uav process's
                        affectedProcess.setProcessId(currentUAVProcess.getProcessId());
                        break;
                    }
                }
            }
        }

        for (TargetProcess process : this.upgradeContext.getAffectedProcessList()) {
            if (process.getProfileName().contains("upgrade")) { // ignore upgrade or upgrade_server process
                if (log.isTraceEnable()) {
                    log.info(this, "Ignore stop action for profile: " + process.getProfileName());
                }

                continue;
            }

            resultList.add(process);
        }

        return resultList;
    }

}
