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

package com.creditease.uav.apm.invokechain.interceptors;

import java.util.HashMap;
import java.util.Map;

import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.apm.slowoper.adapter.MethodSpanAdapter;

public class IVCInjectClsIT {

    // ----------------------------------Convert All Raw Instance to Object--------------------------------------
    public static Object toObj(Object obj) {

        return obj;
    }

    public static Object toObj(boolean obj) {

        return String.valueOf(obj);
    }

    public static Object toObj(int obj) {

        return String.valueOf(obj);
    }

    public static Object toObj(double obj) {

        return String.valueOf(obj);
    }

    public static Object toObj(long obj) {

        return String.valueOf(obj);
    }

    public static Object toObj(float obj) {

        return String.valueOf(obj);
    }

    // ----------------------------------Static -------------------------------------
    /**
     * NOTE：Class级Span，目前不支持递归，其实也不打算支持，因为会导致threadlocal过大（万一有人写了1000级递归就~），所以无论多少递归都只能记录最后一次的结果
     * 
     */
    private static ThreadLocal<Map<String, IVCInjectClsIT>> tl = new ThreadLocal<Map<String, IVCInjectClsIT>>() {

        @Override
        protected Map<String, IVCInjectClsIT> initialValue() {

            return new HashMap<String, IVCInjectClsIT>();
        }
    };

    public static void start(String appid, String cls, String method, String sign, Object[] args) {

        Map<String, IVCInjectClsIT> mp = tl.get();
        IVCInjectClsIT ivc = new IVCInjectClsIT(appid, cls, method, sign);
        mp.put(cls + "." + method + "." + sign, ivc);
        ivc.before(args);
    }

    public static void end(String appid, String cls, String method, String sign, Object arg) {

        Map<String, IVCInjectClsIT> mp = tl.get();
        IVCInjectClsIT ivc = mp.remove(cls + "." + method + "." + sign);
        ivc.after(arg);
    }

    private String appid;

    private String cls;

    private String method;

    private String sign;

    private Map<String, Object> ivcContextParams = new HashMap<String, Object>();

    public IVCInjectClsIT(String appid, String cls, String method, String sign) {
        this.appid = appid;
        this.cls = cls;
        this.method = method;
        this.sign = sign;
    }

    @SuppressWarnings("unchecked")
    public void before(Object[] args) {

        if (args != null && args.length > 0) {

        }
        else {

        }

        // System.out.println(appid + "/" + cls + "." + method + "(" + this.sign + ")" + " start >>>>>>>>>>>>>");

        Map<String, Object> params = new HashMap<String, Object>();

        params.put(InvokeChainConstants.METHOD_SPAN_CLS, cls);
        params.put(InvokeChainConstants.METHOD_SPAN_MTD, method);
        params.put(InvokeChainConstants.METHOD_SPAN_APPID, appid);
        params.put(InvokeChainConstants.METHOD_SPAN_MTDSIGN, sign);

        // register adapter
        UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "registerAdapter",
                MethodSpanAdapter.class);

        ivcContextParams = (Map<String, Object>) UAVServer.instance().runSupporter(
                "com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                InvokeChainConstants.CHAIN_APP_METHOD, InvokeChainConstants.CapturePhase.PRECAP, params,
                MethodSpanAdapter.class, args);
    }

    public void after(Object retObj) {

        // System.out.println(appid + "/" + cls + "." + method + "(" + this.sign + ")" + " end>>>>>>>>>>>>>");

        Map<String, Object> params = new HashMap<String, Object>();

        int rc = -1;

        String responseState = "";

        if (retObj == null) {
            rc = 1;
        }
        else {
            if (Throwable.class.isAssignableFrom(retObj.getClass())) {

                Throwable e = (Throwable) retObj;

                responseState = e.toString();
                rc = -1;
            }
            else {
                rc = 1;
            }
        }

        params.put(InvokeChainConstants.METHOD_SPAN_CLS, cls);
        params.put(InvokeChainConstants.METHOD_SPAN_MTD, method);
        params.put(InvokeChainConstants.METHOD_SPAN_APPID, appid);
        params.put(InvokeChainConstants.METHOD_SPAN_MTDSIGN, sign);
        params.put(CaptureConstants.INFO_CLIENT_RESPONSECODE, rc);
        params.put(CaptureConstants.INFO_CLIENT_RESPONSESTATE, responseState);

        if (ivcContextParams != null) {
            ivcContextParams.putAll(params);
        }

        Object[] objs = { retObj };
        UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.InvokeChainSupporter", "runCap",
                InvokeChainConstants.CHAIN_APP_METHOD, InvokeChainConstants.CapturePhase.DOCAP, ivcContextParams,
                MethodSpanAdapter.class, objs);
    }

}
