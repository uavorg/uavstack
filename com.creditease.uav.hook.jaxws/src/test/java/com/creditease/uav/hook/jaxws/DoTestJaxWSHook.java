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

package com.creditease.uav.hook.jaxws;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service.Mode;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.UAVServer.ServerVendor;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.monitor.log.ConsoleLogger;
import com.creditease.monitorframework.fat.client.CECXFClient;
import com.creditease.monitorframework.fat.client.TestService;
import com.creditease.monitorframework.fat.client.TestService_Service;

public class DoTestJaxWSHook {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void main(String[] args) {

        ConsoleLogger cl = new ConsoleLogger("test");

        cl.setDebugable(true);

        UAVServer.instance().setLog(cl);

        UAVServer.instance().putServerInfo(CaptureConstants.INFO_APPSERVER_VENDOR, ServerVendor.TOMCAT);

        Map config = new HashMap();

        Map adapts = JSONHelper.toObject(
                "{\"org.apache.cxf.frontend.ClientProxy\":{\"getClient\":{args:[\"java.lang.Object\"],target:0}}}",
                Map.class);

        config.put("adapts", adapts);

        JaxWSHookProxy jp = new JaxWSHookProxy("test", config);

        jp.doInstallDProxy(null, "test");

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

        try {
            ts.echoFault();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        Dispatch<SOAPMessage> d = s.createDispatch(
                new QName("http://service.fat.monitorframework.creditease.com/", "TestServicePort"), SOAPMessage.class,
                Mode.MESSAGE);

        try {
            SOAPMessage msg = MessageFactory.newInstance().createMessage();

            d.invoke(msg);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("--------------->CECXFClient");

        CECXFClient client = new CECXFClient(TestService_Service.class, TestService.class,
                TestService_Service.TestServicePort);

        client.setConnectTimeout(30000);
        client.setReceiveTimeout(30000);

        try {
            client.invoke("echo", null);
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
