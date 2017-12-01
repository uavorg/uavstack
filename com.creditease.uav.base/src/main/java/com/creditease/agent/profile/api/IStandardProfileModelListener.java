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

package com.creditease.agent.profile.api;

import java.util.List;
import java.util.Map;

import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.agent.spi.ActionContext;

/**
 * 
 * IStandardProfileModelListener description: do processing when profile modeling
 *
 */
public interface IStandardProfileModelListener {

    /**
     * before MDF process
     * 
     * @param ac
     * @param mdf
     */
    public void onBeforeMDFModeling(ActionContext ac, MonitorDataFrame mdf);

    @SuppressWarnings("rawtypes")
    public void onBeforeFrameModeling(ActionContext ac, MonitorDataFrame mdf, String frameId, List<Map> frameData);

    public void onAppProfileMetaCreate(ActionContext ac, MonitorDataFrame mdf, String appid, String appurl,
            String appgroup, Map<String, Object> appProfile);

    @SuppressWarnings("rawtypes")
    public void onAppClientProfileCreate(ActionContext ac, MonitorDataFrame mdf, String appid, String appurl,
            String appgroup, Map<String, Object> appProfile, List<Map> clients);

    public void onAppProfileCreate(ActionContext ac, MonitorDataFrame mdf, String appid, String appurl, String appgroup,
            Map<String, Object> appProfile);

    @SuppressWarnings("rawtypes")
    public void onAppIPLinkProfileCreate(ActionContext ac, MonitorDataFrame mdf, String appid, String appurl,
            String appgroup, Map<String, Object> appProfile, List<Map> iplinks);

    @SuppressWarnings("rawtypes")
    public void onAfterFrameModeling(ActionContext ac, MonitorDataFrame mdf, String frameId, List<Map> frameData,
            Map<String, Object> appProfile);

    /**
     * after profile process
     * 
     * @param ac
     * @param mdf
     */
    public void onAfterMDFModeling(ActionContext ac, MonitorDataFrame mdf);
}
