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

package com.creditease.agent.feature.logagent;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.creditease.agent.ConfigurationManager;
import com.creditease.agent.feature.LogAgent;
import com.creditease.agent.feature.LogAgent.AppLogPatternInfoCollection;
import com.creditease.agent.feature.logagent.api.LogFilterAndRule;
import com.creditease.agent.feature.logagent.event.Event;
import com.creditease.agent.feature.logagent.far.DefaultLogFilterAndRule;
import com.creditease.agent.feature.logagent.objects.LogPatternInfo;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.ReflectHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.log.api.ISystemLogger;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;

public class RuleFilterFactory {

    private static final String DEFAULT_FILTER_PREFIX = "";

    private static final String DEFAULT_RULE = "{\"separator\":\"\n\", \"assignfields\":{\"content\":1}, \"timestamp\": 0}";

    // FIX 只使用了default_rule的assignfields字段和separator字段
    private LogFilterAndRule DEFAULT_LOGFAR;

    private static RuleFilterFactory factory;

    // Main LogFilterAndRule
    // expire 30 days
    private Cache<String, LogFilterAndRule> logcollection = CacheBuilder.newBuilder()
            .expireAfterAccess(20, TimeUnit.DAYS).<String, LogFilterAndRule> build();

    // Aid LogFilterAndRule
    private Cache<String, List<LogFilterAndRule>> aidlogcollection = CacheBuilder.newBuilder()
            .expireAfterAccess(20, TimeUnit.DAYS).<String, List<LogFilterAndRule>> build();

    private RuleFilterFactory() {

        String defFilter = ConfigurationManager.getInstance().getFeatureConfiguration("logagent", "defrule.filter");

        if (StringHelper.isEmpty(defFilter)) {
            defFilter = DEFAULT_FILTER_PREFIX;
        }

        DEFAULT_LOGFAR = new DefaultLogFilterAndRule(defFilter, JSON.parseObject(DEFAULT_RULE).getString("separator"),
                JSON.parseObject(DEFAULT_RULE).getJSONObject("assignfields"), 0, 0);
    }

    public static RuleFilterFactory getInstance() {

        if (factory == null) {
            factory = new RuleFilterFactory();
        }

        return factory;
    }

    // factory需要通过appid管理所以使用中的filter与rule,以方便做更新与比对
    public LogFilterAndRuleBuilder newBuilder() {

        return new LogFilterAndRuleBuilder();
    }

    protected List<LogFilterAndRule> getAidLogFilterAndRuleList(String id) {

        // id = id.replace('\\', '/');
        return aidlogcollection.getIfPresent(id);
    }

    // this key should be absFilePath --- by hongqiang
    public LogFilterAndRule getLogFilterAndRule(String id) {

        // id = id.replace('\\', '/');
        LogFilterAndRule lfar = null;

        lfar = logcollection.getIfPresent(id);
        if (lfar != null) {
            return lfar;
        }

        try {
            lfar = logcollection.get(id, new Callable<LogFilterAndRule>() {

                @Override
                public LogFilterAndRule call() {

                    return DEFAULT_LOGFAR;
                }
            });
        }
        catch (ExecutionException e) {
        }
        return lfar;
    }

    class RuleObject {

        private String separator;

        private Map<String, Integer> assignfields;

        private int timestamp;

        public String getSeparator() {

            return separator;
        }

        public void setSeparator(String separator) {

            this.separator = separator;
        }

        public Map<String, Integer> getAssignfields() {

            return assignfields;
        }

        public int getTimestamp() {

            return timestamp;
        }

        public void setTimestamp(int timestamp) {

            this.timestamp = timestamp;
        }
    }

    /**
     * aid中用于声名需要哪些辅助抓取器，目前支持： 1.name= StackStaceLog 的辅助抓取
     * 
     * @author 201211070016<br>
     *         <b>其中对于filterAndRule对象规则如下：</b><br>
     *         1. 需要放在com.creditease.agne.feature.logagent.far包中 2. 需要实现LogFilterAndRule接口
     */
    public class LogFilterAndRuleBuilder {

        private String serverid = null;

        private String appid = null;

        private String logid = null;

        private String filter = null;

        private String rule = null;

        private String[] aids = null;

        private int version = 0;

        private JSONObject DEFAULT_ASSIGNFIELDS = JSON.parseObject("{content:1}");

        public String getServerid() {

            return serverid;
        }

        public LogFilterAndRuleBuilder serverId(String serverid) {

            this.serverid = serverid;
            return this;
        }

        public LogFilterAndRuleBuilder logId(String logid) {

            this.logid = logid;
            return this;
        }

        public LogFilterAndRuleBuilder filterRegex(String filter) {

            this.filter = filter;
            return this;
        }

        public LogFilterAndRuleBuilder ruleRegex(String rule) {

            this.rule = rule;
            return this;
        }

        public LogFilterAndRuleBuilder appId(String appid) {

            this.appid = appid;
            return this;
        }

        public LogFilterAndRuleBuilder needAid(String[] aids) {

            this.aids = aids;
            return this;
        }

        public LogFilterAndRuleBuilder version(int version) {

            this.version = version;
            return this;
        }

