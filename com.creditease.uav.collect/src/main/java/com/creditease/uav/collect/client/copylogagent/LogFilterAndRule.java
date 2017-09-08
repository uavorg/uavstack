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

package com.creditease.uav.collect.client.copylogagent;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface LogFilterAndRule {

    @SuppressWarnings("rawtypes")
    public Map doAnalysis(Map<String, String> header, String log);

    // filter1 | filter2 | filter3
    public boolean isMatch(ReliableTaildirEventReader reader, List<Event> events, int num, String log)
            throws IOException;

    /**
     * 结束调用
     * 
     * @param isNeedCollection
     *            是否需要全批处理集合信息
     * @return
     */
    @SuppressWarnings("rawtypes")
    public List<Map> getResult(boolean isNeedCollection);

    /**
     * 取得当前规则的版本
     * 
     * @return
     */
    public int getVersion();

}
