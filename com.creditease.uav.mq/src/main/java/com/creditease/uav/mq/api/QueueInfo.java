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

package com.creditease.uav.mq.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class QueueInfo {

    public QueueInfo(MQFactory.QueueType queueType) {
        this.queueType = queueType;
    }

    public Map<String, String[]> getTopics() {

        return topics;
    }

    public void addTopic(String topic, String... tags) {

        // 1: if there is topic & tags==null, do nothing
        if (topics.containsKey(topic) && null == tags) {
            return;
        }

        // 2: should be no contain this topic and tags==null
        if (null == tags) {
            topics.put(topic, new String[0]);
            return;
        }

        // 3: should be tags!=null, the topic is still unknown
        List<String> tagsOnTopicList = new ArrayList<String>();

        // 3.1: get the existing tags first if exists
        String[] tagsOnTopic = topics.get(topic);
        if (null != tagsOnTopic) {
            for (String tag : tagsOnTopic) {
                tagsOnTopicList.add(tag);
            }
        }

        // 3.2: merging the tags
        for (String tag : tags) {
            if (!tagsOnTopicList.contains(tag)) {
                tagsOnTopicList.add(tag);
            }
        }

        // 3.3 put the tags to topic
        tagsOnTopic = new String[tagsOnTopicList.size()];
        tagsOnTopicList.toArray(tagsOnTopic);
        topics.put(topic, tagsOnTopic);
    }

    public MQFactory.QueueType getQueueType() {

        return queueType;
    }

    // 要监听的主题
    private Map<String, String[]> topics = new HashMap<String, String[]>();

    // 1点对点，2广播
    private MQFactory.QueueType queueType;
}
