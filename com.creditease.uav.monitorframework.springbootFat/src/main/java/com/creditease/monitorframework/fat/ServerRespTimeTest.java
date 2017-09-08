package com.creditease.monitorframework.fat;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class ServerRespTimeTest
 */
@WebServlet("/ServerRespTimeTest")
public class ServerRespTimeTest extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * @see javax.servlet.http.HttpServlet#HttpServlet()
     */
    public ServerRespTimeTest() {
        super();
    }

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest request,
     *      javax.servlet.http.HttpServletResponse response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

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
