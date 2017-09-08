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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.JsHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.spi.AbstractComponent;

public class StrategyJudgement extends AbstractComponent {

    private static final int MIN_SAMPLING_COUNT = 3;

    public StrategyJudgement(String cName, String feature) {
        super(cName, feature);
    }

    /**
     * 
     * 
     * @param cur
     * @param stra
     * @param slices
     * @return
     */
    public Map<String, String> judge(Slice cur, NotifyStrategy stra, List<Slice> slices) {

        JudgeResult re = new JudgeResult();

        List<NotifyStrategy.Condition> conds = stra.getCondtions();
        for (NotifyStrategy.Condition cond : conds) {

            judgeCondition(re, cur, cond, slices);
        }

        return re.buildReadableResult();
    }

    /**
     * judge condition condition may have several strategy expressions and one relation expression support judge
     * expressions match relation
     * 
     * @param re
     * @param cur
     * @param cond
     * @param slices
     */
    private void judgeCondition(JudgeResult re, Slice cur, NotifyStrategy.Condition cond, List<Slice> slices) {

        List<NotifyStrategy.Expression> exprs = cond.getExpressions();
        ConditionResult cr = new ConditionResult(cond);
        for (NotifyStrategy.Expression expr : exprs) {
            judgeExpression(cr, expr, cur, slices);
        }

        re.add(cr);
    }

    /**
     * judge single expression
     * 
     * @param re
     * @param expr
     * @param cur
     * @param slices
     */
    private void judgeExpression(ConditionResult re, NotifyStrategy.Expression expr, Slice cur, List<Slice> slices) {

        boolean fire = false;

        long range = expr.getRange();
        String func = expr.getFunc();
        List<Slice> rangeSlices = rangeSlices(slices, cur, range);
        List<Slice> samplingSlices = samplingSlice(rangeSlices, expr.getSampling());

        String showActualValue = "";

        /**
         * Step 1: collect all possible arg keys for this expression
         */
        Set<String> targetArgs = new HashSet<String>();
        if (expr.isMatchExpr() == true) {
            targetArgs = expr.matchTargetArgs(cur.getArgs().keySet());
        }
        else {
            targetArgs.add(expr.getArg());
        }

        /**
         * Step 2: run every target arg key to see if need alert
         */
        Object actualValue = null;
        String oper = expr.getOperator();
        String expectedValue = expr.getExpectedValue();
        for (String exprArg : targetArgs) {

            /**
             * 2.1: if no range function, take as instantly param
             */
            if (func == null || range == 0) {
                actualValue = cur.getArgs().get(exprArg);
            }
            /**
             * 2.2: count is a special process
             * 
             * {expr:"cpu>0.9",range:30,func:"count>10"} 是说cpu>0.9 在30秒内出现了10次以上
             */
            else if (func.indexOf("count") == 0) {

                NotifyStrategy.Expression countExp = new NotifyStrategy.Expression(func);
                oper = countExp.getOperator();
                expectedValue = countExp.getExpectedValue();
                actualValue = Function.func("count", samplingSlices, exprArg, expr);
            }
            /**
             * 2.2.5: percent is a special process too, similar to 'count'
             */
            else if (func.indexOf("percent") == 0) {

                NotifyStrategy.Expression countExp = new NotifyStrategy.Expression(func);
                oper = countExp.getOperator();
                expectedValue = countExp.getExpectedValue();
                actualValue = Function.func("percent", samplingSlices, exprArg, expr);
            }
            /**
             * 2.3: if there is range function, run range function
             */
            else {
                actualValue = Function.func(func, samplingSlices, exprArg, expr);
            }

            if (actualValue == null) {
                continue;
            }

            /**
             * 2.4 check if match the expression
             */
            if (Function.match(actualValue, oper, expectedValue) == false) {
                continue;
            }

            if (targetArgs.size() > 1) {
                showActualValue += exprArg + oper + actualValue;
            }
            else {
                showActualValue = actualValue.toString();
            }
            fire = true;
        }

        re.addExprResult(fire, expr, showActualValue);
    }

    private List<Slice> rangeSlices(List<Slice> slices, Slice cur, long range) {

        List<Slice> rangeSlices = new ArrayList<Slice>();
        for (int i = 0; i < slices.size(); i++) {
            Slice slice = slices.get(i);
            long timeSpan = cur.getTime() - slice.getTime();

            if (timeSpan <= range) {
                rangeSlices.add(slice);
            }
        }
        return rangeSlices;
    }

