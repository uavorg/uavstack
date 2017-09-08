package com.creditease.uav.monitorframework.springbootFat;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

@WebServlet("/servlet")
public class MyServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -7242365332853851830L;

    @Override
    public void init(ServletConfig config) throws ServletException {

        // TODO Auto-generated method stub

    }

    @Override
    public ServletConfig getServletConfig() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {

        res.getWriter().write("priorety");
        res.flushBuffer();
    }

    @Override
    public String getServletInfo() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void destroy() {

        // TODO Auto-generated method stub

    }

}
