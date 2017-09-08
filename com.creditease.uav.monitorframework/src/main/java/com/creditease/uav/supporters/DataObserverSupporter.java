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

package com.creditease.uav.supporters;

import com.creditease.monitor.datastore.DataObserver;
import com.creditease.uav.common.Supporter;

public class DataObserverSupporter extends Supporter {

    @Override
    public void start() {

        // init the data observer to publish data to tmp store
        DataObserver dataObserver = DataObserver.instance();

        if (null != System.getenv("IS_ULTRON") && true == Boolean.valueOf(System.getenv("IS_ULTRON"))) {
            dataObserver.start(DataObserver.WorkModel.HTTP);
            return;
        }

        String workModel = System.getProperty("com.creditease.uav.dataobserver.workmodel");

        if ("http".equals(workModel)) {
            dataObserver.start(DataObserver.WorkModel.HTTP);
        }
        else {
            dataObserver.start(DataObserver.WorkModel.JMX);
        }
    }

    @Override
    public void stop() {

        DataObserver dataObserver = DataObserver.instance();
        dataObserver.stop();

        super.stop();
    }

}
