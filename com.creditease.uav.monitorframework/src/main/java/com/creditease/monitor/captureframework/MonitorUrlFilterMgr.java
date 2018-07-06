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

package com.creditease.monitor.captureframework;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.uav.profiling.spi.ProfileServiceMapMgr;
import com.creditease.uav.profiling.spi.ProfileServiceMapMgr.ServiceURLBinding;

public class MonitorUrlFilterMgr {

    public enum ListType {
        SERVERURL_BLACKLIST("com.creditease.uav.monitorfilter.serverurl.blacklist"), SERVERURL_WHITELIST(
                "com.creditease.uav.monitorfilter.serverurl.whitelist"), CLIENTURL_BLACKLIST(
                        "com.creditease.uav.monitorfilter.clienturl.blacklist"), CLIENTURL_WHITELIST(
                                "com.creditease.uav.monitorfilter.clienturl.whitelist");

        private String type;

        ListType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {

            return type;
        }
    }

    boolean profileValidate = false;

    private boolean needCache = false;

    private int cacheSize = 100;

    private Map<String, Object> bwlistRepository = new HashMap<String, Object>(8);

    private Map<ListType, LRUCache<String, Boolean>> bwlistCacheRepository;

    private static MonitorUrlFilterMgr bwlistMgr = null;

    private MonitorUrlFilterMgr() {

    }

    public static MonitorUrlFilterMgr getInstance() {

        if (bwlistMgr == null) {
            bwlistMgr = new MonitorUrlFilterMgr();
        }
        return bwlistMgr;
    }

    public void init() {

        // init blackwhitelist regex pattern
        refreshList(ListType.SERVERURL_BLACKLIST.type);
        refreshList(ListType.SERVERURL_WHITELIST.type);
        refreshList(ListType.CLIENTURL_BLACKLIST.type);
        refreshList(ListType.CLIENTURL_WHITELIST.type);

        profileValidate = DataConvertHelper
                .toBoolean(System.getProperty("com.creditease.uav.monitorfilter.servicevalidate"), false);

        needCache = DataConvertHelper.toBoolean(System.getProperty("com.creditease.uav.monitorfilter.needcache"),
                false);

        // init blackwhitelist cache
        if (needCache) {

            bwlistCacheRepository = new HashMap<ListType, LRUCache<String, Boolean>>(8);

            bwlistCacheRepository
                    .put(ListType.SERVERURL_BLACKLIST,
                            new LRUCache<String, Boolean>(DataConvertHelper.toInt(
                                    System.getProperty(
                                            "com.creditease.uav.monitorfilter.serverurl.blacklist.cachesize"),
                                    cacheSize)));
            bwlistCacheRepository
                    .put(ListType.SERVERURL_WHITELIST,
                            new LRUCache<String, Boolean>(DataConvertHelper.toInt(
                                    System.getProperty(
                                            "com.creditease.uav.monitorfilter.serverurl.whitelist.cachesize"),
                                    cacheSize)));
            bwlistCacheRepository
                    .put(ListType.CLIENTURL_BLACKLIST,
                            new LRUCache<String, Boolean>(DataConvertHelper.toInt(
                                    System.getProperty(
                                            "com.creditease.uav.monitorfilter.clienturl.blacklist.cachesize"),
                                    cacheSize)));
            bwlistCacheRepository
                    .put(ListType.CLIENTURL_WHITELIST,
                            new LRUCache<String, Boolean>(DataConvertHelper.toInt(
                                    System.getProperty(
                                            "com.creditease.uav.monitorfilter.clienturl.whitelist.cachesize"),
                                    cacheSize)));
        }

    }

    public void refreshList(String listname) {

        String patternConfig = System.getProperty(listname);
        if (!StringHelper.isEmpty(patternConfig)) {
            try {
                Pattern pattern = Pattern.compile(patternConfig);
                bwlistRepository.put(listname, pattern);
            }
            catch (PatternSyntaxException e) {
                UAVServer.instance().getLog().error("PatternSyntax Illegal", e);
            }
        }

    }

    public boolean isInBlackWhitelist(ListType listtype, String targetUrl) {

        if (needCache) {
            Boolean result = bwlistCacheRepository.get(listtype).get(targetUrl);
            if (result != null) {
                return result;
            }
        }

        Pattern pattern = (Pattern) bwlistRepository.get(listtype.type);
        if (pattern == null) {
            return false;
        }

        Matcher m = pattern.matcher(targetUrl);
        if (m.find()) {
            if (needCache) {
                bwlistCacheRepository.get(listtype).put(targetUrl, true);
            }
            return true;
        }

        if (needCache) {
            bwlistCacheRepository.get(listtype).put(targetUrl, false);
        }

        return false;
    }
    
    public String getBlackWhitelistUrl(ListType listtype, String targetUrl) {
    	// at first should be judged by isInBlackWhitelist
    	Pattern pattern = (Pattern) bwlistRepository.get(listtype.type);
    	if (pattern == null) {
    		return null;
    	}
    	
    	Matcher m = pattern.matcher(targetUrl);
    	if (m.find()) {
    		return m.group();
    	}
    	
    	return null;
    }

    public boolean serviceValidate(String appId, String reUrl, String urlInfos[]) {

        if (profileValidate) {
            /**
             * NOTE: if not in profile services, do not collect;
             */
            ProfileServiceMapMgr smgr = (ProfileServiceMapMgr) UAVServer.instance()
                    .getServerInfo("profile.servicemapmgr");

            ServiceURLBinding sub = smgr.searchServiceURLBinding(appId, reUrl);
            if (sub == null) {
                return false;
            }

            String urlId = urlInfos[2];

            // rewrite pathparam url
            urlId = urlId.substring(0, urlId.indexOf(sub.getPathPattern())) + sub.getPath();

            urlInfos[2] = urlId;
        }
        return true;
    }
}
