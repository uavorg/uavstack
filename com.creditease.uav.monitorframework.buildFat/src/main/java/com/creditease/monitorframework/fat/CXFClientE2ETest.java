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

package com.creditease.monitorframework.fat;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.ClientCallback;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;

import com.creditease.monitorframework.fat.client.TestService;
import com.creditease.monitorframework.fat.client.TestService_Service;

/**
 * Servlet implementation class CXFClientE2ETest
 */
public class CXFClientE2ETest extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * @see javax.servlet.http.HttpServlet#HttpServlet()
     */
    public CXFClientE2ETest() {
        super();
    }

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest request,
     *      javax.servlet.http.HttpServletResponse response)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String test = request.getParameter("test");

        if (test == null || "good".equals(test)) {
            TestService_Service service = new TestService_Service();
            TestService ts = service.getPort(TestService.class);
            ts.echo();
        }
        else if ("fault".equals(test)) {
            TestService_Service service = new TestService_Service();
            TestService ts = service.getPort(TestService.class);
            ts.echoFault();
        }
        else if ("async1".equals(test)) {

            JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
            Client client = dcf.createClient(TestService_Service.WSDL_LOCATION);

            try {
                client.invokeWrapped(new ClientCallback() {

                    @Override
                    public void handleResponse(Map<String, Object> ctx, Object[] res) {

                        super.handleResponse(ctx, res);
                    }

                    @Override
                    public void handleException(Map<String, Object> ctx, Throwable ex) {

                        super.handleException(ctx, ex);
                    }

                }, "echo");
            }
            catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        else if ("async0".equals(test)) {

            Service sr = Service.create(TestService_Service.WSDL_LOCATION, TestService_Service.SERVICE);
            JAXBContext jc = null;
            try {
                jc = JAXBContext.newInstance("com.creditease.monitorframework.fat.client");
            }
            catch (JAXBException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Dispatch d = sr.createDispatch(TestService_Service.TestServicePort, jc, Mode.PAYLOAD);

            d.invokeAsync(null, new AsyncHandler() {

                @Override
                public void handleResponse(Response res) {

                    if (res.isDone()) {
                        try {
                            System.out.println(res.get());
                        }
                        catch (InterruptedException e) {

                            e.printStackTrace();
                        }
                        catch (ExecutionException e) {

                            e.printStackTrace();
                        }
                    }

                }
            });
        }
    }

    /**
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest request,
     *      javax.servlet.http.HttpServletResponse response)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    }

}
