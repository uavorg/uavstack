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

import java.util.List;

import com.creditease.agent.spi.IActionEngine;
import com.creditease.uav.feature.upgrade.action.AbstractUpgradeBaseAction;
import com.creditease.uav.feature.upgrade.beans.TargetProcess;
import com.creditease.uav.feature.upgrade.beans.UpgradeContext;

public abstract class AbstractUAVProcessAction extends AbstractUpgradeBaseAction {

    protected List<TargetProcess> processList;

    public AbstractUAVProcessAction(String cName, String feature, UpgradeContext upgradeContext, IActionEngine engine) {
        super(cName, feature, upgradeContext, engine);
    }

    public List<TargetProcess> getProcessList() {

        return processList;
    }

    public void setProcessList(List<TargetProcess> processList) {

        this.processList = processList;
    }
}
