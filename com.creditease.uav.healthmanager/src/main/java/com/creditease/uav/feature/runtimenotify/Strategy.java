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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.creditease.agent.log.api.ISystemLogger;

@Deprecated
public class Strategy {

    private static final int MIN_SAMPLING_COUNT = 3;

    private List<NotifyExpression> exprs;

    private List<NotifyExpression> causes = new ArrayList<NotifyExpression>();

    private List<Slice> rangeSlices;

    private ISystemLogger logger;

    public Strategy(List<NotifyExpression> exprs, ISystemLogger logger) {
        this.exprs = exprs;
        this.logger = logger;
    }

    public void releaseRangeSlices() {

        if (this.rangeSlices != null) {
            this.rangeSlices.clear();
            this.rangeSlices = null;
        }
    }

    public boolean dumpRangeSlices(Slice cur, RuntimeNotifySliceMgr sliceMgr) {

        /**
         * Step 1: get the max range to pull the slices
         */
        rangeSlices = null;
        long range = getRange();

        if (range > 0) {

            long st = 0;

            long start = cur.getTime() - range;
            if (logger.isDebugEnable()) {
                st = System.currentTimeMillis();
                logger.debug(this,
                        "DumpRangeSlices START: key=" + cur.getKey() + ",start=" + start + ",end=" + cur.getTime());
            }
            // rangeSlices = sliceMgr.getSlice(cur, start, cur.getTime());
            rangeSlices = sliceMgr.getSlices(cur, range);

            if (logger.isDebugEnable()) {
                st = System.currentTimeMillis() - st;
                logger.debug(this, "DumpRangeSlices END(" + st + "): key=" + cur.getKey() + ",range=" + range
                        + ",rangeSlices=" + rangeSlices.size());
            }

            if (rangeSlices.isEmpty()) {
                return false;
            }

            // if (rangeSlices.size() >= 2) {
            // long rstart = rangeSlices.get(0).getTime();
            // long rend = rangeSlices.get(rangeSlices.size() - 1).getTime();
            // if ((rend - rstart) / range < 0.75) {
            // // drop
            // return false;
            // }
            // }
        }
        else {
            rangeSlices = new ArrayList<>(1);
            rangeSlices.add(cur);
        }

        return true;
    }

    /**
     * 规则：应该是触发的所有表达式都要报警，有多少就报多少
     * 
     * @param cur
     * @param sliceMgr
     * @return
     */
    public boolean judge(Slice cur) {

        boolean isAlert = false;

        /**
         * Step 2: run expressions
         */
        for (NotifyExpression expr : exprs) {

            String func = expr.getFunc();
            Object actualValue = null;
            long expRange = expr.getRange();
            String oper = expr.getOperator();
            String expectedValue = expr.getExpectedValue();

            List<Slice> samplingSlices = samplingSlice(expr.getSampling());

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
            for (String exprArg : targetArgs) {

                /**
                 * 2.1: if no range function, take as instantly param
                 */
                if (func == null || expRange == 0) {
                    actualValue = cur.getArgs().get(exprArg);
                }
                /**
                 * 2.2: count is a special process
                 * 
                 * {expr:"cpu>0.9",range:30,func:"count>10"} 是说cpu>0.9 在30秒内出现了10次以上
                 */
                else if (func.indexOf("count") == 0) {

                    NotifyExpression countExp = new NotifyExpression(-1, func, null, null);

                    oper = countExp.getOperator();
                    expectedValue = countExp.getExpectedValue();
                    Slice last = samplingSlices.get(samplingSlices.size() - 1);
                    actualValue = func(expr, "count", samplingSlices, last, exprArg);
                }
                /**
                 * 2.2.5: percent is a special process too, similar to 'count'
                 */
                else if (func.indexOf("percent") == 0) {
                    NotifyExpression countExp = new NotifyExpression(-1, func, null, null);

                    oper = countExp.getOperator();
                    expectedValue = countExp.getExpectedValue();
                    Slice last = samplingSlices.get(samplingSlices.size() - 1);
                    actualValue = func(expr, "percent", samplingSlices, last, exprArg);
                }
                /**
                 * 2.3: if there is range function, run range function
                 */
                else {
                    Slice last = samplingSlices.get(samplingSlices.size() - 1);
                    actualValue = func(expr, func, samplingSlices, last, exprArg);
                }

                if (actualValue == null) {
                    continue;
                }

                /**
                 * 2.4 check if match the expression
                 */
                if (match(actualValue, oper, expectedValue) == false) {
                    continue;
                }

                /**
                 * 2.5 new a new alertExp to take the notification info
                 */
                NotifyExpression alertExp = new NotifyExpression(expr.getConditionIndex(), expr.getArg(),
                        expr.getOperator(), expr.getExpectedValue(), expr.getContext(), expr.getAction());

                if (func != null && expRange > 0) {
                    alertExp.setFunc(expr.getFunc());
                    alertExp.setRange(expr.getRange());
                }

                alertExp.setActualValue(actualValue.toString());
                alertExp.setActualArg(exprArg);

                alertExp.setMsgTemplate(expr.getMsgTemplate());
                causes.add(alertExp);

                isAlert = true;
            }

        }

        return isAlert;
    }

