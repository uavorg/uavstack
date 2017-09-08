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

package com.creditease.agent.helpers;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

public class WebHttpHelper {

    /**
     * getCookie
     * 
     * @param conn
     * @return
     */
    public static String getCookie(HttpURLConnection conn) {

        Map<String, List<String>> resHeaders = conn.getHeaderFields();
        StringBuffer sBuffer = new StringBuffer();
        for (Map.Entry<String, List<String>> entry : resHeaders.entrySet()) {
            String name = entry.getKey();
            if (name == null)
                continue; // http/1.1 line
            List<String> values = entry.getValue();
            if (name.equalsIgnoreCase("Set-Cookie")) {
                for (String value : values) {
                    if (value == null) {
                        continue;
                    }
                    String cookie = value.substring(0, value.indexOf(";") + 1);
                    sBuffer.append(cookie);
                }
            }
        }
        if (sBuffer.length() > 0) {
            return sBuffer.toString();
        }
        return sBuffer.toString();
    }
}
