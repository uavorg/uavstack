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

package com.creditease.uav.notifycenter.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.spi.AgentFeatureComponent;
import com.creditease.uav.cache.api.CacheManager;

public class DoTestLoadNCCache {

    static String notifyJson = "src\\test\\java\\com\\creditease\\uav\\notifycenter\\test\\notify.json";

    public static void main(String[] args) {

        SystemLogger.init("DEBUG", true, 5);

        CacheManager.build("localhost:6379", 5, 5, 5);
        pushEventoNC();
    }

    public static void pushEventoNC() {

        AgentFeatureComponent afc = (AgentFeatureComponent) ConfigurationManager.getInstance()
                .getComponent("notifycenter", "NotificationCenter");

        String rawData = getData(notifyJson);

        afc.exchange("notify.center.put", rawData);

    }

    public static String getData(String fireDir) {

        StringBuffer buffer = new StringBuffer();

        try {

            File file = new File(fireDir);
            BufferedReader reader = null;
            reader = new BufferedReader(new FileReader(file));

            String tempString = null;
            while ((tempString = reader.readLine()) != null) {

                buffer.append(tempString + "\n");
            }
            reader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        String rawData = buffer.toString();

        return rawData;
    }

}
