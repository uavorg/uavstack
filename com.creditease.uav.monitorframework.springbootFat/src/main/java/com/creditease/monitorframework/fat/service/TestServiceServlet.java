package com.creditease.monitorframework.fat.service;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.xml.ws.Endpoint;

import org.apache.cxf.transport.http.DestinationRegistry;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;

/**
 * Servlet implementation class TestServiceServlet
 */
@WebServlet("/TestServiceServlet")
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
