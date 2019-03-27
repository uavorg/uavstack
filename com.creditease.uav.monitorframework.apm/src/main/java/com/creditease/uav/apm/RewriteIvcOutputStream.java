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

import javax.servlet.ServletOutputStream;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.agent.helpers.StringHelper;

public class RewriteIvcOutputStream extends ServletOutputStream {

    private static final int bodyLength = DataConvertHelper
            .toInt(System.getProperty("com.creditease.uav.ivcdat.rpc.body"), 2000);

    private ServletOutputStream outputStream;

    private StringBuilder builder = new StringBuilder();

    private Boolean isWrited = false;

    private String encoding;

    public RewriteIvcOutputStream(ServletOutputStream outputStream, String encoding) {

        this.outputStream = outputStream;
        this.encoding = StringHelper.isEmpty(encoding) ? "utf-8" : encoding;
        if ("iso-8859-1".equalsIgnoreCase(encoding)) {
            this.encoding = "utf-8";
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {

        this.outputStream.write(b, off, len);
        if (!isWrited) {
            int remainderLength = getRemainderLength(len);
            if (remainderLength > 0) {
                builder.append(new String(b, off, remainderLength, encoding));
            }
        }
    }

    @Override
    public void flush() throws IOException {

        this.outputStream.flush();
    }

    @Override
    public void close() throws IOException {

        this.outputStream.close();
        isWrited = true;
    }

    @Override
    public void write(int b) throws IOException {

        this.outputStream.write(b);
        if (!isWrited && this.builder.length() < bodyLength) {
            builder.append((char) b);
        }
    }

    public StringBuilder getContent() {

        return this.builder;
    }

    /**
     * 清空池，方便回收
     */
    public void clearBodyContent() {

        this.builder.delete(0, this.builder.length());
        isWrited = true;
    }

    @Override
    public void write(byte[] b) throws IOException {

        this.outputStream.write(b);
        if (!isWrited) {
            int remainderLength = getRemainderLength(b.length);
            if (remainderLength > 0) {
                builder.append(new String(b, 0, remainderLength, encoding));
            }
        }
    }

    private int getRemainderLength(int length) {

        if (bodyLength <= this.builder.length()) {
            return 0;
        }
        int remainderLength = bodyLength - this.builder.length();
        /**
         * simply consider 1 character cost 2 bytes, it's not accurate.
         */
        return (2 * remainderLength) < length ? (2 * remainderLength) : length;
    }
}
