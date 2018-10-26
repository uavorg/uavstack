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
package com.creditease.uav.elasticsearch.client;

import com.creditease.uav.elasticsearch.index.ESIndexHelper;

/**
 * DoTestESIndex description: 测试ESIndexHelper类
 *
 */
public class DoTestESIndex {

    public static void main(String[] args) {

        String prefix = "apm";
        String time = "2017-9-6";
        long timestamp = 1504627200000l;// 2017-9-6 的时间戳
        // 获取本周的索引
        System.out.println("获取本周的索引：\t" + ESIndexHelper.getIndexOfWeek(prefix));
        // 获取本周前两周的索引
        System.out.println("获取本周前两周的索引：\t" + ESIndexHelper.getIndexOfWeek(prefix, -2));
        // 输出2017年9月6日那周的索引
        System.out.println("输出2017年9月6日那周的索引：(9.3)\t" + ESIndexHelper.getIndexOfWeek(prefix, time));
        // 输出 2017年9月6日的后两周的索引索引
        System.out.println("输出2017年9月6日当周的索引：(9.3)\t" + ESIndexHelper.getIndexOfWeekByMillis(prefix, timestamp));

        // 获取本周的索引
        System.out.println("获取本日的索引：\t" + ESIndexHelper.getIndexOfDay(prefix));
        // 获取本周前两周的索引
        System.out.println("获取本日前两天的索引：\t" + ESIndexHelper.getIndexOfDay(prefix, -2));
        // 输出9月6日那周的索引
        System.out.println("输出2017年9月6日那天的索引：\t" + ESIndexHelper.getIndexOfDay(prefix, time));
        // 输出9月6日那周的索引
        System.out.println("输出2017年9月6日当天的索引：\t" + ESIndexHelper.getIndexOfDayByMillis(prefix, timestamp));
    }

}