    /**
     * 支持采样的数据切片
     * 
     * @param sampling
     *            采样比
     * @return
     */
    private List<Slice> samplingSlice(List<Slice> slices, float sampling) {

        if (slices == null) {
            return null;
        }

        int size = slices.size();

        if (sampling >= 1 || size <= MIN_SAMPLING_COUNT) {
            return slices;
        }

        int count = Math.round(sampling * size);

        if (count <= MIN_SAMPLING_COUNT) {
            count = MIN_SAMPLING_COUNT;
        }

        return slices.subList(size - count, size);
    }

    private String generateJsExpression(String jointCondition, boolean[] exprResult) {

        for (int i = 0; i < exprResult.length; i++) {
            jointCondition = jointCondition.replace("[" + i + "]", String.valueOf(exprResult[i]));
        }
        return jointCondition;
    }

    private class JudgeResult {

        private List<ConditionResult> crs = new ArrayList<>();

        public void add(ConditionResult cr) {

            crs.add(cr);
        }

        public Map<String, String> buildReadableResult() {

            if (crs.size() == 0) {
                return Collections.emptyMap();
            }

            Map<String, String> map = new LinkedHashMap<>();
            for (ConditionResult cr : crs) {

                NotifyStrategy.Condition cond = cr.getCond();
                if (StringHelper.isEmpty(cond.getRelation())) {
                    boolean fire = false;
                    for (boolean b : cr.getCondResult()) {
                        fire = fire || b;
                    }
                    if (fire) {
                        map.put(String.valueOf(cond.getIndex()), toReadable(cr.getExprResult(), null));
                    }
                }
                else {
                    String script = generateJsExpression(cond.getRelation(), cr.getCondResult());
                    Object result = JsHelper.eval(script);

                    if (result == null) {
                        if (log.isTraceEnable()) {
                            log.warn(this, "JsHelper eval Exception: script=" + script);
                        }
                        continue;
                    }

                    if (Boolean.parseBoolean(result.toString())) {
                        map.put(String.valueOf(cond.getIndex()), toReadable(cr.getExprResult(), cond.getRelation()));
                    }
                }
            }

            if (log.isDebugEnable()) {
                log.debug(this, "Judgement: FireSize=" + map.size() + ", conditions=" + JSONHelper.toString(crs));
            }

            if (log.isDebugEnable()) {
                log.debug(this, "Judgement: FireSize=" + map.size() + ", conditions=" + JSONHelper.toString(crs));
            }

            return map;
        }

        private String toReadable(Map<String, String>[] exprResult, String relation) {

            if (StringHelper.isEmpty(relation)) {
                StringBuilder sb = new StringBuilder();
                for (Map<String, String> m : exprResult) {
                    sb.append(makeReadableString(m)).append(" 或者");
                }
                sb.setLength(sb.length() - " 或者".length());
                return sb.toString();
            }
            else {
                relation = relation.replace("&&", " 并且").replace("||", " 或者");
                int i = 0;
                for (Map<String, String> m : exprResult) {
                    relation = relation.replace("[" + i + "]", makeReadableString(m));
                    i++;
                }
                return relation;
            }
        }

        private String makeReadableString(Map<String, String> m) {

            String description = null;
            long range = Long.parseLong(m.get("range"));
            if (range > 0 && m.get("func") != null) {
                // convert to seconds
                long sencodRange = range / 1000;
                description = String.format("%s秒内%s的%s值%s%s，当前值：%s", sencodRange, m.get("actualArg"), m.get("func"),
                        m.get("operator"), m.get("expectedValue"), m.get("actualValue"));
            }
            else {
                description = String.format("%s%s%s，当前值：%s", m.get("actualArg"), m.get("operator"),
                        m.get("expectedValue"), m.get("actualValue"));
            }
            return description;
        }
    }

    private class ConditionResult {

        private NotifyStrategy.Condition cond;
        private boolean[] condResult;
        private Map<String, String>[] exprResult;
        private int idx = 0;

        @SuppressWarnings("unchecked")
        private ConditionResult(NotifyStrategy.Condition cond) {
            this.cond = cond;
            int count = cond.getExpressions().size();
            condResult = new boolean[count];
            exprResult = new Map[count];
        }

        private void addExprResult(int i, boolean result, Map<String, String> reMap) {

            condResult[i] = result;
            exprResult[i] = reMap;
        }

