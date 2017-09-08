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

package com.creditease.uav.apm.invokechain.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.creditease.agent.helpers.IOHelper;
import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;
import com.creditease.monitor.interceptframework.spi.InterceptEventListener;
import com.creditease.uav.apm.invokechain.interceptors.IVCInjectClsIT;
import com.creditease.uav.monitorframework.dproxy.DynamicProxyInstaller;
import com.creditease.uav.monitorframework.dproxy.DynamicProxyProcessor;
import com.creditease.uav.util.MonitorServerUtil;

import javassist.CtClass;
import javassist.CtMethod;

/**
 * 方法级的自动标注
 */
public class InvokeChainClsInjectListener extends InterceptEventListener {

    /**
     * 
     * IVCInjectProcessor description: 自动方法级标注
     *
     */
    private class IVCInjectProcessor extends DynamicProxyProcessor {

        /**
         * 
         * IVCMethodSignature description: 标记方法的签名，以及需要记录的入参和出参
         *
         */
        private class IVCMethodSignature {

            String argSignature = "";

            String inputArgs = "";

            boolean needOutArgs = false;
        }

        /**
         * 
         * IVCMethod description: 同名方法且参数相同的方法集合
         *
         */
        private class IVCMethod {

            int argCount = 0;

            List<IVCMethodSignature> signatures = new ArrayList<IVCMethodSignature>();

            String cls;
        }

        private String appid;

        private String cls;

        private Map<String, IVCMethod> methods = new HashMap<String, IVCMethod>();

        @SuppressWarnings("unchecked")
        public IVCInjectProcessor(String appid, String cls, List<Map<String, Object>> methodMap) {

            this.appid = appid;

            this.cls = cls;

            for (Map<String, Object> method : methodMap) {

                String mtdName = (String) method.get("name");
                List<String> mtdTypes = (List<String>) method.get("types");
                List<Integer> mtdArgs = (List<Integer>) method.get("args");

                if (StringHelper.isEmpty(mtdName) || mtdArgs == null) {
                    logger.warn("InvokeChainInjectCls FAIL as BadMethodMeta: appid=" + appid + ",cls=" + cls
                            + ",method=" + JSONHelper.toString(method), null);
                    continue;
                }

                /**
                 * 提取method的特征和参数个数
                 */
                StringBuilder signatureStr = new StringBuilder("");
                int argCount = 0;

                if (mtdTypes != null && mtdTypes.size() > 0) {
                    for (String mtdType : mtdTypes) {
                        signatureStr.append(mtdType + ",");
                        argCount++;
                    }
                }

                if (signatureStr.indexOf(",") > -1) {
                    signatureStr = signatureStr.deleteCharAt(signatureStr.length() - 1);
                }

                /**
                 * 以方法名+参数个数作为快速key
                 */
                String key = mtdName + "-" + argCount;

                IVCMethod mtd = methods.get(key);

                if (mtd == null) {
                    mtd = new IVCMethod();
                    mtd.cls = this.cls;
                    methods.put(key, mtd);
                }

                mtd.argCount = argCount;

                if (mtdArgs.size() > mtd.argCount + 1) {
                    logger.warn("InvokeChainInjectCls FAIL as BadMethodArgMeta: appid=" + appid + ",cls=" + cls
                            + ",method=" + JSONHelper.toString(method), null);
                    continue;
                }

                /**
                 * 提取这个方法具体的特征
                 */
                IVCMethodSignature signature = new IVCMethodSignature();

                signature.argSignature = signatureStr.toString();

                StringBuilder args = new StringBuilder();
                for (Integer arg : mtdArgs) {
                    if (arg == -1) {
                        signature.needOutArgs = true;
                    }
                    else {
                        int tmp = arg;

                        args.append("IVCInjectClsIT.toObj($" + tmp + "),");
                    }
                }

                if (args.length() > 1) {
                    signature.inputArgs = args.substring(0, args.length() - 1);
                }

                mtd.signatures.add(signature);
            }
        }

