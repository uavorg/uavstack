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

package com.creditease.agent.spi;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

public interface IConfigurationManager {

    public static final String ROOT = "rootpath";
    public static final String CONFIGPATH = "cfgpath";
    public static final String BINPATH = "binpath";
    public static final String PROFILENAME = "profilename";
    public static final String METADATAPATH = "metadatapath";
    public static final String NODEUUID = "nodeuuid";
    public static final String NODETYPE = "nodetype";
    public static final String NODEGROUP = "nodegroup";
    public static final String NODEAPPID = "nodeappid";
    public static final String NODEAPPNAME = "nodeappname";
    public static final String NODEAPPVERSION = "nodeappversion";

    public Object getComponent(String feature, String componentName);

    public Object getComponentResource(String resource, String objName);

    public void registerComponent(String feature, String componentName, Object instance);

    public void unregisterComponent(String feature, String componentName);

    public <T> Set<T> getComponents(Class<T> componentTypeFilter);

    public Set<Object> getComponents();

    public String getConfiguration(String configName);

    public <T> T getConfiguration(Class<T> clz, String configName);

    public Properties getConfigurations();

    public String getContext(String key);

    public String getFeatureConfiguration(String featureName, String key);

    public String getResourceConfiguration(String resourceName, String key);

    public void fireProfileConfigurationUpdateEvent(Map<String, String> config);

    public void fireSystemPropertiesUpdateEvent(Map<String, String> systemPros);

    public void unregisterFeatureComponents(String feature);

    public void init();
}
