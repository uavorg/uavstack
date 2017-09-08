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

package com.creditease.uav.feature.upgrade.action;

import java.nio.file.Path;

import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.IActionEngine;
import com.creditease.uav.feature.upgrade.beans.UpgradeContext;
import com.creditease.uav.feature.upgrade.beans.UpgradeContext.UpgradePhase;

public abstract class OverrideFileAction extends AbstractUpgradeBaseAction {

    public OverrideFileAction(String cName, String feature, UpgradeContext upgradeContext, IActionEngine engine) {
        super(cName, feature, upgradeContext, engine);
    }

    @Override
    public void doAction(ActionContext context) throws Exception {

        setUpgradePhase(UpgradePhase.OVERRIDE_FILE);
        overrideFiles(getSourcePath());
        context.setSucessful(true);

    }

    private Path getSourcePath() {

        Path path = null;
        if (this.upgradeContext.isRollback()) {
            path = this.getBackupZipPath();
        }
        else {
            path = this.getUpgradePackagePath();
        }

        return path;
    }

    protected abstract void overrideFiles(Path sourcePath) throws Exception;

}
