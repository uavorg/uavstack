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

package com.creditease.uav.apm.uem;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.creditease.agent.helpers.StringHelper;

public class RewriteResponseWrapper extends HttpServletResponseWrapper {

    private RewritePrintWriter dWriter;

    private RewriteOutputStream out;

    private boolean isChar = false;

    private HttpServletResponse response;

    private StringBuilder charBuffer = new StringBuilder();

    private String tag;

    public RewriteResponseWrapper(HttpServletResponse response, String tag) {
        super(response);

        this.response = response;

        out = new RewriteOutputStream();
        dWriter = new RewritePrintWriter(out);
        this.tag = tag;
    }

    public String getTag() {

        return tag;
    }

    @Override
    public PrintWriter getWriter() throws IOException {

        return this.dWriter;
    }

    @Override
    public ServletOutputStream getOutputStream() {

        return out;
    }

    public StringBuilder getContent() {

        String encoding = this.getCharacterEncoding();

        if (StringHelper.isEmpty(encoding)) {
            try {
                encoding = this.getContentType();
                String[] eInfo = encoding.split(";")[1].split("=");
                encoding = eInfo[1];
            }
            catch (Exception e) {
                encoding = "utf-8";
            }
        }

        if ("iso-8859-1".equalsIgnoreCase(encoding)) {
            encoding = "utf-8";
        }

        try {
            response.setCharacterEncoding(encoding);
            response.setContentType("text/html;charset=" + encoding);
            charBuffer.append(out.getOutputStream().toString(encoding));
        }
        catch (UnsupportedEncodingException e) {

        }
        this.isChar = true;
        return charBuffer;
    }

    public void doRealFlush() {

        try {
            if (isChar == true) {

                PrintWriter pw = response.getWriter();
                pw.write(charBuffer.toString());
                pw.flush();
                pw.close();
            }
            else {

                OutputStream os = response.getOutputStream();

                out.getOutputStream().writeTo(os);

                os.flush();
                os.close();
            }
        }
        catch (Exception e) {
            // ignore
        }
        finally {

            try {
                out.pFlush();
                out.pClose();
            }
            catch (Exception e) {
                // ignore
            }
        }

    }

    public void flush() {

    }

    public void close() throws IOException {

        // dWriter.close();
    }

    public RewritePrintWriter getDefaultPrintWriter() {

        return this.dWriter;
    }
}
