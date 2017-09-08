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

package com.creditease.uav.helper;

import com.creditease.agent.helpers.RuntimeHelper;

public class DoTestRuntimeHelper {

    public static void main(String[] args) {

        getWindowsConnections();

    }

    @SuppressWarnings("unused")
    private static void getLinuxConnections() {

        try {
            System.out.print(RuntimeHelper.exeShell("netstat -na|grep ESTABLISHED|wc -l", "/c/Users/shell"));
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void getWindowsConnections() {

        try {
            String str = RuntimeHelper.exec("netstat -s");
            int start = str.indexOf("Current Connections");
            str = str.substring(start);
            int end = str.indexOf("\n");
            str = str.substring(0, end);
            String[] conns = str.split("=");
            System.out.print(conns[1].trim());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
