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

package com.creditease.uav.feature.runtimenotify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONArray;
import com.creditease.agent.helpers.DateTimeHelper;
import com.creditease.agent.helpers.EncodeHelper;
import com.creditease.agent.helpers.JSONHelper;
/**
 * notify strategy ds
 */
public class NotifyStrategy {

    public enum Type {
        STREAM("stream"), TIMER("timer");

        private String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String toString() {

            return name;
        }
    }

    private static final String[] OPERATORS = { ":=", "!=", ">", "<", "=" };

    private static final Pattern INDEX_PATTERN = Pattern.compile("\\[\\d+\\]");

    private long maxRange = 0;

    private Type type;
    
    private String name;
    
    private String desc;
    
    private String scope;
    
    private String msgTemplate;
    
    private List<Condition> condtions;

    private List<String> convergences;

    private List<String> context = Collections.emptyList();

    private List<String> instances = Collections.emptyList();

    private Map<String, String> action = Collections.emptyMap();

    public NotifyStrategy() {
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public NotifyStrategy(String name, String json) {

        Map m = JSONHelper.toObject(json, Map.class);
        String scope = (String) m.get("scope");
        String desc = (String) m.get("desc");
        List<String> context = (List<String>) m.get("context");
        List<Object> conds = (List<Object>) m.get("conditions");
        List<String> relations = (List<String>) m.get("relations");
        List<String> convergences = (List<String>) m.get("convergences");
        Map<String, String> action = (Map<String, String>) m.get("action");
        String msgTemplate = (String) m.get("msgTemplate");
        List<String> instances = (List<String>) m.get("instances");

        List<Condition> conditions = determineConditions(conds, relations, name);
        long maxRange = determineMaxRange(conditions);
        Type type = determineType(conds);

        setMaxRange(maxRange).setType(type).setName(name).setDesc(desc).setScope(scope).setMsgTemplate(msgTemplate)
                .setConditions(conditions).setConvergences(convergences).setContext(context).setInstances(instances)
                .setAction(action);
    }

    public NotifyStrategy setMaxRange(long maxRange) {

        this.maxRange = maxRange;
        return this;
    }
    
    public NotifyStrategy setType(Type type) {

        this.type = type;
        return this;
    }

    public NotifyStrategy setName(String name) {

        this.name = name;
        return this;
    }
    
    public NotifyStrategy setDesc(String desc) {
        
        this.desc = desc;
        return this;
    }
    
    public NotifyStrategy setScope(String scope) {
        
        this.scope = scope;
        return this;
    }
    
    public NotifyStrategy setMsgTemplate(String msgTemplate) {
        
        this.msgTemplate = msgTemplate;
        return this;
    }
    
    public NotifyStrategy setConditions(List<Condition> conditions) {
        
        this.condtions = conditions;
        return this;
    }
    
    public NotifyStrategy setConvergences(List<String> convergences) {
        
        this.convergences = convergences;
        return this;
    }
    
    public NotifyStrategy setContext(List<String> context) {
        
        if (context != null && context.size() != 0) {
            this.context = context;
        }
        return this;
    }
    
    public NotifyStrategy setInstances(List<String> instances) {
        
        if (instances != null && instances.size() != 0) {
            this.instances = instances;
        }
        return this;
    }
    
    public NotifyStrategy setAction(Map<String, String> action) {
        
        if (action != null && action.size() != 0) {
            this.action = action;
        }
        return this;
    }

    public long getMaxRange() {

        return maxRange;
    }

    public Type getType() {

        return type;
    }

    public String getName() {

        return name;
    }

    public String getDesc() {

        return desc;
    }

    public String getScope() {

        return scope;
    }

    public String getMsgTemplate() {

        return msgTemplate;
    }

    public List<Condition> getCondtions() {

        return condtions;
    }

    public List<String> getConvergences() {

        return convergences;
    }

    public List<String> getContext() {

        return context;
    }

    public List<String> getInstances() {

        return instances;
    }

    public Map<String, String> getAction() {

        return action;
    }

    private long determineMaxRange(List<Condition> conditions) {
        
        long maxRange = 0;
        for (Condition cond : conditions) {
            for (Expression expr : cond.expressions) {
                maxRange = Math.max(maxRange, expr.range);
            }
        }
        
        return maxRange;
    }
    
    private Type determineType(List<Object> conds) {
        
        Type type = Type.STREAM;
        for (Object o : conds) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cond = (Map<String, Object>) o;
            if (cond.get("type") != null && cond.get("type").equals(Type.TIMER.name)) {
                type = Type.TIMER;
            }
        }
        
        return type;
    }
    
