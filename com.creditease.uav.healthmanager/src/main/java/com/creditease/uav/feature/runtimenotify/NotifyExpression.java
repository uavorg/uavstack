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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Deprecated
public class NotifyExpression {

    private static final String[] OPS = { ":=", "!=", ">", "<", "=" };

    private long range;
    private String func;
    private String arg; // 表达式左值原值，可能是表达式
    private String operator;
    private String expectedValue;
    private String actualValue;
    private String actualArg; // 真正的左值

    private float sampling = 1; // 采样比

    private int conditionIndex = -1; // 表达式在strategy的conditions的顺序号

    private List<String> context = Collections.emptyList();
    private Map<String, String> action = Collections.emptyMap();

    private Set<String> matchArgExpr = new HashSet<String>();

    private String msgTemplate;

    public NotifyExpression(int conditionIndex, String expr, List<String> context, Map<String, String> action) {
        for (String op : OPS) {
            if (expr.contains(op)) {
                String[] exprs = expr.split(op);
                this.arg = exprs[0].trim();

                // suport * as a match
                initMatchArgExpr();

                this.operator = op;
                this.expectedValue = exprs[1];
                break;
            }
        }
        this.conditionIndex = conditionIndex;
        if (context != null) {
            this.context = context;
        }

        if (action != null) {
            this.action = action;
        }
    }

    private void initMatchArgExpr() {

        if (this.arg.indexOf("*") > -1) {
            String[] tmps = this.arg.split("\\*");
            for (String tmp : tmps) {
                matchArgExpr.add(tmp);
            }
        }
    }

    public NotifyExpression(int conditionIndex, String expr, Long range, String func, List<String> context,
            Map<String, String> action) {
        this(conditionIndex, expr, context, action);
        this.range = (range == null) ? 0 : range * 1000;
        this.func = func;
    }

    public NotifyExpression(int conditionIndex, String arg, String operator, String expectedValue, List<String> context,
            Map<String, String> action) {

        this.arg = arg;

        initMatchArgExpr();

        this.operator = operator;
        this.expectedValue = expectedValue;

        this.conditionIndex = conditionIndex;
        if (context != null) {
            this.context = context;
        }

        if (action != null) {
            this.action = action;
        }
    }

    public NotifyExpression(int conditionIndex, String expr, Long range, String func, Float sampling,
            List<String> context, Map<String, String> action) {
        this(conditionIndex, expr, context, action);
        this.range = (range == null) ? 0 : range * 1000;
        this.func = func;
        this.sampling = (sampling == null) ? 1 : sampling;
    }

    public long getRange() {

        return range;
    }

    public void setRange(long range) {

        this.range = range;
    }

    public String getFunc() {

        return func;
    }

    public void setFunc(String func) {

        this.func = func;
    }

    public String getArg() {

        return arg;
    }

    public void setArg(String arg) {

        this.arg = arg;
    }

    public String getOperator() {

        return operator;
    }

    public void setOperator(String operator) {

        this.operator = operator;
    }

    public String getExpectedValue() {

        return expectedValue;
    }

    public void setExpectedValue(String expected) {

        this.expectedValue = expected;
    }

    public List<String> getContext() {

        return context;
    }

    public void setContext(List<String> context) {

        this.context = context;
    }

    public String getActualValue() {

        return actualValue;
    }

    public void setActualValue(String actualValue) {

        this.actualValue = actualValue;
    }

    public Map<String, String> getAction() {

        return action;
    }

    public void setAction(Map<String, String> action) {

        this.action = action;
    }

    public int getConditionIndex() {

        return conditionIndex;
    }

    public String getActualArg() {

        return actualArg;
    }

    public void setActualArg(String actualArg) {

        this.actualArg = actualArg;
    }

    public boolean isMatchExpr() {

        if (this.matchArgExpr.size() > 0) {
            return true;
        }
        return false;
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

    public float getSampling() {

        return sampling;
    }

    
    public String getMsgTemplate() {
    
        return msgTemplate;
    }

    
    public void setMsgTemplate(String msgTemplate) {
    
        this.msgTemplate = msgTemplate;
    }

}
