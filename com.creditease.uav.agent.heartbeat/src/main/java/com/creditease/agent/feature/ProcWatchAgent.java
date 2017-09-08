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

import java.io.File;
import java.util.Map;

import com.creditease.agent.feature.procwatch.ProcWatcher;
import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.osproc.OSProcess;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.agent.spi.IConfigurationManager;

public class ProcWatchAgent extends AgentFeatureComponent {

    private ProcWatcher pw;

    public ProcWatchAgent(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public void start() {

        // mkdir ROOT/metadata/procwatch/procs.cache

        String shellPath = this.getConfigManager().getContext(IConfigurationManager.METADATAPATH);
        String cacheFileDir = this.getConfigManager().getContext(IConfigurationManager.METADATAPATH) + "procwatch";
        IOHelper.createFolder(cacheFileDir);
        String cacheFilePath = cacheFileDir + File.separator + "watch.cache";

        pw = new ProcWatcher(ProcWatcher.class.getSimpleName(), feature);
        pw.setShellPath(shellPath);
        pw.setCacheFilePath(cacheFilePath);
        pw.loadWatch();

        long interval = DataConvertHelper
                .toLong(this.getConfigManager().getFeatureConfiguration(this.feature, "interval"), 30000);

        getTimerWorkManager().scheduleWork(ProcWatcher.class.getSimpleName(), pw, 0, interval);
    }

    @Override
    public void stop() {

        pw.persistent();

        getTimerWorkManager().cancel(ProcWatcher.class.getSimpleName());
        super.stop();
    }

    @Override
    public Object exchange(String eventKey, Object... data) {

        if ("agent.procwatch.iswatch".equals(eventKey)) {

            String pid = (String) data[0];

            return pw.isWatching(pid);
        }
        else if ("agent.procwatch.refresh".equals(eventKey)) {
            @SuppressWarnings("unchecked")
            Map<String, OSProcess> procs = (Map<String, OSProcess>) data[0];

            pw.refresh(procs);

            if (log.isDebugEnable()) {
                log.debug(this, "ProcWatchAgent exchange refresh keys: " + JSONHelper.toString(procs.keySet()));
            }
        }
        else if ("agent.procwatch.watch".equals(eventKey)) {

            int result = pw.watch((String) data[0]);
            if (result == 1) {
                pw.persistent();
            }

            if (log.isDebugEnable()) {
                log.debug(this, "ProcWatchAgent exchange watch pid: " + (String) data[0]);
            }
            return result;
        }
        else if ("agent.procwatch.unwatch".equals(eventKey)) {

            int result = pw.unwatch((String) data[0]);
            if (result == 1) {
                pw.persistent();
            }

            if (log.isDebugEnable()) {
                log.debug(this, "ProcWatchAgent exchange unwatch pid: " + (String) data[0]);
            }
            return result;
        }
        else if ("agent.procwatch.restart".equals(eventKey)) {

            if (log.isDebugEnable()) {
                log.debug(this, "ProcWatchAgent exchange restart pid: " + (String) data[0]);
            }
            return restartProc((String) data[0]);
        }
        return null;
    }

    private int restartProc(String pid) {

        return pw.restart(pid);
    }
}
