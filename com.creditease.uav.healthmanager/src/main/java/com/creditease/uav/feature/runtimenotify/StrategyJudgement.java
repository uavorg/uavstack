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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.creditease.agent.helpers.DateTimeHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.JsHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.http.api.UAVHttpMessage;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.agent.spi.AbstractComponent;
import com.creditease.agent.spi.AbstractSystemInvoker;
import com.creditease.agent.spi.ISystemInvokerMgr.InvokerType;
import com.creditease.uav.cache.api.CacheManager;
import com.creditease.uav.feature.RuntimeNotifyCatcher;
import com.creditease.uav.feature.runtimenotify.NotifyStrategy.Expression;

public class StrategyJudgement extends AbstractComponent {

    private static final int MIN_SAMPLING_COUNT = 3;

    private static final String TIMER_JUDGE_RESULT = "strategy.timer.result";

    private CacheManager cm;

    @SuppressWarnings("rawtypes")
    private AbstractSystemInvoker invoker;

    private String queryServiceName;

    public StrategyJudgement(String cName, String feature) {
        super(cName, feature);
        cm = (CacheManager) this.getConfigManager().getComponent(feature, RuntimeNotifyCatcher.CACHE_MANAGER_NAME);
        invoker = this.getSystemInvokerMgr().getSystemInvoker(InvokerType.HTTP);
        queryServiceName = this.getConfigManager().getFeatureConfiguration(feature, "queryservice");
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
            if (expr.getType() == NotifyStrategy.Type.TIMER) {
                judgeTimerExpression(cr, expr, cur);
            }
            else if (expr.getType() == NotifyStrategy.Type.STREAM) {
                judgeExpression(cr, expr, cur, slices);
            }
        }

