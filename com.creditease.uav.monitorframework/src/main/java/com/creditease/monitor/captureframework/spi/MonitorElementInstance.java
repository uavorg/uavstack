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

package com.creditease.monitor.captureframework.spi;

import java.util.Map;

public interface MonitorElementInstance {

    public enum CompareSetOperation {
        MAX, MIN
    }

    /**
     * get instance id
     * 
     * @return
     */
    public String getInstanceId();

    /**
     * get all values of this instance
     * 
     * @return
     */
    public Map<String, Object> getValues();

    /**
     * set or add new value to the instance
     * 
     * @param key
     * @param value
     */
    public void setValue(String key, Object value);

    /**
     * increment a value NOTE: this operation is thread safe
     * 
     * @param key
     */
    public long increValue(String key);

    /**
     * add a number to the original value NOTE: this operation is thread safe
     * 
     * @param key
     * @param addValue
     */
    public long sumValue(String key, long addValue);

    /**
     * compare the new value to the old value, if MAX, the larger value is saved if MIN, the smaller value is saved
     * NOTE: this operation is thread safe
     * 
     * @param key
     * @param newValue
     * @param operation
     * @return if set is SUCCESS
     */
    public boolean compareSet(String key, long newValue, CompareSetOperation operation);

    /**
     * get an instance value
     * 
     * @param key
     * @return
     */
    public Object getValue(String key);

    /**
     * get a value as long
     * 
     * @param key
     * @return
     */
    public long getValueLong(String key);

    /**
     * marshall to JSON string
     * 
     * @return
     */
    public String toJSONString();

    /**
     * destroy instance
     */
    public void destroy();

    /**
     * get monitor element
     * 
     * @return
     */
    public MonitorElement getMonitorElement();
}
