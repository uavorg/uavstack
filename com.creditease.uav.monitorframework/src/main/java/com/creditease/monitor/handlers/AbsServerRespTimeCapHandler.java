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

import java.util.concurrent.atomic.AtomicLong;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.captureframework.spi.CaptureContext;
import com.creditease.monitor.captureframework.spi.MonitorElementInstance;
import com.creditease.uav.util.MonitorServerUtil;

public class AbsServerRespTimeCapHandler {

    /**
     * this method can be reused for subclasses
     * 
     * @param context
     * @param inst
     */
    protected void recordCounters(CaptureContext context, MonitorElementInstance inst) {

        if (inst == null) {
            return;
        }

        doCommonCounters(context, inst, "ServerEndRespTime.startTime");

        String respCode = context.get(CaptureConstants.INFO_APPSERVER_CONNECTOR_RESPONSECODE).toString();
        if (respCode != null) {
            /**
             * 203: means unkown return code
             */
            int rtCode = DataConvertHelper.toInt(respCode, 203);

            /**
             * NOTE: statistics error & warn for Http
             */
            if (rtCode >= 300 && rtCode < 400) {
                inst.increValue(CaptureConstants.MEI_WARN);
            }
            
            if (rtCode >= 400) {
                inst.increValue(CaptureConstants.MEI_ERROR);
            }
            
            /**
             * NOTE: for no-http
             */
            if (rtCode == -1) {
                inst.increValue(CaptureConstants.MEI_ERROR);
                respCode = "Err";
            }
            else if (rtCode == 1) {
                respCode = "OK";
            }

            inst.increValue(CaptureConstants.MEI_RC + respCode);
        }
    }

    /**
     * doCommonCounters
     * 
     * @param context
     * @param inst
     * @param startTimeKeyInCaptureContext
     */
    protected void doCommonCounters(CaptureContext context, MonitorElementInstance inst,
            String startTimeKeyInCaptureContext) {

        long end = System.currentTimeMillis();
        Object stTimeObj = context.get(startTimeKeyInCaptureContext);

        if (stTimeObj == null) {
            return;
        }

        long st = (Long) stTimeObj;
        long respTime = end - st;

        MonitorServerUtil.doSumTimeAndCounter(inst, respTime);

        boolean isMax = inst.compareSet(CaptureConstants.MEI_RESP_MAXTIME, respTime,
                MonitorElementInstance.CompareSetOperation.MAX);

        /**
         * NOTE: tmax, tmin, 每1小时会自动RESET，避免一个巨大的tmax或一个很小的tmin
         */
        if (isMax == true) {

            inst.setValue(CaptureConstants.MEI_RESP_MAXTIME_ST, end);
        }
        else {
            Object tmaxSt = inst.getValue(CaptureConstants.MEI_RESP_MAXTIME_ST);
            if (tmaxSt == null) {
                return;
            }
            long tmax_st = (Long) tmaxSt;

            if (end - tmax_st > CaptureConstants.MEI_INST_TTL) {
                inst.setValue(CaptureConstants.MEI_RESP_MAXTIME, new AtomicLong(respTime));
                inst.setValue(CaptureConstants.MEI_RESP_MAXTIME_ST, end);
            }
        }

        boolean isMin = inst.compareSet(CaptureConstants.MEI_RESP_MINTIME, respTime,
                MonitorElementInstance.CompareSetOperation.MIN);

        if (isMin == true) {

            inst.setValue(CaptureConstants.MEI_RESP_MINTIME_ST, end);
        }
        else {
            Object tminSt = inst.getValue(CaptureConstants.MEI_RESP_MINTIME_ST);
            if (tminSt == null) {
                return;
            }
            long tmin_st = (Long) tminSt;

            if (end - tmin_st > CaptureConstants.MEI_INST_TTL) {
                inst.setValue(CaptureConstants.MEI_RESP_MINTIME, new AtomicLong(respTime));
                inst.setValue(CaptureConstants.MEI_RESP_MINTIME_ST, end);
            }
        }
    }
}
