/*-
 * <<
 * UAVStack
 * ==
 * Copyright (C) 2016 - 2018 UAVStack
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

import java.io.PrintWriter;

import com.creditease.agent.helpers.DataConvertHelper;

public class RewriteIvcPrintWriter extends PrintWriter {

    private static final int bodyLength = DataConvertHelper
            .toInt(System.getProperty("com.creditease.uav.ivcdat.rpc.body"), 2000);

    private PrintWriter out;

    private StringBuilder builder = new StringBuilder();

    private Boolean isWrited = false;

    public RewriteIvcPrintWriter(PrintWriter out) {

        super(out);
        this.out = out;
    }

    public StringBuilder getContent() {

        return this.builder;
    }

    public void clearBodyContent() {

        this.builder.delete(0, this.builder.length());
        isWrited = true;
    }

    @Override
    public void flush() {

        super.flush();
    }

    @Override
    public void close() {

        super.close();
        isWrited = true;
    }

    @Override
    public void write(int c) {

        out.write(c);
        if (!isWrited && this.builder.length() < bodyLength) {
            this.builder.append((char) c);
        }
    }

    @Override
    public void write(char[] buf, int off, int len) {

        out.write(buf, off, len);
        if (!isWrited) {
            int remainderLength = getRemainderLength(len);
            if (remainderLength > 0) {
                this.builder.append(buf, off, remainderLength);
            }
        }
    }

    @Override
    public void write(char[] buf) {

        out.write(buf);
        if (!isWrited) {
            int remainderLength = getRemainderLength(buf.length);
            if (remainderLength > 0) {
                this.builder.append(buf, 0, remainderLength);
            }
        }
    }

    @Override
    public void write(String s, int off, int len) {

        out.write(s, off, len);
        if (!isWrited) {
            int remainderLength = getRemainderLength(len);
            if (remainderLength > 0) {
                this.builder.append(s, off, remainderLength);
            }
        }
    }

    @Override
    public void write(String s) {

        out.write(s);
        if (!isWrited) {
            int remainderLength = getRemainderLength(s.length());
            if (remainderLength > 0) {
                this.builder.append(s, 0, remainderLength);
            }
        }
    }

    private int getRemainderLength(int length) {

        if (bodyLength <= this.builder.length()) {
            return 0;
        }
        int remainderLength = bodyLength - this.builder.length();
        return remainderLength < length ? remainderLength : length;
    }
}