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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.creditease.agent.feature.logagent.ReliableTaildirEventReader;
import com.creditease.agent.feature.logagent.api.LogFilterAndRule;
import com.creditease.agent.feature.logagent.event.Event;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * 
 * @author 201211070016
 *
 */
public class StackTraceLogFilterAndRule implements LogFilterAndRule {

    Pattern pattern = Pattern.compile("([A-Za-z_]\\w*)(\\.([A-Za-z_]\\w*))+");

    private StringBuffer stacktracelogs = new StringBuffer();

    private static final String INDEX = "stacktrace";

    @SuppressWarnings("rawtypes")
    @Override
    public Map doAnalysis(Map<String, String> header, String log) {

        stacktracelogs.append(log);
        return null;
    }

    @Override
    public boolean isMatch(ReliableTaildirEventReader reader, List<Event> events, int num, String log)
            throws IOException {

        // 如果最后一条日志符合stacktrace，则需要增加抓取，直到不符合stacktrace或日志文件结束
        List<Event> singleEvent = null;
        if (isHeadMatch(log) || isTailMatch(log)) {
            if (num == events.size() - 1) {
                singleEvent = reader.readEvents(1, false, false);
                // 为null说明文件结束
                if (!singleEvent.isEmpty()) {
                    events.add(singleEvent.get(0));
                }
            }
            return true;
        }
        return false;
    }

    public boolean isTailMatch(String log) {

        // startWith "\tat " or "\t... " or "Caused by: "
        if (log.startsWith("\tat ") || log.startsWith("\t... ") || log.startsWith("Caused by: ")) {
            return true;
        }
        return false;
    }

    public boolean isHeadMatch(String log) {

        // startWith clazzname
        int index = log.indexOf(": ");
        if (index <= 0)
            return false;
        String clazz = log.substring(0, index);
        if (pattern.matcher(clazz).matches()) {
            return true;
        }
        return false;
    }

    /**
     * 穿成串
     */
    @SuppressWarnings({ "rawtypes" })
    @Override
    public List<Map> getResult(boolean isNeed) {

        List<Map> list = Lists.newArrayListWithCapacity(1);
        if (isNeed && stacktracelogs.length() > 0) {
            list.add(ImmutableMap.of(INDEX, stacktracelogs.toString()));
            stacktracelogs.setLength(0);
        }
        return list;
    }

    @Override
    public int getVersion() {

        return 0;
    }

}
