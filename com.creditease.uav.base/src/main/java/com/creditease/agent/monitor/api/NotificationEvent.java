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

package com.creditease.agent.monitor.api;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.NetworkHelper;

public class NotificationEvent {

    /**
     * Notification Event List
     */
    public final static String EVENT_HealthReAction = "NotificationEvent.Event.AppServer.HealthReAction";
    public final static String EVENT_NoMBean = "NotificationEvent.Event.AppServer.NoMBean";
    public final static String EVENT_ReAccessFAIL = "NotificationEvent.Event.AppServer.ReAccessFAIL";
    public final static String EVENT_LogRuleExpired = "NotificationEvent.Event.Log.LogRuleExpired";
    public final static String EVENT_LogNotExist = "NotificationEvent.Event.Log.LogFileNotExist";
    public final static String EVENT_LogCatchFailed = "NotificationEvent.Event.Log.LogCatchFailed";
    public final static String EVENT_LogAppProfileError = "NotificationEvent.Event.Log.AppProfileError";
    public final static String EVENT_LogPathConflict = "NotificationEvent.Event.Log.LogPathConflict";

    public static final String EVENT_RT_ALERT_CRASH = "NotificationEvent.RT.CRASH";
    public static final String EVENT_RT_ALERT_THRESHOLD = "NotificationEvent.RT.THRESHOLD";

    // --------------------------------------------------------------------------------------------

    /**
     * Notification Event Special Tag
     * 
     */
    // this tag means the notification manager will not block the event
    public final static String EVENT_Tag_NoBlock = "NotificationEvent.Tag.NoBlock";

    // ---------------------------------------------------------------------------------------------

    private final String id;
    private final String title;
    private final long timeFlag;
    private String description;
    private String host;
    private String ip;

    
    /**
     * Notification Event Level Tag
     * 
     */
    public final static String EVENT_LEVEL_KEY = "notifyLevel";
    public final static String INFO_NOTIFY_EVENT = "info";
    public final static String WARN_NOTIFY_EVENT = "warn";
    public final static String CRITICAL_NOTIFY_EVENT = "critical";
    
    /**
     * NotificationEvent的过滤字段（可选） 增加过滤字段可以提高Notification检索的准确性
     */
    private Map<String, String> args = new LinkedHashMap<String, String>();

    public NotificationEvent(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.timeFlag = System.currentTimeMillis();
        String[] hosts = NetworkHelper.getHosts();
        if (null != hosts && hosts.length == 2) {
            this.host = hosts[0];
            this.ip = hosts[1];
        }
    }

    public NotificationEvent(String id, String title, String description, long time, String ip, String host) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.timeFlag = time;
        this.ip = ip;
        this.host = host;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public NotificationEvent(String jsonStr) {

        Map m = JSONHelper.toObject(jsonStr, Map.class);
        this.timeFlag = (Long) m.get("time");
        this.id = (String) m.get("id");
        this.title = (String) m.get("title");
        this.description = (String) m.get("description");
        this.host = (String) m.get("host");
        this.ip = (String) m.get("ip");
        this.args.putAll((Map<String, String>) m.get("args"));
    }

    public String getDescription() {

        return description;
    }

    public String getTitle() {

        return title;
    }

    public String getId() {

        return id;
    }

    public Map<String, String> getArgs(boolean needDecode) {

        if (needDecode == true) {
            Map<String, String> decodeArgs = new HashMap<String, String>();

            for (String key : args.keySet()) {
                try {
                    String value = URLDecoder.decode(args.get(key), "utf-8");
                    decodeArgs.put(key, value);
                }
                catch (UnsupportedEncodingException e) {
                    // ignore
                }
            }
            return decodeArgs;
        }
        else {
            return args;
        }
    }

    public long getTime() {

        return this.timeFlag;
    }

    public String getHost() {

        return this.host;
    }

    public String getIP() {

        return this.ip;
    }

    public void addArg(String name, String obj) {

        if (name == null || obj == null) {
            return;
        }

        String urlStr;

        try {
            urlStr = URLEncoder.encode(obj, "utf-8");
            this.args.put(name, urlStr);
        }
        catch (UnsupportedEncodingException e) {
            // ignore
        }
    }

    public String getArg(String name) {

        if (name == null) {
            return null;
        }

        String val = this.args.get(name);

        if (null == val) {
            return null;
        }

        try {
            return URLDecoder.decode(val, "utf-8");
        }
        catch (UnsupportedEncodingException e) {
            // ignore
            return null;
        }
    }

    public String toJSONString() {

        StringBuilder sb = new StringBuilder("{");

        sb.append("\"time\":" + timeFlag + ",");
        sb.append("\"id\":\"" + id + "\",");
        sb.append("\"title\":\"" + title + "\",");
        sb.append("\"description\":\"" + description + "\",");
        sb.append("\"host\":\"" + this.host + "\",");
        sb.append("\"ip\":\"" + this.ip + "\",");

        Set<Entry<String, String>> sets = args.entrySet();
        sb.append("\"args\":{");
        for (Entry<String, String> set : sets) {
            sb.append("\"" + set.getKey() + "\":\"" + set.getValue() + "\",");
        }

        if (!sets.isEmpty()) {
            sb = sb.deleteCharAt(sb.length() - 1);
        }

        sb.append("}");

        return sb.append("}").toString();
    }
}
