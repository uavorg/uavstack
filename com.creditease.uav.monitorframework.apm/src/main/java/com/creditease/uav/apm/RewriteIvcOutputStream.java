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
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.ServletOutputStream;

import com.creditease.agent.helpers.StringHelper;

public class RewriteIvcOutputStream extends ServletOutputStream {

    private ServletOutputStream outputStream;

    private StringBuilder builder = new StringBuilder();

    private AtomicBoolean isWrited = new AtomicBoolean();

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
        if (!isWrited.get()) {
            builder.append(new String(b, encoding));
        }
    }

    @Override
    public void flush() throws IOException {

        this.outputStream.flush();
        isWrited.set(true);
    }

    @Override
    public void close() throws IOException {

        this.outputStream.close();
        isWrited.set(true);
    }

    @Override
    public void write(int b) throws IOException {

        this.outputStream.write(b);
    }

    public StringBuilder getContent() {

        return this.builder;
    }

    /**
     * 清空池，方便回收
     */
    public void clearBodyContent() {

        this.builder.delete(0, this.builder.length());
        isWrited.set(true);
    }

    @Override
    public void write(byte[] b) throws IOException {

        this.outputStream.write(b);
    }
}