    /**
     * return all triggered expression
     * 
     * @return
     */
    public List<NotifyExpression> getCauses() {

        return this.causes;
    }

    public long getRange() {

        long max = 0;
        for (NotifyExpression expr : exprs) {
            max = Math.max(max, expr.getRange());
        }
        return max;
    }

    private boolean match(Object actual, String operator, String expected) {

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

    private Float func(NotifyExpression exp, String funcName, List<Slice> slices, Slice curSlice, String targetArg) {

        /**
         * Step 1: we should find out the right range slices for this expression
         */
        List<Slice> range = new ArrayList<Slice>();

        long exprange = exp.getRange();

        for (int i = 0; i < slices.size(); i++) {
            Slice slice = slices.get(i);
            long timeSpan = curSlice.getTime() - slice.getTime();

            if (timeSpan <= exprange) {
                range.add(slice);
            }
        }

        /**
         * Step 2: compute the range result
         */

        Float value = null;
        switch (funcName) {
            case "avg":
                value = avg(range, targetArg);
                break;
            case "sub": // subtract
            case "subtract":
            case "minus":
            case "diff":
                try {
                    value = diff(range, targetArg);
                }
                catch (Exception e) {
                    logger.err(this, "Strategy Operation[" + funcName + "] RUN FAIL.", e);
                }
                break;
            case "sum":
                value = sum(range, targetArg);
                break;
            case "max":
                value = max(range, targetArg);
                break;
            case "min":
                value = min(range, targetArg);
                break;
            case "count":
                value = count(range, exp, targetArg);
                break;
            case "percent":
                value = percent(range, exp, targetArg);
                break;
            default:
                // once
                break;
        }
        return value;
    }

    private float avg(List<Slice> slices, String arg) {

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

    private float sum(List<Slice> slices, String arg) {

        float sum = 0l;
        for (Slice s : slices) {
            Object val = s.getArgs().get(arg);
            if (val != null) {
                sum += Float.parseFloat(val.toString());
            }
        }
        return sum;
    }

    private float diff(List<Slice> slices, String arg) {

        if (slices == null || slices.size() == 0) {
            throw new RuntimeException("Slices is empty");
        }

        Object val1 = slices.get(0).getArgs().get(arg);
        Object val2 = slices.get(slices.size() - 1).getArgs().get(arg);
        if (val1 == null || val2 == null) {
            return 0;
        }

        return Float.parseFloat(val2.toString()) - Float.parseFloat(val1.toString());
    }

    /**
     * count是说满足表达式条件的次数
     * 
     * @param slices
     * @param exp
     * @param arg
     * @return
     */
    private float count(List<Slice> slices, NotifyExpression expr, String arg) {

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

    private float max(List<Slice> slices, String arg) {

        float max = Float.MIN_VALUE;
        for (Slice s : slices) {
            Object val = s.getArgs().get(arg);
            if (val != null) {
                max = Math.max(max, Float.parseFloat(val.toString()));
            }
        }
        return max;
    }

    private float min(List<Slice> slices, String arg) {

        float min = Float.MAX_VALUE;
        for (Slice s : slices) {
            Object val = s.getArgs().get(arg);
            if (val != null) {
                min = Math.min(min, Float.parseFloat(val.toString()));
            }
        }
        return min;
    }

    private float percent(List<Slice> slices, NotifyExpression expr, String arg) {

        float count = count(slices, expr, arg);
        return count / slices.size();
    }

    /**
     * 支持采样的数据切片
     * 
     * @param sampling
     *            采样比
     * @return
     */
    private List<Slice> samplingSlice(float sampling) {

        if (rangeSlices == null) {
            return null;
        }

        int size = rangeSlices.size();

        if (sampling >= 1 || size <= MIN_SAMPLING_COUNT) {
            return rangeSlices;
        }

        int count = Math.round(sampling * size);

        if (count <= MIN_SAMPLING_COUNT) {
            count = MIN_SAMPLING_COUNT;
        }

        return rangeSlices.subList(size - count, size);
    }
}
