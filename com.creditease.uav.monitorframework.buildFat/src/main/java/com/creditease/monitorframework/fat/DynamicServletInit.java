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

package com.creditease.monitorframework.fat;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebListener;

@WebListener
public class DynamicServletInit implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        ServletContext ctx = sce.getServletContext();

        ServletRegistration.Dynamic sd = ctx.addServlet("DynamicServlet",
                "com.creditease.monitorframework.fat.DynamicServlet");

        sd.addMapping("/DynamicServlet");
        sd.setInitParameter("test", "test");
        sd.setLoadOnStartup(1);
        sd.setAsyncSupported(false);

        FilterRegistration.Dynamic fd = ctx.addFilter("DynamicFilter",
                "com.creditease.monitorframework.fat.filters.DynamicFilter");

        fd.addMappingForUrlPatterns(null, true, "/DynamicServlet");
        fd.setInitParameter("test2", "test2");
        fd.setAsyncSupported(false);

        ctx.addListener("com.creditease.monitorframework.fat.listeners.TestServletInitListener");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
