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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.creditease.agent.helpers.ReflectHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.uav.common.BaseComponent;
import com.creditease.uav.profiling.spi.Profile;
import com.creditease.uav.profiling.spi.ProfileConstants;
import com.creditease.uav.profiling.spi.ProfileContext;
import com.creditease.uav.profiling.spi.ProfileElement;
import com.creditease.uav.profiling.spi.ProfileHandler;
import com.creditease.uav.profiling.spi.ProfileRepository;

public class StandardProfile extends BaseComponent implements Profile {

    protected String id;
    protected ProfileRepository repository;

    protected final Map<String, ProfileHandler> handlers = new ConcurrentHashMap<String, ProfileHandler>();

    public StandardProfile(String id) {

        this.id = id;
        repository = new StandardProfileRespository(this);

        /**
         * register the standard profile elements
         */
        ProfileElement jarPE = new StandardProfileElement(ProfileConstants.PROELEM_JARS,
                "com.creditease.uav.profiling.handlers.JarProfileHandler");
        jarPE.setRepository(repository);
        repository.addElement(jarPE);

        /**
         * JEE CPT handler or MSCP CPT handler
         */
        String cptProfileHandler = "com.creditease.uav.profiling.handlers.ComponentProfileHandler";

        UAVServer.ServerVendor sv = (UAVServer.ServerVendor) UAVServer.instance()
                .getServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR);

        if (sv == UAVServer.ServerVendor.MSCP) {
            cptProfileHandler = "com.creditease.uav.mscp.plus.MSCPProfileHandler";
        }

        ProfileElement comPE = new StandardProfileElement(ProfileConstants.PROELEM_COMPONENT, cptProfileHandler);
        comPE.setRepository(repository);
        repository.addElement(comPE);

        ProfileElement logPE = new StandardProfileElement(ProfileConstants.PROELEM_LOGS,
                "com.creditease.uav.profiling.handlers.LogProfileHandler");
        logPE.setRepository(repository);
        repository.addElement(logPE);

        ProfileElement ipLnk = new StandardProfileElement(ProfileConstants.PROELEM_IPLINK,
                "com.creditease.uav.profiling.handlers.IPLinkProfileHandler");
        ipLnk.setRepository(repository);
        repository.addElement(ipLnk);

        ProfileElement clientPE = new StandardProfileElement(ProfileConstants.PROELEM_CLIENT,
                "com.creditease.uav.profiling.handlers.ClientProfileHandler");
        clientPE.setRepository(repository);
        repository.addElement(clientPE);

    }

    @Override
    public String getId() {

        return this.id;
    }

    @Override
    public void doProfiling(ProfileContext context) {

        ProfileElement[] pes = repository.getElements();

        int state = 1;
        repository.setState(state);

        Set<String> IncludePes = context.getPE();

        for (ProfileElement pe : pes) {

            if (IncludePes.size() > 0 && (!IncludePes.contains(pe.getElemId()))) {
                continue;
            }

            String hClass = pe.getElemHandlerClass();

            if (null == hClass) {
                continue;
            }
            /**
             * select profiling handler to process
             */
            ProfileHandler handler = selectHandler(hClass);

            try {
                handler.doProfiling(pe, context);
            }
            catch (Exception e) {
                this.logger.error("ProfileHandler[" + hClass + "] do profile FAILs ", e);
                state = 3;
            }
        }

        if (state == 1) {
            state = 2;
        }

        repository.setState(state);
    }

    private ProfileHandler selectHandler(String capClassName) {

        ProfileHandler caphandler = handlers.get(capClassName);

        if (caphandler == null) {
            caphandler = (ProfileHandler) ReflectHelper.newInstance(capClassName);
            if (caphandler != null) {
                handlers.put(capClassName, caphandler);
            }
        }
        return caphandler;
    }

    @Override
    public ProfileRepository getRepository() {

        return this.repository;
    }

    @Override
    public void destroy() {

        this.handlers.clear();
        this.repository.destroy();
    }

}
