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

package com.creditease.monitor.jee.servlet30;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletRegistration;

import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.uav.common.BaseComponent;
import com.creditease.uav.profiling.handlers.ComponentProfileHandler;
import com.creditease.uav.profiling.spi.ProfileContext;
import com.creditease.uav.profiling.spi.ProfileElementInstance;

public class DynamicServletConfigProfiler extends BaseComponent {

    @SuppressWarnings("unchecked")
    public void loadComponentsByDynamic(InterceptContext itContext, String componentClassName,
            ProfileElementInstance inst, ProfileContext context) {

        if (componentClassName.equals("javax.servlet.annotation.WebServlet")) {
            // dynamic servlets
            List<ServletRegistration.Dynamic> servlets = (List<ServletRegistration.Dynamic>) itContext
                    .get("dyn.servlets");

            if (servlets != null) {
                for (ServletRegistration.Dynamic d : servlets) {

                    String className = d.getClassName();

                    Map<String, Object> info = (Map<String, Object>) inst.getValues().get(className);

                    if (null == info) {
                        info = new LinkedHashMap<String, Object>();
                        inst.setValue(className, info);
                    }

                    Map<String, Object> config = new LinkedHashMap<String, Object>();

                    info.put("dyn", config);

                    config.put("name", d.getName());
                    config.put("urlPatterns", d.getMappings());

                    if (d.getInitParameters() != null && d.getInitParameters().size() > 0) {
                        config.put("initParams", d.getInitParameters());
                    }

                    ComponentProfileHandler.WebServletInfoProcessor.figureOutServiceEngine(className, context, info);
                }
            }
        }

        if (componentClassName.equals("javax.servlet.annotation.WebFilter")) {
            // dynamic filters
            List<FilterRegistration.Dynamic> filters = (List<FilterRegistration.Dynamic>) itContext.get("dyn.filters");

            if (filters != null) {
                for (FilterRegistration.Dynamic d : filters) {

                    String className = d.getClassName();

                    Map<String, Object> info = (Map<String, Object>) inst.getValues().get(className);

                    if (null == info) {
                        info = new LinkedHashMap<String, Object>();
                        inst.setValue(className, info);
                    }

                    Map<String, Object> config = new LinkedHashMap<String, Object>();

                    info.put("dyn", config);

                    config.put("filterName", d.getName());
                    config.put("servletNames", d.getServletNameMappings());
                    config.put("urlPatterns", d.getUrlPatternMappings());
                }
            }
        }

        if (componentClassName.equals("javax.servlet.annotation.WebListener")) {
            // dynamic listeners
            List<String> listeners = (List<String>) itContext.get("dyn.listeners");

            if (listeners != null) {
                for (String d : listeners) {
                    String className = d;
                    Map<String, Object> info = (Map<String, Object>) inst.getValues().get(className);

                    if (null == info) {
                        info = new LinkedHashMap<String, Object>();
                        inst.setValue(className, info);
                    }

                    info.put("dyn", new HashMap<String, Object>());
                }
            }
        }
    }
}
