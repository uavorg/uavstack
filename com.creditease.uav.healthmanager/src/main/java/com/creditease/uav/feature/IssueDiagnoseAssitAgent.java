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

package com.creditease.uav.feature;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.uav.feature.ida.DataWormHoleWorker;

/**
 * IssueDiagnoseAssitAgent
 * 
 * 整合了一堆小工具，可以将特定的信息如（容器画像，MonitorData，ProfileData等）进行某种帮助问题分析的处理，将处理结果输出到日志
 * 
 * 小工具1（ContainerPerfCatcher）：通过容器画像找出那些高于某个性能阈值的进程，并输出到文件中（文件按天建立文件夹，每个小时一个文件）
 */
public class IssueDiagnoseAssitAgent extends AgentFeatureComponent {

    private DataWormHoleWorker wdh;

    private ExecutorService wdhThread;

    public IssueDiagnoseAssitAgent(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public void start() {

        wdh = new DataWormHoleWorker("DataWormHoleWorker", this.feature, "dwhhandlers");

        wdhThread = Executors.newSingleThreadExecutor();

        wdhThread.execute(wdh);

        if (log.isTraceEnable()) {
            log.info(this, "DataWormHoleWorker started");
        }
    }

    @Override
    public void stop() {

        if (wdhThread != null && !wdhThread.isTerminated()) {
            wdhThread.shutdown();
        }

        if (log.isTraceEnable()) {
            log.info(this, "DataWormHoleWorker stopped");
        }
        super.stop();
    }

    @Override
    public Object exchange(String eventKey, Object... data) {

        switch (eventKey) {
            case "ida.put.data":

                /**
                 * put data into DataWormHole
                 */
                wdh.putData(data);

                return Boolean.TRUE;
        }

        return null;
    }

}
