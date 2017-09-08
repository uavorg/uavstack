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

package com.creditease.uav.profiling;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.uav.profiling.spi.ProfileElement;
import com.creditease.uav.profiling.spi.ProfileElementInstance;

public class StandardProfileElementInstance implements ProfileElementInstance {

    private final String id;
    private final Map<String, Object> values = new LinkedHashMap<String, Object>();
    private final ProfileElement pe;
    private int index;

    public StandardProfileElementInstance(ProfileElement pe, String id, int index) {
        this.id = id;
        this.pe = pe;
        this.index = index;
    }

    @Override
    public String toJSONString() {

        StringBuilder sb = new StringBuilder("{");

        sb.append("id:\"" + id + "\",values:{");

        Iterator<Entry<String, Object>> entry = values.entrySet().iterator();

        int count = 0;
        while (entry.hasNext()) {
            Entry<String, Object> e = entry.next();
            sb.append("\"" + e.getKey() + "\":");

            Object val = e.getValue();
            Class<?> valClass = val.getClass();
            if (AtomicLong.class.isAssignableFrom(valClass) || Long.class.isAssignableFrom(valClass)
                    || Integer.class.isAssignableFrom(valClass) || Double.class.isAssignableFrom(valClass)) {
                sb.append(val);
            }
            else if (String.class.isAssignableFrom(valClass)) {
                sb.append("\"" + val + "\"");
            }
            else {
                sb.append(JSONHelper.toString(val));
            }

            sb.append(",");
            count++;
        }

        if (count > 0) {
            sb = sb.deleteCharAt(sb.length() - 1);
        }

        sb.append("}");
        return sb.append("}").toString();
    }

    @Override
    public String getInstanceId() {

        return this.id;
    }

    @Override
    public void setValue(String key, Object value) {

        if (null == key || null == value) {
            return;
        }

        values.put(key, value);
    }

    @Override
    public Map<String, Object> getValues() {

        return values;
    }

    @Override
    public void destroy() {

        this.values.clear();
    }

    @Override
    public ProfileElement getProfileElement() {

        return this.pe;
    }

    @Override
    public int getIndex() {

        return this.index;
    }

}
