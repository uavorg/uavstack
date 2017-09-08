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

import com.creditease.uav.mq.api.MQMessage;
import com.creditease.uav.mq.api.MQMessageListener;

import java.io.UnsupportedEncodingException;


public class MyHandler implements MQMessageListener {

    @Override
    public void handle(MQMessage message) {

        try {
            System.out.println("testmessage  is::" + new String(message.getMessage(), "utf8"));
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
        }
    }
}
