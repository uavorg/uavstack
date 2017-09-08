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

import com.creditease.agent.spi.AbstractTimerWork;

public class ProfileScheduleWorker extends AbstractTimerWork implements UnitTestInterface {

    public ProfileScheduleWorker(String cName, String feature) {
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

        // TODO Auto-generated method stub

    }

    @Override
    public String prepareTestData(String filepath) {

        // TODO Auto-generated method stub
        return null;
    }

}
