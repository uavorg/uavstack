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

package com.creditease.uav.agent;

import java.util.Random;

import com.creditease.agent.helpers.ThreadHelper;
import com.creditease.agent.spi.ComponentMonitor;

public class DoTestMockAJavaMonitorInJSE {

    public static void main(String[] args) {

        ComponentMonitor cm = ComponentMonitor.getMonitor("test");

        Random r = new Random();

        cm.setMetricGroup("test_metric0", "testgp1");
        cm.setMetricGroup("test_metric1", "testgp1");
        cm.setMetricAggregation("test_metric0", ComponentMonitor.Aggregation.Avg);

        int count = 0;

        while (true) {

            if (count < 10) {
                cm.increValue("test_metric0");
            }
            cm.sumValue("test_metric1", r.nextInt(100));
            cm.sumValue("test_metric2", r.nextInt(20));

            cm.flushToSystemProperties();

            ThreadHelper.suspend(5000);

            for (Object key : System.getProperties().keySet()) {

                String skey = (String) key;

                if (skey.indexOf("mo@") > -1) {
                    System.out.println(skey + "=" + System.getProperty(skey));
                }
            }

            count++;

            if (count > 10) {
                cm.removeMetric("test_metric0");
            }
        }

    }

}