        public LogFilterAndRule build(String classname) {

            // get serverid
            serverid = Preconditions.checkNotNull(serverid, "serverid must be set...");
            // get appid
            appid = Preconditions.checkNotNull(appid, "appid must be set...");
            // get logid
            logid = Preconditions.checkNotNull(logid, "logid must be set...");
            // get rule json
            rule = Optional.fromNullable(rule).or(DEFAULT_RULE);
            // parse filter json
            String filterregex = Optional.fromNullable(filter).or(DEFAULT_FILTER_PREFIX);
            // parse rule json
            JSONObject robject = JSON.parseObject(rule);
            String separator = Optional.fromNullable(robject.getString("separator")).or("\t");
            JSONObject assignFields = Optional.fromNullable(robject.getJSONObject("assignfields"))
                    .or(DEFAULT_ASSIGNFIELDS);
            // Verify timeStamp number is available
            int timestampNumber = robject.getIntValue("timestamp");
            // build by reflect
            LogFilterAndRule mainLogFAR = (LogFilterAndRule) ReflectHelper.newInstance(
                    "com.creditease.agent.feature.logagent.far." + classname + "LogFilterAndRule",
                    new Class[] { String.class, String.class, JSONObject.class, int.class, int.class },
                    new Object[] { filterregex, separator, assignFields, timestampNumber, version },
                    ConfigurationManager.getInstance().getFeatureClassLoader("logagent"));
            // LogFilterAndRule mainLogFAR = new DefaultLogFilterAndRule(filterregex, separator, assignFields,
            // timestampNumber);
            LogFilterAndRule aid = null;
            List<LogFilterAndRule> aidLogFARlist = null;
            if (aids != null && aids.length > 0) {
                aidLogFARlist = Lists.newArrayList();
                for (String name : aids) {
                    aid = (LogFilterAndRule) ReflectHelper
                            .newInstance("com.creditease.agent.feature.logagent.far." + name + "LogFilterAndRule");
                    aidLogFARlist.add(aid);
                }

            }
            LogAgent logAgent = (LogAgent) ConfigurationManager.getInstance().getComponent("logagent", "LogAgent");
            AppLogPatternInfoCollection profileMap = logAgent.getLatestLogProfileDataMap();
            LogPatternInfo logPatternInfo = profileMap.get(serverid + "-" + appid,
                    serverid + "-" + appid + "-" + logid);
            pubLogFilterAndRule(logPatternInfo.getAbsolutePath(), mainLogFAR);
            if (aidLogFARlist != null) {
                pubAidLogFilterAndRule(logPatternInfo.getAbsolutePath(), aidLogFARlist);
            }
            return mainLogFAR;
        }

        public LogFilterAndRule build() {

            return build("Default");
        }
    }

    public FilterAndRuleChain createChain(ReliableTaildirEventReader reader, int size) {

        return new FilterAndRuleChain(reader, size);
    }

    public class FilterAndRuleChain {

        private ISystemLogger logger = (ISystemLogger) ConfigurationManager.getInstance().getComponent("logagent",
                "LogDataLog");

        private ReliableTaildirEventReader reader = null;
        @SuppressWarnings("unused")
        private int batchSize = 0;
        // private boolean isHaveLog = false;
        private LogFilterAndRule main = null;
        private List<LogFilterAndRule> aids = null;

        public FilterAndRuleChain(ReliableTaildirEventReader reader, int batchSize) {
            this.reader = reader;
            this.batchSize = batchSize;
        }

        public FilterAndRuleChain setMainLogFilterAndRule(LogFilterAndRule main) {

            this.main = main;
            return this;
        }

        public FilterAndRuleChain setAidLogFilterAndRuleList(List<LogFilterAndRule> aids) {

            this.aids = Optional.fromNullable(aids).or(Lists.<LogFilterAndRule> newArrayList());
            return this;
        }

        /**
         * 增加多FAR main LogFilterAndRule 与 aid LogFilterAndRule的支持 增加对有state LogFilterAndRule 与 无state
         * LogFilterAndRule的支持
         * 
         * @param tf
         * @param backoffWithoutNL
         * @return
         * @throws IOException
         */
        @SuppressWarnings("rawtypes")
        public List<Map> doProcess(List<Event> events, boolean backoffWithoutNL) throws IOException {
            // reader.setCurrentFile(tf);
            // List<Event> events = reader.readEvents(batchSize, backoffWithoutNL);
            // if (!events.isEmpty()) {
            // isHaveLog = true;
            // }

            // LogFilterAndRule main = RuleFilterFactory.getInstance().getLogFilterAndRule(tf.getPath());
            // List<LogFilterAndRule> aids = main.getAllAidsLogFilterAndRule();
            List<Map> datalog = Lists.newArrayList();
            String log = null;
            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                log = new String(event.getBody(), Charsets.UTF_8);
                if (logger.isDebugEnable()) {
                    logger.debug(this, "get log event - [" + log + "]");
                }

                for (LogFilterAndRule aid : aids) {

                    if (aid.isMatch(reader, events, i, log)) {
                        aid.doAnalysis(event.getHeaders(), log);
                    }
                }

                if (main.isMatch(reader, events, i, log)) {
                    main.doAnalysis(event.getHeaders(), log);
                }
            }
            datalog.addAll(main.getResult(true));
            for (LogFilterAndRule aid : aids) {
                datalog.addAll(aid.getResult(true));
            }

            if (logger.isDebugEnable()) {
                logger.debug(this,
                        "datalog >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + JSONHelper.toString(datalog));
            }

            return datalog;
        }
    }

    public void pubLogFilterAndRule(String id, LogFilterAndRule lfar) {

        // 保证路径不存在多余的'/'等
        id = new File(id).getAbsolutePath();

        logcollection.put(id, lfar);
    }

    public void pubAidLogFilterAndRule(String id, List<LogFilterAndRule> lfar) {

        // 保证路径不存在多余的'/'等
        id = new File(id).getAbsolutePath();

        aidlogcollection.put(id, lfar);
    }

    public boolean hasLogFilterAndRule(String id) {

        LogFilterAndRule lfar = logcollection.getIfPresent(id);
        return lfar != null;
    }
}
