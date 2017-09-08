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

package com.creditease.agent.feature.logagent.far;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.creditease.agent.feature.logagent.TaildirSourceConfigurationConstants;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * syslog是早期的系统日志,记录linux系统启动及运行的过程中产生的信息。 后来又出现了Rsyslog和Syslog-ng，目前公司CentOS使用为rsyslog. filterregex:
 * [\\w\\s:]{15}(\\s*[\\p{Graph}]+){2}:\\s.*
 * 
 * @author 201211070016
 *
 */
public class SysLogFilterAndRule extends DefaultLogFilterAndRule {

    Splitter separator = Splitter.onPattern("\\p{Space}").trimResults();

    public SysLogFilterAndRule(String filterregex, String separator, JSONObject fields, int fieldNumber, int version) {
        super(filterregex, separator, fields, fieldNumber, version);
    }

    /**
     * 默认分为四段字：日期/主机名/调用名/消息
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Map doAnalysis(Map<String, String> header, String log) {

        Map<String, String> result = Maps.newHashMap();
        String temp3area = log.substring(15).trim();
        List<String> it = Lists.newArrayList(separator.limit(3).split(temp3area));
        result.put("date", log.substring(0, 15));
        result.put("host", it.get(0));
        result.put("pid", it.get(1));
        result.put("message", it.get(2));
        result.put("_lnum", header.get(TaildirSourceConfigurationConstants.READ_LINE_NUMBER));
        result.put("_timestamp", TaildirSourceConfigurationConstants.READ_TIMESTAMP);
        getMainlogs().add(result);
        return result;
    }

}
