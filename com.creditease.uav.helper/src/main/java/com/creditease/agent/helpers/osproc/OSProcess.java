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

package com.creditease.agent.helpers.osproc;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class OSProcess {

    // process id
    private String pid;

    // listening port
    private Set<String> ports = new HashSet<String>();

    // program name
    private String name = "UNKNOWN";

    // extract info to describe the process
    private Map<String, String> tags = new LinkedHashMap<String, String>();

    public String getPid() {

        return pid;
    }

    public void setPid(String pid) {

        this.pid = pid;
    }

    public void addPort(String port) {

        this.ports.add(port);
    }

    public Set<String> getPorts() {

        return ports;
    }

    public void setPorts(Set<String> ports) {

        this.ports = ports;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public void addTag(String key, String value) {

        this.tags.put(key, value);
    }

    public Map<String, String> getTags() {

        return tags;
    }

    public void setTags(Map<String, String> tags) {

        this.tags = tags;
    }

}
