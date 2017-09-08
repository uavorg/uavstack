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

package com.creditease.agent.helpers.jvmtool;

import java.util.Properties;

public class JVMAgentInfo {

    private Properties agentProperties;
    private Properties systemProperties;
    private String id;
    private String JVMAccessURL;

    public JVMAgentInfo(String id, Properties agentProperties, Properties systemProperties) {
        this.id = id;
        this.agentProperties = agentProperties;
        this.systemProperties = systemProperties;
    }

    public Properties getAgentProperties() {

        return agentProperties;
    }

    public void setAgentProperties(Properties agentProperties) {

        this.agentProperties = agentProperties;
    }

    public Properties getSystemProperties() {

        return systemProperties;
    }

    public void setSystemProperties(Properties systemProperties) {

        this.systemProperties = systemProperties;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getJVMAccessURL() {

        return JVMAccessURL;
    }

    public void setJVMAccessURL(String jVMAccessURL) {

        JVMAccessURL = jVMAccessURL;
    }

}
