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

package com.creditease.uav.feature.ida.tools;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.creditease.agent.feature.hbagent.node.NodeInfo;
import com.creditease.agent.helpers.DateTimeHelper;
import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.uav.feature.ida.BaseIDAHandler;

/**
 * 
 * ContainerPerfCatcher 通过容器画像找出那些高于某个性能阈值的进程，并输出到文件中（文件按天建立文件夹，每个小时一个文件）
 *
 */
public class ContainerPerfCatcher extends BaseIDAHandler {

    private Map<String, Double> thresholds = new LinkedHashMap<String, Double>();

    private String outputFolderPath = ".";

    public ContainerPerfCatcher(String cName, String feature) {
        super(cName, feature);

        String threshold = this.getConfigManager().getFeatureConfiguration(this.feature, this.cName + ".threshold");

        String[] thresholdExps = threshold.split(",");

        for (String thresholdExp : thresholdExps) {
            String[] thd = thresholdExp.split("=");

            Double d = Double.parseDouble(thd[1]);

            thresholds.put(thd[0], d);
        }

        outputFolderPath = this.getConfigManager().getFeatureConfiguration(this.feature, this.cName + ".root");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void handle(Object data) {

        if (!isEnable) {
            return;
        }

        Object[] da = (Object[]) data;

        /*
         * only handle NodeInfo
         */
        if (!NodeInfo.class.isAssignableFrom(da[0].getClass())) {
            return;
        }

        NodeInfo ni = (NodeInfo) da[0];

        String nodeProcsStr = ni.getInfo().get("node.procs");

        if (null == nodeProcsStr) {
            return;
        }

        Map procs = JSONHelper.toObject(nodeProcsStr, Map.class);

        for (Object pidObj : procs.values()) {

            Map proc = (Map) pidObj;

            Object tagsObj = proc.get("tags");

            if (tagsObj == null) {
                continue;
            }

            /**
             * check if any perf value reach the threshold
             */
            Map<String, String> tags = (Map<String, String>) tagsObj;

            boolean isReachThreshold = false;

            String thresHoldExp = "";

            double reachThresHoldVal = -1;

            for (String key : thresholds.keySet()) {

                if (tags.containsKey(key)) {

                    try {
                        double curD = Double.parseDouble(tags.get(key));
                        double d = thresholds.get(key);

                        if (curD >= d) {
                            isReachThreshold = true;
                            reachThresHoldVal = curD;
                            thresHoldExp = key + "=" + d;

                            break;
                        }
                    }
                    catch (NumberFormatException e) {
                        // ignore
                    }
                }
            }

            /**
             * ReachThreshold
             */
            if (isReachThreshold == false) {
                continue;
            }

            String when = DateTimeHelper.toStandardDateFormat(ni.getClientTimestamp());

            String dayFolder = this.outputFolderPath + "/"
                    + DateTimeHelper.toFormat("yyyy-MM-dd", ni.getClientTimestamp());

            IOHelper.createFolder(dayFolder);

            String hourFile = dayFolder + "/" + DateTimeHelper.toFormat("yyyy-MM-dd_HH", ni.getClientTimestamp())
                    + ".uav.ida.log";

            String log = when + "   " + thresHoldExp + "    " + reachThresHoldVal + "   "
                    + JSONHelper.toString(proc).replace("\r", "").replace("\n", "") + System.lineSeparator();

            try {
                IOHelper.writeTxtFile(hourFile, log, "utf-8", true);
            }
            catch (IOException e) {
                this.log.err(this, "WRITE IDA LOG FAIL.", e);
            }

        }

    }
}
