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

package com.creditease.uav.profiling.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * ProfileServiceMapMgr description: 管理整个JVM的应用的服务代码与url pattern的匹配关系，实现通过一个url来快速查找服务代码
 *
 */
public class ProfileServiceMapMgr {

    /**
     * 
     * ServiceURLBinding description: 分析url，建立匹配模型，提供匹配能力
     * 
     * 一般SpringMVC和JAXRS可能包含PathParam，例如/test/{id}/{attr}，那么需要确定非pathParam的部分和共有几个pathParam
     *
     */
    public class ServiceURLBinding {

        private String pathPattern;

        private int pathParamCount = 0;

        private String path;

        /**
         * 允许模糊匹配？
         */
        private boolean allowAbMatch = false;

        public ServiceURLBinding(String url, boolean allowAbMatch) {

            this.path = url;

            int pathParamStartIndex = url.indexOf("{");

            if (pathParamStartIndex > -1 && url.indexOf("}") > -1) {

                String[] pathParams = url.split("/");

                for (String pathParam : pathParams) {
                    if (pathParam.indexOf("{") > -1 && pathParam.indexOf("}") > -1) {
                        pathParamCount++;
                    }
                }

                this.pathPattern = url.substring(0, pathParamStartIndex);
            }
            else {
                this.pathPattern = url;
            }

            this.allowAbMatch = allowAbMatch;
        }

        public boolean match(String targetUrl) {

            if ("".equals(this.pathPattern)) {
                return false;
            }
            /**
             * Step 1: check if the target url contain the path pattern
             */
            int index = targetUrl.indexOf(this.pathPattern);

            if (index == -1) {
                return false;
            }

            /**
             * Step 2: if targetUrl is end with the path pattern, match
             */
            if (targetUrl.endsWith(this.pathPattern) == true) {
                return true;
            }

            /**
             * Step 3: if the targetUrl possible has path param, need check if the path param count match
             */
            String pathParamStr = targetUrl.substring(index + this.pathPattern.length());

            String[] pathParamArray = pathParamStr.split("/");

            if (pathParamArray.length == this.pathParamCount) {
                return true;
            }

            /**
             * Step 4: check if allow abMatch,if yes, as part, then return true, mostly for servlet or filter
             */
            if (this.allowAbMatch == true) {
                return true;
            }

            return false;
        }

        public String getPath() {

            return path;
        }

        public String getPathPattern() {

            return pathPattern;
        }
    }

    /**
     * 
     * ServiceMapBinding description: 每个服务的class，method与url pattern的对应关系
     *
     */
    public class ServiceMapBinding {

        private String clazz;
        private String method;
        private List<ServiceURLBinding> urlBindings = new ArrayList<ServiceURLBinding>();

        public String getClazz() {

            return clazz;
        }

        public void setClazz(String clazz) {

            this.clazz = clazz;
        }

        public String getMethod() {

            return method;
        }

        public void setMethod(String method) {

            this.method = method;
        }

        /**
         * addUrls
         * 
         * @param urls
         */
        public void addUrls(Collection<String> urls, boolean allowAbMatch) {

            for (String url : urls) {
                ServiceURLBinding sub = new ServiceURLBinding(url, allowAbMatch);
                this.urlBindings.add(sub);
            }
        }

        /**
         * match
         * 
         * @param turl
         * @return
         */
        public ServiceURLBinding match(String turl) {

            for (ServiceURLBinding sub : this.urlBindings) {
                if (sub.match(turl) == true) {
                    return sub;
                }
            }

            return null;
        }

        @Override
        public String toString() {

            return this.clazz + "." + this.method;
        }
    }

    /**
     * 
     * ServiceMap description: 包含了三层的ServiceMapBinding列表，提供按层检索对应ServiceMapBinding的能力
     *
     * 第0层：是客户代码的服务，如果这一层没有匹配，则继续第一层
     * 
     * 第1层：是Filter，如果也没有匹配，则继续第二层
     * 
     * 第2层：是Servlet
     */
    public class ServiceMap {

        List<List<ServiceMapBinding>> serviceMapLayers = new ArrayList<List<ServiceMapBinding>>();

        public ServiceMap() {
            for (int i = 0; i < 3; i++) {
                List<ServiceMapBinding> lsSMB = new ArrayList<ServiceMapBinding>();
                serviceMapLayers.add(lsSMB);
            }
        }

        /**
         * addServiceMapBinding
         * 
         * @param clazz
         * @param method
         * @param urls
         * @param layer
         */
        public void addServiceMapBinding(String clazz, String method, Collection<String> urls, int layer,
                boolean useAbMatch) {

            List<ServiceMapBinding> lsSMB = serviceMapLayers.get(layer);

            ServiceMapBinding smb = new ServiceMapBinding();

            smb.setClazz(clazz);
            smb.setMethod(method);

            if (useAbMatch == false && layer > 0) {
                useAbMatch = true;
            }

            smb.addUrls(urls, useAbMatch);

            lsSMB.add(smb);
        }

        public void addServiceMapBinding(String clazz, String method, Collection<String> urls, int layer) {

            addServiceMapBinding(clazz, method, urls, layer, false);
        }

        /**
         * searchServiceMapBinding
         * 
         * @param url
         * @return
         */
        public ServiceMapBinding searchServiceMapBinding(String url) {

            for (List<ServiceMapBinding> lsSMB : serviceMapLayers) {

                for (ServiceMapBinding smb : lsSMB) {
                    if (smb.match(url) != null) {
                        return smb;
                    }
                }
            }

            return null;
        }

        /**
         * searchServiceMapBinding
         * 
         * @param url
         * @return
         */
        public ServiceURLBinding searchServiceURLBindingh(String url) {

            for (List<ServiceMapBinding> lsSMB : serviceMapLayers) {

                for (ServiceMapBinding smb : lsSMB) {
                    ServiceURLBinding sub = smb.match(url);
                    if (sub != null) {
                        return sub;
                    }
                }
            }

            return null;
        }
    }

    private Map<String, ServiceMap> appServiceMaps = new HashMap<String, ServiceMap>();

    /**
     * addServiceMapBinding
     * 
     * @param appid
     * @param clazz
     * @param method
     * @param urls
     * @param layer
     */
    public void addServiceMapBinding(String appid, String clazz, String method, Collection<String> urls, int layer,
            boolean allowAbMatch) {

        ServiceMap sm = this.appServiceMaps.get(appid);

        if (sm == null) {
            sm = new ServiceMap();
            this.appServiceMaps.put(appid, sm);
        }

        sm.addServiceMapBinding(clazz, method, urls, layer, allowAbMatch);
    }

    public void addServiceMapBinding(String appid, String clazz, String method, Collection<String> urls, int layer) {

        addServiceMapBinding(appid, clazz, method, urls, layer, false);
    }

    /**
     * searchServiceMapBinding
     * 
     * @param appid
     * @param url
     * @return
     */
    public ServiceMapBinding searchServiceMapBinding(String appid, String url) {

        ServiceMap sm = this.appServiceMaps.get(appid);

        if (sm == null) {
            return null;
        }

        return sm.searchServiceMapBinding(url);
    }

    /**
     * searchServiceMapBinding
     * 
     * @param appid
     * @param url
     * @return
     */
    public ServiceURLBinding searchServiceURLBinding(String appid, String url) {

        ServiceMap sm = this.appServiceMaps.get(appid);

        if (sm == null) {
            return null;
        }

        return sm.searchServiceURLBindingh(url);
    }
}
