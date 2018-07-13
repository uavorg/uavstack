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

package com.creditease.monitor.handlers;

import com.creditease.agent.helpers.StringHelper;
import com.creditease.monitor.appfra.hook.StandardHookContext;
import com.creditease.monitor.appfra.hook.spi.HookContext;
import com.creditease.monitor.appfra.hook.spi.HookFactory;
import com.creditease.monitor.captureframework.MonitorUrlFilterMgr;
import com.creditease.monitor.captureframework.MonitorUrlFilterMgr.ListType;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.CaptureContext;
import com.creditease.monitor.captureframework.spi.MonitorElement;
import com.creditease.monitor.captureframework.spi.MonitorElementInstance;
import com.creditease.monitor.interceptframework.InterceptSupport;
import com.creditease.uav.appserver.listeners.AppFrkHookFactoryListener;
import com.creditease.uav.profiling.StandardProfileContext;
import com.creditease.uav.profiling.spi.Profile;
import com.creditease.uav.profiling.spi.ProfileConstants;
import com.creditease.uav.profiling.spi.ProfileContext;
import com.creditease.uav.profiling.spi.ProfileFactory;
import com.creditease.uav.util.MonitorServerUtil;

public class ClientRespTimeCapHandler extends ServerEndRespTimeCapHandler {

    @Override
    public void preCap(MonitorElement elem, CaptureContext context) {

        long st = System.currentTimeMillis();
        context.put("ClientRespTime.startTime", st);
    }

    @Override
    public void doCap(MonitorElement elem, CaptureContext context) {

        MonitorElementInstance inst = null;

        if (CaptureConstants.MOELEM_CLIENT_RESPTIME.equals(elem.getMonitorElemId())) {

            String clientRequestURL = (String) context.get(CaptureConstants.INFO_CLIENT_REQUEST_URL);

            String appid = (String) context.get(CaptureConstants.INFO_CLIENT_APPID);

            if (StringHelper.isEmpty(clientRequestURL) || appid == null) {
                return;
            }

            /**
             * rewrite client address if localhost or 127.0.0.1
             */
            clientRequestURL = MonitorServerUtil.rewriteURLForLocalHost(clientRequestURL);

            /**
             * rewrite httpUrl, remove relativeUrl and PathParam
             */
            if (clientRequestURL.indexOf("http") == 0) {
                clientRequestURL = rewriteHttpUrl(clientRequestURL);
            }
            
            if(null == clientRequestURL) {
            	return;
            }

            String clientId = MonitorServerUtil.getServerHostPort() + "#" + appid + "#" + clientRequestURL;

            inst = elem.getInstance(clientId);

            /**
             * do client profiling
             */
            doProfileClient(context, clientRequestURL, appid);
        }

        recordCounters(context, inst);
    }

    /**
     * do client profiling
     * 
     * @param context
     * @param clientRequestURL
     */
    private void doProfileClient(CaptureContext context, String clientRequestURL, String appid) {

        Profile p = ProfileFactory.instance().getProfile(appid);

        if (p != null) {

            ProfileContext pc = new StandardProfileContext();

            String action = (String) context.get(CaptureConstants.INFO_CLIENT_REQUEST_ACTION);
            Integer rc = (Integer) context.get(CaptureConstants.INFO_CLIENT_RESPONSECODE);
            String rs = (String) context.get(CaptureConstants.INFO_CLIENT_RESPONSESTATE);
            String type = (String) context.get(CaptureConstants.INFO_CLIENT_TYPE);
            String server = (String) context.get(CaptureConstants.INFO_CLIENT_TARGETSERVER);

            pc.put(ProfileConstants.PC_ARG_CLIENT_URL, clientRequestURL);
            pc.put(ProfileConstants.PC_ARG_CLIENT_ACTION, action);
            pc.put(ProfileConstants.PC_ARG_CLIENT_RC, rc);
            pc.put(ProfileConstants.PC_ARG_CLIENT_RS, rs);
            pc.put(ProfileConstants.PC_ARG_CLIENT_TYPE, type);
            pc.put(ProfileConstants.PC_ARG_CLIENT_TARGETSERVER, server);

            pc.addPE(ProfileConstants.PROELEM_CLIENT);

            p.doProfiling(pc);
        }

    }

