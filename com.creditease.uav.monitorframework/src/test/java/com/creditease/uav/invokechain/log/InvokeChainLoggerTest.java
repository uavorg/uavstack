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

package com.creditease.uav.invokechain.log;

import com.creditease.monitor.log.DataLogger;

public class InvokeChainLoggerTest {

    public static void main(String[] args) {

        DataLogger chainLogger = new DataLogger("test", "/app/uav1", "invokechain.%g.%u.log", 100000, 1000000, 20);
        for (int i = 0; i < 100; i++) {

            chainLogger.logData("test" + i);
        }
    }
}
