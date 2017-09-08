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

package com.creditease.monitor.jee.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.creditease.monitor.interceptframework.InterceptSupport;
import com.creditease.monitor.interceptframework.spi.InterceptConstants;
import com.creditease.monitor.interceptframework.spi.InterceptContext;
import com.creditease.monitor.interceptframework.spi.InterceptContext.Event;

/**
 * 
 * GlobalFilter description: helps to do a lot of things
 *
 */
public class GlobalFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        InterceptSupport iSupport = InterceptSupport.instance();

        /**
         * Step 1: request
         */
        InterceptContext context = iSupport.createInterceptContext(Event.GLOBAL_FILTER_REQUEST);
        context.put(InterceptConstants.HTTPREQUEST, request);
        context.put(InterceptConstants.HTTPRESPONSE, response);
        context.put(InterceptConstants.FILTERCHAIN, chain);
        iSupport.doIntercept(context);

        // NOTE: get the response, it is a chance for global filter handler to replace response object
        HttpServletResponse tmpResponse = (HttpServletResponse) context.get(InterceptConstants.HTTPRESPONSE);
        // NOTE: get the request, it is a chance for global filter handler to replace request object
        HttpServletRequest tmpRequest = (HttpServletRequest) context.get(InterceptConstants.HTTPREQUEST);

        /**
         * check if get the stop request token,if yes then stop the request
         */
        Object token = context.get(InterceptConstants.STOPREQUEST);

        if (token == null) {
            /**
             * Step 2: run service
             */
            try {
                chain.doFilter(tmpRequest, tmpResponse);
            }
            catch (Throwable e) {
                inteceptResponse(iSupport, context);
                throw new RuntimeException(e);
            }
        }

        /**
         * Step 3: response
         */
        inteceptResponse(iSupport, context);
    }

    private void inteceptResponse(InterceptSupport iSupport, InterceptContext context) {

        InterceptContext respContext = iSupport.createInterceptContext(Event.GLOBAL_FILTER_RESPONSE);
        respContext.putAll(context.getAll());

        iSupport.doIntercept(respContext);
    }

    @Override
    public void destroy() {

    }

}
