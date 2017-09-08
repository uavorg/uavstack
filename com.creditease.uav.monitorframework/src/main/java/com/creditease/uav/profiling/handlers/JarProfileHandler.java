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

package com.creditease.uav.profiling.handlers;

import java.net.URL;
import java.net.URLClassLoader;

import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.uav.common.BaseComponent;
import com.creditease.uav.profiling.spi.ProfileConstants;
import com.creditease.uav.profiling.spi.ProfileContext;
import com.creditease.uav.profiling.spi.ProfileElement;
import com.creditease.uav.profiling.spi.ProfileElementInstance;
import com.creditease.uav.profiling.spi.ProfileHandler;

/**
 * profiling for web application's jar files
 * 
 * @author zhen zhang
 *
 */
public class JarProfileHandler extends BaseComponent implements ProfileHandler {

    @Override
    public void doProfiling(ProfileElement pe, ProfileContext context) {

        /**
         * handler jars
         */
        if (!ProfileConstants.PROELEM_JARS.equals(pe.getElemId())) {
            return;
        }

        InterceptContext ic = context.get(InterceptContext.class);

        if (ic == null) {
            this.logger.warn("Profile:JARS FAILs as No InterceptContext available", null);
            return;
        }

        ClassLoader webappclsLoader = (ClassLoader) ic.get(InterceptConstants.WEBAPPLOADER);

        if (null == webappclsLoader) {
            this.logger.warn("Profile:JARS FAILs as No webappclsLoader available", null);
            return;
        }

        ProfileElementInstance pei = pe.getInstance(ProfileConstants.PEI_LIB);

        if (!URLClassLoader.class.isAssignableFrom(webappclsLoader.getClass())) {
            this.logger.warn("Profile:JARS FAILs as the webappclassloader is not a URLClassloader", null);
            return;
        }

        URL[] urls = ((URLClassLoader) webappclsLoader).getURLs();

        if (urls == null) {
            this.logger.warn("Profile:JARS FAILs as No JAR URLs from webappclassloader", null);
            return;
        }

        for (URL url : urls) {
            String path = url.getPath();
            String[] pinfo = path.split("/");
            pei.setValue(pinfo[pinfo.length - 1], url.toString());
        }

        /**
         * confirm there is update
         */
        pe.getRepository().setUpdate(true);

    }

}
