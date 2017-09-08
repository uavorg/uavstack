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

package com.creditease.monitor;

import java.io.IOException;

import com.creditease.agent.helpers.IOHelper;

/**
 * 
 * UAVServerInstaller description: 安装器
 *
 */
public class UAVServerInstaller {

    public static void main(String[] args) {

        String userHomePath = System.getProperty("user.home");
        print("Start to install UAV MonitorFramework...");
        print("Current User Home:" + userHomePath);

        String uavMOLocation = userHomePath + "/uavmof.location";

        String uavMOFRoot = IOHelper.getCurrentPath();

        try {
            IOHelper.writeTxtFile(uavMOLocation, uavMOFRoot, "utf-8", false);

            print("Write UAV MonitorFramework Root [" + uavMOFRoot + "] to Path [" + uavMOLocation + "]");
        }
        catch (IOException e) {
            print(e);
        }
    }

    private static void print(Object str) {

        System.out.println(str);
    }
}
