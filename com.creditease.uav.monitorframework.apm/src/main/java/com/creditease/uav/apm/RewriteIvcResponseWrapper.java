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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class RewriteIvcResponseWrapper extends HttpServletResponseWrapper {

    private RewriteIvcOutputStream rewriteOutStream;

    private RewriteIvcPrintWriter rewritePrintWriter;

    private StringBuilder builder = new StringBuilder();

    private String tag;

    private HttpServletResponse response;

    public RewriteIvcResponseWrapper(HttpServletResponse response, String tag) {
        super(response);
        this.tag = tag;
        this.response = response;
    }

    public String getTag() {

        return tag;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {

        if (this.rewriteOutStream == null) {
            try {
                this.rewriteOutStream = new RewriteIvcOutputStream(response.getOutputStream(),
                        response.getCharacterEncoding());
            }
            catch (IOException e) {
                // 出现异常时则将异常信息放入
                builder.append(e.toString());
                throw e;
            }
        }

        return this.rewriteOutStream;
    }

    public StringBuilder getContent() {

        if (rewriteOutStream != null) {
            return this.rewriteOutStream.getContent();
        }
        else if (rewritePrintWriter != null) {
            return this.rewritePrintWriter.getContent();
        }
        else if (this.builder != null) {
            return this.builder;
        }

        return new StringBuilder();

    }

    /**
     * 清空池，方便回收
     */
    public void clearBodyContent() {

        if (rewriteOutStream != null) {
            this.rewriteOutStream.clearBodyContent();
        }
        else if (rewritePrintWriter != null) {
            this.rewritePrintWriter.clearBodyContent();
        }
        else {
            this.builder.delete(0, this.builder.length());
        }
    }

    @Override
    public PrintWriter getWriter() throws IOException {

        if (this.rewritePrintWriter == null) {
            try {
                this.rewritePrintWriter = new RewriteIvcPrintWriter(this.response.getWriter());
            }
            catch (IOException e) {
                builder.append(e.toString());
                throw e;
            }
        }
        return rewritePrintWriter;
    }

}