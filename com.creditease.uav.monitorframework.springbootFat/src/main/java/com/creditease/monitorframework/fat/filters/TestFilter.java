package com.creditease.monitorframework.fat.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

@WebFilter("/*")
public class TestFilter implements Filter {

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain fc)
            throws IOException, ServletException {

        System.out.println("FILTER==>" + TestFilter.class.getName());
        fc.doFilter(req, resp);
    }

    @Override
    public void init(FilterConfig fc) throws ServletException {

    }

}
