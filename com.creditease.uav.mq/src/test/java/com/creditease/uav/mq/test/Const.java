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

package com.creditease.uav.mq.test;

import com.creditease.uav.mq.api.MQFactory;
import com.creditease.uav.mq.api.QueueInfo;


public class Const {

    public static QueueInfo topicQueueInfo = new QueueInfo(MQFactory.QueueType.TOPIC);
    public static QueueInfo commonQueueInfo = new QueueInfo(MQFactory.QueueType.QUEUE);
    static {
        // 进行空值测试，如果没有填写queue类型一律按照一般信息算
        // commonQueueInfo.addTopic("aaa");
        // topicQueueInfo.addTopic("aaa");
    }
    public static String message = "message";

}
