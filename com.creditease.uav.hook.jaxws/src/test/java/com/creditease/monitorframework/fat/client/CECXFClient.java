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

package com.creditease.monitorframework.fat.client;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import javax.xml.ws.Service;

/**
 * 通用WebService客户端
 * 
 * @author zhenzhang
 *
 */
public class CECXFClient {

    private Class<?> serviceClass;
    private Class<?> portClass;
    private QName portQName;
    private int connectTimeout = 30000;
    private int receiveTimeout = 30000;
    private Map<String, Method> portMethods = new HashMap<String, Method>();

    public CECXFClient(Class<?> serviceClass, Class<?> portClass, QName portQName) {
        this.serviceClass = serviceClass;
        this.portClass = portClass;
        this.portQName = portQName;
    }

    public int getConnectTimeout() {

        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {

        this.connectTimeout = connectTimeout;
    }

    public int getReceiveTimeout() {

        return receiveTimeout;
    }

    public void setReceiveTimeout(int receiveTimeout) {

        this.receiveTimeout = receiveTimeout;
    }

    /**
     * invoke jaxws service with target URL
     * 
     * @param methodName
     * @param wsdlURL
     * @param param
     * @return
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public Object invoke(String methodName, URL wsdlURL, Object... param)
            throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {

        Constructor<?> con = serviceClass.getConstructor(new Class<?>[] { URL.class });

        Object serviceObj = con.newInstance(wsdlURL);

        return internalInvoke(methodName, serviceObj, param);
    }

    /**
     * invoke jaxws service with default URL
     * 
     * @param methodName
     * @param param
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public Object invoke(String methodName, Object... param) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {

        Object serviceObj = serviceClass.newInstance();

        return internalInvoke(methodName, serviceObj, param);
    }

    private Object internalInvoke(String methodName, Object serviceObj, Object... param)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        Object port = ((Service) serviceObj).getPort(portQName, portClass);

        configHttpConduit(port);

        StringBuilder sb = new StringBuilder(methodName);

        int paraCount = (null == param) ? 0 : param.length;

        String key = sb.append("#").append(paraCount).toString();

        Method mthd = null;

        if (!portMethods.containsKey(key)) {
            Class<?>[] paramTypes = null;

            if (null != param && param.length > 0) {

                paramTypes = new Class<?>[param.length];

                for (int i = 0; i < param.length; i++) {
                    paramTypes[i] = param[i].getClass();
                }
            }

            mthd = portClass.getMethod(methodName, paramTypes);
            portMethods.put(key, mthd);
        }
        else {
            mthd = portMethods.get(key);
        }

        return mthd.invoke(port, param);
    }

    private void configHttpConduit(Object port) {

        // 设置客户端的配置信息，超时等.
        Client proxy = ClientProxy.getClient(port);
        HTTPConduit conduit = (HTTPConduit) proxy.getConduit();
        HTTPClientPolicy policy = new HTTPClientPolicy();

        // 连接服务器超时时间
        policy.setConnectionTimeout(this.connectTimeout);
        // 等待服务器响应超时时间
        policy.setReceiveTimeout(this.receiveTimeout);

        conduit.setClient(policy);
    }

}
