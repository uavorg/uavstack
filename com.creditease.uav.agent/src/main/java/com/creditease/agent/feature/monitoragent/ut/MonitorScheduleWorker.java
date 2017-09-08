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

package com.creditease.agent.feature.monitoragent.ut;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.agent.spi.AbstractTimerWork;
import com.creditease.uav.messaging.api.Message;
import com.creditease.uav.messaging.api.MessageProducer;
import com.creditease.uav.messaging.api.MessagingFactory;

public class MonitorScheduleWorker extends AbstractTimerWork implements UnitTestInterface {

    public static final String testcase1 = "../../utdata/....";

    public MonitorScheduleWorker(String cName, String feature) {
        super(cName, feature);
    }

    @Override
    public void run() {

        /**
         * 执行测试executeTestCase
         */
        executeTestCase();
    }

    @Override
    public void executeTestCase() {

        MessageProducer producer = (MessageProducer) this.getComponentResource("messageproducer",
                "MessageProducerResourceComponent");
        // submit Monitor data
        Message msg = MessagingFactory.createMessage(MonitorDataFrame.MessageType.Monitor.toString());

        String stream = prepareTestData(testcase1);

        msg.setParam(MonitorDataFrame.MessageType.Monitor.toString(), stream);
        boolean check = producer.submit(msg);

        String sendState = "Monitor" + (check ? "SUCCESS" : "FAIL");
        if (log.isTraceEnable()) {
            log.info(this, sendState + "    " + stream);
        }

    }

    @Override
    public String prepareTestData(String filepath) {

        StringBuffer buffer = new StringBuffer();

        try {

            File file = new File(filepath);
            BufferedReader reader = null;
            reader = new BufferedReader(new FileReader(file));

            String tempString = null;
            while ((tempString = reader.readLine()) != null) {

                buffer.append(tempString + "\n");
            }
            reader.close();
        }
        catch (IOException e) {
            // ingore
        }

        String rawData = buffer.toString();

        return rawData;
    }
}
