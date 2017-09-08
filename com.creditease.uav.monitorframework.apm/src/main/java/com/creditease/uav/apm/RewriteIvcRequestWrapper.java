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

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class RewriteIvcRequestWrapper extends HttpServletRequestWrapper {

    private String tag;

    private ServletInputStream inputStream;

    private RewriteIvcInputStream rewriteInputStream;

    private StringBuilder builder;

    public RewriteIvcRequestWrapper(HttpServletRequest request, String tag) {
        super(request);

        this.tag = tag;

        try {
            this.inputStream = request.getInputStream();
        }
        catch (IOException e) {
            this.builder = new StringBuilder(e.toString());
        }
        rewriteInputStream = new RewriteIvcInputStream(this.inputStream, request.getCharacterEncoding());
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {

        return this.rewriteInputStream;
    }

    @Override
    public BufferedReader getReader() throws IOException {

        return new BufferedReader(new InputStreamReader(rewriteInputStream));
    }

    public String getTag() {

        return tag;
    }

    public StringBuilder getContent() {

        if (this.builder == null) {
            return this.rewriteInputStream.getContent();
        }
        else {
            return this.builder;
        }
    }

    /**
     * 清空池，方便回收
     */
    public void clearBodyContent() {

        StringBuilder bodyContent;
        if (this.builder == null) {
            bodyContent = this.rewriteInputStream.getContent();
        }
        else {
            bodyContent = this.builder;
        }
        bodyContent.delete(0, bodyContent.length());
    }

}
