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

package com.creditease.uav.appserver.listeners;

import java.util.HashSet;

import com.creditease.monitor.UAVServer;
import com.creditease.monitor.datastore.DataObserver;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;
import com.creditease.monitor.interceptframework.spi.InterceptEventListener;
import com.creditease.uav.profiling.StandardProfileContext;
import com.creditease.uav.profiling.spi.Profile;
import com.creditease.uav.profiling.spi.ProfileContext;
import com.creditease.uav.profiling.spi.ProfileFactory;
import com.creditease.uav.profiling.spi.ProfileServiceMapMgr;
import com.creditease.uav.util.MonitorServerUtil;

public class AppProfilingListener extends InterceptEventListener {

    public AppProfilingListener() {
        /**
         * for profiling, collect the servlet url patterns
         */
        UAVServer.instance().putServerInfo("monitor.urls", new HashSet<String>());
        UAVServer.instance().putServerInfo("profile.servicemapmgr", new ProfileServiceMapMgr());
    }

    @Override
    public boolean isEventListener(Event event) {

        switch (event) {
            case AFTER_SERVET_INIT:
                break;
            case BEFORE_SERVLET_DESTROY:
                break;
            case WEBCONTAINER_STARTED:
                return true;
            case WEBCONTAINER_STOPPED:
                return true;
            default:
                break;

        }
        return false;
    }

    @Override
    public void handleEvent(InterceptContext context) {

        // get the context path
        String contextpath = (String) context.get(InterceptConstants.CONTEXTPATH);
        String basepath = (String) context.get(InterceptConstants.BASEPATH);

        // swtich event
        Event event = context.getEvent();

        // create the profile context object
        ProfileContext profilecontext = new StandardProfileContext();

        // put InterceptContext into profile context, then profile handler can get info from InterceptContext
        profilecontext.put(InterceptContext.class, context);

        // get the profile factory
        ProfileFactory pf = ProfileFactory.instance();

        switch (event) {
            case AFTER_SERVET_INIT:
                break;
            case BEFORE_SERVLET_DESTROY:
                break;
            case WEBCONTAINER_STARTED:

                startProfiling(contextpath, basepath, pf, profilecontext);

                break;
            case WEBCONTAINER_STOPPED:

                stopProfiling(contextpath, basepath, pf);

                break;
            default:
                break;

        }
    }

    /**
     * start create the profile and doing profiling on webapp startup
     * 
     * @param contextroot
     * @param pf
     * @param webapploader
     * @param context
     */
    protected void startProfiling(String contextroot, String basePath, ProfileFactory pf, ProfileContext context) {

        String appid = MonitorServerUtil.getApplicationId(contextroot, basePath);

        /**
         * NOTE: as the internal application, we will not profile it
         */
        if ("com.creditease.uav".equalsIgnoreCase(appid)) {
            return;
        }

        if (this.logger.isLogEnabled()) {
            this.logger.info("Profiling of Application[ " + appid + "] START...");
        }

        String profileId = MonitorServerUtil.getApplicationId(contextroot, basePath);

        Profile p = pf.buildProfile(profileId);

        if (null != p) {

            if (this.logger.isLogEnabled()) {
                this.logger.info("Creating Profile Object of Application[ " + appid + "] DONE");
            }

            DataObserver.instance().installProfile(p);
            if (this.logger.isLogEnabled()) {
                this.logger.info("Profile Object [" + p.getId() + "] of Application[ " + appid
                        + "]  is INSTALLED on data observer");
            }

            /**
             * doing the profiling
             */
            try {

                p.doProfiling(context);
                if (this.logger.isLogEnabled()) {
                    this.logger.info("Profiling of Application[ " + appid + "] DONE SUCCESS ");
                }
            }
            catch (Exception e) {
                this.logger.error("Profiling of Application[ " + appid + "] FAILs ", e);
            }
        }
        else {
            if (this.logger.isLogEnabled()) {
                this.logger.warn("Profiling of Application[ " + appid + "] FAILs", null);
            }
        }
    }

    /**
     * destory profile and release all resources of the profile
     * 
     * @param contextroot
     * @param pf
     * @param webapploader
     */
    protected void stopProfiling(String contextroot, String basePath, ProfileFactory pf) {

        String appid = MonitorServerUtil.getApplicationId(contextroot, basePath);

        if (this.logger.isLogEnabled()) {
            this.logger.info("destorying profile of Application[ " + appid + "] START...");
        }

        String profileId = MonitorServerUtil.getApplicationId(contextroot, basePath);

        Profile p = pf.destroyProfile(profileId);

        if (null != p) {

            if (this.logger.isLogEnabled()) {
                this.logger.info("destorying profile of Application[ " + appid + "] END SUCCESS");
            }

            DataObserver.instance().uninstallProfile(p);

            if (this.logger.isLogEnabled()) {
                this.logger.info(
                        "profile[" + p.getId() + "] of Application[ " + appid + "]  is UNINSTALLED on data observer");
            }
        }
        else {
            if (this.logger.isLogEnabled()) {
                this.logger.warn("destorying profile of Application[ " + appid + "]  FAILs", null);
            }
        }
    }

}
