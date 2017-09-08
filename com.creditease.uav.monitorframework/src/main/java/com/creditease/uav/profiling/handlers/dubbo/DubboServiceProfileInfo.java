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

package com.creditease.uav.profiling.handlers.dubbo;

import java.util.ArrayList;
import java.util.List;

public class DubboServiceProfileInfo {

    public static class Protocol {

        private Integer port;
        private String pName;
        private String contextpath;
        private String serialization;
        private String charset;
        
        public String getContextpath() {
        
            return contextpath;
        }

        
        public void setContextpath(String contextpath) {
        
            this.contextpath = contextpath;
        }

        
        public String getSerialization() {
        
            return serialization;
        }

        
        public void setSerialization(String serialization) {
        
            this.serialization = serialization;
        }

        
        public String getCharset() {
        
            return charset;
        }

        
        public void setCharset(String charset) {
        
            this.charset = charset;
        }

        public Integer getPort() {

            return port;
        }

        public void setPort(Integer port) {

            this.port = port;
        }

        public String getpName() {

            return pName;
        }

        public void setpName(String pName) {

            this.pName = pName;
        }

    }

    private String appId;

    private String dbAppId;

    private String serviceClass;
    
    private String group;
    
    private String version;

    private List<Protocol> protocols = new ArrayList<Protocol>();

    public void addProtocol(Protocol p) {

        this.protocols.add(p);
    }

    public String getAppId() {

        return appId;
    }

    public void setAppId(String appId) {

        this.appId = appId;
    }

    public String getDbAppId() {

        return dbAppId;
    }

    public void setDbAppId(String dbAppId) {

        this.dbAppId = dbAppId;
    }

    public String getServiceClass() {

        return serviceClass;
    }

    public void setServiceClass(String serviceClass) {

        this.serviceClass = serviceClass;
    }

    public List<Protocol> getProtocols() {

        return protocols;
    }

    public void setProtocols(List<Protocol> protocols) {

        this.protocols = protocols;
    }

    
    public String getGroup() {
    
        return group;
    }

    
    public void setGroup(String group) {
    
        this.group = group;
    }

    
    public String getVersion() {
    
        return version;
    }

    
    public void setVersion(String version) {
    
        this.version = version;
    }

}