        public void addExprResult(boolean result, NotifyStrategy.Expression expr, String actualValue) {

            Map<String, String> m = new HashMap<>();
            m.put("range", String.valueOf(expr.getRange()));
            m.put("func", expr.getFunc());
            m.put("operator", expr.getOperator());
            m.put("expectedValue", expr.getExpectedValue());
            m.put("actualArg", expr.getArg());
            m.put("actualValue", actualValue);
            addExprResult(idx++, result, m);
        }

        public NotifyStrategy.Condition getCond() {

            return cond;
        }

        public boolean[] getCondResult() {

            return condResult;
        }

        public Map<String, String>[] getExprResult() {

            return exprResult;
        }

    }

    private static class Function {

        public static boolean match(Object actual, String operator, String expected) {

            boolean result = false;
            switch (operator) {
                case ":=":
                    result = actual.toString().contains(expected);
                    break;
                case "!=":
                    result = !actual.toString().contains(expected);
                    break;
                case ">":
                    result = Float.parseFloat(actual.toString()) > Float.parseFloat(expected);
                    break;
                case "<":
                    result = Float.parseFloat(actual.toString()) < Float.parseFloat(expected);
                    break;
                case "=":
                    result = Float.parseFloat(actual.toString()) == Float.parseFloat(expected);
                    break;
                default:
                    break;
            }
            return result;
        }

        public static Float func(String funcName, List<Slice> slices, String targetArg,
                NotifyStrategy.Expression expr) {

            // valid
            if (slices == null || slices.size() == 0) {
                throw new RuntimeException("Slices is empty");
            }

            Float value = null;
            switch (funcName) {
                case "avg":
                    value = avg(slices, targetArg);
                    break;
                case "sub": // subtract
                case "subtract":
                case "minus":
                case "diff":
                    value = diff(slices, targetArg);
                    break;
                case "sum":
                    value = sum(slices, targetArg);
                    break;
                case "max":
                    value = max(slices, targetArg);
                    break;
                case "min":
                    value = min(slices, targetArg);
                    break;
                case "count":
                    value = count(slices, targetArg, expr);
                    break;
                case "percent":
                    value = percent(slices, targetArg, expr);
                    break;
                default:
                    // once
                    break;
            }
            return value;
        }

        private static float avg(List<Slice> slices, String arg) {

            float sum = 0l;
            int count = 0;
            for (Slice s : slices) {
                Object val = s.getArgs().get(arg);
                if (val != null) {
                    sum += Float.parseFloat(val.toString());
                    count++;
                }
            }

            if (count == 0) {
                return 0;
            }

            return sum / count;
        }

        private static float diff(List<Slice> slices, String arg) {

            Object val1 = slices.get(0).getArgs().get(arg);
            Object val2 = slices.get(slices.size() - 1).getArgs().get(arg);
            if (val1 == null || val2 == null) {
                return 0;
            }

            return Float.parseFloat(val2.toString()) - Float.parseFloat(val1.toString());
        }

        private static float sum(List<Slice> slices, String arg) {

            float sum = 0l;
            for (Slice s : slices) {
                Object val = s.getArgs().get(arg);
                if (val != null) {
                    sum += Float.parseFloat(val.toString());
                }
            }
            return sum;
        }

        private static float max(List<Slice> slices, String arg) {

            float max = Float.MIN_VALUE;
            for (Slice s : slices) {
                Object val = s.getArgs().get(arg);
                if (val != null) {
                    max = Math.max(max, Float.parseFloat(val.toString()));
                }
            }
            return max;
        }

        private static float min(List<Slice> slices, String arg) {

            float min = Float.MAX_VALUE;
            for (Slice s : slices) {
                Object val = s.getArgs().get(arg);
                if (val != null) {
                    min = Math.min(min, Float.parseFloat(val.toString()));
                }
            }
            return min;
        }

        private static float count(List<Slice> slices, String arg, NotifyStrategy.Expression expr) {

            int count = 0;
            for (Slice s : slices) {
                Object val = s.getArgs().get(arg);
                if (val != null) {
                    if (match(val, expr.getOperator(), expr.getExpectedValue())) {
                        count++;
                    }
                }
            }
            return count;
        }

        private static float percent(List<Slice> slices, String arg, NotifyStrategy.Expression expr) {

            float count = count(slices, arg, expr);
            return count / slices.size();
        }

    }
}
