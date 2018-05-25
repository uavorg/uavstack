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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.uav.common.BaseComponent;
import com.creditease.uav.profiling.spi.ProfileConstants;
import com.creditease.uav.profiling.spi.ProfileContext;
import com.creditease.uav.profiling.spi.ProfileElement;
import com.creditease.uav.profiling.spi.ProfileElementInstance;
import com.creditease.uav.profiling.spi.ProfileHandler;
import com.creditease.uav.util.LimitLinkedHashMap;
import com.creditease.uav.util.MonitorServerUtil;

public class ClientProfileHandler extends BaseComponent implements ProfileHandler {

    @SuppressWarnings("unchecked")
    @Override
    public void doProfiling(ProfileElement elem, ProfileContext context) {

        if (!ProfileConstants.PROELEM_CLIENT.equals(elem.getElemId())) {
            return;
        }

        String clientURL = (String) context.get(ProfileConstants.PC_ARG_CLIENT_URL);

        if (clientURL == null) {
            return;
        }

        long curTime = System.currentTimeMillis();

        String type = (String) context.get(ProfileConstants.PC_ARG_CLIENT_TYPE);

        ProfileElementInstance pei = null;

        String actionPath = null;

        // for http://
        if (clientURL.indexOf("http://") == 0) {

            URI clientTargetURI = null;
            try {
                clientTargetURI = new URI(clientURL);
            }
            catch (URISyntaxException e) {
                return;
            }

            pei = getTargetURIInst(clientTargetURI, elem);

            actionPath = clientTargetURI.getPath();
        }
        // for none jdbc /redis /mongo
        else {
            pei = elem.getInstance(clientURL);

            actionPath = "ap";
        }

        pei.setValue("ts", curTime);

        String ctype = (String) pei.getValues().get("type");

        if (ctype == null) {
            pei.setValue("type", type);
        }
        else {
            if (ctype.indexOf(type) == -1) {
                pei.setValue("type", ctype + "," + type);
            }
        }

        /**
         * NOTE: sometimes the ngnix or proxys or tomcat will tell us the server type in reponse header
         */
        String server = (String) context.get(ProfileConstants.PC_ARG_CLIENT_TARGETSERVER);

        if (!StringHelper.isEmpty(server)) {
            pei.setValue("svr", server);
        }

        Map<String, Object> urls = (Map<String, Object>) pei.getValues().get("urls");

        if (urls == null) {
            int limit = DataConvertHelper
                    .toInt(System.getProperty("com.creditease.uav.profile.eleminst.client.urls.limit"), 100);
            urls = new LimitLinkedHashMap<String, Object>(limit);
            pei.setValue("urls", urls);
        }

        Map<String, Object> urlAttrs = (Map<String, Object>) urls.get(actionPath);

        if (urlAttrs == null) {
            urlAttrs = new HashMap<String, Object>();
            urls.put(actionPath, urlAttrs);
        }

        urlAttrs.put("ts", curTime);

        String action = (String) context.get(ProfileConstants.PC_ARG_CLIENT_ACTION);
        Integer rc = (Integer) context.get(ProfileConstants.PC_ARG_CLIENT_RC);

        if (rc == 1) {
            urlAttrs.put(MonitorServerUtil.getActionTag(action), curTime);
        }
        else if (rc == -1) {
            urlAttrs.put(MonitorServerUtil.getActionErrorTag(action), curTime);
        }

        if (clientURL.startsWith("http")) {
            String rs = (String) context.get(ProfileConstants.PC_ARG_CLIENT_RS);
            if(StringHelper.isNaturalNumber(rs)) {
                urlAttrs.put(MonitorServerUtil.getActionTag(rs), curTime);
            }
        }
    }

    /**
     * 
     * @param clientTargetURI
     * @return
     */
    private ProfileElementInstance getTargetURIInst(URI clientTargetURI, ProfileElement elem) {

        String host = clientTargetURI.getHost();
        int port = clientTargetURI.getPort();
        // String dnsName = null;
        //
        // if (!NetworkHelper.isIPV4(host)) {
        // String tmp = NetworkHelper.getIPByDNS(host);
        // if (tmp != null) {
        // dnsName = host;
        // host = tmp;
        // }
        // }

        String uri = clientTargetURI.getScheme() + "://" + host;
        if (port > 0) {
            uri += ":" + port;
        }

        // http://xxxxx/yyyyy@client type
        ProfileElementInstance pei = elem.getInstance(uri);

        // if (dnsName != null) {
        // pei.setValue("dn", dnsName);
        // }

        return pei;
    }

}
