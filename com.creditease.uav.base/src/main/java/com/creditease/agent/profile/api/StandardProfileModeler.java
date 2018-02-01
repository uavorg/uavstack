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

package com.creditease.agent.profile.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.helpers.StringHelper;
import com.creditease.agent.monitor.api.MonitorDataFrame;
import com.creditease.agent.spi.AbstractBaseAction;
import com.creditease.agent.spi.ActionContext;
import com.creditease.agent.spi.IActionEngine;

/**
 * 
 * StandardProfileModeler description: standard application profiling modeler which process the profile data from MDF
 *
 */
public class StandardProfileModeler extends AbstractBaseAction {

    protected IStandardProfileModelListener listener;

    protected static final String LAZY_INFO = "@LAZY";

    public StandardProfileModeler(String cName, String feature, IActionEngine engine) {
        this(cName, feature, engine, null);
    }

    public StandardProfileModeler(String cName, String feature, IActionEngine engine,
            IStandardProfileModelListener listener) {
        super(cName, feature, engine);

        this.listener = listener;
    }

    public void setListener(IStandardProfileModelListener listener) {

        this.listener = listener;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void doAction(ActionContext context) throws Exception {

        MonitorDataFrame mdf = context.getParam(MonitorDataFrame.class);

        /**
         * onBeforeMDFModeling
         */
        this.listener.onBeforeMDFModeling(context, mdf);

        /**
         * Step 1: build all profile node info
         */
        Map<String, List<Map>> frames = mdf.getDatas();
        for (String appid : frames.keySet()) {

            /**
             * onBeforeFrameModeling
             */
            this.listener.onBeforeFrameModeling(context, mdf, appid, frames.get(appid));

            Map<String, Object> appProfile = new LinkedHashMap<String, Object>();

            /**
             * 1.1 basic info of the application
             */
            Map<String, Object> values = mdf.getElemInstValues(appid, "cpt", "webapp");

            String appurl = (String) values.get("appurl");

            /**
             * appgroup
             */
            String appgroup = "";

            if (values.containsKey("appgroup")) {
                appgroup = (String) values.get("appgroup");
            }

            /**
             * appdes from web.xml's description {org:"owner info"}
             */
            String appdes = "";

            if (values.containsKey("appdes")) {
                appdes = (String) values.get("appdes");
            }

            /**
             * appmetrics
             */
            String appmetrics = "";

            if (values.containsKey("appmetrics")) {
                appmetrics = JSONHelper.toString(values.get("appmetrics"));
            }

            /**
             * appName
             */
            String appName = (String) values.get("appname");

            /**
             * app root
             */
            String appRoot = (String) values.get("webapproot");

            /**
             * MOF metadata
             */
            String mofMeta = "";

            if (values.containsKey("mofmeta")) {
                mofMeta = JSONHelper.toString(values.get("mofmeta"));
            }

            appProfile.put("appid", appid);
            appProfile.put("ip", mdf.getIP());
            appProfile.put("svrid", mdf.getServerId());
            appProfile.put("host", mdf.getHost());
            appProfile.put("time", mdf.getTimeFlag());
            appProfile.put("appname", appName);
            appProfile.put("appdes", appdes);
            appProfile.put("appmetrics", appmetrics);
            appProfile.put("appgroup", appgroup);
            appProfile.put("webapproot", appRoot);
            appProfile.put("appurl", appurl);
            appProfile.put("mofmeta", mofMeta);

            this.listener.onAppProfileMetaCreate(context, mdf, appid, appurl, appgroup, appProfile);

            /**
             * step 1.2: cache the application clients profile data
             */
            String clientsJSONstr = createAppClientProfileData(context, mdf, appid, appurl, appgroup, appProfile);

            /**
             * Step 1.3 cache the application profile data
             */
            createAppProfileData(context, mdf, appProfile, clientsJSONstr, appid, appurl, appgroup);

            /**
             * step 1.4: cache application ip_link & access data
             */
            createAppIPLinkProfileData(context, mdf, appid, appurl, appgroup, appProfile);

            /**
             * onAfterFrameModeling
             */
            this.listener.onAfterFrameModeling(context, mdf, appid, frames.get(appid), appProfile);
        }

        /**
         * onAfterMDFModeling
         */
        this.listener.onAfterMDFModeling(context, mdf);

        context.setSucessful(true);
    }

    @Override
    public String getSuccessNextActionId() {

        return null;
    }

    @Override
    public String getFailureNextActionId() {

        return null;
    }

    @Override
    public String getExceptionNextActionId() {

        return null;
    }

    @SuppressWarnings("rawtypes")
    private String createAppClientProfileData(ActionContext context, MonitorDataFrame mdf, String appid, String appurl,
            String appgroup, Map<String, Object> appProfile) {

        List<Map> clients = mdf.getElemInstances(appid, "clients");

        long checkTime = System.currentTimeMillis();

        long expireTime = 60000;

        for (int i = 0; i < clients.size(); i++) {

            Map client = clients.get(i);

            @SuppressWarnings("unchecked")
            Map<String, Object> cVals = (Map<String, Object>) client.get("values");

            Long client_ts = (Long) cVals.get("ts");

            if (client_ts == null) {
                continue;
            }

            long client_timeout = checkTime - client_ts;

            if (client_timeout < expireTime) {
                cVals.put("state", "1");
            }
            else if (client_timeout >= expireTime && client_timeout < expireTime * 2) {
                cVals.put("state", "0");
            }
            else if (client_timeout >= expireTime * 2) {
                cVals.put("state", "-1");
            }
        }

        String clientsStr = JSONHelper.toString(clients);

        /**
         * onAppClientProfileCreate
         */
        this.listener.onAppClientProfileCreate(context, mdf, appid, appurl, appgroup, appProfile, clients);

        return clientsStr;
    }

    /**
     * cacheAppIPLinkProfileData
     * 
     * @param cm
     * @param mdf
     * @param appid
     * @param appurl
     * @param appgroup
     */
    @SuppressWarnings("rawtypes")
    private void createAppIPLinkProfileData(ActionContext context, MonitorDataFrame mdf, String appid, String appurl,
            String appgroup, Map<String, Object> appProfile) {

        List<Map> iplinks = mdf.getElemInstances(appid, "iplnk");

        /**
         * onAppIPLinkProfileCreate
         */
        this.listener.onAppIPLinkProfileCreate(context, mdf, appid, appurl, appgroup, appProfile, iplinks);

    }

    /**
     * createAppProfileData
     * 
     * @param cm
     * @param mdf
     * @param appid
     */
    private void createAppProfileData(ActionContext context, MonitorDataFrame mdf, Map<String, Object> pi,
            String clientsJSONstr, String appid, String appurl, String appgroup) {

        /**
         * client
         */
        pi.put("cpt.clients", clientsJSONstr);

        /**
         * lib
         */
        pi.put("jars.lib", LAZY_INFO);

        /**
         * logs
         */
        Map<String, Object> logs = mdf.getElemInstValues(appid, "logs", "log4j");

        pi.put("logs.log4j", JSONHelper.toString(logs));

        /**
         * components
         */
        Map<String, Set<String>> compServices = new LinkedHashMap<String, Set<String>>();

        // JEE Service Components
        buildJEEServiceComponents(mdf, pi, appid, appurl, compServices);

        // MSCP Service Components
        buildMSCPServiceComponents(mdf, pi, appid, compServices);

        // Dubbo Components
        buildDubboServiceComponents(mdf, pi, appid, compServices);

        pi.put("cpt.services", JSONHelper.toString(compServices));

        pi.put("state", "1");

        /**
         * onAppProfileCreate
         */
        this.listener.onAppProfileCreate(context, mdf, appid, appurl, appgroup, pi);

    }

    /**
     * buildDubboServiceComponents
     * 
     * @param mdf
     * @param pi
     * @param appid
     * @param compServices
     */
    @SuppressWarnings("unchecked")
    private void buildDubboServiceComponents(MonitorDataFrame mdf, Map<String, Object> pi, String appid,
            Map<String, Set<String>> compServices) {

        Map<String, Object> comps = mdf.getElemInstValues(appid, "cpt", "com.alibaba.dubbo.config.spring.ServiceBean");

        // 获取dubbo provider的ip
        String ip = mdf.getIP();
        if (comps == null || comps.size() == 0) {
            return;
        }
        pi.put("cpt.dubbo.provider", LAZY_INFO);

        for (String compName : comps.keySet()) {

            Map<String, Object> info = (Map<String, Object>) comps.get(compName);

            Set<String> compServicesURLs = compServices.get(compName);

            if (compServicesURLs == null) {
                compServicesURLs = new HashSet<String>();
                compServices.put(compName, compServicesURLs);
            }

            String group = (String) info.get("group");
            String version = (String) info.get("version");
            String servcls = (String) info.get("servcls");

            Map<String, Object> compMethodInfo = (Map<String, Object>) info.get("methods");

            if (compMethodInfo == null || compMethodInfo.size() == 0) {
                continue;
            }

            Map<String, Object> protocols = (Map<String, Object>) info.get("protocols");

            if (protocols == null || protocols.size() == 0) {
                continue;
            }

            for (String method : compMethodInfo.keySet()) {

                for (String protocol : protocols.keySet()) {

                    Map<String, Object> pAttrs = (Map<String, Object>) protocols.get(protocol);

                    Integer port = (Integer) pAttrs.get("port");
                    String path = (String) pAttrs.get("path");

                    path = (StringHelper.isEmpty(path)) ? servcls : path + "/" + servcls;

                    String url = getDubboURL(ip, group, version, method, port, protocol, path);

                    compServicesURLs.add(url);
                }
            }
        }
    }

    private String getDubboURL(String ip, String group, String version, String method, Integer localPort,
            String protocol, String path) {

        StringBuilder requestURL = new StringBuilder();

        requestURL.append(protocol).append("://").append(ip).append(":").append(localPort);

        if (!StringHelper.isEmpty(group)) {
            requestURL.append(":").append(group);
        }

        requestURL.append("/").append(path);

        if (!StringHelper.isEmpty(version)) {
            requestURL.append(".").append(version);
        }

        requestURL.append("/").append(method);

        return requestURL.toString();
    }

    /**
     * buildMSCPServiceComponents
     * 
     * @param mdf
     * @param pi
     * @param appid
     * @param compServices
     */
    @SuppressWarnings("unchecked")
    private void buildMSCPServiceComponents(MonitorDataFrame mdf, Map<String, Object> pi, String appid,
            Map<String, Set<String>> compServices) {

        // http comp
        Map<String, Object> mscpHttp = mdf.getElemInstValues(appid, "cpt",
                "com.creditease.agent.spi.AbstractBaseHttpServComponent");

        if (mscpHttp != null && mscpHttp.size() > 0) {

            pi.put("cpt.mscp.http", LAZY_INFO);

            for (String mscpCompName : mscpHttp.keySet()) {
                Map<String, Object> info = (Map<String, Object>) mscpHttp.get(mscpCompName);

                Set<String> compServicesURLs = compServices.get(mscpCompName);

                if (compServicesURLs == null) {
                    compServicesURLs = new HashSet<String>();
                    compServices.put(mscpCompName, compServicesURLs);
                }

                String httpRootPath = (String) info.get("path");

                Map<String, Object> handlers = (Map<String, Object>) info.get("handlers");

                for (String handlerName : handlers.keySet()) {

                    Map<String, Object> handlerInfo = (Map<String, Object>) handlers.get(handlerName);

                    String handlerPath = (String) handlerInfo.get("path");

                    String serviceURL = httpRootPath + handlerPath;

                    compServicesURLs.add(serviceURL);
                }
            }
        }

        // timer work
        Map<String, Object> mscpTimeWork = mdf.getElemInstValues(appid, "cpt",
                "com.creditease.agent.spi.AbstractTimerWork");
        if (mscpTimeWork != null && mscpTimeWork.size() > 0) {
            pi.put("cpt.mscp.timework", LAZY_INFO);
        }

    }

    /**
     * buildJEEServiceComponents
     * 
     * @param mdf
     * @param pi
     * @param appid
     * @param appurl
     * @param compServices
     */
    private void buildJEEServiceComponents(MonitorDataFrame mdf, Map<String, Object> pi, String appid, String appurl,
            Map<String, Set<String>> compServices) {

        Map<String, Object> servlets = mdf.getElemInstValues(appid, "cpt", "javax.servlet.annotation.WebServlet");

        // all servlet based tech
        if (servlets == null || servlets.size() == 0) {
            return;
        }
        pi.put("cpt.servlets", LAZY_INFO);

        // get servlets URLs
        Map<String, String> serviceServlets = getServletsURLs(appurl, compServices, servlets);

        Map<String, Object> jaxws = mdf.getElemInstValues(appid, "cpt", "javax.jws.WebService");

        if (jaxws != null && jaxws.size() > 0) {
            pi.put("cpt.jaxws", LAZY_INFO);
        }

        String jaxwsBaseURL = getBaseURL(serviceServlets, "jaxws");

        // get jaxws URLs
        getJAXWSURLs(compServices, jaxws, jaxwsBaseURL, appurl);

        Map<String, Object> jaxwsProviders = mdf.getElemInstValues(appid, "cpt", "javax.xml.ws.WebServiceProvider");

        if (jaxwsProviders != null && jaxwsProviders.size() > 0) {
            pi.put("cpt.jaxwsP", LAZY_INFO);

        }

        // get jaxws provider urls
        getJAXWSProviderURLs(jaxwsBaseURL, compServices, jaxwsProviders);

        Map<String, Object> jaxrs = mdf.getElemInstValues(appid, "cpt", "javax.ws.rs.Path");

        if (jaxrs != null && jaxrs.size() > 0) {
            pi.put("cpt.jaxrs", LAZY_INFO);

        }

        // get jaxrs urls
        String jaxrsBaseURL = getBaseURL(serviceServlets, "jaxrs");

        getJAXRSURLs(jaxrsBaseURL, compServices, jaxrs);

        Map<String, Object> springMVC = mdf.getElemInstValues(appid, "cpt",
                "org.springframework.stereotype.Controller");
        if (springMVC != null && springMVC.size() > 0) {
            pi.put("cpt.springmvc", LAZY_INFO);

        }

        Map<String, Object> springMVCRest = mdf.getElemInstValues(appid, "cpt",
                "org.springframework.web.bind.annotation.RestController");
        if (springMVCRest != null && springMVCRest.size() > 0) {
            pi.put("cpt.springmvcRest", LAZY_INFO);

        }

        // get spring mvc urls
        String springmvcBaseURL = getBaseURL(serviceServlets, "springmvc");

        getSpringMVCURLs(springmvcBaseURL, compServices, springMVC);

        getSpringMVCURLs(springmvcBaseURL, compServices, springMVCRest);

        // get struts2 urls
        Map<String, Object> strutsAction = mdf.getElemInstValues(appid, "cpt", "com.opensymphony.xwork2.Action");
        if (strutsAction != null && strutsAction.size() > 0) {
            pi.put("cpt.struts2", LAZY_INFO);

        }
        getStruts2URLs(appurl, compServices, strutsAction);

        // get filters and listeners
        Map<String, Object> filters = mdf.getElemInstValues(appid, "cpt", "javax.servlet.annotation.WebFilter");
        if (filters != null && filters.size() > 0) {
            pi.put("cpt.filters", LAZY_INFO);

        }

        Map<String, Object> listeners = mdf.getElemInstValues(appid, "cpt", "javax.servlet.annotation.WebListener");
        if (listeners != null && listeners.size() > 0) {
            pi.put("cpt.listeners", LAZY_INFO);

        }

    }

    /**
     * get component's BaseURL. if BaseURL is null,use "@" to represent.
     */
    private String getBaseURL(Map<String, String> serviceServlets, String compType) {

        return (serviceServlets.get(compType) == null) ? "@" : serviceServlets.get(compType);

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void getStruts2URLsByAnno(String appurl, Map<String, Set<String>> compServices,
            Map<String, Object> strutsAction, String key, Set<String> extSet) {

        Set<String> compServicesURLs = compServices.get(key);
        if (compServicesURLs == null) {
            compServicesURLs = new HashSet<String>();
            compServices.put(key, compServicesURLs);
        }

        Map<String, Object> valueMap = (Map<String, Object>) strutsAction.get(key);

        Map<String, Object> methodsMap = (Map<String, Object>) valueMap.get("methods");
        Map<String, Object> classAnnoMap = (Map<String, Object>) valueMap.get("anno");

        // get namespaces
        Set<String> namespaces = new HashSet<String>();

        if (classAnnoMap != null && classAnnoMap.size() > 0) {
            for (Map.Entry<String, Object> classAnno : classAnnoMap.entrySet()) {

                String annoKey = classAnno.getKey();
                Object annoValue = classAnno.getValue();

                if ("org.apache.struts2.convention.annotation.Namespace".equals(annoKey)) {
                    namespaces.add((String) ((Map) annoValue).get("value"));
                }

                if ("org.apache.struts2.convention.annotation.Namespaces".equals(annoKey)) {

                    List aList = (List) ((Map) annoValue).get("value");

                    for (Object obj : aList) {
                        namespaces.add((String) ((Map) obj).get("value"));
                    }
                }

            }
        }
        else {
            namespaces.add("/");
        }

        // get actions
        Set<String> actionNames = new HashSet<String>();
        if (methodsMap != null) {
            for (Map.Entry<String, Object> method : methodsMap.entrySet()) {

                Map<String, Object> map = (Map<String, Object>) method.getValue();
                if (map.get("anno") == null) {
                    continue;
                }

                Map<String, Object> methodAnnoMap = (Map<String, Object>) map.get("anno");

                for (Map.Entry<String, Object> methodAnno : methodAnnoMap.entrySet()) {

                    String annoKey = methodAnno.getKey();
                    Object annoValue = methodAnno.getValue();

                    // if have Actions annotation ignore Action annotation
                    if ("org.apache.struts2.convention.annotation.Actions".equals(annoKey)) {

                        List aList = (List) ((Map) annoValue).get("value");

                        for (Object obj : aList) {
                            actionNames.add((String) ((Map) obj).get("value"));
                        }

                        continue;
                    }

                    if ("org.apache.struts2.convention.annotation.Action".equals(annoKey)
                            && !methodAnnoMap.containsKey("org.apache.struts2.convention.annotation.Actions")) {
                        actionNames.add((String) ((Map) annoValue).get("value"));
                    }
                }
            }
        }

        Iterator<String> nsIterator = namespaces.iterator();
        while (nsIterator.hasNext()) {
            String ns = nsIterator.next();

            if (!"/".equals(ns)) {
                ns = ns + "/";
            }

            Iterator<String> actionNamesIterator = actionNames.iterator();
            while (actionNamesIterator.hasNext()) {
                String actionName = actionNamesIterator.next();

                Iterator<String> extIterator = extSet.iterator();
                while (extIterator.hasNext()) {
                    String ext = extIterator.next();
                    if (!"".equals(ext)) {
                        ext = "." + ext;
                    }
                    compServicesURLs.add(appurl.substring(0, appurl.length() - 1) + ns + actionName + ext);
                }

            }
        }
    }

    @SuppressWarnings("unchecked")
    private void getStruts2URLsByDes(String appurl, Map<String, Set<String>> compServices,
            Map<String, Object> strutsAction, String key, Set<String> extSet) {

        Set<String> compServicesURLs = compServices.get(key);
        if (compServicesURLs == null) {
            compServicesURLs = new HashSet<String>();
            compServices.put(key, compServicesURLs);
        }

        Map<String, Object> valueMap = (Map<String, Object>) strutsAction.get(key);

        // get action urls by des
        List<Object> desList = (List<Object>) (valueMap.get("des"));
        if (desList == null || desList.size() <= 0) {
            return;
        }

        Iterator<String> extIterator = extSet.iterator();
        while (extIterator.hasNext()) {
            String ext = extIterator.next();
            if (!"".equals(ext)) {
                ext = "." + ext;
            }

            for (Object desObj : desList) {
                Map<String, Object> aMap = (Map<String, Object>) desObj;

                String ns = (String) aMap.get("namespace");

                if (!"/".equals(ns)) {
                    ns = ns + "/";
                }

                compServicesURLs.add(appurl.substring(0, appurl.length() - 1) + ns + aMap.get("name") + ext);
            }
        }
    }

    private void getStruts2URLs(String appurl, Map<String, Set<String>> compServices,
            Map<String, Object> strutsAction) {

        for (Map.Entry<String, Object> entry : strutsAction.entrySet()) {

            String key = entry.getKey();

            if ("com.opensymphony.xwork2.ActionSupport".equals(key)) {
                continue;
            }

            Set<String> extSet = getStruts2URLExtension(strutsAction);
            getStruts2URLsByAnno(appurl, compServices, strutsAction, key, extSet);
            getStruts2URLsByDes(appurl, compServices, strutsAction, key, extSet);

        }

        // ignore com.opensymphony.xwork2.ActionSupport
        strutsAction.remove("com.opensymphony.xwork2.ActionSupport");
    }

    @SuppressWarnings("unchecked")
    private Set<String> getStruts2URLExtension(Map<String, Object> strutsAction) {

        Set<String> extSet = new HashSet<String>();

        for (Map.Entry<String, Object> entry : strutsAction.entrySet()) {

            String key = entry.getKey();
            Object value = entry.getValue();

            if (!"com.opensymphony.xwork2.ActionSupport".equals(key)) {
                continue;
            }

            Map<String, Object> valueMap = (Map<String, Object>) value;

            List<Object> desList = (List<Object>) valueMap.get("des");

            if (desList == null) {
                continue;
            }

            for (Object desObj : desList) {
                Map<String, Object> aMap = (Map<String, Object>) desObj;
                Object extObj = aMap.get("extension");
                if (extObj == null) {
                    continue;
                }
                /**
                 * in struts2,config extension as "do,action," means url's suffix could be "do" "action" or "".
                 * "do,action,".split(",") result is ["do","action"], "do,action,".split(",",limitnum) result
                 * is["do","action",""].
                 */
                String[] extArr = ((String) extObj).split(",", 100);

                List<String> extList = Arrays.asList(extArr);

                extSet.addAll(extList);

                return extSet;
            }
        }

        if (extSet.size() <= 0) {
            extSet.add("action");
        }

        return extSet;
    }

    /**
     * getJAXWSProviderURLs
     * 
     * @param jaxwsBaseUrl
     * @param compServices
     * @param jaxws
     */
    @SuppressWarnings("unchecked")
    private void getJAXWSProviderURLs(String jaxwsBaseUrl, Map<String, Set<String>> compServices,
            Map<String, Object> jaxws) {

        for (String compId : jaxws.keySet()) {

            Map<String, Object> compInfo = (Map<String, Object>) jaxws.get(compId);

            Set<String> compServicesURLs = compServices.get(compId);

            if (compServicesURLs == null) {
                compServicesURLs = new HashSet<String>();
                compServices.put(compId, compServicesURLs);
            }

            /**
             * NODE: provider may not support description style, so only process annotation
             */
            if (compInfo.containsKey("anno")) {

                Map<String, Object> compDesInfo = (Map<String, Object>) compInfo.get("anno");

                Map<String, Object> annoWebService = (Map<String, Object>) compDesInfo
                        .get("javax.xml.ws.WebServiceProvider");

                String serviceName = (String) annoWebService.get("serviceName");

                if (StringHelper.isEmpty(serviceName)) {

                    String[] serviceImplClsInfo = compId.split("\\.");

                    serviceName = serviceImplClsInfo[serviceImplClsInfo.length - 1] + "Service";
                }

                String serviceURL = jaxwsBaseUrl + serviceName;
                compServicesURLs.add(serviceURL);
            }
        }
    }

    /**
     * getSpringMVCURLs
     * 
     * @param springMVCBaseUrl
     * @param compServices
     * @param spring
     */
    @SuppressWarnings("unchecked")
    private void getSpringMVCURLs(String springMVCBaseUrl, Map<String, Set<String>> compServices,
            Map<String, Object> spring) {

        String realSpringMVCBaseUrl = null;
        String suffix = "";
        if (springMVCBaseUrl != null) {

            // NOTE: handle *.xxx,should remove *.xxx
            realSpringMVCBaseUrl = springMVCBaseUrl;

            int allIndex = realSpringMVCBaseUrl.indexOf("*");

            if (allIndex > -1) {

                // check if there is suffix, such as *.do
                if (allIndex != realSpringMVCBaseUrl.length() - 1) {
                    suffix = realSpringMVCBaseUrl.substring(allIndex + 1, realSpringMVCBaseUrl.length() - 1);
                }

                // get the real access path
                realSpringMVCBaseUrl = realSpringMVCBaseUrl.substring(0, allIndex);
            }
        }

        for (String compId : spring.keySet()) {

            Map<String, Object> compInfo = (Map<String, Object>) spring.get(compId);

            Set<String> compServicesURLs = compServices.get(compId);

            if (compServicesURLs == null) {
                compServicesURLs = new HashSet<String>();
                compServices.put(compId, compServicesURLs);
            }

            if (!compInfo.containsKey("anno")) {
                continue;
            }

            // get resourceClass path
            Map<String, Object> compDesInfo = (Map<String, Object>) compInfo.get("anno");

            List<String> resourceClassRelativePaths = null;

            /**
             * NOTE: RequestMapping is optional to spring mvc resource class
             */

            Map<String, Object> annoWebService = (Map<String, Object>) compDesInfo
                    .get("org.springframework.web.bind.annotation.RequestMapping");

            if (annoWebService != null) {

                resourceClassRelativePaths = (List<String>) annoWebService.get("value");
            }

            /**
             * if there is no path on resource class, we take the path as ""
             */
            if (resourceClassRelativePaths == null) {
                resourceClassRelativePaths = new ArrayList<String>();
                resourceClassRelativePaths.add("");
            }

            for (String resourceClassRelativePath : resourceClassRelativePaths) {

                resourceClassRelativePath = formatRelativePath(resourceClassRelativePath);

                // get the resource class path
                String resourceClassPath = realSpringMVCBaseUrl + resourceClassRelativePath;

                // get methods path
                Map<String, Object> compMethodInfo = (Map<String, Object>) compInfo.get("methods");

                for (String method : compMethodInfo.keySet()) {

                    Map<String, Object> methodInfo = (Map<String, Object>) compMethodInfo.get(method);

                    if (!methodInfo.containsKey("anno")) {
                        continue;
                    }

                    Map<String, Object> methodAnnoInfo = (Map<String, Object>) methodInfo.get("anno");

                    String serviceURL = formatRelativePath(resourceClassPath);

                    /**
                     * each method has Path info except only one
                     */
                    if (methodAnnoInfo.containsKey("org.springframework.web.bind.annotation.RequestMapping")) {

                        Map<String, Object> pathAnnoInfo = (Map<String, Object>) methodAnnoInfo
                                .get("org.springframework.web.bind.annotation.RequestMapping");

                        List<String> methodRelativePaths = (List<String>) pathAnnoInfo.get("value");

                        // FIX NPE
                        if (methodRelativePaths == null) {
                            compServicesURLs.add(serviceURL);
                            continue;
                        }

                        for (String methodRelativePath : methodRelativePaths) {

                            methodRelativePath = formatRelativePath(methodRelativePath);

                            String methodServiceURL;

                            if (methodRelativePath.endsWith(suffix)) {

                                methodServiceURL = formatRelativePath(serviceURL + "/" + methodRelativePath);
                            }
                            else {
                                // endwith "#" means the suffix is added from springMVCBaseUrl,should be removed while on query
                                methodServiceURL = formatRelativePath(
                                        serviceURL + "/" + methodRelativePath + suffix + "#");
                            }

                            compServicesURLs.add(methodServiceURL);
                        }
                    }
                    else {
                        compServicesURLs.add(serviceURL);
                    }
                }

            }
        }
    }

    /**
     * getJAXRSURLs
     * 
     * @param jaxrsBaseUrl
     * @param compServices
     * @param jaxrs
     */
    @SuppressWarnings("unchecked")
    private void getJAXRSURLs(String jaxrsBaseUrl, Map<String, Set<String>> compServices, Map<String, Object> jaxrs) {

        for (String compId : jaxrs.keySet()) {

            Map<String, Object> compInfo = (Map<String, Object>) jaxrs.get(compId);

            Set<String> compServicesURLs = compServices.get(compId);

            if (compServicesURLs == null) {
                compServicesURLs = new HashSet<String>();
                compServices.put(compId, compServicesURLs);
            }

            if (!compInfo.containsKey("anno")) {
                continue;
            }

            // get resourceClass path
            Map<String, Object> compDesInfo = (Map<String, Object>) compInfo.get("anno");

            Map<String, Object> annoWebService = (Map<String, Object>) compDesInfo.get("javax.ws.rs.Path");

            String resourceClassRelativePath = (String) annoWebService.get("value");

            resourceClassRelativePath = formatRelativePath(resourceClassRelativePath);

            // get the resource class path
            String resourceClassPath = jaxrsBaseUrl + resourceClassRelativePath
                    + ("".equalsIgnoreCase(resourceClassRelativePath) ? "" : "/");

            // get methods path
            Map<String, Object> compMethodInfo = (Map<String, Object>) compInfo.get("methods");

            for (String method : compMethodInfo.keySet()) {

                Map<String, Object> methodInfo = (Map<String, Object>) compMethodInfo.get(method);

                if (!methodInfo.containsKey("anno")) {
                    continue;
                }

                Map<String, Object> methodAnnoInfo = (Map<String, Object>) methodInfo.get("anno");

                String serviceURL = resourceClassPath;

                /**
                 * each method has Path info except only one
                 */
                if (methodAnnoInfo.containsKey("javax.ws.rs.Path")) {
                    Map<String, Object> pathAnnoInfo = (Map<String, Object>) methodAnnoInfo.get("javax.ws.rs.Path");

                    String methodRelativePath = (String) pathAnnoInfo.get("value");

                    methodRelativePath = formatRelativePath(methodRelativePath);

                    serviceURL += methodRelativePath;
                }
                else {
                    serviceURL = serviceURL.substring(0, serviceURL.length() - 1);
                }

                compServicesURLs.add(serviceURL);
            }
        }
    }

    /**
     * getJAXWSURLs
     * 
     * @param jaxwsBaseUrl
     * @param compServices
     * @param jaxws
     */
    @SuppressWarnings("unchecked")
    private void getJAXWSURLs(Map<String, Set<String>> compServices, Map<String, Object> jaxws, String jaxwsBaseUrl,
            String appurl) {

        for (String compId : jaxws.keySet()) {

            Map<String, Object> compInfo = (Map<String, Object>) jaxws.get(compId);

            Set<String> compServicesURLs = compServices.get(compId);

            if (compServicesURLs == null) {
                compServicesURLs = new HashSet<String>();
                compServices.put(compId, compServicesURLs);
            }

            if (compInfo.containsKey("dyn")) {

                Map<String, Object> compDesInfo = (Map<String, Object>) compInfo.get("dyn");

                String url = (String) compDesInfo.get("url");

                String serviceURL = null;

                if (StringHelper.isEmpty(url)) {

                    String[] serviceImplClsInfo = compId.split("\\.");

                    url = serviceImplClsInfo[serviceImplClsInfo.length - 1] + "Service";

                    serviceURL = jaxwsBaseUrl + url;

                }
                else if (url.startsWith("http")) {

                    serviceURL = url;

                }
                else {

                    url = this.formatRelativePath(url);
                    serviceURL = jaxwsBaseUrl + url;
                }

                compServicesURLs.add(serviceURL);

            }
            else if (compInfo.containsKey("des")) {

                Map<String, Object> compDesInfo = (Map<String, Object>) compInfo.get("des");

                String url = (String) compDesInfo.get("address");

                if (url == null) {
                    url = (String) compDesInfo.get("url-pattern");
                }

                if ("".equals(url)) {
                    url = "@UNKOWN_SERVICE";
                }

                url = this.formatRelativePath(url);
                String serviceURL = null;
                if (compDesInfo.get("address") != null) {
                    serviceURL = jaxwsBaseUrl + url;
                }
                else if (compDesInfo.get("url-pattern") != null) {
                    serviceURL = appurl + url;
                }

                compServicesURLs.add(serviceURL);
            }
            else if (compInfo.containsKey("anno")) {

                Map<String, Object> compDesInfo = (Map<String, Object>) compInfo.get("anno");

                Map<String, Object> annoWebService = (Map<String, Object>) compDesInfo.get("javax.jws.WebService");

                String serviceName = (String) annoWebService.get("serviceName");

                if (StringHelper.isEmpty(serviceName)) {

                    String[] serviceImplClsInfo = compId.split("\\.");

                    serviceName = serviceImplClsInfo[serviceImplClsInfo.length - 1] + "Service";
                }

                String serviceURL = jaxwsBaseUrl + serviceName;
                compServicesURLs.add(serviceURL);
            }
        }
    }

    /**
     * getServletsURLs
     * 
     * @param appURL
     * @param compServices
     * @param servlets
     * @return
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> getServletsURLs(String appURL, Map<String, Set<String>> compServices,
            Map<String, Object> servlets) {

        // temp store the service base url
        Map<String, String> serviceServlets = new LinkedHashMap<String, String>();

        for (String compId : servlets.keySet()) {

            Map<String, Object> compInfo = (Map<String, Object>) servlets.get(compId);

            Set<String> compServicesURLs = compServices.get(compId);

            if (compServicesURLs == null) {
                compServicesURLs = new HashSet<String>();
                compServices.put(compId, compServicesURLs);
            }

            if (compInfo.containsKey("des")) {

                Map<String, Object> compDesInfo = (Map<String, Object>) compInfo.get("des");

                List<String> urls = (List<String>) compDesInfo.get("urlPatterns");

                if (null == urls || urls.size() == 0) {
                    continue;
                }

                for (String url : urls) {

                    // remove /* for better match, will not handle *.xxxx
                    if (url.lastIndexOf("*") == url.length() - 1) {
                        url = url.substring(0, url.length() - 1);
                    }

                    url = formatRelativePath(url);

                    String serviceURL = appURL + url;

                    if (serviceURL.lastIndexOf("/") == serviceURL.length() - 1) {
                        serviceURL = serviceURL.substring(0, serviceURL.length() - 1);
                    }

                    findoutAppFrameworkServlets(serviceServlets, compInfo, serviceURL);

                    compServicesURLs.add(serviceURL);
                }
            }
            else if (compInfo.containsKey("dyn")) {

                Map<String, Object> compDesInfo = (Map<String, Object>) compInfo.get("dyn");

                List<String> urls = (List<String>) compDesInfo.get("urlPatterns");

                if (null == urls || urls.size() == 0) {
                    continue;
                }

                for (String url : urls) {

                    // remove /* for better match, will not handle *.xxxx
                    if (url.lastIndexOf("*") == url.length() - 1) {
                        url = url.substring(0, url.length() - 1);
                    }

                    url = formatRelativePath(url);

                    String serviceURL = appURL + url;

                    if (serviceURL.lastIndexOf("/") == serviceURL.length() - 1) {
                        serviceURL = serviceURL.substring(0, serviceURL.length() - 1);
                    }

                    findoutAppFrameworkServlets(serviceServlets, compInfo, serviceURL);

                    compServicesURLs.add(serviceURL);
                }
            }
            else if (compInfo.containsKey("anno")) {

                Map<String, Object> compDesInfo = (Map<String, Object>) compInfo.get("anno");

                for (String compDescId : compDesInfo.keySet()) {
                    Map<String, Object> compDescInfo = (Map<String, Object>) compDesInfo.get(compDescId);

                    List<String> urls = (List<String>) compDescInfo.get("urlPatterns");

                    if (null == urls || urls.size() == 0) {
                        urls = (List<String>) compDescInfo.get("value");
                    }

                    if (null == urls || urls.size() == 0) {
                        continue;
                    }

                    for (String url : urls) {

                        // remove /* for better match, will not handle *.xxxx
                        if (url.lastIndexOf("*") == url.length() - 1) {
                            url = url.substring(0, url.length() - 1);
                        }

                        url = formatRelativePath(url);

                        String serviceURL = appURL + url;

                        if (serviceURL.lastIndexOf("/") == serviceURL.length() - 1) {
                            serviceURL = serviceURL.substring(0, serviceURL.length() - 1);
                        }

                        findoutAppFrameworkServlets(serviceServlets, compInfo, serviceURL);

                        compServicesURLs.add(serviceURL);
                    }
                }

            }
        }

        /**
         * we should figure out which JAXWS,JAXRS engine is used NOTE: this is barely by HUMAN, not 100% right, we need
         * refine this part TODO: need refine
         */
        // add axis2
        if (serviceServlets.containsKey("axis2")) {
            serviceServlets.put("jaxws", serviceServlets.get("axis2") + "/");
        }
        else if (serviceServlets.containsKey("xfire")) {
            serviceServlets.put("jaxws", serviceServlets.get("xfire") + "/");
        }
        else if (serviceServlets.containsKey("cxf")) {

            serviceServlets.put("jaxws", serviceServlets.get("cxf") + "/");
        }
        else if (serviceServlets.containsKey("jaxws-ri")) {

            serviceServlets.put("jaxws", serviceServlets.get("jaxws-ri") + "/");
        }

        // add wink
        if (serviceServlets.containsKey("wink")) {
            serviceServlets.put("jaxrs", serviceServlets.get("wink") + "/");
        }
        // add dubbo, also a impl of jaxrs
        else if (serviceServlets.containsKey("dubbo")) {
            serviceServlets.put("jaxrs", serviceServlets.get("dubbo") + "/");
        }
        else if (serviceServlets.containsKey("jersey")) {
            serviceServlets.put("jaxrs", serviceServlets.get("jersey") + "/");
        }
        else if (serviceServlets.containsKey("cxf")) {
            serviceServlets.put("jaxrs", serviceServlets.get("cxf") + "/");
        }
        // add hession
        else if (serviceServlets.containsKey("hession")) {
            serviceServlets.put("jaxrs", serviceServlets.get("hession") + "/");
        }

        if (serviceServlets.containsKey("springmvc")) {
            serviceServlets.put("springmvc", serviceServlets.get("springmvc") + "/");
        }

        return serviceServlets;
    }

    /**
     * findoutAppFrameworkServlets
     * 
     * @param serviceServlets
     * @param compId
     * @param serviceURL
     */
    private void findoutAppFrameworkServlets(Map<String, String> serviceServlets, Map<String, Object> compInfo,
            String serviceURL) {

        if (!compInfo.containsKey("engine")) {
            return;
        }

        String engine = (String) compInfo.get("engine");

        if (engine.indexOf("cxf") > -1) {
            serviceServlets.put("cxf", serviceURL);
        }
        else if (engine.indexOf("jersey") > -1) {
            serviceServlets.put("jersey", serviceURL);
        }
        else if (engine.indexOf("springmvc") > -1) {
            serviceServlets.put("springmvc", serviceURL);
        }
        else if (engine.indexOf("jaxws-ri") > -1) {
            serviceServlets.put("jaxws-ri", serviceURL);
        }
        else if (engine.indexOf("dubbo") > -1) {
            serviceServlets.put("dubbo", serviceURL);
        }
        else if (engine.indexOf("axis2") > -1) {
            serviceServlets.put("axis2", serviceURL);
        }
        else if (engine.indexOf("xfire") > -1) {
            serviceServlets.put("xfire", serviceURL);
        }
        else if (engine.indexOf("wink") > -1) {
            serviceServlets.put("wink", serviceURL);
        }
        else if (engine.indexOf("hession") > -1) {
            serviceServlets.put("hession", serviceURL);
        }
    }

    /**
     * formatRelativePath
     * 
     * @param resourceClassRelativePath
     * @return
     */
    private String formatRelativePath(String resourceClassRelativePath) {

        // remove first /
        if (resourceClassRelativePath.indexOf("/") == 0) {

            if (resourceClassRelativePath.length() == 1) {
                resourceClassRelativePath = "";
            }
            else {
                resourceClassRelativePath = resourceClassRelativePath.substring(1);
            }
        }

        // remove last /
        if (resourceClassRelativePath.lastIndexOf("/") == resourceClassRelativePath.length() - 1) {

            if (resourceClassRelativePath.length() == 1) {
                resourceClassRelativePath = "";
            }
            else if (resourceClassRelativePath.length() > 1) {
                resourceClassRelativePath = resourceClassRelativePath.substring(0,
                        resourceClassRelativePath.length() - 1);
            }
        }
        return resourceClassRelativePath;
    }

}