    private List<Condition> determineConditions(List<Object> conditions, List<String> relations, String name) {
        
        int idx = 0; // expression count
        List<Expression> exprs = new ArrayList<>();
        for (Object o : conditions) {

            // condition is simple string: "arg>123"
            if (String.class.isAssignableFrom(o.getClass())) {
                Expression expression = new Expression((String) o);
                expression.setIdx(idx++);
                exprs.add(expression);
            }
            else {
                @SuppressWarnings("unchecked")
                Map<String, Object> cond = (Map<String, Object>) o;
                Expression expression;
                if (cond.get("type") == null || cond.get("type").equals(Type.STREAM.name)) {
                    String expr = (String) cond.get("expr");
                    String func = (String) cond.get("func");
                    Long range = cond.get("range") == null ? null : Long.valueOf(cond.get("range").toString());
                    Float sampling = cond.get("sampling") == null ? null
                            : Float.valueOf(cond.get("sampling").toString());
                    expression = new Expression(expr, func, range, sampling);
                }
                else {
                    String metric = String.valueOf(cond.get("metric"));

                    //add default metricPrefix if missing, such as "clientResp.", "hostState." and so on                                      
                    String metricPrefix="";
                    
                    int index = metric.indexOf('.');
                    
                    if(index>-1) {
                        metricPrefix = metric.substring(0, metric.indexOf('.'));
                    }                   
                    
                    if(!metricPrefix.contains("State")&&!metricPrefix.contains("Resp")&&!metricPrefix.contains("jvm")) {
                        
                        metricPrefix = name.substring(name.indexOf('@') + 1, name.lastIndexOf('@'));
                        
                        metric = metricPrefix + "."+ metric;
                    }       
                        
                    cond.put("metric", metric);
                    expression = new Expression(cond);
                }
                expression.setIdx(idx++);
                exprs.add(expression);
            }
        }

        idx = 1; // reuse for condition count, start from 1
        List<Condition> conds = null;
        if (relations == null || relations.isEmpty()) {
            conds = new ArrayList<>(conditions.size());

            for (Expression expr : exprs) {
                conds.add(new Condition(idx++, expr));
            }
        }
        else {
            conds = new ArrayList<>(relations.size());
            for (String relation : relations) {

                Matcher m = INDEX_PATTERN.matcher(relation);
                Set<Expression> set = new HashSet<>();
                while (m.find()) {
                    String idxHolder = m.group();
                    int i = Integer.parseInt(idxHolder.substring(1, idxHolder.length() - 1));
                    if (i >= exprs.size()) { // IndexOutOfBoundsException
                        continue;
                    }
                    set.add(exprs.get(i));
                    relation = relation.replace(idxHolder, "{" + i + "}"); // temp i
                }

                List<Expression> list = new ArrayList<>(set);
                for (int i = 0; i < list.size(); i++) {
                    relation = relation.replace("{" + list.get(i).getIdx() + "}", "[" + i + "]");
                }
                conds.add(new Condition(idx++, list, relation));
            }
        }

        return conds;
    }

    protected static class Expression {

        private int idx;
        private Type type;
        private String arg;
        private String operator;
        private String expectedValue;
        private long range = 0;
        private String func;
        private float sampling = 1;
        private String downsample;
        private Boolean[] weekdayLimit=new Boolean[] {true,true,true,true,true,true,true};
        
        private Set<String> matchArgExpr = new HashSet<String>();

        private long time_from;
        private long time_to;
        private long interval;
        private int unit;
        private String upperLimit;
        private String lowerLimit;
        private String time_end;
        private String time_start;
        private String day_start;
        private String day_end;
        
        private String exprAdaptorId;
        
        private String hashcode;
        
        public Expression(String exprStr) {
            for (String op : OPERATORS) {
                if (exprStr.contains(op)) {
                    String[] exprs = exprStr.split(op);
                    this.arg = exprs[0].trim();

                    // suport * as a match
                    initMatchArgExpr();

                    this.operator = op;
                    this.expectedValue = exprs[1];
                    break;
                }
            }
            this.type = Type.STREAM;
        }

        public Expression(String exprStr, String func, Long range, Float sampling) {
            this(exprStr);
            if (range != null && range > 0) {
                this.range = range * 1000; // second to ms
            }
            this.func = func;
            if (sampling != null) {
                this.sampling = sampling;
            }

        }

