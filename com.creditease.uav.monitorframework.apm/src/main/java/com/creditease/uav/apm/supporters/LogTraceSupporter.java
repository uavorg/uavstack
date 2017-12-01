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

package com.creditease.uav.apm.supporters;

import com.creditease.uav.common.Supporter;

/**
 * 
 * LogTraceSupporter description: 日志追踪supporter
 * 
 * 当前supporter作为一个开关，具体实现依附于调用链，并由调用链提供数据支持
 *
 */
public class LogTraceSupporter extends Supporter {

    @Override
    public void start() {

        // do nothing
    }

    @Override
    public void stop() {

        super.stop();
    }

}