    @Override
    protected void recordCounters(CaptureContext context, MonitorElementInstance inst) {

        if (inst == null) {
            return;
        }

        this.doCommonCounters(context, inst, "ClientRespTime.startTime");

        /**
         * Return & Action:
         */
        String action = (String) context.get(CaptureConstants.INFO_CLIENT_REQUEST_ACTION);
        Integer rc = (Integer) context.get(CaptureConstants.INFO_CLIENT_RESPONSECODE);
        String clientRequestURL = (String) context.get(CaptureConstants.INFO_CLIENT_REQUEST_URL);

        if (rc != null && rc == -1) {
            inst.increValue(CaptureConstants.MEI_ERROR);
        }

        if (!StringHelper.isEmpty(action)) {
            if (rc == 1) {
                inst.increValue(MonitorServerUtil.getActionTag(action));
            }
            else if (rc == -1) {
                inst.increValue(MonitorServerUtil.getActionErrorTag(action));
            }
        }
        
        if (clientRequestURL.startsWith("http")) {
            String rs = (String) context.get(CaptureConstants.INFO_CLIENT_RESPONSESTATE);
            if(StringHelper.isNaturalNumber(rs)) {
                inst.increValue(MonitorServerUtil.getActionTag(rs));
            }
        }
    }

    @Override
    public void preStore(MonitorElementInstance instance) {

        super.preStore(instance);

        /**
         * 对于哪些需要将实时数据收集到MonitorElement的Hook，在此处运行
         */
        AppFrkHookFactoryListener listener = (AppFrkHookFactoryListener) InterceptSupport.instance()
                .getEventListener(AppFrkHookFactoryListener.class);

        HookFactory hookF = listener.getHookFactory();

        HookContext cc = new StandardHookContext();

        cc.put("monitor.client.prestore", "1");

        hookF.runHook(cc);
    }

    /**
     * remove relativeUrl and PathParam </br>
     * e.g. http://127.0.0.1:8080/rs/TestRestService/echo1/23/43?query=test </br>
     * => http://127.0.0.1:8080/rs/TestRestService/echo1
     * 
     */
    private String rewriteHttpUrl(String clientRequestURL) {

        int st = clientRequestURL.indexOf("//");
        String url = clientRequestURL.substring(st + 2);

        int st2 = url.indexOf("/");
        String urlhead = clientRequestURL.substring(0, st2 + st + 2);
        String reUrl = url.substring(st2);

        reUrl = MonitorServerUtil.cleanRelativeURL(reUrl);
        String tempUrl = urlhead + reUrl;
        
        MonitorUrlFilterMgr mufm = MonitorUrlFilterMgr.getInstance();
        
        if (mufm.isInBlackWhitelist(ListType.CLIENTURL_WHITELIST,
        		tempUrl) == false) {
        	/**
        	 * NOTE: if in blacklist, do not collect;
        	 */
        	if (mufm.isInBlackWhitelist(ListType.CLIENTURL_BLACKLIST,
        			tempUrl) == true) {
        		return null;
        	}
        	
        }
        
        if (mufm.isInBlackWhitelist(ListType.CLIENTURL_WHITELIST,
        		tempUrl) == true) {
        	// dealing with potential concurrency problems
        	return mufm.getBlackWhitelistUrl(ListType.CLIENTURL_WHITELIST, tempUrl);
        }

        /**
         * NOTE:remove PathParam,simply assume PathParam is digit
         */
        String args[] = reUrl.split("/");

        int endIndex = 0;
        for (int i = 1; i < args.length; i++) {
            try {
                Long.parseLong(args[i]);
            }
            catch (NumberFormatException e) {
                endIndex += (args[i].length() + 1);
                continue;
            }
            break;

        }
        reUrl = reUrl.substring(0, endIndex);

        clientRequestURL = urlhead + reUrl;

        return clientRequestURL;
    }
}