        public Expression(Map<String, Object> cond) {

            this.arg = (String) cond.get("metric");
            
            this.exprAdaptorId = arg.substring(0,arg.indexOf('.'));
            
            this.unit = Integer.parseInt((String) cond.get("unit"));
            
            this.time_from = DateTimeHelper
                    .dateFormat(DateTimeHelper.getToday("yyyy-MM-dd") + " " + cond.get("time_from"), "yyyy-MM-dd HH:mm")
                    .getTime();
            this.time_to = DateTimeHelper
                    .dateFormat(DateTimeHelper.getToday("yyyy-MM-dd") + " " + cond.get("time_to"), "yyyy-MM-dd HH:mm")
                    .getTime();
            
            this.time_start=(String) cond.get("time_start");
            
            this.time_end= (String) cond.get("time_end");
            
            this.day_start=(String) cond.get("day_start");
            
            this.day_end= (String) cond.get("day_end");
            
            if(cond.containsKey("weekdayLimit")) {
                ((JSONArray)cond.get("weekdayLimit")).toArray(this.weekdayLimit);
            }       
            
            if (cond.get("interval") != null) {
                long interval = Long.parseLong((String) cond.get("interval"));
                switch (unit) {
                    case DateTimeHelper.INTERVAL_DAY:
                        interval = interval * 24 * 3600 * 1000;
                        break;
                    case DateTimeHelper.INTERVAL_HOUR:
                        interval = interval * 3600 * 1000;
                        break;
                    case DateTimeHelper.INTERVAL_MINUTE:
                        interval = interval * 60000;
                        break;
                }
                this.interval = interval;
            }

            this.upperLimit = (String) cond.get("upperLimit");
            this.lowerLimit = (String) cond.get("lowerLimit");
            this.func = (String) cond.get("aggr");
            this.downsample=(String) cond.get("downsample");
            this.type = Type.TIMER;
        }

        private void initMatchArgExpr() {

            if (this.arg.indexOf("*") > -1) {
                String[] tmps = this.arg.split("\\*");
                for (String tmp : tmps) {
                    matchArgExpr.add(tmp);
                }
            }
        }

        public boolean isMatchExpr() {

            return matchArgExpr.size() > 0;
        }

        public Set<String> matchTargetArgs(Set<String> srcArgs) {

            Set<String> targetArgs = new HashSet<String>();

            for (String arg : srcArgs) {

                int matchCount = 0;
                for (String matchField : this.matchArgExpr) {

                    if (arg.indexOf(matchField) > -1) {
                        matchCount++;
                    }
                }

                if (matchCount == this.matchArgExpr.size()) {
                    targetArgs.add(arg);
                }
            }

            return targetArgs;
        }

        public String getHashCode() {

            if(this.hashcode == null) {
                hashcode = EncodeHelper.encodeMD5(arg + func + downsample + lowerLimit + upperLimit + time_from + time_to + interval + unit + time_start + time_end + day_start + day_end);
            }
            
            return hashcode;
        }

        public String getArg() {

            return arg;
        }

        public String getOperator() {

            return operator;
        }

        public String getExpectedValue() {

            return expectedValue;
        }

        public long getRange() {

            return range;
        }

        public String getFunc() {

            return func;
        }

        public float getSampling() {

            return sampling;
        }

        public int getIdx() {

            return idx;
        }

        public Type getType() {

            return type;
        }

        public long getTime_from() {

            return time_from;
        }

        public long getTime_to() {

            return time_to;
        }

        public long getInterval() {

            return interval;
        }

        public String getUpperLimit() {

            return upperLimit;
        }

        public String getLowerLimit() {

            return lowerLimit;
        }

        public int getUnit() {

            return unit;
        }

        public void setIdx(int idx) {

            this.idx = idx;
        }
        
        public String getTime_end() {

            return time_end;
        }

        public String getTime_start() {

            return time_start;
        }

        public String getDownsample() {

            return downsample;
        }

        public String getDay_start() {

            return day_start;
        }

        public String getDay_end() {

            return day_end;
        }

        public Boolean[] getWeekdayLimit() {

            return weekdayLimit;
        }
        
        public String getExprAdaptorId() {
            
            return exprAdaptorId;
        }

    }

    protected class Condition {

        private int index;
        private List<Expression> expressions;
        private String relation;

        public Condition(int index, Expression expr) {
            this.index = index;
            List<Expression> exprs = new ArrayList<>(1);
            exprs.add(expr);
            this.expressions = exprs;
        }

        public Condition(int index, List<Expression> exprs, String relation) {
            this.index = index;
            this.expressions = exprs;
            this.relation = relation;
        }

        public int getIndex() {

            return index;
        }

        public List<Expression> getExpressions() {

            return expressions;
        }

        public String getRelation() {

            return relation;
        }

    }

}
