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

import java.util.LinkedHashMap;
import java.util.Map;

import com.creditease.monitor.UAVServer;
import com.creditease.monitor.appfra.hook.spi.HookConstants;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.uav.common.BaseComponent;
import com.creditease.uav.profiling.handlers.dubbo.DubboServiceProfileInfo;
import com.creditease.uav.profiling.spi.ProfileConstants;
import com.creditease.uav.profiling.spi.ProfileContext;
import com.creditease.uav.profiling.spi.ProfileElement;
import com.creditease.uav.profiling.spi.ProfileElementInstance;
import com.creditease.uav.profiling.spi.ProfileHandler;

public class DubboProfileHandler extends BaseComponent implements ProfileHandler {

    @SuppressWarnings("unchecked")
    @Override
    public void doProfiling(ProfileElement elem, ProfileContext context) {

        UAVServer.ServerVendor sv = (UAVServer.ServerVendor) UAVServer.instance()
                .getServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR);

        // only support JEE Application, not support MSCP Application
        if (sv == UAVServer.ServerVendor.MSCP) {
            return;
        }

        if (!ProfileConstants.PROELEM_COMPONENT.equals(elem.getElemId())) {
            return;
        }

        InterceptContext ic = context.get(InterceptContext.class);

        if (ic == null) {
            this.logger.warn("Profile:Annotation FAILs as No InterceptContext available", null);
            return;
        }

        /**
         * 1.get webappclassloader
         */
        ClassLoader webappclsLoader = (ClassLoader) ic.get(InterceptConstants.WEBAPPLOADER);

        if (null == webappclsLoader) {
            this.logger.warn("Profile:JARS FAILs as No webappclsLoader available", null);
            return;
        }

        Map<String,DubboServiceProfileInfo> list = (Map<String,DubboServiceProfileInfo>) ic
                .get(HookConstants.DUBBO_PROFILE_LIST);

        if (list == null) {
            return;
        }

        // set the instance id = simple name of the annotation class
        ProfileElementInstance inst = elem.getInstance("com.alibaba.dubbo.config.spring.ServiceBean");

        for (String servicekey: list.keySet()) {
            
            DubboServiceProfileInfo dspi =list.get(servicekey);

            Map<String, Object> info = new LinkedHashMap<String, Object>();

            //get service class & dubbo application id
            String serviceClass = dspi.getServiceClass();

            info.put("dubboAppId", dspi.getDbAppId());
            info.put("group", dspi.getGroup());
            info.put("version", dspi.getVersion());
            info.put("servcls", serviceClass);

            //get protocols
            Map<String, Object> protocols = new LinkedHashMap<String, Object>();

            info.put("protocols", protocols);

            for (DubboServiceProfileInfo.Protocol pro : dspi.getProtocols()) {

                Map<String, Object> pAttrs = new LinkedHashMap<String, Object>();

                addProtocolAttr(pAttrs, "port", pro.getPort());
                addProtocolAttr(pAttrs, "path", pro.getContextpath());
                addProtocolAttr(pAttrs, "ser", pro.getSerialization());
                addProtocolAttr(pAttrs, "char", pro.getCharset());

                protocols.put(pro.getpName(), pAttrs);
            }
            
            //get methods
            try {
                Class<?> serviceClassI=webappclsLoader.loadClass(serviceClass);
                ComponentProfileHandler.getMethodInfo(serviceClassI, info);
            }
            catch (ClassNotFoundException e) {
                continue;
            }
            
            inst.setValue(servicekey, info);
        }
    }

    /**
     * addProtocolAttr
     * @param pAttrs
     * @param key
     * @param value
     */
    private void addProtocolAttr(Map<String, Object> pAttrs, String key, Object value) {

        if (value == null) {
            return;
        }

        pAttrs.put(key, value);
    }
}
