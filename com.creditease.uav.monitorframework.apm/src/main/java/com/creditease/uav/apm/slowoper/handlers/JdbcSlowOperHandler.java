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

package com.creditease.uav.apm.slowoper.handlers;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.uav.apm.slowoper.span.SlowOperSpan;
import com.creditease.uav.apm.slowoper.spi.SlowOperConstants;
import com.creditease.uav.apm.slowoper.spi.SlowOperContext;

/**
 * 
 * 处理http协议的client类型
 *
 */
public class JdbcSlowOperHandler extends AbstractSlowOperHandler {

    @Override
    public void buildSpanContent(SlowOperContext slowOperContext, SlowOperSpan slowOperSpan) {

        if (slowOperContext.containsKey(SlowOperConstants.PROTOCOL_JDBC_SQL)) {
            String sql = (String) slowOperContext.get(SlowOperConstants.PROTOCOL_JDBC_SQL);
            // 限定采集的协议体的大小
            int sqlLength = DataConvertHelper.toInt(System.getProperty("com.creditease.uav.ivcdat.jdbc.req"), 2000);
            slowOperSpan.appendContent(sql, sqlLength, true);
        }
        else {
            String result = slowOperContext.get(SlowOperConstants.PROTOCOL_JDBC_RESULT).toString();
            // 限定采集的协议体的大小
            int resultLength = DataConvertHelper.toInt(System.getProperty("com.creditease.uav.ivcdat.jdbc.ret"), 2000);
            slowOperSpan.appendContent(result, resultLength, true);
        }
    }
}
