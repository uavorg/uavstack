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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.creditease.uav.helpers.uuid.IdWorker;

public class CommonHelper {

    /**
     * 当可能存在多个key代表同一个含义时，又不确定哪个key会存在，可使用此方法 比如：-profile 或 -p都能返回 profile参数值 getValueFromSeqKeys(configMap,new
     * String[]{"-profile","-p"});
     * 
     * @param m
     * @param keys
     * @return
     */
    public static String getValueFromSeqKeys(Map<String, String> m, String[] keys) {

        if (null == keys) {
            return null;
        }

        for (String key : keys) {

            String val = m.get(key);

            if (null != val) {
                return val;
            }
        }

        return null;
    }

    /**
     * 获取UUID的生成器
     * 
     * @param workerId
     * @param dataCenterId
     * @return
     */
    public static IdWorker getUUIDWorker(int workerId, int dataCenterId) {

        return new IdWorker(workerId, dataCenterId);
    }

    /**
     * 匹配 str 中符合表达式 p的第一个
     * 
     * @param p
     * @param str
     * @return
     */
    public static String match(String p, String str) {

        Pattern pattern = Pattern.compile(p);
        Matcher m = pattern.matcher(str);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

}