        @Override
        public void process(CtMethod m) throws Exception {

            String quickKey = m.getName() + "-" + m.getParameterTypes().length;

            if (!this.methods.containsKey(quickKey)) {
                return;
            }

            StringBuilder signature = new StringBuilder("");

            for (CtClass cc : m.getParameterTypes()) {

                signature.append(cc.getSimpleName() + ",");
            }

            if (signature.indexOf(",") > -1) {
                signature = signature.deleteCharAt(signature.length() - 1);
            }

            IVCMethod ivcmtd = this.methods.get(quickKey);

            String signatureStr = signature.toString();

            IVCMethodSignature foundSign = null;
            for (IVCMethodSignature sign : ivcmtd.signatures) {
                if (sign.argSignature.equals(signatureStr)) {
                    foundSign = sign;
                    break;
                }
            }

            if (foundSign == null) {
                return;
            }

            dpInstaller.defineLocalVal(m, "mObj", IVCInjectClsIT.class);

            String args = "null";

            if (!StringHelper.isEmpty(foundSign.inputArgs)) {
                args = "new Object[]{" + foundSign.inputArgs + "}";
            }

            m.insertBefore("{IVCInjectClsIT.start(\"" + appid + "\",\"" + ivcmtd.cls + "\",\"" + m.getName() + "\",\""
                    + foundSign.argSignature + "\"," + args + ");}");

            String rtarg = "null";

            if (foundSign.needOutArgs == true && m.getReturnType() != null) {
                rtarg = "IVCInjectClsIT.toObj($_)";
            }

            m.insertAfter("{IVCInjectClsIT.end(\"" + appid + "\",\"" + ivcmtd.cls + "\",\"" + m.getName() + "\",\""
                    + foundSign.argSignature + "\"," + rtarg + ");}");

            dpInstaller.addCatch(m, "IVCInjectClsIT.end(\"" + appid + "\",\"" + ivcmtd.cls + "\",\"" + m.getName()
                    + "\",\"" + foundSign.argSignature + "\",$e);");
        }
    }

    private boolean needIVCSupport;

    private Map<String, Map<String, Object>> injectApps = new HashMap<String, Map<String, Object>>();

    private DynamicProxyInstaller dpInstaller = new DynamicProxyInstaller();

    private Map<String, Boolean> appDone = new HashMap<String, Boolean>();

    @SuppressWarnings("unchecked")
    public InvokeChainClsInjectListener() {

        String ivcDataPath = UAVServer.instance().getServerInfo(CaptureConstants.INFO_MOF_METAPATH) + "ivc.data";

        needIVCSupport = IOHelper.exists(ivcDataPath);

        if (!needIVCSupport) {
            return;
        }

        String data = IOHelper.readTxtFile(ivcDataPath, "utf-8");

        injectApps = JSONHelper.toObject(data, Map.class);
    }

    @Override
    public boolean isEventListener(Event event) {

        switch (event) {
            case WEBCONTAINER_RESOURCE_INIT:
            case WEBCONTAINER_INIT:
                return true;
            case AFTER_SERVET_INIT:
                break;
            case BEFORE_SERVLET_DESTROY:
                break;
            case GLOBAL_FILTER_REQUEST:
                break;
            case GLOBAL_FILTER_RESPONSE:
                break;
            case WEBCONTAINER_RESOURCE_CREATE:
                break;
            case WEBCONTAINER_STARTED:
                break;
            case WEBCONTAINER_STOPPED:
                break;
            default:
                break;

        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleEvent(InterceptContext context) {

        if (!this.needIVCSupport) {
            return;
        }

        if (this.injectApps.size() == 0) {
            return;
        }

        // get the context path
        String contextpath = (String) context.get(InterceptConstants.CONTEXTPATH);
        String basepath = (String) context.get(InterceptConstants.BASEPATH);

        String appid = MonitorServerUtil.getApplicationId(contextpath, basepath);

        /**
         * NOTE: as the internal application, we will not inject it
         */
        if ("com.creditease.uav".equalsIgnoreCase(appid)) {
            return;
        }

        // not match the current application
        if (!this.injectApps.containsKey(appid)) {
            return;
        }

        Map<String, Object> ivcInfo = this.injectApps.get(appid);

        if (!ivcInfo.containsKey("classes")) {
            return;
        }

        /**
         * for one application ,only do once.
         */
        if (appDone.containsKey(appid)) {
            return;
        }

        Map<String, Map<String, Object>> classes = (Map<String, Map<String, Object>>) ivcInfo.get("classes");

        // get webapp classloader
        ClassLoader webapploader = (ClassLoader) context.get(InterceptConstants.WEBAPPLOADER);

        dpInstaller.setTargetClassLoader(webapploader);

        for (String cls : classes.keySet()) {

            Map<String, Object> clsInfo = classes.get(cls);

            List<Map<String, Object>> methods = (List<Map<String, Object>>) clsInfo.get("methods");

            if (methods == null || methods.size() == 0) {
                continue;
            }

            dpInstaller.installProxy(cls, new String[] { "com.creditease.uav.apm.invokechain.interceptors" },
                    new IVCInjectProcessor(appid, cls, methods), false);
        }

        dpInstaller.releaseTargetClassLoader();

        appDone.put(appid, true);
    }

}