        re.add(cr);
    }

    /**
     * judge stream expression
     * 
     * @param re
     * @param expr
     * @param cur
     * @param slices
     */
    private void judgeExpression(ConditionResult re, NotifyStrategy.Expression expr, Slice cur, List<Slice> slices) {

        boolean fire = false;
        String showActualValue = "";
        if (cur.getKey().indexOf('@') == -1) {
            fire = false;
            re.setIsJudgeTime(true);
            re.addExprResult(fire, expr, showActualValue);
            return;
        }
        long range = expr.getRange();
        String func = expr.getFunc();
        List<Slice> rangeSlices = rangeSlices(slices, cur, range);
        List<Slice> samplingSlices = samplingSlice(rangeSlices, expr.getSampling());
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

            if (targetArgs.size() > 1) {
                showActualValue += exprArg + oper + actualValue;
            }
            else {
                showActualValue = actualValue.toString();
            }

            /**
             * 2.4 check if match the expression
             */
            if (Function.match(actualValue, oper, expectedValue) == false) {
                continue;
            }

            fire = true;
        }
        re.setIsJudgeTime(true);
        re.addExprResult(fire, expr, showActualValue);

    }

    /**
     * * judge timer expression
     * 
     * @param re
     * @param expr
     * @param cur
     * @param slices
     */

    private void judgeTimerExpression(ConditionResult cr, NotifyStrategy.Expression expr, Slice slice) {

        // get this expr's last judgeResult from cache
        Map<String, Object> judgeResult = getJudgeResult(slice.getKey(), expr);

        if (judgeResult == null) {
            judgeResult = new HashMap<String, Object>();
            judgeResult.put("fire", false);
        }

        // if the judge is called by slice stream, just return the last result
        if (slice.getKey().indexOf('@') > -1) {
            if (judgeResult.get("time_to") != null) {
                Date date = DateTimeHelper.dateFormat(String.valueOf(judgeResult.get("time_to")), "yyyy-MM-dd HH:mm");
                // if the time is not the time expr's judge time,return false
                if (slice.getTime() - date.getTime() > 120000) {
                    judgeResult.put("fire", false);
                }
            }
        }

        // if the judge is called by timerTask
        else {
            Map<String, Long> timeMap = getJudgeTime(expr, slice.getTime());

            // if it's not the judge time ,return the last result. if the last result is out of time,return false.
            if (timeMap == null && judgeResult.get("time_to") != null
                    && isOverdue(DateTimeHelper
                            .dateFormat(String.valueOf(judgeResult.get("time_to")), "yyyy-MM-dd HH:mm").getTime(),
                            slice.getTime(), expr)) {

                judgeResult.put("fire", false);
            }
            else if (timeMap != null) {
                caculateJudgeResult(timeMap, judgeResult, slice.getKey(), expr, cr);
            }

        }

        cr.addTimerExprResult((Boolean) judgeResult.get("fire"), expr, judgeResult);
    }

    private void caculateJudgeResult(Map<String, Long> timeMap, Map<String, Object> judgeResult, String instance,
            Expression expr, ConditionResult cr) {

        // if this time's judge has been done, return the last result.
        if (judgeResult.get("time_to") != null && judgeResult.get("time_to")
                .equals(DateTimeHelper.toFormat("yyyy-MM-dd HH:mm", timeMap.get("time_to")))) {
            return;
        }

        Double currentValue = queryOpentsdb(instance, expr.getArg(), expr.getFunc(), timeMap.get("time_from"),
                timeMap.get("time_to"));
        Double lastValue = 0.0;

        // if the last judgeResult is really the last judgeTime's result, use it's currentValue as lastValue.
        if (judgeResult.get("time_to") != null
                && judgeResult.get("time_to")
                        .equals(DateTimeHelper.toFormat("yyyy-MM-dd HH:mm", timeMap.get("last_time_to")))
                && judgeResult.get("currentValue") != null) {
            lastValue = Double.parseDouble(String.valueOf(judgeResult.get("currentValue")));
        }
        else {
            lastValue = queryOpentsdb(instance, expr.getArg(), expr.getFunc(), timeMap.get("last_time_from"),
                    timeMap.get("last_time_to"));
        }

        judgeResult.put("instance", instance);

        for (String key : timeMap.keySet()) {
            judgeResult.put(key, DateTimeHelper.dateFormat(new Date(timeMap.get(key)), "yyyy-MM-dd HH:mm"));

        }
        judgeResult.put("currentValue", currentValue);
        judgeResult.put("lastValue", lastValue);

        if (currentValue == null || lastValue == null) {
            judgeResult.put("fire", false);
        }
        else {
            judgeResult.put("fire", caculate(currentValue, lastValue, expr, judgeResult));
        }

        // cache the judgeResult
        cm.putHash(RuntimeNotifyCatcher.UAV_CACHE_REGION, TIMER_JUDGE_RESULT, instance + expr.getHashCode(),
                JSONHelper.toString(judgeResult));

        // set there is a judge event
        cr.setIsJudgeTime(true);

    }

    private boolean isOverdue(long time, long currentTime, Expression expr) {

        long interval = expr.getInterval() == 0 ? 24 * 3600 * 1000 : expr.getInterval();
        return (currentTime - time > interval);
    }

    /**
     * caculate and judge if the expr's result is true.
     */
    private boolean caculate(double currentValue, double lastValue, NotifyStrategy.Expression expr,
            Map<String, Object> judgeResult) {

        boolean result = false;

        String limitString = null;

        double diff = currentValue - lastValue;

        String prefix = (diff > 0) ? "" : "-";

        diff = (diff > 0) ? diff : 0 - diff;

        limitString = (diff >= 0) ? expr.getUpperLimit() : expr.getLowerLimit();

        double limit;
        String suffix = "";
        if (limitString.endsWith("%")) {
            limit = Double.parseDouble(limitString.substring(0, limitString.length() - 1));
            diff = diff * 100 / lastValue;
            suffix = "%";
        }
        else {
            limit = Double.parseDouble(limitString);
        }

        if (!"-1".equals(limitString) && diff > limit) {
            result = true;
        }

        judgeResult.put("actualValue", prefix + String.format("%.2f", diff) + suffix);
        judgeResult.put("expectedValue", limitString);

        return result;

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Double queryOpentsdb(String instance, String metric, String func, Long startTime, Long endTime) {

        Double result = null;
        for (int i = 0; i < 3 && result == null; i++) {

            try {
                UAVHttpMessage message = new UAVHttpMessage();

                String data = String.format(
                        "{\"start\":%d,\"end\":%d,\"queries\":[{\"aggregator\":\"avg\",\"downsample\":\"%s\",\"metric\":\"%s\",\"filters\":[{\"filter\":\"%s\",\"tagk\":\"instid\",\"type\":\"regexp\",\"groupBy\":false}]}]}",
                        startTime, endTime, func, metric,
                        instance.replace(":", "/u003a").replace("%", "/u0025").replace("#", "/u0023"));
                message.putRequest("opentsdb.query.json", data);
                message.putRequest("datastore.name", MonitorDataFrame.MessageType.Monitor.toString());

                String queryResponse = String.valueOf(invoker.invoke(queryServiceName, message, String.class));

                if (queryResponse.equals("null") || queryResponse.equals("{}")
                        || queryResponse.equals("{\"rs\":\"[]\"}")) {
                    continue;
                }

                Map<Object, Object> responseMap = JSONHelper.toObject(queryResponse, Map.class);

                List<Map> rsList = JSONHelper.toObjectArray((String) responseMap.get(UAVHttpMessage.RESULT), Map.class);

                for (Object value : ((Map) rsList.get(0).get("dps")).values()) {
                    result = ((BigDecimal) value).doubleValue();
                }
            }
            catch (Exception e) {
                log.err(this, "TimerExpression judgement query opentsdb failed ", e);
            }
        }
        return result;
    }

    /**
     * get judgeResult from redis
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getJudgeResult(String instance, Expression expr) {

        if (instance.contains("@")) {
            instance = instance.substring(instance.lastIndexOf('@') + 1);
            if (instance.endsWith("_")) {
                instance = instance.substring(0, instance.length() - 1);
            }
        }
        Map<String, String> resultMap = cm.getHash(RuntimeNotifyCatcher.UAV_CACHE_REGION, TIMER_JUDGE_RESULT,
                instance + expr.getHashCode());

        String result = resultMap.get(instance + expr.getHashCode());

        return JSONHelper.toObject(result, Map.class);
    }

    /**
     * judge if this time is the expr's judge time. if it's the judge time, return the time slot and last time slot,
     * else return null;
     */
    private Map<String, Long> getJudgeTime(Expression expr, long time) {

        Map<String, Long> timeMap = new HashMap<String, Long>();
        long time_to = time;
        long time_from = time - (expr.getTime_to() - expr.getTime_from());
        timeMap.put("time_from", time_from);
        timeMap.put("time_to", time_to);
        long last_time_to;
        if (expr.getInterval() != 0) {

            if ((time_to - expr.getTime_to()) % expr.getInterval() == 0) {

                timeMap.put("last_time_from", time_from - expr.getInterval());
                timeMap.put("last_time_to", time_to - expr.getInterval());
            }
            else {
                return null;
            }
        }
        else {
            if ((time_to - expr.getTime_to()) % (24 * 3600 * 1000) == 0) {
                switch (expr.getUnit()) {
                    case DateTimeHelper.INTERVAL_DAY:

                        timeMap.put("last_time_from", time_from - 24 * 3600 * 1000);
                        timeMap.put("last_time_to", time_to - 24 * 3600 * 1000);
                        break;
                    case DateTimeHelper.INTERVAL_WEEK:

                        timeMap.put("last_time_from", time_from - 7 * 24 * 3600 * 1000);
                        timeMap.put("last_time_to", time_to - 7 * 24 * 3600 * 1000);
                        break;
                    case DateTimeHelper.INTERVAL_MONTH:

                        last_time_to = DateTimeHelper.getMonthAgo(new Date(time_to)).getTime();
                        timeMap.put("last_time_to", last_time_to);
                        timeMap.put("last_time_from", last_time_to - (time_to - time_from));
                        break;
                    case DateTimeHelper.INTERVAL_YEAR:

                        last_time_to = DateTimeHelper.getYearAgo(new Date(time_to)).getTime();
                        timeMap.put("last_time_to", last_time_to);
                        timeMap.put("last_time_from", last_time_to - (time_to - time_from));
                        break;
                }
            }
            else {
                return null;
            }

        }

        return timeMap;
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
                    if (fire && cr.isJudgeTime()) {
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

                    if (Boolean.parseBoolean(result.toString()) && cr.isJudgeTime()) {
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

            String description = "";
            if (NotifyStrategy.Type.STREAM.toString().equals(m.get("type"))) {
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
            }
            else if (NotifyStrategy.Type.TIMER.toString().equals(m.get("type"))) {

                description = ("true".equals(m.get("fire"))) ? String.format(
                        "%s在%s至%s时间段的%s值%s比%s至%s%s超过%s，当前值：%s。上期值：%s，本期值：%s", m.get("metric"), m.get("time_from"),
                        m.get("time_to"), m.get("func"), m.get("tag"), m.get("last_time_from"), m.get("last_time_to"),
                        (m.get("actualValue").contains("-")) ? "降幅" : "增幅", m.get("expectedValue"),
                        (m.get("actualValue").contains("-")) ? m.get("actualValue").substring(1) : m.get("actualValue"),
                        m.get("lastValue"), m.get("currentValue")) : "false";
            }

            return description;
        }
    }

    private class ConditionResult {

        private NotifyStrategy.Condition cond;
        private boolean[] condResult;
        private Map<String, String>[] exprResult;
        private int idx = 0;
        private boolean isJudgeTime = false;

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

        public void addTimerExprResult(boolean result, NotifyStrategy.Expression expr, Map<String, Object> reMap) {

            Map<String, String> m = new HashMap<>();
            for (String key : reMap.keySet()) {
                m.put(key, String.valueOf(reMap.get(key)));
            }

            m.put("tag", expr.getInterval() == 0 ? "同" : "环");
            m.put("metric", expr.getArg());
            m.put("func", expr.getFunc());
            m.put("type", NotifyStrategy.Type.TIMER.toString());
            addExprResult(idx++, result, m);
        }

        public void addExprResult(boolean result, NotifyStrategy.Expression expr, String actualValue) {

            Map<String, String> m = new HashMap<>();
            m.put("range", String.valueOf(expr.getRange()));
            m.put("func", expr.getFunc());
            m.put("operator", expr.getOperator());
            m.put("expectedValue", expr.getExpectedValue());
            m.put("actualArg", expr.getArg());
            m.put("actualValue", actualValue);
            m.put("type", NotifyStrategy.Type.STREAM.toString());
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

        public boolean isJudgeTime() {

            return isJudgeTime;
        }

        public void setIsJudgeTime(boolean isJudgeTime) {

            this.isJudgeTime = isJudgeTime;
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
