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

package com.creditease.monitorframework.fat.invokechain;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import com.creditease.monitorframework.fat.client.TestService;
import com.creditease.monitorframework.fat.client.TestService_Service;

/**
 * 用来测试web service
 *
 */
@Singleton
@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Path("ws")
public class WSService {
    
    private final String SERVICEURL = "http://localhost:8080/com.creditease.uav.monitorframework.buildFat/ws/TestService";

    @GET
    @Path("test")
    public String test() {

        TestService_Service s = new TestService_Service();
        TestService ts = s.getTestServicePort();

        // 设置客户端的配置信息，超时等.
        Client proxy = ClientProxy.getClient(ts);
        HTTPConduit conduit = (HTTPConduit) proxy.getConduit();
        HTTPClientPolicy policy = new HTTPClientPolicy();

        // 连接服务器超时时间
        policy.setConnectionTimeout(30000);
        // 等待服务器响应超时时间
        policy.setReceiveTimeout(30000);

        conduit.setClient(policy);

        ts.echo();
        return "web service perfect";
    }
    
    @GET
    @Path("testwsclient")
    public String testWsClient() {
        
        JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setAddress(this.SERVICEURL);
        factoryBean.setServiceClass(TestService.class);
        factoryBean.getInInterceptors().add(new LoggingInInterceptor());

        TestService TestServicePort = (TestService) factoryBean.create();

        return TestServicePort.echo();
        
    }
    
    @GET
    @Path("testwsdispatch")
    public String testWsDispatch() {
        String namespace = "http://service.fat.monitorframework.creditease.com/";
        String wsdlUrl = SERVICEURL+"?wsdl";
    
      //1、创建服务(Service)
        URL url = null;
        try {
            url = new URL(wsdlUrl);
        }
        catch (MalformedURLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        QName qname = new QName(namespace,"TestService");
        Service service = Service.create(url, qname);
        
        //2、创建Dispatch
        //public interface Dispatch<T>extends BindingProviderDispatch 接口提供对动态调用服务端点操作的支持。javax.xml.ws.Service 接口作为创建 Dispatch 实例的工厂。 
        Dispatch<SOAPMessage> dispatch = service.createDispatch(new QName(namespace,"TestServicePort"), SOAPMessage.class, Service.Mode.MESSAGE);
        
        //3、创建SOAPMessage
        SOAPMessage message = null;
        try {
            MessageFactory factory = MessageFactory.newInstance();
            message = factory.createMessage();
            SOAPPart part = message.getSOAPPart();
            SOAPEnvelope envelope = part.getEnvelope();
            SOAPBody body = envelope.getBody();
            QName portQname = new QName(namespace, 
                    "echo");
            
                body.addBodyElement(portQname);
        }
        catch (SOAPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        //4、通过Dispatch传递消息,并返回响应消息
        SOAPMessage returnMessage = dispatch.invoke(message);
        try {
            returnMessage.writeTo(System.out);;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        //打印返回消息
        System.out.println();
        
        return "dispatch";
        
    }
}
