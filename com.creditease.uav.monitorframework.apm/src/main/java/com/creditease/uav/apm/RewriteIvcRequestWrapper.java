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

package com.creditease.uav.apm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class RewriteIvcRequestWrapper extends HttpServletRequestWrapper {

    private HttpServletRequest request;
    private Map<String, String[]> parameterMap;

    private String tag;

    private RewriteIvcInputStream rewriteInputStream;

    private StringBuilder builder;

    public RewriteIvcRequestWrapper(HttpServletRequest request, String tag) {

        super(request);

        this.request = request;
        this.tag = tag;
    }

    @Override
    public String getParameter(String name) {

        initAllParameters();

        return request.getParameter(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {

        initAllParameters();

        return request.getParameterMap();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {

        return wrapServletInputStream();
    }

    @Override
    public BufferedReader getReader() throws IOException {

        return new BufferedReader(new InputStreamReader(wrapServletInputStream()));
    }

    public String getTag() {

        return tag;
    }

    public Map<String, String[]> getAllParameters() {

        if (parameterMap == null) {
            return Collections.emptyMap();
        }
        return parameterMap;
    }

    private void initAllParameters() {

        if (parameterMap == null) {
            Map<String, String[]> map = request.getParameterMap();
            parameterMap = new HashMap<String, String[]>(map);
        }
    }

    private ServletInputStream wrapServletInputStream() throws IOException {

        if (rewriteInputStream == null) {
            try {
                ServletInputStream inputStream = request.getInputStream();
                rewriteInputStream = new RewriteIvcInputStream(inputStream, request.getCharacterEncoding());
            }
            catch (IOException e) {
                builder = new StringBuilder(e.toString());
                throw e;
            }
        }

        return rewriteInputStream;
    }

    public StringBuilder getContent() {

        if (builder != null) {
            return builder;
        }

        if (rewriteInputStream == null) {
            return new StringBuilder();
        }

        return rewriteInputStream.getContent();
    }

    /**
     * 清空池，方便回收， 虽然可能并没有什么卵用
     */
    public void clearBodyContent() {

        if (builder != null || rewriteInputStream == null) {
            return;
        }

        StringBuilder bodyContent = rewriteInputStream.getContent();
        bodyContent.delete(0, bodyContent.length());
    }

}
