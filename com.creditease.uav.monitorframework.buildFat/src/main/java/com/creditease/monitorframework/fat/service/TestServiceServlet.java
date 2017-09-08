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

package com.creditease.monitorframework.fat.service;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.xml.ws.Endpoint;

import org.apache.cxf.transport.http.DestinationRegistry;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;

/**
 * Servlet implementation class TestServiceServlet
 */
public class TestServiceServlet extends CXFNonSpringServlet {

    private static final long serialVersionUID = 1L;

    /**
     * @see org.apache.cxf.transport.servlet.CXFNonSpringServlet#CXFNonSpringServlet()
     */
    public TestServiceServlet() {
        super();
    }

    /**
     * @see org.apache.cxf.transport.servlet.CXFNonSpringServlet#CXFNonSpringServlet(org.apache.cxf.transport.http.DestinationRegistry)
     */
    public TestServiceServlet(DestinationRegistry destinationRegistry) {
        super(destinationRegistry);
    }

    /**
     * @see org.apache.cxf.transport.servlet.CXFNonSpringServlet#CXFNonSpringServlet(org.apache.cxf.transport.http.DestinationRegistry,
     *      boolean)
     */
    public TestServiceServlet(DestinationRegistry destinationRegistry, boolean loadBus) {
        super(destinationRegistry, loadBus);
    }

    @Override
    protected void loadBus(ServletConfig sc) {

        super.loadBus(sc);

        Endpoint.publish("/TestService", new TestService());
        Endpoint.publish("/ProfileService", new ProfileServiceImpl());
    }

    @Override
    public void service(ServletRequest arg0, ServletResponse arg1) throws ServletException, IOException {

        super.service(arg0, arg1);
    }

}
