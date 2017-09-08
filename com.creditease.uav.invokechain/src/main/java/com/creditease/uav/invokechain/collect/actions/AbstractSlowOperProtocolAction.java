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

package com.creditease.uav.invokechain.collect.actions;

import java.util.LinkedList;
import java.util.List;

import com.creditease.agent.helpers.EncodeHelper;
import com.creditease.agent.spi.AbstractBaseAction;
import com.creditease.agent.spi.IActionEngine;

public abstract class AbstractSlowOperProtocolAction extends AbstractBaseAction {

    public AbstractSlowOperProtocolAction(String cName, String feature, IActionEngine engine) {
        super(cName, feature, engine);
    }

    /**
     * 获取当前action处理的协议类型
     * 
     * @return
     */
    public abstract String getProtocolType();

    /**
     * 解析普通协议
     * 
     * 协议体样例：10.10.37.56_8080_1500288690772_35_1;1;0002;1;2;64;com.creditease.
     * monitorframework.fat.ivc.MyTestInjectObj@5620af9a;4;true;o;1;0
     * 
     * 协议解析遵循如下规则：
     * 
     * （1）以分号分割，前四位分别为traceId，spanId，epinfo；
     * 
     * （2）头部之后的分号之前为需要读取的长度，之后为具体内容。（方法级的返回参数使用o;分割）.
     * 
     * @param body
     * @return
     */
    protected List<String> analyzeProtocol(String content) {

        List<String> result = new LinkedList<>();
        // 解析协议头
        StringBuilder builder = new StringBuilder();
        int headPoint = 0;
        int bodyBegin = 0;
        for (int i = 0; i < content.length(); i++) {
            char item = content.charAt(i);
            if (item == ';') {
                result.add(builder.toString());
                builder.delete(0, builder.length());
                headPoint++;
                if (headPoint == 3) {
                    bodyBegin = i + 1;
                    break;
                }
            }
            else {
                builder.append(item);
            }
        }
        // 解析协议体
        for (int i = bodyBegin; i < content.length(); i++) {
            char item = content.charAt(i);
            if (item == ';') {
                int length = Integer.parseInt(builder.toString());
                builder.delete(0, builder.length());
                for (int j = 0; j < length; j++) {
                    builder.append(content.charAt(++i));
                }
                try {
                    result.add(EncodeHelper.urlDecode(builder.toString()));
                }
                catch (Exception e) {
                    result.add(builder.toString());
                }
                builder.delete(0, builder.length());
                i++;
                // 特殊解析一下方法级的
                if (i < content.length() && content.charAt(i + 1) == 'o') {
                    i = i + 2;
                }
            }
            else {
                builder.append(item);
            }
        }
        return result;
    }
}
